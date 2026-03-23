package com.abinbev.b2b.price.api.services.browseprice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abinbev.b2b.price.api.domain.BrowsePrice;
import com.abinbev.b2b.price.api.domain.PriceFact;
import com.abinbev.b2b.price.api.helpers.ApiConstants;
import com.abinbev.b2b.price.api.helpers.CalculateDaysBehindToShowValidUntilHelper;
import com.newrelic.api.agent.Trace;

@Service
public class CalculateBrowsePriceService {

	private final PricingConfigurationService pricingConfigurationService;
	private final CalculateItemBasePriceService calculateItemBasePriceService;
	private final CalculateTaxBasePriceService calculateTaxBasePriceService;
	private final CalculateTaxService calculateTaxService;
	private final CalculateOrderItemChargeAmountService calculateOrderItemChargeAmountService;
	private final RoundValueService roundValueService;
	private final CalculateDaysBehindToShowValidUntilHelper calculateDaysBehindToShowValidUntilHelper;

	@Autowired
	public CalculateBrowsePriceService(final PricingConfigurationService pricingConfigurationService,
			final CalculateItemBasePriceService calculateItemBasePriceService,
			final CalculateTaxBasePriceService calculateTaxBasePriceService, final CalculateTaxService calculateTaxService,
			final CalculateOrderItemChargeAmountService calculateOrderItemChargeAmountService, final RoundValueService roundValueService,
			final CalculateDaysBehindToShowValidUntilHelper calculateDaysBehindToShowValidUntilHelper) {

		this.pricingConfigurationService = pricingConfigurationService;
		this.calculateItemBasePriceService = calculateItemBasePriceService;
		this.calculateTaxBasePriceService = calculateTaxBasePriceService;
		this.calculateTaxService = calculateTaxService;
		this.calculateOrderItemChargeAmountService = calculateOrderItemChargeAmountService;
		this.roundValueService = roundValueService;
		this.calculateDaysBehindToShowValidUntilHelper = calculateDaysBehindToShowValidUntilHelper;
	}

	@Trace(dispatcher = true)
	public BrowsePrice execute(final PriceFact priceFact, final String country) {

		final BigDecimal itemBasePrice = calculateItemBasePriceService.execute(priceFact, country, true);
		final BigDecimal itemPromotionalPrice = calculateItemBasePriceService.execute(priceFact, country, false);

		final boolean isExcludeTaxesEnabled = pricingConfigurationService
				.getPriceConfig(country, ApiConstants.EXCLUDE_TAXES_FROM_BROWSE_PRICE);

		final boolean isExcludeChargeEnabled = pricingConfigurationService
				.getPriceConfig(country, ApiConstants.EXCLUDE_CHARGE_FROM_BROWSE_PRICE);

		final BigDecimal price = calculatePrice(priceFact, country, itemPromotionalPrice, isExcludeTaxesEnabled, isExcludeChargeEnabled);
		final BigDecimal originalPrice = calculatePrice(priceFact, country, itemBasePrice, isExcludeTaxesEnabled, isExcludeChargeEnabled);

		return fillBrowsePrice(priceFact, originalPrice, price);
	}

	private BigDecimal calculatePrice(final PriceFact priceFact, final String country, BigDecimal itemBasePrice,
			final boolean isExcludeTaxesEnabled, final boolean isExcludeChargeEnabled) {

		if (pricingConfigurationService.getPriceConfig(country, ApiConstants.ROUND_TAXES_AND_DISCOUNT_BEFORE_SUBTOTAL_AND_TOTAL)) {
			itemBasePrice = roundValueService.execute(itemBasePrice);
		}

		if (isExcludeTaxesEnabled && isExcludeChargeEnabled) {
			return itemBasePrice;
		}

		BigDecimal calculatedChargeAmount = BigDecimal.ZERO;
		BigDecimal calculatedTaxes = BigDecimal.ZERO;

		final BigDecimal taxBasePrice = calculateTaxBasePriceService
				.execute(itemBasePrice, priceFact.getDeposit(), priceFact.getConsignment(), country);

		if (!isExcludeTaxesEnabled) {
			calculatedTaxes = calculateTaxService.execute(priceFact.getTaxes(), taxBasePrice, itemBasePrice, country);
		}

		if (!isExcludeChargeEnabled) {
			calculatedChargeAmount = calculateOrderItemChargeAmountService
					.execute(priceFact.getCharges(), taxBasePrice, isExcludeTaxesEnabled ? Collections.emptyMap() : priceFact.getTaxes(),
							country);
		}

		return roundValueService.execute(itemBasePrice.add(calculatedTaxes).add(calculatedChargeAmount));
	}

