package com.mattelogic.inchfab.base.enums;

import static java.util.stream.Collectors.toMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.mattelogic.inchfab.base.exception.UnitConstantException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UnitConstant {

  // Mass conversion constants
  G_PER_LB("g/lb", "Grams per pound", Category.MASS),

  // Time conversion constants
  MIN_PER_H("min/h", "Minutes per hour", Category.TIME),
  S_PER_MIN("s/min", "Seconds per minute", Category.TIME),
  S_PER_H("s/h", "Seconds per hour", Category.TIME),

  // Power conversion constants
  W_PER_KW("W/kW", "Watts per kilowatt", Category.POWER),

  // Electric conversion constants
  A_PER_NM("A/nm", "Amperes per nanometer", Category.ELECTRIC),

  // Length conversion constants
  NM_PER_UM("nm/μm", "Nanometers per micrometer", Category.LENGTH),

  // Volume conversion constants
  CC_PER_L("cc/L", "Cubic centimeters per liter", Category.VOLUME),
  ML_PER_L("mL/L", "Milliliters per liter", Category.VOLUME),
  CC_PER_CF("cc/cf", "Cubic centimeters per cubic foot", Category.VOLUME);

  private static final Map<String, UnitConstant> VALUE_MAP = Stream.of(values())
      .collect(toMap(
          constant -> constant.value.toLowerCase(),
          constant -> constant
      ));

  private final String value;
  private final String description;
  private final Category category;

  @JsonCreator
  public static UnitConstant fromValue(String value) {
    return Optional.ofNullable(value)
        .map(String::toLowerCase)
        .map(VALUE_MAP::get)
        .orElseThrow(() -> new UnitConstantException("Invalid unit conversion: " + value));
  }

  public static UnitConstant[] getByCategory(Category category) {
    return Stream.of(values())
        .filter(constant -> constant.isCategory(category))
        .toArray(UnitConstant[]::new);
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  public boolean isCategory(Category category) {
    return this.category == category;
  }

  public enum Category {
    MASS,
    TIME,
    POWER,
    ELECTRIC,
    LENGTH,
    VOLUME
  }
}