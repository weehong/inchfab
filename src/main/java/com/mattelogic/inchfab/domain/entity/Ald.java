package com.mattelogic.inchfab.domain.entity;

import jakarta.persistence.Id;
import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ald")
public record Ald(
    @Id
    String id,
    String name,
    Double gasOverhead,
    Integer waferPerRun,
    Integer runsNeededPerJobStep,
    Integer setupTakedownTime,
    Double periodicCost,
    Double overheadPower,
    Double heaterPowerDraw,
    List<Setting> settings
) {

  public record Setting(
      String name,
      String appliesTo,
      Integer latent,
      Double effectiveLatent,
      String effectiveProcess
  ) {

  }
}