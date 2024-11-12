package com.mattelogic.inchfab.common.constant;

import org.springframework.http.HttpStatus;

public final class ErrorTypeConstant {

  public static final String ERROR_BASE_URL = "https://docs.inchfab.com/errors";

  public static final ErrorType MISSING_VALUE = new ErrorType(
      "missing-value",
      "Missing Value",
      "A required value is missing",
      HttpStatus.BAD_REQUEST
  );

  public static final ErrorType VALUE_NOT_FOUND = new ErrorType(
      "value-not-found",
      "Value Not Found",
      "The requested value could not be found",
      HttpStatus.NOT_FOUND
  );

  public static final ErrorType CONVERSION_ERROR = new ErrorType(
      "conversion-error",
      "Conversion Error",
      "Error converting value",
      HttpStatus.BAD_REQUEST
  );

  public static final ErrorType MALFORMED_JSON = new ErrorType(
      "malformed-json",
      "Malformed JSON Request",
      "Malformed JSON request. Please check the request body format.",
      HttpStatus.BAD_REQUEST
  );

  public static final ErrorType INVALID_DATA_ACCESS = new ErrorType(
      "invalid-data-access",
      "Invalid Data Access Usage",
      "Expected unique result or null, but got more than one",
      HttpStatus.CONFLICT
  );

  public static final ErrorType VALIDATION_ERROR = new ErrorType(
      "validation-error",
      "Validation Failed",
      "One or more fields have validation errors",
      HttpStatus.BAD_REQUEST
  );

  public static final ErrorType ILLEGAL_ARGUMENT = new ErrorType(
      "illegal-argument",
      "Invalid Argument",
      "Invalid argument provided",
      HttpStatus.BAD_REQUEST
  );

  public static final ErrorType COMPANY_NOT_FOUND = new ErrorType(
      "company-not-found",
      "Company Not Found",
      "The requested company resource could not be found",
      HttpStatus.NOT_FOUND
  );

  public static final ErrorType DUPLICATE_COMPANY = new ErrorType(
      "duplicate-company",
      "Duplicate Company",
      "A company with this name already exists",
      HttpStatus.CONFLICT
  );

  public static final ErrorType PROJECT_NOT_FOUND = new ErrorType(
      "project-not-found",
      "Project Not Found",
      "The requested project resource could not be found",
      HttpStatus.NOT_FOUND
  );

  public static final ErrorType DUPLICATE_PROJECT = new ErrorType(
      "duplicate-project",
      "Duplicate Project",
      "A project with this name already exists",
      HttpStatus.CONFLICT
  );

  public static final ErrorType UNSUPPORTED_PROCESS_TYPE = new ErrorType(
      "unsupported_process_type",
      "Unsupported Process Type",
      "The specified process type is not supported",
      HttpStatus.BAD_REQUEST
  );

  public static final ErrorType PROCESSING_STEP_ERROR = new ErrorType(
      "processing_step_error",
      "Process Step Error",
      "An error occurred while processing the calculation step",
      HttpStatus.INTERNAL_SERVER_ERROR
  );

  public static final ErrorType DATABASE_ERROR = new ErrorType(
      "database-error",
      "Database Error",
      "An error occurred while accessing the database",
      HttpStatus.INTERNAL_SERVER_ERROR
  );

  public static final ErrorType FORMULA_DIVISION_BY_ZERO = new ErrorType(
      "formula-division-by-zero",
      "Formula Division By Zero",
      "Attempted division by zero or near-zero value in formula calculation",
      HttpStatus.BAD_REQUEST
  );

  public static final ErrorType FORMULA_NEGATIVE_VALUE = new ErrorType(
      "formula-negative-value",
      "Formula Negative Value",
      "Negative values are not allowed in formula calculation",
      HttpStatus.BAD_REQUEST
  );

  public static final ErrorType FORMULA_ARITHMETIC_OVERFLOW = new ErrorType(
      "formula-arithmetic-overflow",
      "Formula Arithmetic Overflow",
      "Formula calculation resulted in value overflow",
      HttpStatus.BAD_REQUEST
  );

  public static final ErrorType FORMULA_INVALID_FLOATING_POINT = new ErrorType(
      "formula-invalid-floating-point",
      "Formula Invalid Floating Point",
      "Invalid floating point value in formula calculation",
      HttpStatus.BAD_REQUEST
  );

  public record ErrorType(
      String path,
      String title,
      String defaultMessage,
      HttpStatus status
  ) {

  }
}