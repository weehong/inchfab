package com.mattelogic.inchfab.core.exception;

public class DuplicateProjectException extends RuntimeException {

  public DuplicateProjectException(String message) {
    super(message);
  }

  public DuplicateProjectException(String message, Throwable cause) {
    super(message, cause);
  }
}