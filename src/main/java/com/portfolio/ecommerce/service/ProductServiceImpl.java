package com.portfolio.ecommerce.service;

import com.portfolio.ecommerce.dto.ProductDTO;
import com.portfolio.ecommerce.dto.ProductRequestDTO;
import com.portfolio.ecommerce.exception.BusinessException;
import com.portfolio.ecommerce.exception.ResourceNotFoundException;
import com.portfolio.ecommerce.model.Category;
import com.portfolio.ecommerce.model.Product;
import com.portfolio.ecommerce.repository.CategoryRepository;
import com.portfolio.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable)
                .map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return toDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category", "id", categoryId);
        }
        return productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable)
                .map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> searchProductsByName(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(name, pageable)
                .map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice.compareTo(maxPrice) > 0) {
            throw new BusinessException("minPrice cannot be greater than maxPrice");
        }
        return productRepository.findByPriceRange(minPrice, maxPrice)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getInStockProducts() {
        return productRepository.findInStockProducts()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductDTO createProduct(ProductRequestDTO requestDTO) {
        if (requestDTO.getSku() != null && productRepository.existsBySku(requestDTO.getSku())) {
            throw new BusinessException("A product with SKU '" + requestDTO.getSku() + "' already exists");
        }

        Product product = buildProductFromRequest(requestDTO, new Product());
        return toDTO(productRepository.save(product));
    }

    @Override
    public ProductDTO updateProduct(Long id, ProductRequestDTO requestDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        // Check SKU conflict with another product
        if (requestDTO.getSku() != null) {
            productRepository.findAll().stream()
                    .filter(p -> requestDTO.getSku().equals(p.getSku()) && !p.getId().equals(id))
                    .findFirst()
                    .ifPresent(p -> {
                        throw new BusinessException("A product with SKU '" + requestDTO.getSku() + "' already exists");
                    });
        }

        buildProductFromRequest(requestDTO, product);
        return toDTO(productRepository.save(product));
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        // Soft delete: mark as inactive instead of removing from DB
        product.setActive(false);
        productRepository.save(product);
    }

    // --- Helpers ---

    private Product buildProductFromRequest(ProductRequestDTO dto, Product product) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : 0);
        product.setSku(dto.getSku());
        product.setImageUrl(dto.getImageUrl());
        product.setActive(dto.getActive() != null ? dto.getActive() : true);

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }

        return product;
    }

    private ProductDTO toDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .sku(product.getSku())
                .imageUrl(product.getImageUrl())
                .active(product.getActive())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
