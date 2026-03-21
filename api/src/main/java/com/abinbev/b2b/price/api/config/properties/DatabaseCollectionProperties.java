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
public class DatabaseCollectionProperties {

	@Value("#{environment.DATABASE_ZONE_COLLECTIONS}")
	private String collectionsEnv;

	private Map<String, String> collections;

	@Value("${database.queryTimeoutMS}")
	private Long queryTimeoutMS;

	public String getCollectionByCountry(final String country) {

		return collections.get(country.toUpperCase());
	}

	public Map<String, String> getCollections() {

		return collections;
	}

	public void setCollections(final Map<String, String> collections) {

		if (StringUtils.isNotBlank(collectionsEnv)) {
			try {
				final Type type = new TypeReference<Map<String, String>>() {
				}.getType();
				this.collections = new Gson().fromJson(collectionsEnv, type);
			} catch (final Exception e) {
				this.collections = collections;
			}
		} else {
			this.collections = collections;
		}
	}

	public Long getQueryTimeoutMS() {

		return queryTimeoutMS;
	}

	public void setQueryTimeoutMS(final Long queryTimeoutMS) {

		this.queryTimeoutMS = queryTimeoutMS;
	}
}