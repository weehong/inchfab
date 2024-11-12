package com.mattelogic.inchfab.domain.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ManualProcessRequestDto(
    @NotNull(message = "Wafer size is required")
    @Min(value = 1, message = "Wafer size must be greater than or equal to 1")
    Integer waferSize,

    @NotBlank(message = "Name is required")
    String name,

    @NotNull(message = "Lot Charge is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Value must be greater than 0")
    Double lotCharge,

    @NotNull(message = "Lot Size is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Value must be greater than 0")
    Double lotSize,

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Value must be greater than 0")
    Double amount,

    @NotNull(message = "Rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Value must be greater than 0")
    Double rate
) {

}