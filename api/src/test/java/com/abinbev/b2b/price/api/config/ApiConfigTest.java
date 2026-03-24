package com.abinbev.b2b.price.api.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import com.abinbev.b2b.price.api.PriceApiApplication;

@SpringBootTest
@ContextConfiguration(classes = { PriceApiApplication.class })
class ApiConfigTest {

	private static final String COUNTRY_BR = "BR";
	private static final String UNKNOWN_COUNTRY = "XY";
	private static final String DEFAULT_VENDOR_ID = "44092477-b173-477a-a24a-25eeab20fccb";
	private static final String COUNTRY_CA = "CA";
	private static final String DEFAULT_VENDOR_ID_CONFIG = "{\"BR\":\"44092477-b173-477a-a24a-25eeab20fccb\",\"CA\":\"86e691a4-16a8-4478-bc43-461c76fad14b\"}";

	@Autowired
	private ApiConfig apiConfig;

	@Test
	void shouldLoadDefaultValuesWhenUsingMainConfiguration() {

		assertThat(apiConfig.getDefaultVendorId(), aMapWithSize(3));
		assertThat(apiConfig.getPriceV3RequestBodyItemsLimit(), is(equalTo(50)));
	}

	@Test
	void shouldReturnCorrectVendorIdWhenCountryIsBr() {

		assertThat(apiConfig.getDefaultVendorIdByCountry(COUNTRY_BR), is(DEFAULT_VENDOR_ID));
	}

	@Test
	void shouldReturnEmptyStringWhenUsingInvalidCountry() {

		assertThat(apiConfig.getDefaultVendorIdByCountry(UNKNOWN_COUNTRY), emptyString());
	}

	@Test
	void shouldSetDefaultVendorIdMapWhenPropertiesFromEnvIsNull() {

		ReflectionTestUtils.setField(apiConfig, "propertiesFromEnv", null);

		final Map<String, String> defaultVendorIdMap = Map.of(UNKNOWN_COUNTRY, DEFAULT_VENDOR_ID);

		apiConfig.setDefaultVendorId(defaultVendorIdMap);

		assertAll(() -> assertThat(apiConfig.getDefaultVendorId(), is(aMapWithSize(1))),
				() -> assertThat(apiConfig.getDefaultVendorId(), hasKey("XY")),
				() -> assertThat(apiConfig.getDefaultVendorId().get(UNKNOWN_COUNTRY),
						is("44092477-b173-477a-a24a-25eeab20fccb")));
	}

	@Test
	void shouldSetDefaultVendorIdMapWhenPropertiesFromEnvIsSet() {

		ReflectionTestUtils.setField(apiConfig, "propertiesFromEnv", DEFAULT_VENDOR_ID_CONFIG);

		apiConfig.setDefaultVendorId(null);

		assertAll(() -> assertThat(apiConfig.getDefaultVendorId(), is(aMapWithSize(2))),
				() -> assertThat(apiConfig.getDefaultVendorId().keySet(), contains("BR", "CA")),
				() -> assertThat(apiConfig.getDefaultVendorId().get(COUNTRY_BR), is("44092477-b173-477a-a24a-25eeab20fccb")),
				() -> assertThat(apiConfig.getDefaultVendorId().get(COUNTRY_CA), is("86e691a4-16a8-4478-bc43-461c76fad14b")));
	}
}