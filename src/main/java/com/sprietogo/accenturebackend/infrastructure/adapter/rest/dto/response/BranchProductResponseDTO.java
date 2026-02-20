package com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.response;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BranchProductResponseDTO {

    private Long id;
    private ResponseDTO branch;
    private ResponseDTO product;
    private Integer stock;

}
