package com.mattelogic.inchfab.domain.entity;

import jakarta.persistence.Id;
import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "wet-process")
public record WetProcess(
    @Id
    String id,
    String name,
    Double heaterPowerDraw,
    String waferPerRun,
    String runsNeededPerJobStep,
    Integer setupTakedownTime,
    Double periodicCost,
    Double overheadPower,
    List<WetProcessSettingDto> settings
) {

  public record WetProcessSettingDto(
      String name,
      String appliesTo,
      Integer latent,
      Double effectiveLatent,
      String effectiveProcess,
      String effectiveProcessNote
  ) {

  }
}