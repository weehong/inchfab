package com.mattelogic.inchfab.domain.entity;

import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "drie")
public record Drie(
    @Id
    String id,
    String name,
    Double gasOverhead,
    Integer waferPerRun,
    Integer runsNeededPerJobStep,
    Integer setupTakedownTime,
    Double periodicCost,
    Double overheadPower,
    Double etchRate,
    Integer heBspPressure,
    List<SettingDto> settings
) {

  public record SettingDto(
      String name,
      Double totalEffectiveProcess,
      Double totalEffectiveLatent,
      List<ProcessDto> processes
  ) {

  }

  public record ProcessDto(
      String name,
      Integer process,
      Integer latent,
      Double attenuation,
      Double efficiency,
      Integer shared,
      Double effectiveProcess,
      Double effectiveLatent
  ) {

  }
}