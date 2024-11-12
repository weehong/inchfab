package com.mattelogic.inchfab.core.exception;

public class CompanyNotFoundException extends RuntimeException {

  public CompanyNotFoundException(String message) {
    super(message);
  }

  public CompanyNotFoundException(Long id) {
    super(String.format("Company with ID %d could not be found", id));
  }
}