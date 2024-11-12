package com.mattelogic.inchfab.core.dtos.response;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;

public record ProjectResponseDto(
    Long id,
    Long companyId,
    String requesterId,
    String requesterName,
    String submitterId,
    String submitterName,
    String name,
    Integer waferSize,
    String rootFolderId,
    String projectFolderId,
    String uploadFiles,
    String substrateType,
    Double laborCost,
    Double electricalCost,
    Double totalLaborCost,
    Double totalPeriodicCost,
    Double totalTime,
    Double totalTimeCost,
    Double totalPowerCost,
    Double totalGasCost,
    Double totalTargetMaterialCost,
    Double totalWetEtchantCost,
    Double totalLithographyReagentCost,
    Double totalMetrologyInspectionCost,
    Double totalExternalProcessCost,
    Double totalManuallyInputProcessCost,
    Double totalSubstrateCost,
    Boolean status,
    JsonNode projectStep,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

  public ProjectResponseDto withoutProjectStep() {
    return new ProjectResponseDto(
        id,
        companyId,
        requesterId,
        requesterName,
        submitterId,
        submitterName,
        name,
        waferSize,
        rootFolderId,
        projectFolderId,
        uploadFiles,
        substrateType,
        laborCost,
        electricalCost,
        totalLaborCost,
        totalPeriodicCost,
        totalTime,
        totalTimeCost,
        totalPowerCost,
        totalGasCost,
        totalTargetMaterialCost,
        totalWetEtchantCost,
        totalLithographyReagentCost,
        totalMetrologyInspectionCost,
        totalExternalProcessCost,
        totalManuallyInputProcessCost,
        totalSubstrateCost,
        status,
        null,  // Set projectStep to null
        createdAt,
        updatedAt
    );
  }
}
