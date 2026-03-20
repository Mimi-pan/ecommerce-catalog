package com.portfolio.ecommerce.dto;

import com.portfolio.ecommerce.model.CartItem;
import lombok.Data;
import java.util.List;

@Data
public class CartResponse {
    private List<CartItem> items;
    private double totalPrice;
}