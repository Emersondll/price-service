package com.abinbev.b2b.price.api.services.browseprice;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.MapUtils;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abinbev.b2b.price.api.domain.TaxFact;
import com.abinbev.b2b.price.api.domain.TaxOrderSubTotalFact;
import com.abinbev.b2b.price.api.domain.bre.WhenApplyRoundingOnTaxesEnum;
import com.abinbev.b2b.price.api.helpers.ApiConstants;
import com.newrelic.api.agent.Trace;

@Service
public class CalculateTaxService {

	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
	private static final MathContext DEFAULT_MATH_CONTEXT = MathContext.DECIMAL128;

	private final PricingConfigurationService pricingConfigurationService;
	private final RoundValueService roundValueService;

	@Autowired
	public CalculateTaxService(final RoundValueService roundValueService, final PricingConfigurationService pricingConfigurationService) {

		this.pricingConfigurationService = pricingConfigurationService;
		this.roundValueService = roundValueService;
	}

	public BigDecimal execute(final Map<String, TaxFact> taxes, final BigDecimal taxBasePrice, final BigDecimal itemBasePrice,
			final String country) {

		return MapUtils.isNotEmpty(taxes) ? getCalculatedTaxes(taxes, taxBasePrice, itemBasePrice, country) : BigDecimal.ZERO;
	}

	@Trace(dispatcher = true)
	private BigDecimal getCalculatedTaxes(final Map<String, TaxFact> taxes, final BigDecimal taxBasePrice, final BigDecimal itemBasePrice,
			final String country) {

		final Map<String, BigDecimal> mappedTaxValueByTaxId = new HashMap<>();

		BigDecimal taxHidden = BigDecimal.ZERO;
		final List<String> taxIds = new ArrayList<>();

		for (final TaxFact tax : taxes.values()) {
			if (isTaxApplicable(itemBasePrice, tax, country)) {
				final var entryTaxIdTaxAmount = getMapOfCalculatedTaxValueByTaxId(taxes, tax, taxBasePrice, country);
				mappedTaxValueByTaxId.put(entryTaxIdTaxAmount.getKey(), entryTaxIdTaxAmount.getValue());

				if (tax.isHidden()) {
					taxHidden = taxHidden.add(mappedTaxValueByTaxId.get(tax.getTaxId()));
					taxIds.add(tax.getTaxId());
				}
			}
		}
		taxIds.forEach(mappedTaxValueByTaxId.keySet()::remove);

		return MapUtils.isNotEmpty(mappedTaxValueByTaxId) ?
				sumTaxValuesAndRoundIfApplicable(mappedTaxValueByTaxId, taxHidden, country) :
				BigDecimal.ZERO;
	}

	private boolean isTaxApplicable(final BigDecimal itemBasePrice, final TaxFact tax, final String country) {

		if (pricingConfigurationService.getPriceConfig(country, ApiConstants.EXCLUDE_CONDITIONAL_TAXES_FROM_BROWSE_PRICE)
				&& isTaxWithCondition(tax)) {
			return false;
		}

		return pricingConfigurationService.getPriceConfig(country, ApiConstants.PRICES_IGNORE_TAX_CONDITION) || !isTaxWithCondition(tax)
				|| hasItemBasePriceGreaterThanOrderMinimumPrice(itemBasePrice, tax);
	}

	private boolean isTaxWithCondition(final TaxFact tax) {

		return tax.getConditions() != null;
	}

	private boolean hasItemBasePriceGreaterThanOrderMinimumPrice(final BigDecimal itemBasePrice, final TaxFact tax) {

		final BigDecimal orderMinimumPrice = Optional.ofNullable(tax.getConditions().getOrderSubTotal())
				.map(TaxOrderSubTotalFact::getMinimumValue).orElse(null);

		return itemBasePrice != null && orderMinimumPrice != null && itemBasePrice.compareTo(orderMinimumPrice) >= 0;
	}

	private Map.Entry<String, BigDecimal> getMapOfCalculatedTaxValueByTaxId(final Map<String, TaxFact> taxes, final TaxFact tax,
			final BigDecimal taxBasePrice, final String country) {

		return Map.entry(tax.getTaxId(), sumTaxDependenciesIfExists(taxes, tax, taxBasePrice, country));
	}

	private BigDecimal sumTaxDependenciesIfExists(final Map<String, TaxFact> taxes, final TaxFact tax, final BigDecimal taxBasePrice,
			final String country) {

		BigDecimal dependenciesAmount = BigDecimal.ZERO;

		if (isNotEmpty(tax.getTaxBaseInclusionIds())) {
			for (final String dependencyId : tax.getTaxBaseInclusionIds()) {
				dependenciesAmount = dependenciesAmount
						.add(sumTaxDependenciesIfExists(taxes, taxes.get(dependencyId), taxBasePrice, country));
			}
		}

		return calculateTaxAmountBasedOnTaxType(tax, dependenciesAmount, taxBasePrice, country);
	}

	private BigDecimal calculateTaxAmountBasedOnTaxType(final TaxFact tax, final BigDecimal dependenciesAmount,
			final BigDecimal taxBasePrice, final String country) {

		final BigDecimal taxAmount = ApiConstants.TAX_PERCENT_TYPE.equals(tax.getType()) ?
				calculateTaxAmountByPercentage(tax, dependenciesAmount, taxBasePrice) :
				tax.getValue();

		if (pricingConfigurationService.getPriceConfigWhenApplyRoundingOnTaxesEnum(country) == WhenApplyRoundingOnTaxesEnum.ON_EACH_TAX) {
			return roundValueService.execute(taxAmount);
		}

		return taxAmount;
	}

	private BigDecimal calculateTaxAmountByPercentage(final TaxFact tax, final BigDecimal dependenciesAmount,
			final BigDecimal taxBasePrice) {

		final BigDecimal basePrice = tax.getBase() != null ? tax.getBase().add(dependenciesAmount) : taxBasePrice.add(dependenciesAmount);

		return tax.getValue().multiply(basePrice).divide(ONE_HUNDRED, DEFAULT_MATH_CONTEXT);
	}

	private BigDecimal sumTaxValuesAndRoundIfApplicable(final Map<String, BigDecimal> mappedTaxValueByTaxId, final BigDecimal taxHidden,
			final String country) {

		final BigDecimal summedTaxes = sumProductTaxesValues(mappedTaxValueByTaxId.values());

		final Pair<BigDecimal, BigDecimal> calculatedItemTax = shouldRoundTaxAmount(country) ?
				new Pair<>(roundValueService.execute(summedTaxes), roundValueService.execute(taxHidden)) :
				new Pair<>(summedTaxes, taxHidden);

		return calculatedItemTax.getValue0().add(calculatedItemTax.getValue1());
	}

	private BigDecimal sumProductTaxesValues(final Collection<BigDecimal> taxesValuesList) {

		BigDecimal taxesSum = BigDecimal.ZERO;

		for (final BigDecimal tax : taxesValuesList) {
			taxesSum = taxesSum.add(tax);
		}

		return taxesSum;
	}

	private boolean shouldRoundTaxAmount(final String country) {

		return pricingConfigurationService.getPriceConfigWhenApplyRoundingOnTaxesEnum(country) == WhenApplyRoundingOnTaxesEnum.IN_THE_END;
	}
}
