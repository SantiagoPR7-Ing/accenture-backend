package com.sprietogo.accenturebackend.application.port.service;


import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.request.RequestDTO;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.response.ResponseDTO;
import reactor.core.publisher.Mono;

public interface FranchiseServicePort {

    Mono<ResponseDTO> createFranchise(RequestDTO requestDTO);
    Mono<ResponseDTO> updateFranchise(String name, Long id);
    Mono<ResponseDTO> getFranchise(String name);

}
