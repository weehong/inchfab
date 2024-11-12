package com.mattelogic.inchfab.core.dtos.response;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
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
    BigDecimal totalLaborCost,
    BigDecimal totalPeriodicCost,
    BigDecimal totalTime,
    BigDecimal totalTimeCost,
    BigDecimal totalPowerCost,
    BigDecimal totalGasCost,
    BigDecimal totalTargetMaterialCost,
    BigDecimal totalWetEtchantCost,
    BigDecimal totalLithographyReagentCost,
    BigDecimal totalMetrologyInspectionCost,
    BigDecimal totalExternalProcessCost,
    BigDecimal totalManuallyInputProcessCost,
    BigDecimal totalSubstrateCost,
    Boolean status,
    JsonNode projectStep,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

}
