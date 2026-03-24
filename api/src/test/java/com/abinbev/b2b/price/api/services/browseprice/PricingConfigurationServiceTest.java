package com.abinbev.b2b.price.api.services.browseprice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abinbev.b2b.price.api.config.properties.PricingConfigurationProperties;
import com.abinbev.b2b.price.api.domain.bre.WhenApplyRoundingOnTaxesEnum;
import com.abinbev.b2b.price.api.helpers.ApiConstants;

@ExtendWith(MockitoExtension.class)
class PricingConfigurationServiceTest {

	private static final String COUNTRY = "br";
	private static final String MOCKED_CONFIG_KEY = "MOCKED_CONFIG_KEY";

	@Mock
	private PricingConfigurationProperties pricingConfigurationProperties;

	@InjectMocks
	private PricingConfigurationService pricingConfigurationService;

	@Test
	void shouldReturnBooleanWhenConfigurationIsValid() {

		final Map<String, String> configByKey = Map.of(MOCKED_CONFIG_KEY, "true");

		doReturn(configByKey).when(pricingConfigurationProperties).getConfigurationByCountry(COUNTRY);

		assertThat(pricingConfigurationService.getPriceConfig(COUNTRY, MOCKED_CONFIG_KEY), is(equalTo(Boolean.TRUE)));
	}

	@Test
	void shouldReturnEnumWhenConfigurationEqualsToOnEachTax() {

		final Map<String, String> configByKey = Map.of(ApiConstants.WHEN_APPLY_ROUNDING_ON_TAXES, "ON_EACH_TAX");

		doReturn(configByKey).when(pricingConfigurationProperties).getConfigurationByCountry(COUNTRY);

		assertThat(pricingConfigurationService.getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY),
				is(equalTo(WhenApplyRoundingOnTaxesEnum.ON_EACH_TAX)));
	}

	@Test
	void shouldReturnEnumWhenConfigurationEqualsToInTheEnd() {

		final Map<String, String> configByKey = Map.of(ApiConstants.WHEN_APPLY_ROUNDING_ON_TAXES, "IN_THE_END");

		doReturn(configByKey).when(pricingConfigurationProperties).getConfigurationByCountry(COUNTRY);

		assertThat(pricingConfigurationService.getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY),
				is(equalTo(WhenApplyRoundingOnTaxesEnum.IN_THE_END)));
	}

}
