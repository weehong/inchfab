package com.mattelogic.inchfab.domain.controller;

import com.mattelogic.inchfab.common.dto.ApiResponseDto;
import com.mattelogic.inchfab.core.dtos.request.CostRequestDto;
import com.mattelogic.inchfab.core.dtos.response.ResultResponseDto;
import com.mattelogic.inchfab.core.model.ProjectStep;
import com.mattelogic.inchfab.domain.dto.request.LpcvdRequestDto;
import com.mattelogic.inchfab.domain.service.CalculateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lp-cvd")
public class LpcvdController {

  private final CalculateService<ProjectStep, CostRequestDto, LpcvdRequestDto> service;

  @PostMapping
  public ApiResponseDto<ResultResponseDto> calculate(
      @Valid @RequestBody LpcvdRequestDto request) {
    return service.calculate(request);
  }
}
