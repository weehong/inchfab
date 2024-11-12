package com.mattelogic.inchfab.domain.model;

/**
 * Record for holding power calculation parameters.
 * Used to simplify power consumption calculations by grouping related parameters.
 *
 * @param heaterPowerDraw Power draw of the heater
 * @param cleanTime Duration of cleaning phase
 * @param cleanTemperature Temperature during cleaning
 * @param depositionTemperature Temperature during deposition
 * @param depositionTime Duration of deposition phase
 */
public record PowerCalculationParams(
    double heaterPowerDraw,
    double cleanTime,
    double cleanTemperature,
    double depositionTemperature,
    double depositionTime
) {}
