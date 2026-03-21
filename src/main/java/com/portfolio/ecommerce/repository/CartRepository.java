package com.portfolio.ecommerce.repository;

import com.portfolio.ecommerce.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<CartItem, Long> {

    // เพิ่มบรรทัดนี้
    Optional<CartItem> findByProductId(Long productId);
}