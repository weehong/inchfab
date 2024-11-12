package com.mattelogic.inchfab.core.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
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
    List<ResultResponseDto> unitTotalCosts
) {

  public static class ResultResponseDtoBuilder {
    private Double calculateTotalCost() {
      return nullToZero(periodicCost) +
          nullToZero(power) +
          nullToZero(gas) +
          nullToZero(targetMaterial) +
          nullToZero(wetEtchant) +
          nullToZero(lithographyReagent) +
          nullToZero(metrologyInspectionCost) +
          nullToZero(externalCost) +
          nullToZero(manualCost) +
          nullToZero(substrateCost);
    }

    private Double nullToZero(Double value) {
      return value != null ? value : 0.0;
    }

    public ResultResponseDto build() {
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
          unitTotalCosts
      );
    }
  }
}