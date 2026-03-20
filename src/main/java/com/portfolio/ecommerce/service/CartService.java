package com.portfolio.ecommerce.service;

import com.portfolio.ecommerce.dto.CartItemRequest;
import com.portfolio.ecommerce.dto.CartResponse;

public interface CartService {
    CartResponse addItem(CartItemRequest request);
    CartResponse getCart();
    void removeItem(Long itemId);
}