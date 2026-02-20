package com.sprietogo.accenturebackend.infrastructure.adapter.persistence.repository.r2dbc;

import com.sprietogo.accenturebackend.domain.model.BranchProductEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface BranchProductR2dbcRepository extends R2dbcRepository<BranchProductEntity, Long> {

    Mono<Boolean> existsByBranchIdAndProductId(Long branchId, Long productId);

    Mono<Void> deleteByBranchIdAndProductId(Long branchId, Long productId);

    Flux<BranchProductEntity> findAllByBranchId(Long branchId);

    Mono<BranchProductEntity> findByBranchIdAndProductId(Long branchId, Long productId);

    @Query("""
        UPDATE branch_product
        SET stock = :stock
        WHERE branch_id = :branchId
          AND product_id = :productId
        RETURNING id, branch_id, product_id, stock, created_at, updated_at
        """)
    Mono<BranchProductEntity> updateStock(Long branchId, Long productId, Integer stock);
}