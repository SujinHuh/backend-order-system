package com.example.ordersystem.order.service;

import com.example.ordersystem.global.error.exception.BusinessException;
import com.example.ordersystem.order.domain.Order;
import com.example.ordersystem.order.domain.OrderRepository;
import com.example.ordersystem.order.domain.OrderStatus;
import com.example.ordersystem.product.domain.Category;
import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.domain.ProductRepository;
import com.example.ordersystem.product.exception.NotEnoughStockException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrderConcurrencyIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("같은 상품의 동시 주문 완료 요청은 성공한 주문 수만큼만 재고를 차감한다.")
    void concurrentComplete_sameProduct_deductsOnlyAvailableStock() throws Exception {
        Product product = productRepository.save(Product.builder()
                .name("Concurrent Product")
                .price(1000L)
                .stockQuantity(2)
                .category(Category.FOOD)
                .build());

        List<Long> orderIds = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Long orderId = orderService.createOrder(List.of(new OrderService.OrderItemRequest(product.getId(), 1))).getId();
            orderService.updateStatus(orderId, OrderStatus.ACCEPTED);
            orderIds.add(orderId);
        }

        List<Result> results = runConcurrently(orderIds.stream()
                .<Callable<Result>>map(orderId -> () -> completeOrder(orderId))
                .toList());

        long successCount = results.stream().filter(Result::isSuccess).count();
        long notEnoughStockCount = results.stream()
                .filter(result -> result.failure() instanceof NotEnoughStockException)
                .count();
        Product reloadedProduct = productRepository.findById(product.getId()).orElseThrow();
        long completedOrderCount = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .count();

        assertThat(successCount).isEqualTo(2);
        assertThat(notEnoughStockCount).isEqualTo(3);
        assertThat(reloadedProduct.getStockQuantity()).isZero();
        assertThat(completedOrderCount).isEqualTo(2);
    }

    @Test
    @DisplayName("같은 주문의 동시 완료 요청은 재고를 중복 차감하지 않는다.")
    void concurrentComplete_sameOrder_doesNotDeductStockTwice() throws Exception {
        Product product = productRepository.save(Product.builder()
                .name("Single Order Product")
                .price(1000L)
                .stockQuantity(10)
                .category(Category.FOOD)
                .build());
        Long orderId = orderService.createOrder(List.of(new OrderService.OrderItemRequest(product.getId(), 3))).getId();
        orderService.updateStatus(orderId, OrderStatus.ACCEPTED);

        List<Callable<Result>> tasks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            tasks.add(() -> completeOrder(orderId));
        }

        List<Result> results = runConcurrently(tasks);

        long successCount = results.stream().filter(Result::isSuccess).count();
        long invalidTransitionCount = results.stream()
                .filter(result -> result.failure() instanceof BusinessException)
                .count();
        Product reloadedProduct = productRepository.findById(product.getId()).orElseThrow();
        Order reloadedOrder = orderRepository.findById(orderId).orElseThrow();

        assertThat(successCount).isEqualTo(1);
        assertThat(invalidTransitionCount).isEqualTo(4);
        assertThat(reloadedProduct.getStockQuantity()).isEqualTo(7);
        assertThat(reloadedOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    private Result completeOrder(Long orderId) {
        try {
            orderService.updateStatus(orderId, OrderStatus.COMPLETED);
            return Result.success();
        } catch (RuntimeException exception) {
            return Result.failure(exception);
        }
    }

    private List<Result> runConcurrently(List<Callable<Result>> tasks) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(tasks.size());
        CountDownLatch ready = new CountDownLatch(tasks.size());
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Result>> futures = new ArrayList<>();
            for (Callable<Result> task : tasks) {
                futures.add(executorService.submit(() -> {
                    ready.countDown();
                    start.await();
                    return task.call();
                }));
            }
            ready.await();
            start.countDown();

            List<Result> results = new ArrayList<>();
            for (Future<Result> future : futures) {
                results.add(future.get());
            }
            return results;
        } finally {
            executorService.shutdownNow();
        }
    }

    private record Result(boolean isSuccess, RuntimeException failure) {

        static Result success() {
            return new Result(true, null);
        }

        static Result failure(RuntimeException exception) {
            return new Result(false, exception);
        }
    }
}
