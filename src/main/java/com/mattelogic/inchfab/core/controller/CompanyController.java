package com.mattelogic.inchfab.core.controller;

import com.mattelogic.inchfab.common.dto.ApiResponseDto;
import com.mattelogic.inchfab.core.dtos.request.CompanyRequestDto;
import com.mattelogic.inchfab.core.dtos.response.CompanyResponseDto;
import com.mattelogic.inchfab.core.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/companies")
@Tag(name = "Company", description = "Company management API")
public class CompanyController {

  private final CompanyService companyService;

  @Operation(summary = "Get all companies")
  @ApiResponse(
      responseCode = "200",
      description = "Found all companies",
      content = @Content(mediaType = "application/json",
          schema = @Schema(implementation = CompanyResponseDto.class))
  )
  @GetMapping
  public ResponseEntity<ApiResponseDto<List<CompanyResponseDto>>> getAll() {
    log.debug("REST request to get all Companies");
    return ResponseEntity.ok(companyService.all());
  }

  @Operation(summary = "Get a company by id")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Found the company",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = CompanyResponseDto.class))),
      @ApiResponse(responseCode = "404", description = "Company not found",
          content = @Content)
  })
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponseDto<CompanyResponseDto>> getById(
      @Parameter(description = "id of company to be searched")
      @PathVariable Long id
  ) {
    log.debug("REST request to get Company : {}", id);
    return ResponseEntity.ok(companyService.getById(id));
  }

  @Operation(summary = "Create a new company")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Company created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid input"),
      @ApiResponse(responseCode = "409", description = "Company already exists")
  })
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<ApiResponseDto<CompanyResponseDto>> create(
      @Parameter(description = "Company to add. Cannot null or empty.",
          required = true, schema = @Schema(implementation = CompanyRequestDto.class))
      @Valid @RequestBody CompanyRequestDto request
  ) {
    log.debug("REST request to save Company : {}", request);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(companyService.create(request));
  }

  @Operation(summary = "Update an existing company")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Company updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid input"),
      @ApiResponse(responseCode = "404", description = "Company not found"),
      @ApiResponse(responseCode = "409", description = "Company name already exists")
  })
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponseDto<CompanyResponseDto>> update(
      @Parameter(description = "id of company to be updated")
      @PathVariable Long id,
      @Parameter(description = "Company details to update",
          required = true, schema = @Schema(implementation = CompanyRequestDto.class))
      @Valid @RequestBody CompanyRequestDto request
  ) {
    log.debug("REST request to update Company : {}, {}", id, request);
    return ResponseEntity.ok(companyService.update(id, request));
  }

  @Operation(summary = "Get all projects for a company")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved projects"),
      @ApiResponse(responseCode = "400", description = "Invalid company ID supplied"),
      @ApiResponse(responseCode = "403", description = "Forbidden"),
      @ApiResponse(responseCode = "404", description = "Company not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping(
      value = "/{companyId}/projects",
      produces = "application/json"
  )
  public ResponseEntity<ApiResponseDto<CompanyResponseDto>> getAllProjectsByCompany(
      @Parameter(description = "ID of company whose projects are to be retrieved")
      @PathVariable @Min(1) Long companyId
  ) {
    log.debug("REST request to get all projects for Company ID : {}", companyId);
    return ResponseEntity.ok(companyService.getCompanyWithProjects(companyId));
  }

  @Operation(summary = "Delete a company")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Company deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Company not found")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponseDto<Boolean>> delete(
      @Parameter(description = "id of company to be deleted")
      @PathVariable Long id
  ) {
    log.debug("REST request to delete Company : {}", id);
    return ResponseEntity.ok(companyService.delete(id));
  }
}