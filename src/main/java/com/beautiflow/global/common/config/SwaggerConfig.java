package com.beautiflow.global.common.config;

import java.util.List; // List import 추가

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		final String securitySchemeName = "Bearer Authentication";

		// ✅ 이 부분을 추가하여 서버 URL을 명시합니다.
//		final Server server = new Server().url("http://localhost:8080").description("Default Server URL");

		return new OpenAPI()
				.info(new Info()
						.title("BeautiFlow API Docs")
						.version("1.0")
						.description("BeautiFlow 백엔드 API 명세서입니다."))
				// ✅ servers() 메서드 호출을 추가합니다.
				.servers(
						// ✅ API 서버 주소를 추가합니다.
						List.of(
								new Server().url("http://localhost:8080").description("Local Dev Server"),
								new Server().url("http://3.38.93.35").description("EC2 IP Address"),
								new Server().url("https://beautiflow.co.kr").description("Production Domain")
						)
				)
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