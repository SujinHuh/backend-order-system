package com.example.ordersystem.order.controller;

import com.example.ordersystem.order.controller.dto.OrderCreateRequest;
import com.example.ordersystem.order.controller.dto.OrderResponse;
import com.example.ordersystem.order.domain.Order;
import com.example.ordersystem.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@RequestBody @Valid OrderCreateRequest request) {
        Order order = orderService.createOrder(
                request.getItems().stream()
                        .map(item -> new OrderService.OrderItemRequest(item.getProductId(), item.getQuantity()))
                        .collect(Collectors.toList())
        );
        return OrderResponse.from(order);
    }
}
