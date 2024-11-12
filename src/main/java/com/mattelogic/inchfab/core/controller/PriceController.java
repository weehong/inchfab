package com.mattelogic.inchfab.core.controller;

import com.mattelogic.inchfab.common.dto.ApiResponseDto;
import com.mattelogic.inchfab.core.dtos.request.PriceRequestDto;
import com.mattelogic.inchfab.core.dtos.response.PriceResponseDto;
import com.mattelogic.inchfab.core.service.PriceServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/price-calculator")
@Tag(name = "Price Calculator", description = "API endpoints for managing project pricing calculations")
public class PriceController {

  private final PriceServiceImpl service;

  @Operation(
      summary = "Calculate or update project price",
      description = "Creates a new price calculation or updates existing one for the specified project"
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Price successfully updated",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = PriceResponseDto.class))
      ),
      @ApiResponse(
          responseCode = "201",
          description = "Price successfully created",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = PriceResponseDto.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Invalid input supplied",
          content = @Content
      ),
      @ApiResponse(
          responseCode = "404",
          description = "Project not found",
          content = @Content
      ),
      @ApiResponse(
          responseCode = "500",
          description = "Internal server error",
          content = @Content
      )
  })
  @PostMapping("/{projectId}")
  public ResponseEntity<ApiResponseDto<PriceResponseDto>> calculate(
      @Parameter(description = "ID of project to calculate price for", required = true)
      @PathVariable Long projectId,
      @Parameter(description = "Price calculation request details", required = true)
      @Valid @RequestBody PriceRequestDto request) throws ServiceException {
    log.debug("Calculating price for project ID: {} with request: {}", projectId, request);
    return ResponseEntity.ok(service.upsertByProjectId(projectId, request));
  }

  @Operation(
      summary = "Get project price",
      description = "Retrieves the existing price calculation for the specified project"
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Price successfully retrieved",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = PriceResponseDto.class))
      ),
      @ApiResponse(
          responseCode = "404",
          description = "Price calculation not found for the specified project",
          content = @Content
      ),
      @ApiResponse(
          responseCode = "500",
          description = "Internal server error",
          content = @Content
      )
  })
  @GetMapping("/{projectId}")
  public ResponseEntity<ApiResponseDto<PriceResponseDto>> get(
      @Parameter(description = "ID of project to fetch price for", required = true)
      @PathVariable Long projectId) {
    log.debug("Fetching price for project ID: {}", projectId);
    return ResponseEntity.ok(service.getByProjectId(projectId));
  }
}