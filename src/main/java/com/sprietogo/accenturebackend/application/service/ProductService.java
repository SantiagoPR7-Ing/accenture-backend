package com.sprietogo.accenturebackend.application.service;

import com.sprietogo.accenturebackend.application.port.repository.ProductRepositoryPort;
import com.sprietogo.accenturebackend.application.port.service.ProductServicePort;
import com.sprietogo.accenturebackend.domain.exception.ApiException;
import com.sprietogo.accenturebackend.domain.model.ProductEntity;
import com.sprietogo.accenturebackend.infrastructure.adapter.persistence.mapper.ProductMapper;
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
public class ProductService implements ProductServicePort {

    private final ProductRepositoryPort productRepositoryPort;

    @Override
    public Mono<ResponseDTO> saveProduct(RequestDTO requestDTO) {
        return Mono.justOrEmpty(requestDTO)
                .switchIfEmpty(Mono.error(ApiException.badRequest("PRODUCT_BODY_REQUIRED", "Request body is required")))
                .map(req -> {
                    String trimmed = req.getName() == null ? null : req.getName().trim();
                    req.setName(trimmed);
                    return req;
                })
                .flatMap(req -> {
                    if (req.getName() == null || req.getName().isBlank()) {
                        return Mono.error(ApiException.badRequest("PRODUCT_NAME_REQUIRED", "Product name is required"));
                    }

                    return productRepositoryPort.existsProductByName(req.getName())
                            .flatMap(exists -> {
                                if (exists) {
                                    return Mono.error(ApiException.conflict(
                                            "PRODUCT_ALREADY_EXISTS",
                                            "Product already exists: " + req.getName()
                                    ));
                                }

                                ProductEntity entity = ProductMapper.toEntity(req);

                                return productRepositoryPort.saveProduct(entity)
                                        .map(ProductMapper::toResponseDto)
                                        .onErrorMap(DataIntegrityViolationException.class, ex ->
                                                ApiException.conflict(
                                                        "PRODUCT_ALREADY_EXISTS",
                                                        "Product already exists: " + req.getName()
                                                )
                                        );
                            });
                });
    }

    @Override
    public Mono<ResponseDTO> updateProduct(String name, Long id) {
        if (id == null || id <= 0) {
            return Mono.error(ApiException.badRequest("PRODUCT_ID_REQUIRED", "Product id is required"));
        }

        return Mono.justOrEmpty(name)
                .map(String::trim)
                .switchIfEmpty(Mono.error(ApiException.badRequest("PRODUCT_NAME_REQUIRED", "Product name is required")))
                .flatMap(newName -> {
                    if (newName.isBlank()) {
                        return Mono.error(ApiException.badRequest("PRODUCT_NAME_REQUIRED", "Product name is required"));
                    }

                    return productRepositoryPort.getProductById(id)
                            .switchIfEmpty(Mono.error(ApiException.notFound(
                                    "PRODUCT_NOT_FOUND",
                                    "Product not found: " + id
                            )))
                            .flatMap(existing -> {

                                if (existing.getName() != null && existing.getName().equalsIgnoreCase(newName)) {
                                    return Mono.just(ProductMapper.toResponseDto(existing));
                                }

                                return productRepositoryPort.existsProductByName(newName)
                                        .flatMap(exists -> {
                                            if (exists) {
                                                return Mono.error(ApiException.conflict(
                                                        "PRODUCT_ALREADY_EXISTS",
                                                        "Product already exists: " + newName
                                                ));
                                            }

                                            RequestDTO req = new RequestDTO();
                                            req.setName(newName);

                                            ProductEntity toUpdate = ProductMapper.toEntity(req, id);

                                            // opcional: conservar timestamps
                                            toUpdate.setCreatedAt(existing.getCreatedAt());
                                            toUpdate.setUpdatedAt(existing.getUpdatedAt());

                                            return productRepositoryPort.saveProduct(toUpdate)
                                                    .map(ProductMapper::toResponseDto)
                                                    .onErrorMap(DataIntegrityViolationException.class, ex ->
                                                            ApiException.conflict(
                                                                    "PRODUCT_ALREADY_EXISTS",
                                                                    "Product already exists: " + newName
                                                            )
                                                    );
                                        });
                            });
                });
    }

    @Override
    public Mono<ResponseDTO> getProductByName(String name) {
        return Mono.justOrEmpty(name)
                .map(String::trim)
                .switchIfEmpty(Mono.error(ApiException.badRequest("PRODUCT_NAME_REQUIRED", "Product name is required")))
                .flatMap(cleanName -> {
                    if (cleanName.isBlank()) {
                        return Mono.error(ApiException.badRequest("PRODUCT_NAME_REQUIRED", "Product name is required"));
                    }

                    return productRepositoryPort.getProductByName(cleanName)
                            .switchIfEmpty(Mono.error(ApiException.notFound(
                                    "PRODUCT_NOT_FOUND",
                                    "Product not found: " + cleanName
                            )))
                            .map(ProductMapper::toResponseDto);
                });
    }
}
