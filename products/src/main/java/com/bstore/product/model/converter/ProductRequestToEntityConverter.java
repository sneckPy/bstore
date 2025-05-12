package com.bstore.product.model.converter;

import com.bstore.product.model.entity.Product;
import com.bstore.commons.model.request.ProductRequest;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ProductRequestToEntityConverter implements Converter<ProductRequest, Product> {
    @Override
    public Product convert(ProductRequest source) {
        return Product.builder()
                .name(source.getName())
                .description(source.getDescription())
                .price(source.getPrice())
                .imageUrl(source.getImageUrl())
                .build();
    }
}
