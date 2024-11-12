package com.mattelogic.inchfab.domain.service;

import com.mattelogic.inchfab.base.enums.GasConstant;
import com.mattelogic.inchfab.base.enums.UnitConstant;
import com.mattelogic.inchfab.base.repository.ConstantRepository;
import com.mattelogic.inchfab.base.repository.GasRepository;
import com.mattelogic.inchfab.base.repository.TargetRepository;
import com.mattelogic.inchfab.common.dto.ApiResponseDto;
import com.mattelogic.inchfab.core.dtos.request.CostRequestDto;
import com.mattelogic.inchfab.core.model.ProjectStep;
import com.mattelogic.inchfab.domain.component.FormulaComponent;
import com.mattelogic.inchfab.domain.component.UnitConverterComponent;
import com.mattelogic.inchfab.domain.dto.request.MagnetronSputteringRequestDto;
import com.mattelogic.inchfab.core.dtos.response.ResultResponseDto;
import com.mattelogic.inchfab.domain.enums.ProcessDefinition;
import com.mattelogic.inchfab.domain.model.ProcessStep;
import com.mattelogic.inchfab.domain.repository.MagnetronSputteringRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public final class MagnetronSputteringServiceImpl implements
    CalculateService<ProjectStep, CostRequestDto, MagnetronSputteringRequestDto> {

  private static final String PROCESS_NAME = ProcessDefinition.MAGNETRON_SPUTTER.getValue();
  private static final double DEPOSITION_RATE = 6.0;
  private static final double PRESPUTTER_TIME = 60.0;
  private static final double PRESPUTTER_O2_GAS_RATE = 5.0;
  private static final double DEPOSITION_O2_GAS_RATE = 5.0;

  private final FormulaComponent formulaComponent;
  private final UnitConverterComponent unitConverterComponent;
  private final MagnetronSputteringRepository magnetronSputteringRepository;
  private final GasRepository gasRepository;
  private final TargetRepository targetRepository;
  private final ConstantRepository constantRepository;

  @Override
  public ResultResponseDto calculate(ProjectStep step, CostRequestDto request) {
    return calculateProcessResults(new MagnetronSputteringRequestDto(
        request.waferSize(),
        step.name(),
        step.thickness()
    ));
  }

  @Override
  public ApiResponseDto<ResultResponseDto> calculate(MagnetronSputteringRequestDto request) {
    log.info("Processing Magnetron Sputtering calculation request: {}", request);
    ResultResponseDto response = calculateProcessResults(request);
    log.info("Magnetron Sputtering calculation completed successfully");
    return createSuccessResponse(response);
  }

  private ResultResponseDto calculateProcessResults(MagnetronSputteringRequestDto request) {
    double waferPerRun = findMagnetronSputteringValue(
        ProcessDefinition.WAFER_PER_RUN);
    double runNeededPerJobStep = Math.ceil(request.waferSize() / waferPerRun);

    return ResultResponseDto.builder()
        .processName(PROCESS_NAME)
        .laborTime(calculateLaborTime(request) * runNeededPerJobStep)
        .periodicCost(calculatePeriodicCost(request) * runNeededPerJobStep)
        .power(calculateTotalPower(request) * runNeededPerJobStep)
        .gas(calculateGasUsage(request) * runNeededPerJobStep)
        .targetMaterial(calculateTotalTargetMaterial(request) * runNeededPerJobStep)
        .totalTime(calculateTotalTime(request) * runNeededPerJobStep)
        .build();
  }

  private ApiResponseDto<ResultResponseDto> createSuccessResponse(ResultResponseDto response) {
    return ApiResponseDto.<ResultResponseDto>builder()
        .status(200)
        .message("Calculate Magnetron Sputtering result successfully")
        .data(response)
        .build();
  }

  private double calculateTotalPower(MagnetronSputteringRequestDto request) {
    List<ProcessStep> powerSteps = createPowerSteps(request);
    return calculateOverhead(request) +
        powerSteps.stream()
            .mapToDouble(step -> calculatePowerForStep(request, step))
            .sum();
  }

  private List<ProcessStep> createPowerSteps(MagnetronSputteringRequestDto request) {
    return List.of(
        new ProcessStep(
            ProcessDefinition.PRESPUTTER.getValue(),
            this::calculatePresputterTime,
            ProcessDefinition.PRESPUTTER.getValue()
        ),
        new ProcessStep(
            ProcessDefinition.DEPOSITION.getValue(),
            () -> calculateDepositionTime(request),
            ProcessDefinition.DEPOSITION.getValue()
        ),
        new ProcessStep(
            ProcessDefinition.MATCHING.getValue(),
            () -> calculateProcessTime(request),
            ProcessDefinition.MATCHING.getValue()
        )
    );
  }

  private double calculateGasUsage(MagnetronSputteringRequestDto request) {
    double sPerMin = constantRepository.findValueByTypeAndMetric(
        ProcessDefinition.TIME.getValue(),
        UnitConstant.S_PER_MIN.getValue()
    ).orElse(0.0);
    // Using Formula2: (A * B + C * D) / s_per_min * (1 + E)
    double gasOverhead = findMagnetronSputteringValue(ProcessDefinition.GAS_OVERHEAD);
    double price = gasRepository.findPriceByName(GasConstant.AR.getValue()).orElse(0.0);
    double quantity = formulaComponent.calculateFormula2(
        PRESPUTTER_O2_GAS_RATE,
        calculatePresputterTime(),
        DEPOSITION_O2_GAS_RATE,
        calculateDepositionTime(request),
        sPerMin,
        gasOverhead
    );

    // Using Formula7: A * B * C
    return formulaComponent.calculateFormula4(quantity, price);
  }

  private double calculateTotalTargetMaterial(MagnetronSputteringRequestDto request) {
    // Using Formula5: A * B
    return formulaComponent.calculateFormula4(
        calculateTargetsConsumed(request),
        calculateTargetCost(request)
    );
  }

  private double calculateProcessTime(MagnetronSputteringRequestDto request) {
    // Using Formula4: A + B
    return formulaComponent.calculateFormula3(
        calculatePresputterTime(),
        calculateDepositionTime(request)
    );
  }

  private double calculatePresputterTime() {
    return PRESPUTTER_TIME;
  }

  private double calculateDepositionTime(MagnetronSputteringRequestDto request) {
    double sPerMin = constantRepository.findValueByTypeAndMetric(
        ProcessDefinition.TIME.getValue(),
        UnitConstant.S_PER_MIN.getValue()
    ).orElse(0.0);
    // Using Formula8: A / B * s_per_min
    return formulaComponent.calculateFormula7(
        request.thickness(),
        calculateDepositionRate(),
        sPerMin
    );
  }

  private double calculateDepositionRate() {
    double convertToMinute = unitConverterComponent.convertSecondsMinutes(DEPOSITION_RATE, true);
    return unitConverterComponent.convertAngstromsNanometers(convertToMinute, false);
  }

  private double calculateSetupTakedownTime(MagnetronSputteringRequestDto request) {
    return Optional.of(findMagnetronSputteringValue(ProcessDefinition.SETUP_TAKEDOWN_TIME))
        .filter(value -> calculateProcessTime(request) != 0.0)
        .orElse(0.0);
  }

  private double calculatePowerForStep(MagnetronSputteringRequestDto request, ProcessStep step) {
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

  private double calculateTotalTime(MagnetronSputteringRequestDto request) {
    return unitConverterComponent.convertSecondsHours(calculateTotalUsage(request), false);
  }

  private double calculateTotalUsage(MagnetronSputteringRequestDto request) {
    // Using Formula4: A + B
    return formulaComponent.calculateFormula3(
        calculateProcessTime(request),
        calculateSetupTakedownTime(request)
    );
  }

  private double calculateLaborTime(MagnetronSputteringRequestDto request) {
    return unitConverterComponent.convertSecondsHours(
        calculateSetupTakedownTime(request),
        false
    );
  }

  private double calculatePeriodicCost(MagnetronSputteringRequestDto request) {
    // Using Formula5: A * B
    return formulaComponent.calculateFormula4(
        calculateProcessTime(request),
        findMagnetronSputteringValue(ProcessDefinition.PERIODIC_COST)
    );
  }

  private double calculateOverhead(MagnetronSputteringRequestDto request) {
    // Using Formula5: A * B
    return formulaComponent.calculateFormula4(
        calculateTotalUsage(request),
        findMagnetronSputteringValue(ProcessDefinition.OVERHEAD_POWER)
    );
  }

  private double calculateTargetsConsumed(MagnetronSputteringRequestDto request) {
    double targetLifetime = calculateTargetLifetime(request);
    // Using Formula8: A / B * s_per_min with s_per_min = 1.0
    return formulaComponent.calculateFormula7(
        calculateProcessTime(request),
        targetLifetime,
        1.0
    );
  }

  private double calculateTargetLifetime(MagnetronSputteringRequestDto request) {
    return unitConverterComponent.convertSecondsHours(
        targetRepository.findFieldByName(
            request.name(),
            ProcessDefinition.LIFETIME_HOURS.getValue()
        ).orElse(0.0),
        true
    );
  }

  private double calculateTargetCost(MagnetronSputteringRequestDto request) {
    return targetRepository.findFieldByName(
        request.name(),
        ProcessDefinition.PRICE.getValue()
    ).orElse(0.0);
  }

  private Double findMagnetronSputteringValue(ProcessDefinition parameter) {
    return magnetronSputteringRepository.findValueByNameAndField(
        PROCESS_NAME,
        parameter.getValue()
    ).orElse(0.0);
  }

  private Double findEffectiveProcessValue(String processType) {
    return magnetronSputteringRepository.findTotalEffectiveProcessValueBySettingsName(
        PROCESS_NAME,
        processType
    ).orElse(0.0);
  }

  private Double findEffectiveLatentValue(String processType) {
    return magnetronSputteringRepository.findTotalEffectiveLatentValueBySettingsName(
        PROCESS_NAME,
        processType
    ).orElse(0.0);
  }
}