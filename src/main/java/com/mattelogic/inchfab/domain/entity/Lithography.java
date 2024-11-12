package com.mattelogic.inchfab.domain.entity;

import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "lithography")
public record Lithography(
    @Id
    String id,
    String name,
    Double heaterPowerDraw,
    String heaterPowerDrawNote,
    String waferPerRun,
    String waferPerRunNote,
    String runsNeededPerJobStep,
    String runsNeededPerJobStepNote,
    Integer setupTakedownTime,
    String setupTakedownTimeNote,
    Double periodicCost,
    Double overheadPower,
    List<ProcessSetting> settings
) {

  public record ProcessSetting(
      String name,
      Double totalEffectiveLatent,
      List<Step> steps
  ) {

  }

  public record Step(
      String name,
      Integer process,
      Integer latent,
      Integer shared,
      Object effectiveProcess,  // Can be Double or String
      String effectiveProcessNote,
      Double effectiveLatent
  ) {

  }
}