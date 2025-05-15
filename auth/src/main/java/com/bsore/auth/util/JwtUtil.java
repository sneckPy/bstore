package com.bsore.auth.util;

import com.bstore.commons.model.response.UserDetailsResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private SecretKey key;
    private long accessMs;

    @Value("${application.security.jwt.secret-key}")
    private String secret;

    @Value("${application.security.jwt.access-token-expiration}")
    private long accessExpiration;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessMs = accessExpiration;
    }

    public String generateAccessToken(UserDetailsResponse user) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessMs);
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("email", user.getEmail())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
