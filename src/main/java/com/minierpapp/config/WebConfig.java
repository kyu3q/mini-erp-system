package com.minierpapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> containerCustomizer() {
        return factory -> {
            factory.addConnectorCustomizers(connector -> {
                connector.setAsyncTimeout(300000); // 5分
                connector.setMaxParameterCount(-1);
            });
        };
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseRegisteredSuffixPatternMatch(true);
    }
}