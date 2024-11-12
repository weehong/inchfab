package com.mattelogic.inchfab.domain.service;

import com.mattelogic.inchfab.base.repository.WetChemicalRepository;
import com.mattelogic.inchfab.common.dto.ApiResponseDto;
import com.mattelogic.inchfab.core.dtos.request.CostRequestDto;
import com.mattelogic.inchfab.core.dtos.response.ResultResponseDto;
import com.mattelogic.inchfab.core.model.ProjectStep;
import com.mattelogic.inchfab.domain.component.FormulaComponent;
import com.mattelogic.inchfab.domain.component.UnitConverterComponent;
import com.mattelogic.inchfab.domain.dto.request.WetProcessRequestDto;
import com.mattelogic.inchfab.domain.enums.ProcessDefinition;
import com.mattelogic.inchfab.domain.model.ProcessStep;
import com.mattelogic.inchfab.domain.repository.WetProcessRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public final class WetProcessServiceImpl implements
    CalculateService<ProjectStep, CostRequestDto, WetProcessRequestDto> {

  private static final String PROCESS_NAME = ProcessDefinition.WET_PROCESS.getValue();
  private static final String DI_WATER = "di_water";

  private final FormulaComponent formulaComponent;
  private final UnitConverterComponent unitConverterComponent;
  private final WetProcessRepository wetProcessRepository;
  private final WetChemicalRepository wetChemicalRepository;

  @Override
  public ResultResponseDto calculate(ProjectStep step, CostRequestDto request) {
    return calculateProcessResults(new WetProcessRequestDto(
        request.waferSize(),
        step.name(),
        step.thickness()
    ));
  }

  @Override
  public ApiResponseDto<ResultResponseDto> calculate(WetProcessRequestDto request) {
    log.info("Processing wet process calculation request: {}", request);
    ResultResponseDto response = calculateProcessResults(request);
    log.info("Wet process calculation completed successfully");
    return createSuccessResponse(response);
  }

  private ResultResponseDto calculateProcessResults(WetProcessRequestDto request) {
    return ResultResponseDto.builder()
        .processName(PROCESS_NAME)
        .laborTime(calculateLaborTime(request))
        .periodicCost(calculatePeriodicCost(request))
        .power(calculateTotalPower(request))
        .wetEtchant(calculateWetChemical(request))
        .totalTime(calculateTotalTime(request))
        .build();
  }

  private ApiResponseDto<ResultResponseDto> createSuccessResponse(ResultResponseDto response) {
    return ApiResponseDto.<ResultResponseDto>builder()
        .status(200)
        .message("Calculate Wet Process result successfully")
        .data(response)
        .build();
  }

  private double calculateTotalPower(WetProcessRequestDto request) {
    List<ProcessStep> powerSteps = createPowerSteps(request);
    return calculateOverheadPower(request) +
        powerSteps.stream()
            .mapToDouble(step -> calculatePowerForStep(request, step))
            .sum();
  }

  private List<ProcessStep> createPowerSteps(WetProcessRequestDto request) {
    return List.of(
        new ProcessStep(
            ProcessDefinition.ETCH.getValue(),
            () -> calculateEtchTime(request),
            ProcessDefinition.ETCH.getValue()
        ),
        new ProcessStep(
            ProcessDefinition.CLEAN.getValue(),
            () -> calculateCleanTime(request),
            ProcessDefinition.CLEAN.getValue()
        ),
        new ProcessStep(
            ProcessDefinition.HEATER.getValue(),
            () -> calculateSetupTakedownTime(request),
            ProcessDefinition.HEATER.getValue()
        )
    );
  }

  private double calculatePowerForStep(WetProcessRequestDto request, ProcessStep step) {
    double time = step.timeCalculator().get();
    double heaterPowerDraw = findWetProcessValue(ProcessDefinition.HEATER_POWER_DRAW);

    if (ProcessDefinition.HEATER.getValue().equals(step.parameterValue())) {
      double latentPower = wetProcessRepository.findEffectiveLatentValueBySettingsName(
          PROCESS_NAME,
          step.parameterValue()
      ).orElse(0.0);
      // Using Formula5: A * B
      return formulaComponent.calculateFormula4(time, latentPower);
    }

    double temperature = getTemperatureForStep(request, step.parameterValue());
    // Using Formula11: A * B * C / s_per_h / W_per_kW
    return formulaComponent.calculateFormula8(
        time,
        temperature,
        heaterPowerDraw,
        3600.0,  // s_per_h
        1000.0   // W_per_kW
    );
  }

  private double getTemperatureForStep(WetProcessRequestDto request, String stepType) {
    return ProcessDefinition.ETCH.getValue().equals(stepType)
        ? wetChemicalRepository.findFieldByName(
        request.name(),
        ProcessDefinition.ETCH_TEMP.getValue()
    ).orElse(0.0)
        : wetProcessRepository.findCleanFieldByName(
            PROCESS_NAME,
            ProcessDefinition.TEMPERATURE.getValue()
        ).orElse(0.0);
  }

  private double calculateWetChemical(WetProcessRequestDto request) {
    double lotSize = wetChemicalRepository.findFieldByName(
        request.name(),
        ProcessDefinition.LOT_SIZE.getValue()
    ).orElse(0.0);
    double runsNeededPerStep = Math.ceil(request.waferSize() / lotSize);

    double mainChemical = wetChemicalRepository.findFieldByName(
        request.name(),
        ProcessDefinition.COST_PER_RUN.getValue()
    ).orElse(0.0);

    double diWater = wetChemicalRepository.findFieldByName(
        DI_WATER,
        ProcessDefinition.COST_PER_RUN.getValue()
    ).orElse(0.0);

    // Using Formula4: A + B
    return formulaComponent.calculateFormula3(
        mainChemical * runsNeededPerStep,
        diWater * runsNeededPerStep
    );
  }

  private double calculateEtchTime(WetProcessRequestDto request) {
    double etchRate = wetChemicalRepository.findFieldByName(
        request.name(),
        ProcessDefinition.ETCH_RATE.getValue()
    ).orElse(0.0);

    if (etchRate == 0.0) {
      return 0.0;
    }

    // Using Formula8: A / B * s_per_min for base etch time
    double baseEtchTime = formulaComponent.calculateFormula7(
        request.thickness(),
        etchRate,
        1.0
    );

    // Using Formula5: A * B
    return formulaComponent.calculateFormula4(baseEtchTime, calculateRunTime(request));
  }

  private double calculateCleanTime(WetProcessRequestDto request) {
    // Using Formula5: A * B
    return formulaComponent.calculateFormula4(
        findWetProcessValue(ProcessDefinition.CLEAN_TIME),
        calculateRunTime(request)
    );
  }

  private double calculateDryTime(WetProcessRequestDto request) {
    // Using Formula5: A * B
    return formulaComponent.calculateFormula4(
        calculateRunTime(request),
        findWetProcessValue(ProcessDefinition.DRYING_RUN)
    );
  }

  private double calculateRunTime(WetProcessRequestDto request) {
    double runsNeededPerStep = wetChemicalRepository.findFieldByName(
        request.name(),
        ProcessDefinition.LOT_SIZE.getValue()
    ).orElse(0.0);

    // Using Formula8: A / B * s_per_min with s_per_min = 1.0
    return Math.ceil(formulaComponent.calculateFormula7(
        request.waferSize(),
        runsNeededPerStep,
        1.0
    ));
  }

  private double calculateProcessTime(WetProcessRequestDto request) {
    double etchTime = calculateEtchTime(request);
    double cleanTime = calculateCleanTime(request);
    double dryTime = calculateDryTime(request);

    // Using Formula4: A + B for summing multiple times
    return formulaComponent.calculateFormula3(
        etchTime,
        formulaComponent.calculateFormula3(cleanTime, dryTime)
    );
  }

  private double calculateSetupTakedownTime(WetProcessRequestDto request) {
    // Using Formula5: A * B
    return formulaComponent.calculateFormula4(
        calculateRunTime(request),
        findWetProcessValue(ProcessDefinition.SETUP_TAKEDOWN_TIME)
    );
  }

  private double calculateLaborTime(WetProcessRequestDto request) {
    double cleanTime = calculateCleanTime(request);
    double dryTime = calculateDryTime(request);
    double setupTime = calculateSetupTakedownTime(request);

    double totalLaborTime = formulaComponent.calculateFormula3(
        cleanTime,
        formulaComponent.calculateFormula3(dryTime, setupTime)
    );

    return unitConverterComponent.convertSecondsHours(totalLaborTime, false);
  }

  private double calculatePeriodicCost(WetProcessRequestDto request) {
    // Using Formula5: A * B
    return formulaComponent.calculateFormula4(
        calculateProcessTime(request),
        findWetProcessValue(ProcessDefinition.PERIODIC_COST)
    );
  }

  private double calculateTotalTime(WetProcessRequestDto request) {
    return unitConverterComponent.convertSecondsHours(
        calculateTotalUsage(request),
        false
    );
  }

  private double calculateTotalUsage(WetProcessRequestDto request) {
    // Using Formula4: A + B
    return formulaComponent.calculateFormula3(
        calculateProcessTime(request),
        calculateSetupTakedownTime(request)
    );
  }

  private double calculateOverheadPower(WetProcessRequestDto request) {
    // Using Formula5: A * B
    return formulaComponent.calculateFormula4(
        calculateTotalUsage(request),
        findWetProcessValue(ProcessDefinition.OVERHEAD_POWER)
    );
  }

  private Double findWetProcessValue(ProcessDefinition parameter) {
    return wetProcessRepository.findValueByNameAndField(
        PROCESS_NAME,
        parameter.getValue()
    ).orElse(0.0);
  }
}