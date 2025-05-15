package com.bsore.auth.service;

import com.bsore.auth.model.entity.RefreshToken;
import com.bsore.auth.repository.RefreshTokenRepository;
import com.bstore.commons.exception.DataRetrievalException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repo;
    private final long refreshMs;
    @Getter
    private final boolean cookieSecure;

    public RefreshTokenService(
            RefreshTokenRepository repo,
            @Value("${application.security.jwt.refresh-token-expiration}") long refreshMs,
            @Value("${application.security.cookie-secure}") boolean cookieSecure
    ) {
        this.repo = repo;
        this.refreshMs = refreshMs;
        this.cookieSecure = cookieSecure;
    }

    public long getDurationSeconds() {
        return refreshMs / 1000;
    }

    public RefreshToken createToken(Long userId) {
        RefreshToken token = new RefreshToken();
        token.setJti(UUID.randomUUID().toString());
        token.setUserId(userId);
        LocalDateTime now = LocalDateTime.now();
        token.setIssuedAt(now);
        token.setExpiresAt(now.plusSeconds(getDurationSeconds()));
        return repo.save(token);
    }

    @Transactional
    public RefreshToken rotateToken(RefreshToken oldToken) {
        oldToken.setRevoked(true);
        oldToken.setRevokedAt(LocalDateTime.now());
        RefreshToken newToken = createToken(oldToken.getUserId());
        oldToken.setReplacedByJti(newToken.getJti());
        repo.save(oldToken);
        return newToken;
    }

    public void verifyExpiration(RefreshToken token) {
        if (token.isRevoked() || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            repo.delete(token);
            throw new DataRetrievalException("Refresh token expired or revoked");
        }
    }

    public RefreshToken findByJti(String jti) {
        return repo.findByJti(jti)
                .orElseThrow(() -> new DataRetrievalException("Refresh token not found"));
    }
}