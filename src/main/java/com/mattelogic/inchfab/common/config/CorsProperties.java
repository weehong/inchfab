package com.mattelogic.inchfab.common.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.cors")
public record CorsProperties(
    List<String> origins,
    List<String> methods
) {

}