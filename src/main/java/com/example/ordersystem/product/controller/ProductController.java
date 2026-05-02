package com.example.ordersystem.product.controller;

import com.example.ordersystem.product.controller.dto.ProductCreateRequest;
import com.example.ordersystem.product.controller.dto.ProductResponse;
import com.example.ordersystem.product.controller.dto.ProductUpdateRequest;
import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@RequestBody @Valid ProductCreateRequest request) {
        Product product = productService.createProduct(
                request.getName(),
                request.getPrice(),
                request.getStockQuantity(),
                request.getCategory()
        );
        return ProductResponse.from(product);
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(
            @PathVariable Long id,
            @RequestBody @Valid ProductUpdateRequest request) {
        Product product = productService.updateProduct(
                id,
                request.getName(),
                request.getPrice(),
                request.getStockQuantity(),
                request.getCategory()
        );
        return ProductResponse.from(product);
    }

    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        Product product = productService.getProduct(id);
        return ProductResponse.from(product);
    }

    @GetMapping
    public Page<ProductResponse> getProducts(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Product> products = productService.getProducts(pageable);
        return products.map(ProductResponse::from);
    }
}
