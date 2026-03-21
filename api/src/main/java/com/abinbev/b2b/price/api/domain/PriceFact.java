package com.abinbev.b2b.price.api.domain;

import java.math.BigDecimal;
import java.util.Map;

public class PriceFact {

	private BigDecimal basePrice;
	private String measureUnit;
	private BigDecimal minimumPrice;
	private BigDecimal deposit;
	private Integer quantityPerPallet;
	private PromotionalPriceFact promotionalPrice;
	private Map<String, TaxFact> taxes;
	private Map<String, ChargeFact> charges;
	private String sku;
	private String validFrom;
	private BigDecimal consignment;
	private String vendorItemId;
	private String timezone;
	private String country;

	public PriceFact() {

		super();
	}

	public BigDecimal getBasePrice() {

		return basePrice;
	}

	public void setBasePrice(final BigDecimal basePrice) {

		this.basePrice = basePrice;
	}

	public String getMeasureUnit() {

		return measureUnit;
	}

	public void setMeasureUnit(final String measureUnit) {

		this.measureUnit = measureUnit;
	}

	public BigDecimal getMinimumPrice() {

		return minimumPrice;
	}

	public void setMinimumPrice(final BigDecimal minimumPrice) {

		this.minimumPrice = minimumPrice;
	}

	public BigDecimal getDeposit() {

		return deposit;
	}

	public void setDeposit(final BigDecimal deposit) {

		this.deposit = deposit;
	}

	public Integer getQuantityPerPallet() {

		return quantityPerPallet;
	}

	public void setQuantityPerPallet(final Integer quantityPerPallet) {

		this.quantityPerPallet = quantityPerPallet;
	}

	public PromotionalPriceFact getPromotionalPrice() {

		return promotionalPrice;
	}

	public void setPromotionalPrice(final PromotionalPriceFact promotionalPrice) {

		this.promotionalPrice = promotionalPrice;
	}

	public Map<String, TaxFact> getTaxes() {

		return taxes;
	}

	public void setTaxes(final Map<String, TaxFact> taxes) {

		this.taxes = taxes;
	}

	public Map<String, ChargeFact> getCharges() {

		return charges;
	}

	public void setCharges(final Map<String, ChargeFact> charges) {

		this.charges = charges;
	}

	public String getSku() {

		return sku;
	}

	public void setSku(final String sku) {

		this.sku = sku;
	}

	public String getValidFrom() {

		return validFrom;
	}

	public void setValidFrom(final String validFrom) {

		this.validFrom = validFrom;
	}

	public BigDecimal getConsignment() {

		return consignment;
	}

	public void setConsignment(final BigDecimal consignment) {

		this.consignment = consignment;
	}

	public String getVendorItemId() {

		return vendorItemId;
	}

	public void setVendorItemId(final String vendorItemId) {

		this.vendorItemId = vendorItemId;
	}


	public String getTimezone() {

		return timezone;
	}

	public void setTimezone(final String timezone) {

		this.timezone = timezone;
	}

	public String getCountry() {

		return country;
	}

	public void setCountry(final String country) {

		this.country = country;
	}
}
