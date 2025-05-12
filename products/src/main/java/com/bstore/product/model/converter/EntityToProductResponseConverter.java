package com.bstore.product.model.converter;

import com.bstore.product.model.entity.Product;
import com.bstore.commons.model.response.ProductResponse;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class EntityToProductResponseConverter implements Converter<Product, ProductResponse> {
    @Override
    public ProductResponse convert(Product source) {
        return ProductResponse.builder()
                .id(source.getId())
                .name(source.getName())
                .description(source.getDescription())
                .price(source.getPrice())
                .imageUrl(source.getImageUrl())
                .createdAt(source.getCreatedAt())
                .updatedAt(source.getUpdatedAt())
                .build();
    }
}
