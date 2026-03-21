package com.abinbev.b2b.price.api.converters.v3;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.abinbev.b2b.price.api.domain.v3.PriceNormalizedInfo;
import com.abinbev.b2b.price.api.rest.vo.v3.ChargeResponseVoV3;
import com.abinbev.b2b.price.api.rest.vo.v3.PriceResponseVoV3;
import com.abinbev.b2b.price.api.rest.vo.v3.PromotionalPriceResponseVoV3;
import com.abinbev.b2b.price.api.rest.vo.v3.TaxAmountResponseVoV3;
import com.abinbev.b2b.price.api.rest.vo.v3.TaxBaseChangesCompareWithResponseVoV3;
import com.abinbev.b2b.price.api.rest.vo.v3.TaxBaseChangesResponseVoV3;
import com.abinbev.b2b.price.api.rest.vo.v3.TaxConditionResponseVoV3;
import com.abinbev.b2b.price.api.rest.vo.v3.TaxDeductionBaseChangesResponseVoV3;
import com.abinbev.b2b.price.api.rest.vo.v3.TaxDeductionResponseVoV3;
import com.abinbev.b2b.price.api.rest.vo.v3.TaxOrderSubTotalResponseVoV3;
import com.abinbev.b2b.price.api.rest.vo.v3.TaxResponseVoV3;
import com.abinbev.b2b.price.domain.model.v2.ChargeV2;
import com.abinbev.b2b.price.domain.model.v2.PriceEntityV2;
import com.abinbev.b2b.price.domain.model.v2.PromotionalPriceV2;
import com.abinbev.b2b.price.domain.model.v2.TaxAmountV2;
import com.abinbev.b2b.price.domain.model.v2.TaxBaseChangesV2;
import com.abinbev.b2b.price.domain.model.v2.TaxConditionV2;
import com.abinbev.b2b.price.domain.model.v2.TaxDeductionV2;
import com.abinbev.b2b.price.domain.model.v2.TaxOrderSubTotalV2;
import com.abinbev.b2b.price.domain.model.v2.TaxV2;
import com.abinbev.b2b.price.domain.model.v2.enums.PriceMeasureUnitType;

@Component
public class PriceNormalizedInfoToPriceResponseVoV3Converter {

	public PriceResponseVoV3 convert(final PriceNormalizedInfo priceNormalizedInfo) {

		final PriceResponseVoV3 priceResponseVoV3 = new PriceResponseVoV3();

		final PriceEntityV2 priceEntityV2 = priceNormalizedInfo.getSelectedPrice();

		if (isNull(priceEntityV2)) {
			return null;
		}

		priceResponseVoV3.setVendorItemId(priceEntityV2.getId().getVendorItemId());
		priceResponseVoV3.setValidFrom(convertValidFrom(priceEntityV2.getId().getValidFrom(), priceEntityV2.getTimezone()));
		priceResponseVoV3.setSku(priceEntityV2.getSku());
		priceResponseVoV3.setBasePrice(priceEntityV2.getBasePrice());
		priceResponseVoV3.setMinimumPrice(priceEntityV2.getMinimumPrice());
		priceResponseVoV3.setMeasureUnit(priceEntityV2.getMeasureUnit());
		priceResponseVoV3.setType(convertType(priceEntityV2.getType()));
		priceResponseVoV3.setDeposit(priceEntityV2.getDeposit());
		priceResponseVoV3.setConsignment(priceEntityV2.getConsignment());
		priceResponseVoV3.setQuantityPerPallet(priceEntityV2.getQuantityPerPallet());
		priceResponseVoV3.setSuggestedRetailPrice(priceEntityV2.getSuggestedRetailPrice());
		priceResponseVoV3.setTimezone(priceEntityV2.getTimezone());
		priceResponseVoV3.setPromotionalPrice(
				convertNullablePromotionalPriceV2ToPromotionalPriceResponseVoV3(priceEntityV2.getPromotionalPrice()));
		priceResponseVoV3.setTaxes(ofNullable(priceEntityV2.getTaxes()).map(this::convertNullableTaxV2ToTaxResponseVoV3).orElse(null));
		priceResponseVoV3.setCharges(
				ofNullable(priceEntityV2.getCharges()).map(this::convertNullableChargeV2ToChargeResponseVoV3).orElse(null));

		priceResponseVoV3.setItemId(priceNormalizedInfo.getItemId());
		priceResponseVoV3.setContractId(priceNormalizedInfo.getContractId());

		if (isNull(priceNormalizedInfo.getContractId())) {
			priceResponseVoV3.setDeliveryCenterId(priceNormalizedInfo.getDeliveryCenterId());
		}

		return priceResponseVoV3;
	}

