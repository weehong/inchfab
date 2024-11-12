package com.mattelogic.inchfab.common.exception;

import static com.mattelogic.inchfab.common.constant.ErrorTypeConstant.DATABASE_ERROR;

import com.mattelogic.inchfab.common.constant.ErrorTypeConstant;
import com.mattelogic.inchfab.common.constant.ErrorTypeConstant.ErrorType;
import com.mattelogic.inchfab.common.dto.ErrorResponseDto;
import com.mattelogic.inchfab.common.model.ErrorDetail;
import com.mattelogic.inchfab.common.model.ValidationErrorDetail;
import com.mattelogic.inchfab.core.exception.CompanyNotFoundException;
import com.mattelogic.inchfab.core.exception.DuplicateCompanyException;
import com.mattelogic.inchfab.core.exception.DuplicateProjectException;
import com.mattelogic.inchfab.core.exception.ProcessingStepException;
import com.mattelogic.inchfab.core.exception.ProjectNotFoundException;
import com.mattelogic.inchfab.core.exception.UnsupportedProcessTypeException;
import com.mattelogic.inchfab.domain.exception.GasCalculationException;
import com.mattelogic.inchfab.domain.exception.TimeCalculationException;
import com.mattelogic.inchfab.domain.exception.UnitConversionException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.CompletionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler({
      GasCalculationException.class,
      TimeCalculationException.class,
      UnitConversionException.class,
      HttpMessageNotReadableException.class,
      InvalidDataAccessApiUsageException.class,
      MethodArgumentNotValidException.class,
      IllegalArgumentException.class,
      DuplicateCompanyException.class,
      DuplicateProjectException.class,
      CompanyNotFoundException.class,
      ProjectNotFoundException.class,
      UnsupportedProcessTypeException.class,
      // Remove ProcessingStepException from here since it has its own handler
      DataAccessException.class
  })
  public ResponseEntity<ErrorResponseDto> handleException(
      final Exception ex,
      final HttpServletRequest request
  ) {
    ErrorType errorType = switch (ex) {
      case GasCalculationException ignored -> ErrorTypeConstant.MISSING_VALUE;
      case TimeCalculationException ignored -> ErrorTypeConstant.VALUE_NOT_FOUND;
      case UnitConversionException ignored -> ErrorTypeConstant.CONVERSION_ERROR;
      case HttpMessageNotReadableException ignored -> ErrorTypeConstant.MALFORMED_JSON;
      case InvalidDataAccessApiUsageException ignored -> ErrorTypeConstant.INVALID_DATA_ACCESS;
      case DataAccessException ignored -> DATABASE_ERROR;
      case MethodArgumentNotValidException ignored -> ErrorTypeConstant.VALIDATION_ERROR;
      case IllegalArgumentException ignored -> ErrorTypeConstant.ILLEGAL_ARGUMENT;
      case DuplicateCompanyException ignored -> ErrorTypeConstant.DUPLICATE_COMPANY;
      case CompanyNotFoundException ignored -> ErrorTypeConstant.COMPANY_NOT_FOUND;
      case DuplicateProjectException ignored -> ErrorTypeConstant.DUPLICATE_PROJECT;
      case ProjectNotFoundException ignored -> ErrorTypeConstant.PROJECT_NOT_FOUND;
      case UnsupportedProcessTypeException ignored -> ErrorTypeConstant.UNSUPPORTED_PROCESS_TYPE;
      default -> throw new IllegalStateException("Unexpected exception type: " + ex.getClass());
    };

    return handleError(ex, errorType, request);
  }

  @ExceptionHandler(ProcessingStepException.class)
  public ResponseEntity<ErrorResponseDto> handleProcessingStepException(
      ProcessingStepException ex,
      HttpServletRequest request) {
    log.error("Processing step error: {}", ex.getMessage(), ex);

    return handleError(ex, ErrorTypeConstant.PROCESSING_STEP_ERROR, request);
  }

  @ExceptionHandler(CompletionException.class)
  public ResponseEntity<ErrorResponseDto> handleCompletionException(
      CompletionException ex,
      HttpServletRequest request) {
    Throwable cause = ExceptionUtils.getRootCause(ex);

    if (cause instanceof ProcessingStepException processingStepEx) {
      return handleProcessingStepException(processingStepEx, request);
    }

    // For other exceptions, try to handle them with our regular exception handler
    if (cause instanceof Exception actualException) {
      try {
        return handleException(actualException, request);
      } catch (IllegalStateException e) {
        log.error("Unhandled exception during async processing: {}", cause.getMessage(), cause);
        return handleError(
            actualException,
            DATABASE_ERROR,
            request
        );
      }
    }

    // Default error handling
    log.error("Unexpected error during async processing: {}", ex.getMessage(), ex);
    return handleError(
        ex,
        DATABASE_ERROR,
        request
    );
  }

  public ResponseEntity<ErrorResponseDto> handleError(
      final Exception ex,
      final ErrorType errorType,
      final HttpServletRequest request
  ) {
    ErrorResponseDto detail = switch (ex) {
      case MethodArgumentNotValidException e -> {
        ValidationErrorDetail validationDetail = ValidationErrorDetail.fromFieldErrors(
            e.getBindingResult().getFieldErrors()
        );
        yield createErrorResponse(errorType, errorType.defaultMessage(),
            request.getRequestURI(), validationDetail.fieldErrors());
      }
      case Exception e -> createErrorResponse(errorType, e.getMessage(),
          request.getRequestURI(), null);
    };

    return ResponseEntity
        .status(errorType.status())
        .body(detail);
  }

  private ErrorResponseDto createErrorResponse(
      final ErrorType errorType,
      final String detail,
      final String instance,
      final Map<String, String> errors
  ) {
    ErrorDetail errorDetail = ErrorDetail.of(errorType, detail, instance, errors);

    return ErrorResponseDto.builder()
        .timestamp(errorDetail.timestamp())
        .type(errorDetail.type())
        .title(errorDetail.title())
        .status(errorDetail.status())
        .detail(errorDetail.detail())
        .instance(errorDetail.instance())
        .errors(errorDetail.errors())
        .build();
  }
}