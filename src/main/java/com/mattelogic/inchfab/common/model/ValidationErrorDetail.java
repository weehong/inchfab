package com.mattelogic.inchfab.common.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.validation.FieldError;

public record ValidationErrorDetail(Map<String, String> fieldErrors) {

  public static ValidationErrorDetail fromFieldErrors(final List<FieldError> fieldErrors) {
    Map<String, String> errors = fieldErrors.stream()
        .collect(Collectors.toMap(
            FieldError::getField,
            error -> error.getDefaultMessage() != null ? error.getDefaultMessage()
                : "Invalid value",
            (first, second) -> first
        ));

    return new ValidationErrorDetail(errors);
  }
}