package com.mattelogic.inchfab.core.dtos.response;

import com.mattelogic.inchfab.core.dtos.request.PriceRequestDto.Engineering;
import com.mattelogic.inchfab.core.dtos.request.PriceRequestDto.Lot;
import com.mattelogic.inchfab.core.dtos.request.PriceRequestDto.Mask;
import com.mattelogic.inchfab.core.dtos.request.PriceRequestDto.Pricing;
import com.mattelogic.inchfab.core.dtos.request.PriceRequestDto.Process;
import com.mattelogic.inchfab.core.dtos.request.PriceRequestDto.Total;

public record PriceResponseDto(
    Engineering engineering,
    Mask mask,
    Lot lot,
    Process process,
    Pricing pricing,
    Total total
) {

}