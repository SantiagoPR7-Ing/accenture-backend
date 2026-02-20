package com.sprietogo.accenturebackend.infrastructure.adapter.persistence.repository;

import com.sprietogo.accenturebackend.application.port.repository.ProductRepositoryPort;
import com.sprietogo.accenturebackend.domain.model.ProductEntity;
import com.sprietogo.accenturebackend.infrastructure.adapter.persistence.repository.r2dbc.ProductR2dbcRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@AllArgsConstructor
public class ProductRepository implements ProductRepositoryPort {

    private final ProductR2dbcRepository repository;

    @Override
    public Mono<ProductEntity> saveProduct(ProductEntity productEntity) {
        return repository.save(productEntity);
    }

    @Override
    public Mono<Boolean> existsProductByName(String name) {
        return repository.existsByName(name);
    }

    @Override
    public Mono<ProductEntity> getProductByName(String name) {
        return repository.findByName(name);
    }

    @Override
    public Mono<ProductEntity> getProductById(Long id) {
        return repository.findById(id);
    }
}