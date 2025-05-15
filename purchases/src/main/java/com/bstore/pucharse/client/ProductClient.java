package com.bstore.pucharse.client;

import com.bstore.commons.model.response.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "product-service", url = "${gateway.url}/products")
public interface ProductClient {

    @GetMapping
    List<ProductResponse> getProductsByIds(@RequestParam(name = "ids", required = false) String idsParam);
}
