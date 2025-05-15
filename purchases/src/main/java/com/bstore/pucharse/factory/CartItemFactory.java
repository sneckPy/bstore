package com.bstore.pucharse.factory;

import com.bstore.commons.model.request.CartRequest;
import com.bstore.pucharse.model.entity.Cart;
import com.bstore.pucharse.model.entity.CartItem;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class CartItemFactory {

    public List<CartItem> buildItems(CartRequest request, Cart cart) {
        Map<Long, Integer> itemsMap = request.getItems();
        if (itemsMap == null) {
            return Collections.emptyList();
        }
        return itemsMap.entrySet()
                .stream()
                .filter(entry -> entry.getKey() != null && entry.getValue() != null && entry.getValue() > 0)
                .map(entry -> CartItem.builder()
                        .cart(cart)
                        .productId(entry.getKey())
                        .quantity(entry.getValue())
                        .build())
                .toList();
    }

    public CartItem buildItem(Long productId, Integer quantity, Cart cart) {
        if (productId == null || quantity == null || quantity <= 0 || cart == null) {
            return null;
        }
        return CartItem.builder()
                .cart(cart)
                .productId(productId)
                .quantity(quantity)
                .build();
    }
}