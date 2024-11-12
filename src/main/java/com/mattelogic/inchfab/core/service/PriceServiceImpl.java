package com.mattelogic.inchfab.core.service;

import com.mattelogic.inchfab.common.dto.ApiResponseDto;
import com.mattelogic.inchfab.core.dtos.request.PriceRequestDto;
import com.mattelogic.inchfab.core.dtos.response.PriceResponseDto;
import com.mattelogic.inchfab.core.entity.Price;
import com.mattelogic.inchfab.core.exception.PriceNotFoundException;
import com.mattelogic.inchfab.core.mapper.PriceMapper;
import com.mattelogic.inchfab.core.repository.PriceRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PriceServiceImpl implements GenericService<PriceResponseDto, PriceRequestDto> {

  private final PriceRepository repository;
  private final PriceMapper mapper;

  @Override
  public ApiResponseDto<PriceResponseDto> create(PriceRequestDto request) {
    throw new UnsupportedOperationException("Create prices is not supported");
  }

  public ApiResponseDto<PriceResponseDto> upsertByProjectId(Long projectId,
      PriceRequestDto request) {
    Optional<Price> existingPrice = repository.findByProjectId(projectId);

    if (existingPrice.isPresent()) {
      log.info("Updating existing price for project {}", projectId);
      Price price = existingPrice.get();
      mapper.updateEntityFromDto(request, price, projectId);
      Price updatedPrice = repository.save(price);
      return buildResponse(HttpStatus.OK, "Price updated successfully",
          mapper.toResponseDto(updatedPrice));
    } else {
      log.info("Creating new price for project {}", projectId);
      Price newPrice = mapper.toEntity(request, projectId);
      Price savedPrice = repository.save(newPrice);
      return buildResponse(HttpStatus.CREATED, "Price created successfully",
          mapper.toResponseDto(savedPrice));
    }
  }

  public ApiResponseDto<PriceResponseDto> getByProjectId(Long projectId) {
    Price price = repository.findByProjectId(projectId)
        .orElseThrow(() -> new PriceNotFoundException(projectId));

    return ApiResponseDto.<PriceResponseDto>builder()
        .status(HttpStatus.OK.value())
        .message("Price fetched successfully")
        .data(mapper.toResponseDto(price))
        .build();
  }

  @Override
  public ApiResponseDto<List<PriceResponseDto>> all() {

    throw new UnsupportedOperationException("Listing all prices is not supported");
  }

  @Override
  public ApiResponseDto<PriceResponseDto> getById(Long id) {
    throw new UnsupportedOperationException("Getting price by ID is not supported");
  }

  @Override
  public ApiResponseDto<PriceResponseDto> update(Long id, PriceRequestDto request) {
    throw new UnsupportedOperationException("Updating price is not supported");
  }

  @Override
  public ApiResponseDto<Boolean> delete(Long id) {
    throw new UnsupportedOperationException("Deleting price is not supported");
  }

  private ApiResponseDto<PriceResponseDto> buildResponse(HttpStatus status, String message,
      PriceResponseDto data) {
    return ApiResponseDto.<PriceResponseDto>builder()
        .status(status.value())
        .message(message)
        .data(data)
        .build();
  }
}