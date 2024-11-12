package com.mattelogic.inchfab.domain.service;

import com.mattelogic.inchfab.common.dto.ApiResponseDto;
import com.mattelogic.inchfab.core.dtos.request.CostRequestDto;
import com.mattelogic.inchfab.core.dtos.response.ResultResponseDto;
import com.mattelogic.inchfab.core.model.ProjectStep;
import com.mattelogic.inchfab.domain.dto.request.ExternalProcessRequestDto;
import com.mattelogic.inchfab.domain.enums.ProcessDefinition;
import com.mattelogic.inchfab.domain.repository.ExternalProcessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public final class ExternalProcessServiceImpl implements
    CalculateService<ProjectStep, CostRequestDto, ExternalProcessRequestDto> {

  private static final String PROCESS_NAME = "External Process";

  private final ExternalProcessRepository externalProcessRepository;

  @Override
  public ResultResponseDto calculate(ProjectStep step, CostRequestDto request) {
    return calculateProcessResults(new ExternalProcessRequestDto(
        request.waferSize(),
        step.name(),
        step.amount()
    ));
  }

  @Override
  public ApiResponseDto<ResultResponseDto> calculate(ExternalProcessRequestDto request) {
    log.info("Processing External Process calculation request: {}", request);

    ResultResponseDto response = calculateProcessResults(request);
    log.info("External Process calculation completed successfully");

    return createSuccessResponse(response);
  }

  private ResultResponseDto calculateProcessResults(ExternalProcessRequestDto request) {
    return ResultResponseDto.builder()
        .processName(PROCESS_NAME)
        .externalCost(calculateExternalCost(request))
        .build();
  }

  private ApiResponseDto<ResultResponseDto> createSuccessResponse(ResultResponseDto response) {
    return ApiResponseDto.<ResultResponseDto>builder()
        .status(200)
        .message("Calculate External Process result successfully")
        .data(response)
        .build();
  }

  private double calculateExternalCost(ExternalProcessRequestDto request) {
    double setupCost = findExternalProcessValue(request.name(),
        ProcessDefinition.SETUP_COST.getValue());
    double lotCharge = findExternalProcessValue(request.name(),
        ProcessDefinition.LOT_CHARGE.getValue());
    double lotSize = findExternalProcessValue(request.name(),
        ProcessDefinition.LOT_SIZE.getValue());
    double amountRate = findExternalProcessValue(request.name(),
        ProcessDefinition.AMOUNT_RATE.getValue());
    double sum =
        Math.ceil(request.waferSize() / lotSize) * lotCharge + amountRate * request.amount();
    return (setupCost + sum) / request.waferSize();
  }

  private double findExternalProcessValue(String name, String fieldName) {
    return externalProcessRepository.findFieldByName(name, fieldName)
        .orElse(0.0);
  }
}
