package com.mattelogic.inchfab.core.controller;

import com.mattelogic.inchfab.common.dto.ApiResponseDto;
import com.mattelogic.inchfab.core.dtos.request.CostRequestDto;
import com.mattelogic.inchfab.core.dtos.response.ResultResponseDto;
import com.mattelogic.inchfab.core.service.ProcessCostCalculatorServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cost-calculator")
public class CostCalculatorController {

  private final ProcessCostCalculatorServiceImpl service;

  @Operation(summary = "Calculate cost")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Calculated cost",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ResultResponseDto.class))),
      @ApiResponse(responseCode = "400", description = "Invalid input",
          content = @Content),
      @ApiResponse(responseCode = "404", description = "Cost not found",
          content = @Content)
  })
  @PostMapping
  public ResponseEntity<ApiResponseDto<ResultResponseDto>> calculate(
      @Valid @RequestBody CostRequestDto request) throws Throwable {
    log.debug("REST request to calculate cost: {}", request);
    return ResponseEntity.ok(service.calculate(request));
  }
}
