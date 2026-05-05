package com.example.ordersystem.order.domain;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.example.ordersystem.order.domain.QOrder.order;
import static com.example.ordersystem.order.domain.QOrderItem.orderItem;

public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public OrderRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public Optional<Order> findByIdWithItemsAndProducts(Long id) {
        return Optional.ofNullable(queryFactory
                .selectFrom(order)
                .distinct()
                .leftJoin(order.orderItems, orderItem).fetchJoin()
                .leftJoin(orderItem.product).fetchJoin()
                .where(order.id.eq(id))
                .fetchOne());
    }

    @Override
    public Page<Order> search(OrderStatus status, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        List<Long> orderIds = queryFactory
                .select(order.id)
                .from(order)
                .where(
                        statusEq(status),
                        createdAtGoe(from),
                        createdAtLoe(to)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderSpecifiers(pageable))
                .fetch();

        if (orderIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, count(status, from, to));
        }

        List<Order> content = fetchOrdersWithItemsAndProducts(orderIds);

        return new PageImpl<>(content, pageable, count(status, from, to));
    }

    private long count(OrderStatus status, LocalDateTime from, LocalDateTime to) {
        Long count = queryFactory
                .select(order.count())
                .from(order)
                .where(
                        statusEq(status),
                        createdAtGoe(from),
                        createdAtLoe(to)
                )
                .fetchOne();
        return count == null ? 0L : count;
    }

    private List<Order> fetchOrdersWithItemsAndProducts(List<Long> orderIds) {
        Map<Long, Integer> orderIndex = indexByOrderId(orderIds);
        return queryFactory
                .selectFrom(order)
                .distinct()
                .leftJoin(order.orderItems, orderItem).fetchJoin()
                .leftJoin(orderItem.product).fetchJoin()
                .where(order.id.in(orderIds))
                .fetch()
                .stream()
                .sorted(Comparator.comparingInt(foundOrder -> orderIndex.get(foundOrder.getId())))
                .toList();
    }

    private Map<Long, Integer> indexByOrderId(List<Long> orderIds) {
        return IntStream.range(0, orderIds.size())
                .boxed()
                .collect(Collectors.toMap(orderIds::get, index -> index));
    }

    private BooleanExpression statusEq(OrderStatus status) {
        return status == null ? null : order.status.eq(status);
    }

    private BooleanExpression createdAtGoe(LocalDateTime from) {
        return from == null ? null : order.createdAt.goe(from);
    }

    private BooleanExpression createdAtLoe(LocalDateTime to) {
        return to == null ? null : order.createdAt.loe(to);
    }

    private OrderSpecifier<?>[] orderSpecifiers(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return new OrderSpecifier<?>[]{order.id.desc()};
        }

        return pageable.getSort().stream()
                .map(sortOrder -> new OrderSpecifier<>(
                        sortOrder.isAscending() ? com.querydsl.core.types.Order.ASC : com.querydsl.core.types.Order.DESC,
                        orderSortProperty(sortOrder.getProperty())
                ))
                .toArray(OrderSpecifier[]::new);
    }

    private ComparableExpressionBase<?> orderSortProperty(String property) {
        return switch (property) {
            case "status" -> order.status;
            case "createdAt" -> order.createdAt;
            case "updatedAt" -> order.updatedAt;
            default -> order.id;
        };
    }
}
