package com.mattelogic.inchfab.domain.entity;

import jakarta.persistence.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "essential")
public record Essential(
    @Id
    String id,
    Double laborCost,
    Double electricalCost
) {

}

