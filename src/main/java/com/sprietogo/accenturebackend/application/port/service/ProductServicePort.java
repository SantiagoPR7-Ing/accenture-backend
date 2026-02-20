package com.sprietogo.accenturebackend.application.port.service;

import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.request.RequestDTO;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.response.ResponseDTO;
import reactor.core.publisher.Mono;

public interface ProductServicePort {

    Mono<ResponseDTO> saveProduct(RequestDTO requestDTO);
    Mono<ResponseDTO> updateProduct(String name, Long id);
    Mono<ResponseDTO> getProductByName(String name);
}
