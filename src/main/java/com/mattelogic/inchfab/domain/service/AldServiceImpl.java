package com.mattelogic.inchfab.domain.service;

import com.mattelogic.inchfab.base.enums.GasConstant;
import com.mattelogic.inchfab.base.enums.UnitConstant;
import com.mattelogic.inchfab.base.repository.ConstantRepository;
import com.mattelogic.inchfab.base.repository.GasRepository;
import com.mattelogic.inchfab.base.repository.VacuumProcessRepository;
import com.mattelogic.inchfab.common.dto.ApiResponseDto;
import com.mattelogic.inchfab.core.dtos.request.CostRequestDto;
import com.mattelogic.inchfab.core.model.ProjectStep;
import com.mattelogic.inchfab.domain.component.FormulaComponent;
import com.mattelogic.inchfab.domain.component.UnitConverterComponent;
import com.mattelogic.inchfab.domain.dto.request.AldRequestDto;
import com.mattelogic.inchfab.core.dtos.response.ResultResponseDto;
import com.mattelogic.inchfab.domain.enums.ProcessDefinition;
import com.mattelogic.inchfab.domain.repository.AldRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public final class AldServiceImpl implements
    CalculateService<ProjectStep, CostRequestDto, AldRequestDto> {

  private static final String PROCESS_NAME = ProcessDefinition.ALD.getValue();
  private static final double N2_GAS_RATE = 5.0;
  private static final List<GasConstant> PROCESS_GASES = List.of(
      GasConstant.TMA, GasConstant.TDMAT, GasConstant.H2O, GasConstant.N2
  );
  private static final List<ProcessDefinition> ALD_CYCLE_STEPS = List.of(
      ProcessDefinition.FLOW_PRECURSOR_A,
      ProcessDefinition.FLOW_PRECURSOR_B,
      ProcessDefinition.PURGE_A,
      ProcessDefinition.PURGE_B
  );

  private final FormulaComponent formulaComponent;
  private final AldRepository aldRepository;
  private final GasRepository gasRepository;
  private final VacuumProcessRepository vacuumProcessRepository;
  private final UnitConverterComponent unitConverterComponent;
  private final ConstantRepository constantRepository;

  @Override
  public ResultResponseDto calculate(ProjectStep step, CostRequestDto request) {
    return calculateProcessResults(new AldRequestDto(
        request.waferSize(),
        step.name(),
        step.thickness()
    ));
  }

  @Override
  public ApiResponseDto<ResultResponseDto> calculate(AldRequestDto request) {
    log.info("Processing ALD calculation request: {}", request);
    ResultResponseDto response = calculateProcessResults(request);
    log.info("ALD calculation completed successfully");
    return createSuccessResponse(response);
  }

  private ResultResponseDto calculateProcessResults(AldRequestDto request) {
    double waferPerRun = findAldValue(ProcessDefinition.WAFER_PER_RUN);
    double runNeededPerJobStep = Math.ceil(request.waferSize() / waferPerRun);

    return ResultResponseDto.builder()
        .processName(PROCESS_NAME)
        .laborTime(calculateLaborTime(request) * runNeededPerJobStep)
        .periodicCost(calculatePeriodicCost(request) * runNeededPerJobStep)
        .power(calculateTotalPower(request) * runNeededPerJobStep)
        .gas(calculateGasUsage(request) * runNeededPerJobStep)
        .totalTime(calculateTotalTime(request) * runNeededPerJobStep)
        .build();
  }

  private ApiResponseDto<ResultResponseDto> createSuccessResponse(ResultResponseDto response) {
    return ApiResponseDto.<ResultResponseDto>builder()
        .status(200)
        .message("Calculate ALD result successfully")
        .data(response)
        .build();
  }

  private double calculateLaborTime(AldRequestDto request) {
    return unitConverterComponent.convertSecondsHours(
        calculateSetupTakedownTime(request), false);
  }

  private double calculatePeriodicCost(AldRequestDto request) {
    // Using Formula5: A * B
    return formulaComponent.calculateFormula4(
        findAldValue(ProcessDefinition.PERIODIC_COST),
        calculateProcessTime(request)
    );
  }

  private double calculateTotalPower(AldRequestDto request) {
    double gasOverhead = calculateGasOverheadPower(request);
    double heaterLatentPower = calculateHeaterLatentPower(request);
    double processStepsPower = calculateProcessStepsPower(request);

    // Using Formula4: A + B for each addition
    return formulaComponent.calculateFormula3(
        processStepsPower,
        formulaComponent.calculateFormula3(gasOverhead, heaterLatentPower)
    );
  }

  private double calculateProcessStepsPower(AldRequestDto request) {
    return Stream.of(ProcessDefinition.values())
        .filter(ALD_CYCLE_STEPS::contains)
        .mapToDouble(step -> calculateStepPowerValue(request, step))
        .sum();
  }

  private double calculateGasOverheadPower(AldRequestDto request) {
    // Using Formula5: A * B
    return formulaComponent.calculateFormula4(
        calculateTotalUsageTime(request),
        findAldValue(ProcessDefinition.OVERHEAD_POWER)
    );
  }

  private double calculateHeaterLatentPower(AldRequestDto request) {
    // Using Formula5: A * B
    return formulaComponent.calculateFormula4(
        calculateSetupTakedownTime(request),
        aldRepository.findEffectiveLatentValueBySettingsName(
            PROCESS_NAME,
            ProcessDefinition.HEATER.getValue()
        ).orElse(0.0)
    );
  }

  private double calculateGasUsage(AldRequestDto request) {
    return PROCESS_GASES.stream()
        .mapToDouble(gas -> calculateGasCost(gas, request))
        .sum();
  }

  private double calculateGasCost(GasConstant gas, AldRequestDto request) {
    double purgeTimeA = calculateProcessTypeTime(request, ProcessDefinition.PURGE_A);
    double purgeTimeB = calculateProcessTypeTime(request, ProcessDefinition.PURGE_B);
    double precursorTimeA = calculateProcessTypeTime(request, ProcessDefinition.FLOW_PRECURSOR_A);
    double precursorTimeB = calculateProcessTypeTime(request, ProcessDefinition.FLOW_PRECURSOR_B);
    double sPerMin = constantRepository.findValueByTypeAndMetric(
        ProcessDefinition.TIME.getValue(),
        UnitConstant.S_PER_MIN.getValue()
    ).orElse(0.0);

    if (gas.equals(GasConstant.N2)) {
      // Using Formula2: (A * B + C * D) / s_per_min * (1 + E) for N2 gas
      double quantity = formulaComponent.calculateFormula2(
          purgeTimeA,
          N2_GAS_RATE,
          purgeTimeB,
          N2_GAS_RATE,
          sPerMin,
          0.0
      );
      double price = gasRepository.findPriceByName(gas.getValue()).orElse(0.0);
      double gasOverhead = 1.0 + findAldValue(ProcessDefinition.GAS_OVERHEAD);

      // Using Formula7: A * B * C
      return formulaComponent.calculateFormula4(quantity, price);
    } else {
      double gasRateA = findGasValue(request, gas, ProcessDefinition.FLOW_PRECURSOR_A);
      double gasRateB = findGasValue(request, gas, ProcessDefinition.FLOW_PRECURSOR_B);
      double gasOverhead = findAldValue(ProcessDefinition.GAS_OVERHEAD);
      double price = gasRepository.findPriceByName(gas.getValue()).orElse(0.0);

      // Using Formula2: (A * B + C * D) / s_per_min * (1 + E) for process gases
      double quantity = formulaComponent.calculateFormula2(
          precursorTimeA,
          gasRateA,
          precursorTimeB,
          gasRateB,
          sPerMin,
          gasOverhead
      );

      // Using Formula7: A * B * C
      return formulaComponent.calculateFormula4(quantity, price);
    }
  }

  private double calculateTotalUsageTime(AldRequestDto request) {
    // Using Formula4: A + B
    return formulaComponent.calculateFormula3(
        calculateProcessTime(request),
        calculateSetupTakedownTime(request)
    );
  }

  private double calculateTotalTime(AldRequestDto request) {
    return unitConverterComponent.convertSecondsHours(
        calculateTotalUsageTime(request),
        false
    );
  }

  private double calculateProcessTypeTime(AldRequestDto request, ProcessDefinition processType) {
    double value = findProcessValue(
        request,
        processType,
        ProcessDefinition.STEP_TIME
    );
    // Using Formula5: A * B
    return formulaComponent.calculateFormula4(
        value,
        calculateNumberOfCycles(request)
    );
  }

  private double calculateProcessTime(AldRequestDto request) {
    double sPerMin = constantRepository.findValueByTypeAndMetric(
        ProcessDefinition.TIME.getValue(),
        UnitConstant.S_PER_MIN.getValue()
    ).orElse(0.0);
    double rate = findProcessValue(
        request,
        ProcessDefinition.FLOW_PRECURSOR_A,
        ProcessDefinition.BASE_PROCESS_RATE
    );
    // Using Formula8: A / B * s_per_min
    return formulaComponent.calculateFormula7(
        request.thickness(),
        rate,
        sPerMin
    );
  }

  private double calculateSetupTakedownTime(AldRequestDto request) {
    return Optional.of(findAldValue(ProcessDefinition.SETUP_TAKEDOWN_TIME))
        .filter(value -> calculateProcessTime(request) != 0.0)
        .orElse(0.0);
  }

  private double calculateStepPowerValue(AldRequestDto request, ProcessDefinition process) {
    double temperature = findProcessValue(request, process, ProcessDefinition.TEMPERATURE);
    double heaterPowerDraw = findAldValue(ProcessDefinition.HEATER_POWER_DRAW);
    double time = calculateProcessStepTime(request, process);

    // Using Formula7: A * B * C
    double powerValue = formulaComponent.calculateFormula6(temperature, heaterPowerDraw, time);
    return convertToKilowattHours(powerValue);
  }

  private double calculateNumberOfCycles(AldRequestDto request) {
    double processTime = calculateProcessTime(request);
    double cycleTime = vacuumProcessRepository.findValueByTypeAndMetric(
        PROCESS_NAME,
        request.name(),
        ALD_CYCLE_STEPS.stream().map(ProcessDefinition::getValue).toList(),
        ProcessDefinition.STEP_TIME.getValue()
    ).orElse(0.0);

    // Using Formula8: A / B * s_per_min with s_per_min = 1.0
    return formulaComponent.calculateFormula7(processTime, cycleTime, 1.0);
  }

  private double findAldValue(ProcessDefinition parameter) {
    return aldRepository.findValueByNameAndField(
        PROCESS_NAME,
        parameter.getValue()
    ).orElse(0.0);
  }

  private double findGasValue(AldRequestDto request, GasConstant gas,
      ProcessDefinition processType) {
    return vacuumProcessRepository.findValueByTypeAndMetric(
        PROCESS_NAME,
        request.name(),
        List.of(processType.getValue()),
        gas.getValue()
    ).orElse(0.0);
  }

  private double findProcessValue(
      AldRequestDto request,
      ProcessDefinition process,
      ProcessDefinition parameter
  ) {
    return vacuumProcessRepository.findValueByTypeAndMetric(
        PROCESS_NAME,
        request.name(),
        List.of(process.getValue()),
        parameter.getValue()
    ).orElse(0.0);
  }

  private double convertToKilowattHours(double powerValue) {
    return unitConverterComponent.convertWattsKilowatts(
        unitConverterComponent.convertSecondsHours(powerValue, false),
        false
    );
  }

  private double calculateProcessStepTime(AldRequestDto request, ProcessDefinition process) {
    // Using Formula5: A * B
    return formulaComponent.calculateFormula4(
        calculateNumberOfCycles(request),
        findProcessValue(request, process, ProcessDefinition.STEP_TIME)
    );
  }
}