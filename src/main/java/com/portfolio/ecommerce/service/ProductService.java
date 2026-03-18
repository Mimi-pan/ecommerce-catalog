package com.portfolio.ecommerce.service;

import com.portfolio.ecommerce.dto.ProductDTO;
import com.portfolio.ecommerce.dto.ProductRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    Page<ProductDTO> getAllProducts(Pageable pageable);

    ProductDTO getProductById(Long id);

    Page<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable);

    Page<ProductDTO> searchProductsByName(String name, Pageable pageable);

    List<ProductDTO> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    List<ProductDTO> getInStockProducts();

    ProductDTO createProduct(ProductRequestDTO requestDTO);

    ProductDTO updateProduct(Long id, ProductRequestDTO requestDTO);

    void deleteProduct(Long id);
}
