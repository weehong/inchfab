package com.mattelogic.inchfab.core.service;

import com.mattelogic.inchfab.core.dtos.request.CostRequestDto;
import com.mattelogic.inchfab.core.dtos.response.ResultResponseDto;
import com.mattelogic.inchfab.core.model.ProjectStep;
import com.mattelogic.inchfab.domain.enums.ProcessDefinition;
import com.mattelogic.inchfab.domain.service.ProcessServiceRegistryImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AsyncProcessingService {

  private final ProcessServiceRegistryImpl processServiceRegistry;

  @Async("processTaskExecutor")
  @Transactional(readOnly = true)
  public CompletableFuture<ResultResponseDto> processStepAsync(ProjectStep step,
      CostRequestDto request) {
    log.info("Starting async processing for step: {}", step.sequenceId());
    try {
      ResultResponseDto result = processStep(step, request);

      List<ResultResponseDto> unitCosts = new ArrayList<>();
      unitCosts.add(result);

      ResultResponseDto resultWithUnits = ResultResponseDto.builder()
          .processName(result.processName())
          .laborTime(result.laborTime())
          .periodicCost(result.periodicCost())
          .power(result.power())
          .gas(result.gas())
          .targetMaterial(result.targetMaterial())
          .wetEtchant(result.wetEtchant())
          .lithographyReagent(result.lithographyReagent())
          .metrologyInspectionCost(result.metrologyInspectionCost())
          .externalCost(result.externalCost())
          .manualCost(result.manualCost())
          .substrateCost(result.substrateCost())
          .totalTime(result.totalTime())
          .unitTotalCosts(unitCosts)
          .build();

      return CompletableFuture.completedFuture(resultWithUnits);
    } catch (Exception e) {
      log.error("Error processing step {}: {}", step.sequenceId(), e.getMessage(), e);
      return CompletableFuture.failedFuture(e);
    }
  }

  private ResultResponseDto processStep(ProjectStep step, CostRequestDto request) {
    log.debug("Processing step {} with parameters: {}", step.sequenceId(), step);
    ProcessDefinition processDefinition = ProcessDefinition.fromValue(step.processType());
    return processServiceRegistry.calculate(processDefinition, step, request);
  }
}