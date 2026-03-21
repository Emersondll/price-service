package com.abinbev.b2b.price.api.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("offerspointtov2")
public class OffersPointToV2 {

	private String enabledCountries;

	public String getEnabledCountries() {

		return enabledCountries;
	}

	public void setEnabledCountries(final String enabledCountries) {

		this.enabledCountries = enabledCountries;
	}
}
