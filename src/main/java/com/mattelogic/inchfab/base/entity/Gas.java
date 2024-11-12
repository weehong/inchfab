package com.mattelogic.inchfab.base.entity;

import java.math.BigDecimal;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "gas")
public record Gas(
    @Id
    String id,
    String name,
    BigDecimal price,
    BigDecimal effectiveCapacity,
    Double grade,
    BigDecimal density,
    Double fillQuantityLb,
    Integer fillQuantityScf,
    BigDecimal totalPrice
) {

}