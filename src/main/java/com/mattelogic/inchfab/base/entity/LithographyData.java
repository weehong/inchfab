package com.mattelogic.inchfab.base.entity;

import java.util.List;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "lithography-data")
public record LithographyData(
    @Id
    String id,
    Map<String, Boolean> laborAttentionNeeded,
    List<Photoresist> photoresists
) {

  public record Photoresist(
      String name,
      String resistType,
      Double maxThickness,
      Temperature temperatures,
      ProcessTime processTime,
      ProcessTime setupTakedown,
      ProcessTime laborTime,
      ProcessTime runSize,
      Material materials
  ) {

    public record Temperature(
        Object softbake,  // Can be Integer or Map<String, Integer>
        Object postbake,  // Can be Integer or Map<String, Integer>
        Object hardbake   // Can be Integer or Map<String, Integer>
    ) {

    }

    public record ProcessTime(
        Integer hmdsPreSoak,
        Integer spinCoat,
        Integer softbake,
        Integer rehydration,
        Integer contactAlignment,
        Integer contactExposure,
        Integer masklessAlignment,
        Integer masklessExposure,
        Integer postbake,
        Integer developer,
        Integer drying,
        Integer hardbake
    ) {

    }

    public record Material(
        String hmds,
        String photoresist,
        String developer
    ) {

    }
  }
}