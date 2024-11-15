package com.mattelogic.inchfab.core.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(name = "projects")
public class Project {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne
  @JoinColumn(name = "company_id")
  private Company company;
  private String requesterId;
  private String requesterName;
  private String submitterId;
  private String submitterName;
  private String rootFolderId;
  private String projectFolderId;
  private String uploadFile;
  private String name;
  private Integer waferSize;
  private String substrateType;
  private Double laborCost;
  private Double electricalCost;
  private Double totalTime;
  private Double totalTimeCost;
  private Double totalLaborCost;
  private Double totalPeriodicCost;
  private Double totalPowerCost;
  private Double totalGasCost;
  private Double totalTargetMaterialCost;
  private Double totalWetEtchantCost;
  private Double totalLithographyReagentCost;
  private Double totalMetrologyInspectionCost;
  private Double totalExternalProcessCost;
  private Double totalManuallyInputProcessCost;
  private Double totalSubstrateCost;
  @Type(JsonBinaryType.class)
  @Column(columnDefinition = "json")
  private JsonNode projectStep;
  private Boolean status;
  @Column(name = "created_at", nullable = false, updatable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;
  @Column(name = "updated_at", nullable = false)
  @UpdateTimestamp
  private LocalDateTime updatedAt;

  public static Project create(
      Company company,
      String name,
      String requesterId,
      String requesterName,
      Integer waferSize,
      Double laborCost,
      Double electricalCost
  ) {
    Project project = new Project();
    project.setCompany(company);
    project.setRequesterId(requesterId);
    project.setRequesterName(requesterName);
    project.setName(name);
    project.setWaferSize(waferSize);
    project.setStatus(true);
    project.setLaborCost(laborCost);
    project.setElectricalCost(electricalCost);
    project.setTotalTime(0.0);
    project.setTotalTimeCost(0.0);
    project.setTotalLaborCost(0.0);
    project.setTotalPeriodicCost(0.0);
    project.setTotalPowerCost(0.0);
    project.setTotalGasCost(0.0);
    project.setTotalTargetMaterialCost(0.0);
    project.setTotalWetEtchantCost(0.0);
    project.setTotalLithographyReagentCost(0.0);
    project.setTotalMetrologyInspectionCost(0.0);
    project.setTotalExternalProcessCost(0.0);
    project.setTotalManuallyInputProcessCost(0.0);
    project.setTotalSubstrateCost(0.0);
    project.setCreatedAt(null);
    project.setUpdatedAt(null);
    return project;
  }

  public static Project copyFrom(Project project, List<Project> existingProjects) {
    String newName = generateCopyName(project.getName(), existingProjects);

    Project copiedProject = new Project();
    copiedProject.setCompany(project.getCompany());
    copiedProject.setRequesterId(project.getRequesterId());
    copiedProject.setRequesterName(project.getRequesterName());
    copiedProject.setSubmitterId(project.getSubmitterId());
    copiedProject.setSubmitterName(project.getSubmitterName());
    copiedProject.setRootFolderId(project.getRootFolderId());
    copiedProject.setProjectFolderId(project.getProjectFolderId());
    copiedProject.setUploadFile(project.getUploadFile());
    copiedProject.setName(newName);
    copiedProject.setWaferSize(project.getWaferSize());
    copiedProject.setSubstrateType(project.getSubstrateType());
    copiedProject.setStatus(false);
    copiedProject.setLaborCost(project.getLaborCost());
    copiedProject.setElectricalCost(project.getElectricalCost());
    copiedProject.setTotalTime(project.getTotalTime());
    copiedProject.setTotalTimeCost(project.getTotalTimeCost());
    copiedProject.setTotalLaborCost(project.getTotalLaborCost());
    copiedProject.setTotalPeriodicCost(project.getTotalPeriodicCost());
    copiedProject.setTotalPowerCost(project.getTotalPowerCost());
    copiedProject.setTotalGasCost(project.getTotalGasCost());
    copiedProject.setTotalTargetMaterialCost(project.getTotalTargetMaterialCost());
    copiedProject.setTotalWetEtchantCost(project.getTotalWetEtchantCost());
    copiedProject.setTotalLithographyReagentCost(project.getTotalLithographyReagentCost());
    copiedProject.setTotalMetrologyInspectionCost(project.getTotalMetrologyInspectionCost());
    copiedProject.setTotalExternalProcessCost(project.getTotalExternalProcessCost());
    copiedProject.setTotalManuallyInputProcessCost(project.getTotalManuallyInputProcessCost());
    copiedProject.setTotalSubstrateCost(project.getTotalSubstrateCost());
    copiedProject.setCreatedAt(null);
    copiedProject.setUpdatedAt(null);
    copiedProject.setProjectStep(
        project.getProjectStep() == null || project.getProjectStep().isEmpty()
            ? objectMapper.createArrayNode()
            : project.getProjectStep());

    return copiedProject;
  }

  private static String generateCopyName(String originalName, List<Project> existingProjects) {
    Pattern copyPattern = Pattern.compile("^(.+?)(?:\\s+-\\s+Copy\\s+(\\d+))*$");
    Matcher matcher = copyPattern.matcher(originalName);

    if (!matcher.matches()) {
      return originalName + " - Copy 1";
    }

    String baseName = matcher.group(1);

    int highestCopyNumber = existingProjects.stream()
        .map(Project::getName)
        .filter(name -> name.startsWith(baseName))
        .map(name -> {
          Pattern numberPattern = Pattern.compile(
              Pattern.quote(baseName) + "\\s+-\\s+Copy\\s+(\\d+)$");
          Matcher m = numberPattern.matcher(name);
          if (!m.matches()) {
            return 0;
          }
          return Integer.parseInt(m.group(1));
        })
        .max(Integer::compareTo)
        .orElse(0);

    return baseName + " - Copy " + (highestCopyNumber + 1);
  }

  public void clearProjectStep() {
    this.projectStep = null;
  }

  public Double calculateTotalCost() {
    return Stream.of(
            totalLaborCost,
            totalPeriodicCost,
            totalPowerCost,
            totalGasCost,
            totalTargetMaterialCost,
            totalWetEtchantCost,
            totalLithographyReagentCost,
            totalMetrologyInspectionCost,
            totalExternalProcessCost,
            totalManuallyInputProcessCost,
            totalSubstrateCost
        )
        .filter(Objects::nonNull)
        .reduce(0.0, Double::sum);
  }
}