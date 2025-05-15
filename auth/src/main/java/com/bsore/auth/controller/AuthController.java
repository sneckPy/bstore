package com.bsore.auth.controller;

import com.bsore.auth.model.entity.RefreshToken;
import com.bsore.auth.service.AuthService;
import com.bsore.auth.service.RefreshTokenService;
import com.bstore.commons.model.request.LoginRequest;
import com.bstore.commons.model.request.RefreshRequest;
import com.bstore.commons.model.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse tokens = authService.authenticate(request);
        ResponseCookie cookie = ResponseCookie.from("refreshToken", tokens.getRefreshToken())
                .httpOnly(true)
                .secure(refreshService.isCookieSecure())
                .sameSite("Strict")
                .path("/")
                .maxAge(refreshService.getDurationSeconds())
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(tokens.withRefresh());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {
        AuthResponse tokens = authService.refreshTokens(request.getRefreshToken());
        ResponseCookie cookie = ResponseCookie.from("refreshToken", tokens.getRefreshToken())
                .httpOnly(true)
                .secure(refreshService.isCookieSecure())
                .sameSite("Strict")
                .path("/")
                .maxAge(refreshService.getDurationSeconds())
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(tokens.withoutRefresh());
    }
}