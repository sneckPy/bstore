package com.bstore.pucharse.controller;

import com.bstore.commons.model.request.CartRequest;
import com.bstore.commons.model.response.CartResponse;
import com.bstore.pucharse.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable Long userId) {
        Optional<CartResponse> response = cartService.findByUserId(userId);
        if (response.isEmpty()) return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        return ResponseEntity.status(HttpStatus.OK).body(response.get());
    }

    @PutMapping("/{userId}")
    public ResponseEntity<CartResponse> updateCart(@PathVariable Long userId, @RequestBody CartRequest request) {
        CartResponse updated = cartService.updateCart(userId, request);
        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

}
