package com.mattelogic.inchfab.core.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CompanyRequestDto(
    @NotBlank(message = "Name is cannot be blank.")
    @NotNull(message = "Name is cannot be null.")
    String name,

    String logo,

    @Email(message = "Email is not valid.")
    @NotBlank(message = "Name is cannot be blank.")
    @NotNull(message = "Name is cannot be null.")
    String email,

    @NotBlank(message = "Name is cannot be blank.")
    @NotNull(message = "Name is cannot be null.")
    String title
) {

}
