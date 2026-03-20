package com.portfolio.ecommerce.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ProductController.
 * Uses a real Spring context + H2 in-memory database seeded from data.sql.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ProductControllerIT {

    @Autowired
    private MockMvc mockMvc;

    // -----------------------------------------------------------------------
    // GET /api/v1/products  (paginated)
    // -----------------------------------------------------------------------

    @Test
    void getAllProducts_defaultPage_returnsTenOfEleven() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))      // default page size = 10
                .andExpect(jsonPath("$.totalElements", is(11)))     // 11 seeded products
                .andExpect(jsonPath("$.totalPages", is(2)));
    }

    @Test
    void getAllProducts_pageTwo_returnsOneProduct() throws Exception {
        mockMvc.perform(get("/api/v1/products?page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void getAllProducts_sortByPriceDesc_firstProductIsExpensive() throws Exception {
        mockMvc.perform(get("/api/v1/products?sort=price,desc"))
                .andExpect(status().isOk())
                // Ergonomic Office Chair at $349.99 is the most expensive
                .andExpect(jsonPath("$.content[0].name", is("Ergonomic Office Chair")));
    }

    // -----------------------------------------------------------------------
    // GET /api/v1/products/{id}
    // -----------------------------------------------------------------------

    @Test
    void getProductById_existing_returnsFullProduct() throws Exception {
        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Wireless Bluetooth Headphones")))
                .andExpect(jsonPath("$.price", is(89.99)))
                .andExpect(jsonPath("$.categoryName", is("Electronics")))
                .andExpect(jsonPath("$.sku", is("ELEC-001")))
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    void getProductById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    // -----------------------------------------------------------------------
    // GET /api/v1/products/search?name=...
    // -----------------------------------------------------------------------

    @Test
    void searchByName_partialMatch_returnsResults() throws Exception {
        mockMvc.perform(get("/api/v1/products/search?name=keyboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Mechanical Keyboard")));
    }

    @Test
    void searchByName_caseInsensitive_returnsResults() throws Exception {
        mockMvc.perform(get("/api/v1/products/search?name=YOGA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Yoga Mat")));
    }

    @Test
    void searchByName_noMatch_returnsEmptyPage() throws Exception {
        mockMvc.perform(get("/api/v1/products/search?name=zzznomatch"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)));
    }

    // -----------------------------------------------------------------------
    // GET /api/v1/products/category/{categoryId}
    // -----------------------------------------------------------------------

    @Test
    void getProductsByCategory_books_returnsTwoProducts() throws Exception {
        mockMvc.perform(get("/api/v1/products/category/3"))   // Books = category 3
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].categoryName", everyItem(is("Books"))));
    }

    @Test
    void getProductsByCategory_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/products/category/999"))
                .andExpect(status().isNotFound());
    }

    // -----------------------------------------------------------------------
    // GET /api/v1/products/price-range
    // -----------------------------------------------------------------------

    @Test
    void getByPriceRange_validRange_returnsMatchingProducts() throws Exception {
        mockMvc.perform(get("/api/v1/products/price-range?min=20&max=50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[*].price",
                        everyItem(allOf(greaterThanOrEqualTo(20.0), lessThanOrEqualTo(50.0)))));
    }

    @Test
    void getByPriceRange_minGreaterThanMax_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/products/price-range?min=100&max=10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Bad Request")));
    }

    // -----------------------------------------------------------------------
    // GET /api/v1/products/in-stock
    // -----------------------------------------------------------------------

    @Test
    void getInStockProducts_allSeededProductsHaveStock() throws Exception {
        mockMvc.perform(get("/api/v1/products/in-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(11)));   // all 11 seeded products have stock > 0
    }

    // -----------------------------------------------------------------------
    // POST /api/v1/products
    // -----------------------------------------------------------------------

    @Test
    void createProduct_validRequest_returns201() throws Exception {
        String json = """
                {
                  "name": "Gaming Mouse",
                  "description": "Wireless gaming mouse with 12000 DPI",
                  "price": 59.99,
                  "stockQuantity": 25,
                  "sku": "ELEC-100",
                  "categoryId": 1
                }
                """;

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Gaming Mouse")))
                .andExpect(jsonPath("$.price", is(59.99)))
                .andExpect(jsonPath("$.categoryName", is("Electronics")))
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    void createProduct_missingRequiredFields_returns422() throws Exception {
        String json = """
                {
                  "description": "No name and no price"
                }
                """;

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error", is("Validation Failed")))
                .andExpect(jsonPath("$.fieldErrors.name", notNullValue()))
                .andExpect(jsonPath("$.fieldErrors.price", notNullValue()));
    }

    @Test
    void createProduct_negativePriceField_returns422() throws Exception {
        String json = """
                {
                  "name": "Broken Product",
                  "price": -5.00
                }
                """;

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors.price", notNullValue()));
    }

    @Test
    void createProduct_duplicateSku_returns400() throws Exception {
        String json = """
                {
                  "name": "Another Headphone",
                  "price": 99.99,
                  "sku": "ELEC-001",
                  "categoryId": 1
                }
                """;

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("SKU")));
    }

    // -----------------------------------------------------------------------
    // PUT /api/v1/products/{id}
    // -----------------------------------------------------------------------

    @Test
    void updateProduct_validRequest_returnsUpdatedProduct() throws Exception {
        String json = """
                {
                  "name": "Wireless Headphones Pro",
                  "price": 109.99,
                  "stockQuantity": 40,
                  "categoryId": 1
                }
                """;

        mockMvc.perform(put("/api/v1/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Wireless Headphones Pro")))
                .andExpect(jsonPath("$.price", is(109.99)));
    }

    // -----------------------------------------------------------------------
    // DELETE /api/v1/products/{id}  (soft delete)
    // -----------------------------------------------------------------------

    @Test
    void deleteProduct_softDeletes_productNoLongerInActiveList() throws Exception {
        // Delete product 1
        mockMvc.perform(delete("/api/v1/products/1"))
                .andExpect(status().isNoContent());

        // Verify it no longer appears in the in-stock list
        mockMvc.perform(get("/api/v1/products/in-stock"))
                .andExpect(jsonPath("$", hasSize(10)));   // was 11, now 10
    }

    @Test
    void deleteProduct_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/products/999"))
                .andExpect(status().isNotFound());
    }
}
