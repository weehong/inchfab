package com.mattelogic.inchfab.core.controller;

import com.mattelogic.inchfab.common.dto.ApiResponseDto;
import com.mattelogic.inchfab.core.dtos.request.ProjectRequestDto;
import com.mattelogic.inchfab.core.dtos.response.ProjectResponseDto;
import com.mattelogic.inchfab.core.service.GenericService;
import com.mattelogic.inchfab.core.service.ProjectServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/projects")
@Tag(name = "Project", description = "Project management API")
public class ProjectController {

  private final GenericService<ProjectResponseDto, ProjectRequestDto> projectService;

  @Operation(summary = "Get all projects")
  @ApiResponse(
      responseCode = "200",
      description = "Found all projects",
      content = @Content(mediaType = "application/json",
          schema = @Schema(implementation = ProjectResponseDto.class))
  )
  @GetMapping
  public ResponseEntity<ApiResponseDto<List<ProjectResponseDto>>> getAll() {
    log.debug("REST request to get all Projects");
    return ResponseEntity.ok(projectService.all());
  }

  @Operation(summary = "Get a project by id")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Found the project",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ProjectResponseDto.class))),
      @ApiResponse(responseCode = "404", description = "Project not found",
          content = @Content)
  })
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponseDto<ProjectResponseDto>> getById(
      @Parameter(description = "id of project to be searched")
      @PathVariable Long id
  ) {
    log.debug("REST request to get Project : {}", id);
    return ResponseEntity.ok(projectService.getById(id));
  }

  @Operation(summary = "Create a new project")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Project created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid input"),
      @ApiResponse(responseCode = "409", description = "Project already exists")
  })
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<ApiResponseDto<ProjectResponseDto>> create(
      @Parameter(description = "Project to add. Cannot be null or empty.",
          required = true, schema = @Schema(implementation = ProjectRequestDto.class))
      @Valid @RequestBody ProjectRequestDto request
  ) {
    log.debug("REST request to save Project : {}", request);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(projectService.create(request));
  }

  @Operation(summary = "Create a draft project")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Draft project created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid input")
  })
  @PostMapping("/draft")
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<ApiResponseDto<ProjectResponseDto>> createDraft(
      @Parameter(description = "Project draft to add",
          required = true, schema = @Schema(implementation = ProjectRequestDto.class))
      @Valid @RequestBody ProjectRequestDto request
  ) {
    log.debug("REST request to save draft Project : {}", request);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(projectService.create(request));
  }

  @Operation(summary = "Clone an existing project")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Project cloned successfully"),
      @ApiResponse(responseCode = "404", description = "Source project not found")
  })
  @PostMapping("/{projectId}/clone")
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<ApiResponseDto<ProjectResponseDto>> clone(
      @Parameter(description = "id of project to be cloned")
      @PathVariable Long projectId
  ) {
    log.debug("REST request to clone Project : {}", projectId);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(((ProjectServiceImpl) projectService).copyProject(projectId));
  }

  @Operation(summary = "Update an existing project")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Project updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid input"),
      @ApiResponse(responseCode = "404", description = "Project not found"),
      @ApiResponse(responseCode = "409", description = "Project name already exists")
  })
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponseDto<ProjectResponseDto>> update(
      @Parameter(description = "id of project to be updated")
      @PathVariable Long id,
      @Parameter(description = "Project details to update",
          required = true, schema = @Schema(implementation = ProjectRequestDto.class))
      @Valid @RequestBody ProjectRequestDto request
  ) {
    log.debug("REST request to update Project : {}, {}", id, request);
    return ResponseEntity.ok(projectService.update(id, request));
  }

  @Operation(summary = "Delete a project")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Project not found")
  })
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ResponseEntity<ApiResponseDto<Boolean>> delete(
      @Parameter(description = "id of project to be deleted")
      @PathVariable Long id
  ) {
    log.debug("REST request to delete Project : {}", id);
    return ResponseEntity.ok(projectService.delete(id));
  }
}