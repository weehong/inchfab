package com.mattelogic.inchfab.base.util;

import com.mattelogic.inchfab.base.enums.UnitConstant;
import com.mattelogic.inchfab.base.exception.UnitConstantException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class for UnitConstant operations.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UnitConstantUtil {

  /**
   * Validates if two unit constants are compatible for conversion.
   *
   * @param from source unit constant
   * @param to   target unit constant
   * @throws UnitConstantException if units are incompatible
   */
  public static void validateCompatibleUnits(UnitConstant from, UnitConstant to) {
    if (from.getCategory() != to.getCategory()) {
      throw new UnitConstantException(
          "Incompatible unit conversion: %s to %s"
              .formatted(from.getValue(), to.getValue())
      );
    }
  }
}