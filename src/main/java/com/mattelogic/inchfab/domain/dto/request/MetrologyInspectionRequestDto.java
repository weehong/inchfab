package com.mattelogic.inchfab.domain.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MetrologyInspectionRequestDto(
    @NotBlank(message = "Name is required")
    String name,

    @NotBlank(message = "Location is required")
    String location,

    @NotNull(message = "Time wafer hour is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Value must be greater than 0")
    Double timeWaferHour
) {

}