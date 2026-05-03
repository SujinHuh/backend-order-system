package com.example.ordersystem.order.controller;

import com.example.ordersystem.global.error.GlobalExceptionHandler;
import com.example.ordersystem.order.controller.dto.OrderCreateRequest;
import com.example.ordersystem.order.controller.dto.OrderResponse;
import com.example.ordersystem.order.controller.dto.OrderStatusUpdateRequest;
import com.example.ordersystem.order.domain.Order;
import com.example.ordersystem.order.domain.OrderStatus;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

        Product product = createProduct(1L, "Product A", 1000L);
        Order order = Order.createOrder();
        order.addOrderItem(product, 2);
        ReflectionTestUtils.setField(order, "id", 1L);

        given(orderService.createOrder(any())).willReturn(OrderResponse.from(order));

        // when & then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.totalAmount").value(2000L));
    }

    @Test
    @DisplayName("주문 상태를 변경한다.")
    void updateStatus() throws Exception {
        // given
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest(OrderStatus.ACCEPTED);
        Product product = createProduct(1L, "Product A", 1000L);
        Order order = Order.createOrder();
        order.addOrderItem(product, 1);
        ReflectionTestUtils.setField(order, "id", 1L);
        
        given(orderService.updateStatus(any(), any())).willReturn(OrderResponse.from(order));

        // when & then
        mockMvc.perform(patch("/api/orders/{id}/status", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    private Product createProduct(Long id, String name, long price) {
        Product product = Product.builder()
                .name(name)
                .price(price)
                .stockQuantity(10)
                .category(Category.FOOD)
                .build();
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
