package com.sprietogo.accenturebackend.application.service;

import com.sprietogo.accenturebackend.application.port.repository.FranchiseRepositoryPort;
import com.sprietogo.accenturebackend.domain.exception.ApiException;
import com.sprietogo.accenturebackend.domain.model.FranchiseEntity;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.request.RequestDTO;
import org.junit.jupiter.api.BeforeEach;
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
class FranchiseServiceTest {

    @Mock
    private FranchiseRepositoryPort franchiseRepositoryPort;

    @InjectMocks
    private FranchiseService franchiseService;

    private RequestDTO req(String name) {
        RequestDTO dto = new RequestDTO();
        dto.setName(name);
        return dto;
    }

    @BeforeEach
    void setUp() {
        // franchiseService
    }


    @Test
    void createFranchise_whenBodyNull_shouldReturn400() {
        StepVerifier.create(franchiseService.createFranchise(null))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("FRANCHISE_BODY_REQUIRED", ex.getCode());
                })
                .verify();

        verifyNoInteractions(franchiseRepositoryPort);
    }

    @Test
    void createFranchise_whenNameBlank_shouldReturn400() {
        StepVerifier.create(franchiseService.createFranchise(req("   ")))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("FRANCHISE_NAME_REQUIRED", ex.getCode());
                })
                .verify();

        verifyNoInteractions(franchiseRepositoryPort);
    }

    @Test
    void createFranchise_whenAlreadyExists_shouldReturn409() {
        when(franchiseRepositoryPort.existsFranchiseByName("Acme"))
                .thenReturn(Mono.just(true));

        StepVerifier.create(franchiseService.createFranchise(req("Acme")))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.CONFLICT, ex.getStatus());
                    assertEquals("FRANCHISE_ALREADY_EXISTS", ex.getCode());
                })
                .verify();

        verify(franchiseRepositoryPort).existsFranchiseByName("Acme");
        verify(franchiseRepositoryPort, never()).saveFranchise(any());
    }

    @Test
    void createFranchise_whenOk_shouldSaveAndReturnResponse() {
        when(franchiseRepositoryPort.existsFranchiseByName("Acme"))
                .thenReturn(Mono.just(false));

        FranchiseEntity saved = new FranchiseEntity(1L, "Acme", null, null);
        when(franchiseRepositoryPort.saveFranchise(any()))
                .thenReturn(Mono.just(saved));

        StepVerifier.create(franchiseService.createFranchise(req("  Acme  ")))
                .assertNext(resp -> {
                    assertEquals(1L, resp.getId());
                    assertEquals("Acme", resp.getName());
                })
                .verifyComplete();

        // captura del entity para validar trim
        ArgumentCaptor<FranchiseEntity> captor = ArgumentCaptor.forClass(FranchiseEntity.class);
        verify(franchiseRepositoryPort).saveFranchise(captor.capture());
        assertNull(captor.getValue().getId());
        assertEquals("Acme", captor.getValue().getName());
    }

    @Test
    void createFranchise_whenDbUniqueViolation_shouldReturn409() {
        when(franchiseRepositoryPort.existsFranchiseByName("Acme"))
                .thenReturn(Mono.just(false));

        when(franchiseRepositoryPort.saveFranchise(any()))
                .thenReturn(Mono.error(new DataIntegrityViolationException("dup")));

        StepVerifier.create(franchiseService.createFranchise(req("Acme")))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.CONFLICT, ex.getStatus());
                    assertEquals("FRANCHISE_ALREADY_EXISTS", ex.getCode());
                })
                .verify();
    }

    // -------------------------
    // updateFranchise
    // -------------------------

    @Test
    void updateFranchise_whenIdInvalid_shouldReturn400() {
        StepVerifier.create(franchiseService.updateFranchise("New", 0L))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("FRANCHISE_ID_REQUIRED", ex.getCode());
                })
                .verify();

        verifyNoInteractions(franchiseRepositoryPort);
    }

    @Test
    void updateFranchise_whenNameNull_shouldReturn400() {
        StepVerifier.create(franchiseService.updateFranchise(null, 10L))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("FRANCHISE_NAME_REQUIRED", ex.getCode());
                })
                .verify();

        verifyNoInteractions(franchiseRepositoryPort);
    }

    @Test
    void updateFranchise_whenNotFound_shouldReturn404() {
        when(franchiseRepositoryPort.getFranchiseById(10L)).thenReturn(Mono.empty());

        StepVerifier.create(franchiseService.updateFranchise("New", 10L))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
                    assertEquals("FRANCHISE_NOT_FOUND", ex.getCode());
                })
                .verify();

        verify(franchiseRepositoryPort).getFranchiseById(10L);
        verify(franchiseRepositoryPort, never()).saveFranchise(any());
    }

    @Test
    void updateFranchise_whenSameName_shouldReturnExistingWithoutSaving() {
        FranchiseEntity existing = new FranchiseEntity(10L, "Acme", null, null);
        when(franchiseRepositoryPort.getFranchiseById(10L)).thenReturn(Mono.just(existing));

        StepVerifier.create(franchiseService.updateFranchise(" acme ", 10L))
                .assertNext(resp -> {
                    assertEquals(10L, resp.getId());
                    assertEquals("Acme", resp.getName());
                })
                .verifyComplete();

        verify(franchiseRepositoryPort).getFranchiseById(10L);
        verify(franchiseRepositoryPort, never()).existsFranchiseByName(any());
        verify(franchiseRepositoryPort, never()).saveFranchise(any());
    }

    @Test
    void updateFranchise_whenNewNameAlreadyExists_shouldReturn409() {
        FranchiseEntity existing = new FranchiseEntity(10L, "Acme", null, null);
        when(franchiseRepositoryPort.getFranchiseById(10L)).thenReturn(Mono.just(existing));
        when(franchiseRepositoryPort.existsFranchiseByName("New")).thenReturn(Mono.just(true));

        StepVerifier.create(franchiseService.updateFranchise("New", 10L))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.CONFLICT, ex.getStatus());
                    assertEquals("FRANCHISE_ALREADY_EXISTS", ex.getCode());
                })
                .verify();

        verify(franchiseRepositoryPort).getFranchiseById(10L);
        verify(franchiseRepositoryPort).existsFranchiseByName("New");
        verify(franchiseRepositoryPort, never()).saveFranchise(any());
    }

    @Test
    void updateFranchise_whenOk_shouldSaveAndReturnResponse() {
        OffsetDateTime created = OffsetDateTime.now().minusDays(1);
        OffsetDateTime updated = OffsetDateTime.now().minusHours(1);

        FranchiseEntity existing = new FranchiseEntity(10L, "Acme", created, updated);

        when(franchiseRepositoryPort.getFranchiseById(10L)).thenReturn(Mono.just(existing));
        when(franchiseRepositoryPort.existsFranchiseByName("New")).thenReturn(Mono.just(false));

        FranchiseEntity saved = new FranchiseEntity(10L, "New", created, updated);
        when(franchiseRepositoryPort.saveFranchise(any())).thenReturn(Mono.just(saved));

        StepVerifier.create(franchiseService.updateFranchise("  New  ", 10L))
                .assertNext(resp -> {
                    assertEquals(10L, resp.getId());
                    assertEquals("New", resp.getName());
                })
                .verifyComplete();

        ArgumentCaptor<FranchiseEntity> captor = ArgumentCaptor.forClass(FranchiseEntity.class);
        verify(franchiseRepositoryPort).saveFranchise(captor.capture());

        FranchiseEntity toSave = captor.getValue();
        assertEquals(10L, toSave.getId());
        assertEquals("New", toSave.getName());
        assertEquals(created, toSave.getCreatedAt());
        assertEquals(updated, toSave.getUpdatedAt());
    }

    @Test
    void updateFranchise_whenDbUniqueViolation_shouldReturn409() {
        FranchiseEntity existing = new FranchiseEntity(10L, "Acme", null, null);

        when(franchiseRepositoryPort.getFranchiseById(10L)).thenReturn(Mono.just(existing));
        when(franchiseRepositoryPort.existsFranchiseByName("New")).thenReturn(Mono.just(false));
        when(franchiseRepositoryPort.saveFranchise(any()))
                .thenReturn(Mono.error(new DataIntegrityViolationException("dup")));

        StepVerifier.create(franchiseService.updateFranchise("New", 10L))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.CONFLICT, ex.getStatus());
                    assertEquals("FRANCHISE_ALREADY_EXISTS", ex.getCode());
                })
                .verify();
    }

    // -------------------------
    // getFranchise
    // -------------------------

    @Test
    void getFranchise_whenNameNull_shouldReturn400() {
        StepVerifier.create(franchiseService.getFranchise(null))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("FRANCHISE_NAME_REQUIRED", ex.getCode());
                })
                .verify();

        verifyNoInteractions(franchiseRepositoryPort);
    }

    @Test
    void getFranchise_whenBlank_shouldReturn400() {
        StepVerifier.create(franchiseService.getFranchise("   "))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("FRANCHISE_NAME_REQUIRED", ex.getCode());
                })
                .verify();

        verifyNoInteractions(franchiseRepositoryPort);
    }

    @Test
    void getFranchise_whenNotFound_shouldReturn404() {
        when(franchiseRepositoryPort.getFranchiseByName("Acme")).thenReturn(Mono.empty());

        StepVerifier.create(franchiseService.getFranchise("Acme"))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
                    assertEquals("FRANCHISE_NOT_FOUND", ex.getCode());
                })
                .verify();

        verify(franchiseRepositoryPort).getFranchiseByName("Acme");
    }

    @Test
    void getFranchise_whenOk_shouldReturnResponse() {
        FranchiseEntity entity = new FranchiseEntity(1L, "Acme", null, null);
        when(franchiseRepositoryPort.getFranchiseByName("Acme")).thenReturn(Mono.just(entity));

        StepVerifier.create(franchiseService.getFranchise("  Acme  "))
                .assertNext(resp -> {
                    assertEquals(1L, resp.getId());
                    assertEquals("Acme", resp.getName());
                })
                .verifyComplete();

        verify(franchiseRepositoryPort).getFranchiseByName("Acme");
    }
}
