package com.mattelogic.inchfab.domain.entity;

import jakarta.persistence.Id;
import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "magnetron-sputtering")
public record MagnetronSputtering(
    @Id
    String id,
    String name,
    Double gasOverhead,
    Double waferPerRun,
    Integer runsNeededPerJobStep,
    Integer setupTakedownTime,
    Double periodicCost,
    Double overheadPower,
    List<Setting> settings
) {

  public record Setting(
      String name,
      Double totalEffectiveProcess,
      Double totalEffectiveLatent,
      List<Process> processes
  ) {

  }

  public record Process(
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