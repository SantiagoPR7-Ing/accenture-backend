package com.sprietogo.accenturebackend.infrastructure.adapter.persistence.mapper;

import com.sprietogo.accenturebackend.domain.model.ProductEntity;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.request.RequestDTO;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.response.ResponseDTO;

public final class ProductMapper {

    private ProductMapper() {}


    public static ProductEntity toEntity(RequestDTO dto) {
        if (dto == null) return null;
        return new ProductEntity(
                null,
                dto.getName(),
                null,
                null
        );
    }

    public static ProductEntity toEntity(RequestDTO dto, Long id) {
        if (dto == null) return null;
        return new ProductEntity(
                id,
                dto.getName(),
                null,
                null
        );
    }

    public static ResponseDTO toResponseDto(ProductEntity entity) {
        if (entity == null) return null;
        return new ResponseDTO(entity.getId(), entity.getName());
    }

}
