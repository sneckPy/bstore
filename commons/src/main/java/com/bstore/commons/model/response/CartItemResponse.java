package com.bstore.commons.model.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {
    private ProductResponse product;
    private Integer quantity;
}
