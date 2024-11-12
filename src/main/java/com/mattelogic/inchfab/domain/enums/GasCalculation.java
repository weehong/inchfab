package com.mattelogic.inchfab.domain.enums;

import com.mattelogic.inchfab.domain.exception.GasCalculationException;
import java.util.function.Function;

public enum GasCalculation {

  SIMPLE_MULTIPLICATION(values -> {
    if (values.length != 2) {
      throw new GasCalculationException(
          "Simple multiplication requires exactly 2 values");
    }
    return values[0] * values[1];
  }),

  DUAL_MULTIPLICATION_SUM(values -> {
    if (values.length != 4) {
      throw new GasCalculationException(
          "Dual multiplication sum requires exactly 4 values");
    }
    return (values[0] * values[1]) + (values[2] * values[3]);
  });

  private final Function<double[], Double> calculator;

  GasCalculation(Function<double[], Double> calculator) {
    this.calculator = calculator;
  }

  public double calculate(double[] values) {
    return calculator.apply(values);
  }
}