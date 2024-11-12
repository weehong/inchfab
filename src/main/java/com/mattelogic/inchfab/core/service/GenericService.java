package com.mattelogic.inchfab.core.service;

import com.mattelogic.inchfab.common.dto.ApiResponseDto;
import java.util.List;

public interface GenericService<T, K> {

  ApiResponseDto<T> create(K k);

  ApiResponseDto<List<T>> all();

  ApiResponseDto<T> getById(Long id);

  ApiResponseDto<T> update(Long id, K k);

  ApiResponseDto<Boolean> delete(Long id);
}
