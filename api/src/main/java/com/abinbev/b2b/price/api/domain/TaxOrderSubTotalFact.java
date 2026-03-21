package com.abinbev.b2b.price.api.domain;

import java.math.BigDecimal;

public class TaxOrderSubTotalFact { 

	private BigDecimal minimumValue;

	public TaxOrderSubTotalFact() {

		super();
	}

	public TaxOrderSubTotalFact(final BigDecimal minimumValue) {

		this.minimumValue = minimumValue;
	}

	public BigDecimal getMinimumValue() {

		return minimumValue;
	}

	public void setMinimumValue(final BigDecimal minimumValue) {

		this.minimumValue = minimumValue;
	}

}
