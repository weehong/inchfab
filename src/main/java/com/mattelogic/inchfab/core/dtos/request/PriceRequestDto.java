package com.mattelogic.inchfab.core.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record PriceRequestDto(
    Integer projectId,
    Engineering engineering,
    Mask mask,
    Lot lot,
    Process process,
    Pricing pricing,
    Total total
) {

  public record Engineering(
      int hours,
      BigDecimal rate
  ) {

  }

  public record Mask(
      BigDecimal unitPrice,
      int count
  ) {

  }

  public record Lot(
      int minSize
  ) {

  }

  public record Process(
      BigDecimal margin,
      BigDecimal inFabCost,
      BigDecimal thirdPartyCost,
      BigDecimal waferPrice
  ) {

  }

  public record Pricing(
      Development dev,
      Production prod
  ) {

    public record Development(
        BigDecimal margin,
        BigDecimal price
    ) {

    }

    public record Production(
        @JsonProperty("100") PricePoint p100,
        @JsonProperty("1000") PricePoint p1000
    ) {

      public record PricePoint(
          BigDecimal margin,
          BigDecimal price
      ) {

      }
    }
  }

  public record Total(
      BigDecimal engineering,
      BigDecimal wafer,
      BigDecimal mask,
      BigDecimal project
  ) {

  }
}