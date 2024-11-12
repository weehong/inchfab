package com.mattelogic.inchfab.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mattelogic.inchfab.common.dto.ApiResponseDto;
import com.mattelogic.inchfab.core.dtos.request.CostRequestDto;
import com.mattelogic.inchfab.core.dtos.request.ProjectRequestDto;
import com.mattelogic.inchfab.core.dtos.response.ProjectResponseDto;
import com.mattelogic.inchfab.core.dtos.response.ResultResponseDto;
import com.mattelogic.inchfab.core.entity.Project;
import com.mattelogic.inchfab.core.exception.DuplicateProjectException;
import com.mattelogic.inchfab.core.exception.ProjectCostCalculationException;
import com.mattelogic.inchfab.core.exception.ProjectNotFoundException;
import com.mattelogic.inchfab.core.exception.ProjectStepConversionException;
import com.mattelogic.inchfab.core.mapper.ProjectMapper;
import com.mattelogic.inchfab.core.model.ProjectStep;
import com.mattelogic.inchfab.core.repository.ProjectRepository;
import com.mattelogic.inchfab.domain.repository.EssentialRepository;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements GenericService<ProjectResponseDto, ProjectRequestDto> {

  private final ProjectRepository projectRepository;
  private final ProjectMapper projectMapper;
  private final ObjectMapper objectMapper;
  private final ProcessCostCalculatorServiceImpl processCostCalculatorService;
  private final EssentialRepository essentialRepository;

  @Transactional
  @Override
  public ApiResponseDto<ProjectResponseDto> create(ProjectRequestDto projectRequestDto) {
    try {
      log.debug("Creating new project with name: {}", projectRequestDto.name());

      double laborCost = essentialRepository.findLaborCost();
      double electricalCost = essentialRepository.findElectricityCost();

      Project project = projectMapper.toEntity(projectRequestDto, laborCost, electricalCost);

      if (project.getProjectStep() == null || project.getProjectStep().isEmpty()) {
        project.setProjectStep(objectMapper.createArrayNode());
      } else {
        calculateAndUpdateProjectCosts(
            project,
            projectRequestDto.substrateType(),
            projectRequestDto.waferSize()
        );
      }

      project.setProjectStep(
          project.getProjectStep() == null || project.getProjectStep().isEmpty()
              ? objectMapper.createArrayNode()
              : project.getProjectStep());

      project = projectRepository.save(project);

      return ApiResponseDto.<ProjectResponseDto>builder()
          .status(HttpStatus.CREATED.value())
          .message("Project created successfully")
          .data(projectMapper.toResponseDto(project))
          .build();
    } catch (DataIntegrityViolationException e) {
      if (e.getMessage().contains("projects_name_key")) {
        throw new DuplicateProjectException(
            String.format("Project with name '%s' already exists", projectRequestDto.name()));
      }
      throw e;
    }
  }

  @Override
  public ApiResponseDto<List<ProjectResponseDto>> all() {
    log.debug("Fetching all projects");
    List<Project> projects = projectRepository.findAll();
    return ApiResponseDto.<List<ProjectResponseDto>>builder()
        .status(HttpStatus.OK.value())
        .message("Projects fetched successfully")
        .data(projects.stream()
            .map(projectMapper::toResponseDto)
            .toList())
        .build();
  }

  @Override
  public ApiResponseDto<ProjectResponseDto> getById(Long id) {
    log.debug("Fetching project with id: {}", id);
    Project project = projectRepository.findById(id)
        .orElseThrow(() -> new ProjectNotFoundException(id));

    return ApiResponseDto.<ProjectResponseDto>builder()
        .status(HttpStatus.OK.value())
        .message("Project fetched successfully")
        .data(projectMapper.toResponseDto(project))
        .build();
  }

  @Override
  @Transactional
  public ApiResponseDto<ProjectResponseDto> update(Long id, ProjectRequestDto projectRequestDto) {
    try {
      log.debug("Updating project with id: {} and name: {}", id, projectRequestDto.name());

      Project project = projectRepository.findById(id)
          .orElseThrow(() -> new ProjectNotFoundException(id));

      projectMapper.updateEntityFromDto(project, projectRequestDto);

      if (project.getProjectStep() != null && !project.getProjectStep().isEmpty()) {
        calculateAndUpdateProjectCosts(
            project,
            projectRequestDto.substrateType(),
            projectRequestDto.waferSize()
        );
      } else {
        project.setProjectStep(objectMapper.createArrayNode());
        projectMapper.updateProjectWithCalculationResult(project,
            ResultResponseDto.builder().build());
      }

      Project updatedProject = projectRepository.save(project);

      return ApiResponseDto.<ProjectResponseDto>builder()
          .status(HttpStatus.OK.value())
          .message("Project updated successfully")
          .data(projectMapper.toResponseDto(updatedProject))
          .build();
    } catch (DataIntegrityViolationException e) {
      if (e.getMessage().contains("projects_name_key")) {
        throw new DuplicateProjectException(
            String.format("Cannot update: Project with name '%s' already exists",
                projectRequestDto.name()));
      }
      throw e;
    }
  }

  @Override
  @Transactional
  public ApiResponseDto<Boolean> delete(Long id) {
    log.debug("Deleting project with id: {}", id);
    if (!projectRepository.existsById(id)) {
      throw new ProjectNotFoundException(id);
    }

    projectRepository.deleteById(id);
    return ApiResponseDto.<Boolean>builder()
        .status(HttpStatus.OK.value())
        .message("Project deleted successfully")
        .data(true)
        .build();
  }

  @Transactional
  public ApiResponseDto<ProjectResponseDto> copyProject(Long id) {
    log.debug("Copying project with id: {}", id);
    Project sourceProject = projectRepository.findById(id)
        .orElseThrow(() -> new ProjectNotFoundException(id));

    List<Project> existingProjects = projectRepository.findAll();
    Project copiedProject = Project.copyFrom(sourceProject, existingProjects);

    if (copiedProject.getProjectStep() != null && !copiedProject.getProjectStep().isEmpty()) {
      calculateAndUpdateProjectCosts(
          copiedProject,
          copiedProject.getSubstrateType(),
          copiedProject.getWaferSize()
      );
    }

    Project savedProject = projectRepository.save(copiedProject);

    return ApiResponseDto.<ProjectResponseDto>builder()
        .status(HttpStatus.CREATED.value())
        .message("Project copied successfully")
        .data(projectMapper.toResponseDto(savedProject))
        .build();
  }

  private void calculateAndUpdateProjectCosts(Project project, String substrateType,
      Integer waferSize) {
    try {
      List<ProjectStep> projectSteps = convertJsonNodesToProjectSteps(project.getProjectStep());

      CostRequestDto costRequestDto = new CostRequestDto(
          substrateType,
          waferSize,
          projectSteps
      );

      ResultResponseDto resultResponseDto = processCostCalculatorService
          .calculatePriceTotalResult(costRequestDto);

      Map<Long, ResultResponseDto> unitCostsMap = createUnitCostsMap(resultResponseDto,
          projectSteps);
      updateProjectStepsWithCosts(project, unitCostsMap);
      projectMapper.updateProjectWithCalculationResult(project, resultResponseDto);

    } catch (Throwable e) {
      log.error("Error calculating price total result: {}", e.getMessage());
      throw new ProjectCostCalculationException("Failed to calculate price total", e);
    }
  }

  private Map<Long, ResultResponseDto> createUnitCostsMap(
      ResultResponseDto resultResponseDto,
      List<ProjectStep> projectSteps
  ) {
    Map<String, ResultResponseDto> processTypeToResultMap = new HashMap<>();
    Map<Long, ResultResponseDto> sequenceIdToResultMap = new HashMap<>();

    if (resultResponseDto.unitTotalCosts() != null) {
      resultResponseDto.unitTotalCosts().forEach(unitCost -> {
        if (unitCost.processName() != null) {
          processTypeToResultMap.put(unitCost.processName(), unitCost);
        }
      });
    }

    projectSteps.forEach(step -> {
      ResultResponseDto matchingResult = processTypeToResultMap.get(step.processType());
      if (matchingResult != null) {
        sequenceIdToResultMap.put(step.sequenceId(), matchingResult);
      } else {
        sequenceIdToResultMap.put(step.sequenceId(), ResultResponseDto.builder()
            .processName(step.processType())
            .laborTime(0.0)
            .periodicCost(0.0)
            .power(0.0)
            .gas(0.0)
            .targetMaterial(0.0)
            .wetEtchant(0.0)
            .lithographyReagent(0.0)
            .metrologyInspectionCost(0.0)
            .externalCost(0.0)
            .manualCost(0.0)
            .substrateCost(0.0)
            .totalTime(0.0)
            .build());
      }
    });

    return sequenceIdToResultMap;
  }

  private void updateProjectStepsWithCosts(Project project,
      Map<Long, ResultResponseDto> unitCostsMap) {
    ArrayNode updatedProjectSteps = objectMapper.createArrayNode();
    JsonNode originalSteps = project.getProjectStep();

    for (JsonNode stepNode : originalSteps) {
      ObjectNode updatedStep = stepNode.deepCopy();
      Long sequenceId = stepNode.get("sequenceId").asLong();

      ResultResponseDto unitCost = unitCostsMap.get(sequenceId);

      ObjectNode costDetails = createCostDetailsNode(unitCost);
      updatedStep.set("costDetails", costDetails);

      updatedProjectSteps.add(updatedStep);
    }

    project.setProjectStep(updatedProjectSteps);
  }

  private ObjectNode createCostDetailsNode(ResultResponseDto unitCost) {
    ObjectNode costDetails = objectMapper.createObjectNode();

    Double laborTime = nullToZero(unitCost != null ? unitCost.laborTime() : null);
    Double periodicCost = nullToZero(unitCost != null ? unitCost.periodicCost() : null);
    Double power = nullToZero(unitCost != null ? unitCost.power() : null);
    Double gas = nullToZero(unitCost != null ? unitCost.gas() : null);
    Double targetMaterial = nullToZero(unitCost != null ? unitCost.targetMaterial() : null);
    Double wetEtchant = nullToZero(unitCost != null ? unitCost.wetEtchant() : null);
    Double lithographyReagent = nullToZero(unitCost != null ? unitCost.lithographyReagent() : null);
    Double metrologyInspectionCost = nullToZero(
        unitCost != null ? unitCost.metrologyInspectionCost() : null);
    Double externalCost = nullToZero(unitCost != null ? unitCost.externalCost() : null);
    Double manualCost = nullToZero(unitCost != null ? unitCost.manualCost() : null);
    Double substrateCost = nullToZero(unitCost != null ? unitCost.substrateCost() : null);
    Double totalTime = nullToZero(unitCost != null ? unitCost.totalTime() : null);

    costDetails.put("laborTime", laborTime);
    costDetails.put("periodicCost", periodicCost);
    costDetails.put("power", power);
    costDetails.put("gas", gas);
    costDetails.put("targetMaterial", targetMaterial);
    costDetails.put("wetEtchant", wetEtchant);
    costDetails.put("lithographyReagent", lithographyReagent);
    costDetails.put("metrologyInspectionCost", metrologyInspectionCost);
    costDetails.put("externalCost", externalCost);
    costDetails.put("manualCost", manualCost);
    costDetails.put("substrateCost", substrateCost);
    costDetails.put("totalTime", totalTime);

    return costDetails;
  }

  private List<ProjectStep> convertJsonNodesToProjectSteps(JsonNode jsonNodes) {
    if (jsonNodes == null || !jsonNodes.isArray()) {
      return List.of();
    }

    return StreamSupport.stream(jsonNodes.spliterator(), false)
        .map(node -> {
          try {
            return objectMapper.treeToValue(node, ProjectStep.class);
          } catch (Exception e) {
            log.error("Error converting JsonNode to ProjectStep: {}", e.getMessage());
            throw new ProjectStepConversionException("Failed to convert project steps", e);
          }
        })
        .toList();
  }

  private double nullToZero(Double value) {
    return value != null ? value : 0.0;
  }
}