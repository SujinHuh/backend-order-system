package com.example.ordersystem.product.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {

    Page<Product> search(Category category, Pageable pageable);
}
