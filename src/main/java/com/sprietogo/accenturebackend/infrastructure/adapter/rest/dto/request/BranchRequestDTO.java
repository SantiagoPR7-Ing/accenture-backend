package com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.request;

import com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.response.ResponseDTO;
import com.sprietogo.accenturebackend.utils.Constants;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BranchRequestDTO extends ResponseDTO {

    @NotNull(message = "|franchiseId|" + Constants.ERROR_NOT_NULL)
    private Long franchiseId;

}
