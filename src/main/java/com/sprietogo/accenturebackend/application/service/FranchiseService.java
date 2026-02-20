package com.sprietogo.accenturebackend.application.service;

import com.sprietogo.accenturebackend.application.port.repository.FranchiseRepositoryPort;
import com.sprietogo.accenturebackend.application.port.service.FranchiseServicePort;
import com.sprietogo.accenturebackend.domain.exception.ApiException;
import com.sprietogo.accenturebackend.domain.model.FranchiseEntity;
import com.sprietogo.accenturebackend.infrastructure.adapter.persistence.mapper.FranchiseMapper;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.request.RequestDTO;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.response.ResponseDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@AllArgsConstructor
public class FranchiseService implements FranchiseServicePort {

    private final FranchiseRepositoryPort franchiseRepositoryPort;

    @Override
    public Mono<ResponseDTO> createFranchise(RequestDTO requestDTO) {
        return Mono.justOrEmpty(requestDTO)
                .switchIfEmpty(Mono.error(ApiException.badRequest("FRANCHISE_BODY_REQUIRED", "Request body is required")))
                .map(req -> {
                    String trimmed = req.getName() == null ? null : req.getName().trim();
                    req.setName(trimmed);
                    return req;
                })
                .flatMap(req -> {
                    if (req.getName() == null || req.getName().isBlank()) {
                        return Mono.error(ApiException.badRequest("FRANCHISE_NAME_REQUIRED", "Franchise name is required"));
                    }

                    return franchiseRepositoryPort.existsFranchiseByName(req.getName())
                            .flatMap(exists -> {
                                if (exists) {
                                    return Mono.error(ApiException.conflict(
                                            "FRANCHISE_ALREADY_EXISTS",
                                            "Franchise already exists: " + req.getName()
                                    ));
                                }

                                FranchiseEntity entity = FranchiseMapper.toEntity(req);

                                return franchiseRepositoryPort.saveFranchise(entity)
                                        .map(FranchiseMapper::toResponseDto)
                                        .onErrorMap(DataIntegrityViolationException.class, ex ->
                                                ApiException.conflict(
                                                        "FRANCHISE_ALREADY_EXISTS",
                                                        "Franchise already exists: " + req.getName()
                                                )
                                        );
                            });
                });
    }

    @Override
    public Mono<ResponseDTO> updateFranchise(String name, Long id) {
        if (id == null || id <= 0) {
            return Mono.error(ApiException.badRequest("FRANCHISE_ID_REQUIRED", "Franchise id is required"));
        }

        return Mono.justOrEmpty(name)
                .map(String::trim)
                .switchIfEmpty(Mono.error(ApiException.badRequest("FRANCHISE_NAME_REQUIRED", "Franchise name is required")))
                .flatMap(newName -> {
                    if (newName.isBlank()) {
                        return Mono.error(ApiException.badRequest("FRANCHISE_NAME_REQUIRED", "Franchise name is required"));
                    }

                    return franchiseRepositoryPort.getFranchiseById(id)
                            .switchIfEmpty(Mono.error(ApiException.notFound(
                                    "FRANCHISE_NOT_FOUND",
                                    "Franchise not found: " + id
                            )))
                            .flatMap(existing -> {
                                if (existing.getName() != null && existing.getName().equalsIgnoreCase(newName)) {
                                    return Mono.just(FranchiseMapper.toResponseDto(existing));
                                }

                                return franchiseRepositoryPort.existsFranchiseByName(newName)
                                        .flatMap(exists -> {
                                            if (exists) {
                                                return Mono.error(ApiException.conflict(
                                                        "FRANCHISE_ALREADY_EXISTS",
                                                        "Franchise already exists: " + newName
                                                ));
                                            }

                                            RequestDTO req = new RequestDTO();
                                            req.setName(newName);

                                            FranchiseEntity toUpdate = FranchiseMapper.toEntity(req, id);
                                            toUpdate.setCreatedAt(existing.getCreatedAt());
                                            toUpdate.setUpdatedAt(existing.getUpdatedAt());

                                            return franchiseRepositoryPort.saveFranchise(toUpdate)
                                                    .map(FranchiseMapper::toResponseDto)
                                                    .onErrorMap(DataIntegrityViolationException.class, ex ->
                                                            ApiException.conflict(
                                                                    "FRANCHISE_ALREADY_EXISTS",
                                                                    "Franchise already exists: " + newName
                                                            )
                                                    );
                                        });
                            });
                });
    }

    @Override
    public Mono<ResponseDTO> getFranchise(String name) {
        return Mono.justOrEmpty(name)
                .map(String::trim)
                .switchIfEmpty(Mono.error(ApiException.badRequest("FRANCHISE_NAME_REQUIRED", "Franchise name is required")))
                .flatMap(cleanName -> {
                    if (cleanName.isBlank()) {
                        return Mono.error(ApiException.badRequest("FRANCHISE_NAME_REQUIRED", "Franchise name is required"));
                    }

                    return franchiseRepositoryPort.getFranchiseByName(cleanName)
                            .switchIfEmpty(Mono.error(ApiException.notFound(
                                    "FRANCHISE_NOT_FOUND",
                                    "Franchise not found: " + cleanName
                            )))
                            .map(FranchiseMapper::toResponseDto);
                });
    }
}
