package com.abinbev.b2b.price.api.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("security")
public class SecurityProperties {

	private boolean enabled;
	private String jwtApps;

	public boolean isEnabled() {

		return enabled;
	}

	public void setEnabled(final boolean enabled) {

		this.enabled = enabled;
	}

	public String getJwtApps() {

		return jwtApps;
	}

	public void setJwtApps(final String jwtApps) {

		this.jwtApps = jwtApps;
	}
}
