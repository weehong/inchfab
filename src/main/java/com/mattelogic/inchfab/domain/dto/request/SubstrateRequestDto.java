package com.mattelogic.inchfab.domain.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubstrateRequestDto(
    @NotNull(message = "Wafer size is required")
    @Min(value = 1, message = "Wafer size must be greater than or equal to 1")
    Integer waferSize,

    @NotBlank(message = "Name is required")
    String name
) {

}
