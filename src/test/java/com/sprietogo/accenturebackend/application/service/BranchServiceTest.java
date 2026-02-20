package com.sprietogo.accenturebackend.application.service;

import com.sprietogo.accenturebackend.application.port.repository.BranchRepositoryPort;
import com.sprietogo.accenturebackend.application.port.repository.FranchiseRepositoryPort;
import com.sprietogo.accenturebackend.domain.exception.ApiException;
import com.sprietogo.accenturebackend.domain.model.BranchEntity;
import com.sprietogo.accenturebackend.domain.model.FranchiseEntity;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.request.BranchRequestDTO;
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

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BranchServiceTest {

    @Mock
    private BranchRepositoryPort branchRepositoryPort;

    @Mock
    private FranchiseRepositoryPort franchiseRepositoryPort;

    @InjectMocks
    private BranchService branchService;

    private BranchRequestDTO req(Long franchiseId, String name) {
        BranchRequestDTO dto = new BranchRequestDTO();
        dto.setFranchiseId(franchiseId);
        dto.setName(name);
        return dto;
    }

    private FranchiseEntity franchise(Long id, String name) {
        return new FranchiseEntity(id, name, null, null);
    }

    // -------------------------
    // createBranch
    // -------------------------

    @Test
    void createBranch_whenBodyNull_shouldReturn400() {
        StepVerifier.create(branchService.createBranch(null))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("BRANCH_BODY_REQUIRED", ex.getCode());
                })
                .verify();

        verifyNoInteractions(branchRepositoryPort, franchiseRepositoryPort);
    }

    @Test
    void createBranch_whenFranchiseIdInvalid_shouldReturn400() {
        StepVerifier.create(branchService.createBranch(req(0L, "Main")))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("BRANCH_FRANCHISE_ID_REQUIRED", ex.getCode());
                })
                .verify();

        verifyNoInteractions(branchRepositoryPort, franchiseRepositoryPort);
    }

    @Test
    void createBranch_whenNameBlank_shouldReturn400() {
        StepVerifier.create(branchService.createBranch(req(1L, "   ")))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("BRANCH_NAME_REQUIRED", ex.getCode());
                })
                .verify();

        verifyNoInteractions(branchRepositoryPort, franchiseRepositoryPort);
    }

    @Test
    void createBranch_whenFranchiseNotFound_shouldReturn404() {
        when(franchiseRepositoryPort.getFranchiseById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(branchService.createBranch(req(1L, "Main")))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
                    assertEquals("FRANCHISE_NOT_FOUND", ex.getCode());
                })
                .verify();

        verify(franchiseRepositoryPort).getFranchiseById(1L);
        verify(branchRepositoryPort, never()).existsBranchByFranchiseIdAndName(anyLong(), anyString());
        verify(branchRepositoryPort, never()).saveBranch(any());
    }

    @Test
    void createBranch_whenAlreadyExists_shouldReturn409() {
        when(franchiseRepositoryPort.getFranchiseById(1L)).thenReturn(Mono.just(franchise(1L, "Fr1")));
        when(branchRepositoryPort.existsBranchByFranchiseIdAndName(1L, "Main")).thenReturn(Mono.just(true));

        StepVerifier.create(branchService.createBranch(req(1L, "Main")))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.CONFLICT, ex.getStatus());
                    assertEquals("BRANCH_ALREADY_EXISTS", ex.getCode());
                })
                .verify();

        verify(branchRepositoryPort).existsBranchByFranchiseIdAndName(1L, "Main");
        verify(branchRepositoryPort, never()).saveBranch(any());
    }

    @Test
    void createBranch_whenOk_shouldSaveAndReturnResponse() {
        when(franchiseRepositoryPort.getFranchiseById(1L)).thenReturn(Mono.just(franchise(1L, "Fr1")));
        when(branchRepositoryPort.existsBranchByFranchiseIdAndName(1L, "Main")).thenReturn(Mono.just(false));

        BranchEntity saved = new BranchEntity(10L, 1L, "Main", null, null);
        when(branchRepositoryPort.saveBranch(any())).thenReturn(Mono.just(saved));

        StepVerifier.create(branchService.createBranch(req(1L, "  Main  ")))
                .assertNext(resp -> {
                    assertEquals(10L, resp.getId());
                    assertEquals("Main", resp.getName());
                    assertNotNull(resp.getFranchise());
                    assertEquals(1L, resp.getFranchise().getId());
                    assertEquals("Fr1", resp.getFranchise().getName());
                })
                .verifyComplete();

        ArgumentCaptor<BranchEntity> captor = ArgumentCaptor.forClass(BranchEntity.class);
        verify(branchRepositoryPort).saveBranch(captor.capture());
        assertNull(captor.getValue().getId());
        assertEquals(1L, captor.getValue().getFranchiseId());
        assertEquals("Main", captor.getValue().getName()); // trim OK
    }

    @Test
    void createBranch_whenDbUniqueViolation_shouldReturn409() {
        when(franchiseRepositoryPort.getFranchiseById(1L)).thenReturn(Mono.just(franchise(1L, "Fr1")));
        when(branchRepositoryPort.existsBranchByFranchiseIdAndName(1L, "Main")).thenReturn(Mono.just(false));
        when(branchRepositoryPort.saveBranch(any()))
                .thenReturn(Mono.error(new DataIntegrityViolationException("dup")));

        StepVerifier.create(branchService.createBranch(req(1L, "Main")))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.CONFLICT, ex.getStatus());
                    assertEquals("BRANCH_ALREADY_EXISTS", ex.getCode());
                })
                .verify();
    }

    // -------------------------
    // updateBranch
    // -------------------------

    @Test
    void updateBranch_whenIdInvalid_shouldReturn400() {
        StepVerifier.create(branchService.updateBranch("New", 0L))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("BRANCH_ID_REQUIRED", ex.getCode());
                })
                .verify();

        verifyNoInteractions(branchRepositoryPort, franchiseRepositoryPort);
    }

    @Test
    void updateBranch_whenNameNull_shouldReturn400() {
        StepVerifier.create(branchService.updateBranch(null, 10L))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("BRANCH_NAME_REQUIRED", ex.getCode());
                })
                .verify();

        verifyNoInteractions(branchRepositoryPort, franchiseRepositoryPort);
    }

    @Test
    void updateBranch_whenBranchNotFound_shouldReturn404() {
        when(branchRepositoryPort.getBranchById(10L)).thenReturn(Mono.empty());

        StepVerifier.create(branchService.updateBranch("New", 10L))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
                    assertEquals("BRANCH_NOT_FOUND", ex.getCode());
                })
                .verify();

        verify(branchRepositoryPort).getBranchById(10L);
        verifyNoInteractions(franchiseRepositoryPort);
        verify(branchRepositoryPort, never()).saveBranch(any());
    }


    @Test
    void updateBranch_whenSameName_shouldReturnResponseWithoutSaving() {
        FranchiseEntity fr = franchise(1L, "Fr1");
        BranchEntity existing = new BranchEntity(10L, 1L, "Main", null, null);

        when(branchRepositoryPort.getBranchById(10L)).thenReturn(Mono.just(existing));
        when(franchiseRepositoryPort.getFranchiseById(1L)).thenReturn(Mono.just(fr));

        StepVerifier.create(branchService.updateBranch("  main  ", 10L))
                .assertNext(resp -> {
                    assertEquals(10L, resp.getId());
                    assertEquals("Main", resp.getName());
                    assertEquals(1L, resp.getFranchise().getId());
                    assertEquals("Fr1", resp.getFranchise().getName());
                })
                .verifyComplete();

        verify(branchRepositoryPort, never()).existsBranchByFranchiseIdAndName(anyLong(), anyString());
        verify(branchRepositoryPort, never()).saveBranch(any());
    }

    @Test
    void updateBranch_whenNewNameAlreadyExists_shouldReturn409() {
        FranchiseEntity fr = franchise(1L, "Fr1");
        BranchEntity existing = new BranchEntity(10L, 1L, "Main", null, null);

        when(branchRepositoryPort.getBranchById(10L)).thenReturn(Mono.just(existing));
        when(franchiseRepositoryPort.getFranchiseById(1L)).thenReturn(Mono.just(fr));
        when(branchRepositoryPort.existsBranchByFranchiseIdAndName(1L, "New")).thenReturn(Mono.just(true));

        StepVerifier.create(branchService.updateBranch("New", 10L))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.CONFLICT, ex.getStatus());
                    assertEquals("BRANCH_ALREADY_EXISTS", ex.getCode());
                })
                .verify();

        verify(branchRepositoryPort, never()).saveBranch(any());
    }

    @Test
    void updateBranch_whenOk_shouldSaveAndReturnResponse_andKeepTimestamps() {
        OffsetDateTime created = OffsetDateTime.now().minusDays(1);
        OffsetDateTime updated = OffsetDateTime.now().minusHours(2);

        FranchiseEntity fr = franchise(1L, "Fr1");
        BranchEntity existing = new BranchEntity(10L, 1L, "Main", created, updated);

        when(branchRepositoryPort.getBranchById(10L)).thenReturn(Mono.just(existing));
        when(franchiseRepositoryPort.getFranchiseById(1L)).thenReturn(Mono.just(fr));
        when(branchRepositoryPort.existsBranchByFranchiseIdAndName(1L, "New")).thenReturn(Mono.just(false));

        BranchEntity saved = new BranchEntity(10L, 1L, "New", created, updated);
        when(branchRepositoryPort.saveBranch(any())).thenReturn(Mono.just(saved));

        StepVerifier.create(branchService.updateBranch("  New  ", 10L))
                .assertNext(resp -> {
                    assertEquals(10L, resp.getId());
                    assertEquals("New", resp.getName());
                    assertEquals(1L, resp.getFranchise().getId());
                    assertEquals("Fr1", resp.getFranchise().getName());
                })
                .verifyComplete();

        ArgumentCaptor<BranchEntity> captor = ArgumentCaptor.forClass(BranchEntity.class);
        verify(branchRepositoryPort).saveBranch(captor.capture());

        BranchEntity toSave = captor.getValue();
        assertEquals(10L, toSave.getId());
        assertEquals(1L, toSave.getFranchiseId());
        assertEquals("New", toSave.getName());
        assertEquals(created, toSave.getCreatedAt());
        assertEquals(updated, toSave.getUpdatedAt());
    }

    @Test
    void updateBranch_whenDbUniqueViolation_shouldReturn409() {
        FranchiseEntity fr = franchise(1L, "Fr1");
        BranchEntity existing = new BranchEntity(10L, 1L, "Main", null, null);

        when(branchRepositoryPort.getBranchById(10L)).thenReturn(Mono.just(existing));
        when(franchiseRepositoryPort.getFranchiseById(1L)).thenReturn(Mono.just(fr));
        when(branchRepositoryPort.existsBranchByFranchiseIdAndName(1L, "New")).thenReturn(Mono.just(false));
        when(branchRepositoryPort.saveBranch(any()))
                .thenReturn(Mono.error(new DataIntegrityViolationException("dup")));

        StepVerifier.create(branchService.updateBranch("New", 10L))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.CONFLICT, ex.getStatus());
                    assertEquals("BRANCH_ALREADY_EXISTS", ex.getCode());
                })
                .verify();
    }

    // -------------------------
    // getAllByFranchiseId
    // -------------------------

    @Test
    void getAllByFranchiseId_whenIdInvalid_shouldReturn400() {
        StepVerifier.create(branchService.getAllByFranchiseId(0L))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("BRANCH_FRANCHISE_ID_REQUIRED", ex.getCode());
                })
                .verify();

        verifyNoInteractions(branchRepositoryPort, franchiseRepositoryPort);
    }

    @Test
    void getAllByFranchiseId_whenFranchiseNotFound_shouldReturn404() {
        when(franchiseRepositoryPort.getFranchiseById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(branchService.getAllByFranchiseId(1L))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
                    assertEquals("FRANCHISE_NOT_FOUND", ex.getCode());
                })
                .verify();

        verify(franchiseRepositoryPort).getFranchiseById(1L);
        verify(branchRepositoryPort, never()).getAllByFranchiseId(anyLong());
    }

    @Test
    void getAllByFranchiseId_whenOk_shouldReturnList() {
        FranchiseEntity fr = franchise(1L, "Fr1");
        when(franchiseRepositoryPort.getFranchiseById(1L)).thenReturn(Mono.just(fr));

        BranchEntity b1 = new BranchEntity(10L, 1L, "B1", null, null);
        BranchEntity b2 = new BranchEntity(11L, 1L, "B2", null, null);
        when(branchRepositoryPort.getAllByFranchiseId(1L)).thenReturn(Flux.just(b1, b2));

        StepVerifier.create(branchService.getAllByFranchiseId(1L))
                .assertNext(dto -> assertEquals("B1", dto.getName()))
                .assertNext(dto -> assertEquals("B2", dto.getName()))
                .verifyComplete();

        verify(branchRepositoryPort).getAllByFranchiseId(1L);
    }

    // -------------------------
    // getBranch
    // -------------------------

    @Test
    void getBranch_whenFranchiseIdInvalid_shouldReturn400() {
        StepVerifier.create(branchService.getBranch(0L, "Main"))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("BRANCH_FRANCHISE_ID_REQUIRED", ex.getCode());
                })
                .verify();

        verifyNoInteractions(branchRepositoryPort, franchiseRepositoryPort);
    }

    @Test
    void getBranch_whenNameBlank_shouldReturn400() {
        StepVerifier.create(branchService.getBranch(1L, "   "))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("BRANCH_NAME_REQUIRED", ex.getCode());
                })
                .verify();

        verifyNoInteractions(branchRepositoryPort, franchiseRepositoryPort);
    }


    @Test
    void getBranch_whenBranchNotFound_shouldReturn404() {
        when(franchiseRepositoryPort.getFranchiseById(1L)).thenReturn(Mono.just(franchise(1L, "Fr1")));
        when(branchRepositoryPort.getBranchByFranchiseIdAndName(1L, "Main")).thenReturn(Mono.empty());

        StepVerifier.create(branchService.getBranch(1L, "Main"))
                .expectErrorSatisfies(err -> {
                    ApiException ex = (ApiException) err;
                    assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
                    assertEquals("BRANCH_NOT_FOUND", ex.getCode());
                })
                .verify();

        verify(branchRepositoryPort).getBranchByFranchiseIdAndName(1L, "Main");
    }

    @Test
    void getBranch_whenOk_shouldReturnDto() {
        FranchiseEntity fr = franchise(1L, "Fr1");
        BranchEntity br = new BranchEntity(10L, 1L, "Main", null, null);

        when(franchiseRepositoryPort.getFranchiseById(1L)).thenReturn(Mono.just(fr));
        when(branchRepositoryPort.getBranchByFranchiseIdAndName(1L, "Main")).thenReturn(Mono.just(br));

        StepVerifier.create(branchService.getBranch(1L, "  Main  "))
                .assertNext(dto -> {
                    assertEquals(10L, dto.getId());
                    assertEquals("Main", dto.getName());
                    assertNotNull(dto.getFranchise());
                    assertEquals(1L, dto.getFranchise().getId());
                    assertEquals("Fr1", dto.getFranchise().getName());
                })
                .verifyComplete();
    }
}
