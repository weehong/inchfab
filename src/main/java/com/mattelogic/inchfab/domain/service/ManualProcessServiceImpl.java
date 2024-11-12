package com.mattelogic.inchfab.domain.service;

import com.mattelogic.inchfab.common.dto.ApiResponseDto;
import com.mattelogic.inchfab.core.dtos.request.CostRequestDto;
import com.mattelogic.inchfab.core.dtos.response.ResultResponseDto;
import com.mattelogic.inchfab.core.model.ProjectStep;
import com.mattelogic.inchfab.domain.dto.request.ManualProcessRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public final class ManualProcessServiceImpl implements
    CalculateService<ProjectStep, CostRequestDto, ManualProcessRequestDto> {

  private static final String PROCESS_NAME = "Manual Process";

  @Override
  public ResultResponseDto calculate(ProjectStep step, CostRequestDto request) {
    return calculateProcessResults(new ManualProcessRequestDto(
        request.waferSize(),
        step.name(),
        step.lotCharge(),
        step.lotSize(),
        step.amount(),
        step.rate()
    ));
  }

  @Override
  public ApiResponseDto<ResultResponseDto> calculate(ManualProcessRequestDto request) {
    log.info("Processing Manual Process calculation request: {}", request);

    ResultResponseDto response = calculateProcessResults(request);
    log.info("Manual Process calculation completed successfully");

    return createSuccessResponse(response);
  }

  private ResultResponseDto calculateProcessResults(ManualProcessRequestDto request) {
    return ResultResponseDto.builder()
        .processName(PROCESS_NAME)
        .manualCost(calculateManualProcess(request))
        .build();
  }

  private ApiResponseDto<ResultResponseDto> createSuccessResponse(ResultResponseDto response) {
    return ApiResponseDto.<ResultResponseDto>builder()
        .status(200)
        .message("Calculate Manual Process result successfully")
        .data(response)
        .build();
  }

  private double calculateManualProcess(ManualProcessRequestDto request) {
    return Math.ceil(request.waferSize() / request.lotSize())
        * (request.lotCharge() + request.amount() * request.rate())
        / request.waferSize();
  }
}
