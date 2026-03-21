package com.abinbev.b2b.price.api.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("supportedproperties")
public class SupportedProperties {

	private String countries;
	private String countriesV2;
	private String countriesV3;

	public String getCountries() {

		return countries;
	}

	public void setCountries(final String countries) {

		this.countries = countries;
	}

	public String getCountriesV2() {

		return countriesV2;
	}

	public void setCountriesV2(final String countriesV2) {

		this.countriesV2 = countriesV2;
	}

	public String getCountriesV3() {

		return countriesV3;
	}

	public void setCountriesV3(final String countriesV3) {

		this.countriesV3 = countriesV3;
	}
}
