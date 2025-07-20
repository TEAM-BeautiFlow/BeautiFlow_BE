package com.beautiflow.global.common.config;

import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		SecurityScheme securityScheme = new SecurityScheme()
				.type(SecurityScheme.Type.HTTP)
				.scheme("bearer")
				.bearerFormat("JWT")
				.in(SecurityScheme.In.HEADER)
				.name("Authorization");

		SecurityRequirement securityRequirement = new SecurityRequirement().addList("BearerAuth");

		return new OpenAPI()
				.info(new Info()
						.title("BeautiFlow API Docs")
						.version("1.0")
						.description("BeautiFlow 백엔드 API 명세서입니다."))
				.addSecurityItem(securityRequirement)
				.schemaRequirement("BearerAuth", securityScheme);
	}
}