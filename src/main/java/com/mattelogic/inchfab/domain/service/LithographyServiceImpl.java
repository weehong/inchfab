package com.mattelogic.inchfab.domain.service;

import com.mattelogic.inchfab.base.enums.UnitConstant;
import com.mattelogic.inchfab.base.repository.ConstantRepository;
import com.mattelogic.inchfab.base.repository.LithographyDataRepository;
import com.mattelogic.inchfab.common.dto.ApiResponseDto;
import com.mattelogic.inchfab.core.dtos.request.CostRequestDto;
import com.mattelogic.inchfab.core.model.ProjectStep;
import com.mattelogic.inchfab.domain.component.FormulaComponent;
import com.mattelogic.inchfab.domain.component.UnitConverterComponent;
import com.mattelogic.inchfab.domain.dto.request.LithographyRequestDto;
import com.mattelogic.inchfab.core.dtos.response.ResultResponseDto;
import com.mattelogic.inchfab.domain.enums.ProcessDefinition;
import com.mattelogic.inchfab.domain.exception.LithographyCalculationException;
import com.mattelogic.inchfab.domain.repository.LithographyRepository;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public final class LithographyServiceImpl implements
    CalculateService<ProjectStep, CostRequestDto, LithographyRequestDto> {

  private static final String PROCESS_NAME = ProcessDefinition.LITHOGRAPHY.getValue();
  private static final String SNF_LETTER = "snf";
  private static final double HMDS_TEMPERATURE = 150.0;

  private final FormulaComponent formulaComponent;
  private final UnitConverterComponent unitConverter;
  private final LithographyRepository lithographyRepository;
  private final LithographyDataRepository lithographyDataRepository;
  private final ConstantRepository constantRepository;

  @Override
  public ResultResponseDto calculate(ProjectStep step, CostRequestDto request) {
    return calculateProcessResults(new LithographyRequestDto(
        request.waferSize(),
        step.name(),
        step.thickness(),
        step.aligner()
    ));
  }

  @Override
  public ApiResponseDto<ResultResponseDto> calculate(LithographyRequestDto request) {
    log.info("Processing lithography calculation request: {}", request);
    try {
      ResultResponseDto response = calculateProcessResults(request);
      log.info("Lithography calculation completed successfully");
      return createSuccessResponse(response);
    } catch (Exception e) {
      log.error("Error processing lithography calculation", e);
      throw new LithographyCalculationException("Failed to process lithography calculation", e);
    }
  }

  private ResultResponseDto calculateProcessResults(LithographyRequestDto request) {
    return ResultResponseDto.builder()
        .processName(PROCESS_NAME)
        .laborTime(calculateLaborTime(request))
        .periodicCost(calculatePeriodicCost(request))
        .power(calculateTotalPower(request))
        .lithographyReagent(calculateReagentCosts(request))
        .externalCost(0.0)
        .totalTime(calculateTotalTime(request))
        .build();
  }

  private List<ProcessStep> getProcessSteps() {
    return List.of(
        new ProcessStep("HMDS Pre-Soak", ProcessDefinition.HMDS_PRE_SOAK,
            this::hmdsPreSoakTime, this::hmdsPreSoakSetupTime, this::hmdsPreSoakLaborTime),
        new ProcessStep("Spin Coat", ProcessDefinition.SPIN_COAT,
            this::spinCoatTime, this::spinCoatSetupTime, this::spinCoatLaborTime),
        new ProcessStep("Soft Bake", ProcessDefinition.SOFT_BAKE,
            this::softbakeTime, this::softbakeSetupTime, this::softbakeLaborTime),
        new ProcessStep("Rehydration", ProcessDefinition.REHYDRATION,
            this::rehydrationTime, this::rehydrationSetupTime, this::rehydrationLaborTime),
        new ProcessStep("Contact Alignment", ProcessDefinition.CONTACT_ALIGNMENT,
            this::contactAlignmentTime, this::contactAlignmentSetupTime,
            this::contactAlignmentLaborTime),
        new ProcessStep("Contact Exposure", ProcessDefinition.CONTACT_EXPOSURE,
            this::contactExposureTime, this::contactExposureSetupTime,
            this::contactExposureLaborTime),
        new ProcessStep("Maskless Alignment", ProcessDefinition.MASKLESS_ALIGNMENT,
            this::masklessAlignmentTime, this::masklessAlignmentSetupTime,
            this::masklessAlignmentLaborTime),
        new ProcessStep("Maskless Exposure", ProcessDefinition.MASKLESS_EXPOSURE,
            this::masklessExposureTime, this::masklessExposureSetupTime,
            this::masklessExposureLaborTime),
        new ProcessStep("Post Bake", ProcessDefinition.POST_BAKE,
            this::postBakeTime, this::postBakeSetupTime, this::postBakeLaborTime),
        new ProcessStep("Developer", ProcessDefinition.DEVELOPER,
            this::developerTime, this::developerSetupTime, this::developerLaborTime),
        new ProcessStep("Drying", ProcessDefinition.DRYING,
            this::dryingTime, this::dryingSetupTime, this::dryingLaborTime),
        new ProcessStep("Hard Bake", ProcessDefinition.HARD_BAKE,
            this::hardBakeTime, this::hardBakeSetupTime, this::hardBakeLaborTime)
    );
  }

  private List<CalculationStep> getAlignmentExposureTime() {
    return List.of(
        new CalculationStep(
            ProcessDefinition.CONTACT_ALIGNMENT,
            this::contactAlignmentTime,
            this::contactAlignmentSetupTime),
        new CalculationStep(
            ProcessDefinition.CONTACT_EXPOSURE,
            this::contactExposureTime,
            this::contactExposureSetupTime),
        new CalculationStep(
            ProcessDefinition.MASKLESS_ALIGNMENT,
            this::masklessAlignmentTime,
            this::masklessAlignmentSetupTime),
        new CalculationStep(
            ProcessDefinition.MASKLESS_EXPOSURE,
            this::masklessExposureTime,
            this::masklessExposureSetupTime)
    );
  }

  private double calculateTotalTime(LithographyRequestDto request) {
    double totalTime = Stream.of(
            calculateTotalProcessTime(request),
            calculateTotalSetupTime(request)
        ).mapToDouble(Double::doubleValue)
        .sum();
    return unitConverter.convertSecondsHours(totalTime, false);
  }

  private double calculateTimeWithBatching(ProcessStep step, LithographyRequestDto request,
      double runSize, TimeCalculationType type) {
    if (runSize <= 0.0) {
      return 0.0;
    }

    double batchCount = Math.ceil(request.waferSize() / runSize);
    return type.getTimeFunction(step).apply(request) * batchCount;
  }

  private double calculateStepTimeByType(ProcessStep step, LithographyRequestDto request,
      TimeCalculationType type) {
    double runSize = findRunSize(request.name(), step);
    return calculateTimeWithBatching(step, request, runSize, type);
  }

  private double calculateTotalProcessTime(LithographyRequestDto request) {
    return getProcessSteps().stream()
        .mapToDouble(step -> calculateStepTimeByType(step, request, TimeCalculationType.PROCESS))
        .sum();
  }

  private double calculateTotalSetupTime(LithographyRequestDto request) {
    return getProcessSteps().stream()
        .mapToDouble(step -> calculateStepTimeByType(step, request, TimeCalculationType.SETUP))
        .sum();
  }

  private double calculateLaborTime(LithographyRequestDto request) {
    double laborTime = getProcessSteps().stream()
        .mapToDouble(step -> calculateStepTimeByType(step, request, TimeCalculationType.LABOR))
        .sum();
    return unitConverter.convertSecondsHours(laborTime, false);
  }

  private double calculatePeriodicCost(LithographyRequestDto request) {
    double periodicCost = findLithographyValue(ProcessDefinition.PERIODIC_COST);
    double totalTime = calculateTotalProcessTime(request) + calculateTotalSetupTime(request);
    // Using Formula5: A * B
    return formulaComponent.calculateFormula4(totalTime, periodicCost);
  }

  private double calculateExternalCost(LithographyRequestDto request) {
    double price = lithographyRepository.findPrice(request.aligner()).orElse(0.0) / 60;

    double total = getAlignmentExposureTime().stream()
        .mapToDouble(step -> calculateExternalCost(request, step))
        .sum();

    return unitConverter.convertSecondsMinutes(
        formulaComponent.calculateFormula4(price, total),
        false);
  }

  private double calculateTotalPower(LithographyRequestDto request) {
    return Stream.of(
            calculateOverheadPower(request),
            calculateHeaterPowers(request),
            calculateOtherPowers(request),
            calculateAlignmentAndExposurePower(request)
        ).mapToDouble(Double::doubleValue)
        .sum();
  }

  private double calculateOverheadPower(LithographyRequestDto request) {
    // Calculate total time using Formula4: A + B
    double totalTime = formulaComponent.calculateFormula3(
        calculateTotalProcessTime(request),
        calculateTotalSetupTime(request)
    );
    double overheadPower = findLithographyValue(ProcessDefinition.OVERHEAD_POWER);

    // Using Formula5: A * B
    return formulaComponent.calculateFormula4(totalTime, overheadPower);
  }

  private double calculateHeaterPowers(LithographyRequestDto request) {
    List<HeaterStep> heaterSteps = List.of(
        new HeaterStep(ProcessDefinition.HMDS,
            req -> HMDS_TEMPERATURE,
            this::hmdsPreSoakTime,
            this::hmdsPreSoakSetupTime),
        new HeaterStep(ProcessDefinition.SOFT_BAKE, this::softbakeTemp,
            this::softbakeTime, this::softbakeSetupTime),
        new HeaterStep(ProcessDefinition.POST_BAKE, this::postbakeTemp,
            this::postBakeTime, this::postBakeSetupTime),
        new HeaterStep(ProcessDefinition.HARD_BAKE, this::hardbakeTemp,
            this::hardBakeTime, this::hardBakeSetupTime)
    );

    return heaterSteps.stream()
        .mapToDouble(step -> checkProcessTimeIsZero(request, step.parameter())
            ? 0.0
            : calculateHeaterPower(request, step))
        .sum();
  }

  private double calculateHeaterPower(LithographyRequestDto request, HeaterStep step) {
    double heaterPowerDraw = findLithographyValue(ProcessDefinition.HEATER_POWER_DRAW);
    double processTemp = step.temperatureFunc().apply(request);
    double processTime = step.processTimeFunc().apply(request);
    double sPerH = constantRepository.findValueByTypeAndMetric(
        ProcessDefinition.TIME.getValue(),
        UnitConstant.S_PER_H.getValue()
    ).orElse(0.0);
    double wPerKw = constantRepository.findValueByTypeAndMetric(
        ProcessDefinition.POWER.getValue(),
        UnitConstant.W_PER_KW.getValue()
    ).orElse(0.0);

    // Using Formula11: A * B * C / s_per_h / W_per_kW
    double wattage = formulaComponent.calculateFormula8(
        processTime,
        processTemp,
        heaterPowerDraw,
        sPerH,
        wPerKw
    );

    double effectiveLatent = lithographyRepository.findEffectiveLatentBySettingsAndStepName(
        PROCESS_NAME,
        ProcessDefinition.HEATER.getValue(),
        step.parameter().getValue()
    ).orElse(0.0);

    // Using Formula5: A * B for setup power
    double setupPower = formulaComponent.calculateFormula4(
        step.setupTimeFunc().apply(request),
        effectiveLatent
    );

    return wattage + setupPower;
  }

  private double calculateOtherPowers(LithographyRequestDto request) {
    List<CalculationStep> otherSteps = List.of(
        new CalculationStep(
            ProcessDefinition.SPIN_COAT,
            this::spinCoatTime,
            this::spinCoatSetupTime),
        new CalculationStep(
            ProcessDefinition.DEVELOPER,
            this::developerTime,
            this::developerSetupTime),
        new CalculationStep(
            ProcessDefinition.DRYING,
            this::dryingTime,
            this::dryingSetupTime)
    );

    return otherSteps.stream()
        .filter(step -> !checkProcessTimeIsZero(request, step.parameter))
        .mapToDouble(step -> calculateOtherPower(request, step))
        .sum();
  }

  private double calculateExternalCost(LithographyRequestDto request, CalculationStep step) {
    if (findProcessTime(request, step.parameter) == 0.0) {
      return 0.0;
    }

    ProcessStep processStep = new ProcessStep(
        step.parameter.getValue(),
        step.parameter,
        step.processTimeFunc,
        step.setupTimeFunc,
        req -> 0.0
    );

    double runSize = findRunSize(request.name(), processStep);
    double processTime = calculateTimeWithBatching(processStep, request, runSize,
        TimeCalculationType.PROCESS);
    double setupTime = calculateTimeWithBatching(processStep, request, runSize,
        TimeCalculationType.SETUP);

    return Stream.of(
            processTime,
            setupTime
        ).mapToDouble(Double::doubleValue)
        .sum();
  }

  private double calculateOtherPower(LithographyRequestDto request, CalculationStep step) {
    if (findProcessTime(request, step.parameter) == 0.0) {
      return 0.0;
    }

    ProcessStep processStep = new ProcessStep(
        step.parameter.getValue(),
        step.parameter,
        step.processTimeFunc,
        step.setupTimeFunc,
        req -> 0.0
    );

    double runSize = findRunSize(request.name(), processStep);
    double processTime = calculateTimeWithBatching(processStep, request, runSize,
        TimeCalculationType.PROCESS);
    double setupTime = calculateTimeWithBatching(processStep, request, runSize,
        TimeCalculationType.SETUP);

    // Using Formula6: A * B + C * D
    return formulaComponent.calculateFormula5(
        processTime,
        getEffectiveProcess(step.parameter),
        setupTime,
        getEffectiveLatent(step.parameter)
    );
  }

  private double calculateAlignmentAndExposurePower(LithographyRequestDto request) {
    if (isSNF().test(request.aligner())) {
      return 0.0;
    }

    return Stream.of(
            new CalculationStep(
                ProcessDefinition.CONTACT_ALIGNMENT,
                this::contactAlignmentTime,
                this::contactAlignmentSetupTime
            ),
            new CalculationStep(
                ProcessDefinition.CONTACT_EXPOSURE,
                this::contactExposureTime,
                this::contactExposureSetupTime
            )
        )
        .mapToDouble(step -> calculateStepPower(request, step))
        .sum();
  }

  private double calculateStepPower(LithographyRequestDto request, CalculationStep step) {
    ProcessStep processStep = new ProcessStep(
        step.parameter().getValue(),
        step.parameter,
        step.processTimeFunc,
        step.setupTimeFunc,
        req -> 0.0
    );

    double runSize = findRunSize(request.name(), processStep);
    double processTime = calculateTimeWithBatching(processStep, request, runSize,
        TimeCalculationType.PROCESS);
    double setupTime = calculateTimeWithBatching(processStep, request, runSize,
        TimeCalculationType.SETUP);

    // Using Formula6: A * B + C * D
    return formulaComponent.calculateFormula5(
        processTime,
        getEffectiveProcess(ProcessDefinition.ALIGN_EXPOSURE_TIME),
        setupTime,
        getEffectiveLatent(ProcessDefinition.ALIGN_EXPOSURE_TIME)
    );
  }

  private double calculateReagentCosts(LithographyRequestDto request) {
    List<MaterialParameters> materials = List.of(
        new MaterialParameters(ProcessDefinition.HMDS, ProcessDefinition.HMDS_PRE_SOAK),
        new MaterialParameters(ProcessDefinition.PHOTORESIST, ProcessDefinition.SPIN_COAT),
        new MaterialParameters(ProcessDefinition.DEVELOPER, ProcessDefinition.DEVELOPER)
    );

    return materials.stream()
        .mapToDouble(material -> calculateMaterialCost(request, material))
        .sum();
  }

  private double calculateMaterialCost(LithographyRequestDto request, MaterialParameters params) {
    double costPerRun = lithographyDataRepository.findMaterialValue(
        request.name(),
        params.material.getValue(),
        ProcessDefinition.COST_PER_RUN.getValue()
    ).orElse(0.0);

    double runSize = lithographyDataRepository.findFieldByNameAndField(
        request.name(),
        ProcessDefinition.RUN_SIZE.getValue(),
        params.processStep.getValue()
    ).orElse(0.0);

    if (runSize <= 0) {
      return 0.0;
    }

    // Using Formula8: A / B * s_per_min with s_per_min = 1.0, then multiply by costPerRun
    double runs = Math.ceil(request.waferSize() / runSize);
    // Using Formula5: A * B
    return formulaComponent.calculateFormula4(costPerRun, runs);
  }

  // Temperature Methods
  private double softbakeTemp(LithographyRequestDto request) {
    return findTemperature(request, ProcessDefinition.SOFT_BAKE);
  }

  private double postbakeTemp(LithographyRequestDto request) {
    return findTemperature(request, ProcessDefinition.POST_BAKE);
  }

  private double hardbakeTemp(LithographyRequestDto request) {
    return findTemperature(request, ProcessDefinition.HARD_BAKE);
  }

  private double findTemperature(LithographyRequestDto request, ProcessDefinition step) {
    return lithographyDataRepository.findFieldByNameAndField(
        request.name(),
        ProcessDefinition.TEMPERATURE.getValue(),
        step.getValue()
    ).orElse(0.0);
  }

  // Process Time Methods
  private double hmdsPreSoakTime(LithographyRequestDto request) {
    return findProcessTime(request, ProcessDefinition.HMDS_PRE_SOAK);
  }

  private double spinCoatTime(LithographyRequestDto request) {
    return findProcessTime(request, ProcessDefinition.SPIN_COAT);
  }

  private double softbakeTime(LithographyRequestDto request) {
    return findProcessTime(request, ProcessDefinition.SOFT_BAKE);
  }

  private double rehydrationTime(LithographyRequestDto request) {
    return findProcessTime(request, ProcessDefinition.REHYDRATION);
  }

  private double contactAlignmentTime(LithographyRequestDto request) {
    return !isMaskless(request.aligner())
        ? findProcessTime(request, ProcessDefinition.CONTACT_ALIGNMENT)
        : 0.0;
  }

  private double contactExposureTime(LithographyRequestDto request) {
    return !isMaskless(request.aligner())
        ? findProcessTime(request, ProcessDefinition.CONTACT_EXPOSURE)
        : 0.0;
  }

  private double masklessAlignmentTime(LithographyRequestDto request) {
    return isMaskless(request.aligner())
        ? findProcessTime(request, ProcessDefinition.MASKLESS_ALIGNMENT)
        : 0.0;
  }

  private double masklessExposureTime(LithographyRequestDto request) {
    return isMaskless(request.aligner())
        ? findProcessTime(request, ProcessDefinition.MASKLESS_EXPOSURE)
        : 0.0;
  }

  private double postBakeTime(LithographyRequestDto request) {
    return findProcessTime(request, ProcessDefinition.POST_BAKE);
  }

  private double developerTime(LithographyRequestDto request) {
    return findProcessTime(request, ProcessDefinition.DEVELOPER);
  }

  private double dryingTime(LithographyRequestDto request) {
    return findProcessTime(request, ProcessDefinition.DRYING);
  }

  private double hardBakeTime(LithographyRequestDto request) {
    return findProcessTime(request, ProcessDefinition.HARD_BAKE);
  }

  // Setup Time Methods
  private double hmdsPreSoakSetupTime(LithographyRequestDto request) {
    return findSetupTime(request, ProcessDefinition.HMDS_PRE_SOAK);
  }

  private double spinCoatSetupTime(LithographyRequestDto request) {
    return findSetupTime(request, ProcessDefinition.SPIN_COAT);
  }

  private double softbakeSetupTime(LithographyRequestDto request) {
    return findSetupTime(request, ProcessDefinition.SOFT_BAKE);
  }

  private double rehydrationSetupTime(LithographyRequestDto request) {
    return findSetupTime(request, ProcessDefinition.REHYDRATION);
  }

  private double contactAlignmentSetupTime(LithographyRequestDto request) {
    return !isMaskless(request.aligner())
        ? findSetupTime(request, ProcessDefinition.CONTACT_ALIGNMENT)
        : 0.0;
  }

  private double contactExposureSetupTime(LithographyRequestDto request) {
    return !isMaskless(request.aligner())
        ? findSetupTime(request, ProcessDefinition.CONTACT_EXPOSURE)
        : 0.0;
  }

  private double masklessAlignmentSetupTime(LithographyRequestDto request) {
    return isMaskless(request.aligner())
        ? findSetupTime(request, ProcessDefinition.MASKLESS_ALIGNMENT)
        : 0.0;
  }

  private double masklessExposureSetupTime(LithographyRequestDto request) {
    return isMaskless(request.aligner())
        ? findSetupTime(request, ProcessDefinition.MASKLESS_EXPOSURE)
        : 0.0;
  }

  private double postBakeSetupTime(LithographyRequestDto request) {
    return findSetupTime(request, ProcessDefinition.POST_BAKE);
  }

  private double developerSetupTime(LithographyRequestDto request) {
    return findSetupTime(request, ProcessDefinition.DEVELOPER);
  }

  private double dryingSetupTime(LithographyRequestDto request) {
    return findSetupTime(request, ProcessDefinition.DRYING);
  }

  private double hardBakeSetupTime(LithographyRequestDto request) {
    return findSetupTime(request, ProcessDefinition.HARD_BAKE);
  }

  // Labor Time Methods
  private double hmdsPreSoakLaborTime(LithographyRequestDto request) {
    return findLaborTime(request, ProcessDefinition.HMDS_PRE_SOAK);
  }

  private double spinCoatLaborTime(LithographyRequestDto request) {
    return findLaborTime(request, ProcessDefinition.SPIN_COAT);
  }

  private double softbakeLaborTime(LithographyRequestDto request) {
    return findLaborTime(request, ProcessDefinition.SOFT_BAKE);
  }

  private double rehydrationLaborTime(LithographyRequestDto request) {
    return findLaborTime(request, ProcessDefinition.REHYDRATION);
  }

  private double contactAlignmentLaborTime(LithographyRequestDto request) {
    return !isMaskless(request.aligner())
        ? findLaborTime(request, ProcessDefinition.CONTACT_ALIGNMENT)
        : 0.0;
  }

  private double contactExposureLaborTime(LithographyRequestDto request) {
    return !isMaskless(request.aligner())
        ? findLaborTime(request, ProcessDefinition.CONTACT_EXPOSURE)
        : 0.0;
  }

  private double masklessAlignmentLaborTime(LithographyRequestDto request) {
    return isMaskless(request.aligner())
        ? findLaborTime(request, ProcessDefinition.MASKLESS_ALIGNMENT)
        : 0.0;
  }

  private double masklessExposureLaborTime(LithographyRequestDto request) {
    return isMaskless(request.aligner())
        ? findLaborTime(request, ProcessDefinition.MASKLESS_EXPOSURE)
        : 0.0;
  }

  private double postBakeLaborTime(LithographyRequestDto request) {
    return findLaborTime(request, ProcessDefinition.POST_BAKE);
  }

  private double developerLaborTime(LithographyRequestDto request) {
    return findLaborTime(request, ProcessDefinition.DEVELOPER);
  }

  private double dryingLaborTime(LithographyRequestDto request) {
    return findLaborTime(request, ProcessDefinition.DRYING);
  }

  private double hardBakeLaborTime(LithographyRequestDto request) {
    return findLaborTime(request, ProcessDefinition.HARD_BAKE);
  }

  // Lookup Methods
  private double findProcessTime(LithographyRequestDto request, ProcessDefinition step) {
    return lithographyDataRepository.findFieldByNameAndField(
        request.name(),
        ProcessDefinition.PROCESS_TIME.getValue(),
        step.getValue()
    ).orElse(0.0);
  }

  private boolean checkProcessTimeIsZero(LithographyRequestDto request, ProcessDefinition step) {
    if (step == ProcessDefinition.HMDS) {
      return findProcessTime(request, ProcessDefinition.HMDS_PRE_SOAK) == 0.0;
    }
    return findProcessTime(request, step) == 0.0;
  }

  private double findSetupTime(LithographyRequestDto request, ProcessDefinition step) {
    return lithographyDataRepository.findFieldByNameAndField(
        request.name(),
        ProcessDefinition.SETUP_TAKEDOWN.getValue(),
        step.getValue()
    ).orElse(0.0);
  }

  private double findLaborTime(LithographyRequestDto request, ProcessDefinition step) {
    return lithographyDataRepository.findFieldByNameAndField(
        request.name(),
        ProcessDefinition.LABOR_TIME.getValue(),
        step.getValue()
    ).orElse(0.0);
  }

  private double getEffectiveProcess(ProcessDefinition parameter) {
    return lithographyRepository.findEffectiveProcessBySettingsAndStepName(
        PROCESS_NAME,
        ProcessDefinition.OTHER_POWER.getValue(),
        parameter.getValue()
    ).orElse(0.0);
  }

  private double getEffectiveLatent(ProcessDefinition parameter) {
    return lithographyRepository.findEffectiveLatentBySettingsAndStepName(
        PROCESS_NAME,
        ProcessDefinition.OTHER_POWER.getValue(),
        parameter.getValue()
    ).orElse(0.0);
  }

  private boolean isMaskless(String alignerType) {
    return alignerType.equalsIgnoreCase("snf_heidelberg");
  }

  private Predicate<String> isSNF() {
    return str -> str != null && str.toLowerCase().contains(SNF_LETTER);
  }

  private Double findLithographyValue(ProcessDefinition parameter) {
    return lithographyRepository.findValueByNameAndField(
        PROCESS_NAME,
        parameter.getValue()
    ).orElse(0.0);
  }

  private double findRunSize(String processName, ProcessStep step) {
    return lithographyDataRepository.findFieldByNameAndField(
        processName,
        ProcessDefinition.RUN_SIZE.getValue(),
        step.parameter().getValue()
    ).orElse(0.0);
  }

  private ApiResponseDto<ResultResponseDto> createSuccessResponse(ResultResponseDto response) {
    return ApiResponseDto.<ResultResponseDto>builder()
        .status(200)
        .message("Calculate lithography result successfully")
        .data(response)
        .build();
  }

  private enum TimeCalculationType {
    PROCESS(ProcessStep::processTimeFunc),
    SETUP(ProcessStep::setupTimeFunc),
    LABOR(ProcessStep::laborTimeFunc);

    private final Function<ProcessStep, Function<LithographyRequestDto, Double>> timeFunction;

    TimeCalculationType(
        Function<ProcessStep, Function<LithographyRequestDto, Double>> timeFunction) {
      this.timeFunction = timeFunction;
    }

    Function<LithographyRequestDto, Double> getTimeFunction(ProcessStep step) {
      return timeFunction.apply(step);
    }
  }

  private record ProcessStep(
      String name,
      ProcessDefinition parameter,
      Function<LithographyRequestDto, Double> processTimeFunc,
      Function<LithographyRequestDto, Double> setupTimeFunc,
      Function<LithographyRequestDto, Double> laborTimeFunc
  ) {

  }

  private record HeaterStep(
      ProcessDefinition parameter,
      Function<LithographyRequestDto, Double> temperatureFunc,
      Function<LithographyRequestDto, Double> processTimeFunc,
      Function<LithographyRequestDto, Double> setupTimeFunc
  ) {

  }

  private record CalculationStep(
      ProcessDefinition parameter,
      Function<LithographyRequestDto, Double> processTimeFunc,
      Function<LithographyRequestDto, Double> setupTimeFunc
  ) {

  }

  private record MaterialParameters(
      ProcessDefinition material,
      ProcessDefinition processStep
  ) {

  }
}

