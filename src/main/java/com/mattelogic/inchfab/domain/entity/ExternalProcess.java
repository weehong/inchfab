package com.mattelogic.inchfab.domain.entity;

import jakarta.persistence.Id;
import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "external-process")
public record ExternalProcess(
    @Id
    String id,
    String name,
    List<PricingTier> pricing,
    String pricingStructure,
    Double setupCost,
    Double lotCharge,
    Integer lotSize,
    Boolean usesAmount,
    Double amountRate,
    String amountUnit
) {

  public record PricingTier(
      Integer quantity,
      Double unitCost
  ) {

  }
}