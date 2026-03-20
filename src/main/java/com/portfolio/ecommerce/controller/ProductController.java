package com.portfolio.ecommerce.controller;

import com.portfolio.ecommerce.dto.ProductDTO;
import com.portfolio.ecommerce.dto.ProductRequestDTO;
import com.portfolio.ecommerce.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Browse, search, and manage products in the catalog")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "List all active products",
               description = "Returns a paginated list of active products. Supports sorting by any field.\n\n" +
                             "**Examples:** `?sort=price,asc` · `?page=1&size=5` · `?sort=name,desc`")
    @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field and direction, e.g. price,asc", example = "id,asc")
            @RequestParam(defaultValue = "id,asc") String[] sort) {

        Pageable pageable = buildPageable(page, size, sort);
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    @Operation(summary = "Get product by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product found"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(
            @Parameter(description = "Product ID", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @Operation(summary = "Get products by category",
               description = "Returns paginated products filtered by category ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Products retrieved"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductDTO>> getProductsByCategory(
            @Parameter(description = "Category ID", example = "1") @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId, pageable));
    }

    @Operation(summary = "Search products by name",
               description = "Case-insensitive partial-match search. Example: `?name=key` matches 'Mechanical Keyboard'.")
    @ApiResponse(responseCode = "200", description = "Search results")
    @GetMapping("/search")
    public ResponseEntity<Page<ProductDTO>> searchByName(
            @Parameter(description = "Search term", example = "headphone") @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.searchProductsByName(name, pageable));
    }

    @Operation(summary = "Filter products by price range",
               description = "Returns all active products with price between `min` and `max` (inclusive).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Products within price range"),
        @ApiResponse(responseCode = "400", description = "min is greater than max")
    })
    @GetMapping("/price-range")
    public ResponseEntity<List<ProductDTO>> getByPriceRange(
            @Parameter(description = "Minimum price", example = "20.00") @RequestParam BigDecimal min,
            @Parameter(description = "Maximum price", example = "100.00") @RequestParam BigDecimal max) {

        return ResponseEntity.ok(productService.getProductsByPriceRange(min, max));
    }

    @Operation(summary = "List in-stock products",
               description = "Returns all active products with stockQuantity > 0.")
    @ApiResponse(responseCode = "200", description = "In-stock products")
    @GetMapping("/in-stock")
    public ResponseEntity<List<ProductDTO>> getInStockProducts() {
        return ResponseEntity.ok(productService.getInStockProducts());
    }

    @Operation(summary = "Create a new product",
               security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Product created"),
        @ApiResponse(responseCode = "400", description = "SKU already exists"),
        @ApiResponse(responseCode = "401", description = "JWT token required"),
        @ApiResponse(responseCode = "422", description = "Validation failed")
    })
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductRequestDTO requestDTO) {
        ProductDTO created = productService.createProduct(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update a product",
               description = "Fully replaces all fields. Include all required fields in the request body.",
               security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product updated"),
        @ApiResponse(responseCode = "400", description = "SKU conflict"),
        @ApiResponse(responseCode = "401", description = "JWT token required"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @Parameter(description = "Product ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody ProductRequestDTO requestDTO) {
        return ResponseEntity.ok(productService.updateProduct(id, requestDTO));
    }

    @Operation(summary = "Soft-delete a product",
               description = "Marks the product as `active = false`. It won't appear in listings but is preserved in the database.",
               security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Product deactivated"),
        @ApiResponse(responseCode = "401", description = "JWT token required"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID", example = "1") @PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // --- Helpers ---

    private Pageable buildPageable(int page, int size, String[] sort) {
        Sort.Direction direction = (sort.length > 1 && sort[1].equalsIgnoreCase("desc"))
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(direction, sort[0]));
    }
}
