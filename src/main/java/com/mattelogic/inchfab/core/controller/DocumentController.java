package com.mattelogic.inchfab.core.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mattelogic.inchfab.common.dto.ApiResponseDto;
import com.mattelogic.inchfab.core.dtos.request.DocumentRequestDto;
import com.mattelogic.inchfab.core.dtos.response.DocumentResponseDto;
import com.mattelogic.inchfab.core.service.DocumentServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/documents")
public class DocumentController {

  private final DocumentServiceImpl documentService;

  @GetMapping
  public ResponseEntity<String> getAll() {
    //    documentService.listAllFolders();

    log.info("Document Controller - getAll()");
    return ResponseEntity.ok("Document found");
  }

  @PostMapping
  public ResponseEntity<ApiResponseDto<DocumentResponseDto>> upload(
      @ModelAttribute DocumentRequestDto documentRequestDto) throws JsonProcessingException {
    return ResponseEntity.ok(documentService.upload(documentRequestDto));
  }
}
