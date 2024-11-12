package com.mattelogic.inchfab.core.dtos.request;

import com.mattelogic.inchfab.core.model.ProjectStep;
import java.util.List;

public record CostRequestDto(
    String substrateType,
    Integer waferSize,
    List<ProjectStep> projectSteps
) {

}