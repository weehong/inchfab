package com.mattelogic.inchfab.core.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CompanyResponseDto(
    Long id,
    String name,
    String logo,
    String title,
    String email,
    List<ProjectResponseDto> projects
) {

}
