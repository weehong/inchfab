package com.mattelogic.inchfab.base.entity;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "metrology-inspection")
public record MetrologyInspection(
    String name,
    String facility,
    Double hourlyRate,
    Double minuteRate
) {

}
