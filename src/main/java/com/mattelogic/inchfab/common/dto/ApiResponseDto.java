package com.mattelogic.inchfab.common.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ApiResponseDto<T> {

  private int status;
  private String message;
  private T data;
}
