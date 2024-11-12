package com.mattelogic.inchfab.base.model;

import com.mattelogic.inchfab.base.enums.UnitConstant;
import com.mattelogic.inchfab.domain.enums.ProcessDefinition;

public record UnitConversion(String type, String metric) {

  public static UnitConversion of(ProcessDefinition parameter, UnitConstant constant) {
    return new UnitConversion(parameter.getValue(), constant.getValue());
  }
}