package com.minierpapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.context.annotation.Bean;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }

    @Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }
}