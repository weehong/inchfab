package com.mattelogic.inchfab.domain.component;

import com.mattelogic.inchfab.domain.enums.TimeOperation;
import com.mattelogic.inchfab.domain.exception.TimeCalculationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Component for handling time-related calculations and conversions.
 */
@Component
@RequiredArgsConstructor
public class TimeComponent {

  private final UnitConverterComponent unitConverterComponent;

  /**
   * Performs time calculation based on specified operation
   *
   * @param firstDuration  first time value
   * @param secondDuration second time value
   * @param operation      operation to perform
   * @return result of the calculation
   * @throws TimeCalculationException if input is invalid during division
   */
  public double calculateGroupA(
      double firstDuration,
      double secondDuration,
      TimeOperation operation) {
    if (operation == TimeOperation.DIVIDE) {
      validateInputs(firstDuration, secondDuration);
    }
    return operation.apply(firstDuration, secondDuration);
  }

  /**
   * Converts time calculation result to minutes
   *
   * @param firstDuration  first time value
   * @param secondDuration second time value
   * @param operation      operation to perform
   * @return result in minutes
   * @throws TimeCalculationException if input is invalid during division
   */
  public double calculateGroupB(
      double firstDuration,
      double secondDuration,
      TimeOperation operation,
      boolean isMultiply) {
    double result = calculateGroupA(firstDuration, secondDuration, operation);
    return unitConverterComponent.convertSecondsMinutes(result, isMultiply);
  }

  private void validateInputs(double... durations) {
    for (double duration : durations) {
      if (isInvalidInput(duration)) {
        throw new TimeCalculationException(
            "Invalid duration value: " + duration);
      }
    }
  }

  private boolean isInvalidInput(double duration) {
    return Double.isNaN(duration)
        || Double.isInfinite(duration)
        || duration <= 0;
  }
}