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
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

  @Transactional
  @Override
  public ApiResponseDto<ProjectResponseDto> create(ProjectRequestDto projectRequestDto) {
    try {
      log.debug("Creating new project with name: {}", projectRequestDto.name());
      Project project = projectMapper.toEntity(projectRequestDto);

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
        projectMapper.updateProjectWithCalculationResult(project, ResultResponseDto.builder().build());
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

  private void calculateAndUpdateProjectCosts(Project project, String substrateType, Integer waferSize) {
    try {
      List<ProjectStep> projectSteps = convertJsonNodesToProjectSteps(project.getProjectStep());

      CostRequestDto costRequestDto = new CostRequestDto(
          substrateType,
          waferSize,
          projectSteps
      );

      ResultResponseDto resultResponseDto = processCostCalculatorService
          .calculatePriceTotalResult(costRequestDto);

      Map<Long, ResultResponseDto> unitCostsMap = new HashMap<>();
      if (resultResponseDto.unitTotalCosts() != null) {
        for (ResultResponseDto unitCost : resultResponseDto.unitTotalCosts()) {
          // Assuming the processName contains or matches the sequence ID
          for (ProjectStep step : projectSteps) {
            if (unitCost.processName().equals(step.processType())) {
              unitCostsMap.put(step.sequenceId(), unitCost);
              break;
            }
          }
        }
      }

      ArrayNode updatedProjectSteps = objectMapper.createArrayNode();
      JsonNode originalSteps = project.getProjectStep();

      for (JsonNode stepNode : originalSteps) {
        ObjectNode updatedStep = (ObjectNode) stepNode.deepCopy();
        Long sequenceId = stepNode.get("sequenceId").asLong();

        ResultResponseDto unitCost = unitCostsMap.get(sequenceId);
        if (unitCost != null) {
          ObjectNode costDetails = objectMapper.createObjectNode()
              .put("laborTime", nullToZero(unitCost.laborTime()))
              .put("periodicCost", nullToZero(unitCost.periodicCost()))
              .put("power", nullToZero(unitCost.power()))
              .put("gas", nullToZero(unitCost.gas()))
              .put("targetMaterial", nullToZero(unitCost.targetMaterial()))
              .put("wetEtchant", nullToZero(unitCost.wetEtchant()))
              .put("lithographyReagent", nullToZero(unitCost.lithographyReagent()))
              .put("metrologyInspectionCost", nullToZero(unitCost.metrologyInspectionCost()))
              .put("externalCost", nullToZero(unitCost.externalCost()))
              .put("manualCost", nullToZero(unitCost.manualCost()))
              .put("substrateCost", nullToZero(unitCost.substrateCost()))
              .put("totalTime", nullToZero(unitCost.totalTime()))
              .put("totalCost", nullToZero(unitCost.totalCost()));

          updatedStep.set("costDetails", costDetails);
        }
        updatedProjectSteps.add(updatedStep);
      }

      project.setProjectStep(updatedProjectSteps);
      projectMapper.updateProjectWithCalculationResult(project, resultResponseDto);

    } catch (Throwable e) {
      log.error("Error calculating price total result: {}", e.getMessage());
      throw new ProjectCostCalculationException("Failed to calculate price total", e);
    }
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