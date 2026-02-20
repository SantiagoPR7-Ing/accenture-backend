package com.sprietogo.accenturebackend.infrastructure.adapter.persistence.repository;

import com.sprietogo.accenturebackend.application.port.repository.FranchiseRepositoryPort;
import com.sprietogo.accenturebackend.domain.model.FranchiseEntity;
import com.sprietogo.accenturebackend.infrastructure.adapter.persistence.repository.r2dbc.FranchiseR2dbcRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@AllArgsConstructor
public class FranchiseRepository implements FranchiseRepositoryPort {

    private final FranchiseR2dbcRepository repository;

    @Override
    public Mono<FranchiseEntity> saveFranchise(FranchiseEntity franchiseEntity) {
        return repository.save(franchiseEntity);
    }

    @Override
    public Mono<Boolean> existsFranchiseByName(String name) {
        return repository.existsByName(name);
    }

    @Override
    public Mono<FranchiseEntity> getFranchiseByName(String name) {
        return repository.findByName(name);
    }

    @Override
    public Mono<FranchiseEntity> getFranchiseById(Long id) {
        return repository.findById(id);
    }
}
