package com.abinbev.b2b.price.api.config;

import static org.apache.commons.collections4.MapUtils.getString;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.google.gson.Gson;

@Configuration
@ConfigurationProperties(prefix = "api")
public class ApiConfig {

	private static final Logger log = LoggerFactory.getLogger(ApiConfig.class);

	@Value("#{environment.DEFAULT_VENDOR_ID}")
	private String propertiesFromEnv;

	private String useDefaultVendorId;

	private Map<String, String> defaultVendorId;

	private Integer priceV3RequestBodyItemsLimit;

	private String excludeDDCAndVendorIdFromQuery;

	public Map<String, String> getDefaultVendorId() {

		return defaultVendorId;
	}

	public void setDefaultVendorId(final Map<String, String> defaultVendorId) {

		if (isNoneBlank(propertiesFromEnv)) {
			try {
				this.defaultVendorId = new Gson().fromJson(propertiesFromEnv, Map.class);
			} catch (final Exception ex) {
				log.error("Error trying to get defaultVendorId from env: " + ex.getMessage(), ex);
				this.defaultVendorId = defaultVendorId;
			}
		} else {
			this.defaultVendorId = defaultVendorId;
		}
	}

	public String getDefaultVendorIdByCountry(final String country) {

		return getString(defaultVendorId, country.toUpperCase(), EMPTY);
	}

	public String getUseDefaultVendorId() {

		return useDefaultVendorId;
	}

	public void setUseDefaultVendorId(final String useDefaultVendorId) {

		this.useDefaultVendorId = Optional.ofNullable(useDefaultVendorId).orElse("");
	}

	public boolean shouldUseDefaultVendorId(final String country) {

		return getUseDefaultVendorId().toUpperCase().contains(country.toUpperCase());
	}

	public Integer getPriceV3RequestBodyItemsLimit() {

		return priceV3RequestBodyItemsLimit;
	}

	public void setPriceV3RequestBodyItemsLimit(final Integer priceV3RequestBodyItemsLimit) {

		this.priceV3RequestBodyItemsLimit = priceV3RequestBodyItemsLimit;
	}

	public String getExcludeDDCAndVendorIdFromQuery() {

		return excludeDDCAndVendorIdFromQuery;
	}

	public void setExcludeDDCAndVendorIdFromQuery(String excludeDDCAndVendorIdFromQuery) {

		this.excludeDDCAndVendorIdFromQuery = excludeDDCAndVendorIdFromQuery;
	}

	public List<String> getExcludedDDCAndVendorIdFromQueryAsList() {
		return excludeDDCAndVendorIdFromQuery != null
				? Arrays.asList(excludeDDCAndVendorIdFromQuery.split(","))
				: Collections.emptyList();
	}
}
