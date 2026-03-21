package com.abinbev.b2b.price.api.converters;

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
import com.abinbev.b2b.price.domain.model.Charge;
import com.abinbev.b2b.price.domain.model.PriceEntity;
import com.abinbev.b2b.price.domain.model.PromotionalPrice;
import com.abinbev.b2b.price.domain.model.Tax;
import com.abinbev.b2b.price.domain.model.TaxCondition;
import com.abinbev.b2b.price.domain.model.TaxOrderSubTotal;

@Component
public class PriceEntityToPriceFactConverter {

	public PriceFact convert(final PriceEntity priceEntity) {

		final PriceFact priceFact = new PriceFact();
		priceFact.setBasePrice(priceEntity.getBasePrice());
		priceFact.setMeasureUnit(priceEntity.getMeasureUnit());
		priceFact.setMinimumPrice(priceEntity.getMinimumPrice());
		priceFact.setDeposit(priceEntity.getDeposit());
		priceFact.setQuantityPerPallet(priceEntity.getQuantityPerPallet());
		priceFact.setPromotionalPrice(convertPromotionalPrice(priceEntity.getPromotionalPrice()));
		priceFact.setTaxes(ofNullable(priceEntity.getTaxes()).map(this::convertTaxes).orElse(null));
		priceFact.setCharges(ofNullable(priceEntity.getCharges()).map(this::convertCharges).orElse(null));
		priceFact.setSku(priceEntity.getId().getSku());
		priceFact.setValidFrom(priceEntity.getValidFrom());
		priceFact.setConsignment(priceEntity.getConsignment());
		priceFact.setTimezone(priceEntity.getTimezone());
		priceFact.setCountry(priceEntity.getCountry());

		return priceFact;
	}

	private PromotionalPriceFact convertPromotionalPrice(PromotionalPrice promotionalPrice) {

		if (promotionalPrice == null) {
			return null;
		}

		final PromotionalPriceFact promotionalPriceFact = new PromotionalPriceFact();
		promotionalPriceFact.setExternalId(promotionalPrice.getExternalId());
		promotionalPriceFact.setPrice(promotionalPrice.getPrice());
		promotionalPriceFact.setValidUntil(promotionalPrice.getValidUntil());

		return promotionalPriceFact;
	}

	private Map<String, TaxFact> convertTaxes(final Map<String, Tax> taxesMap) {

		final Map<String, TaxFact> taxesFactMap = new HashedMap<>();
		taxesMap.forEach((k, v) -> taxesFactMap.put(k, convertTaxValue(v)));

		return taxesFactMap;
	}

	private TaxFact convertTaxValue(final Tax tax) {

		if (tax == null) {
			return null;
		}

		final TaxFact taxFact = new TaxFact();
		taxFact.setBase(tax.getBase());
		taxFact.setConditions(converTaxCondition(tax.getConditions()));
		taxFact.setHidden(tax.isHidden());
		taxFact.setTaxBaseInclusionIds(tax.getTaxBaseInclusionIds());
		taxFact.setTaxId(tax.getTaxId());
		taxFact.setType(tax.getType());
		taxFact.setValue(tax.getValue());

		return taxFact;
	}

	private TaxConditionFact converTaxCondition(final TaxCondition taxCondition) {

		if (taxCondition == null) {
			return null;
		}

		final TaxConditionFact taxConditionFact = new TaxConditionFact();
		taxConditionFact.setOrderSubTotal(convertTaxOrderSubTotal(taxCondition.getOrderSubTotal()));

		return taxConditionFact;
	}

	private TaxOrderSubTotalFact convertTaxOrderSubTotal(final TaxOrderSubTotal taxOrderSubTotal) {

		if (taxOrderSubTotal == null) {
			return null;
		}

		final TaxOrderSubTotalFact taxOrderSubTotalFact = new TaxOrderSubTotalFact();
		taxOrderSubTotalFact.setMinimumValue(taxOrderSubTotal.getMinimumValue());

		return taxOrderSubTotalFact;
	}

	private Map<String, ChargeFact> convertCharges(final Map<String, Charge> chargesMap) {

		final Map<String, ChargeFact> chargesFactMap = new HashedMap<>();
		chargesMap.forEach((k, v) -> chargesFactMap.put(k, convertChargeValue(v)));

		return chargesFactMap;
	}

	private ChargeFact convertChargeValue(final Charge charge) {

		if (charge == null) {
			return null;
		}

		final ChargeFact chargeFact = new ChargeFact();
		chargeFact.setBase(charge.getBase());
		chargeFact.setChargeId(charge.getChargeId());
		chargeFact.setType(charge.getType());
		chargeFact.setValue(charge.getValue());

		return chargeFact;
	}
}
