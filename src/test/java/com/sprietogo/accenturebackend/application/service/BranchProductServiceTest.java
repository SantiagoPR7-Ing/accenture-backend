package com.sprietogo.accenturebackend.application.service;

import com.sprietogo.accenturebackend.application.port.repository.BranchProductRepositoryPort;
import com.sprietogo.accenturebackend.application.port.repository.BranchRepositoryPort;
import com.sprietogo.accenturebackend.application.port.repository.ProductRepositoryPort;
import com.sprietogo.accenturebackend.domain.exception.ApiException;
import com.sprietogo.accenturebackend.domain.model.BranchEntity;
import com.sprietogo.accenturebackend.domain.model.BranchProductEntity;
import com.sprietogo.accenturebackend.domain.model.ProductEntity;
import com.sprietogo.accenturebackend.infrastructure.adapter.persistence.repository.query.ReportingQueryRepository;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.request.BranchProductRequestDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BranchProductServiceTest {

    @Mock private BranchProductRepositoryPort branchProductRepositoryPort;
    @Mock private BranchRepositoryPort branchRepositoryPort;
    @Mock private ProductRepositoryPort productRepositoryPort;
    @Mock private ReportingQueryRepository reportingQueryRepository;

    @InjectMocks
    private BranchProductService branchProductService;

    private BranchProductRequestDTO req(Long branchId, Long productId, Integer stock) {
        BranchProductRequestDTO dto = new BranchProductRequestDTO();
        dto.setBranchId(branchId);
        dto.setProductId(productId);
        dto.setStock(stock);
        return dto;
    }

    private BranchEntity branch(Long id, String name) {
        BranchEntity b = new BranchEntity();
        b.setId(id);
        b.setName(name);
        return b;
    }

    private ProductEntity product(Long id, String name) {
        ProductEntity p = new ProductEntity();
        p.setId(id);
        p.setName(name);
        return p;
    }

    private BranchProductEntity bp(Long id, Long branchId, Long productId, Integer stock) {
        BranchProductEntity e = new BranchProductEntity();
        e.setId(id);
        e.setBranchId(branchId);
        e.setProductId(productId);
        e.setStock(stock);
        return e;
    }

    // -------------------------
    // getBranchProductById
    // -------------------------

    @Test
    void getBranchProductById_whenIdInvalid_shouldReturn400() {
        StepVerifier.create(branchProductService.getBranchProductById(0L))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("BRANCH_PRODUCT_ID_REQUIRED", ex.getCode());
                })
                .verify();

        verifyNoInteractions(branchProductRepositoryPort, branchRepositoryPort, productRepositoryPort, reportingQueryRepository);
    }

    @Test
    void getBranchProductById_whenNotFound_shouldReturn404() {
        when(branchProductRepositoryPort.getBranchProductById(10L)).thenReturn(Mono.empty());

        StepVerifier.create(branchProductService.getBranchProductById(10L))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
                    assertEquals("BRANCH_PRODUCT_NOT_FOUND", ex.getCode());
                })
                .verify();

        verify(branchProductRepositoryPort).getBranchProductById(10L);
    }

    @Test
    void getBranchProductById_whenOk_shouldReturnEnrichedResponse() {
        BranchProductEntity bp = bp(1L, 100L, 200L, 50);

        when(branchProductRepositoryPort.getBranchProductById(1L)).thenReturn(Mono.just(bp));
        when(branchRepositoryPort.getBranchById(100L)).thenReturn(Mono.just(branch(100L, "B1")));
        when(productRepositoryPort.getProductById(200L)).thenReturn(Mono.just(product(200L, "P1")));

        StepVerifier.create(branchProductService.getBranchProductById(1L))
                .assertNext(dto -> {
                    assertEquals(1L, dto.getId());
                    assertEquals(50, dto.getStock());
                    assertNotNull(dto.getBranch());
                    assertEquals(100L, dto.getBranch().getId());
                    assertEquals("B1", dto.getBranch().getName());
                    assertNotNull(dto.getProduct());
                    assertEquals(200L, dto.getProduct().getId());
                    assertEquals("P1", dto.getProduct().getName());
                })
                .verifyComplete();
    }

    // -------------------------
    // createBranchProduct
    // -------------------------

    @Test
    void createBranchProduct_whenBodyNull_shouldReturn400() {
        StepVerifier.create(branchProductService.createBranchProduct(null))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("BRANCH_PRODUCT_BODY_REQUIRED", ex.getCode());
                })
                .verify();

        verifyNoInteractions(branchProductRepositoryPort, branchRepositoryPort, productRepositoryPort);
    }

    @Test
    void createBranchProduct_whenBranchIdInvalid_shouldReturn400() {
        StepVerifier.create(branchProductService.createBranchProduct(req(0L, 1L, 1)))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("BRANCH_ID_REQUIRED", ex.getCode());
                })
                .verify();
    }

    @Test
    void createBranchProduct_whenProductIdInvalid_shouldReturn400() {
        StepVerifier.create(branchProductService.createBranchProduct(req(1L, 0L, 1)))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("PRODUCT_ID_REQUIRED", ex.getCode());
                })
                .verify();
    }

    @Test
    void createBranchProduct_whenStockInvalid_shouldReturn400() {
        StepVerifier.create(branchProductService.createBranchProduct(req(1L, 2L, -1)))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("STOCK_INVALID", ex.getCode());
                })
                .verify();
    }

    @Test
    void createBranchProduct_whenProductNotFound_shouldReturn404() {
        when(branchRepositoryPort.getBranchById(1L)).thenReturn(Mono.just(branch(1L, "B1")));
        when(productRepositoryPort.getProductById(2L)).thenReturn(Mono.empty());

        StepVerifier.create(branchProductService.createBranchProduct(req(1L, 2L, 10)))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
                    assertEquals("PRODUCT_NOT_FOUND", ex.getCode());
                })
                .verify();

        verify(productRepositoryPort).getProductById(2L);
    }

    @Test
    void createBranchProduct_whenAlreadyExists_shouldReturn409() {
        when(branchRepositoryPort.getBranchById(1L)).thenReturn(Mono.just(branch(1L, "B1")));
        when(productRepositoryPort.getProductById(2L)).thenReturn(Mono.just(product(2L, "P1")));
        when(branchProductRepositoryPort.existsByBranchIdAndProductId(1L, 2L)).thenReturn(Mono.just(true));

        StepVerifier.create(branchProductService.createBranchProduct(req(1L, 2L, 10)))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.CONFLICT, ex.getStatus());
                    assertEquals("BRANCH_PRODUCT_ALREADY_EXISTS", ex.getCode());
                })
                .verify();

        verify(branchProductRepositoryPort, never()).saveBranchProduct(any());
    }

    @Test
    void createBranchProduct_whenOk_shouldSaveAndReturnResponse() {
        BranchEntity b = branch(1L, "B1");
        ProductEntity p = product(2L, "P1");

        when(branchRepositoryPort.getBranchById(1L)).thenReturn(Mono.just(b));
        when(productRepositoryPort.getProductById(2L)).thenReturn(Mono.just(p));
        when(branchProductRepositoryPort.existsByBranchIdAndProductId(1L, 2L)).thenReturn(Mono.just(false));

        BranchProductEntity saved = bp(10L, 1L, 2L, 99);
        when(branchProductRepositoryPort.saveBranchProduct(any())).thenReturn(Mono.just(saved));

        StepVerifier.create(branchProductService.createBranchProduct(req(1L, 2L, 99)))
                .assertNext(dto -> {
                    assertEquals(10L, dto.getId());
                    assertEquals(99, dto.getStock());
                    assertEquals(1L, dto.getBranch().getId());
                    assertEquals("B1", dto.getBranch().getName());
                    assertEquals(2L, dto.getProduct().getId());
                    assertEquals("P1", dto.getProduct().getName());
                })
                .verifyComplete();

        ArgumentCaptor<BranchProductEntity> captor = ArgumentCaptor.forClass(BranchProductEntity.class);
        verify(branchProductRepositoryPort).saveBranchProduct(captor.capture());
        assertNull(captor.getValue().getId());
        assertEquals(1L, captor.getValue().getBranchId());
        assertEquals(2L, captor.getValue().getProductId());
        assertEquals(99, captor.getValue().getStock());
    }

    @Test
    void createBranchProduct_whenDbUniqueViolation_shouldReturn409() {
        when(branchRepositoryPort.getBranchById(1L)).thenReturn(Mono.just(branch(1L, "B1")));
        when(productRepositoryPort.getProductById(2L)).thenReturn(Mono.just(product(2L, "P1")));
        when(branchProductRepositoryPort.existsByBranchIdAndProductId(1L, 2L)).thenReturn(Mono.just(false));

        when(branchProductRepositoryPort.saveBranchProduct(any()))
                .thenReturn(Mono.error(new DataIntegrityViolationException("dup")));

        StepVerifier.create(branchProductService.createBranchProduct(req(1L, 2L, 10)))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.CONFLICT, ex.getStatus());
                    assertEquals("BRANCH_PRODUCT_ALREADY_EXISTS", ex.getCode());
                })
                .verify();
    }

    // -------------------------
    // updateBranchProduct
    // -------------------------

    @Test
    void updateBranchProduct_whenBodyNull_shouldReturn400() {
        StepVerifier.create(branchProductService.updateBranchProduct(null, 10L))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("BRANCH_PRODUCT_BODY_REQUIRED", ex.getCode());
                })
                .verify();
    }

    @Test
    void updateBranchProduct_whenIdInvalid_shouldReturn400() {
        StepVerifier.create(branchProductService.updateBranchProduct(req(1L, 2L, 10), 0L))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("BRANCH_PRODUCT_ID_REQUIRED", ex.getCode());
                })
                .verify();
    }

    @Test
    void updateBranchProduct_whenNotFound_shouldReturn404() {
        when(branchProductRepositoryPort.getBranchProductById(10L)).thenReturn(Mono.empty());

        StepVerifier.create(branchProductService.updateBranchProduct(req(1L, 2L, 10), 10L))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
                    assertEquals("BRANCH_PRODUCT_NOT_FOUND", ex.getCode());
                })
                .verify();
    }

    @Test
    void updateBranchProduct_whenKeysChanged_shouldReturn400() {
        BranchProductEntity existing = bp(10L, 1L, 2L, 10);
        when(branchProductRepositoryPort.getBranchProductById(10L)).thenReturn(Mono.just(existing));

        StepVerifier.create(branchProductService.updateBranchProduct(req(9L, 2L, 10), 10L))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("BRANCH_PRODUCT_KEYS_IMMUTABLE", ex.getCode());
                })
                .verify();

        verify(branchProductRepositoryPort, never()).updateBranchProduct(any());
    }

    @Test
    void updateBranchProduct_whenOk_shouldUpdateAndReturnEnrichedResponse() {
        BranchProductEntity existing = bp(10L, 1L, 2L, 10);

        when(branchProductRepositoryPort.getBranchProductById(10L)).thenReturn(Mono.just(existing));
        when(branchProductRepositoryPort.updateBranchProduct(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        when(branchRepositoryPort.getBranchById(1L)).thenReturn(Mono.just(branch(1L, "B1")));
        when(productRepositoryPort.getProductById(2L)).thenReturn(Mono.just(product(2L, "P1")));

        StepVerifier.create(branchProductService.updateBranchProduct(req(1L, 2L, 99), 10L))
                .assertNext(dto -> {
                    assertEquals(10L, dto.getId());
                    assertEquals(99, dto.getStock());
                    assertEquals("B1", dto.getBranch().getName());
                    assertEquals("P1", dto.getProduct().getName());
                })
                .verifyComplete();

        ArgumentCaptor<BranchProductEntity> captor = ArgumentCaptor.forClass(BranchProductEntity.class);
        verify(branchProductRepositoryPort).updateBranchProduct(captor.capture());
        assertEquals(99, captor.getValue().getStock());
    }

    // -------------------------
    // existsByBranchIdAndProductId
    // -------------------------

    @Test
    void existsByBranchIdAndProductId_whenBranchInvalid_shouldReturn400() {
        StepVerifier.create(branchProductService.existsByBranchIdAndProductId(0L, 1L))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("BRANCH_ID_REQUIRED", ex.getCode());
                })
                .verify();
    }

    @Test
    void existsByBranchIdAndProductId_whenOk_shouldReturnBoolean() {
        when(branchProductRepositoryPort.existsByBranchIdAndProductId(1L, 2L)).thenReturn(Mono.just(true));

        StepVerifier.create(branchProductService.existsByBranchIdAndProductId(1L, 2L))
                .expectNext(true)
                .verifyComplete();
    }

    // -------------------------
    // deleteByBranchIdAndProductId
    // -------------------------

    @Test
    void deleteByBranchIdAndProductId_whenNotFound_shouldReturn404() {
        when(branchProductRepositoryPort.existsByBranchIdAndProductId(1L, 2L)).thenReturn(Mono.just(false));

        StepVerifier.create(branchProductService.deleteByBranchIdAndProductId(1L, 2L))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
                    assertEquals("BRANCH_PRODUCT_NOT_FOUND", ex.getCode());
                })
                .verify();

        verify(branchProductRepositoryPort, never()).deleteByBranchIdAndProductId(anyLong(), anyLong());
    }

    @Test
    void deleteByBranchIdAndProductId_whenOk_shouldDelete() {
        when(branchProductRepositoryPort.existsByBranchIdAndProductId(1L, 2L)).thenReturn(Mono.just(true));
        when(branchProductRepositoryPort.deleteByBranchIdAndProductId(1L, 2L)).thenReturn(Mono.empty());

        StepVerifier.create(branchProductService.deleteByBranchIdAndProductId(1L, 2L))
                .verifyComplete();

        verify(branchProductRepositoryPort).deleteByBranchIdAndProductId(1L, 2L);
    }

    // -------------------------
    // getAllByBranchId
    // -------------------------

    @Test
    void getAllByBranchId_whenBranchInvalid_shouldReturn400() {
        StepVerifier.create(branchProductService.getAllByBranchId(0L))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("BRANCH_ID_REQUIRED", ex.getCode());
                })
                .verify();
    }

    @Test
    void getAllByBranchId_whenBranchNotFound_shouldReturn404() {
        when(branchRepositoryPort.getBranchById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(branchProductService.getAllByBranchId(1L))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
                    assertEquals("BRANCH_NOT_FOUND", ex.getCode());
                })
                .verify();
    }

    @Test
    void getAllByBranchId_whenOk_shouldReturnList() {
        BranchEntity b = branch(1L, "B1");

        when(branchRepositoryPort.getBranchById(1L)).thenReturn(Mono.just(b));

        BranchProductEntity bp1 = bp(10L, 1L, 2L, 5);
        BranchProductEntity bp2 = bp(11L, 1L, 3L, 7);

        when(branchProductRepositoryPort.getAllByBranchId(1L)).thenReturn(Flux.just(bp1, bp2));

        when(productRepositoryPort.getProductById(2L)).thenReturn(Mono.just(product(2L, "P2")));
        when(productRepositoryPort.getProductById(3L)).thenReturn(Mono.just(product(3L, "P3")));

        StepVerifier.create(branchProductService.getAllByBranchId(1L))
                .assertNext(dto -> {
                    assertEquals(10L, dto.getId());
                    assertEquals("B1", dto.getBranch().getName());
                    assertEquals("P2", dto.getProduct().getName());
                })
                .assertNext(dto -> {
                    assertEquals(11L, dto.getId());
                    assertEquals("P3", dto.getProduct().getName());
                })
                .verifyComplete();
    }

    // -------------------------
    // getByBranchIdAndProductId
    // -------------------------

    @Test
    void getByBranchIdAndProductId_whenNotFound_shouldReturn404() {
        when(branchProductRepositoryPort.getByBranchIdAndProductId(1L, 2L)).thenReturn(Mono.empty());

        StepVerifier.create(branchProductService.getByBranchIdAndProductId(1L, 2L))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
                    assertEquals("BRANCH_PRODUCT_NOT_FOUND", ex.getCode());
                })
                .verify();
    }

    @Test
    void getByBranchIdAndProductId_whenOk_shouldReturnEnriched() {
        BranchProductEntity bp = bp(10L, 1L, 2L, 50);

        when(branchProductRepositoryPort.getByBranchIdAndProductId(1L, 2L)).thenReturn(Mono.just(bp));
        when(branchRepositoryPort.getBranchById(1L)).thenReturn(Mono.just(branch(1L, "B1")));
        when(productRepositoryPort.getProductById(2L)).thenReturn(Mono.just(product(2L, "P1")));

        StepVerifier.create(branchProductService.getByBranchIdAndProductId(1L, 2L))
                .assertNext(dto -> {
                    assertEquals(10L, dto.getId());
                    assertEquals(50, dto.getStock());
                    assertEquals("B1", dto.getBranch().getName());
                    assertEquals("P1", dto.getProduct().getName());
                })
                .verifyComplete();
    }

    // -------------------------
    // updateBranchProductStock
    // -------------------------

    @Test
    void updateBranchProductStock_whenNotExists_shouldReturn404() {
        when(branchProductRepositoryPort.existsByBranchIdAndProductId(1L, 2L)).thenReturn(Mono.just(false));

        StepVerifier.create(branchProductService.updateBranchProductStock(1L, 2L, 99))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
                    assertEquals("BRANCH_PRODUCT_NOT_FOUND", ex.getCode());
                })
                .verify();

        verify(branchProductRepositoryPort, never()).updateBranchProductStock(anyLong(), anyLong(), anyInt());
    }

    @Test
    void updateBranchProductStock_whenOk_shouldUpdateAndReturnEnriched() {
        when(branchProductRepositoryPort.existsByBranchIdAndProductId(1L, 2L)).thenReturn(Mono.just(true));

        BranchProductEntity updated = bp(10L, 1L, 2L, 99);
        when(branchProductRepositoryPort.updateBranchProductStock(1L, 2L, 99)).thenReturn(Mono.just(updated));

        when(branchRepositoryPort.getBranchById(1L)).thenReturn(Mono.just(branch(1L, "B1")));
        when(productRepositoryPort.getProductById(2L)).thenReturn(Mono.just(product(2L, "P1")));

        StepVerifier.create(branchProductService.updateBranchProductStock(1L, 2L, 99))
                .assertNext(dto -> {
                    assertEquals(10L, dto.getId());
                    assertEquals(99, dto.getStock());
                    assertEquals("B1", dto.getBranch().getName());
                    assertEquals("P1", dto.getProduct().getName());
                })
                .verifyComplete();
    }

    // -------------------------
    // getTopStockProductsByBranch
    // -------------------------

    @Test
    void getTopStockProductsByBranch_whenFranchiseIdInvalid_shouldReturn400() {
        StepVerifier.create(branchProductService.getTopStockProductsByBranch(0L))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("FRANCHISE_ID_REQUIRED", ex.getCode());
                })
                .verify();

        verifyNoInteractions(reportingQueryRepository);
    }

    @Test
    void getTopStockProductsByBranch_whenOk_shouldMapRowsToDto() {
        ReportingQueryRepository.TopStockProductByBranchRow r1 =
                new ReportingQueryRepository.TopStockProductByBranchRow(1L, "B1", 10L, "P1", 100);
        ReportingQueryRepository.TopStockProductByBranchRow r2 =
                new ReportingQueryRepository.TopStockProductByBranchRow(2L, "B2", 11L, "P2", 50);

        when(reportingQueryRepository.findTopStockProductsByBranch(99L))
                .thenReturn(Flux.just(r1, r2));

        StepVerifier.create(branchProductService.getTopStockProductsByBranch(99L))
                .assertNext(dto -> {
                    assertNotNull(dto.branch());
                    assertEquals(1L, dto.branch().getId());
                    assertEquals("B1", dto.branch().getName());
                    assertNotNull(dto.branch());
                    assertEquals(10L, dto.product().getId());
                    assertEquals("P1", dto.product().getName());
                    assertEquals(100, dto.stock());
                })
                .assertNext(dto -> {
                    assertEquals(2L, dto.branch().getId());
                    assertEquals(11L, dto.product().getId());
                    assertEquals(50, dto.stock());
                })
                .verifyComplete();

        verify(reportingQueryRepository).findTopStockProductsByBranch(99L);
    }
}
