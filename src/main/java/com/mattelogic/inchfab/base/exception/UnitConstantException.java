package com.mattelogic.inchfab.base.exception;

public class UnitConstantException extends RuntimeException {

  public UnitConstantException(String message) {
    super(message);
  }

  public UnitConstantException(String message, Throwable cause) {
    super(message, cause);
  }
}