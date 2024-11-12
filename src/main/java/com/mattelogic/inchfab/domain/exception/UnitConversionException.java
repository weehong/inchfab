package com.mattelogic.inchfab.domain.exception;

/**
 * Custom runtime exception for handling errors during unit conversion operations. This exception is
 * thrown when unit conversions fail or encounter invalid/incompatible units.
 */
public class UnitConversionException extends RuntimeException {

  /**
   * Constructs a new UnitConversionException with the specified detail message.
   *
   * @param message the detail message describing the cause of the exception
   */
  public UnitConversionException(String message) {
    super(message);
  }

  /**
   * Constructs a new UnitConversionException with the specified detail message and cause.
   *
   * @param message the detail message describing the cause of the exception
   * @param cause   the cause of the exception (which is saved for later retrieval by the
   *                {@link #getCause()} method)
   */
  public UnitConversionException(String message, Throwable cause) {
    super(message, cause);
  }
}