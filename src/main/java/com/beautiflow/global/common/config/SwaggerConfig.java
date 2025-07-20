package com.beautiflow.global.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		final String securitySchemeName = "Bearer Authentication";

		return new OpenAPI()
				.info(new Info()
						.title("BeautiFlow API Docs")
						.version("1.0")
						.description("BeautiFlow 백엔드 API 명세서입니다."))
				.addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
				.components(new Components()
						.addSecuritySchemes(securitySchemeName,
								new SecurityScheme()
										.name("Authorization")
										.type(SecurityScheme.Type.HTTP)
										.scheme("bearer")
										.bearerFormat("JWT")));
	}
}