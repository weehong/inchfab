package com.mattelogic.inchfab.domain.service;

import com.mattelogic.inchfab.common.dto.ApiResponseDto;
import com.mattelogic.inchfab.core.dtos.response.ResultResponseDto;

public sealed interface CalculateService<S, T, U> permits AldServiceImpl, DrieServiceImpl,
    ExternalProcessServiceImpl, IcpcvdServiceImpl, LithographyServiceImpl, LpcvdServiceImpl,
    MagnetronSputteringServiceImpl, ManualProcessServiceImpl, MetrologyInspectionServiceImpl,
    RieServiceImpl, SubstrateServiceImpl, WetProcessServiceImpl {

  ResultResponseDto calculate(S step, T request);

  ApiResponseDto<ResultResponseDto> calculate(U request);
}
