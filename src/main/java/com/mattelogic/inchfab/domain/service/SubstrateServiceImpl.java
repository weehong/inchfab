package com.mattelogic.inchfab.domain.service;

import com.mattelogic.inchfab.common.dto.ApiResponseDto;
import com.mattelogic.inchfab.core.dtos.request.CostRequestDto;
import com.mattelogic.inchfab.core.model.ProjectStep;
import com.mattelogic.inchfab.domain.dto.request.SubstrateRequestDto;
import com.mattelogic.inchfab.core.dtos.response.ResultResponseDto;
import com.mattelogic.inchfab.domain.repository.SubstrateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public final class SubstrateServiceImpl implements
    CalculateService<ProjectStep, CostRequestDto, SubstrateRequestDto> {

  private static final String PROCESS_NAME = "Substrate";
  private final SubstrateRepository substrateRepository;

  @Override
  public ResultResponseDto calculate(ProjectStep step, CostRequestDto request) {
    return calculateProcessResults(new SubstrateRequestDto(
        request.waferSize(),
        step.name()
    ));
  }

  @Override
  public ApiResponseDto<ResultResponseDto> calculate(SubstrateRequestDto request) {
    log.info("Processing Substrate calculation request: {}", request);

    ResultResponseDto response = calculateProcessResults(request);
    log.info("Substrate calculation completed successfully");

    return createSuccessResponse(response);
  }

  private ResultResponseDto calculateProcessResults(SubstrateRequestDto request) {
    return ResultResponseDto.builder()
        .processName(PROCESS_NAME)
        .substrateCost(calculateSubstrate(request))
        .build();
  }

  private ApiResponseDto<ResultResponseDto> createSuccessResponse(ResultResponseDto response) {
    return ApiResponseDto.<ResultResponseDto>builder()
        .status(200)
        .message("Calculate Substrate result successfully")
        .data(response)
        .build();
  }

  private double calculateSubstrate(SubstrateRequestDto request) {
    return substrateRepository.findNearestPriceForMethod(
        request.name(),
        request.waferSize()
    ).orElse(0.0);
  }
}
