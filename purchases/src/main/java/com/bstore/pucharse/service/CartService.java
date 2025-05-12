package com.bstore.pucharse.service;

import com.bstore.commons.model.request.CartRequest;
import com.bstore.commons.model.response.CartItemResponse;
import com.bstore.commons.model.response.CartResponse;
import com.bstore.commons.model.response.ProductResponse;
import com.bstore.pucharse.factory.CartItemFactory;
import com.bstore.pucharse.model.entity.Cart;
import com.bstore.pucharse.model.entity.CartItem;
import com.bstore.pucharse.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemFactory itemFactory;
    private final WebClient productWebClient;
    private static final Logger log = LoggerFactory.getLogger(CartService.class);

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
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> Cart.builder().userId(userId).build());

        if (cart.getItems() != null) cart.getItems().clear();
        else cart.setItems(new ArrayList<>());
        List<CartItem> newItems = itemFactory.buildItems(request, cart);
        cart.getItems().addAll(newItems);
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
        BigDecimal total = BigDecimal.ZERO;
        if (products != null) {
            total = products.stream()
                    .map(p -> {
                        Integer qty = qtyByProductId.get(p.getId());
                        return p.getPrice().multiply(BigDecimal.valueOf(qty == null ? 0 : qty));
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return total;
    }

    private List<ProductResponse> getProducts(Cart cart) {
        if (cart.getItems().isEmpty()) return Collections.emptyList();

        String idsParam = cart.getItems().stream()
                .map(CartItem::getProductId)
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String relative = "/products?ids=" + idsParam;
        log.info("Llamando a Product Service en URI relativa -> {}", relative);

        return productWebClient.get()
                .uri("/products/?ids=" + idsParam)
                .exchangeToFlux(response -> {
                    log.info("Products call status: {}", response.statusCode());
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToFlux(ProductResponse.class);
                    } else {
                        return response.bodyToMono(String.class)
                                .flatMapMany(body -> {
                                    log.warn("Products call body: {}", body);
                                    return Flux.empty();
                                });
                    }
                })
                .collectList()
                .block();

    }

    private Map<Long, Integer> getProductQty(Cart saved) {
        return saved.getItems().stream().collect(Collectors.toMap(CartItem::getProductId, CartItem::getQuantity));
    }

    private List<CartItemResponse> mapCartItemResponse(List<ProductResponse> products, Map<Long, Integer> qtyByProductId) {
        return products.stream()
                .map(p -> {
                    CartItemResponse cartItemResponse = new CartItemResponse();
                    cartItemResponse.setProduct(p);
                    cartItemResponse.setQuantity(qtyByProductId.getOrDefault(p.getId(), 0));
                    return cartItemResponse;
                })
                .toList();
    }
}


