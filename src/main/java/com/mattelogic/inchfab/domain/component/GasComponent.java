package com.mattelogic.inchfab.domain.component;

import com.mattelogic.inchfab.domain.enums.GasCalculation;
import com.mattelogic.inchfab.domain.exception.GasCalculationException;
import com.mattelogic.inchfab.domain.model.GasCalculationParameter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Component responsible for gas-related calculations and unit conversions. All calculations are
 * converted to minutes as the final step.
 */
@Component
@RequiredArgsConstructor
public class GasComponent {

  private final UnitConverterComponent unitConverterComponent;

  /**
   * Calculates gas measurement for group A using dual multiplication sum Formula: (flow * pressure
   * + temperature * density) converted to minutes
   *
   * @param flow        gas flow rate
   * @param pressure    gas pressure
   * @param temperature gas temperature
   * @param density     gas density
   * @return result in minutes
   * @throws GasCalculationException if input values are invalid
   */
  public double calculateGroupA(
      double flow,
      double pressure,
      double temperature,
      double density,
      boolean isMultiply) {
    return calculateAndConvert(
        GasCalculation.DUAL_MULTIPLICATION_SUM,
        new double[]{flow, pressure, temperature, density},
        isMultiply);
  }

  /**
   * Calculates gas measurement for group B using simple multiplication Formula: (flow * pressure)
   * converted to minutes
   *
   * @param flow     gas flow rate
   * @param pressure gas pressure
   * @return result in minutes
   * @throws GasCalculationException if input values are invalid
   */
  public double calculateGroupB(double flow, double pressure, boolean isMultiply) {
    return calculateAndConvert(
        GasCalculation.SIMPLE_MULTIPLICATION,
        new double[]{flow, pressure},
        isMultiply);
  }

  /**
   * Calculates gas quantity based on gas calculation parameters. Uses Group B calculation method
   * with parameters from GasCalculationParameter.
   *
   * @param params the gas calculation parameters
   * @return calculated gas quantity in minutes
   * @throws GasCalculationException if input values are invalid
   */
  public double calculateGasQuantity(GasCalculationParameter params) {
    double result = calculateGroupA(
        params.gasC(),
        params.gasA(),
        params.gasD(),
        params.gasC(),
        false
    );
    return unitConverterComponent.convertSecondsMinutes(result, false);
  }

  private double calculateAndConvert(
      GasCalculation calculation,
      double[] values,
      boolean isMultiply) {
    validateInputs(values);
    double result = calculation.calculate(values);
    return unitConverterComponent.convertSecondsMinutes(result, isMultiply);
  }

  private void validateInputs(double... values) {
    for (int i = 0; i < values.length; i++) {
      if (isInvalidValue(values[i])) {
        throw new GasCalculationException(
            "Invalid value at position %d: %f".formatted(i, values[i]));
      }
    }
  }

  private boolean isInvalidValue(double value) {
    return Double.isNaN(value) ||
        Double.isInfinite(value);
  }
}