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
import com.mattelogic.inchfab.domain.dto.request.IcpcvdRequestDto;
import com.mattelogic.inchfab.domain.enums.ProcessDefinition;
import com.mattelogic.inchfab.domain.model.ProcessStep;
import com.mattelogic.inchfab.domain.repository.IcpcvdRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public final class IcpcvdServiceImpl implements
    CalculateService<ProjectStep, CostRequestDto, IcpcvdRequestDto> {

  private static final String PROCESS_NAME = ProcessDefinition.ICP_CVD.getValue();
  private static final double CLEAN_TIME = 0.0;
  private static final double O2_GAS_RATE = 5.0;
  private static final List<GasConstant> PROCESS_GASES = List.of(
      GasConstant.SIH4HE, GasConstant.O2, GasConstant.N2, GasConstant.AR
  );

  private final FormulaComponent formulaComponent;
  private final UnitConverterComponent unitConverterComponent;
  private final GasRepository gasRepository;
  private final VacuumProcessRepository vacuumProcessRepository;
  private final IcpcvdRepository icpcvdRepository;
  private final ConstantRepository constantRepository;

  @Override
  public ResultResponseDto calculate(ProjectStep step, CostRequestDto request) {
    return calculateProcessResults(new IcpcvdRequestDto(
        request.waferSize(),
        step.name(),
        step.thickness(),
        step.refractiveIndex(),
        step.filmStress()
    ));
  }

  @Override
  public ApiResponseDto<ResultResponseDto> calculate(IcpcvdRequestDto request) {
    log.info("Processing ICP-CVD calculation request: {}", request);
    ResultResponseDto response = calculateProcessResults(request);
    log.info("ICP-CVD calculation completed successfully");
    return createSuccessResponse(response);
  }

  private ResultResponseDto calculateProcessResults(IcpcvdRequestDto request) {
    double waferPerRun = findIcpcvdValue(ProcessDefinition.WAFER_PER_RUN);
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
        .message("Calculate ICP-CVD result successfully")
        .data(response)
        .build();
  }

  private double calculateTotalPower(IcpcvdRequestDto request) {
    List<ProcessStep> powerSteps = createPowerSteps(request);
    return calculateOverheadPower(request) +
        powerSteps.stream()
            .mapToDouble(step -> calculatePowerForStep(request, step))
            .sum();
  }

  private List<ProcessStep> createPowerSteps(IcpcvdRequestDto request) {
    return List.of(
        new ProcessStep(
            ProcessDefinition.DEPOSITION.getValue(),
            () -> calculateDepositionTime(request),
            ProcessDefinition.DEPOSITION.getValue()
        ),
        new ProcessStep(
            ProcessDefinition.CLEAN.getValue(),
            () -> CLEAN_TIME,
            ProcessDefinition.CLEAN.getValue()
        ),
        new ProcessStep(
            ProcessDefinition.MATCHING.getValue(),
            () -> calculateProcessTime(request),
            ProcessDefinition.MATCHING.getValue()
        )
    );
  }

  private double calculatePowerForStep(IcpcvdRequestDto request, ProcessStep step) {
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

  private double calculateGasUsage(IcpcvdRequestDto request) {
    return PROCESS_GASES.stream()
        .mapToDouble(gas -> calculateGasCost(gas, request))
        .sum();
  }

  private double calculateGasCost(GasConstant gas, IcpcvdRequestDto request) {
    double gasValue = findGasValue(gas, request);
    double cleanGasValue = gas == GasConstant.O2 ? O2_GAS_RATE : 0.0;
    double depositionTime = calculateDepositionTime(request);
    double sPerMin = constantRepository.findValueByTypeAndMetric(
        ProcessDefinition.TIME.getValue(),
        UnitConstant.S_PER_MIN.getValue()
    ).orElse(0.0);
    double gasOverhead = findIcpcvdValue(ProcessDefinition.GAS_OVERHEAD);
    double price = gasRepository.findPriceByName(gas.getValue()).orElse(0.0);

    // Using Formula2: (A * B + C * D) / s_per_min * (1 + E)
    double quantity = formulaComponent.calculateFormula2(
        gasValue,
        depositionTime,
        cleanGasValue,
        CLEAN_TIME,
        sPerMin,
        gasOverhead
    );

    return quantity * price;
  }

  private double calculateLaborTime(IcpcvdRequestDto request) {
    return unitConverterComponent.convertSecondsHours(
        calculateSetupTakedownTime(request),
        false
    );
  }

  private double calculatePeriodicCost(IcpcvdRequestDto request) {
    // Using Formula5: A * B
    return formulaComponent.calculateFormula4(
        findIcpcvdValue(ProcessDefinition.PERIODIC_COST),
        calculateProcessTime(request)
    );
  }

  private double calculateDepositionTime(IcpcvdRequestDto request) {
    double sPerMin = constantRepository.findValueByTypeAndMetric(
        ProcessDefinition.TIME.getValue(),
        UnitConstant.S_PER_MIN.getValue()
    ).orElse(0.0);
    double depositionRate = findProcessValue(request, ProcessDefinition.BASE_PROCESS_RATE);
    // Using Formula8: A / B * s_per_min
    return formulaComponent.calculateFormula7(
        request.thickness(),
        depositionRate,
        sPerMin
    );
  }

  private double calculateProcessTime(IcpcvdRequestDto request) {
    // Using Formula4: A + B
    return formulaComponent.calculateFormula3(
        calculateDepositionTime(request),
        CLEAN_TIME
    );
  }

  private double calculateSetupTakedownTime(IcpcvdRequestDto request) {
    return Optional.of(findIcpcvdValue(ProcessDefinition.SETUP_TAKEDOWN_TIME))
        .filter(value -> calculateDepositionTime(request) != 0.0)
        .orElse(0.0);
  }

  private double calculateTotalTime(IcpcvdRequestDto request) {
    return unitConverterComponent.convertSecondsHours(
        calculateTotalUsageTime(request),
        false
    );
  }

  private double calculateTotalUsageTime(IcpcvdRequestDto request) {
    // Using Formula4: A + B
    return formulaComponent.calculateFormula3(
        calculateProcessTime(request),
        calculateSetupTakedownTime(request)
    );
  }

  private double calculateOverheadPower(IcpcvdRequestDto request) {
    // Using Formula5: A * B
    return formulaComponent.calculateFormula4(
        calculateTotalUsageTime(request),
        findIcpcvdValue(ProcessDefinition.OVERHEAD_POWER)
    );
  }

  private Double findEffectiveProcessValue(String processType) {
    return icpcvdRepository.findTotalEffectiveProcessValueBySettingsName(
        PROCESS_NAME,
        processType
    ).orElse(0.0);
  }

  private Double findEffectiveLatentValue(String processType) {
    return icpcvdRepository.findTotalEffectiveLatentValueBySettingsName(
        PROCESS_NAME,
        processType
    ).orElse(0.0);
  }

  private double findGasValue(GasConstant gas, IcpcvdRequestDto request) {
    return vacuumProcessRepository.findValueByTypeAndMetric(
        PROCESS_NAME,
        request.name(),
        gas.getValue()
    ).orElse(0.0);
  }

  private Double findIcpcvdValue(ProcessDefinition parameter) {
    return icpcvdRepository.findValueByNameAndField(
        PROCESS_NAME,
        parameter.getValue()
    ).orElse(0.0);
  }

  private double findProcessValue(IcpcvdRequestDto request, ProcessDefinition parameter) {
    return vacuumProcessRepository.findValueByTypeAndMetric(
        PROCESS_NAME,
        request.name(),
        parameter.getValue()
    ).orElse(0.0);
  }
}