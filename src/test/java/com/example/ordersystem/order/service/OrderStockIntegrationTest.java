package com.example.ordersystem.order.service;

import com.example.ordersystem.order.controller.dto.OrderResponse;
import com.example.ordersystem.order.domain.Order;
import com.example.ordersystem.order.domain.OrderRepository;
import com.example.ordersystem.order.domain.OrderStatus;
import com.example.ordersystem.product.domain.Category;
import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.domain.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class OrderStockIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("주문 완료 시 실제 상품 재고가 차감되고, 취소 시 복구된다.")
    void order_statusChange_stockIntegration() {
        // given: 재고 10개인 상품과 PENDING 주문
        Product product = Product.builder()
                .name("Product")
                .price(1000L)
                .stockQuantity(10)
                .category(Category.FOOD)
                .build();
        productRepository.save(product);

        OrderResponse orderResponse = orderService.createOrder(List.of(new OrderService.OrderItemRequest(product.getId(), 3)));
        Long orderId = orderResponse.getId();
        
        // when 1: ACCEPTED (재고 변화 없음)
        orderService.updateStatus(orderId, OrderStatus.ACCEPTED);
        
        // then 1
        assertThat(productRepository.findById(product.getId()).get().getStockQuantity()).isEqualTo(10);

        // when 2: COMPLETED (재고 차감 발생)
        orderService.updateStatus(orderId, OrderStatus.COMPLETED);

        // then 2
        assertThat(productRepository.findById(product.getId()).get().getStockQuantity()).isEqualTo(7);

        // when 3: CANCELED (재고 복구 발생)
        orderService.updateStatus(orderId, OrderStatus.CANCELED);

        // then 3
        assertThat(productRepository.findById(product.getId()).get().getStockQuantity()).isEqualTo(10);
    }
}
