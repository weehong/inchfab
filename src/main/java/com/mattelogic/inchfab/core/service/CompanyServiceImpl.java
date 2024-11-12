package com.mattelogic.inchfab.core.service;

import com.mattelogic.inchfab.common.dto.ApiResponseDto;
import com.mattelogic.inchfab.core.dtos.request.CompanyRequestDto;
import com.mattelogic.inchfab.core.dtos.response.CompanyResponseDto;
import com.mattelogic.inchfab.core.entity.Company;
import com.mattelogic.inchfab.core.exception.CompanyNotFoundException;
import com.mattelogic.inchfab.core.exception.DuplicateCompanyException;
import com.mattelogic.inchfab.core.mapper.CompanyMapper;
import com.mattelogic.inchfab.core.repository.CompanyRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CompanyServiceImpl implements
    GenericService<CompanyResponseDto, CompanyRequestDto> {

  private final CompanyRepository companyRepository;
  private final CompanyMapper companyMapper;

  @Transactional
  @Override
  public ApiResponseDto<CompanyResponseDto> create(CompanyRequestDto companyRequestDto) {
    try {
      Company company = companyMapper.toEntity(companyRequestDto);
      company = companyRepository.save(company);
      return ApiResponseDto.<CompanyResponseDto>builder()
          .status(HttpStatus.CREATED.value())
          .message("Company created successfully")
          .data(companyMapper.toResponseDto(company))
          .build();
    } catch (DataIntegrityViolationException e) {
      if (e.getMessage().contains("companies_name_key")) {
        throw new DuplicateCompanyException(
            String.format("Company with name '%s' already exists", companyRequestDto.name()));
      }
      throw e;
    }
  }

  @Override
  public ApiResponseDto<List<CompanyResponseDto>> all() {
    List<Company> companies = companyRepository.findAll();
    return ApiResponseDto.<List<CompanyResponseDto>>builder()
        .status(HttpStatus.OK.value())
        .message("Companies fetched successfully")
        .data(companies.stream()
            .map(companyMapper::toResponseDto)
            .toList())
        .build();
  }

  @Override
  public ApiResponseDto<CompanyResponseDto> getById(Long id) {
    Company company = companyRepository.findById(id)
        .orElseThrow(() -> new CompanyNotFoundException(id));

    return ApiResponseDto.<CompanyResponseDto>builder()
        .status(HttpStatus.OK.value())
        .message("Company fetched successfully")
        .data(companyMapper.toResponseDto(company))
        .build();
  }

  @Override
  public ApiResponseDto<CompanyResponseDto> update(Long id, CompanyRequestDto companyRequestDto) {
    try {
      if (!companyRepository.existsById(id)) {
        throw new CompanyNotFoundException(id);
      }

      Company updatedCompany = companyMapper.toEntity(companyRequestDto);
      updatedCompany.setId(id);

      updatedCompany.getProjects().forEach(project -> project.setCompany(updatedCompany));
      
      Company savedCompany = companyRepository.save(updatedCompany);

      return ApiResponseDto.<CompanyResponseDto>builder()
          .status(HttpStatus.OK.value())
          .message("Company updated successfully")
          .data(companyMapper.toResponseDto(savedCompany))
          .build();
    } catch (DataIntegrityViolationException e) {
      if (e.getMessage().contains("companies_name_key")) {
        throw new DuplicateCompanyException(
            String.format("Cannot update: Company with name '%s' already exists",
                companyRequestDto.name()));
      }
      throw e;
    }
  }

  @Override
  public ApiResponseDto<Boolean> delete(Long id) {
    if (!companyRepository.existsById(id)) {
      throw new CompanyNotFoundException(id);
    }

    companyRepository.deleteById(id);
    return ApiResponseDto.<Boolean>builder()
        .status(HttpStatus.OK.value())
        .message("Company deleted successfully")
        .data(true)
        .build();
  }
}