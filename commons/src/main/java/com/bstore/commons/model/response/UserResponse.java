package com.bstore.commons.model.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String shippingAddress;
    private LocalDate birthDate;
}
