package com.sarkari.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sarkariOpenAPI() {
        String securitySchemeName = "apiKeyAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Sarkari REST API")
                        .version("v1")
                        .description("Enterprise-grade REST API for Sarkari dashboard posts")
                        .contact(new Contact().name("Sarkari API Team")))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name("X-API-KEY")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("Provide API key via X-API-KEY header")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }
}
