package com.bstore.pucharse.service;

import com.bstore.commons.model.request.CartRequest;
import com.bstore.commons.model.response.CartItemResponse;
import com.bstore.commons.model.response.CartResponse;
import com.bstore.commons.model.response.ProductResponse;
import com.bstore.pucharse.factory.CartItemFactory;
import com.bstore.pucharse.model.entity.Cart;
import com.bstore.pucharse.model.entity.CartItem;
import com.bstore.pucharse.repository.CartRepository;
import com.bstore.pucharse.client.ProductClient;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemFactory itemFactory;
    private final ProductClient productClient;


    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    @Transactional
    public Cart getOrCreateUserCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> Cart.builder().userId(userId).items(new ArrayList<>()).build());
    }

    public Optional<CartResponse> findByUserId(Long userId) {
        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
        if (cartOpt.isEmpty()) return Optional.empty();

        Cart cart = cartOpt.get();
        List<ProductResponse> products = getProducts(cart);

        Map<Long, Integer> qtyByProductId = getProductQty(cart);

        return Optional.of(CartResponse.builder()
                .cartId(cart.getId())
                .items(mapCartItemResponse(products, qtyByProductId))
                .total(getCartTotalPrice(products, qtyByProductId))
                .build());
    }

    @Transactional
    public CartResponse updateCart(Long userId, CartRequest request) {
        Cart cart = getOrCreateUserCart(userId);

        Map<Long, Integer> itemsToProcess = request != null ? request.getItems() : Collections.emptyMap();

        List<CartItem> existingItems = cart.getItems();
        if (existingItems == null) {
            existingItems = new ArrayList<>();
            cart.setItems(existingItems);
        }

        Map<Long, Integer> processedRequestItems = new HashMap<>(itemsToProcess);

        Iterator<CartItem> iterator = existingItems.iterator();
        while (iterator.hasNext()) {
            CartItem item = iterator.next();
            Long productId = item.getProductId();

            if (processedRequestItems.containsKey(productId)) {
                Integer quantityChange = processedRequestItems.get(productId);

                if (quantityChange != null && quantityChange != 0) {
                    item.setQuantity(item.getQuantity() + quantityChange);

                    processedRequestItems.remove(productId);

                    if (item.getQuantity() == null || item.getQuantity() <= 0) {
                        iterator.remove();
                    }
                } else if (quantityChange != null) {
                    iterator.remove();
                    processedRequestItems.remove(productId);
                } else {
                    log.warn("Null quantity provided for item {} in CartRequest for user {}. Ignoring.", productId, userId);
                }
            }
        }

        List<CartItem> newItemsToAdd = processedRequestItems.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue() > 0)
                .map(entry -> {
                    Long productId = entry.getKey();
                    Integer quantity = entry.getValue();
                    return itemFactory.buildItem(productId, quantity, cart);
                })
                .filter(Objects::nonNull)
                .toList();

        existingItems.addAll(newItemsToAdd);

        Cart saved = cartRepository.save(cart);

        List<ProductResponse> products = getProducts(saved);
        Map<Long, Integer> qtyByProductId = getProductQty(saved);

        return CartResponse.builder()
                .cartId(saved.getId())
                .items(mapCartItemResponse(products, qtyByProductId))
                .total(getCartTotalPrice(products, qtyByProductId))
                .build();
    }

    @Transactional
    public void clearCart(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }

    private BigDecimal getCartTotalPrice(List<ProductResponse> products, Map<Long, Integer> qtyByProductId) {
        if (products == null || qtyByProductId == null) {
            return BigDecimal.ZERO;
        }
        return products.stream()
                .filter(p -> p.getPrice() != null && qtyByProductId.getOrDefault(p.getId(), 0) > 0)
                .map(p -> p.getPrice().multiply(
                        BigDecimal.valueOf(qtyByProductId.get(p.getId()))
                ))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<ProductResponse> getProducts(Cart cart) {
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> productIds = cart.getItems().stream()
                .map(CartItem::getProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (productIds.isEmpty()) {
            return Collections.emptyList();
        }

        String idsParam = productIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        log.info("Calling Product Service via Feign client with IDs: {}", idsParam);

        try {
            return productClient.getProductsByIds(idsParam);
        } catch (Exception e) {
            log.error("Error fetching products via Feign client: {}", e.getMessage(), e);
            return Collections.emptyList(); // Retorna lista vacía en caso de error
        }
    }

    private Map<Long, Integer> getProductQty(Cart saved) {
        if (saved == null || saved.getItems() == null) return Collections.emptyMap();
        return saved.getItems().stream()
                .filter(item -> item != null && item.getProductId() != null && item.getQuantity() != null && item.getQuantity() > 0)
                .collect(Collectors.toMap(CartItem::getProductId, CartItem::getQuantity, Integer::sum));
    }

    private List<CartItemResponse> mapCartItemResponse(List<ProductResponse> products, Map<Long, Integer> qtyByProductId) {
        if (products == null || products.isEmpty() || qtyByProductId == null || qtyByProductId.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, ProductResponse> productMap = products.stream()
                .filter(p -> p != null && p.getId() != null)
                .collect(Collectors.toMap(ProductResponse::getId, p -> p));

        return qtyByProductId.entrySet().stream()
                .map(entry -> {
                    Long productId = entry.getKey();
                    Integer quantity = entry.getValue();
                    ProductResponse product = productMap.get(productId);

                    if (product != null && quantity != null && quantity > 0) {
                        CartItemResponse cartItemResponse = new CartItemResponse();
                        cartItemResponse.setProduct(product);
                        cartItemResponse.setQuantity(quantity);
                        return cartItemResponse;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }
}