package com.sprietogo.accenturebackend.infrastructure.adapter.rest.dto.request;


import com.sprietogo.accenturebackend.utils.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RequestDTO {

    @NotBlank(message = "|name|" + Constants.ERROR_NOT_BLANK)
    @Size(max = 120, message = "|name| No puede superar los 120 caracteres")
    private String name;

}
