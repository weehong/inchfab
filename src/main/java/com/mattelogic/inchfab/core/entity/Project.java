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
import java.math.BigDecimal;
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
  private BigDecimal laborCost;
  private BigDecimal electricalCost;
  private BigDecimal totalTime;
  private BigDecimal totalTimeCost;
  private BigDecimal totalLaborCost;
  private BigDecimal totalPeriodicCost;
  private BigDecimal totalPowerCost;
  private BigDecimal totalGasCost;
  private BigDecimal totalTargetMaterialCost;
  private BigDecimal totalWetEtchantCost;
  private BigDecimal totalLithographyReagentCost;
  private BigDecimal totalMetrologyInspectionCost;
  private BigDecimal totalExternalProcessCost;
  private BigDecimal totalManuallyInputProcessCost;
  private BigDecimal totalSubstrateCost;
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
      Integer waferSize
  ) {
    Project project = new Project();
    project.setCompany(company);
    project.setRequesterId(requesterId);
    project.setRequesterName(requesterName);
    project.setName(name);
    project.setWaferSize(waferSize);
    project.setStatus(true);
    project.setLaborCost(BigDecimal.ZERO);
    project.setElectricalCost(BigDecimal.ZERO);
    project.setTotalTime(BigDecimal.ZERO);
    project.setTotalTimeCost(BigDecimal.ZERO);
    project.setTotalLaborCost(BigDecimal.ZERO);
    project.setTotalPeriodicCost(BigDecimal.ZERO);
    project.setTotalPowerCost(BigDecimal.ZERO);
    project.setTotalGasCost(BigDecimal.ZERO);
    project.setTotalTargetMaterialCost(BigDecimal.ZERO);
    project.setTotalWetEtchantCost(BigDecimal.ZERO);
    project.setTotalLithographyReagentCost(BigDecimal.ZERO);
    project.setTotalMetrologyInspectionCost(BigDecimal.ZERO);
    project.setTotalExternalProcessCost(BigDecimal.ZERO);
    project.setTotalManuallyInputProcessCost(BigDecimal.ZERO);
    project.setTotalSubstrateCost(BigDecimal.ZERO);
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
    // Pattern to extract base name and copy number from original name
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
          // Pattern to extract copy number from existing project names
          Pattern numberPattern = Pattern.compile(
              Pattern.quote(baseName) + "\\s+-\\s+Copy\\s+(\\d+)$");
          Matcher m = numberPattern.matcher(name);
          if (!m.matches()) {
            return 0;  // Not a copy
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

  public BigDecimal calculateTotalCost() {
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
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
