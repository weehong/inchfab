package com.mattelogic.inchfab.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mattelogic.inchfab.common.constant.ErrorTypeConstant;
import com.mattelogic.inchfab.common.dto.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.AuthenticationEntryPoint;

@Configuration
public class AuthenticationEntryPointConfig {

  private static final ErrorTypeConstant.ErrorType AUTH_ERROR = new ErrorTypeConstant.ErrorType(
      "unauthorized",
      "Authentication Failed",
      "Invalid or missing JWT token",
      HttpStatus.UNAUTHORIZED
  );

  private final ObjectMapper objectMapper;

  public AuthenticationEntryPointConfig(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Bean
  public AuthenticationEntryPoint customAuthenticationEntryPoint() {
    return (request, response, authException) -> {
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      response.setContentType("application/json");

      ErrorResponseDto errorResponse = createErrorResponse(request);
      objectMapper.writeValue(response.getOutputStream(), errorResponse);
    };
  }

  private ErrorResponseDto createErrorResponse(HttpServletRequest request) {
    return ErrorResponseDto.builder()
        .timestamp(LocalDateTime.now())
        .type(ErrorTypeConstant.ERROR_BASE_URL + "/" + AUTH_ERROR.path())
        .title(AUTH_ERROR.title())
        .status(AUTH_ERROR.status().value())
        .detail(AUTH_ERROR.defaultMessage())
        .instance(request.getServletPath())
        .build();
  }
}