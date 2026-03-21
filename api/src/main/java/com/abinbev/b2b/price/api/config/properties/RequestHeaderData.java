package com.abinbev.b2b.price.api.config.properties;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class RequestHeaderData {

	private String authorization;
	private String country;
	private String requestTraceId;
	private String serviceName;

	public String getAuthorization() {

		return authorization;
	}

	public void setAuthorization(final String authorization) {

		this.authorization = authorization;
	}

	public String getCountry() {

		return country;
	}

	public void setCountry(final String country) {

		this.country = country;
	}

	public String getRequestTraceId() {

		return requestTraceId;
	}

	public void setRequestTraceId(final String requestTraceId) {

		this.requestTraceId = requestTraceId;
	}

	public String getServiceName() {

		return serviceName;
	}

	public void setServiceName(final String serviceName) {

		this.serviceName = serviceName;
	}
}
