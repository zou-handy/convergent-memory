package com.convergentmemory.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(title = "Convergent Memory API", version = "v0.1",
                 description = "Personal memory REST API - Markdown as truth, DB as index, AgentScope as converger"),
    security = @SecurityRequirement(name = "bearerAuth"),
    servers = {
        @Server(url = "http://134.209.66.112", description = "Production (DigitalOcean nyc1)"),
        @Server(url = "http://localhost:8081", description = "Local dev")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    description = "Bearer token. For demo, use: dev-secret-key-change-me"
)
public class OpenApiConfig {
}
