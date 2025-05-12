package com.bstore.product.service;

import com.bstore.commons.exception.DataRetrievalException;
import com.bstore.product.model.entity.Product;
import com.bstore.commons.model.request.ProductRequest;
import com.bstore.commons.model.response.ProductResponse;
import com.bstore.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;
    private final ConversionService conversionService;

    public List<ProductResponse> getAll() {
        return repository.findAll().stream().map(product -> conversionService.convert(product, ProductResponse.class)).toList();
    }

    public List<ProductResponse> findByIds(List<Long> ids) {
        return repository.findAllById(ids)
                .stream()
                .map(product -> conversionService.convert(product, ProductResponse.class))
                .toList();
    }

    public ProductResponse create(ProductRequest request) {
        Product entity = Objects.requireNonNull(conversionService.convert(request, Product.class));
        return conversionService.convert(repository.save(entity), ProductResponse.class);
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product product = repository.findById(id).orElseThrow(() -> new DataRetrievalException("Product not found: " + id));
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImageUrl(request.getImageUrl());
        Product updated = repository.save(product);
        return conversionService.convert(updated, ProductResponse.class);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}