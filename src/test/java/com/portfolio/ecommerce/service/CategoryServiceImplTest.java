package com.portfolio.ecommerce.service;

import com.portfolio.ecommerce.dto.CategoryDTO;
import com.portfolio.ecommerce.dto.CategoryRequestDTO;
import com.portfolio.ecommerce.exception.BusinessException;
import com.portfolio.ecommerce.exception.ResourceNotFoundException;
import com.portfolio.ecommerce.model.Category;
import com.portfolio.ecommerce.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CategoryServiceImpl.
 * Dependencies (CategoryRepository) are mocked with Mockito —
 * no Spring context or database is needed.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category electronics;
    private Category books;

    @BeforeEach
    void setUp() {
        electronics = Category.builder()
                .id(1L)
                .name("Electronics")
                .description("Gadgets and devices")
                .products(new ArrayList<>())
                .build();

        books = Category.builder()
                .id(3L)
                .name("Books")
                .description("All genres")
                .products(new ArrayList<>())
                .build();
    }

    // -----------------------------------------------------------------------
    // getAllCategories
    // -----------------------------------------------------------------------

    @Test
    void getAllCategories_returnsMappedDTOs() {
        when(categoryRepository.findAllWithProducts()).thenReturn(List.of(electronics, books));

        List<CategoryDTO> result = categoryService.getAllCategories();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CategoryDTO::getName)
                .containsExactly("Electronics", "Books");
    }

    @Test
    void getAllCategories_emptyRepository_returnsEmptyList() {
        when(categoryRepository.findAllWithProducts()).thenReturn(List.of());

        List<CategoryDTO> result = categoryService.getAllCategories();

        assertThat(result).isEmpty();
    }

    @Test
    void getAllCategories_usesJoinFetchMethod_notFindAll() {
        when(categoryRepository.findAllWithProducts()).thenReturn(List.of());

        categoryService.getAllCategories();

        // Verify we're using findAllWithProducts (JOIN FETCH) and NOT findAll (N+1)
        verify(categoryRepository).findAllWithProducts();
        verify(categoryRepository, never()).findAll();
    }

    // -----------------------------------------------------------------------
    // getCategoryById
    // -----------------------------------------------------------------------

    @Test
    void getCategoryById_existing_returnsCategoryDTO() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(electronics));

        CategoryDTO result = categoryService.getCategoryById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Electronics");
        assertThat(result.getDescription()).isEqualTo("Gadgets and devices");
        assertThat(result.getProductCount()).isEqualTo(0);
    }

    @Test
    void getCategoryById_notFound_throwsResourceNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category")
                .hasMessageContaining("99");
    }

    // -----------------------------------------------------------------------
    // createCategory
    // -----------------------------------------------------------------------

    @Test
    void createCategory_uniqueName_savesAndReturnsDTO() {
        CategoryRequestDTO request = CategoryRequestDTO.builder()
                .name("Gaming")
                .description("Games and consoles")
                .build();

        Category saved = Category.builder()
                .id(6L)
                .name("Gaming")
                .description("Games and consoles")
                .products(new ArrayList<>())
                .build();

        when(categoryRepository.existsByNameIgnoreCase("Gaming")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryDTO result = categoryService.createCategory(request);

        assertThat(result.getId()).isEqualTo(6L);
        assertThat(result.getName()).isEqualTo("Gaming");
        assertThat(result.getProductCount()).isEqualTo(0);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_duplicateName_throwsBusinessException() {
        CategoryRequestDTO request = CategoryRequestDTO.builder()
                .name("Electronics")
                .build();

        when(categoryRepository.existsByNameIgnoreCase("Electronics")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");

        verify(categoryRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // updateCategory
    // -----------------------------------------------------------------------

    @Test
    void updateCategory_validRequest_updatesNameAndDescription() {
        CategoryRequestDTO request = CategoryRequestDTO.builder()
                .name("Consumer Electronics")
                .description("Updated description")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(electronics));
        when(categoryRepository.findByNameIgnoreCase("Consumer Electronics")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoryDTO result = categoryService.updateCategory(1L, request);

        assertThat(result.getName()).isEqualTo("Consumer Electronics");
        assertThat(result.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void updateCategory_sameNameSameId_doesNotThrow() {
        // Updating a category with its own name (not a duplicate conflict)
        CategoryRequestDTO request = CategoryRequestDTO.builder()
                .name("Electronics")
                .description("Updated description")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(electronics));
        when(categoryRepository.findByNameIgnoreCase("Electronics")).thenReturn(Optional.of(electronics));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatCode(() -> categoryService.updateCategory(1L, request))
                .doesNotThrowAnyException();
    }

    @Test
    void updateCategory_notFound_throwsResourceNotFoundException() {
        CategoryRequestDTO request = CategoryRequestDTO.builder().name("X").build();

        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -----------------------------------------------------------------------
    // deleteCategory
    // -----------------------------------------------------------------------

    @Test
    void deleteCategory_noProducts_deletesSuccessfully() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(electronics));

        categoryService.deleteCategory(1L);

        verify(categoryRepository).delete(electronics);
    }

    @Test
    void deleteCategory_notFound_throwsResourceNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository, never()).delete(any());
    }
}
