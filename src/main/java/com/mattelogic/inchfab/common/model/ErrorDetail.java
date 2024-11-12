package com.mattelogic.inchfab.common.model;

import com.mattelogic.inchfab.common.constant.ErrorTypeConstant;
import com.mattelogic.inchfab.common.constant.ErrorTypeConstant.ErrorType;
import java.time.LocalDateTime;
import java.util.Map;

public record ErrorDetail(
    LocalDateTime timestamp,
    String type,
    String title,
    int status,
    String detail,
    String instance,
    Map<String, String> errors
) {

  public static ErrorDetail of(
      final ErrorType errorType,
      final String detail,
      final String instance,
      final Map<String, String> errors
  ) {
    return new ErrorDetail(
        LocalDateTime.now(),
        ErrorTypeConstant.ERROR_BASE_URL + "/" + errorType.path(),
        errorType.title(),
        errorType.status().value(),
        detail,
        instance,
        errors
    );
  }
}
