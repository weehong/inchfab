package com.mattelogic.inchfab.domain.model;

import java.util.function.Supplier;

/**
 * Record for storing process step parameters to reduce code duplication
 */
public record ProcessStep(
    String processType,
    Supplier<Double> timeCalculator,
    String parameterValue
) {

}