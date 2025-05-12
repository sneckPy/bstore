package com.bstore.product.model.converter;

import com.bstore.product.model.entity.Product;
import com.bstore.commons.model.request.ProductRequest;
import com.bstore.commons.model.response.ProductResponse;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ProductGenericConverter implements GenericConverter {

    private final EntityToProductResponseConverter entityToProductResponseConverter;
    private final ProductRequestToEntityConverter productRequestToEntityConverter;


    public ProductGenericConverter(EntityToProductResponseConverter entityToProductResponseConverter, ProductRequestToEntityConverter productRequestToEntityConverter) {
        this.entityToProductResponseConverter = entityToProductResponseConverter;
        this.productRequestToEntityConverter = productRequestToEntityConverter;
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Set.of(new ConvertiblePair(ProductRequest.class, Product.class), new ConvertiblePair(Product.class, ProductRequest.class));
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType,@NonNull TypeDescriptor targetType) {
        if (sourceType.getType() == ProductRequest.class && targetType.getType() == Product.class) {
            return productRequestToEntityConverter.convert((ProductRequest) source);
        }
        if (sourceType.getType() == Product.class && targetType.getType() == ProductResponse.class) {
            return entityToProductResponseConverter.convert((Product) source);
        }
        throw new IllegalArgumentException("Product converter not mapped");
    }
}
