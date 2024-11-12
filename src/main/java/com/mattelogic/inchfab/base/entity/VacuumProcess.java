package com.mattelogic.inchfab.base.entity;

import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "vacuum-process")
public record VacuumProcess(
    @Id
    String id,
    String type,
    List<Process> processes
) {

  public record Process(
      String name,
      List<Subprocess> subprocesses,
      Parameters parameters
  ) {

  }

  public record Subprocess(
      String name,
      Parameters parameters
  ) {

  }

  public record Parameters(
      Double baseProcessRate,
      Integer sf6,
      Integer o2,
      Integer ar,
      Integer cl2,
      Integer bcl3,
      Integer he,
      Integer c4f8,
      Integer cf4,
      Integer sih4,
      Integer n2,
      Integer sih2cl2,
      Integer n2o,
      Integer nh3,
      Integer tma,
      Integer h2o,
      Integer tdmat,
      Integer icpPower,
      Integer substratePower,
      Integer pressure,
      Integer temperature,
      Double stepTime
  ) {

  }

  public record StepTimeResult(
      String processName,
      String subprocessName,
      Double stepTime
  ) {

  }
}