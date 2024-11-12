package com.mattelogic.inchfab.domain.component;

import com.mattelogic.inchfab.common.constant.ErrorTypeConstant;
import com.mattelogic.inchfab.domain.exception.FormulaCalculationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Component for handling various formula calculations.
 */
@Component
@RequiredArgsConstructor
public class FormulaComponent {

  private static final double EPSILON = 1e-10;
  private final UnitConverterComponent unitConverterComponent;

  /**
   * Calculates formula: A * B / s_per_min * (1 + C)
   */
  public double calculateFormula1(double a, double b, double sPerMin, double c) {
    validateInputs(a, b, sPerMin, c);
    validateDivision(sPerMin);

    try {
      return (a * b) / sPerMin * (1 + c);
    } catch (ArithmeticException e) {
      throw new FormulaCalculationException(
          ErrorTypeConstant.FORMULA_ARITHMETIC_OVERFLOW,
          "Overflow in formula1 calculation"
      );
    }
  }

  /**
   * Calculates formula: (A * B + C * D) / s_per_min * (1 + E)
   */
  public double calculateFormula2(double a, double b, double c, double d, double sPerMin,
      double e) {
    validateInputs(a, b, c, d, sPerMin, e);
    validateDivision(sPerMin);

    try {
      return (a * b + c * d) / sPerMin * (1 + e);
    } catch (ArithmeticException err) {
      throw new FormulaCalculationException(
          ErrorTypeConstant.FORMULA_ARITHMETIC_OVERFLOW,
          "Overflow in formula3 calculation"
      );
    }
  }

  /**
   * Calculates formula: A + B
   */
  public double calculateFormula3(double a, double b) {
    validateInputs(a, b);

    try {
      return a + b;
    } catch (ArithmeticException e) {
      throw new FormulaCalculationException(
          ErrorTypeConstant.FORMULA_ARITHMETIC_OVERFLOW,
          "Overflow in formula5 calculation"
      );
    }
  }

  /**
   * Calculates formula: A * B
   */
  public double calculateFormula4(double a, double b) {
    validateInputs(a, b);

    try {
      return a * b;
    } catch (ArithmeticException e) {
      throw new FormulaCalculationException(
          ErrorTypeConstant.FORMULA_ARITHMETIC_OVERFLOW,
          "Overflow in formula6 calculation"
      );
    }
  }

  /**
   * Calculates formula: A * B + C * D
   */
  public double calculateFormula5(double a, double b, double c, double d) {
    validateInputs(a, b, c, d);

    try {
      return a * b + c * d;
    } catch (ArithmeticException e) {
      throw new FormulaCalculationException(
          ErrorTypeConstant.FORMULA_ARITHMETIC_OVERFLOW,
          "Overflow in formula7/13 calculation"
      );
    }
  }

  /**
   * Calculates formula: A * B * C
   */
  public double calculateFormula6(double a, double b, double c) {
    validateInputs(a, b, c);

    try {
      return a * b * c;
    } catch (ArithmeticException e) {
      throw new FormulaCalculationException(
          ErrorTypeConstant.FORMULA_ARITHMETIC_OVERFLOW,
          "Overflow in formula8 calculation"
      );
    }
  }

  /**
   * Calculates formula: A / B * s_per_min
   */
  public double calculateFormula7(double a, double b, double sPerMin) {
    validateInputs(a, b, sPerMin);
    validateDivision(b);

    try {
      return a / b * sPerMin;
    } catch (ArithmeticException e) {
      throw new FormulaCalculationException(
          ErrorTypeConstant.FORMULA_ARITHMETIC_OVERFLOW,
          "Overflow in formula9 calculation"
      );
    }
  }

  /**
   * Calculates formula: A * B * C / s_per_h / W_per_kW
   */
  public double calculateFormula8(double a, double b, double c, double sPerH, double wPerKw) {
    validateInputs(a, b, c, sPerH, wPerKw);
    validateDivision(sPerH);
    validateDivision(wPerKw);

    try {
      return (a * b * c) / sPerH / wPerKw;
    } catch (ArithmeticException e) {
      throw new FormulaCalculationException(
          ErrorTypeConstant.FORMULA_ARITHMETIC_OVERFLOW,
          "Overflow in formula12 calculation"
      );
    }
  }

  private void validateInputs(double... values) {
    for (double value : values) {
      validateSingleValue(value);
    }
  }

  private void validateSingleValue(double value) {
    if (Double.isNaN(value)) {
      throw new FormulaCalculationException(
          ErrorTypeConstant.FORMULA_INVALID_FLOATING_POINT,
          "Value is NaN: " + value
      );
    }
    if (Double.isInfinite(value)) {
      throw new FormulaCalculationException(
          ErrorTypeConstant.FORMULA_INVALID_FLOATING_POINT,
          "Value is infinite: " + value
      );
    }
    if (value < 0) {
      throw new FormulaCalculationException(
          ErrorTypeConstant.FORMULA_NEGATIVE_VALUE,
          "Negative value not allowed: " + value
      );
    }
  }

  private void validateDivision(double divisor) {
    if (Math.abs(divisor) < EPSILON) {
      throw new FormulaCalculationException(
          ErrorTypeConstant.FORMULA_DIVISION_BY_ZERO,
          "Division by zero or near-zero value: " + divisor
      );
    }
  }
}
