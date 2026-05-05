package com.example.ordersystem.order.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OrderRepositoryCustom {

    Optional<Order> findByIdWithItemsAndProducts(Long id);

    Page<Order> search(OrderStatus status, LocalDateTime from, LocalDateTime to, Pageable pageable);
}
