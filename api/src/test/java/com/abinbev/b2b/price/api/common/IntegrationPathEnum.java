package com.abinbev.b2b.price.api.common;

public enum IntegrationPathEnum {
	REPOSITORY("repository-data");

	private final String path;

	IntegrationPathEnum(final String path) {

		this.path = path;
	}

	public String getPath() {

		return path;
	}
}
