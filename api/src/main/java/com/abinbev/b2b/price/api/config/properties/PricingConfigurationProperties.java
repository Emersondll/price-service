package com.abinbev.b2b.price.api.config.properties;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.abinbev.b2b.price.api.exceptions.PricingConfigurationNotFoundException;
import com.abinbev.b2b.price.api.helpers.ApiConstants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;

@Configuration
@ConfigurationProperties("pricing")
public class PricingConfigurationProperties {

	private static final Logger LOGGER = LoggerFactory.getLogger(PricingConfigurationProperties.class);

	private Map<String, Map<String, String>> configuration;

	@Value("#{environment.PRICING_CONFIGURATION}")
	private String configurationFromEnv;

	public Map<String, String> getConfigurationByCountry(final String country) {

		final Map<String, String> configurationByCountry;

		final String countryLowerCase = country.toLowerCase();

		if (!configuration.containsKey(countryLowerCase)) {
			configurationByCountry = configuration.get(ApiConstants.DEFAULT_CONFIGURATION);
		} else {
			configurationByCountry = configuration.get(countryLowerCase);
		}

		if (configurationByCountry == null) {
			throw PricingConfigurationNotFoundException.configurationNotFoundException(country);
		}

		return configurationByCountry;
	}

	public Map<String, Map<String, String>> getConfiguration() {

		return configuration;
	}

	public void setConfiguration(final Map<String, Map<String, String>> configuration) {

		if (StringUtils.isNotBlank(configurationFromEnv)) {
			LOGGER.debug("applying configurations from environment variable");
			// HACK: To fix spring relaxed binding behavior when inject maps
			try {
				this.configuration = new Gson().fromJson(configurationFromEnv, new TypeReference<Map<String, Map<String, String>>>() {
				}.getType());
			} catch (final Exception e) {
				LOGGER.error("Error converting configurationFromEnv", e);
				this.configuration = configuration;
			}
		} else {
			this.configuration = configuration;
		}
	}

}
