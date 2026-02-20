package com.sprietogo.accenturebackend.infrastructure.adapter.persistence.mapper;

import com.sprietogo.accenturebackend.domain.model.FranchiseEntity;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.request.RequestDTO;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.response.ResponseDTO;
import org.springframework.stereotype.Component;

@Component
public final class FranchiseMapper {

    private FranchiseMapper() {}

    public static FranchiseEntity toEntity(RequestDTO dto) {
        if (dto == null) return null;
        return new FranchiseEntity(
                null,
                dto.getName(),
                null,
                null
        );
    }

    public static FranchiseEntity toEntity(RequestDTO dto, Long id) {
        if (dto == null) return null;
        return new FranchiseEntity(
                id,
                dto.getName(),
                null,
                null
        );
    }

    public static ResponseDTO toResponseDto(FranchiseEntity entity) {
        if (entity == null) return null;
        return new ResponseDTO(entity.getId(), entity.getName());
    }

}
