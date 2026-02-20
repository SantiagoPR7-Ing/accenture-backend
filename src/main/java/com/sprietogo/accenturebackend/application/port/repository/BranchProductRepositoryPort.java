package com.sprietogo.accenturebackend.application.port.repository;

import com.sprietogo.accenturebackend.domain.model.BranchProductEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface BranchProductRepositoryPort {

    Mono<BranchProductEntity> getBranchProductById(Long id);

    Mono<BranchProductEntity> saveBranchProduct(BranchProductEntity branchProductEntity);

    Mono<BranchProductEntity> updateBranchProduct(BranchProductEntity branchProductEntity);

    Mono<Boolean> existsByBranchIdAndProductId(Long branchId, Long productId);

    Mono<Void> deleteByBranchIdAndProductId(Long branchId, Long productId);

    Flux<BranchProductEntity> getAllByBranchId(Long branchId);

    Mono<BranchProductEntity> getByBranchIdAndProductId(Long branchId, Long productId);

    Mono<BranchProductEntity> updateBranchProductStock(Long branchId, Long productId, Integer stock);
}