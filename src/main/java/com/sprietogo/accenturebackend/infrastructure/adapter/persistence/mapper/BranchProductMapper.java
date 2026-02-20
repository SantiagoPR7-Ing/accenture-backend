package com.sprietogo.accenturebackend.infrastructure.adapter.persistence.mapper;

import com.sprietogo.accenturebackend.domain.model.BranchEntity;
import com.sprietogo.accenturebackend.domain.model.BranchProductEntity;
import com.sprietogo.accenturebackend.domain.model.ProductEntity;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.request.BranchProductRequestDTO;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.response.BranchProductResponseDTO;
import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.response.ResponseDTO;

public final class BranchProductMapper {

    private BranchProductMapper() {}

    public static BranchProductEntity toEntity(BranchProductRequestDTO dto) {
        if (dto == null) return null;
        return new BranchProductEntity(
                null,
                dto.getBranchId(),
                dto.getProductId(),
                dto.getStock(),
                null,
                null
        );
    }


    public static BranchProductResponseDTO toResponseDto(BranchProductEntity branchProductEntity
            , BranchEntity branchEntity, ProductEntity productEntity) {
        if (branchProductEntity == null || branchEntity == null
         || productEntity == null) return null;

        BranchProductResponseDTO dto = new BranchProductResponseDTO();
        dto.setId(branchProductEntity.getId());

        dto.setBranch(new ResponseDTO(branchEntity.getId(), branchEntity.getName()));
        dto.setProduct(new ResponseDTO(productEntity.getId(), productEntity.getName()));
        dto.setStock(branchProductEntity.getStock());

        return dto;
    }

}
