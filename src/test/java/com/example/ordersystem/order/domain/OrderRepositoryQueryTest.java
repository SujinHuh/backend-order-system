package com.example.ordersystem.order.domain;

import com.example.ordersystem.order.controller.dto.OrderResponse;
import com.example.ordersystem.product.domain.Category;
import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.domain.ProductRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class OrderRepositoryQueryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("주문 상태 조건으로 목록을 페이징 조회한다.")
    void searchByStatus() {
        Order pending = orderRepository.save(createOrder("Pending Product"));
        Order accepted = orderRepository.save(createOrder("Accepted Product"));
        accepted.changeStatus(OrderStatus.ACCEPTED);

        Page<Order> result = orderRepository.search(OrderStatus.ACCEPTED, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent())
                .extracting(Order::getId)
                .containsExactly(accepted.getId());
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).doesNotContain(pending);
    }

    @Test
    @DisplayName("주문 생성 기간 조건으로 목록을 페이징 조회한다.")
    void searchByCreatedAtRange() {
        Order oldOrder = orderRepository.save(createOrder("Old Product"));
        Order targetOrder = orderRepository.save(createOrder("Target Product"));
        setCreatedAt(oldOrder.getId(), LocalDateTime.of(2026, 1, 1, 10, 0));
        setCreatedAt(targetOrder.getId(), LocalDateTime.of(2026, 1, 2, 10, 0));

        Page<Order> result = orderRepository.search(
                null,
                LocalDateTime.of(2026, 1, 2, 0, 0),
                LocalDateTime.of(2026, 1, 2, 23, 59),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent())
                .extracting(Order::getId)
                .containsExactly(targetOrder.getId());
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("주문 상태와 생성 기간 조건을 함께 조합해 조회한다.")
    void searchByStatusAndCreatedAtRange() {
        Order acceptedOldOrder = orderRepository.save(createOrder("Accepted Old Product"));
        acceptedOldOrder.changeStatus(OrderStatus.ACCEPTED);
        Order acceptedTargetOrder = orderRepository.save(createOrder("Accepted Target Product"));
        acceptedTargetOrder.changeStatus(OrderStatus.ACCEPTED);
        Order pendingTargetOrder = orderRepository.save(createOrder("Pending Target Product"));
        setCreatedAt(acceptedOldOrder.getId(), LocalDateTime.of(2026, 1, 1, 10, 0));
        setCreatedAt(acceptedTargetOrder.getId(), LocalDateTime.of(2026, 1, 2, 10, 0));
        setCreatedAt(pendingTargetOrder.getId(), LocalDateTime.of(2026, 1, 2, 11, 0));

        Page<Order> result = orderRepository.search(
                OrderStatus.ACCEPTED,
                LocalDateTime.of(2026, 1, 2, 0, 0),
                LocalDateTime.of(2026, 1, 2, 23, 59),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent())
                .extracting(Order::getId)
                .containsExactly(acceptedTargetOrder.getId());
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("주문 상세 조회는 주문 항목과 상품 정보 응답 매핑이 가능하다.")
    void findByIdWithItemsAndProducts() {
        Order order = orderRepository.save(createOrder("Detail Product"));
        entityManager.flush();
        entityManager.clear();

        Order foundOrder = orderRepository.findByIdWithItemsAndProducts(order.getId()).orElseThrow();
        OrderResponse response = OrderResponse.from(foundOrder);

        assertThat(Hibernate.isInitialized(foundOrder.getOrderItems())).isTrue();
        assertThat(Hibernate.isInitialized(foundOrder.getOrderItems().get(0).getProduct())).isTrue();
        assertThat(response.getOrderItems()).hasSize(1);
        assertThat(response.getOrderItems().get(0).getProductName()).isEqualTo("Detail Product");
    }

    @Test
    @DisplayName("주문 목록 조회는 주문 항목과 상품 정보 응답 매핑이 가능하다.")
    void searchWithItemsAndProducts() {
        Order order = orderRepository.save(createOrder("List Product"));
        entityManager.flush();
        entityManager.clear();

        Page<Order> result = orderRepository.search(null, null, null, PageRequest.of(0, 10));
        Order foundOrder = result.getContent().get(0);
        assertThat(Hibernate.isInitialized(foundOrder.getOrderItems())).isTrue();
        assertThat(Hibernate.isInitialized(foundOrder.getOrderItems().get(0).getProduct())).isTrue();
        OrderResponse response = OrderResponse.from(foundOrder);

        assertThat(response.getId()).isEqualTo(order.getId());
        assertThat(response.getOrderItems()).hasSize(1);
        assertThat(response.getOrderItems().get(0).getProductName()).isEqualTo("List Product");
    }

    @Test
    @DisplayName("요청 페이지가 마지막 페이지를 넘어도 전체 주문 수를 유지한다.")
    void searchOutOfRangePageKeepsTotalCount() {
        orderRepository.save(createOrder("First Product"));
        orderRepository.save(createOrder("Second Product"));
        entityManager.flush();
        entityManager.clear();

        Page<Order> result = orderRepository.search(null, null, null, PageRequest.of(2, 10));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    private Order createOrder(String productName) {
        Product product = productRepository.save(Product.builder()
                .name(productName)
                .price(1000L)
                .stockQuantity(10)
                .category(Category.FOOD)
                .build());
        Order order = Order.createOrder();
        order.addOrderItem(product, 1);
        return order;
    }

    private void setCreatedAt(Long orderId, LocalDateTime createdAt) {
        entityManager.flush();
        jdbcTemplate.update("update orders set created_at = ? where id = ?", createdAt, orderId);
        entityManager.clear();
    }
}
