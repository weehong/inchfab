package com.mattelogic.inchfab.domain.service;

import com.mattelogic.inchfab.core.dtos.request.CostRequestDto;
import com.mattelogic.inchfab.core.exception.UnsupportedProcessTypeException;
import com.mattelogic.inchfab.core.model.ProjectStep;
import com.mattelogic.inchfab.core.dtos.response.ResultResponseDto;
import com.mattelogic.inchfab.domain.enums.ProcessDefinition;
import java.util.EnumMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProcessServiceRegistryImpl {

  private final Map<ProcessDefinition, CalculateService<ProjectStep, CostRequestDto, ?>> serviceMap;

  @Autowired
  public ProcessServiceRegistryImpl(
      DrieServiceImpl drieService,
      RieServiceImpl rieService,
      AldServiceImpl aldService,
      IcpcvdServiceImpl icpcvdService,
      LpcvdServiceImpl lpcvdService,
      MetrologyInspectionServiceImpl metrologyInspectionService,
      MagnetronSputteringServiceImpl magnetronService,
      WetProcessServiceImpl wetProcessService,
      LithographyServiceImpl lithographyService,
      ExternalProcessServiceImpl externalProcessService,
      SubstrateServiceImpl substrateService
  ) {
    serviceMap = new EnumMap<>(ProcessDefinition.class);
    serviceMap.put(ProcessDefinition.DRIE, drieService);
    serviceMap.put(ProcessDefinition.RIE, rieService);
    serviceMap.put(ProcessDefinition.ALD, aldService);
    serviceMap.put(ProcessDefinition.ICP_CVD, icpcvdService);
    serviceMap.put(ProcessDefinition.LP_CVD, lpcvdService);
    serviceMap.put(ProcessDefinition.METROLOGY_INSPECTION, metrologyInspectionService);
    serviceMap.put(ProcessDefinition.MAGNETRON_SPUTTER, magnetronService);
    serviceMap.put(ProcessDefinition.WET_PROCESS, wetProcessService);
    serviceMap.put(ProcessDefinition.LITHOGRAPHY, lithographyService);
    serviceMap.put(ProcessDefinition.EXTERNAL_PROCESS, externalProcessService);
    serviceMap.put(ProcessDefinition.SUBSTRATE, substrateService);
  }

  public ResultResponseDto calculate(ProcessDefinition processDefinition, ProjectStep step,
      CostRequestDto request) {
    CalculateService<ProjectStep, CostRequestDto, ?> service = serviceMap.get(processDefinition);
    if (service == null) {
      throw new UnsupportedProcessTypeException(
          "Process type not implemented: " + processDefinition,
          new IllegalStateException()
      );
    }
    return service.calculate(step, request);
  }
}