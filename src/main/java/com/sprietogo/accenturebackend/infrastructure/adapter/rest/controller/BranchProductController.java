package com.sprietogo.accenturebackend.infrastructure.adapter.rest.controller;

import com.sprietogo.accenturebackend.application.port.service.BranchProductServicePort;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.request.BranchProductRequestDTO;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.response.BranchProductResponseDTO;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.response.TopStockProductByBranchResponseDTO;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/api/v1/branch-products", produces = MediaType.APPLICATION_JSON_VALUE)
public class BranchProductController {

    private final BranchProductServicePort branchProductServicePort;

    @GetMapping("/{id}")
    public Mono<BranchProductResponseDTO> getById(@PathVariable Long id) {
        return branchProductServicePort.getBranchProductById(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<BranchProductResponseDTO> create(@Valid @RequestBody BranchProductRequestDTO requestDTO) {
        return branchProductServicePort.createBranchProduct(requestDTO);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<BranchProductResponseDTO> update(@PathVariable Long id,
                                                @Valid @RequestBody BranchProductRequestDTO requestDTO) {
        return branchProductServicePort.updateBranchProduct(requestDTO, id);
    }

    @DeleteMapping
    public Mono<Void> delete(@RequestParam("branchId") Long branchId,
                             @RequestParam("productId") Long productId) {
        return branchProductServicePort.deleteByBranchIdAndProductId(branchId, productId);
    }

    @GetMapping
    public Flux<BranchProductResponseDTO> getAllByBranch(@RequestParam("branchId") Long branchId) {
        return branchProductServicePort.getAllByBranchId(branchId);
    }

    @GetMapping("/by-ids")
    public Mono<BranchProductResponseDTO> getByBranchAndProduct(@RequestParam("branchId") Long branchId,
                                                                @RequestParam("productId") Long productId) {
        return branchProductServicePort.getByBranchIdAndProductId(branchId, productId);
    }

    @PatchMapping("/stock")
    public Mono<BranchProductResponseDTO> updateStock(@RequestParam("branchId") Long branchId,
                                                     @RequestParam("productId") Long productId,
                                                     @RequestParam("stock") Integer stock) {
        return branchProductServicePort.updateBranchProductStock(branchId, productId, stock);
    }

    @GetMapping("/report/top-stock")
    public Flux<TopStockProductByBranchResponseDTO> topStock(@RequestParam("franchiseId") Long franchiseId) {
        return branchProductServicePort.getTopStockProductsByBranch(franchiseId);
    }

}
