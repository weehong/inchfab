package com.mattelogic.inchfab.base.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.mattelogic.inchfab.base.exception.GasConstantException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GasConstant {
  C4F8("c4f8", "Octafluorocyclobutane", Category.FLUORINE_BASED),
  SF6("sf6", "Sulfur Hexafluoride", Category.FLUORINE_BASED),
  CF4("cf4", "Carbon Tetrafluoride", Category.FLUORINE_BASED),
  CHF3("chf3", "Trifluoromethane", Category.FLUORINE_BASED),

  AR("ar", "Argon", Category.INERT),
  N2("n2", "Nitrogen", Category.INERT),
  HE("he", "Helium", Category.INERT),

  SIH4("sih4", "Silane", Category.SILICON_BASED),
  SIH4HE("sih4+he", "Silane with Helium", Category.SILICON_BASED),
  SIH2CL2("sih2cl2", "Dichlorosilane", Category.SILICON_BASED),

  CL2("cl2", "Chlorine", Category.CHLORINE_BASED),
  BCL3("bcl3", "Boron Trichloride", Category.CHLORINE_BASED),

  O2("o2", "Oxygen", Category.OXIDIZING),
  N2O("n2o", "Nitrous Oxide", Category.OXIDIZING),

  NH3("nh3", "Ammonia", Category.NITROGEN_CONTAINING),

  TEOS("teos", "Tetraethyl Orthosilicate", Category.PRECURSOR),
  TMA("tma", "Trimethylaluminum", Category.PRECURSOR),
  TDMAT("tdmat", "Tetrakis(dimethylamino)titanium", Category.PRECURSOR),
  H2O("h2o", "Water Vapor", Category.PRECURSOR);

  private static final Map<String, GasConstant> VALUE_MAP = Stream.of(values())
      .collect(Collectors.toMap(
          constant -> constant.value.toLowerCase(),
          constant -> constant
      ));

  private final String value;
  private final String chemicalName;
  private final Category category;

  @JsonCreator
  public static GasConstant fromValue(String value) {
    return Optional.ofNullable(value)
        .map(String::toLowerCase)
        .map(VALUE_MAP::get)
        .orElseThrow(() -> new GasConstantException("Invalid gas constant: " + value));
  }

  public static Set<GasConstant> getByCategory(Category category) {
    return Arrays.stream(values())
        .filter(gas -> gas.category == category)
        .collect(Collectors.toSet());
  }

  private static boolean isIncompatiblePair(GasConstant gas1, GasConstant gas2) {
    return (gas1.category == Category.OXIDIZING && gas2.category == Category.CHLORINE_BASED) ||
        (gas1.category == Category.CHLORINE_BASED && gas2.category == Category.OXIDIZING) ||
        (gas1 == H2O && gas2.category == Category.CHLORINE_BASED) ||
        (gas2 == H2O && gas1.category == Category.CHLORINE_BASED);
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  public boolean isCompatibleWith(GasConstant other) {
    if (this.category == Category.INERT || other.category == Category.INERT) {
      return true;
    }

    if (this.category == other.category) {
      return true;
    }

    return !isIncompatiblePair(this, other);
  }

  @Getter
  public enum Category {
    FLUORINE_BASED("Fluorine-based Gases"),
    INERT("Inert Gases"),
    SILICON_BASED("Silicon-based Gases"),
    CHLORINE_BASED("Chlorine-based Gases"),
    OXIDIZING("Oxidizing Gases"),
    NITROGEN_CONTAINING("Nitrogen-containing Gases"),
    PRECURSOR("Precursor Gases");

    private final String description;

    Category(String description) {
      this.description = description;
    }
  }
}