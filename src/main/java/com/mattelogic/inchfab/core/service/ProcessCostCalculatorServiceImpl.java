package com.mattelogic.inchfab.core.service;

import com.mattelogic.inchfab.common.dto.ApiResponseDto;
import com.mattelogic.inchfab.core.dtos.request.CostRequestDto;
import com.mattelogic.inchfab.core.dtos.response.ResultResponseDto;
import com.mattelogic.inchfab.core.exception.ProcessingStepException;
import com.mattelogic.inchfab.domain.repository.EssentialRepository;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessCostCalculatorServiceImpl {

  private final AsyncProcessingService asyncProcessingService;
  private final EssentialRepository essentialRepository;

  @Transactional(readOnly = true)
  public ApiResponseDto<ResultResponseDto> calculate(CostRequestDto request) throws Throwable {
    log.info("Starting cost calculation for {} steps", request.projectSteps().size());
    return executeCalculation(request, this::createSuccessResponse);
  }

  @Transactional(readOnly = true)
  public ResultResponseDto calculatePriceTotalResult(CostRequestDto request) throws Throwable {
    log.info("Starting price calculation result for {} steps", request.projectSteps().size());
    return executeCalculation(request, result -> result);
  }

  private <T> T executeCalculation(CostRequestDto request,
      Function<ResultResponseDto, T> resultHandler) throws Throwable {
    try {
      List<ResultResponseDto> results = calculateStepsInParallel(request);
      ResultResponseDto aggregatedResult = aggregateResults(results, request.waferSize());
      return resultHandler.apply(aggregatedResult);
    } catch (CompletionException e) {
      log.error("Error during cost calculation", e);
      throw ExceptionUtils.getRootCause(e);
    }
  }

  private List<ResultResponseDto> calculateStepsInParallel(CostRequestDto request) {
    List<CompletableFuture<ResultResponseDto>> futures = createStepFutures(request);
    return waitForAllFutures(futures);
  }

  private List<CompletableFuture<ResultResponseDto>> createStepFutures(CostRequestDto request) {
    return request.projectSteps().stream()
        .map(step -> asyncProcessingService.processStepAsync(step, request)
            .exceptionally(throwable -> handleStepProcessingError(throwable, step.sequenceId())))
        .toList();
  }

  private ResultResponseDto handleStepProcessingError(Throwable throwable, Long stepId) {
    Throwable rootCause = ExceptionUtils.getRootCause(throwable);
    String errorMessage = String.format("Failed to process step %d: %s",
        stepId,
        rootCause.getMessage());
    throw new ProcessingStepException(errorMessage, rootCause);
  }

  private List<ResultResponseDto> waitForAllFutures(
      List<CompletableFuture<ResultResponseDto>> futures) {
    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
        .thenApply(v -> futures.stream()
            .map(CompletableFuture::join)
            .filter(Objects::nonNull)
            .toList())
        .join();
  }

  private ResultResponseDto aggregateResults(List<ResultResponseDto> results, int waferSize) {
    double laborCost = essentialRepository.findLaborCost();
    double electricityCost = essentialRepository.findElectricityCost();

    List<ResultResponseDto> allUnitCosts = results.stream()
        .filter(result -> result.unitTotalCosts() != null)
        .flatMap(result -> result.unitTotalCosts().stream())
        .toList();

    return ResultResponseDto.builder()
        .processName("Total")
        .laborTime(calculateLaborCost(results, laborCost))
        .periodicCost(sumValue(results, ResultResponseDto::periodicCost))
        .power(calculatePowerCost(results, electricityCost))
        .gas(sumValue(results, ResultResponseDto::gas))
        .targetMaterial(sumValue(results, ResultResponseDto::targetMaterial))
        .wetEtchant(sumValue(results, ResultResponseDto::wetEtchant))
        .lithographyReagent(sumValue(results, ResultResponseDto::lithographyReagent))
        .metrologyInspectionCost(calculateWaferSizeDependentCost(
            results, ResultResponseDto::metrologyInspectionCost, waferSize))
        .externalCost(calculateWaferSizeDependentCost(
            results, ResultResponseDto::externalCost, waferSize))
        .manualCost(sumValue(results, ResultResponseDto::manualCost))
        .substrateCost(sumValue(results, ResultResponseDto::substrateCost))
        .totalTime(sumValue(results, ResultResponseDto::totalTime))
        .unitTotalCosts(allUnitCosts) // Add all unit costs to the final result
        .build();
  }

  private double calculateLaborCost(List<ResultResponseDto> results, double laborCost) {
    return sumValue(results, ResultResponseDto::laborTime) * laborCost;
  }

  private double calculatePowerCost(List<ResultResponseDto> results, double electricityCost) {
    return sumValue(results, ResultResponseDto::power) * electricityCost;
  }

  private double calculateWaferSizeDependentCost(
      List<ResultResponseDto> results,
      Function<ResultResponseDto, Double> valueExtractor,
      int waferSize) {
    return sumValue(results, valueExtractor) * waferSize;
  }

  private Double sumValue(List<ResultResponseDto> results,
      Function<ResultResponseDto, Double> valueExtractor) {
    return results.stream()
        .map(valueExtractor)
        .filter(Objects::nonNull)
        .mapToDouble(Double::doubleValue)
        .sum();
  }

  private ApiResponseDto<ResultResponseDto> createSuccessResponse(ResultResponseDto response) {
    return ApiResponseDto.<ResultResponseDto>builder()
        .status(200)
        .message("Calculate cost result successfully")
        .data(response)
        .build();
  }
}