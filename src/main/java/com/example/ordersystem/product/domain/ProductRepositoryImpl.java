package com.example.ordersystem.product.domain;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.example.ordersystem.product.domain.QProduct.product;

public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public ProductRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public Page<Product> search(Category category, Pageable pageable) {
        List<Product> content = queryFactory
                .selectFrom(product)
                .where(categoryEq(category))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderSpecifiers(pageable))
                .fetch();

        return new PageImpl<>(content, pageable, count(category));
    }

    private long count(Category category) {
        Long count = queryFactory
                .select(product.count())
                .from(product)
                .where(categoryEq(category))
                .fetchOne();
        return count == null ? 0L : count;
    }

    private BooleanExpression categoryEq(Category category) {
        return category == null ? null : product.category.eq(category);
    }

    private OrderSpecifier<?>[] orderSpecifiers(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return new OrderSpecifier<?>[]{product.id.desc()};
        }

        return pageable.getSort().stream()
                .map(order -> new OrderSpecifier<>(
                        order.isAscending() ? Order.ASC : Order.DESC,
                        productSortProperty(order.getProperty())
                ))
                .toArray(OrderSpecifier[]::new);
    }

    private ComparableExpressionBase<?> productSortProperty(String property) {
        return switch (property) {
            case "name" -> product.name;
            case "price" -> product.price;
            case "stockQuantity" -> product.stockQuantity;
            case "category" -> product.category;
            case "createdAt" -> product.createdAt;
            case "updatedAt" -> product.updatedAt;
            default -> product.id;
        };
    }
}
