package com.sprietogo.accenturebackend.infrastructure.adapter.persistence.repository;

import com.sprietogo.accenturebackend.application.port.repository.BranchRepositoryPort;
import com.sprietogo.accenturebackend.domain.model.BranchEntity;
import com.sprietogo.accenturebackend.infrastructure.adapter.persistence.repository.r2dbc.BranchR2dbcRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@AllArgsConstructor
public class BranchRepository implements BranchRepositoryPort {

    private final BranchR2dbcRepository repository;

    @Override
    public Mono<BranchEntity> saveBranch(BranchEntity entity) {
        return repository.save(entity);
    }

    @Override
    public Flux<BranchEntity> getAllByFranchiseId(Long franchiseId) {
        return repository.findAllByFranchiseId(franchiseId);
    }

    @Override
    public Mono<Boolean> existsBranchByFranchiseIdAndName(Long franchiseId, String name) {
        return repository.existsByFranchiseIdAndName(franchiseId, name);
    }

    @Override
    public Mono<BranchEntity> getBranchByFranchiseIdAndName(Long franchiseId, String name) {
        return repository.findByFranchiseIdAndName(franchiseId, name);
    }

    @Override
    public Mono<BranchEntity> getBranchById(Long id) {
        return repository.findById(id);
    }
}
