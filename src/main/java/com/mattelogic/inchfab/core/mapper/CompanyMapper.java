package com.mattelogic.inchfab.core.mapper;

import com.mattelogic.inchfab.core.dtos.request.CompanyRequestDto;
import com.mattelogic.inchfab.core.dtos.response.CompanyResponseDto;
import com.mattelogic.inchfab.core.entity.Company;
import org.springframework.stereotype.Component;

@Component
public class CompanyMapper {

  public CompanyResponseDto toResponseDto(Company company) {
    return new CompanyResponseDto(
        company.getId(),
        company.getName(),
        company.getLogo(),
        company.getTitle(),
        company.getEmail()
    );
  }

  public Company toEntity(CompanyRequestDto request) {
    Company company = new Company();

    company.setName(request.name());
    company.setLogo(request.logo());
    company.setTitle(request.title());
    company.setEmail(request.email());

    return company;
  }
}
