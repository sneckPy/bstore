package com.bstore.commons.model.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetailsResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String shippingAddress;
    private LocalDate birthDate;
    private String passwordHash;
}
