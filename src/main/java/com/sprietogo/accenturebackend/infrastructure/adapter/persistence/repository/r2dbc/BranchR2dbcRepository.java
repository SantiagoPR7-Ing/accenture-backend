package com.sprietogo.accenturebackend.infrastructure.adapter.persistence.repository.r2dbc;

import com.sprietogo.accenturebackend.domain.model.BranchEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface BranchR2dbcRepository extends R2dbcRepository<BranchEntity, Long> {

    Flux<BranchEntity> findAllByFranchiseId(Long franchiseId);

    Mono<Boolean> existsByFranchiseIdAndName(Long franchiseId, String name);

    Mono<BranchEntity> findByFranchiseIdAndName(Long franchiseId, String name);
}