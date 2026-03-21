package com.abinbev.b2b.price.api.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("priceupfront")
public class PriceUpFrontProperties {

	private String countriesEnabled;

	public String getCountriesEnabled() {

		return countriesEnabled;
	}

	public void setCountriesEnabled(final String countriesEnabled) {

		this.countriesEnabled = countriesEnabled;
	}

}
