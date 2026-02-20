package com.sprietogo.accenturebackend.infrastructure.adapter.rest.controller;

import com.sprietogo.accenturebackend.application.port.service.BranchServicePort;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.request.BranchRequestDTO;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.response.BranchResponseDTO;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/api/v1/branches", produces = MediaType.APPLICATION_JSON_VALUE)
public class BranchController {

    private final BranchServicePort branchServicePort;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<BranchResponseDTO> create(@Valid @RequestBody BranchRequestDTO requestDTO) {
        return branchServicePort.createBranch(requestDTO);
    }


    @PutMapping("/id/{id}/name/{name}")
    public Mono<BranchResponseDTO> updateName(@PathVariable Long id, @PathVariable() String name) {
        return branchServicePort.updateBranch(name, id);
    }

    @GetMapping
    public Flux<BranchResponseDTO> getAllByFranchise(@RequestParam("franchiseId") Long franchiseId) {
        return branchServicePort.getAllByFranchiseId(franchiseId);
    }

    @GetMapping("/name/{name}")
    public Mono<BranchResponseDTO> getByName(
            @RequestParam("franchiseId") Long id,
            @PathVariable() String name
    ) {
        return branchServicePort.getBranch(id, name);
    }
}
