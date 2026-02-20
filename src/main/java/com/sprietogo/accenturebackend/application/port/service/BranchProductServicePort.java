package com.sprietogo.accenturebackend.application.port.service;


import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.request.BranchProductRequestDTO;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.response.BranchProductResponseDTO;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.response.TopStockProductByBranchResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BranchProductServicePort {

    Mono<BranchProductResponseDTO> getBranchProductById(Long id);

    Mono<BranchProductResponseDTO> createBranchProduct(BranchProductRequestDTO request);

    Mono<BranchProductResponseDTO> updateBranchProduct(BranchProductRequestDTO request, Long id);

    Mono<Boolean> existsByBranchIdAndProductId(Long branchId, Long productId);

    Mono<Void> deleteByBranchIdAndProductId(Long branchId, Long productId);

    Flux<BranchProductResponseDTO> getAllByBranchId(Long branchId);

    Mono<BranchProductResponseDTO> getByBranchIdAndProductId(Long branchId, Long productId);

    Mono<BranchProductResponseDTO> updateBranchProductStock(Long branchId, Long productId, Integer stock);

    Flux<TopStockProductByBranchResponseDTO> getTopStockProductsByBranch(Long franchiseId);
}