	private BrowsePrice fillBrowsePrice(final PriceFact item, final BigDecimal calculatedOriginalPrice,
			final BigDecimal calculatedBrowsePrice) {

		final BrowsePrice browsePrice;

		if (!Objects.isNull(item.getPromotionalPrice()) && StringUtils.isNotBlank(item.getPromotionalPrice().getValidUntil())) {

			final String timezone = item.getTimezone();

			final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
			final var today = ZonedDateTime.now(ZoneId.of(timezone));

			ZonedDateTime validUntil = null;

			try {
				validUntil = ZonedDateTime
						.of(LocalDate.parse(item.getPromotionalPrice().getValidUntil(), formatter).atTime(23, 59, 59), ZoneId.of(timezone));
			} catch (final Exception ex) {
				return returnDefaultBrowsePrice(item, calculatedBrowsePrice, null);
			}
			if (isTodayBeforeOrEqualValidUntil(today, validUntil)) {
				browsePrice = returnDefaultBrowsePrice(item, calculatedOriginalPrice, calculatedBrowsePrice);
			} else {
				browsePrice = returnDefaultBrowsePrice(item, calculatedOriginalPrice, null);
			}

		} else {

			if (Objects.isNull(item.getPromotionalPrice()) || Objects.isNull(item.getPromotionalPrice().getPrice())) {
				browsePrice = returnDefaultBrowsePrice(item, calculatedOriginalPrice, null);
			} else {
				browsePrice = returnDefaultBrowsePrice(item, calculatedOriginalPrice, calculatedBrowsePrice);
			}
		}

		return browsePrice;
	}

	private BrowsePrice returnDefaultBrowsePrice(final PriceFact item, final BigDecimal calculatedOriginalPrice,
			final BigDecimal calculatedBrowsePrice) {

		final BrowsePrice browsePrice = new BrowsePrice();

		browsePrice.setSku(item.getSku());
		browsePrice.setVendorItemId(item.getVendorItemId());

		if (Objects.isNull(calculatedBrowsePrice)) {
			browsePrice.setPrice(calculatedOriginalPrice);
			browsePrice.setOriginalPrice(null);
		} else {
			browsePrice.setPrice(calculatedBrowsePrice);
			browsePrice.setOriginalPrice(calculatedOriginalPrice);
		}

		if (shouldSetValidUntil(item)) {
			browsePrice.setValidUntil(item.getPromotionalPrice().getValidUntil());
		}

		return browsePrice;
	}

	private boolean shouldSetValidUntil(final PriceFact item) {

		return item.getPromotionalPrice() != null && item.getPromotionalPrice().getValidUntil() != null
				&& calculateDaysBehindToShowValidUntilHelper
				.shouldShowValidUntil(item.getPromotionalPrice().getValidUntil(), item.getCountry(), item.getTimezone())
				&& !hasValidUntilExpired(item);
	}

	private boolean hasValidUntilExpired(final PriceFact item) {

		final var formatter = DateTimeFormatter.ISO_DATE;
		final var validUntilAtEndOfDay = ZonedDateTime
				.of(LocalDate.parse(item.getPromotionalPrice().getValidUntil(), formatter).atTime(23, 59, 59),
						ZoneId.of(item.getTimezone()));

		final var currentZonedDateTime = ZonedDateTime.now(ZoneId.of(item.getTimezone()));
		return currentZonedDateTime.isAfter(validUntilAtEndOfDay);
	}

	private boolean isTodayBeforeOrEqualValidUntil(final ZonedDateTime today, final ZonedDateTime validUntil) {

		return !today.isAfter(validUntil);
	}

}
