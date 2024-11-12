package com.mattelogic.inchfab.domain.model;

/**
 * Record for power calculation parameters
 */
public record PowerParameter(
    double processTime,
    double setupTakedownTime,
    double effectiveProcess,
    double effectiveLatent
) {

}