package com.mattelogic.inchfab.core.exception;

public class PriceNotFoundException extends RuntimeException {

  public PriceNotFoundException(String message) {
    super(message);
  }

  public PriceNotFoundException(Long id) {
    super(String.format("Price with Project ID %d could not be found", id));
  }
}