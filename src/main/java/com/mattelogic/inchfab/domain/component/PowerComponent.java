package com.mattelogic.inchfab.domain.component;

import com.mattelogic.inchfab.domain.enums.PowerFormula;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Component responsible for power-related calculations. Follows SOLID principles by having a single
 * responsibility and dependency injection.
 */
@Component
@RequiredArgsConstructor
public class PowerComponent {

  private final UnitConverterComponent unitConverterComponent;

  /**
   * Calculates power for group A using simple multiplication
   *
   * @param valueA first operand
   * @param valueB second operand
   * @return calculated power
   */
  public double calculateGroupA(double valueA, double valueB) {
    return PowerFormula.SIMPLE_MULTIPLICATION.calculate(valueA, valueB);
  }

  /**
   * Calculates power for group B using dual multiplication and sum
   *
   * @param valueA first operand
   * @param valueB second operand
   * @param valueC third operand
   * @param valueD fourth operand
   * @return calculated power
   */
  public double calculateGroupB(double valueA, double valueB, double valueC, double valueD) {
    return PowerFormula.DUAL_MULTIPLICATION_SUM.calculate(valueA, valueB, valueC, valueD);
  }

  /**
   * Calculates power for group C using triple multiplication
   *
   * @param valueA first operand
   * @param valueB second operand
   * @param valueC third operand
   * @return calculated power
   */
  public double calculateGroupC(double valueA, double valueB, double valueC) {
    return PowerFormula.TRIPLE_MULTIPLICATION.calculate(valueA, valueB, valueC);
  }

  /**
   * Calculates power for group D with unit conversion
   *
   * @param valueA first operand
   * @param valueB second operand
   * @param valueC third operand
   * @return calculated power in kilowatts
   */
  public double calculateGroupD(double valueA, double valueB, double valueC) {
    double rawPower = calculateGroupC(valueA, valueB, valueC);
    return unitConverterComponent.convertWattsKilowatts(rawPower, false);
  }
}