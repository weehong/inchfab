package com.mattelogic.inchfab.common.util;

import com.mattelogic.inchfab.common.dto.GlobalResponseDto;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Slf4j
public class ResponseBuilder {

  public static <T> ResponseEntity<GlobalResponseDto<T>> buildResponse(
      T data, HttpStatus successStatusCode, HttpStatus failStatusCode, String successMessage) {

    boolean isNullOrEmpty =
        (data == null) || (data instanceof List<?> && ((List<?>) data).isEmpty());

    GlobalResponseDto<T> response = new GlobalResponseDto<>(
        !isNullOrEmpty,
        isNullOrEmpty ? "Failed to proceed with the action" : successMessage,
        isNullOrEmpty ? null : data,
        null
    );

    return ResponseEntity.status(
            isNullOrEmpty
                ? failStatusCode
                : successStatusCode
        )
        .body(response);
  }
}
