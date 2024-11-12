package com.mattelogic.inchfab.domain.enums;

import java.util.function.DoubleBinaryOperator;
import java.util.function.Function;

/**
 * Enum representing different power calculation formulas. Provides predefined mathematical
 * operations for power-related computations.
 */
public enum PowerFormula {

  /**
   * Performs simple multiplication of two values. Formula: result = a * b
   */
  SIMPLE_MULTIPLICATION((a, b) -> a * b),

  /**
   * Performs multiplication of two pairs of values and sums the results. Formula: result = (a * b)
   * + (c * d)
   */
  DUAL_MULTIPLICATION_SUM((a, b, c, d) -> (a * b) + (c * d)),

  /**
   * Performs multiplication of three values. Formula: result = a * b * c
   */
  TRIPLE_MULTIPLICATION((a, b, c) -> a * b * c);

  /**
   * The function that performs the actual calculation for each enum constant.
   */
  private final Function<double[], Double> formula;

  /**
   * Constructs a PowerFormula for binary operations.
   *
   * @param operator the binary operator that performs calculation on two doubles
   */
  PowerFormula(DoubleBinaryOperator operator) {
    this.formula = values -> operator.applyAsDouble(values[0], values[1]);
  }

  /**
   * Constructs a PowerFormula for quadruple operations.
   *
   * @param operator the operator that performs calculation on four doubles
   */
  PowerFormula(QuadOperator operator) {
    this.formula = values -> operator.apply(values[0], values[1], values[2], values[3]);
  }

  /**
   * Constructs a PowerFormula for triple operations.
   *
   * @param operator the operator that performs calculation on three doubles
   */
  PowerFormula(TripleOperator operator) {
    this.formula = values -> operator.apply(values[0], values[1], values[2]);
  }

  /**
   * Executes the power calculation using the provided values.
   *
   * @param values variable number of double values to be used in the calculation
   * @return the result of the calculation
   */
  public double calculate(double... values) {
    return formula.apply(values);
  }

  /**
   * Functional interface for operations that take three double parameters.
   */
  @FunctionalInterface
  private interface TripleOperator {

    /**
     * Applies the operation to three double values.
     *
     * @param a the first value
     * @param b the second value
     * @param c the third value
     * @return the result of the operation
     */
    double apply(double a, double b, double c);
  }

  /**
   * Functional interface for operations that take four double parameters.
   */
  @FunctionalInterface
  private interface QuadOperator {

    /**
     * Applies the operation to four double values.
     *
     * @param a the first value
     * @param b the second value
     * @param c the third value
     * @param d the fourth value
     * @return the result of the operation
     */
    double apply(double a, double b, double c, double d);
  }
}