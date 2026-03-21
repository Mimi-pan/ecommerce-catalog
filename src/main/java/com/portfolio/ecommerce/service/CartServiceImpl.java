package com.portfolio.ecommerce.service;

import com.portfolio.ecommerce.dto.CartItemRequest;
import com.portfolio.ecommerce.dto.CartResponse;
import com.portfolio.ecommerce.model.CartItem;
import com.portfolio.ecommerce.model.Product;
import com.portfolio.ecommerce.repository.CartRepository;
import com.portfolio.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @Override
    public CartResponse addItem(CartItemRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Optional<CartItem> existingItem = cartRepository
                .findByProductId(request.getProductId());

        if (existingItem.isPresent()) {
            // มีแล้ว → บวก quantity
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            item.setQuantity(newQuantity);
            item.setPrice(product.getPrice()
                    .multiply(BigDecimal.valueOf(newQuantity))
                    .doubleValue());
            cartRepository.save(item);
        } else {
            // ยังไม่มี → สร้างใหม่
            CartItem item = new CartItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setQuantity(request.getQuantity());
            item.setPrice(product.getPrice()
                    .multiply(BigDecimal.valueOf(request.getQuantity()))
                    .doubleValue());
            cartRepository.save(item);
        }

        return getCart();
    }

    @Override
    public CartResponse getCart() {
        List<CartItem> items = cartRepository.findAll();
        double total = items.stream()
                .mapToDouble(CartItem::getPrice)
                .sum();

        CartResponse response = new CartResponse();
        response.setItems(items);
        response.setTotalPrice(total);
        return response;
    }

    @Override
    public void removeItem(Long itemId) {
        cartRepository.deleteById(itemId);
    }
}