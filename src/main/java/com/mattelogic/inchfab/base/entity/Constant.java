package com.mattelogic.inchfab.base.entity;

import java.util.List;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "constant")
public record Constant(
    @Id
    String id,
    Map<String, List<ConversionUnit>> conversions
) {

  public record ConversionUnit(
      Double value,
      String metric
  ) {

  }
}