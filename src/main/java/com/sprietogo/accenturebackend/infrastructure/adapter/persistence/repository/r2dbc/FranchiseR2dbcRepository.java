package com.sprietogo.accenturebackend.infrastructure.adapter.persistence.repository.r2dbc;

import com.sprietogo.accenturebackend.domain.model.FranchiseEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface FranchiseR2dbcRepository extends R2dbcRepository<FranchiseEntity, Long> {

    Mono<Boolean> existsByName(String name);

    Mono<FranchiseEntity> findByName(String name);
}
