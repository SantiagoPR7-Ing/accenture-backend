package com.sprietogo.accenturebackend.application.port.repository;

import com.sprietogo.accenturebackend.domain.model.BranchEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BranchRepositoryPort {

    Mono<BranchEntity> saveBranch(BranchEntity entity);

    Flux<BranchEntity> getAllByFranchiseId(Long franchiseId);

    Mono<Boolean> existsBranchByFranchiseIdAndName(Long franchiseId, String name);

    Mono<BranchEntity> getBranchByFranchiseIdAndName(Long franchiseId, String name);
    Mono<BranchEntity> getBranchById(Long id);

}
