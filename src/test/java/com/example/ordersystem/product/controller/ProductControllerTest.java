package com.example.ordersystem.product.controller;

import com.example.ordersystem.product.controller.dto.ProductCreateRequest;
import com.example.ordersystem.product.controller.dto.ProductResponse;
import com.example.ordersystem.product.controller.dto.ProductUpdateRequest;
import com.example.ordersystem.product.domain.Category;
import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.service.ProductService;
import com.example.ordersystem.global.error.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    @DisplayName("상품을 등록한다.")
    void createProduct() throws Exception {
        // given
        ProductCreateRequest request = new ProductCreateRequest("Product A", 1000L, 10, Category.FOOD);
        Product product = createProduct(1L, "Product A", 1000L, 10);
        given(productService.createProduct(any(), any(Long.class), any(Integer.class), any())).willReturn(product);

        // when & then
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Product A"));
    }

    @Test
    @DisplayName("상품을 수정한다.")
    void updateProduct() throws Exception {
        // given
        ProductUpdateRequest request = new ProductUpdateRequest("Updated Product", 2000L, 20, Category.FASHION);
        Product product = createProduct(1L, "Updated Product", 2000L, 20);
        given(productService.updateProduct(any(), any(), any(Long.class), any(Integer.class), any())).willReturn(product);

        // when & then
        mockMvc.perform(put("/api/products/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Product"));
    }

    @Test
    @DisplayName("상품을 단건 조회한다.")
    void getProduct() throws Exception {
        // given
        Product product = createProduct(1L, "Product A", 1000L, 10);
        given(productService.getProduct(1L)).willReturn(product);

        // when & then
        mockMvc.perform(get("/api/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Product A"));
    }

    @Test
    @DisplayName("상품 목록을 페이징 조회한다.")
    void getProducts() throws Exception {
        // given
        Product product = createProduct(1L, "Product A", 1000L, 10);
        Page<Product> page = new PageImpl<>(List.of(product));
        given(productService.getProducts(any(), any(Pageable.class))).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/products")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Product A"));
    }

    @Test
    @DisplayName("카테고리 조건으로 상품 목록을 페이징 조회한다.")
    void getProductsByCategory() throws Exception {
        // given
        Product product = createProduct(1L, "Food Product", 1000L, 10);
        Page<Product> page = new PageImpl<>(List.of(product));
        given(productService.getProducts(any(), any(Pageable.class))).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/products")
                .param("category", "FOOD")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].category").value("FOOD"))
                .andExpect(jsonPath("$.content[0].name").value("Food Product"));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productService).getProducts(eq(Category.FOOD), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("존재하지 않는 상품을 조회하면 404 에러가 발생한다.")
    void getProduct_notFound() throws Exception {
        // given
        given(productService.getProduct(999L)).willThrow(new com.example.ordersystem.global.error.exception.EntityNotFoundException("Product not found"));

        // when & then
        mockMvc.perform(get("/api/products/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("C003"));
    }

    @Test
    @DisplayName("상품 수정 시 필수 값이 누락되면 400 에러가 발생한다.")
    void updateProduct_invalidInput() throws Exception {
        // given
        ProductUpdateRequest request = new ProductUpdateRequest("", null, -1, null);

        // when & then
        mockMvc.perform(put("/api/products/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("상품 등록 시 재고 수량이 누락되면 400 에러가 발생한다.")
    void createProduct_missingStockQuantity() throws Exception {
        // given
        ProductCreateRequest request = new ProductCreateRequest("Product A", 1000L, null, Category.FOOD);

        // when & then
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("상품 등록 시 상품명이 100자를 초과하면 400 에러가 발생한다.")
    void createProduct_nameTooLong() throws Exception {
        // given
        ProductCreateRequest request = new ProductCreateRequest("a".repeat(101), 1000L, 10, Category.FOOD);

        // when & then
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("상품 수정 시 재고 수량이 누락되면 400 에러가 발생한다.")
    void updateProduct_missingStockQuantity() throws Exception {
        // given
        ProductUpdateRequest request = new ProductUpdateRequest("Updated Product", 2000L, null, Category.FASHION);

        // when & then
        mockMvc.perform(put("/api/products/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    private Product createProduct(Long id, String name, long price, int stockQuantity) {
        Product product = Product.builder()
                .name(name)
                .price(price)
                .stockQuantity(stockQuantity)
                .category(Category.FOOD)
                .build();
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
