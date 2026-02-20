package com.sprietogo.accenturebackend.application.service;

import com.sprietogo.accenturebackend.application.port.repository.BranchRepositoryPort;
import com.sprietogo.accenturebackend.application.port.repository.FranchiseRepositoryPort;
import com.sprietogo.accenturebackend.application.port.service.BranchServicePort;
import com.sprietogo.accenturebackend.domain.exception.ApiException;
import com.sprietogo.accenturebackend.domain.model.BranchEntity;
import com.sprietogo.accenturebackend.domain.model.FranchiseEntity;
import com.sprietogo.accenturebackend.infrastructure.adapter.persistence.mapper.BranchMapper;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.request.BranchRequestDTO;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.response.BranchResponseDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@AllArgsConstructor
public class BranchService implements BranchServicePort {

    private final BranchRepositoryPort branchRepositoryPort;
    private final FranchiseRepositoryPort franchiseRepositoryPort;

    @Override
    public Mono<BranchResponseDTO> createBranch(BranchRequestDTO requestDTO) {
        return Mono.justOrEmpty(requestDTO)
                .switchIfEmpty(Mono.error(ApiException.badRequest("BRANCH_BODY_REQUIRED", "Request body is required")))
                .map(req -> {
                    String trimmed = req.getName() == null ? null : req.getName().trim();
                    req.setName(trimmed);
                    return req;
                })
                .flatMap(req -> {
                    Long franchiseId = req.getFranchiseId();
                    String name = req.getName();

                    if (franchiseId == null || franchiseId <= 0) {
                        return Mono.error(ApiException.badRequest("BRANCH_FRANCHISE_ID_REQUIRED", "franchiseId is required"));
                    }
                    if (name == null || name.isBlank()) {
                        return Mono.error(ApiException.badRequest("BRANCH_NAME_REQUIRED", "Branch name is required"));
                    }

                    Mono<FranchiseEntity> franchiseMono = franchiseRepositoryPort.getFranchiseById(franchiseId)
                            .switchIfEmpty(Mono.error(ApiException.notFound(
                                    "FRANCHISE_NOT_FOUND",
                                    "Franchise not found: " + franchiseId
                            )));

                    return franchiseMono.flatMap(franchise ->
                            branchRepositoryPort.existsBranchByFranchiseIdAndName(franchiseId, name)
                                    .flatMap(exists -> {
                                        if (exists) {
                                            return Mono.error(ApiException.conflict(
                                                    "BRANCH_ALREADY_EXISTS",
                                                    "Branch already exists in franchiseId=" + franchiseId + ": " + name
                                            ));
                                        }

                                        BranchEntity entity = BranchMapper.toEntity(req);

                                        return branchRepositoryPort.saveBranch(entity)
                                                .map(saved -> BranchMapper.toResponseDto(saved, franchise))
                                                .onErrorMap(DataIntegrityViolationException.class, ex ->
                                                        ApiException.conflict(
                                                                "BRANCH_ALREADY_EXISTS",
                                                                "Branch already exists in franchiseId=" + franchiseId + ": " + name
                                                        )
                                                );
                                    })
                    );
                });
    }

