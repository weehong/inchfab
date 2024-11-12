package com.mattelogic.inchfab.core.model;

public record ProjectStep(
    Long sequenceId,
    String processType,
    String name,
    String description,
    String photoresist,
    String aligner,
    String material,
    String location,
    Double maskArea,
    Double depth,
    Double thickness,
    Double refractiveIndex,
    Double timeWaferHour,
    Double cost,
    Double totalCost,
    Double amount,
    Double lotCharge,
    Double lotSize,
    Double rate,
    Double setupCost,
    Double amountRate,
    Double costPerWafer,
    Double laborTime,
    Double periodicCost,
    Double powerCost,
    Double gasCost,
    Double targetMaterialCost,
    Double wetEtchantCost,
    Double lithographyReagentCost,
    Integer filmStress
) {

}
