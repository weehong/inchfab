package com.mattelogic.inchfab.core.dtos.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    String rootFolderId,
    String projectFolderId,
    String uploadFile,
    String substrateType,
    Integer waferSize,
    Double totalTime,
    Double totalLaborCost,
    Double totalPeriodicCost,
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
    JsonNode projectStep
) {

}
