package com.mattelogic.inchfab.domain.enums;

import com.mattelogic.inchfab.domain.exception.TimeCalculationException;
import java.util.function.DoubleBinaryOperator;

/**
 * Enum representing basic arithmetic operations for time calculations. Provides safe mathematical
 * operations with error handling for time-related computations.
 */
public enum TimeOperation {
  /**
   * Multiplies two time values. Formula: result = a * b
   */
  MULTIPLY((a, b) -> a * b),

  /**
   * Divides first time value by second. Formula: result = a / b
   *
   * @throws TimeCalculationException if the divisor is zero
   */
  DIVIDE((a, b) -> {
    if (b == 0) {
      throw new TimeCalculationException("Division by zero");
    }
    return a / b;
  }),

  /**
   * Adds two time values. Formula: result = a + b
   */
  ADD(Double::sum),

  /**
   * Subtracts second time value from first. Formula: result = a - b
   */
  SUBTRACT((a, b) -> a - b);

  /**
   * The binary operator that performs the actual calculation for each operation.
   */
  private final DoubleBinaryOperator operator;

  /**
   * Constructs a TimeOperation with the specified binary operator.
   *
   * @param operator the binary operator that performs the calculation
   */
  TimeOperation(DoubleBinaryOperator operator) {
    this.operator = operator;
  }

  /**
   * Applies the time operation to two double values.
   *
   * @param first  the first operand
   * @param second the second operand
   * @return the result of the operation
   * @throws TimeCalculationException if division by zero is attempted
   */
  public double apply(double first, double second) {
    return operator.applyAsDouble(first, second);
  }
}