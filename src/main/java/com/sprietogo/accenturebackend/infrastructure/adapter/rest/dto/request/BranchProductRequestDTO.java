package com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.request;

import com.sprietogo.accenturebackend.utils.Constants;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BranchProductRequestDTO {

    @NotNull(message = "|branchId| " + Constants.ERROR_NOT_NULL)
    private Long branchId;

    @NotNull(message = "|productId| " + Constants.ERROR_NOT_NULL)
    private Long productId;

    @NotNull(message = "|stock| " + Constants.ERROR_NOT_NULL)
    private Integer stock;

}
