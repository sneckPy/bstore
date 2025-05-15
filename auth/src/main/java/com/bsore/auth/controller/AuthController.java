package com.bsore.auth.controller;

import com.bsore.auth.client.UsersClient;
import com.bsore.auth.model.entity.RefreshToken;
import com.bsore.auth.service.AuthService;
import com.bsore.auth.service.RefreshTokenService;
import com.bstore.commons.model.request.LoginRequest;
import com.bstore.commons.model.request.RefreshRequest;
import com.bstore.commons.model.request.UserRequest;
import com.bstore.commons.model.response.AuthResponse;
import com.bstore.commons.model.response.RegistrationResponse;
import com.bstore.commons.model.response.UserDetailsResponse;
import com.bstore.commons.model.response.UserResponse;
import feign.FeignException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshService;
    private final UsersClient usersClient;

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody UserRequest userRequest) {
        UserResponse created = usersClient.registerUser(userRequest);
        try {
            AuthResponse tokens = authService.authenticate(new LoginRequest(userRequest.getEmail(), userRequest.getPassword()), usersClient.getByEmail(created.getEmail()));
            return ResponseEntity.status(HttpStatus.CREATED).body(RegistrationResponse.builder().user(created).auth(tokens).build());
        } catch (FeignException ex) {
            try {
                usersClient.deleteUser(created.getId());
            } catch (Exception deleteEx) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No fue posible hacer rollback del registro.");
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No fue posible autenticar al nuevo usuario; se ha revertido el registro.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse tokens = authService.authenticate(request, usersClient.getByEmail(request.getEmail()));
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
        RefreshToken oldToken = refreshService.findByJti(request.getRefreshToken());
        UserDetailsResponse userDetailsResponse = usersClient.getByEmailById(oldToken.getUserId());
        AuthResponse tokens = authService.refreshTokens(oldToken, userDetailsResponse);

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