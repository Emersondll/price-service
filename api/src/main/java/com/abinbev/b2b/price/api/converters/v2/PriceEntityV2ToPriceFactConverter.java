package com.abinbev.b2b.price.api.converters.v2;

import static java.util.Optional.ofNullable;

import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Component;

import com.abinbev.b2b.price.api.domain.ChargeFact;
import com.abinbev.b2b.price.api.domain.PriceFact;
import com.abinbev.b2b.price.api.domain.PromotionalPriceFact;
import com.abinbev.b2b.price.api.domain.TaxConditionFact;
import com.abinbev.b2b.price.api.domain.TaxFact;
import com.abinbev.b2b.price.api.domain.TaxOrderSubTotalFact;
import com.abinbev.b2b.price.domain.model.v2.ChargeV2;
import com.abinbev.b2b.price.domain.model.v2.PriceEntityV2;
import com.abinbev.b2b.price.domain.model.v2.PromotionalPriceV2;
import com.abinbev.b2b.price.domain.model.v2.TaxConditionV2;
import com.abinbev.b2b.price.domain.model.v2.TaxOrderSubTotalV2;
import com.abinbev.b2b.price.domain.model.v2.TaxV2;

@Component
public class PriceEntityV2ToPriceFactConverter {

	public PriceFact convert(final PriceEntityV2 priceEntityV2) {

		final PriceFact priceFact = new PriceFact();
		priceFact.setBasePrice(priceEntityV2.getBasePrice());
		priceFact.setMeasureUnit(priceEntityV2.getMeasureUnit());
		priceFact.setMinimumPrice(priceEntityV2.getMinimumPrice());
		priceFact.setDeposit(priceEntityV2.getDeposit());
		priceFact.setQuantityPerPallet(priceEntityV2.getQuantityPerPallet());
		priceFact.setPromotionalPrice(convertPromotionalPriceV2(priceEntityV2.getPromotionalPrice()));
		priceFact.setTaxes(ofNullable(priceEntityV2.getTaxes()).map(this::convertTaxesV2).orElse(null));
		priceFact.setCharges(ofNullable(priceEntityV2.getCharges()).map(this::convertChargesV2).orElse(null));
		priceFact.setSku(priceEntityV2.getSku());
		priceFact.setValidFrom(priceEntityV2.getValidFrom());
		priceFact.setConsignment(priceEntityV2.getConsignment());
		priceFact.setVendorItemId(priceEntityV2.getId().getVendorItemId());
		priceFact.setTimezone(priceEntityV2.getTimezone());
		priceFact.setCountry(priceEntityV2.getCountry());

		return priceFact;
	}

	private PromotionalPriceFact convertPromotionalPriceV2(final PromotionalPriceV2 promotionalPriceV2) {

		if (promotionalPriceV2 == null) {
			return null;
		}

		final PromotionalPriceFact promotionalPriceFact = new PromotionalPriceFact();
		promotionalPriceFact.setExternalId(promotionalPriceV2.getExternalId());
		promotionalPriceFact.setPrice(promotionalPriceV2.getPrice());
		promotionalPriceFact.setValidUntil(promotionalPriceV2.getValidUntil());

		return promotionalPriceFact;
	}

	private Map<String, TaxFact> convertTaxesV2(final Map<String, TaxV2> taxesMapV2) {

		final Map<String, TaxFact> taxesFactMap = new HashedMap<>();
		taxesMapV2.forEach((k, v) -> taxesFactMap.put(k, convertTaxValueV2(v)));

		return taxesFactMap;
	}

	private TaxFact convertTaxValueV2(final TaxV2 taxV2) {

		if (taxV2 == null) {
			return null;
		}

		final TaxFact taxFact = new TaxFact();
		taxFact.setBase(taxV2.getBase());
		taxFact.setConditions(convertTaxConditionV2(taxV2.getConditions()));
		taxFact.setHidden(taxV2.isHidden());
		taxFact.setTaxBaseInclusionIds(taxV2.getTaxBaseInclusionIds());
		taxFact.setTaxId(taxV2.getTaxId());
		taxFact.setType(taxV2.getType());
		taxFact.setValue(taxV2.getValue());

		return taxFact;
	}

	private TaxConditionFact convertTaxConditionV2(final TaxConditionV2 taxConditionV2) {

		if (taxConditionV2 == null) {
			return null;
		}

		final TaxConditionFact taxConditionFact = new TaxConditionFact();
		taxConditionFact.setOrderSubTotal(convertTaxOrderSubTotalV2(taxConditionV2.getOrderSubTotal()));

		return taxConditionFact;
	}

	private TaxOrderSubTotalFact convertTaxOrderSubTotalV2(final TaxOrderSubTotalV2 taxOrderSubTotalV2) {

		if (taxOrderSubTotalV2 == null) {
			return null;
		}

		final TaxOrderSubTotalFact taxOrderSubTotalFact = new TaxOrderSubTotalFact();
		taxOrderSubTotalFact.setMinimumValue(taxOrderSubTotalV2.getMinimumValue());

		return taxOrderSubTotalFact;
	}

	private Map<String, ChargeFact> convertChargesV2(final Map<String, ChargeV2> chargesMapV2) {

		final Map<String, ChargeFact> chargesFactMap = new HashedMap<>();
		chargesMapV2.forEach((k, v) -> chargesFactMap.put(k, convertChargeValueV2(v)));

		return chargesFactMap;
	}

	private ChargeFact convertChargeValueV2(final ChargeV2 chargeV2) {

		if (chargeV2 == null) {
			return null;
		}

		final ChargeFact chargeFact = new ChargeFact();
		chargeFact.setBase(chargeV2.getBase());
		chargeFact.setChargeId(chargeV2.getChargeId());
		chargeFact.setType(chargeV2.getType());
		chargeFact.setValue(chargeV2.getValue());

		return chargeFact;
	}
}
