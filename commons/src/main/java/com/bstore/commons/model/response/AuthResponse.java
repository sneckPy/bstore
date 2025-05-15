package com.bstore.commons.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;

    private String refreshToken;

    public AuthResponse withoutRefresh() {
        return AuthResponse.builder()
                .accessToken(this.accessToken)
                .build();
    }

    public AuthResponse withRefresh() {
        return AuthResponse.builder()
                .accessToken(this.accessToken)
                .refreshToken(this.refreshToken)
                .build();
    }
}