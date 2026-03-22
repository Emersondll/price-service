package com.abinbev.b2b.price.api.helpers;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.MDC;

public class MDCManager {

	private MDCManager() {

	}

	public static void registerServiceLogData(final HttpServletRequest request) {

		putHeaderByHttpRequest(ApiConstants.REQUEST_TRACE_ID_HEADER, request);
		putHeaderByHttpRequest(ApiConstants.COUNTRY_HEADER, request);

		putHeaderByString(ApiConstants.SERVICE_NAME_LABEL, ApiConstants.PRICE_SERVICE);
	}

	private static void putHeaderByHttpRequest(final String header, final HttpServletRequest request) {

		MDC.put(header, request.getHeader(header));
	}

	private static void putHeaderByString(final String header, final String value) {

		MDC.put(header, value);
	}

}