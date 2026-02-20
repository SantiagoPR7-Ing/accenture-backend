package com.sprietogo.accenturebackend.infrastructure.adapter.persistence.mapper;

import com.sprietogo.accenturebackend.domain.model.BranchEntity;
import com.sprietogo.accenturebackend.domain.model.FranchiseEntity;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.request.BranchRequestDTO;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.response.BranchResponseDTO;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.response.ResponseDTO;

public final class BranchMapper {

    private BranchMapper() {}

    public static BranchEntity toEntity(BranchRequestDTO dto) {
        if (dto == null) return null;
        return new BranchEntity(
                null,
                dto.getFranchiseId(),
                dto.getName(),
                null,
                null
        );
    }

    public static BranchEntity toEntity(BranchRequestDTO dto, Long id) {
        if (dto == null) return null;
        return new BranchEntity(
                id,
                dto.getFranchiseId(),
                dto.getName(),
                null,
                null
        );
    }

    public static BranchResponseDTO toResponseDto(BranchEntity branchEntity, FranchiseEntity franchiseEntity) {
        if (branchEntity == null || franchiseEntity == null) return null;

        BranchResponseDTO dto = new BranchResponseDTO(new ResponseDTO(franchiseEntity.getId(),
                franchiseEntity.getName()));
        dto.setName(branchEntity.getName());
        dto.setId(branchEntity.getId());

        return dto;
    }

}
