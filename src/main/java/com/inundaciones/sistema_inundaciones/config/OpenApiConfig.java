package com.inundaciones.sistema_inundaciones.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    @Value("${server.port}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("SIstema de inundaciones API")
                        .description("Sistema para control de inundaciones con ESP32 y Spring Boot")
                        .version("v1.0.0"))
                .servers(List.of(
                        new Server()
                                .url("https://sistema-inundaciones-arqui-production.up.railway.app")
                                .description("Servidor de Producción"),
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Servidor de Desarrollo")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT para autenticación")));
    }
}
