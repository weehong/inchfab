package com.mattelogic.inchfab.core.mapper;

import com.mattelogic.inchfab.core.dtos.request.PriceRequestDto;
import com.mattelogic.inchfab.core.dtos.request.PriceRequestDto.Engineering;
import com.mattelogic.inchfab.core.dtos.request.PriceRequestDto.Lot;
import com.mattelogic.inchfab.core.dtos.request.PriceRequestDto.Mask;
import com.mattelogic.inchfab.core.dtos.request.PriceRequestDto.Pricing;
import com.mattelogic.inchfab.core.dtos.request.PriceRequestDto.Pricing.Development;
import com.mattelogic.inchfab.core.dtos.request.PriceRequestDto.Pricing.Production;
import com.mattelogic.inchfab.core.dtos.request.PriceRequestDto.Pricing.Production.PricePoint;
import com.mattelogic.inchfab.core.dtos.request.PriceRequestDto.Process;
import com.mattelogic.inchfab.core.dtos.request.PriceRequestDto.Total;
import com.mattelogic.inchfab.core.dtos.response.PriceResponseDto;
import com.mattelogic.inchfab.core.entity.Price;
import org.springframework.stereotype.Component;

@Component
public class PriceMapper {

  public Price toEntity(PriceRequestDto dto, Long projectId) {
    Price price = new Price();
    price.setProjectId(projectId);
    price.setEngineeringHours(dto.engineering().hours());
    price.setEngineeringRate(dto.engineering().rate());
    price.setMaskUnitPrice(dto.mask().unitPrice());
    price.setMaskCount(dto.mask().count());
    price.setMinLotSize(dto.lot().minSize());
    price.setProcessMargin(dto.process().margin());
    price.setProcessInFabCost(dto.process().inFabCost());
    price.setProcessThirdPartyCost(dto.process().thirdPartyCost());
    price.setProcessWaferPrice(dto.process().waferPrice());
    price.setDevMargin(dto.pricing().dev().margin());
    price.setDevPrice(dto.pricing().dev().price());
    price.setProd100Margin(dto.pricing().prod().p100().margin());
    price.setProd100Price(dto.pricing().prod().p100().price());
    price.setProd1000Margin(dto.pricing().prod().p1000().margin());
    price.setProd1000Price(dto.pricing().prod().p1000().price());
    price.setTotalEngineering(dto.total().engineering());
    price.setTotalWafer(dto.total().wafer());
    price.setTotalMask(dto.total().mask());
    price.setTotalProject(dto.total().project());
    price.setCreatedAt(null);
    return price;
  }

  public PriceResponseDto toResponseDto(Price entity) {
    return new PriceResponseDto(
        new Engineering(entity.getEngineeringHours(), entity.getEngineeringRate()),
        new Mask(entity.getMaskUnitPrice(), entity.getMaskCount()),
        new Lot(entity.getMinLotSize()),
        new Process(
            entity.getProcessMargin(),
            entity.getProcessInFabCost(),
            entity.getProcessThirdPartyCost(),
            entity.getProcessWaferPrice()
        ),
        new Pricing(
            new Development(entity.getDevMargin(), entity.getDevPrice()),
            new Production(
                new PricePoint(entity.getProd100Margin(), entity.getProd100Price()),
                new PricePoint(entity.getProd1000Margin(), entity.getProd1000Price())
            )
        ),
        new Total(
            entity.getTotalEngineering(),
            entity.getTotalWafer(),
            entity.getTotalMask(),
            entity.getTotalProject()
        )
    );
  }

  public void updateEntityFromDto(PriceRequestDto dto, Price entity, Long projectId) {
    entity.setProjectId(projectId);
    entity.setEngineeringHours(dto.engineering().hours());
    entity.setEngineeringRate(dto.engineering().rate());
    entity.setMaskUnitPrice(dto.mask().unitPrice());
    entity.setMaskCount(dto.mask().count());
    entity.setMinLotSize(dto.lot().minSize());
    entity.setProcessMargin(dto.process().margin());
    entity.setProcessInFabCost(dto.process().inFabCost());
    entity.setProcessThirdPartyCost(dto.process().thirdPartyCost());
    entity.setProcessWaferPrice(dto.process().waferPrice());
    entity.setDevMargin(dto.pricing().dev().margin());
    entity.setDevPrice(dto.pricing().dev().price());
    entity.setProd100Margin(dto.pricing().prod().p100().margin());
    entity.setProd100Price(dto.pricing().prod().p100().price());
    entity.setProd1000Margin(dto.pricing().prod().p1000().margin());
    entity.setProd1000Price(dto.pricing().prod().p1000().price());
    entity.setTotalEngineering(dto.total().engineering());
    entity.setTotalWafer(dto.total().wafer());
    entity.setTotalMask(dto.total().mask());
    entity.setTotalProject(dto.total().project());
  }
}