package com.sprietogo.accenturebackend.application.port.service;

import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.request.BranchRequestDTO;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.response.BranchResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BranchServicePort {

    Mono<BranchResponseDTO> createBranch(BranchRequestDTO requestDTO);
    Mono<BranchResponseDTO> updateBranch(String name, Long id);
    Flux<BranchResponseDTO> getAllByFranchiseId(Long franchiseId);
    Mono<BranchResponseDTO> getBranch(Long franchiseId, String name);
}
