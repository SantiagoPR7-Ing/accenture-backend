package com.sprietogo.accenturebackend.infrastructure.adapter.persistence.repository;

import com.sprietogo.accenturebackend.application.port.repository.BranchProductRepositoryPort;
import com.sprietogo.accenturebackend.domain.model.BranchProductEntity;
import com.sprietogo.accenturebackend.infrastructure.adapter.persistence.repository.r2dbc.BranchProductR2dbcRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@AllArgsConstructor
public class BranchProductRepository implements BranchProductRepositoryPort {

    private final BranchProductR2dbcRepository repository;


    @Override
    public Mono<BranchProductEntity> getBranchProductById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Mono<BranchProductEntity> saveBranchProduct(BranchProductEntity entity) {
        return repository.save(entity);
    }

    @Override
    public Mono<BranchProductEntity> updateBranchProduct(BranchProductEntity entity) {
        return repository.save(entity);
    }

    @Override
    public Mono<Boolean> existsByBranchIdAndProductId(Long branchId, Long productId) {
        return repository.existsByBranchIdAndProductId(branchId, productId);
    }

    @Override
    public Mono<Void> deleteByBranchIdAndProductId(Long branchId, Long productId) {
        return repository.deleteByBranchIdAndProductId(branchId, productId);
    }

    @Override
    public Flux<BranchProductEntity> getAllByBranchId(Long branchId) {
        return repository.findAllByBranchId(branchId);
    }

    @Override
    public Mono<BranchProductEntity> getByBranchIdAndProductId(Long branchId, Long productId) {
        return repository.findByBranchIdAndProductId(branchId, productId);
    }

    @Override
    public Mono<BranchProductEntity> updateBranchProductStock(Long branchId, Long productId, Integer stock) {
        return repository.updateStock(branchId, productId, stock);
    }
}
