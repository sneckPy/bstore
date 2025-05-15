package com.bstore.commons.model.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationResponse {
    private UserResponse user;
    private AuthResponse auth;
}
