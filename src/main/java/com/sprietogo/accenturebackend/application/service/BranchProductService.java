package com.sprietogo.accenturebackend.application.service;

import com.sprietogo.accenturebackend.application.port.repository.BranchProductRepositoryPort;
import com.sprietogo.accenturebackend.application.port.repository.BranchRepositoryPort;
import com.sprietogo.accenturebackend.application.port.repository.ProductRepositoryPort;
import com.sprietogo.accenturebackend.application.port.service.BranchProductServicePort;
import com.sprietogo.accenturebackend.domain.exception.ApiException;
import com.sprietogo.accenturebackend.domain.model.BranchEntity;
import com.sprietogo.accenturebackend.domain.model.BranchProductEntity;
import com.sprietogo.accenturebackend.domain.model.ProductEntity;
import com.sprietogo.accenturebackend.infrastructure.adapter.persistence.mapper.BranchProductMapper;
import com.sprietogo.accenturebackend.infrastructure.adapter.persistence.repository.query.ReportingQueryRepository;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.request.BranchProductRequestDTO;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.response.BranchProductResponseDTO;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.response.ResponseDTO;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.response.TopStockProductByBranchResponseDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.sprietogo.accenturebackend.utils.Constants.*;

@Service
@Slf4j
@AllArgsConstructor
public class BranchProductService implements BranchProductServicePort {

    private final BranchProductRepositoryPort branchProductRepositoryPort;
    private final BranchRepositoryPort branchRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;

    private final ReportingQueryRepository reportingQueryRepository;

    @Override
    public Mono<BranchProductResponseDTO> getBranchProductById(Long id) {
        if (id == null || id <= 0) {
            return Mono.error(ApiException.badRequest("BRANCH_PRODUCT_ID_REQUIRED", "BranchProduct id is required"));
        }

        return branchProductRepositoryPort.getBranchProductById(id)
                .switchIfEmpty(Mono.error(ApiException.notFound(
                        BRANCH_PRODUCT_NOT_FOUND,
                        "BranchProduct not found: " + id
                )))
                .flatMap(this::enrichAndMapToResponse);
    }

    @Override
    public Mono<BranchProductResponseDTO> createBranchProduct(BranchProductRequestDTO request) {
        return Mono.justOrEmpty(request)
                .switchIfEmpty(Mono.error(ApiException.badRequest("BRANCH_PRODUCT_BODY_REQUIRED", "Request body is required")))
                .flatMap(req -> {
                    Long branchId = req.getBranchId();
                    Long productId = req.getProductId();
                    Integer stock = req.getStock();

                    if (branchId == null || branchId <= 0) {
                        return Mono.error(ApiException.badRequest(BRANCH_REQUIRED, BRANCH_ID_MSG_REQUIRED));
                    }
                    if (productId == null || productId <= 0) {
                        return Mono.error(ApiException.badRequest(PRODUCT_REQUIRED, PRODUCT_MSG_REQUIRED));
                    }
                    if (stock == null || stock < 0) {
                        return Mono.error(ApiException.badRequest(STOCK_REQUIRED, STOCK_MSG_REQUIRED));
                    }

                    Mono<BranchEntity> branchMono = branchRepositoryPort.getBranchById(branchId)
                            .switchIfEmpty(Mono.error(ApiException.notFound(
                                    BRANCH_NOT_FOUND,
                                    BRANCH_MSG_REQUIRED + branchId
                            )));

                    Mono<ProductEntity> productMono = productRepositoryPort.getProductById(productId)
                            .switchIfEmpty(Mono.error(ApiException.notFound(
                                    PRODUCT_NOT_FOUND,
                                    PRODUCT_NOT_FOUND_MSG + productId
                            )));

                    return Mono.zip(branchMono, productMono)
                            .flatMap(tuple ->
                                    branchProductRepositoryPort.existsByBranchIdAndProductId(branchId, productId)
                                            .flatMap(exists -> {
                                                if (exists) {
                                                    return Mono.error(ApiException.conflict(
                                                            "BRANCH_PRODUCT_ALREADY_EXISTS",
                                                            "Product already exists in branch. branchId=" + branchId + ", productId=" + productId
                                                    ));
                                                }

                                                BranchProductEntity entity = BranchProductMapper.toEntity(req);

                                                return branchProductRepositoryPort.saveBranchProduct(entity)
                                                        .map(saved -> BranchProductMapper.toResponseDto(saved, tuple.getT1(), tuple.getT2()))
                                                        .onErrorMap(DataIntegrityViolationException.class, ex ->
                                                                ApiException.conflict(
                                                                        "BRANCH_PRODUCT_ALREADY_EXISTS",
                                                                        "Product already exists in branch. branchId=" + branchId + ", productId=" + productId
                                                                )
                                                        );
                                            })
                            );
                });
    }

