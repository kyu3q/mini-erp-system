package com.minierpapp.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
                .requestMatchers(
                    AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/customers/search/**"),
                    AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/customers/code/**"),
                    AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/customers")
                ).hasAnyRole("USER", "ADMIN")
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/customers/**")).hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
            .httpBasic(basic -> {});

        return http.build();
    }
}