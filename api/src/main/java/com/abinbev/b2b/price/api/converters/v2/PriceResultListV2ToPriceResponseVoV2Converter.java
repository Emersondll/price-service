package com.abinbev.b2b.price.api.converters.v2;

import static java.util.Optional.ofNullable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.abinbev.b2b.price.api.domain.v2.PriceResultListV2;
import com.abinbev.b2b.price.api.rest.vo.v2.ChargeResponseVoV2;
import com.abinbev.b2b.price.api.rest.vo.v2.PriceResponseVoV2;
import com.abinbev.b2b.price.api.rest.vo.v2.PriceVoV2;
import com.abinbev.b2b.price.api.rest.vo.v2.PromotionalPriceResponseVoV2;
import com.abinbev.b2b.price.api.rest.vo.v2.TaxConditionResponseVoV2;
import com.abinbev.b2b.price.api.rest.vo.v2.TaxOrderSubTotalResponseVoV2;
import com.abinbev.b2b.price.api.rest.vo.v2.TaxResponseVoV2;
import com.abinbev.b2b.price.domain.model.v2.ChargeV2;
import com.abinbev.b2b.price.domain.model.v2.PriceEntityV2;
import com.abinbev.b2b.price.domain.model.v2.PromotionalPriceV2;
import com.abinbev.b2b.price.domain.model.v2.TaxConditionV2;
import com.abinbev.b2b.price.domain.model.v2.TaxOrderSubTotalV2;
import com.abinbev.b2b.price.domain.model.v2.TaxV2;
import com.abinbev.b2b.price.domain.model.v2.enums.PriceMeasureUnitType;

@Component
public class PriceResultListV2ToPriceResponseVoV2Converter {

	public PriceResponseVoV2 convert(final PriceResultListV2 priceResultListV2) {

		final PriceResponseVoV2 priceResponseVoV2 = new PriceResponseVoV2();
		priceResponseVoV2.setPrices(convertPriceEntityV2ListToPriceVoV2List(priceResultListV2.getPriceEntities()));
		priceResponseVoV2.setPagination(priceResultListV2.getPagination());
		return priceResponseVoV2;
	}

	private List<PriceVoV2> convertPriceEntityV2ListToPriceVoV2List(final List<PriceEntityV2> priceEntities) {

		final List<PriceVoV2> priceVoV2List = new ArrayList<>();

		for (final PriceEntityV2 priceEntity : priceEntities) {
			priceVoV2List.add(convertNotNullablePriceEntityV2ToPriceVoV2(priceEntity));
		}

		return priceVoV2List;
	}

	private PriceVoV2 convertNotNullablePriceEntityV2ToPriceVoV2(final PriceEntityV2 priceEntityV2) {

		final PriceVoV2 priceVoV2 = new PriceVoV2();
		priceVoV2.setVendorId(priceEntityV2.getId().getVendorId());
		priceVoV2.setVendorAccountId(priceEntityV2.getId().getId());
		priceVoV2.setVendorItemId(priceEntityV2.getId().getVendorItemId());
		priceVoV2.setValidFrom(convertValidFrom(priceEntityV2.getId().getValidFrom(), priceEntityV2.getTimezone()));
		priceVoV2.setSku(priceEntityV2.getSku());
		priceVoV2.setBasePrice(priceEntityV2.getBasePrice());
		priceVoV2.setMinimumPrice(priceEntityV2.getMinimumPrice());
		priceVoV2.setMeasureUnit(priceEntityV2.getMeasureUnit());
		priceVoV2.setType(convertType(priceEntityV2.getType()));
		priceVoV2.setDeposit(priceEntityV2.getDeposit());
		priceVoV2.setConsignment(priceEntityV2.getConsignment());
		priceVoV2.setQuantityPerPallet(priceEntityV2.getQuantityPerPallet());
		priceVoV2.setTimezone(priceEntityV2.getTimezone());
		priceVoV2.setPromotionalPrice(convertNullablePromotionalPriceV2ToPromotionalPriceResponseVoV2(priceEntityV2.getPromotionalPrice()));
		priceVoV2.setTaxes(ofNullable(priceEntityV2.getTaxes()).map(this::convertNullableTaxV2ToTaxResponseVoV2).orElse(null));
		priceVoV2.setCharges(ofNullable(priceEntityV2.getCharges()).map(this::convertNullableChargeV2ToChargeResponseVoV2).orElse(null));

		return priceVoV2;
	}

	private String convertType(final PriceMeasureUnitType type) {

		return Objects.nonNull(type) ? type.name() : "PER_UNIT";
	}

