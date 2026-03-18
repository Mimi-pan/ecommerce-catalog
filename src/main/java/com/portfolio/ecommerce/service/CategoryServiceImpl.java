package com.portfolio.ecommerce.service;

import com.portfolio.ecommerce.dto.CategoryDTO;
import com.portfolio.ecommerce.dto.CategoryRequestDTO;
import com.portfolio.ecommerce.exception.BusinessException;
import com.portfolio.ecommerce.exception.ResourceNotFoundException;
import com.portfolio.ecommerce.model.Category;
import com.portfolio.ecommerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return toDTO(category);
    }

    @Override
    public CategoryDTO createCategory(CategoryRequestDTO requestDTO) {
        if (categoryRepository.existsByNameIgnoreCase(requestDTO.getName())) {
            throw new BusinessException("Category with name '" + requestDTO.getName() + "' already exists");
        }

        Category category = Category.builder()
                .name(requestDTO.getName())
                .description(requestDTO.getDescription())
                .build();

        Category saved = categoryRepository.save(category);
        return toDTO(saved);
    }

    @Override
    public CategoryDTO updateCategory(Long id, CategoryRequestDTO requestDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Check for name conflict with another category
        categoryRepository.findByNameIgnoreCase(requestDTO.getName())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BusinessException("Category with name '" + requestDTO.getName() + "' already exists");
                    }
                });

        category.setName(requestDTO.getName());
        category.setDescription(requestDTO.getDescription());

        return toDTO(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (!category.getProducts().isEmpty()) {
            throw new BusinessException("Cannot delete category with existing products. Reassign products first.");
        }

        categoryRepository.delete(category);
    }

    // --- Mapper ---

    private CategoryDTO toDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .productCount(category.getProducts().size())
                .build();
    }
}
