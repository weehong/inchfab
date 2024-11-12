package com.mattelogic.inchfab.domain.service;

import com.mattelogic.inchfab.base.enums.GasConstant;
import com.mattelogic.inchfab.base.enums.UnitConstant;
import com.mattelogic.inchfab.base.repository.ConstantRepository;
import com.mattelogic.inchfab.base.repository.GasRepository;
import com.mattelogic.inchfab.base.repository.VacuumProcessRepository;
import com.mattelogic.inchfab.common.dto.ApiResponseDto;
import com.mattelogic.inchfab.core.dtos.request.CostRequestDto;
import com.mattelogic.inchfab.core.dtos.response.ResultResponseDto;
import com.mattelogic.inchfab.core.model.ProjectStep;
import com.mattelogic.inchfab.domain.component.FormulaComponent;
import com.mattelogic.inchfab.domain.component.UnitConverterComponent;
import com.mattelogic.inchfab.domain.dto.request.RieRequestDto;
import com.mattelogic.inchfab.domain.enums.ProcessDefinition;
import com.mattelogic.inchfab.domain.model.ProcessStep;
import com.mattelogic.inchfab.domain.repository.RieRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public final class RieServiceImpl implements
    CalculateService<ProjectStep, CostRequestDto, RieRequestDto> {

  private static final String PROCESS_NAME = ProcessDefinition.RIE.getValue();
  private static final double O2_GAS_RATE = 5.0;
  private static final List<GasConstant> PROCESS_GASES = List.of(
      GasConstant.SF6, GasConstant.C4F8, GasConstant.CF4,
      GasConstant.CHF3, GasConstant.O2, GasConstant.AR,
      GasConstant.CL2, GasConstant.BCL3
  );

  private final FormulaComponent formulaComponent;
  private final RieRepository rieRepository;
  private final GasRepository gasRepository;
  private final VacuumProcessRepository vacuumProcessRepository;
  private final UnitConverterComponent unitConverterComponent;
  private final ConstantRepository constantRepository;

  @Override
  public ResultResponseDto calculate(ProjectStep step, CostRequestDto request) {
    return calculateProcessResults(new RieRequestDto(
        request.waferSize(),
        step.name(),
        step.depth(),
        step.maskArea()
    ));
  }

  @Override
  public ApiResponseDto<ResultResponseDto> calculate(RieRequestDto request) {
    log.info("Processing RIE calculation request: {}", request);
    ResultResponseDto response = calculateProcessResults(request);
    log.info("RIE calculation completed successfully");
    return createSuccessResponse(response);
  }

  private ResultResponseDto calculateProcessResults(RieRequestDto request) {
    double waferPerRun = findRieValue(ProcessDefinition.WAFER_PER_RUN);
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
        .message("Calculate RIE result successfully")
        .data(response)
        .build();
  }

  private double calculateTotalPower(RieRequestDto request) {
    List<ProcessStep> powerSteps = createPowerSteps(request);
    return calculateOverhead(request) +
        powerSteps.stream()
            .mapToDouble(step -> calculatePowerForStep(request, step))
            .sum();
  }

  private List<ProcessStep> createPowerSteps(RieRequestDto request) {
    return List.of(
        new ProcessStep(
            ProcessDefinition.DEPOSITION.getValue(),
            () -> calculateEtchTime(request),
            ProcessDefinition.DEPOSITION.getValue()
        ),
        new ProcessStep(
            ProcessDefinition.CLEAN.getValue(),
            this::calculateCleanTime,
            ProcessDefinition.CLEAN.getValue()
        ),
        new ProcessStep(
            ProcessDefinition.MATCHING.getValue(),
            this::calculateCleanTime,
            ProcessDefinition.MATCHING.getValue()
        )
    );
  }

  private double calculatePowerForStep(RieRequestDto request, ProcessStep step) {
    double processTime = step.timeCalculator().get();
    double setupTakedownTime = calculateSetupTakedownTime(request);
    double effectiveProcess = findEffectiveProcessValue(step.parameterValue());
    double effectiveLatent = findEffectiveLatentValue(step.parameterValue());

    // Using Formula6: A * B + C * D
    return formulaComponent.calculateFormula5(
        processTime,
        effectiveProcess,
        setupTakedownTime,
        effectiveLatent
    );
  }

  private double calculateGasUsage(RieRequestDto request) {
    return PROCESS_GASES.stream()
        .mapToDouble(gas -> calculateGasCost(gas, request))
        .sum();
  }

  private double calculateGasCost(GasConstant gas, RieRequestDto request) {
    double etchGasValue = findGasValue(gas, request);
    double cleanGasValue = gas.equals(GasConstant.O2) ? O2_GAS_RATE : 0.0;
    double etchTime = calculateEtchTime(request);
    double cleanTime = calculateCleanTime();
    double sPerMin = constantRepository.findValueByTypeAndMetric(
        ProcessDefinition.TIME.getValue(),
        UnitConstant.S_PER_MIN.getValue()
    ).orElse(0.0);
    double gasOverhead = findRieValue(ProcessDefinition.GAS_OVERHEAD);
    double price = gasRepository.findPriceByName(gas.getValue()).orElse(0.0);

    double quantity = formulaComponent.calculateFormula2(
        etchGasValue,
        etchTime,
        cleanGasValue,
        cleanTime,
        sPerMin,
        gasOverhead
    );

    return quantity * price;
  }

  // Time Calculations
  private double calculateProcessTime(RieRequestDto request) {
    // Using Formula4: A + B
    return formulaComponent.calculateFormula3(
        calculateEtchTime(request),
        calculateCleanTime()
    );
  }

  private double calculateEtchTime(RieRequestDto request) {
    double sPerMin = constantRepository.findValueByTypeAndMetric(
        ProcessDefinition.TIME.getValue(),
        UnitConstant.S_PER_MIN.getValue()
    ).orElse(0.0);
    // Using Formula8: A / B * s_per_min
    return formulaComponent.calculateFormula7(
        request.depth(),
        findRieValue(ProcessDefinition.ETCH_RATE),
        sPerMin
    );
  }

  private double calculateCleanTime() {
    return findRieValue(ProcessDefinition.CLEAN_TIME);
  }

  private double calculateTotalTime(RieRequestDto request) {
    return unitConverterComponent.convertSecondsHours(
        calculateTotalUsageTime(request),
        false
    );
  }

  private double calculateTotalUsageTime(RieRequestDto request) {
    // Using Formula4: A + B
    return formulaComponent.calculateFormula3(
        calculateProcessTime(request),
        calculateSetupTakedownTime(request)
    );
  }

  private double calculateSetupTakedownTime(RieRequestDto request) {
    return Optional.of(findRieValue(ProcessDefinition.SETUP_TAKEDOWN_TIME))
        .filter(value -> calculateProcessTime(request) != 0.0)
        .orElse(0.0);
  }

  // Cost and Labor Calculations
  private double calculateLaborTime(RieRequestDto request) {
    return unitConverterComponent.convertSecondsHours(
        calculateSetupTakedownTime(request),
        false
    );
  }

  private double calculatePeriodicCost(RieRequestDto request) {
    // Using Formula5: A * B
    return formulaComponent.calculateFormula4(
        findRieValue(ProcessDefinition.PERIODIC_COST),
        calculateProcessTime(request)
    );
  }

  private double calculateOverhead(RieRequestDto request) {
    // Using Formula5: A * B
    return formulaComponent.calculateFormula4(
        findRieValue(ProcessDefinition.OVERHEAD_POWER),
        calculateTotalUsageTime(request)
    );
  }

  // Repository Access Methods
  private Double findRieValue(ProcessDefinition parameter) {
    return rieRepository.findValueByNameAndField(
        PROCESS_NAME,
        parameter.getValue()
    ).orElse(0.0);
  }

  private Double findEffectiveProcessValue(String processType) {
    return rieRepository.findTotalEffectiveProcessValueBySettingsName(
        PROCESS_NAME,
        processType
    ).orElse(0.0);
  }

  private Double findEffectiveLatentValue(String processType) {
    return rieRepository.findTotalEffectiveLatentValueBySettingsName(
        PROCESS_NAME,
        processType
    ).orElse(0.0);
  }

  private double findGasValue(GasConstant gas, RieRequestDto request) {
    return vacuumProcessRepository.findValueByTypeAndMetric(
        PROCESS_NAME,
        request.name(),
        gas.getValue()
    ).orElse(0.0);
  }
}