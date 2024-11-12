package com.mattelogic.inchfab.base.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "wet-chemical")
public record WetChemical(
    @Id
    String id,
    String name,
    String etchedMaterial,
    String etchant,
    Integer lotSize,
    Double lotVolume,
    Double etchRate,
    Double etchTemp,
    Double volume,
    String vendor,
    Double cost,
    Double costPerVolume,
    Double costPerRun
) {

}