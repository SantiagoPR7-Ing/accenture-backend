package com.sprietogo.accenturebackend.application.port.repository;

import com.sprietogo.accenturebackend.domain.model.FranchiseEntity;
import reactor.core.publisher.Mono;

public interface FranchiseRepositoryPort {

    Mono<FranchiseEntity> saveFranchise(FranchiseEntity franchiseEntity);

    Mono<Boolean> existsFranchiseByName(String name);

    Mono<FranchiseEntity> getFranchiseByName(String name);

    Mono<FranchiseEntity> getFranchiseById(Long id);
}
