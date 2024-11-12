package com.mattelogic.inchfab.base.entity;

import jakarta.persistence.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "target")
public record Target(
    @Id
    String id,
    String name,
    String material,
    Double purity,
    Double diameter,
    Double thickness,
    Double volume,
    Double density,
    Double weight,
    Object price,  // Can be Double or String ("P.O.R.")
    Double lifetimeHours,
    Double lifetimeSeconds
) {

}
