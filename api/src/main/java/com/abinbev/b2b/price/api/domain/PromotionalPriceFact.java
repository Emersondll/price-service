package com.abinbev.b2b.price.api.domain;

import java.math.BigDecimal;

public class PromotionalPriceFact { 

	private BigDecimal price;
	private String externalId;
	private String validUntil;

	public PromotionalPriceFact() {

		super();
	}

	public PromotionalPriceFact(final BigDecimal price, final String externalId, final String validUntil) {

		this.price = price;
		this.externalId = externalId;
		this.validUntil = validUntil;
	}

	public BigDecimal getPrice() {

		return price;
	}

	public void setPrice(final BigDecimal price) {

		this.price = price;
	}

	public String getExternalId() {

		return externalId;
	}

	public void setExternalId(final String externalId) {

		this.externalId = externalId;
	}

	public String getValidUntil() {

		return validUntil;
	}

	public void setValidUntil(final String validUntil) {

		this.validUntil = validUntil;
	}
}
