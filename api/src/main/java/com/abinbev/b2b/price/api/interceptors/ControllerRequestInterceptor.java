package com.abinbev.b2b.price.api.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Generic interceptor for all controllers.
 *
 * @author thiagoteixeira
 */
@Component
public class ControllerRequestInterceptor implements HandlerInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerRequestInterceptor.class);

	@Override
	public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response, final Object handler,
			@Nullable final Exception ex) throws Exception {

		final String logMessage = String.format("Response: %s %s; Response Status: %s ", request.getMethod(), request.getRequestURI(),
				response.getStatus());
		LOGGER.debug(logMessage);
	}

}