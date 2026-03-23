package com.abinbev.b2b.price.api.services.browseprice;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abinbev.b2b.price.api.domain.ChargeFact;
import com.abinbev.b2b.price.api.domain.TaxFact;
import com.abinbev.b2b.price.api.domain.bre.WhenApplyRoundingOnTaxesEnum;
import com.abinbev.b2b.price.api.helpers.ApiConstants;
import com.newrelic.api.agent.Trace;

@Service
public class CalculateOrderItemChargeAmountService {

	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
	private static final MathContext DEFAULT_MATH_CONTEXT = MathContext.DECIMAL128;

	private final PricingConfigurationService pricingConfigurationService;
	private final CalculateTaxService calculateTaxService;
	private final RoundValueService roundValueService;

	@Autowired
	public CalculateOrderItemChargeAmountService(final PricingConfigurationService pricingConfigurationService,
			final CalculateTaxService calculateTaxService, final RoundValueService roundValueService) {

		this.pricingConfigurationService = pricingConfigurationService;
		this.calculateTaxService = calculateTaxService;
		this.roundValueService = roundValueService;
	}

	@Trace(dispatcher = true)
	public BigDecimal execute(final Map<String, ChargeFact> charges, final BigDecimal taxBasePrice, final Map<String, TaxFact> taxes,
			final String country) {

		return MapUtils.isNotEmpty(charges) ?
				getCalculatedChargeAmountWithChargeTax(charges, taxBasePrice, taxes, country) :
				BigDecimal.ZERO;
	}

	private BigDecimal getCalculatedChargeAmountWithChargeTax(final Map<String, ChargeFact> charges, final BigDecimal taxBasePrice,
			final Map<String, TaxFact> taxes, final String country) {

		final List<BigDecimal> chargeTaxes = new ArrayList<>();

		for (final ChargeFact charge : charges.values()) {
			chargeTaxes.add(calculateChargeAmountAndTaxBasedOnChargeType(charge, taxBasePrice,
					Objects.requireNonNullElseGet(taxes, Collections::emptyMap), country));
		}

		return calculateChargeAmount(chargeTaxes, country);
	}

	private BigDecimal calculateChargeAmountAndTaxBasedOnChargeType(final ChargeFact charge, final BigDecimal taxBasePrice,
			final Map<String, TaxFact> taxes, final String country) {

		return ApiConstants.TAX_PERCENT_TYPE.equals(charge.getType()) ?
				calculateChargeAmountByPercentage(taxBasePrice, charge, taxes, country) :
				calculateChargeAmountByFinancial(charge, taxes, country);
	}

	private BigDecimal calculateChargeAmountByPercentage(final BigDecimal taxBasePrice, final ChargeFact charge,
			final Map<String, TaxFact> taxes, final String country) {

		final BigDecimal basePrice = charge.getBase() != null ? charge.getBase() : taxBasePrice;
		final BigDecimal chargeValue = charge.getValue().multiply(basePrice).divide(ONE_HUNDRED, DEFAULT_MATH_CONTEXT);

		return calculateChargesTaxes(taxes, chargeValue, country);
	}

	private BigDecimal calculateChargeAmountByFinancial(final ChargeFact charge, final Map<String, TaxFact> taxes, final String country) {

		final BigDecimal chargeValue = charge.getValue() != null ? charge.getValue() : BigDecimal.ZERO;

		return calculateChargesTaxes(taxes, chargeValue, country);
	}

	private BigDecimal calculateChargesTaxes(final Map<String, TaxFact> taxes, final BigDecimal chargeValue, final String country) {

		final BigDecimal chargeTaxAmount = calculateTaxService.execute(taxes, chargeValue, BigDecimal.ZERO, country);
		final BigDecimal summedChargeValueWithTax = chargeValue.add(chargeTaxAmount);

		return pricingConfigurationService.getPriceConfigWhenApplyRoundingOnTaxesEnum(country) == WhenApplyRoundingOnTaxesEnum.ON_EACH_TAX ?
				roundValueService.execute(summedChargeValueWithTax) :
				summedChargeValueWithTax;
	}

	private BigDecimal calculateChargeAmount(final List<BigDecimal> chargeTaxes, final String country) {

		final BigDecimal chargeAmount = getSummedChargeAmountAndTax(chargeTaxes);

		return shouldRoundTaxAmount(country) ? roundValueService.execute(chargeAmount) : chargeAmount;
	}

	private BigDecimal getSummedChargeAmountAndTax(final List<BigDecimal> chargeTaxes) {

		BigDecimal chargeAmount = BigDecimal.ZERO;

		for (final BigDecimal pairedChargeAmountWithChargeTax : chargeTaxes) {
			chargeAmount = chargeAmount.add(pairedChargeAmountWithChargeTax);
		}

		return chargeAmount;
	}

	private boolean shouldRoundTaxAmount(final String country) {

		return pricingConfigurationService.getPriceConfigWhenApplyRoundingOnTaxesEnum(country) == WhenApplyRoundingOnTaxesEnum.IN_THE_END;
	}

}
