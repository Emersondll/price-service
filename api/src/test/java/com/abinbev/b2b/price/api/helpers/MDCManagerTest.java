package com.abinbev.b2b.price.api.helpers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;

class MDCManagerTest {

	private static final String REQUEST_TRACE_ID = "0a2f9f71-f96d-47c9-9ce1-c7af73a449d5";
	private static final String MOCKED_COUNTRY_US = "US";

	@Test
	void shouldRegisterServiceLogDataWhenHasValidParameters() {

		final HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
		when(mockHttpServletRequest.getHeader(ApiConstants.REQUEST_TRACE_ID_HEADER)).thenReturn(REQUEST_TRACE_ID);
		when(mockHttpServletRequest.getHeader(ApiConstants.COUNTRY_HEADER)).thenReturn(MOCKED_COUNTRY_US);

		MDCManager.registerServiceLogData(mockHttpServletRequest);
	}

}
