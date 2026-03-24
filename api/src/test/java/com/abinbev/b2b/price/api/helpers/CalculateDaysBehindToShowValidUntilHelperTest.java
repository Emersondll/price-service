package com.abinbev.b2b.price.api.helpers;

import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_COUNTRY_BR;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_COUNTRY_US;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_TIME_ZONE_ID_NY;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_TIME_ZONE_ID_SP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abinbev.b2b.price.api.config.properties.PricingConfigurationProperties;

@ExtendWith(MockitoExtension.class)
class CalculateDaysBehindToShowValidUntilHelperTest {

	@Mock
	private PricingConfigurationProperties pricingConfigurationProperties;

	@InjectMocks
	private CalculateDaysBehindToShowValidUntilHelper calculateDaysBehindToShowValidUntilHelper;

	@Test
	void shouldValidateWhenValidUntilIsAbleToBeShownForUs() {

		final Map<String, String> configByKey = Map.of(ApiConstants.DAYS_BEHIND_TO_SHOW_VALID_UNTIL, "14");

		Mockito.when(pricingConfigurationProperties.getConfigurationByCountry("us")).thenReturn(configByKey);

		assertThat(calculateDaysBehindToShowValidUntilHelper
				.shouldShowValidUntil(getDatePlusDays(13, MOCKED_TIME_ZONE_ID_NY), MOCKED_COUNTRY_US, MOCKED_TIME_ZONE_ID_NY), is(true));
	}

	@Test
	void shouldValidateWhenValidUntilIsNotAbleToBeShownForUs() {

		final Map<String, String> configByKey = Map.of(ApiConstants.DAYS_BEHIND_TO_SHOW_VALID_UNTIL, "14");

		Mockito.when(pricingConfigurationProperties.getConfigurationByCountry("us")).thenReturn(configByKey);

		assertThat(calculateDaysBehindToShowValidUntilHelper
				.shouldShowValidUntil(getDatePlusDays(15, MOCKED_TIME_ZONE_ID_NY), MOCKED_COUNTRY_US, MOCKED_TIME_ZONE_ID_NY), is(false));
	}

	@Test
	void shouldValidateWhenValidUntilIsNotAbleToBeShownForUsSecondScenario() {

		final Map<String, String> configByKey = Map.of(ApiConstants.DAYS_BEHIND_TO_SHOW_VALID_UNTIL, "14");

		Mockito.when(pricingConfigurationProperties.getConfigurationByCountry("us")).thenReturn(configByKey);

		assertThat(calculateDaysBehindToShowValidUntilHelper
				.shouldShowValidUntil(getDatePlusDays(14, MOCKED_TIME_ZONE_ID_NY), MOCKED_COUNTRY_US, MOCKED_TIME_ZONE_ID_NY), is(false));
	}

	@Test
	void shouldValidateWhenValidUntilIsAbleToBeShownOtherCountries() {

		final Map<String, String> configByKey = Map.of(ApiConstants.DAYS_BEHIND_TO_SHOW_VALID_UNTIL, "0");

		Mockito.when(pricingConfigurationProperties.getConfigurationByCountry("br")).thenReturn(configByKey);

		assertThat(calculateDaysBehindToShowValidUntilHelper
				.shouldShowValidUntil(getDatePlusDays(20, MOCKED_TIME_ZONE_ID_SP), MOCKED_COUNTRY_BR, MOCKED_TIME_ZONE_ID_SP), is(true));
	}

	@Test
	void shouldValidateWhenValidUntilIsAbleToBeShownOtherCountriesSecondScenario() {

		final Map<String, String> configByKey = Map.of(ApiConstants.DAYS_BEHIND_TO_SHOW_VALID_UNTIL, "0");

		Mockito.when(pricingConfigurationProperties.getConfigurationByCountry("br")).thenReturn(configByKey);

		assertThat(calculateDaysBehindToShowValidUntilHelper
				.shouldShowValidUntil(getDatePlusDays(5, MOCKED_TIME_ZONE_ID_SP), MOCKED_COUNTRY_BR, MOCKED_TIME_ZONE_ID_SP), is(true));
	}

	@Test
	void shouldValidateWhenValidUntilIsAbleToBeShownOtherCountriesThirdScenario() {

		final Map<String, String> configByKey = Map.of(ApiConstants.DAYS_BEHIND_TO_SHOW_VALID_UNTIL, "0");

		Mockito.when(pricingConfigurationProperties.getConfigurationByCountry("br")).thenReturn(configByKey);

		assertThat(calculateDaysBehindToShowValidUntilHelper
				.shouldShowValidUntil(getDatePlusDays(-1, MOCKED_TIME_ZONE_ID_SP), MOCKED_COUNTRY_BR, MOCKED_TIME_ZONE_ID_SP), is(true));
	}

	private String getDatePlusDays(final int daysToAdd, final String timezone) {

		final ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(timezone)).plusDays(daysToAdd);
		return zonedDateTime.toLocalDate().toString();
	}

}
