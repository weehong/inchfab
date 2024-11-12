package com.mattelogic.inchfab.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private static final String[] SWAGGER_WHITELIST = {
      "/v3/api-docs/**",
      "/swagger-ui/**",
      "/swagger-ui.html",
      "/swagger-resources/**",
      "/webjars/**"
  };

  private final CorsConfig corsConfig;
  private final JwtConfiguration jwtConfiguration;
  private final AuthenticationEntryPointConfig authenticationEntryPointConfig;

  public SecurityConfig(
      CorsConfig corsConfig,
      JwtConfiguration jwtConfiguration,
      AuthenticationEntryPointConfig authenticationEntryPointConfig
  ) {
    this.corsConfig = corsConfig;
    this.jwtConfiguration = jwtConfiguration;
    this.authenticationEntryPointConfig = authenticationEntryPointConfig;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(SWAGGER_WHITELIST).permitAll()
            .requestMatchers("/.well-known/**").permitAll()
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt.decoder(jwtConfiguration.jwtDecoder()))
            .authenticationEntryPoint(
                authenticationEntryPointConfig.customAuthenticationEntryPoint())
        )
        .build();
  }
}

