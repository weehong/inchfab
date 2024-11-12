package com.mattelogic.inchfab.domain.service;

import com.mattelogic.inchfab.base.repository.MetrologyInspectionRepository;
import com.mattelogic.inchfab.common.dto.ApiResponseDto;
import com.mattelogic.inchfab.core.dtos.request.CostRequestDto;
import com.mattelogic.inchfab.core.model.ProjectStep;
import com.mattelogic.inchfab.domain.dto.request.MetrologyInspectionRequestDto;
import com.mattelogic.inchfab.core.dtos.response.ResultResponseDto;
import com.mattelogic.inchfab.domain.enums.PowerFormula;
import com.mattelogic.inchfab.domain.enums.ProcessDefinition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public final class MetrologyInspectionServiceImpl implements
    CalculateService<ProjectStep, CostRequestDto, MetrologyInspectionRequestDto> {

  private static final String PROCESS_NAME = ProcessDefinition.METROLOGY_INSPECTION.getValue();

  private final MetrologyInspectionRepository metrologyInspectionRepository;

  @Override
  public ResultResponseDto calculate(ProjectStep step, CostRequestDto request) {
    return calculateProcessResults(new MetrologyInspectionRequestDto(
        step.name(),
        step.location(),
        step.timeWaferHour()
    ));
  }

  @Override
  public ApiResponseDto<ResultResponseDto> calculate(MetrologyInspectionRequestDto request) {
    log.info("Processing Metrology Inspection calculation request: {}", request);
    ResultResponseDto response = calculateProcessResults(request);
    log.info("Metrology Inspection calculation completed successfully");
    return createSuccessResponse(response);
  }

  private ResultResponseDto calculateProcessResults(MetrologyInspectionRequestDto request) {
    return ResultResponseDto.builder()
        .processName(PROCESS_NAME)
        .metrologyInspectionCost(calculateCost(request))
        .build();
  }

  private ApiResponseDto<ResultResponseDto> createSuccessResponse(ResultResponseDto response) {
    return ApiResponseDto.<ResultResponseDto>builder()
        .status(200)
        .message("Calculate Metrology Inspection result successfully")
        .data(response)
        .build();
  }

  private double calculateCost(MetrologyInspectionRequestDto request) {
    double price = metrologyInspectionRepository.findFieldByName(
        request.location(),
        ProcessDefinition.HOURLY_RATE.getValue()
    ).orElse(0.0);
    return PowerFormula.SIMPLE_MULTIPLICATION.calculate(
        price,
        request.timeWaferHour()
    );
  }

}