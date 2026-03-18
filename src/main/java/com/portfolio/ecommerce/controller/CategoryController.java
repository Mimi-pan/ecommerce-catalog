package com.portfolio.ecommerce.controller;

import com.portfolio.ecommerce.dto.CategoryDTO;
import com.portfolio.ecommerce.dto.CategoryRequestDTO;
import com.portfolio.ecommerce.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * GET /api/v1/categories
     * Returns all categories with their product count.
     */
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    /**
     * GET /api/v1/categories/{id}
     * Returns a single category by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    /**
     * POST /api/v1/categories
     * Creates a new category.
     */
    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryRequestDTO requestDTO) {
        CategoryDTO created = categoryService.createCategory(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/v1/categories/{id}
     * Fully updates an existing category.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequestDTO requestDTO) {
        return ResponseEntity.ok(categoryService.updateCategory(id, requestDTO));
    }

    /**
     * DELETE /api/v1/categories/{id}
     * Deletes a category (only if it has no products).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
