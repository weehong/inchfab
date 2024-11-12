package com.mattelogic.inchfab.core.service;

import com.mattelogic.inchfab.common.dto.ApiResponseDto;

public interface CompanyService<T, K> extends GenericService<T, K> {

  ApiResponseDto<T> getCompanyWithProjects(Long companyId);
}
