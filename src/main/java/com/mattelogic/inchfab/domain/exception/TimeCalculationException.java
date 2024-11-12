package com.mattelogic.inchfab.domain.exception;

/**
 * Custom runtime exception for handling errors during time-related calculations. This exception is
 * thrown when time calculations fail or encounter invalid data.
 */
public class TimeCalculationException extends RuntimeException {

  /**
   * Constructs a new TimeCalculationException with the specified detail message.
   *
   * @param message the detail message describing the cause of the exception
   */
  public TimeCalculationException(String message) {
    super(message);
  }

  /**
   * Constructs a new TimeCalculationException with the specified detail message and cause.
   *
   * @param message the detail message describing the cause of the exception
   * @param cause   the cause of the exception (which is saved for later retrieval by the
   *                {@link #getCause()} method)
   */
  public TimeCalculationException(String message, Throwable cause) {
    super(message, cause);
  }
}