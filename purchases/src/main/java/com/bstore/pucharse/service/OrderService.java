package com.bstore.pucharse.service;

import com.bstore.commons.model.request.OrderRequest;
import com.bstore.commons.model.response.OrderItemResponse;
import com.bstore.commons.model.response.OrderResponse;
import com.bstore.commons.model.response.ProductResponse;
import com.bstore.pucharse.model.entity.Order;
import com.bstore.pucharse.model.entity.OrderItem;
import com.bstore.pucharse.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient productWebClient;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        List<OrderItem> items = request.getItems().stream()
                .map(r -> OrderItem.builder()
                        .productId(r.getProductId())
                        .quantity(r.getQuantity())
                        .order(null)
                        .unitPrice(BigDecimal.ZERO)
                        .createdAt(LocalDateTime.now())
                        .build())
                .toList();

        List<ProductResponse> products = getProducts(items);

        Map<Long, BigDecimal> priceById = products.stream().collect(Collectors.toMap(ProductResponse::getId, ProductResponse::getPrice));

        items.forEach(i -> i.setUnitPrice(priceById.getOrDefault(i.getProductId(), BigDecimal.ZERO)));

        Order order = Order.builder()
                .userId(request.getUserId())
                .createdAt(LocalDateTime.now())
                .build();

        items.forEach(i -> i.setOrder(order));
        if (order.getItems() == null) order.setItems(new ArrayList<>());
        order.getItems().addAll(items);

        BigDecimal total = calculateTotal(products,
                items.stream().collect(Collectors.toMap(
                        OrderItem::getProductId,
                        OrderItem::getQuantity
                )));
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);
        return mapToResponse(saved);
    }

    private List<ProductResponse> getProducts(List<OrderItem> items) {
        String idsParam = items.stream()
                .map(OrderItem::getProductId)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        String relative = "/products/?ids=" + idsParam;

        return productWebClient.get()
                .uri(relative)
                .exchangeToFlux(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToFlux(ProductResponse.class);
                    } else {
                        return response.bodyToMono(String.class)
                                .flatMapMany(body -> Flux.empty());
                    }
                })
                .collectList()
                .block();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private BigDecimal calculateTotal(List<ProductResponse> products, Map<Long, Integer> qtyByProductId) {
        BigDecimal total = BigDecimal.ZERO;
        if (products != null) {
            total = products.stream()
                    .map(p -> {
                        Integer qty = qtyByProductId.get(p.getId());
                        return p.getPrice()
                                .multiply(BigDecimal.valueOf(qty == null ? 0 : qty));
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return total;
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(i -> OrderItemResponse.builder()
                        .productId(i.getProductId())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .build())
                .collect(Collectors.toList());
        return OrderResponse.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .createdAt(order.getCreatedAt())
                .totalAmount(order.getTotalAmount())
                .items(items)
                .build();
    }
}
