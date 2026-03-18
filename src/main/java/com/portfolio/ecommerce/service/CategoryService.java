package com.portfolio.ecommerce.service;

import com.portfolio.ecommerce.dto.CategoryDTO;
import com.portfolio.ecommerce.dto.CategoryRequestDTO;

import java.util.List;

public interface CategoryService {

    List<CategoryDTO> getAllCategories();

    CategoryDTO getCategoryById(Long id);

    CategoryDTO createCategory(CategoryRequestDTO requestDTO);

    CategoryDTO updateCategory(Long id, CategoryRequestDTO requestDTO);

    void deleteCategory(Long id);
}
