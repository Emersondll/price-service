package com.abinbev.b2b.price.api.config.properties;

import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(value = "toggle", ignoreInvalidFields = true)
public class ToggleConfigurationProperties {

	private Map<String, Boolean> code;

	public boolean isEnabledCodeToggle(final String toggle) {

		return code.getOrDefault(toggle, false);

	}

	public Map<String, Boolean> getCode() {

		return nonNull(code) ? code : emptyMap();
	}

	public void setCode(final Map<String, Boolean> code) {

		this.code = code;
	}
}