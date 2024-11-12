package com.mattelogic.inchfab.domain.component;

import com.mattelogic.inchfab.base.enums.UnitConstant;
import com.mattelogic.inchfab.base.model.UnitConversion;
import com.mattelogic.inchfab.base.repository.ConstantRepository;
import com.mattelogic.inchfab.domain.enums.ConversionOperation;
import com.mattelogic.inchfab.domain.enums.ProcessDefinition;
import com.mattelogic.inchfab.domain.exception.UnitConversionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Component responsible for converting between different units of measurement.
 */
@Component
@RequiredArgsConstructor
public class UnitConverterComponent {

  private final ConstantRepository constantRepository;

  /**
   * Converts between seconds and minutes
   *
   * @param value      value to convert
   * @param isMultiply true if converting to minutes, false if converting to seconds
   * @return converted value
   * @throws UnitConversionException if conversion fails
   */
  public double convertSecondsMinutes(double value, boolean isMultiply) {
    validateInput(value);
    UnitConversion conversion = UnitConversion.of(
        ProcessDefinition.TIME,
        UnitConstant.S_PER_MIN
    );
    return convert(
        value,
        conversion,
        isMultiply
            ? ConversionOperation.MULTIPLY
            : ConversionOperation.DIVIDE
    );
  }

  /**
   * Converts between watts and kilowatts
   *
   * @param value      value to convert
   * @param isMultiply true if converting to kilowatts, false if converting to watts
   * @return converted value
   * @throws UnitConversionException if conversion fails
   */
  public double convertWattsKilowatts(double value, boolean isMultiply) {
    validateInput(value);
    UnitConversion conversion = UnitConversion.of(
        ProcessDefinition.POWER,
        UnitConstant.W_PER_KW
    );
    return convert(value, conversion,
        isMultiply ? ConversionOperation.MULTIPLY : ConversionOperation.DIVIDE);
  }

  /**
   * Converts between seconds and hours
   *
   * @param value      value to convert
   * @param isMultiply true if converting to hours, false if converting to seconds
   * @return converted value
   * @throws UnitConversionException if conversion fails
   */
  public double convertSecondsHours(double value, boolean isMultiply) {
    validateInput(value);
    UnitConversion conversion = UnitConversion.of(
        ProcessDefinition.TIME,
        UnitConstant.S_PER_H
    );
    return convert(value, conversion,
        isMultiply ? ConversionOperation.MULTIPLY : ConversionOperation.DIVIDE);
  }

  public double convertAngstromsNanometers(double value, boolean isMultiply) {
    validateInput(value);
    UnitConversion conversion = UnitConversion.of(
        ProcessDefinition.LENGTH,
        UnitConstant.A_PER_NM
    );
    return convert(value, conversion,
        isMultiply ? ConversionOperation.MULTIPLY : ConversionOperation.DIVIDE);
  }

  private double convert(double value, UnitConversion conversion, ConversionOperation operation) {
    double conversionFactor = getConversionFactor(conversion);
    validateConversionFactor(conversionFactor, conversion);

    return switch (operation) {
      case MULTIPLY -> value * conversionFactor;
      case DIVIDE -> value / conversionFactor;
    };
  }

  private double getConversionFactor(UnitConversion conversion) {
    return constantRepository
        .findValueByTypeAndMetric(conversion.type(), conversion.metric())
        .orElseThrow(() -> new UnitConversionException(
            "Conversion factor not found for type: %s and metric: %s"
                .formatted(conversion.type(), conversion.metric())
        ));
  }

  private void validateInput(double value) {
    if (Double.isNaN(value) || Double.isInfinite(value)) {
      throw new UnitConversionException("Invalid input value: " + value);
    }
  }

  private void validateConversionFactor(double factor, UnitConversion conversion) {
    if (factor == 0.0) {
      throw new UnitConversionException(
          "Zero conversion factor found for type: %s and metric: %s"
              .formatted(conversion.type(), conversion.metric())
      );
    }
  }
}