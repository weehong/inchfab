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
import com.mattelogic.inchfab.domain.dto.request.LpcvdRequestDto;
import com.mattelogic.inchfab.core.dtos.response.ResultResponseDto;
import com.mattelogic.inchfab.domain.enums.ProcessDefinition;
import com.mattelogic.inchfab.domain.model.ProcessStep;
import com.mattelogic.inchfab.domain.repository.LpcvdRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public final class LpcvdServiceImpl implements
    CalculateService<ProjectStep, CostRequestDto, LpcvdRequestDto> {

  private static final String PROCESS_NAME = ProcessDefinition.LP_CVD.getValue();
  private static final List<GasConstant> PROCESS_GASES = List.of(
      GasConstant.SIH4, GasConstant.O2, GasConstant.N2, GasConstant.N2O,
      GasConstant.NH3, GasConstant.SIH2CL2
  );

  private final FormulaComponent formulaComponent;
  private final LpcvdRepository lpcvdRepository;
  private final GasRepository gasRepository;
  private final VacuumProcessRepository vacuumProcessRepository;
  private final UnitConverterComponent unitConverterComponent;
  private final ConstantRepository constantRepository;

  @Override
  public ResultResponseDto calculate(ProjectStep step, CostRequestDto request) {
    return calculateProcessResults(new LpcvdRequestDto(
        request.waferSize(),
        step.name(),
        step.thickness()
    ));
  }

  @Override
  public ApiResponseDto<ResultResponseDto> calculate(LpcvdRequestDto request) {
    log.info("Processing LPCVD calculation request: {}", request);
    ResultResponseDto response = calculateProcessResults(request);
    log.info("LPCVD calculation completed successfully");
    return createSuccessResponse(response);
  }

  private ResultResponseDto calculateProcessResults(LpcvdRequestDto request) {
    double waferPerRun = findLpcvdValue(ProcessDefinition.WAFER_PER_RUN);
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
        .message("Calculate LPCVD result successfully")
        .data(response)
        .build();
  }

  private double calculateTotalPower(LpcvdRequestDto request) {
    List<ProcessStep> powerSteps = createPowerSteps(request);
    return calculateGasOverheadPower(request) +
        powerSteps.stream()
            .mapToDouble(step -> calculatePowerForStep(request, step))
            .sum();
  }

  private List<ProcessStep> createPowerSteps(LpcvdRequestDto request) {
    return List.of(
        new ProcessStep(
            ProcessDefinition.DEPOSITION.getValue(),
            () -> calculateDepositionTime(request),
            ProcessDefinition.DEPOSITION.getValue()
        ),
        new ProcessStep(
            ProcessDefinition.CLEAN.getValue(),
            () -> findProcessValue(request, ProcessDefinition.CLEAN_TIME),
            ProcessDefinition.CLEAN.getValue()
        ),
        new ProcessStep(
            ProcessDefinition.RAMP_UP_DOWN_TEMPERATURE.getValue(),
            this::getRampUpDownTime,
            ProcessDefinition.RAMP_UP_DOWN_TEMPERATURE.getValue()
        ),
        new ProcessStep(
            ProcessDefinition.HEATER.getValue(),
            () -> calculateSetupTakedownTime(request),
            ProcessDefinition.HEATER.getValue()
        )
    );
  }

  private double calculatePowerForStep(LpcvdRequestDto request, ProcessStep step) {
    double temperature = findProcessValue(request, ProcessDefinition.TEMPERATURE);
    double heaterPowerDraw = findLpcvdValue(ProcessDefinition.HEATER_POWER_DRAW);
    double time = step.timeCalculator().get();
    double wPerKw = constantRepository.findValueByTypeAndMetric(
        ProcessDefinition.POWER.getValue(),
        UnitConstant.W_PER_KW.getValue()
    ).orElse(0.0);
    double sPerH = constantRepository.findValueByTypeAndMetric(
        ProcessDefinition.TIME.getValue(),
        UnitConstant.S_PER_H.getValue()
    ).orElse(0.0);

    if (ProcessDefinition.DEPOSITION.getValue().equals(step.parameterValue())) {
      return formulaComponent.calculateFormula8(time,
          temperature, heaterPowerDraw, wPerKw, sPerH);
    } else {
      return formulaComponent.calculateFormula4(time, getEffectiveLatentValue());
    }
  }

  private double calculateGasUsage(LpcvdRequestDto request) {
    return PROCESS_GASES.stream()
        .mapToDouble(gas -> calculateGasCost(gas, request))
        .sum();
  }

  private double calculateDepositionTime(LpcvdRequestDto request) {
    double depositionRate = findProcessValue(request, ProcessDefinition.BASE_PROCESS_RATE);
    double sPerMin = constantRepository.findValueByTypeAndMetric(
        ProcessDefinition.TIME.getValue(),
        UnitConstant.S_PER_MIN.getValue()
    ).orElse(0.0);
    // Using Formula8: A / B * s_per_min
    return formulaComponent.calculateFormula7(
        request.thickness(),
        depositionRate,
        sPerMin
    );
  }

  private double calculateGasCost(GasConstant gas, LpcvdRequestDto request) {
    double gasValue = findGasValue(gas, request);
    double depositionTime = calculateDepositionTime(request);
    double sPerMin = constantRepository.findValueByTypeAndMetric(
        ProcessDefinition.TIME.getValue(),
        UnitConstant.S_PER_MIN.getValue()
    ).orElse(0.0);

    // Using Formula2: (A * B + C * D) / s_per_min * (1 + E)
    double quantity = formulaComponent.calculateFormula2(
        gasValue,
        depositionTime,
        0.0,
        0.0,
        sPerMin,
        0.0
    );

    double price = gasRepository.findPriceByName(gas.getValue()).orElse(0.0);
    double gasOverhead = 1.0 + findLpcvdValue(ProcessDefinition.GAS_OVERHEAD);

    // Using Formula7: A * B * C
    return formulaComponent.calculateFormula6(quantity, gasOverhead, price);
  }

  private double getEffectiveLatentValue() {
    return lpcvdRepository.findEffectiveLatentValueBySettingsName(
        PROCESS_NAME,
        ProcessDefinition.HEATER.getValue()
    ).orElse(0.0);
  }

  private double getRampUpDownTime() {
    return findLpcvdValue(ProcessDefinition.RAMP_UP_DOWN_TEMPERATURE);
  }

  private double calculateProcessTime(LpcvdRequestDto request) {
    double cleanTime = findProcessValue(request, ProcessDefinition.CLEAN_TIME);
    // Using Formula4: A + B multiple times for summing
    return formulaComponent.calculateFormula3(
        calculateDepositionTime(request),
        formulaComponent.calculateFormula3(getRampUpDownTime(), cleanTime)
    );
  }

  private double calculateGasOverheadPower(LpcvdRequestDto request) {
    // Using Formula5: A * B
    return formulaComponent.calculateFormula4(
        calculateTotalUsageTime(request),
        findLpcvdValue(ProcessDefinition.OVERHEAD_POWER)
    );
  }

  private double calculateLaborTime(LpcvdRequestDto request) {
    return unitConverterComponent.convertSecondsHours(
        calculateSetupTakedownTime(request),
        false
    );
  }

  private double calculatePeriodicCost(LpcvdRequestDto request) {
    // Using Formula5: A * B
    return formulaComponent.calculateFormula4(
        findLpcvdValue(ProcessDefinition.PERIODIC_COST),
        calculateProcessTime(request)
    );
  }

  private double calculateTotalTime(LpcvdRequestDto request) {
    return unitConverterComponent.convertSecondsHours(
        calculateTotalUsageTime(request),
        false
    );
  }

  private double calculateTotalUsageTime(LpcvdRequestDto request) {
    // Using Formula4: A + B
    return formulaComponent.calculateFormula3(
        calculateProcessTime(request),
        calculateSetupTakedownTime(request)
    );
  }

  private double calculateSetupTakedownTime(LpcvdRequestDto request) {
    return Optional.of(findLpcvdValue(ProcessDefinition.SETUP_TAKEDOWN_TIME))
        .filter(value -> calculateDepositionTime(request) != 0.0)
        .orElse(0.0);
  }

  private Double findLpcvdValue(ProcessDefinition parameter) {
    return lpcvdRepository.findValueByNameAndField(
        PROCESS_NAME,
        parameter.getValue()
    ).orElse(0.0);
  }

  private double findGasValue(GasConstant gas, LpcvdRequestDto request) {
    return vacuumProcessRepository.findValueByTypeAndMetric(
        PROCESS_NAME,
        request.name(),
        gas.getValue()
    ).orElse(0.0);
  }

  private double findProcessValue(LpcvdRequestDto request, ProcessDefinition parameter) {
    return vacuumProcessRepository.findValueByTypeAndMetric(
        PROCESS_NAME,
        request.name(),
        parameter.getValue()
    ).orElse(0.0);
  }

  private double convertToPowerUnits(double power) {
    return unitConverterComponent.convertSecondsHours(
        unitConverterComponent.convertWattsKilowatts(power, false),
        false
    );
  }
}