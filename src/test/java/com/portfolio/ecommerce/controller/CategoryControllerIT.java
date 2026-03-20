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
 * Integration tests for CategoryController.
 * Runs against a real Spring context with an H2 in-memory database,
 * seeded from data.sql before each test.
 *
 * @DirtiesContext resets the application context (and therefore the H2 database)
 * before each test so that mutations from one test don't bleed into another.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class CategoryControllerIT {

    @Autowired
    private MockMvc mockMvc;

    // -----------------------------------------------------------------------
    // GET /api/v1/categories
    // -----------------------------------------------------------------------

    @Test
    void getAllCategories_returnsAllSeededCategories() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[*].name",
                        containsInAnyOrder("Electronics", "Clothing", "Books", "Home & Garden", "Sports")));
    }

    @Test
    void getAllCategories_includesProductCount() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                // Electronics has 3 seeded products
                .andExpect(jsonPath("$[?(@.name == 'Electronics')].productCount", contains(3)))
                // Sports has 2 seeded products
                .andExpect(jsonPath("$[?(@.name == 'Sports')].productCount", contains(2)));
    }

    // -----------------------------------------------------------------------
    // GET /api/v1/categories/{id}
    // -----------------------------------------------------------------------

    @Test
    void getCategoryById_existing_returnsCategory() throws Exception {
        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Electronics")))
                .andExpect(jsonPath("$.productCount", is(3)));
    }

    @Test
    void getCategoryById_notFound_returns404WithErrorBody() throws Exception {
        mockMvc.perform(get("/api/v1/categories/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("Category")));
    }

    // -----------------------------------------------------------------------
    // POST /api/v1/categories
    // -----------------------------------------------------------------------

    @Test
    void createCategory_validRequest_returns201WithBody() throws Exception {
        String json = """
                {
                  "name": "Gaming",
                  "description": "Consoles, accessories, and games"
                }
                """;

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Gaming")))
                .andExpect(jsonPath("$.description", is("Consoles, accessories, and games")))
                .andExpect(jsonPath("$.productCount", is(0)));
    }

    @Test
    void createCategory_duplicateName_returns400() throws Exception {
        String json = """
                {
                  "name": "Electronics",
                  "description": "A duplicate"
                }
                """;

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    void createCategory_blankName_returns422WithFieldError() throws Exception {
        String json = """
                {
                  "name": "",
                  "description": "No name given"
                }
                """;

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error", is("Validation Failed")))
                .andExpect(jsonPath("$.fieldErrors.name", notNullValue()));
    }

    // -----------------------------------------------------------------------
    // PUT /api/v1/categories/{id}
    // -----------------------------------------------------------------------

    @Test
    void updateCategory_validRequest_returnsUpdatedCategory() throws Exception {
        String json = """
                {
                  "name": "Consumer Electronics",
                  "description": "Updated description"
                }
                """;

        mockMvc.perform(put("/api/v1/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Consumer Electronics")));
    }

    @Test
    void updateCategory_notFound_returns404() throws Exception {
        String json = """
                {
                  "name": "Ghost Category"
                }
                """;

        mockMvc.perform(put("/api/v1/categories/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    // -----------------------------------------------------------------------
    // DELETE /api/v1/categories/{id}
    // -----------------------------------------------------------------------

    @Test
    void deleteCategory_withProducts_returns400() throws Exception {
        // Category 1 (Electronics) has products — cannot delete
        mockMvc.perform(delete("/api/v1/categories/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("products")));
    }

    @Test
    void deleteCategory_afterCreating_returnsNoContent() throws Exception {
        // Create an empty category, then delete it
        String json = """
                {
                  "name": "Temporary",
                  "description": "Will be deleted"
                }
                """;

        String response = mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Extract the new ID from the JSON response
        int newId = com.jayway.jsonpath.JsonPath.read(response, "$.id");

        mockMvc.perform(delete("/api/v1/categories/" + newId))
                .andExpect(status().isNoContent());
    }
}