    @Override
    public Mono<BranchProductResponseDTO> updateBranchProduct(BranchProductRequestDTO request, Long id) {
        return Mono.justOrEmpty(request)
                .switchIfEmpty(Mono.error(ApiException.badRequest("BRANCH_PRODUCT_BODY_REQUIRED", "Request body is required")))
                .flatMap(req -> {
                    if (id == null || id <= 0) {
                        return Mono.error(ApiException.badRequest("BRANCH_PRODUCT_ID_REQUIRED", "BranchProduct id is required"));
                    }

                    Long branchId = req.getBranchId();
                    Long productId = req.getProductId();
                    Integer stock = req.getStock();

                    if (branchId == null || branchId <= 0) {
                        return Mono.error(ApiException.badRequest(BRANCH_REQUIRED, BRANCH_ID_MSG_REQUIRED));
                    }
                    if (productId == null || productId <= 0) {
                        return Mono.error(ApiException.badRequest(PRODUCT_REQUIRED, PRODUCT_MSG_REQUIRED));
                    }
                    if (stock == null || stock < 0) {
                        return Mono.error(ApiException.badRequest(STOCK_REQUIRED, STOCK_MSG_REQUIRED));
                    }

                    return branchProductRepositoryPort.getBranchProductById(id)
                            .switchIfEmpty(Mono.error(ApiException.notFound(
                                    BRANCH_PRODUCT_NOT_FOUND,
                                    "BranchProduct not found: " + id
                            )))
                            .flatMap(existing -> {
                                if (!existing.getBranchId().equals(branchId) || !existing.getProductId().equals(productId)) {
                                    return Mono.error(ApiException.badRequest(
                                            "BRANCH_PRODUCT_KEYS_IMMUTABLE",
                                            "branchId/productId cannot be changed for an existing BranchProduct"
                                    ));
                                }

                                existing.setStock(stock);

                                return branchProductRepositoryPort.updateBranchProduct(existing)
                                        .flatMap(this::enrichAndMapToResponse);
                            });
                });
    }

    @Override
    public Mono<Boolean> existsByBranchIdAndProductId(Long branchId, Long productId) {
        if (branchId == null || branchId <= 0) {
            return Mono.error(ApiException.badRequest(BRANCH_REQUIRED, BRANCH_ID_MSG_REQUIRED));
        }
        if (productId == null || productId <= 0) {
            return Mono.error(ApiException.badRequest(PRODUCT_REQUIRED, PRODUCT_MSG_REQUIRED));
        }
        return branchProductRepositoryPort.existsByBranchIdAndProductId(branchId, productId);
    }

