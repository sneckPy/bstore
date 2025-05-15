package com.bsore.auth.service;

import com.bsore.auth.client.UsersClient;
import com.bsore.auth.model.entity.RefreshToken;
import com.bsore.auth.util.JwtUtil;
import com.bstore.commons.model.request.LoginRequest;
import com.bstore.commons.model.response.AuthResponse;
import com.bstore.commons.model.response.UserDetailsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsersClient usersClient;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse authenticate(LoginRequest request, UserDetailsResponse user) {
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }
        String accessToken = jwtUtil.generateAccessToken(user);
        RefreshToken refreshToken = refreshService.createToken(user.getId());
        return new AuthResponse(accessToken, refreshToken.getJti());
    }


    @Transactional
    public AuthResponse refreshTokens(RefreshToken oldToken, UserDetailsResponse userDetailsResponse) {
        refreshService.verifyExpiration(oldToken);
        RefreshToken newToken = refreshService.rotateToken(oldToken);
        String newAccessToken = jwtUtil.generateAccessToken(userDetailsResponse);
        return new AuthResponse(newAccessToken, newToken.getJti());
    }

}
