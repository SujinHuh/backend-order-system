package com.example.ordersystem.order.controller;

import com.example.ordersystem.order.domain.OrderRepository;
import com.example.ordersystem.order.domain.OrderStatus;
import com.example.ordersystem.order.service.OrderService;
import com.example.ordersystem.product.domain.Category;
import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.domain.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderQueryApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("주문 단건 API는 실제 DB 주문의 항목과 상품 정보를 반환한다.")
    void getOrder() throws Exception {
        Product product = productRepository.save(createProduct("Order Product"));
        Long orderId = orderService.createOrder(List.of(new OrderService.OrderItemRequest(product.getId(), 2))).getId();

        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.orderItems.length()").value(1))
                .andExpect(jsonPath("$.orderItems[0].productId").value(product.getId()))
                .andExpect(jsonPath("$.orderItems[0].productName").value("Order Product"))
                .andExpect(jsonPath("$.totalAmount").value(2000L));
    }

    @Test
    @DisplayName("주문 목록 API는 상태 조건과 페이징을 실제 DB 조회에 적용한다.")
    void getOrders_withStatusAndPaging() throws Exception {
        Product product = productRepository.save(createProduct("List Product"));
        Long pendingOrderId = orderService.createOrder(List.of(new OrderService.OrderItemRequest(product.getId(), 1))).getId();
        Long acceptedOrderId = orderService.createOrder(List.of(new OrderService.OrderItemRequest(product.getId(), 1))).getId();
        orderService.updateStatus(acceptedOrderId, OrderStatus.ACCEPTED);

        mockMvc.perform(get("/api/orders")
                .param("status", "PENDING")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(pendingOrderId))
                .andExpect(jsonPath("$.content[0].orderItems[0].productName").value("List Product"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("주문 목록 API는 상태와 생성 기간 조합 조건을 실제 DB 조회에 적용한다.")
    void getOrders_withStatusAndCreatedAtRange() throws Exception {
        Product product = productRepository.save(createProduct("Range Product"));
        Long acceptedOldOrderId = orderService.createOrder(List.of(new OrderService.OrderItemRequest(product.getId(), 1))).getId();
        orderService.updateStatus(acceptedOldOrderId, OrderStatus.ACCEPTED);
        Long acceptedTargetOrderId = orderService.createOrder(List.of(new OrderService.OrderItemRequest(product.getId(), 1))).getId();
        orderService.updateStatus(acceptedTargetOrderId, OrderStatus.ACCEPTED);
        Long pendingTargetOrderId = orderService.createOrder(List.of(new OrderService.OrderItemRequest(product.getId(), 1))).getId();
        setCreatedAt(acceptedOldOrderId, LocalDateTime.of(2026, 1, 1, 10, 0));
        setCreatedAt(acceptedTargetOrderId, LocalDateTime.of(2026, 1, 2, 10, 0));
        setCreatedAt(pendingTargetOrderId, LocalDateTime.of(2026, 1, 2, 11, 0));

        mockMvc.perform(get("/api/orders")
                .param("status", "ACCEPTED")
                .param("from", "2026-01-02T00:00:00")
                .param("to", "2026-01-02T23:59:00")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(acceptedTargetOrderId))
                .andExpect(jsonPath("$.content[0].orderItems[0].productName").value("Range Product"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("존재하지 않는 주문 단건 조회는 실제 예외 응답으로 반환된다.")
    void getOrder_notFound() throws Exception {
        mockMvc.perform(get("/api/orders/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("C003"));
    }

    @Test
    @DisplayName("주문 목록 API의 잘못된 날짜 형식은 실제 binding 오류 응답으로 반환된다.")
    void getOrders_invalidDateTime() throws Exception {
        mockMvc.perform(get("/api/orders")
                .param("from", "invalid-date-time"))
                .andExpect(status().isBadRequest());
    }

    private Product createProduct(String name) {
        return Product.builder()
                .name(name)
                .price(1000L)
                .stockQuantity(10)
                .category(Category.FOOD)
                .build();
    }

    private void setCreatedAt(Long orderId, LocalDateTime createdAt) {
        jdbcTemplate.update("update orders set created_at = ? where id = ?", createdAt, orderId);
    }
}
