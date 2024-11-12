package com.mattelogic.inchfab.domain.exception;

/**
 * Custom runtime exception for handling errors during gas calculations. This exception is thrown
 * when gas-related calculations fail or encounter invalid data.
 */
public class GasCalculationException extends RuntimeException {

  /**
   * Constructs a new GasCalculationException with the specified detail message.
   *
   * @param message the detail message describing the cause of the exception
   */
  public GasCalculationException(String message) {
    super(message);
  }

  /**
   * Constructs a new GasCalculationException with the specified detail message and cause.
   *
   * @param message the detail message describing the cause of the exception
   * @param cause   the cause of the exception (which is saved for later retrieval by the
   *                {@link #getCause()} method)
   */
  public GasCalculationException(String message, Throwable cause) {
    super(message, cause);
  }
}