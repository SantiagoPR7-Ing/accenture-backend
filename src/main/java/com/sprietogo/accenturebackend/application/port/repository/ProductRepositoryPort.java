package com.sprietogo.accenturebackend.application.port.repository;

import com.sprietogo.accenturebackend.domain.model.ProductEntity;
import reactor.core.publisher.Mono;

public interface ProductRepositoryPort {

    Mono<ProductEntity> saveProduct(ProductEntity productEntity);

    Mono<Boolean> existsProductByName(String name);

    Mono<ProductEntity> getProductByName(String name);

    Mono<ProductEntity> getProductById(Long id);

}