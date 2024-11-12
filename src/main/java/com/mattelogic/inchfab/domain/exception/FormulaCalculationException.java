package com.mattelogic.inchfab.domain.exception;

import com.mattelogic.inchfab.common.constant.ErrorTypeConstant.ErrorType;

public class FormulaCalculationException extends RuntimeException {

  private final ErrorType errorType;
  private final String details;

  public FormulaCalculationException(ErrorType errorType, String details) {
    super(String.format("%s: %s - %s", errorType.title(), errorType.defaultMessage(), details));
    this.errorType = errorType;
    this.details = details;
  }
}