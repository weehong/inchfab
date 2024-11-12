package com.mattelogic.inchfab.domain.entity;

import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "rie")
public record Rie(
    @Id
    String id,
    String name,
    BigDecimal gasOverhead,
    Integer waferPerRun,
    Integer runsNeededPerJobStep,
    Integer setupTakedownTime,
    BigDecimal periodicCost,
    BigDecimal overheadPower,
    BigDecimal etchRate,
    Integer heBspPressure,
    List<Setting> settings
) {

  public record Setting(
      String name,
      BigDecimal totalEffectiveProcess,
      BigDecimal totalEffectiveLatent,
      List<Process> processes
  ) {

    public record Process(
        String name,
        Integer process,
        Integer latent,
        BigDecimal efficiency,
        Integer shared,
        BigDecimal effectiveProcess,
        BigDecimal effectiveLatent,
        Integer attenuation
    ) {

    }
  }
}