package com.sprietogo.accenturebackend.application.service;

import com.sprietogo.accenturebackend.application.port.repository.ProductRepositoryPort;
import com.sprietogo.accenturebackend.domain.exception.ApiException;
import com.sprietogo.accenturebackend.domain.model.ProductEntity;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.request.RequestDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @InjectMocks
    private ProductService productService;

    private RequestDTO req(String name) {
        RequestDTO dto = new RequestDTO();
        dto.setName(name);
        return dto;
    }

    // -------------------------
    // saveProduct
    // -------------------------

    @Test
    void saveProduct_whenBodyNull_shouldReturn400() {
        StepVerifier.create(productService.saveProduct(null))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("PRODUCT_BODY_REQUIRED", ex.getCode());
                })
                .verify();

        verifyNoInteractions(productRepositoryPort);
    }

    @Test
    void saveProduct_whenNameBlank_shouldReturn400() {
        StepVerifier.create(productService.saveProduct(req("   ")))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("PRODUCT_NAME_REQUIRED", ex.getCode());
                })
                .verify();

        verifyNoInteractions(productRepositoryPort);
    }

    @Test
    void saveProduct_whenAlreadyExists_shouldReturn409() {
        when(productRepositoryPort.existsProductByName("CocaCola"))
                .thenReturn(Mono.just(true));

        StepVerifier.create(productService.saveProduct(req("CocaCola")))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.CONFLICT, ex.getStatus());
                    assertEquals("PRODUCT_ALREADY_EXISTS", ex.getCode());
                })
                .verify();

        verify(productRepositoryPort).existsProductByName("CocaCola");
        verify(productRepositoryPort, never()).saveProduct(any());
    }

    @Test
    void saveProduct_whenOk_shouldSaveAndReturnResponse() {
        when(productRepositoryPort.existsProductByName("CocaCola"))
                .thenReturn(Mono.just(false));

        ProductEntity saved = new ProductEntity(1L, "CocaCola", null, null);
        when(productRepositoryPort.saveProduct(any()))
                .thenReturn(Mono.just(saved));

        StepVerifier.create(productService.saveProduct(req("  CocaCola  ")))
                .assertNext(resp -> {
                    assertEquals(1L, resp.getId());
                    assertEquals("CocaCola", resp.getName());
                })
                .verifyComplete();

        ArgumentCaptor<ProductEntity> captor = ArgumentCaptor.forClass(ProductEntity.class);
        verify(productRepositoryPort).saveProduct(captor.capture());
        assertNull(captor.getValue().getId());
        assertEquals("CocaCola", captor.getValue().getName());
    }

    @Test
    void saveProduct_whenDbUniqueViolation_shouldReturn409() {
        when(productRepositoryPort.existsProductByName("CocaCola"))
                .thenReturn(Mono.just(false));

        when(productRepositoryPort.saveProduct(any()))
                .thenReturn(Mono.error(new DataIntegrityViolationException("dup")));

        StepVerifier.create(productService.saveProduct(req("CocaCola")))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.CONFLICT, ex.getStatus());
                    assertEquals("PRODUCT_ALREADY_EXISTS", ex.getCode());
                })
                .verify();
    }

    // -------------------------
    // updateProduct
    // -------------------------

    @Test
    void updateProduct_whenIdInvalid_shouldReturn400() {
        StepVerifier.create(productService.updateProduct("New", 0L))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("PRODUCT_ID_REQUIRED", ex.getCode());
                })
                .verify();

        verifyNoInteractions(productRepositoryPort);
    }

    @Test
    void updateProduct_whenNameNull_shouldReturn400() {
        StepVerifier.create(productService.updateProduct(null, 10L))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("PRODUCT_NAME_REQUIRED", ex.getCode());
                })
                .verify();

        verifyNoInteractions(productRepositoryPort);
    }

    @Test
    void updateProduct_whenNotFound_shouldReturn404() {
        when(productRepositoryPort.getProductById(10L)).thenReturn(Mono.empty());

        StepVerifier.create(productService.updateProduct("New", 10L))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
                    assertEquals("PRODUCT_NOT_FOUND", ex.getCode());
                })
                .verify();

        verify(productRepositoryPort).getProductById(10L);
        verify(productRepositoryPort, never()).saveProduct(any());
    }

    @Test
    void updateProduct_whenSameName_shouldReturnExistingWithoutSaving() {
        ProductEntity existing = new ProductEntity(10L, "CocaCola", null, null);
        when(productRepositoryPort.getProductById(10L)).thenReturn(Mono.just(existing));

        StepVerifier.create(productService.updateProduct("  cocacola  ", 10L))
                .assertNext(resp -> {
                    assertEquals(10L, resp.getId());
                    assertEquals("CocaCola", resp.getName());
                })
                .verifyComplete();

        verify(productRepositoryPort).getProductById(10L);
        verify(productRepositoryPort, never()).existsProductByName(any());
        verify(productRepositoryPort, never()).saveProduct(any());
    }

    @Test
    void updateProduct_whenNewNameAlreadyExists_shouldReturn409() {
        ProductEntity existing = new ProductEntity(10L, "CocaCola", null, null);
        when(productRepositoryPort.getProductById(10L)).thenReturn(Mono.just(existing));
        when(productRepositoryPort.existsProductByName("New")).thenReturn(Mono.just(true));

        StepVerifier.create(productService.updateProduct("New", 10L))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.CONFLICT, ex.getStatus());
                    assertEquals("PRODUCT_ALREADY_EXISTS", ex.getCode());
                })
                .verify();

        verify(productRepositoryPort).getProductById(10L);
        verify(productRepositoryPort).existsProductByName("New");
        verify(productRepositoryPort, never()).saveProduct(any());
    }

    @Test
    void updateProduct_whenOk_shouldSaveAndReturnResponse_andKeepTimestamps() {
        OffsetDateTime created = OffsetDateTime.now().minusDays(1);
        OffsetDateTime updated = OffsetDateTime.now().minusHours(1);

        ProductEntity existing = new ProductEntity(10L, "CocaCola", created, updated);

        when(productRepositoryPort.getProductById(10L)).thenReturn(Mono.just(existing));
        when(productRepositoryPort.existsProductByName("New")).thenReturn(Mono.just(false));

        ProductEntity saved = new ProductEntity(10L, "New", created, updated);
        when(productRepositoryPort.saveProduct(any())).thenReturn(Mono.just(saved));

        StepVerifier.create(productService.updateProduct("  New  ", 10L))
                .assertNext(resp -> {
                    assertEquals(10L, resp.getId());
                    assertEquals("New", resp.getName());
                })
                .verifyComplete();

        ArgumentCaptor<ProductEntity> captor = ArgumentCaptor.forClass(ProductEntity.class);
        verify(productRepositoryPort).saveProduct(captor.capture());

        ProductEntity toSave = captor.getValue();
        assertEquals(10L, toSave.getId());
        assertEquals("New", toSave.getName());
        assertEquals(created, toSave.getCreatedAt());
        assertEquals(updated, toSave.getUpdatedAt());
    }

    @Test
    void updateProduct_whenDbUniqueViolation_shouldReturn409() {
        ProductEntity existing = new ProductEntity(10L, "CocaCola", null, null);

        when(productRepositoryPort.getProductById(10L)).thenReturn(Mono.just(existing));
        when(productRepositoryPort.existsProductByName("New")).thenReturn(Mono.just(false));
        when(productRepositoryPort.saveProduct(any()))
                .thenReturn(Mono.error(new DataIntegrityViolationException("dup")));

        StepVerifier.create(productService.updateProduct("New", 10L))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.CONFLICT, ex.getStatus());
                    assertEquals("PRODUCT_ALREADY_EXISTS", ex.getCode());
                })
                .verify();
    }

    // -------------------------
    // getProductByName
    // -------------------------

    @Test
    void getProductByName_whenNameNull_shouldReturn400() {
        StepVerifier.create(productService.getProductByName(null))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("PRODUCT_NAME_REQUIRED", ex.getCode());
                })
                .verify();

        verifyNoInteractions(productRepositoryPort);
    }

    @Test
    void getProductByName_whenBlank_shouldReturn400() {
        StepVerifier.create(productService.getProductByName("   "))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("PRODUCT_NAME_REQUIRED", ex.getCode());
                })
                .verify();

        verifyNoInteractions(productRepositoryPort);
    }

    @Test
    void getProductByName_whenNotFound_shouldReturn404() {
        when(productRepositoryPort.getProductByName("CocaCola")).thenReturn(Mono.empty());

        StepVerifier.create(productService.getProductByName("CocaCola"))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
                    assertEquals("PRODUCT_NOT_FOUND", ex.getCode());
                })
                .verify();

        verify(productRepositoryPort).getProductByName("CocaCola");
    }

    @Test
    void getProductByName_whenOk_shouldReturnResponse() {
        ProductEntity entity = new ProductEntity(1L, "CocaCola", null, null);
        when(productRepositoryPort.getProductByName("CocaCola")).thenReturn(Mono.just(entity));

        StepVerifier.create(productService.getProductByName("  CocaCola  "))
                .assertNext(resp -> {
                    assertEquals(1L, resp.getId());
                    assertEquals("CocaCola", resp.getName());
                })
                .verifyComplete();

        verify(productRepositoryPort).getProductByName("CocaCola");
    }
}
