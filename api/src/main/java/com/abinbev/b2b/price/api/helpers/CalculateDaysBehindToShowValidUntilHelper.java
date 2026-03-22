package com.abinbev.b2b.price.api.helpers;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.abinbev.b2b.price.api.config.properties.PricingConfigurationProperties;

@Component
public class CalculateDaysBehindToShowValidUntilHelper {

	private final PricingConfigurationProperties pricingConfigurationProperties;

	@Autowired
	public CalculateDaysBehindToShowValidUntilHelper(final PricingConfigurationProperties pricingConfigurationProperties) {

		this.pricingConfigurationProperties = pricingConfigurationProperties;
	}

	public boolean shouldShowValidUntil(final String validUntil, final String country, final String timezone) {

		final var daysBehind = Integer.parseInt(
				pricingConfigurationProperties.getConfigurationByCountry(country).get(ApiConstants.DAYS_BEHIND_TO_SHOW_VALID_UNTIL));

		if (daysBehind == 0) {
			return true;
		}

		final var formatter = DateTimeFormatter.ISO_DATE;

		final var validUntilAtEndOfDay = ZonedDateTime.of(LocalDate.parse(validUntil, formatter).atTime(23, 59, 59), ZoneId.of(timezone));

		final var currentZonedDateTime = ZonedDateTime.now(ZoneId.of(timezone));

		return currentZonedDateTime.compareTo(validUntilAtEndOfDay.minusDays(daysBehind)) >= 0;
	}
}