	private String convertValidFrom(final Instant validFrom, final String timezone) {

		if (validFrom == null) {
			return null;
		}
		return validFrom.atZone(ZoneId.of(timezone)).format(DateTimeFormatter.ISO_LOCAL_DATE);
	}

	private PromotionalPriceResponseVoV2 convertNullablePromotionalPriceV2ToPromotionalPriceResponseVoV2(
			final PromotionalPriceV2 promotionalPrice) {

		if (promotionalPrice == null) {
			return null;
		}

		final PromotionalPriceResponseVoV2 promotionalPriceResponseVoV2 = new PromotionalPriceResponseVoV2();
		promotionalPriceResponseVoV2.setPrice(promotionalPrice.getPrice());
		promotionalPriceResponseVoV2.setExternalId(promotionalPrice.getExternalId());
		promotionalPriceResponseVoV2.setValidUntil(promotionalPrice.getValidUntil());
		return promotionalPriceResponseVoV2;
	}

	private Map<String, TaxResponseVoV2> convertNullableTaxV2ToTaxResponseVoV2(final Map<String, TaxV2> taxes) {

		final Map<String, TaxResponseVoV2> taxResponseVoV2Map = new HashMap<>();

		for (final var taxV2Entry : taxes.entrySet()) {
			taxResponseVoV2Map.put(taxV2Entry.getKey(), convertNotNullableTaxV2ToTaxResponseVoV2(taxV2Entry.getValue()));
		}

		return taxResponseVoV2Map;
	}

	private TaxResponseVoV2 convertNotNullableTaxV2ToTaxResponseVoV2(final TaxV2 tax) {

		final TaxResponseVoV2 taxResponseVoV2 = new TaxResponseVoV2();
		taxResponseVoV2.setTaxId(tax.getTaxId());
		taxResponseVoV2.setType(tax.getType());
		taxResponseVoV2.setValue(tax.getValue());
		taxResponseVoV2.setTaxBaseInclusionIds(tax.getTaxBaseInclusionIds());
		taxResponseVoV2.setHidden(tax.isHidden());
		taxResponseVoV2.setBase(tax.getBase());
		taxResponseVoV2.setConditions(convertNullableTaxConditionV2ToTaxConditionResponseVoV2(tax.getConditions()));
		taxResponseVoV2.setProportional(tax.getProportional());
		return taxResponseVoV2;
	}

	private TaxConditionResponseVoV2 convertNullableTaxConditionV2ToTaxConditionResponseVoV2(final TaxConditionV2 conditions) {

		if (conditions == null) {
			return null;
		}

		final TaxConditionResponseVoV2 taxConditionResponseVoV2 = new TaxConditionResponseVoV2();
		taxConditionResponseVoV2
				.setOrderSubTotal(convertNullableTaxOrderSubTotalV2ToTaxOrderSubTotalResponseVoV2(conditions.getOrderSubTotal()));
		return taxConditionResponseVoV2;
	}

	private TaxOrderSubTotalResponseVoV2 convertNullableTaxOrderSubTotalV2ToTaxOrderSubTotalResponseVoV2(
			final TaxOrderSubTotalV2 orderSubTotal) {

		if (orderSubTotal == null) {
			return null;
		}
		final TaxOrderSubTotalResponseVoV2 taxOrderSubTotalResponseVoV2 = new TaxOrderSubTotalResponseVoV2();
		taxOrderSubTotalResponseVoV2.setMinimumValue(orderSubTotal.getMinimumValue());
		return taxOrderSubTotalResponseVoV2;
	}

	private Map<String, ChargeResponseVoV2> convertNullableChargeV2ToChargeResponseVoV2(final Map<String, ChargeV2> charges) {

		final Map<String, ChargeResponseVoV2> chargeResponseVoV2Map = new HashMap<>();

		for (final var chargeEntry : charges.entrySet()) {
			chargeResponseVoV2Map.put(chargeEntry.getKey(), convertNotNullableChargeV2ToChargeResponseVoV2(chargeEntry.getValue()));
		}

		return chargeResponseVoV2Map;
	}

	private ChargeResponseVoV2 convertNotNullableChargeV2ToChargeResponseVoV2(final ChargeV2 charge) {

		final ChargeResponseVoV2 chargeResponseVoV2 = new ChargeResponseVoV2();
		chargeResponseVoV2.setChargeId(charge.getChargeId());
		chargeResponseVoV2.setType(charge.getType());
		chargeResponseVoV2.setValue(charge.getValue());
		chargeResponseVoV2.setBase(charge.getBase());
		return chargeResponseVoV2;
	}
}
