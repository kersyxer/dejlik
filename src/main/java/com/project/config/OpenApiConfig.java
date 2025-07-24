package com.project.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // Додаємо глобальне посилання на security scheme
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(
                        new Components()
                                // Визначаємо сам Bearer JWT scheme
                                .addSecuritySchemes(BEARER_SCHEME,
                                        new SecurityScheme()
                                                .name(BEARER_SCHEME)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                );
    }
}