	private String convertType(final PriceMeasureUnitType type) {

		return nonNull(type) ? type.name() : "PER_UNIT";
	}

	private String convertValidFrom(final Instant validFrom, final String timezone) {

		if (isNull(validFrom)) {
			return null;
		}
		return validFrom.atZone(ZoneId.of(timezone)).format(DateTimeFormatter.ISO_LOCAL_DATE);
	}

	private PromotionalPriceResponseVoV3 convertNullablePromotionalPriceV2ToPromotionalPriceResponseVoV3(
			final PromotionalPriceV2 promotionalPrice) {

		if (isNull(promotionalPrice)) {
			return null;
		}

		final PromotionalPriceResponseVoV3 promotionalPriceResponseVoV3 = new PromotionalPriceResponseVoV3();
		promotionalPriceResponseVoV3.setPrice(promotionalPrice.getPrice());
		promotionalPriceResponseVoV3.setExternalId(promotionalPrice.getExternalId());
		promotionalPriceResponseVoV3.setValidUntil(promotionalPrice.getValidUntil());
		return promotionalPriceResponseVoV3;
	}

	private List<TaxResponseVoV3> convertNullableTaxV2ToTaxResponseVoV3(final Map<String, TaxV2> taxes) {

		final List<TaxResponseVoV3> taxResponseVoV3List = new ArrayList<>();

		for (final var taxV2Entry : taxes.entrySet()) {
			taxResponseVoV3List.add(convertNotNullableTaxV2ToTaxResponseVoV3(taxV2Entry.getValue()));
		}

		return taxResponseVoV3List;
	}

	private TaxResponseVoV3 convertNotNullableTaxV2ToTaxResponseVoV3(final TaxV2 tax) {

		final TaxResponseVoV3 taxResponseVoV3 = new TaxResponseVoV3();
		taxResponseVoV3.setTaxId(tax.getTaxId());
		taxResponseVoV3.setType(tax.getType());
		taxResponseVoV3.setValue(tax.getValue());
		taxResponseVoV3.setTaxBaseInclusionIds(tax.getTaxBaseInclusionIds());
		taxResponseVoV3.setHidden(tax.isHidden());
		taxResponseVoV3.setBase(tax.getBase());
		taxResponseVoV3.setConditions(convertNullableTaxConditionV2ToTaxConditionResponseVoV3(tax.getConditions()));
		taxResponseVoV3.setProportional(tax.getProportional());
		taxResponseVoV3.setBaseChanges(convertNullableTaxBaseChangesV2ToTaxBaseChangesResponseVoV3(tax.getBaseChanges()));
		taxResponseVoV3.setDeduction(convertNullableTaxDeductionV2ToTaxDeductionResponseVoV3(tax.getDeduction()));

		return taxResponseVoV3;
	}

	private TaxBaseChangesResponseVoV3 convertNullableTaxBaseChangesV2ToTaxBaseChangesResponseVoV3(final TaxBaseChangesV2 baseChangesV2) {

		if (isNull(baseChangesV2)) {
			return null;
		}

		final TaxBaseChangesResponseVoV3 taxBaseChangesResponseVoV3 = new TaxBaseChangesResponseVoV3();
		taxBaseChangesResponseVoV3.setIncrease(baseChangesV2.getIncrease());
		taxBaseChangesResponseVoV3.setReduction(baseChangesV2.getReduction());

		if (nonNull(baseChangesV2.getCompareWith())) {
			final TaxBaseChangesCompareWithResponseVoV3 taxBaseChangesCompareWithResponseVoV3 = new TaxBaseChangesCompareWithResponseVoV3();
			taxBaseChangesCompareWithResponseVoV3.setBase(baseChangesV2.getCompareWith().getBase());
			taxBaseChangesCompareWithResponseVoV3.setType(baseChangesV2.getCompareWith().getType().name());
			taxBaseChangesResponseVoV3.setCompareWith(taxBaseChangesCompareWithResponseVoV3);
		}

		return taxBaseChangesResponseVoV3;
	}

