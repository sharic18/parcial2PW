package com.consultas;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI().info(new Info()
                .title("API Gestion de Consultas Medicas")
                .version("1.0")
                .description("Roles: ADMIN, MEDICO y PACIENTE. Primero ejecute POST /api/auth/login "
                        + "y luego pruebe los demas endpoints (la sesion se guarda en una cookie)."));
    }
}
