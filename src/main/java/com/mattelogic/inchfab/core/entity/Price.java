package com.mattelogic.inchfab.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "project_pricing")
public class Price {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "project_id", nullable = false)
  private Long projectId;

  @Column(name = "engineering_hours", nullable = false)
  private Integer engineeringHours;

  @Column(name = "engineering_rate", nullable = false, precision = 10, scale = 2)
  private BigDecimal engineeringRate;

  @Column(name = "mask_unit_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal maskUnitPrice;

  @Column(name = "mask_count", nullable = false)
  private Integer maskCount;

  @Column(name = "min_lot_size", nullable = false)
  private Integer minLotSize;

  @Column(name = "process_margin", nullable = false, precision = 5, scale = 2)
  private BigDecimal processMargin;

  @Column(name = "process_in_fab_cost", nullable = false, precision = 10, scale = 2)
  private BigDecimal processInFabCost;

  @Column(name = "process_third_party_cost", nullable = false, precision = 10, scale = 2)
  private BigDecimal processThirdPartyCost;

  @Column(name = "process_wafer_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal processWaferPrice;

  @Column(name = "dev_margin", nullable = false, precision = 5, scale = 2)
  private BigDecimal devMargin;

  @Column(name = "dev_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal devPrice;

  @Column(name = "prod_100_margin", nullable = false, precision = 5, scale = 2)
  private BigDecimal prod100Margin;

  @Column(name = "prod_100_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal prod100Price;

  @Column(name = "prod_1000_margin", nullable = false, precision = 5, scale = 2)
  private BigDecimal prod1000Margin;

  @Column(name = "prod_1000_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal prod1000Price;

  @Column(name = "total_engineering", nullable = false, precision = 10, scale = 2)
  private BigDecimal totalEngineering;

  @Column(name = "total_wafer", nullable = false, precision = 10, scale = 2)
  private BigDecimal totalWafer;

  @Column(name = "total_mask", nullable = false, precision = 10, scale = 2)
  private BigDecimal totalMask;

  @Column(name = "total_project", nullable = false, precision = 10, scale = 2)
  private BigDecimal totalProject;

  @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
  @CreationTimestamp
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
  @UpdateTimestamp
  private LocalDateTime updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id", referencedColumnName = "id", insertable = false, updatable = false)
  private Project project;
}