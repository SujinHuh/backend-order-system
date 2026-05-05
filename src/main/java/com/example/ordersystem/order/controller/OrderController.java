package com.example.ordersystem.order.controller;

import com.example.ordersystem.order.controller.dto.OrderCreateRequest;
import com.example.ordersystem.order.controller.dto.OrderResponse;
import com.example.ordersystem.order.controller.dto.OrderStatusUpdateRequest;
import com.example.ordersystem.order.domain.OrderStatus;
import com.example.ordersystem.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@RequestBody @Valid OrderCreateRequest request) {
        return orderService.createOrder(
                request.getItems().stream()
                        .map(item -> new OrderService.OrderItemRequest(item.getProductId(), item.getQuantity()))
                        .collect(Collectors.toList())
        );
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(
            @PathVariable Long id,
            @RequestBody @Valid OrderStatusUpdateRequest request) {
        return orderService.updateStatus(id, request.getStatus());
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @GetMapping
    public Page<OrderResponse> getOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return orderService.getOrders(status, from, to, pageable);
    }
}
