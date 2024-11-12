package com.mattelogic.inchfab.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"status", "message", "data", "error"})
public record GlobalResponseDto<T>(

    @JsonProperty("status")
    boolean success,
    String message,
    T data,
    Map<String, String> errors
) {

}
