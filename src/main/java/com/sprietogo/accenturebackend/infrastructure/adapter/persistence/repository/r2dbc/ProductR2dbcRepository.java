package com.sprietogo.accenturebackend.infrastructure.adapter.persistence.repository.r2dbc;

import com.sprietogo.accenturebackend.domain.model.ProductEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ProductR2dbcRepository extends R2dbcRepository<ProductEntity, Long> {

    Mono<Boolean> existsByName(String name);

    Mono<ProductEntity> findByName(String name);
}