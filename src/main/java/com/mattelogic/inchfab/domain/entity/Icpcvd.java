package com.mattelogic.inchfab.domain.entity;

import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "icp-cvd")
public record Icpcvd(
    @Id
    String id,
    String name,
    Double gasOverhead,
    Integer waferPerRun,
    Integer runsNeededPerJobStep,
    Integer setupTakedownTime,
    Double periodicCost,
    Double overheadPower,
    List<Setting> settings
) {

  public record Setting(
      String name,
      Double totalEffectiveProcess,
      Double totalEffectiveLatent,
      List<Process> processes
  ) {

    /**
     * Record representing an individual ICP-CVD process with its parameters.
     * <p>
     * Process and latent times measured in seconds. Efficiency expressed as a ratio. Attenuation
     * represents power reduction factor. Shared indicates resource sharing status.
     *
     * @param name             Process identifier (e.g., "icp_coil", "substrate_electrode")
     * @param process          Active process time in seconds
     * @param latent           Latent/idle time in seconds
     * @param efficiency       Power efficiency ratio (0-1)
     * @param attenuation      Power attenuation factor (optional)
     * @param shared           Resource sharing indicator
     * @param effectiveProcess Effective power during active processing
     * @param effectiveLatent  Effective power during latent phase
     */
    public record Process(
        String name,
        Integer process,
        Integer latent,
        Double efficiency,
        Integer attenuation,
        Integer shared,
        Double effectiveProcess,
        Double effectiveLatent
    ) {

    }
  }
}