package com.mattelogic.inchfab.domain.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record IcpcvdRequestDto(
    @NotNull(message = "Wafer size is required")
    @Min(value = 1, message = "Wafer size must be greater than or equal to 1")
    Integer waferSize,

    @NotBlank(message = "Name is required")
    String name,

    @NotNull(message = "Thickness is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Value must be greater than 0")
    Double thickness,

    @NotNull(message = "Refractive Index is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Value must be greater than 0")
    Double refractiveIndex,

    @NotNull(message = "Film Stress is required")
    Integer filmStress
) {

}
