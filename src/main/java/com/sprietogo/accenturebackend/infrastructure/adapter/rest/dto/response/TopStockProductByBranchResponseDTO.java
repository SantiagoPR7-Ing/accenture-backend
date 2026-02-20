package com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.response;

public record TopStockProductByBranchResponseDTO(
            ResponseDTO branch,
            ResponseDTO product,
            Integer stock
    ) {}