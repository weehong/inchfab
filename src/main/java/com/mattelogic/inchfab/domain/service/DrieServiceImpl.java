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
import com.mattelogic.inchfab.domain.dto.request.DrieRequestDto;
import com.mattelogic.inchfab.core.dtos.response.ResultResponseDto;
import com.mattelogic.inchfab.domain.enums.ProcessDefinition;
import com.mattelogic.inchfab.domain.model.ProcessStep;
import com.mattelogic.inchfab.domain.repository.DrieRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public final class DrieServiceImpl implements
    CalculateService<ProjectStep, CostRequestDto, DrieRequestDto> {

  private static final String PROCESS_NAME = ProcessDefinition.DRIE.getValue();
  private static final double HE_BSP_RATE = 4.0;
  private static final List<GasConstant> PROCESS_GASES = List.of(
      GasConstant.SF6, GasConstant.C4F8, GasConstant.O2,
      GasConstant.AR, GasConstant.HE
  );
  private static final List<ProcessDefinition> PROCESS_STEPS = List.of(
      ProcessDefinition.SPUTTER,
      ProcessDefinition.ETCH,
      ProcessDefinition.DEPOSITION,
      ProcessDefinition.MATCHING
  );

  private final FormulaComponent formulaComponent;
  private final DrieRepository drieRepository;
  private final GasRepository gasRepository;
  private final VacuumProcessRepository vacuumProcessRepository;
  private final UnitConverterComponent unitConverterComponent;
  private final ConstantRepository constantRepository;

  @Override
  public ResultResponseDto calculate(ProjectStep step, CostRequestDto request) {
    return calculateProcessResults(new DrieRequestDto(
        request.waferSize(),
        step.name(),
        step.depth(),
        step.maskArea()
    ));
  }

  @Override
  public ApiResponseDto<ResultResponseDto> calculate(DrieRequestDto request) {
    log.info("Processing DRIE calculation request: {}", request);

    ResultResponseDto response = calculateProcessResults(request);
    log.info("DRIE calculation completed successfully");

    return createSuccessResponse(response);
  }

  private ResultResponseDto calculateProcessResults(DrieRequestDto request) {
    double waferPerRun = findDrieValue(ProcessDefinition.WAFER_PER_RUN);
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
        .message("Calculate DRIE result successfully")
        .data(response)
        .build();
  }

  private double calculateLaborTime(DrieRequestDto request) {
    return unitConverterComponent.convertSecondsHours(calculateSetupTakedownTime(request), false);
  }

  private double calculatePeriodicCost(DrieRequestDto request) {
    // Using Formula6: A * B
    return formulaComponent.calculateFormula4(
        findDrieValue(ProcessDefinition.PERIODIC_COST),
        calculateProcessTime(request)
    );
  }

  private double calculateTotalPower(DrieRequestDto request) {
    List<ProcessStep> steps = createProcessSteps(request);
    return calculateOverhead(request) +
        steps.stream()
            .mapToDouble(step -> calculatePowerForStep(request, step))
            .sum();
  }

  private List<ProcessStep> createProcessSteps(DrieRequestDto request) {
    return PROCESS_STEPS.stream()
        .map(step -> new ProcessStep(
            step.getValue(),
            () -> calculateStepTime(request, step),
            step.getValue()))
        .toList();
  }

  private double calculatePowerForStep(DrieRequestDto request, ProcessStep step) {
    double processTime = step.timeCalculator().get();
    double setupTakedownTime = calculateSetupTakedownTime(request);
    double effectiveProcess = findEffectiveProcessValue(step.parameterValue());
    double effectiveLatent = findEffectiveLatentValue(step.parameterValue());

    // Using Formula7: A * B + C * D
    return formulaComponent.calculateFormula5(
        processTime,
        effectiveProcess,
        setupTakedownTime,
        effectiveLatent
    );
  }

  private double calculateGasUsage(DrieRequestDto request) {
    return PROCESS_GASES.stream()
        .mapToDouble(gas -> calculateGasCost(gas, request))
        .sum();
  }

  private double calculateGasCost(GasConstant gas, DrieRequestDto request) {
    double processTime = calculateProcessTime(request);
    double gasValue = determineGasValue(gas, request);
    double price = gasRepository.findPriceByName(gas.getValue()).orElse(0.0);
    double sPerMin = constantRepository.findValueByTypeAndMetric(
        ProcessDefinition.TIME.getValue(),
        UnitConstant.S_PER_MIN.getValue()
    ).orElse(0.0);
    double gasOverhead = findDrieValue(ProcessDefinition.GAS_OVERHEAD);
    double quantityPrice = formulaComponent.calculateFormula1(gasValue, processTime, sPerMin,
        gasOverhead);

    return quantityPrice * price;
  }

  private double determineGasValue(GasConstant gas, DrieRequestDto request) {
    return gas.equals(GasConstant.HE)
        ? HE_BSP_RATE
        : vacuumProcessRepository.getMaxGasValue(
            PROCESS_NAME,
            request.name(),
            gas.getValue()
        ).orElse(0.0);
  }

  private Double findDrieValue(ProcessDefinition parameter) {
    return drieRepository.findValueByNameAndField(
        PROCESS_NAME,
        parameter.getValue()
    ).orElse(0.0);
  }

  private Double findEffectiveProcessValue(String processType) {
    return drieRepository.findTotalEffectiveProcessValueBySettingsName(
        PROCESS_NAME,
        processType
    ).orElse(0.0);
  }

  private Double findEffectiveLatentValue(String processType) {
    return drieRepository.findTotalEffectiveLatentValueBySettingsName(
        PROCESS_NAME,
        processType
    ).orElse(0.0);
  }

  private double calculateStepTime(DrieRequestDto request, ProcessDefinition processType) {
    double numberOfCycles = calculateNumberOfCycles(request);
    double stepTime = vacuumProcessRepository.findValueByTypeAndMetric(
        PROCESS_NAME,
        request.name(),
        List.of(processType.getValue()),
        ProcessDefinition.STEP_TIME.getValue()
    ).orElse(0.0);

    // Using Formula6: A * B
    return formulaComponent.calculateFormula4(numberOfCycles, stepTime);
  }

  private double calculateProcessTime(DrieRequestDto request) {
    double rate = findDrieValue(ProcessDefinition.ETCH_RATE);
    return formulaComponent.calculateFormula7(request.depth(), rate, 60.0);
  }

  private double calculateSetupTakedownTime(DrieRequestDto request) {
    return Optional.of(findDrieValue(ProcessDefinition.SETUP_TAKEDOWN_TIME))
        .filter(value -> calculateProcessTime(request) != 0.0)
        .orElse(0.0);
  }

  private double calculateTotalTime(DrieRequestDto request) {
    return unitConverterComponent.convertSecondsHours(
        calculateTotalUsageTime(request),
        false
    );
  }

  private double calculateTotalUsageTime(DrieRequestDto request) {
    // Using Formula5: A + B
    return formulaComponent.calculateFormula3(
        calculateProcessTime(request),
        calculateSetupTakedownTime(request)
    );
  }

  private double calculateOverhead(DrieRequestDto request) {
    double overheadPower = findDrieValue(ProcessDefinition.OVERHEAD_POWER);
    // Using Formula6: A * B
    return formulaComponent.calculateFormula4(overheadPower, calculateTotalUsageTime(request));
  }

  private double calculateNumberOfCycles(DrieRequestDto request) {
    double processTime = calculateProcessTime(request);
    double cycleTime = vacuumProcessRepository.findValueByTypeAndMetric(
        PROCESS_NAME,
        request.name(),
        PROCESS_STEPS.stream()
            .map(ProcessDefinition::getValue)
            .toList(),
        ProcessDefinition.STEP_TIME.getValue()
    ).orElse(0.0);

    // Using Formula9: A / B * s_per_min, with s_per_min = 1.0
    return Math.ceil(Math.abs(
        formulaComponent.calculateFormula7(processTime, cycleTime, 1.0)
    ));
  }
}