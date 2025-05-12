package com.bstore.pucharse.factory;

import com.bstore.commons.model.request.CartRequest;
import com.bstore.pucharse.model.entity.Cart;
import com.bstore.pucharse.model.entity.CartItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CartItemFactory {
    public List<CartItem> buildItems(CartRequest request, Cart cart) {
        Map<Long, Integer> itemsMap = request.getItems();
        return itemsMap.entrySet()
                .stream()
                .map(e -> CartItem.builder()
                        .cart(cart)
                        .productId(e.getKey())
                        .quantity(e.getValue())
                        .build())
                .toList();
    }
}
