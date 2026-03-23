package com.abinbev.b2b.price.api.services.browseprice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abinbev.b2b.price.api.config.properties.PricingConfigurationProperties;
import com.abinbev.b2b.price.api.domain.bre.WhenApplyRoundingOnTaxesEnum;
import com.abinbev.b2b.price.api.helpers.ApiConstants;

@Service
public class PricingConfigurationService {

	private final PricingConfigurationProperties pricingConfigurationProperties;

	@Autowired
	public PricingConfigurationService(final PricingConfigurationProperties pricingConfigurationProperties) {

		this.pricingConfigurationProperties = pricingConfigurationProperties;
	}

	public boolean getPriceConfig(final String country, final String priceConfigKey) {

		return Boolean.parseBoolean(pricingConfigurationProperties.getConfigurationByCountry(country).get(priceConfigKey));
	}

	public WhenApplyRoundingOnTaxesEnum getPriceConfigWhenApplyRoundingOnTaxesEnum(final String country) {

		return WhenApplyRoundingOnTaxesEnum
				.valueOf(pricingConfigurationProperties.getConfigurationByCountry(country).get(ApiConstants.WHEN_APPLY_ROUNDING_ON_TAXES));
	}

}