    @Override
    public Mono<BranchResponseDTO> updateBranch(String name, Long id) {
        if (id == null || id <= 0) {
            return Mono.error(ApiException.badRequest("BRANCH_ID_REQUIRED", "Branch id is required"));
        }

        return Mono.justOrEmpty(name)
                .map(String::trim)
                .switchIfEmpty(Mono.error(ApiException.badRequest("BRANCH_NAME_REQUIRED", "Branch name is required")))
                .flatMap(newName -> {
                    if (newName.isBlank()) {
                        return Mono.error(ApiException.badRequest("BRANCH_NAME_REQUIRED", "Branch name is required"));
                    }

                    return branchRepositoryPort.getBranchById(id)
                            .switchIfEmpty(Mono.error(ApiException.notFound(
                                    "BRANCH_NOT_FOUND",
                                    "Branch not found: " + id
                            )))
                            .flatMap(existingBranch -> {
                                Long franchiseId = existingBranch.getFranchiseId();

                                Mono<FranchiseEntity> franchiseMono = franchiseRepositoryPort.getFranchiseById(franchiseId)
                                        .switchIfEmpty(Mono.error(ApiException.notFound(
                                                "FRANCHISE_NOT_FOUND",
                                                "Franchise not found: " + franchiseId
                                        )));

                                if (existingBranch.getName() != null && existingBranch.getName().equalsIgnoreCase(newName)) {
                                    return franchiseMono.map(franchise -> BranchMapper.toResponseDto(existingBranch, franchise));
                                }

                                Mono<Boolean> existsMono = branchRepositoryPort.existsBranchByFranchiseIdAndName(franchiseId, newName);

                                return Mono.zip(franchiseMono, existsMono)
                                        .flatMap(tuple -> {
                                            FranchiseEntity franchise = tuple.getT1();
                                            Boolean exists = tuple.getT2();

                                            if (exists) {
                                                return Mono.error(ApiException.conflict(
                                                        "BRANCH_ALREADY_EXISTS",
                                                        "Branch already exists in franchiseId=" + franchiseId + ": " + newName
                                                ));
                                            }

                                            BranchRequestDTO req = new BranchRequestDTO();
                                            req.setFranchiseId(franchiseId);
                                            req.setName(newName);

                                            BranchEntity toUpdate = BranchMapper.toEntity(req, id);

                                            toUpdate.setCreatedAt(existingBranch.getCreatedAt());
                                            toUpdate.setUpdatedAt(existingBranch.getUpdatedAt());

                                            return branchRepositoryPort.saveBranch(toUpdate)
                                                    .map(saved -> BranchMapper.toResponseDto(saved, franchise))
                                                    .onErrorMap(DataIntegrityViolationException.class, ex ->
                                                            ApiException.conflict(
                                                                    "BRANCH_ALREADY_EXISTS",
                                                                    "Branch already exists in franchiseId=" + franchiseId + ": " + newName
                                                            )
                                                    );
                                        });
                            });
                });
    }

    @Override
    public Flux<BranchResponseDTO> getAllByFranchiseId(Long franchiseId) {
        if (franchiseId == null || franchiseId <= 0) {
            return Flux.error(ApiException.badRequest("BRANCH_FRANCHISE_ID_REQUIRED", "franchiseId is required"));
        }

        Mono<FranchiseEntity> franchiseMono = franchiseRepositoryPort.getFranchiseById(franchiseId)
                .switchIfEmpty(Mono.error(ApiException.notFound(
                        "FRANCHISE_NOT_FOUND",
                        "Franchise not found: " + franchiseId
                )))
                .cache();

        return franchiseMono.flatMapMany(franchise ->
                branchRepositoryPort.getAllByFranchiseId(franchiseId)
                        .map(branch -> BranchMapper.toResponseDto(branch, franchise))
        );
    }

    @Override
    public Mono<BranchResponseDTO> getBranch(Long franchiseId, String name) {
        if (franchiseId == null || franchiseId <= 0) {
            return Mono.error(ApiException.badRequest("BRANCH_FRANCHISE_ID_REQUIRED", "franchiseId is required"));
        }

        String cleanName = name == null ? null : name.trim();
        if (cleanName == null || cleanName.isBlank()) {
            return Mono.error(ApiException.badRequest("BRANCH_NAME_REQUIRED", "Branch name is required"));
        }

        Mono<FranchiseEntity> franchiseMono = franchiseRepositoryPort.getFranchiseById(franchiseId)
                .switchIfEmpty(Mono.error(ApiException.notFound(
                        "FRANCHISE_NOT_FOUND",
                        "Franchise not found: " + franchiseId
                )));

        Mono<BranchEntity> branchMono = branchRepositoryPort.getBranchByFranchiseIdAndName(franchiseId, cleanName)
                .switchIfEmpty(Mono.error(ApiException.notFound(
                        "BRANCH_NOT_FOUND",
                        "Branch not found in franchiseId=" + franchiseId + ": " + cleanName
                )));

        return Mono.zip(franchiseMono, branchMono)
                .map(tuple -> BranchMapper.toResponseDto(tuple.getT2(), tuple.getT1()));
    }
}
