package com.mattelogic.inchfab.core.dtos.response;

public record CompanyResponseDto(
    Long id,
    String name,
    String logo,
    String title,
    String email
) {

}
