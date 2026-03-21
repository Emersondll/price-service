package com.abinbev.b2b.price.api.config;

import java.time.LocalDate;
import java.util.List;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.web.method.HandlerMethod;

import com.abinbev.b2b.price.api.helpers.ApiConstants;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
@PropertySource("classpath:version.properties")
public class SwaggerConfig {

	private static final String BEARER_SCHEME = "bearer";
	private static final String BEARER_FORMAT = "JWT";

	@Value("${swagger.url}")
	private String baseUrl;

	@Bean
	public GroupedOpenApi apiV1() {

		return GroupedOpenApi.builder().group(ApiConstants.API_VERSION_1_LABEL).pathsToMatch("/v1/**")
				.addOpenApiCustomizer(openApiCustomizer1dot0()).build();
	}

	@Bean
	public GroupedOpenApi apiV2() {

		return GroupedOpenApi.builder().group(ApiConstants.API_VERSION_2_LABEL).pathsToMatch("/v2/**")
				.addOpenApiCustomizer(openApiCustomizer2dot0()).build();
	}

	@Bean
	public GroupedOpenApi apiV3() {

		return GroupedOpenApi.builder().group(ApiConstants.API_VERSION_3_LABEL).pathsToMatch("/v3/**")
				.addOpenApiCustomizer(openApiCustomizer3dot0()).build();
	}

	@Bean
	public OpenApiCustomizer openApiCustomizer1dot0() {

		final int currentYear = LocalDate.now().getYear();
		return openApi -> openApi.servers(List.of(new Server().url(baseUrl)))
				.info(new Info().title(ApiConstants.API_TITLE).description(ApiConstants.API_DESCRIPTION).version("1.0")
						.license(new License().name(ApiConstants.API_LICENSE + currentYear))).getComponents()
				.addSecuritySchemes(HttpHeaders.AUTHORIZATION,
						new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme(BEARER_SCHEME).bearerFormat(BEARER_FORMAT));
	}

	@Bean
	public OpenApiCustomizer openApiCustomizer2dot0() {

		final int currentYear = LocalDate.now().getYear();
		return openApi -> openApi.servers(List.of(new Server().url(baseUrl)))
				.info(new Info().title(ApiConstants.API_TITLE).description(ApiConstants.API_DESCRIPTION).version("2.0")
						.license(new License().name(ApiConstants.API_LICENSE + currentYear))).getComponents()
				.addSecuritySchemes(HttpHeaders.AUTHORIZATION,
						new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme(BEARER_SCHEME).bearerFormat(BEARER_FORMAT));
	}

	@Bean
	public OpenApiCustomizer openApiCustomizer3dot0() {

		final int currentYear = LocalDate.now().getYear();
		return openApi -> openApi.servers(List.of(new Server().url(baseUrl)))
				.info(new Info().title(ApiConstants.API_TITLE).description(ApiConstants.API_DESCRIPTION).version("3.0")
						.license(new License().name(ApiConstants.API_LICENSE + currentYear))).getComponents()
				.addSecuritySchemes(HttpHeaders.AUTHORIZATION,
						new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme(BEARER_SCHEME).bearerFormat(BEARER_FORMAT));
	}

	@Bean
	public OperationCustomizer customGlobalHeaders() {

		return (final Operation operation, final HandlerMethod handlerMethod) -> {
			operation.addSecurityItem(new SecurityRequirement().addList(HttpHeaders.AUTHORIZATION));
			return operation;
		};
	}
}
