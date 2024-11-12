package com.mattelogic.inchfab.core.exception;

public class DuplicateCompanyException extends RuntimeException {

  public DuplicateCompanyException(String message) {
    super(message);
  }

  public DuplicateCompanyException(String message, Throwable cause) {
    super(message, cause);
  }
}