	private TaxDeductionResponseVoV3 convertNullableTaxDeductionV2ToTaxDeductionResponseVoV3(final TaxDeductionV2 deductionV2) {

		if (isNull(deductionV2)) {
			return null;
		}

		final TaxDeductionBaseChangesResponseVoV3 taxDeductionBaseChangesResponseVoV3 = ofNullable(deductionV2.getBaseChanges()).map(
						taxDeductionBaseChangesV2 -> new TaxDeductionBaseChangesResponseVoV3(taxDeductionBaseChangesV2.getReduction()))
				.orElse(null);

		final TaxDeductionResponseVoV3 taxDeductionResponseVoV3 = new TaxDeductionResponseVoV3();
		taxDeductionResponseVoV3.setType(deductionV2.getType());
		taxDeductionResponseVoV3.setValue(deductionV2.getValue());
		taxDeductionResponseVoV3.setBaseChanges(taxDeductionBaseChangesResponseVoV3);

		return taxDeductionResponseVoV3;
	}

	private TaxConditionResponseVoV3 convertNullableTaxConditionV2ToTaxConditionResponseVoV3(final TaxConditionV2 conditions) {

		if (isNull(conditions)) {
			return null;
		}

		final TaxConditionResponseVoV3 taxConditionResponseVoV3 = new TaxConditionResponseVoV3();
		taxConditionResponseVoV3.setOrderSubTotal(
				convertNullableTaxOrderSubTotalV2ToTaxOrderSubTotalResponseVoV3(conditions.getOrderSubTotal()));
		taxConditionResponseVoV3.setTaxAmount(convertNullableTaxAmountV2ToTaxAmountResponseVoV3(conditions.getTaxAmount()));
		return taxConditionResponseVoV3;
	}

	private TaxOrderSubTotalResponseVoV3 convertNullableTaxOrderSubTotalV2ToTaxOrderSubTotalResponseVoV3(
			final TaxOrderSubTotalV2 orderSubTotal) {

		if (isNull(orderSubTotal)) {
			return null;
		}
		final TaxOrderSubTotalResponseVoV3 taxOrderSubTotalResponseVoV3 = new TaxOrderSubTotalResponseVoV3();
		taxOrderSubTotalResponseVoV3.setMinimumValue(orderSubTotal.getMinimumValue());
		return taxOrderSubTotalResponseVoV3;
	}

	private TaxAmountResponseVoV3 convertNullableTaxAmountV2ToTaxAmountResponseVoV3(TaxAmountV2 taxAmount) {

		if (isNull(taxAmount)) {
			return null;
		}
		final TaxAmountResponseVoV3 taxAmountResponseVoV3 = new TaxAmountResponseVoV3();
		taxAmountResponseVoV3.setTaxId(taxAmount.getTaxId());
		taxAmountResponseVoV3.setThreshold(taxAmount.getThreshold());
		taxAmountResponseVoV3.setScope(taxAmount.getScope());
		taxAmountResponseVoV3.setOperator(taxAmount.getOperator());
		return taxAmountResponseVoV3;
	}

	private List<ChargeResponseVoV3> convertNullableChargeV2ToChargeResponseVoV3(final Map<String, ChargeV2> charges) {

		final List<ChargeResponseVoV3> chargeResponseVoV3List = new ArrayList<>();

		for (final var chargeEntry : charges.entrySet()) {
			chargeResponseVoV3List.add(convertNotNullableChargeV2ToChargeResponseVoV3(chargeEntry.getValue()));
		}

		return chargeResponseVoV3List;
	}

	private ChargeResponseVoV3 convertNotNullableChargeV2ToChargeResponseVoV3(final ChargeV2 charge) {

		final ChargeResponseVoV3 chargeResponseVoV3 = new ChargeResponseVoV3();
		chargeResponseVoV3.setChargeId(charge.getChargeId());
		chargeResponseVoV3.setType(charge.getType());
		chargeResponseVoV3.setValue(charge.getValue());
		chargeResponseVoV3.setBase(charge.getBase());
		return chargeResponseVoV3;
	}
}
