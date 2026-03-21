package com.abinbev.b2b.price.api.config.properties;

import java.lang.reflect.Type;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;

@Configuration
@ConfigurationProperties(prefix = "database.zone")
public class DatabaseCollectionPropertiesV2 {

	@Value("#{environment.DATABASE_ZONE_COLLECTIONS_V2}")
	private String collectionsEnvV2;

	private Map<String, String> collectionsV2;
	
	@Value("${database.queryTimeoutMS}")
	private Long queryTimeoutMS;
	
	public String getCollectionByCountryV2(final String country) {

		return collectionsV2.get(country.toUpperCase());
	}

	public Map<String, String> getCollectionsV2() {

		return collectionsV2;
	}

	public void setCollectionsV2(final Map<String, String> collections) {

		if (StringUtils.isNotBlank(collectionsEnvV2)) {
			try {
				final Type type = new TypeReference<Map<String, String>>() {
				}.getType();
				collectionsV2 = new Gson().fromJson(collectionsEnvV2, type);
			} catch (final Exception e) {
				collectionsV2 = collections;
			}
		} else {
			collectionsV2 = collections;
		}
	}

	public Long getQueryTimeoutMS() {
		return queryTimeoutMS;
	}

	public void setQueryTimeoutMS(final Long queryTimeoutMS) {
		this.queryTimeoutMS = queryTimeoutMS;
	}

}
