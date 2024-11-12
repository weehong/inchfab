package com.mattelogic.inchfab.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Comprehensive enum representing both process types and parameters used in semiconductor
 * manufacturing. Each definition maps to a specific string value for JSON
 * serialization/deserialization and is categorized by its role in the manufacturing process.
 */
public enum ProcessDefinition {
  // Process Types - Etching
  DRIE("drie", Category.PROCESS_TYPE),
  RIE("rie", Category.PROCESS_TYPE),
  ETCH("etch", Category.PROCESS_TYPE),

  // Process Types - Deposition
  ALD("ald", Category.PROCESS_TYPE),
  LP_CVD("lp_cvd", Category.PROCESS_TYPE),
  MAGNETRON_SPUTTER("magnetron_sputtering", Category.PROCESS_TYPE),
  ICP_CVD("icp_cvd", Category.PROCESS_TYPE),
  SPUTTER("sputter", Category.PROCESS_TYPE),
  PRESPUTTER("presputter", Category.PARAMETER),
  DEPOSITION("deposition", Category.PROCESS_TYPE),

  // Process Types - Material Specific
  AL2O3("al2o3", Category.PROCESS_TYPE),
  TIO2("tio2", Category.PROCESS_TYPE),

  // Process Types - Wet Processing
  WET_CHEMICAL("wet_chemical", Category.PROCESS_TYPE),
  WET_PROCESS("wet_process", Category.PROCESS_TYPE),

  // Process Types - Other
  LITHOGRAPHY("lithography", Category.PROCESS_TYPE),
  METROLOGY_INSPECTION("metrology_inspection", Category.PROCESS_TYPE),
  EXTERNAL_PROCESS("external_process", Category.PROCESS_TYPE),
  CUSTOM_PROCESS("custom_process", Category.PROCESS_TYPE),
  HEATER("heater", Category.PROCESS_TYPE),

  // Process Types - ALD Specific Steps
  FLOW_PRECURSOR_A("flow_precursor_a", Category.PROCESS_TYPE),
  PURGE_A("purge_a", Category.PROCESS_TYPE),
  FLOW_PRECURSOR_B("flow_precursor_b", Category.PROCESS_TYPE),
  PURGE_B("purge_b", Category.PROCESS_TYPE),

  // Parameters - Basic Measurements
  MASS("mass", Category.PARAMETER),
  TIME("time", Category.PARAMETER),
  POWER("power", Category.PARAMETER),
  LENGTH("length", Category.PARAMETER),
  VOLUME("volume", Category.PARAMETER),
  TEMPERATURE("temperature", Category.PARAMETER),
  PRESSURE("pressure", Category.PARAMETER),
  PRICE("price", Category.PARAMETER),

  // Parameters - Time Related
  STEP_TIME("stepTime", Category.PARAMETER),
  SETUP_TAKEDOWN("setupTakedown", Category.PARAMETER),
  SETUP_TAKEDOWN_TIME("setupTakedownTime", Category.PARAMETER),
  PROCESS_TIME("processTime", Category.PARAMETER),
  LABOR_TIME("laborTime", Category.PARAMETER),
  CLEAN_TIME("cleanTime", Category.PARAMETER),
  DRYING_RUN("dryingTime", Category.PARAMETER),
  ALIGN_EXPOSURE_TIME("align_exposure_system", Category.PARAMETER),
  LIFETIME_HOURS("lifetimeHours", Category.PARAMETER),
  LIFETIME_SECONDS("lifetimeSeconds", Category.PARAMETER),

  // Parameters - Process Specific
  ETCH_RATE("etchRate", Category.PARAMETER),
  ETCH_TEMP("etchTemp", Category.PARAMETER),
  BASE_PROCESS_RATE("baseProcessRate", Category.PARAMETER),
  HEATER_POWER_DRAW("heaterPowerDraw", Category.PARAMETER),
  RAMP_UP_DOWN_TEMPERATURE("rampUpDownTemperature", Category.PARAMETER),

  // Parameters - Resource Usage
  GAS_OVERHEAD("gasOverhead", Category.PARAMETER),
  PERIODIC_COST("periodicCost", Category.PARAMETER),
  OVERHEAD_POWER("overheadPower", Category.PARAMETER),
  OTHER_POWER("other_power", Category.PARAMETER),
  VOLUME_USED_PER_RUN("volumeUsedPerRun", Category.PARAMETER),

