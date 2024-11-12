package com.mattelogic.inchfab.domain.exception;

/**
 * Custom exception for lithography calculation errors. Extends RuntimeException to allow unchecked
 * exception handling.
 */
public class LithographyCalculationException extends RuntimeException {

  /**
   * Constructs a new lithography calculation exception with the specified detail message.
   *
   * @param message the detail message
   */
  public LithographyCalculationException(String message) {
    super(message);
  }

  /**
   * Constructs a new lithography calculation exception with the specified detail message and
   * cause.
   *
   * @param message the detail message
   * @param cause   the cause of the exception
   */
  public LithographyCalculationException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new lithography calculation exception with the specified cause.
   *
   * @param cause the cause of the exception
   */
  public LithographyCalculationException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new lithography calculation exception with the specified detail message, cause,
   * suppression enabled or disabled, and writable stack trace enabled or disabled.
   *
   * @param message            the detail message
   * @param cause              the cause of the exception
   * @param enableSuppression  whether or not suppression is enabled or disabled
   * @param writableStackTrace whether or not the stack trace should be writable
   */
  public LithographyCalculationException(String message, Throwable cause,
      boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}