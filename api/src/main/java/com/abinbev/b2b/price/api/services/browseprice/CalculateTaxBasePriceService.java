package com.abinbev.b2b.price.api.services.browseprice;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abinbev.b2b.price.api.helpers.ApiConstants;
import com.newrelic.api.agent.Trace;

@Service
public class CalculateTaxBasePriceService {

	private final PricingConfigurationService pricingConfigurationService;

	@Autowired
	public CalculateTaxBasePriceService(final PricingConfigurationService pricingConfigurationService) {

		this.pricingConfigurationService = pricingConfigurationService;
	}

	@Trace(dispatcher = true)
	public BigDecimal execute(final BigDecimal itemBasePrice, final BigDecimal deposit, final BigDecimal consignment,
			final String country) {

		final boolean priceInclDeposit = pricingConfigurationService.getPriceConfig(country, ApiConstants.PRICE_INCLUDE_DEPOSIT);
		final boolean taxInclDeposit = pricingConfigurationService.getPriceConfig(country, ApiConstants.TAX_INCLUDE_DEPOSIT);

		if (priceInclDeposit && !taxInclDeposit) {
			return itemBasePrice.subtract(getValueOrZero(deposit)).subtract(getValueOrZero(consignment));
		} else if (!priceInclDeposit && taxInclDeposit) {
			return itemBasePrice.add(getValueOrZero(deposit)).add(getValueOrZero(consignment));
		}
		return itemBasePrice;
	}

	private BigDecimal getValueOrZero(final BigDecimal value) {

		return value != null ? value : BigDecimal.ZERO;
	}

}
