package com.mattelogic.inchfab.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDto {

  private LocalDateTime timestamp;
  private String type;
  private String title;
  private int status;
  private String detail;
  private String instance;
  private Map<String, String> errors;
}