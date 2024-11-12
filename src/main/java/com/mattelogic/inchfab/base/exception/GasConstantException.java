package com.mattelogic.inchfab.base.exception;

public class GasConstantException extends RuntimeException {

  public GasConstantException(String message) {
    super(message);
  }

  public GasConstantException(String message, Throwable cause) {
    super(message, cause);
  }
}