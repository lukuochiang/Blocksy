package com.blocksy.server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI blocksyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Norvo API")
                        .description("Norvo MVP 接口文档")
                        .version("v1.0.0"))
                .components(new Components().addSecuritySchemes(
                        "bearerAuth",
                        new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("在此输入 JWT token（无需手动输入 Bearer 前缀）")
                ));
    }
}
