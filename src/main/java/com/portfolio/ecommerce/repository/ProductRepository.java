package com.portfolio.ecommerce.repository;

import com.portfolio.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find all active products with pagination
    Page<Product> findByActiveTrue(Pageable pageable);

    // Find by category
    Page<Product> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);

    // Search by name (case-insensitive, partial match)
    Page<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    // Find products within a price range
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                   @Param("maxPrice") BigDecimal maxPrice);

    // Find in-stock products
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.stockQuantity > 0")
    List<Product> findInStockProducts();

    // Check if SKU already exists
    boolean existsBySku(String sku);
}