    @Override
    public Mono<Void> deleteByBranchIdAndProductId(Long branchId, Long productId) {
        if (branchId == null || branchId <= 0) {
            return Mono.error(ApiException.badRequest(BRANCH_REQUIRED, BRANCH_ID_MSG_REQUIRED));
        }
        if (productId == null || productId <= 0) {
            return Mono.error(ApiException.badRequest(PRODUCT_REQUIRED, PRODUCT_MSG_REQUIRED));
        }

        return branchProductRepositoryPort.existsByBranchIdAndProductId(branchId, productId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(ApiException.notFound(
                                BRANCH_PRODUCT_NOT_FOUND,
                                "Branch-Product not found for branchId =" + branchId + " and product-Id=" + productId
                        ));
                    }
                    return branchProductRepositoryPort.deleteByBranchIdAndProductId(branchId, productId);
                });
    }

    @Override
    public Flux<BranchProductResponseDTO> getAllByBranchId(Long branchId) {
        if (branchId == null || branchId <= 0) {
            return Flux.error(ApiException.badRequest(BRANCH_REQUIRED, BRANCH_ID_MSG_REQUIRED));
        }

        Mono<BranchEntity> branchMono = branchRepositoryPort.getBranchById(branchId)
                .switchIfEmpty(Mono.error(ApiException.notFound(
                        BRANCH_NOT_FOUND,
                        BRANCH_MSG_REQUIRED + branchId
                )))
                .cache();

        return branchMono.flatMapMany(branch ->
                branchProductRepositoryPort.getAllByBranchId(branchId)
                        .flatMap(bp ->
                                productRepositoryPort.getProductById(bp.getProductId())
                                        .switchIfEmpty(Mono.error(ApiException.notFound(
                                                PRODUCT_NOT_FOUND,
                                                PRODUCT_NOT_FOUND_MSG + bp.getProductId()
                                        )))
                                        .map(product -> BranchProductMapper.toResponseDto(bp, branch, product))
                        )
        );
    }

    @Override
    public Mono<BranchProductResponseDTO> getByBranchIdAndProductId(Long branchId, Long productId) {
        if (branchId == null || branchId <= 0) {
            return Mono.error(ApiException.badRequest(BRANCH_REQUIRED, BRANCH_ID_MSG_REQUIRED));
        }
        if (productId == null || productId <= 0) {
            return Mono.error(ApiException.badRequest(PRODUCT_REQUIRED, PRODUCT_MSG_REQUIRED));
        }

        return branchProductRepositoryPort.getByBranchIdAndProductId(branchId, productId)
                .switchIfEmpty(Mono.error(ApiException.notFound(
                        BRANCH_PRODUCT_NOT_FOUND,
                        "BranchProduct not found for branchId=" + branchId + " and productId=" + productId
                )))
                .flatMap(this::enrichAndMapToResponse);
    }

    @Override
    public Mono<BranchProductResponseDTO> updateBranchProductStock(Long branchId, Long productId, Integer stock) {
        if (branchId == null || branchId <= 0) {
            return Mono.error(ApiException.badRequest(BRANCH_REQUIRED, BRANCH_ID_MSG_REQUIRED));
        }
        if (productId == null || productId <= 0) {
            return Mono.error(ApiException.badRequest(PRODUCT_REQUIRED, PRODUCT_MSG_REQUIRED));
        }
        if (stock == null || stock < 0) {
            return Mono.error(ApiException.badRequest(STOCK_REQUIRED, STOCK_MSG_REQUIRED));
        }

        return branchProductRepositoryPort.existsByBranchIdAndProductId(branchId, productId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(ApiException.notFound(
                                BRANCH_PRODUCT_NOT_FOUND,
                                "BranchProduct not found for branchId=" + branchId + " and productId=" + productId
                        ));
                    }
                    return branchProductRepositoryPort.updateBranchProductStock(branchId, productId, stock)
                            .flatMap(this::enrichAndMapToResponse);
                });
    }

    @Override
    public Flux<TopStockProductByBranchResponseDTO> getTopStockProductsByBranch(Long franchiseId) {
        if (franchiseId == null || franchiseId <= 0) {
            return Flux.error(ApiException.badRequest("FRANCHISE_ID_REQUIRED", "franchiseId is required"));
        }


        return reportingQueryRepository.findTopStockProductsByBranch(franchiseId)
                .map(row -> new TopStockProductByBranchResponseDTO(
                        new ResponseDTO(row.branchId(), row.branchName()),
                        new ResponseDTO(row.productId(), row.productName()),
                        row.stock()
                ));
    }

    private Mono<BranchProductResponseDTO> enrichAndMapToResponse(BranchProductEntity bp) {
        Mono<BranchEntity> branchMono = branchRepositoryPort.getBranchById(bp.getBranchId())
                .switchIfEmpty(Mono.error(ApiException.notFound(
                        BRANCH_NOT_FOUND,
                        BRANCH_MSG_REQUIRED + bp.getBranchId()
                )));

        Mono<ProductEntity> productMono = productRepositoryPort.getProductById(bp.getProductId())
                .switchIfEmpty(Mono.error(ApiException.notFound(
                        PRODUCT_NOT_FOUND,
                        PRODUCT_NOT_FOUND_MSG + bp.getProductId()
                )));

        return Mono.zip(branchMono, productMono)
                .map(tuple -> BranchProductMapper.toResponseDto(bp, tuple.getT1(), tuple.getT2()));
    }


}
