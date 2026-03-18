package com.portfolio.ecommerce.controller;

import com.portfolio.ecommerce.dto.ProductDTO;
import com.portfolio.ecommerce.dto.ProductRequestDTO;
import com.portfolio.ecommerce.service.ProductService;
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
public class ProductController {

    private final ProductService productService;

    /**
     * GET /api/v1/products
     * Returns all active products with pagination and sorting.
     *
     * Query params:
     *   page  - page number (default: 0)
     *   size  - items per page (default: 10)
     *   sort  - field to sort by, e.g. "price,asc"
     */
    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {

        Pageable pageable = buildPageable(page, size, sort);
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    /**
     * GET /api/v1/products/{id}
     * Returns a single product by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * GET /api/v1/products/category/{categoryId}
     * Returns paginated products filtered by category.
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductDTO>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId, pageable));
    }

    /**
     * GET /api/v1/products/search?name=...
     * Searches products by name (case-insensitive, partial match).
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ProductDTO>> searchByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.searchProductsByName(name, pageable));
    }

    /**
     * GET /api/v1/products/price-range?min=...&max=...
     * Returns products within a price range.
     */
    @GetMapping("/price-range")
    public ResponseEntity<List<ProductDTO>> getByPriceRange(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {

        return ResponseEntity.ok(productService.getProductsByPriceRange(min, max));
    }

    /**
     * GET /api/v1/products/in-stock
     * Returns all products with stock > 0.
     */
    @GetMapping("/in-stock")
    public ResponseEntity<List<ProductDTO>> getInStockProducts() {
        return ResponseEntity.ok(productService.getInStockProducts());
    }

    /**
     * POST /api/v1/products
     * Creates a new product.
     */
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductRequestDTO requestDTO) {
        ProductDTO created = productService.createProduct(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/v1/products/{id}
     * Fully updates an existing product.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDTO requestDTO) {
        return ResponseEntity.ok(productService.updateProduct(id, requestDTO));
    }

    /**
     * DELETE /api/v1/products/{id}
     * Soft-deletes a product (marks as inactive).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
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
