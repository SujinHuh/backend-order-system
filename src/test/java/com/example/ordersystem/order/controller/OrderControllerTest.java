package com.example.ordersystem.order.controller;

import com.example.ordersystem.global.error.GlobalExceptionHandler;
import com.example.ordersystem.order.controller.dto.OrderCreateRequest;
import com.example.ordersystem.order.domain.Order;
import com.example.ordersystem.order.service.OrderService;
import com.example.ordersystem.product.domain.Category;
import com.example.ordersystem.product.domain.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    @DisplayName("주문을 생성한다.")
    void createOrder() throws Exception {
        // given
        OrderCreateRequest.OrderItemRequest item = new OrderCreateRequest.OrderItemRequest(1L, 2);
        OrderCreateRequest request = new OrderCreateRequest(List.of(item));

        Product product = Product.builder().name("Product A").price(1000L).stockQuantity(10).category(Category.FOOD).build();
        ReflectionTestUtils.setField(product, "id", 1L);

        Order order = Order.createOrder();
        order.addOrderItem(product, 2);
        ReflectionTestUtils.setField(order, "id", 1L);

        given(orderService.createOrder(any())).willReturn(order);

        // when & then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.totalAmount").value(2000L));
    }

    @Test
    @DisplayName("주문 항목이 없으면 400 에러가 발생한다.")
    void createOrder_emptyItems() throws Exception {
        // given
        OrderCreateRequest request = new OrderCreateRequest(List.of());

        // when & then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    @Test
    @DisplayName("수량이 1보다 작으면 400 에러가 발생한다.")
    void createOrder_invalidQuantity() throws Exception {
        // given
        OrderCreateRequest.OrderItemRequest item = new OrderCreateRequest.OrderItemRequest(1L, 0);
        OrderCreateRequest request = new OrderCreateRequest(List.of(item));

        // when & then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    @Test
    @DisplayName("존재하지 않는 상품으로 주문하면 404 에러가 발생한다.")
    void createOrder_productNotFound() throws Exception {
        // given
        OrderCreateRequest.OrderItemRequest item = new OrderCreateRequest.OrderItemRequest(999L, 1);
        OrderCreateRequest request = new OrderCreateRequest(List.of(item));
        given(orderService.createOrder(any())).willThrow(new com.example.ordersystem.global.error.exception.EntityNotFoundException("Product not found"));

        // when & then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("C003"));
    }
}