  // Parameters - Process Control
  MATCHING("matching", Category.PARAMETER),
  SUBSTRATE("substrate", Category.PARAMETER),
  CLEAN("clean", Category.PARAMETER),
  RUNS_NEEDED_PER_STEP("runsNeededPerStep", Category.PARAMETER),
  LOT_SIZE("lotSize", Category.PARAMETER),
  RUN_SIZE("runSize", Category.PARAMETER),
  COST_PER_RUN("costPerRun", Category.PARAMETER),
  LOT_VOLUME("lotVolume", Category.PARAMETER),
  VENDOR("vendor", Category.PARAMETER),
  COST("cost", Category.PARAMETER),
  COST_PER_VOLUME("costPerVolume", Category.PARAMETER),
  SETUP_COST("setupCost", Category.PARAMETER),
  LOT_CHARGE("lotCharge", Category.PARAMETER),
  AMOUNT_RATE("amountRate", Category.PARAMETER),

  // Parameters - Materials and Components
  MATERIALS("materials", Category.PARAMETER),
  SOFT_BAKE("softbake", Category.PARAMETER),
  POST_BAKE("postbake", Category.PARAMETER),
  HARD_BAKE("hardbake", Category.PARAMETER),
  HOT_PLATE("hotplate", Category.PARAMETER),
  OVEN("oven", Category.PARAMETER),
  HMDS("hmds", Category.PARAMETER),
  PHOTORESIST("photoresist", Category.PARAMETER),
  HMDS_PRE_SOAK("hmdsPreSoak", Category.PARAMETER),
  SPIN_COAT("spin_coat", Category.PARAMETER),
  REHYDRATION("rehydration", Category.PARAMETER),

  // Parameters - Alignment and Exposure
  CONTACT_ALIGNMENT("contactAlignment", Category.PARAMETER),
  CONTACT_EXPOSURE("contactExposure", Category.PARAMETER),
  MASKLESS_ALIGNMENT("masklessAlignment", Category.PARAMETER),
  MASKLESS_EXPOSURE("masklessExposure", Category.PARAMETER),
  DEVELOPER("developer", Category.PARAMETER),
  DRYING("drying", Category.PARAMETER),
  HOURLY_RATE("hourlyRate", Category.PARAMETER),
  WAFER_PER_RUN("waferPerRun", Category.PARAMETER);


  private final String value;
  private final Category category;

  /**
   * Constructs a ProcessDefinition with the specified string value and category.
   *
   * @param value    the string representation of the process definition
   * @param category the category of the process definition
   */
  ProcessDefinition(String value, Category category) {
    this.value = value;
    this.category = category;
  }

  /**
   * Creates a ProcessDefinition from a string value. Used for JSON deserialization.
   *
   * @param value the string value to convert
   * @return the corresponding ProcessDefinition enum constant
   * @throws IllegalArgumentException if no matching definition is found
   */
  @JsonCreator
  public static ProcessDefinition fromValue(String value) {
    for (ProcessDefinition definition : ProcessDefinition.values()) {
      if (definition.value.equalsIgnoreCase(value)) {
        return definition;
      }
    }
    throw new IllegalArgumentException("Invalid process definition: " + value);
  }

  /**
   * Gets the string value of the process definition. Used for JSON serialization.
   *
   * @return the string representation of the process definition
   */
  @JsonValue
  public String getValue() {
    return value;
  }

  /**
   * Gets the category of the process definition.
   *
   * @return the category (PROCESS_TYPE or PARAMETER)
   */
  public Category getCategory() {
    return category;
  }

  /**
   * Checks if this definition is a process type.
   *
   * @return true if this is a process type, false otherwise
   */
  public boolean isProcessType() {
    return category == Category.PROCESS_TYPE;
  }

  /**
   * Checks if this definition is a parameter.
   *
   * @return true if this is a parameter, false otherwise
   */
  public boolean isParameter() {
    return category == Category.PARAMETER;
  }

  /**
   * Enum representing the category of the process definition.
   */
  public enum Category {
    PROCESS_TYPE,
    PARAMETER
  }
}