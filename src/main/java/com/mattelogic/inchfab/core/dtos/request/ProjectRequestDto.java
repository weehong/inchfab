package com.mattelogic.inchfab.core.dtos.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record ProjectRequestDto(

    @Column(columnDefinition = "json")
    List<JsonNode> projectSteps,

    @NotNull(message = "Company ID is required")
    Long companyId,

    @NotBlank(message = "Requester ID is cannot be blank.")
    @NotNull(message = "Requester ID is cannot be null.")
    String requesterId,

    @NotBlank(message = "Requester Name is cannot be blank.")
    @NotNull(message = "Requester Name is cannot be null.")
    String requesterName,

    @NotBlank(message = "Submitter ID is cannot be blank.")
    @NotNull(message = "Submitter ID is cannot be null.")
    String submitterId,

    @NotBlank(message = "Submitter Name is cannot be blank.")
    @NotNull(message = "Submitter Name is cannot be null.")
    String submitterName,

    @NotBlank(message = "Name is required")
    String name,

    String substrateType,
    Integer waferSize,
    BigDecimal totalTime,
    BigDecimal totalLaborCost,
    BigDecimal totalPeriodicCost,
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
    JsonNode projectStep
) {

}
