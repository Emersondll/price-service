package com.abinbev.b2b.price.api.config;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

import com.abinbev.b2b.price.api.config.properties.RequestHeaderData;
import com.abinbev.b2b.price.api.helpers.ApiConstants;
import com.abinbev.b2b.price.api.helpers.MDCManager;

@Configuration
public class RequestLoggingFilterConfig {

	private final RequestHeaderData requestHeaderData;
	@Value("${logging.request-payload.max-payload-length}")
	private int maxPayloadLength;
	@Value("${logging.request-payload.enabled}")
	private boolean shouldLogRequest;

	@Autowired
	public RequestLoggingFilterConfig(final RequestHeaderData requestHeaderData) {

		this.requestHeaderData = requestHeaderData;
	}

	@Bean
	public AbstractRequestLoggingFilter logFilter() {

		final AbstractRequestLoggingFilter filter = new AbstractRequestLoggingFilter() {

			@Override
			protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
					final FilterChain filterChain) throws ServletException, IOException {

				if (shouldLogRequest(request)) {
					MDCManager.registerServiceLogData(request);
					addHeadersToRequestHeaderDataObject(request);
				}

				super.doFilterInternal(request, response, filterChain);
			}

			@Override
			protected void beforeRequest(final HttpServletRequest request, final String message) {

				if (shouldLogRequest(request)) {
					logger.info(message.replace("\n", ""));
				}
			}

			@Override
			protected void afterRequest(@NotNull final HttpServletRequest request, final String message) {

				if (shouldLogRequest(request)) {
					logger.info(message.replace("\n", ""));
					MDC.clear();
				}
			}
		};

		filter.setIncludeQueryString(true);
		filter.setIncludePayload(true);
		filter.setMaxPayloadLength(maxPayloadLength);
		return filter;
	}

	private void addHeadersToRequestHeaderDataObject(final HttpServletRequest request) {

		requestHeaderData.setServiceName(ApiConstants.PRICE_SERVICE);
		requestHeaderData.setRequestTraceId(request.getHeader(ApiConstants.REQUEST_TRACE_ID_HEADER));
		requestHeaderData.setCountry(request.getHeader(ApiConstants.COUNTRY_HEADER));
		requestHeaderData.setAuthorization(request.getHeader(ApiConstants.AUTHORIZATION_HEADER));
	}

	private boolean shouldLogRequest(final HttpServletRequest request) {

		return shouldLogRequest && !request.getRequestURI().startsWith("/actuator");
	}
}