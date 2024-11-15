package com.mattelogic.inchfab.core.mapper;

import com.mattelogic.inchfab.core.dtos.request.ProjectRequestDto;
import com.mattelogic.inchfab.core.dtos.response.ProjectResponseDto;
import com.mattelogic.inchfab.core.dtos.response.ResultResponseDto;
import com.mattelogic.inchfab.core.entity.Company;
import com.mattelogic.inchfab.core.entity.Project;
import com.mattelogic.inchfab.core.exception.CompanyNotFoundException;
import com.mattelogic.inchfab.core.repository.CompanyRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectMapper {

  private final CompanyRepository companyRepository;

  public Project toEntity(
      ProjectRequestDto request,
      Double laborCost,
      Double electricalCost
  ) {
    Company company = companyRepository.findById(request.companyId())
        .orElseThrow(() -> new CompanyNotFoundException(request.companyId()));

    Project project = Project.create(
        company,
        request.name(),
        request.requesterId(),
        request.requesterName(),
        request.waferSize(),
        laborCost,
        electricalCost
    );

    updateProjectFields(project, request);
    return project;
  }

  public void updateProjectWithCalculationResult(Project project, ResultResponseDto result) {
    Optional.ofNullable(result).ifPresent(r -> {
      project.setTotalTime(r.totalTime());
      project.setTotalLaborCost(r.laborTime());
      project.setTotalPeriodicCost(r.periodicCost());
      project.setTotalPowerCost(r.power());
      project.setTotalGasCost(r.gas());
      project.setTotalTargetMaterialCost(r.targetMaterial());
      project.setTotalWetEtchantCost(r.wetEtchant());
      project.setTotalLithographyReagentCost(r.lithographyReagent());
      project.setTotalMetrologyInspectionCost(r.metrologyInspectionCost());
      project.setTotalExternalProcessCost(r.externalCost());
      project.setTotalManuallyInputProcessCost(r.manualCost());
      project.setTotalSubstrateCost(r.substrateCost());
    });
  }

  public ProjectResponseDto toResponseDto(Project project) {
    return Optional.ofNullable(project)
        .map(p -> new ProjectResponseDto(
            p.getId(),
            p.getCompany().getId(),
            p.getRequesterId(),
            p.getRequesterName(),
            p.getSubmitterId(),
            p.getSubmitterName(),
            p.getName(),
            p.getWaferSize(),
            p.getRootFolderId(),
            p.getProjectFolderId(),
            p.getUploadFile(),
            p.getSubstrateType(),
            p.getLaborCost(),
            p.getElectricalCost(),
            p.getTotalLaborCost(),
            p.getTotalTime(),
            p.getTotalPeriodicCost(),
            p.getTotalPowerCost(),
            p.getTotalGasCost(),
            p.getTotalTargetMaterialCost(),
            p.getTotalWetEtchantCost(),
            p.getTotalLithographyReagentCost(),
            p.getTotalMetrologyInspectionCost(),
            p.getTotalExternalProcessCost(),
            p.getTotalManuallyInputProcessCost(),
            p.getTotalSubstrateCost(),
            p.calculateTotalCost(),
            p.getStatus(),
            p.getProjectStep(),
            p.getCreatedAt(),
            p.getUpdatedAt()
        ))
        .orElseThrow(() -> new IllegalArgumentException("Project cannot be null"));
  }

  private void updateProjectFields(
      Project project,
      ProjectRequestDto request) {
    Optional.ofNullable(request.submitterId()).ifPresent(project::setSubmitterId);
    Optional.ofNullable(request.submitterName()).ifPresent(project::setSubmitterName);
    Optional.ofNullable(request.rootFolderId()).ifPresent(project::setRootFolderId);
    Optional.ofNullable(request.projectFolderId()).ifPresent(project::setProjectFolderId);
    Optional.ofNullable(request.uploadFile()).ifPresent(project::setUploadFile);
    Optional.ofNullable(request.waferSize()).ifPresent(project::setWaferSize);
    Optional.ofNullable(request.substrateType()).ifPresent(project::setSubstrateType);
    Optional.ofNullable(request.totalTime()).ifPresent(project::setTotalTime);
    Optional.ofNullable(request.totalLaborCost()).ifPresent(project::setTotalLaborCost);
    Optional.ofNullable(request.totalPeriodicCost()).ifPresent(project::setTotalPeriodicCost);
    Optional.ofNullable(request.totalPowerCost()).ifPresent(project::setTotalPowerCost);
    Optional.ofNullable(request.totalGasCost()).ifPresent(project::setTotalGasCost);
    Optional.ofNullable(request.totalTargetMaterialCost())
        .ifPresent(project::setTotalTargetMaterialCost);
    Optional.ofNullable(request.totalWetEtchantCost()).ifPresent(project::setTotalWetEtchantCost);
    Optional.ofNullable(request.totalLithographyReagentCost())
        .ifPresent(project::setTotalLithographyReagentCost);
    Optional.ofNullable(request.totalMetrologyInspectionCost())
        .ifPresent(project::setTotalMetrologyInspectionCost);
    Optional.ofNullable(request.totalExternalProcessCost())
        .ifPresent(project::setTotalExternalProcessCost);
    Optional.ofNullable(request.totalManuallyInputProcessCost())
        .ifPresent(project::setTotalManuallyInputProcessCost);
    Optional.ofNullable(request.totalSubstrateCost()).ifPresent(project::setTotalSubstrateCost);
    Optional.ofNullable(request.status()).ifPresent(project::setStatus);
    project.setProjectStep(request.projectStep());
  }

  public void updateEntityFromDto(Project project, ProjectRequestDto request) {
    if (shouldUpdateCompany(project, request)) {
      updateCompany(project, request);
    }
    updateProjectFields(project, request);
  }

  private boolean shouldUpdateCompany(Project project, ProjectRequestDto request) {
    return Optional.ofNullable(request.companyId())
        .map(newCompanyId -> !newCompanyId.equals(project.getCompany().getId()))
        .orElse(false);
  }

  private void updateCompany(Project project, ProjectRequestDto request) {
    Company newCompany = companyRepository.findById(request.companyId())
        .orElseThrow(() -> new CompanyNotFoundException(request.companyId()));
    project.setCompany(newCompany);
  }
}