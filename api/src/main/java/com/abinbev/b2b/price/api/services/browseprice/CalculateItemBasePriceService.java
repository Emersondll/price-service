package com.abinbev.b2b.price.api.services.browseprice;

import java.math.BigDecimal;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abinbev.b2b.price.api.domain.PriceFact;
import com.abinbev.b2b.price.api.helpers.ApiConstants;
import com.newrelic.api.agent.Trace;

@Service
public class CalculateItemBasePriceService {

	private final PricingConfigurationService pricingConfigurationService;

	@Autowired
	public CalculateItemBasePriceService(final PricingConfigurationService pricingConfigurationService) {

		this.pricingConfigurationService = pricingConfigurationService;
	}

	@Trace(dispatcher = true)
	public BigDecimal execute(final PriceFact priceFact, final String country, final boolean ignorePromotionalPrice) {

		final BigDecimal itemBasePrice = getProductBasePrice(priceFact, ignorePromotionalPrice);

		return pricingConfigurationService.getPriceConfig(country, ApiConstants.PRICE_INCLUDE_DEPOSIT) ?
				sumProductBasePriceWithDepositAndConsignment(itemBasePrice, priceFact.getDeposit(), priceFact.getConsignment()) :
				itemBasePrice;
	}

	private BigDecimal getProductBasePrice(final PriceFact priceFact, final boolean ignorePromotionalPrice) {

		return Objects.nonNull(priceFact.getPromotionalPrice())
				&& !ignorePromotionalPrice ?
				priceFact.getPromotionalPrice().getPrice().max(priceFact.getMinimumPrice()) :
				priceFact.getBasePrice();
	}

	private BigDecimal sumProductBasePriceWithDepositAndConsignment(final BigDecimal itemBasePrice, final BigDecimal deposit,
			final BigDecimal consignment) {

		return itemBasePrice.add(getValueOrZero(deposit)).add(getValueOrZero(consignment));
	}

	private BigDecimal getValueOrZero(final BigDecimal value) {

		return value != null ? value : BigDecimal.ZERO;
	}

}
