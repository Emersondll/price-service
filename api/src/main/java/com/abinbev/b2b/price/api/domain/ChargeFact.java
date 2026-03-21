package com.abinbev.b2b.price.api.domain;

import java.math.BigDecimal;

public class ChargeFact { 

	private String chargeId;
	private String type;
	private BigDecimal value;
	private BigDecimal base;

	public ChargeFact() {

	}

	public ChargeFact(final String chargeId, final String type, final BigDecimal value, final BigDecimal base) {

		this.chargeId = chargeId;
		this.type = type;
		this.value = value;
		this.base = base;
	}

	public BigDecimal getBase() {

		return base;
	}

	public void setBase(final BigDecimal base) {

		this.base = base;
	}

	public BigDecimal getValue() {

		return value;
	}

	public void setValue(final BigDecimal value) {

		this.value = value;
	}

	public String getChargeId() {

		return chargeId;
	}

	public void setChargeId(final String chargeId) {

		this.chargeId = chargeId;
	}

	public String getType() {

		return type;
	}

	public void setType(final String type) {

		this.type = type;
	}
}
