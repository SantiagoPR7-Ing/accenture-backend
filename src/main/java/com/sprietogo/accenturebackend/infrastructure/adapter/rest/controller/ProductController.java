package com.sprietogo.accenturebackend.infrastructure.adapter.rest.controller;

import com.sprietogo.accenturebackend.application.port.service.ProductServicePort;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.request.RequestDTO;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.response.ResponseDTO;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/api/v1/products", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductController {

    private final ProductServicePort productServicePort;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseDTO> create(@Valid @RequestBody RequestDTO requestDTO) {
        return productServicePort.saveProduct(requestDTO);
    }

    @GetMapping("/name/{name}")
    public Mono<ResponseDTO> getByName(@PathVariable String name) {
        return productServicePort.getProductByName(name);
    }

    @PutMapping("/id/{id}/name/{name}")
    public Mono<ResponseDTO> updateName(@PathVariable Long id, @PathVariable() String name) {
        return productServicePort.updateProduct(name, id);
    }
}
