package com.mattelogic.inchfab.domain.dto.response;

import java.util.Objects;
import java.util.stream.Stream;
import lombok.Builder;

@Builder
public record ResultResponseDto(
    String processName,
    Double laborTime,
    Double periodicCost,
    Double power,
    Double gas,
    Double targetMaterial,
    Double wetEtchant,
    Double lithographyReagent,
    Double metrologyInspectionCost,
    Double externalCost,
    Double manualCost,
    Double substrateCost,
    Double totalTime,
    Double totalCost
) {

  public static class ResultResponseDtoBuilder {

    public ResultResponseDto build() {
      double total = Stream.of(
              laborTime,
              periodicCost,
              power,
              gas,
              targetMaterial,
              wetEtchant,
              lithographyReagent,
              metrologyInspectionCost,
              externalCost,
              manualCost,
              substrateCost
          )
          .filter(Objects::nonNull)
          .mapToDouble(Double::doubleValue)
          .sum();

      return new ResultResponseDto(
          processName,
          laborTime,
          periodicCost,
          power,
          gas,
          targetMaterial,
          wetEtchant,
          lithographyReagent,
          metrologyInspectionCost,
          externalCost,
          manualCost,
          substrateCost,
          totalTime,
          total
      );
    }
  }
}