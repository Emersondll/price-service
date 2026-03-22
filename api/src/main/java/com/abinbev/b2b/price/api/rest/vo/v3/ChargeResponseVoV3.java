package com.abinbev.b2b.price.api.rest.vo.v3;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChargeResponseVoV3")
public class ChargeResponseVoV3 {

	@Schema(description = "The charge alphanumeric identifier", required = true, example = "LOGISTIC_COST")
	private String chargeId;

	@Schema(description = "The charge type represents the way it's going to be calculated. It can be percent or cash", required = true, example = "$ or %")
	private String type;

	@Schema(description = "The value of the charge. Its application depends on the type and it is a financial value with the max number of decimal places as 15", required = true, example = "20.99")
	private BigDecimal value;

	@Schema(description = "The base replaces the base price of the sku in the charge calculation. It is a financial value with the max number of decimal places as 15", example = "10.99")
	private BigDecimal base;

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

	public BigDecimal getValue() {

		return value;
	}

	public void setValue(final BigDecimal value) {

		this.value = value;
	}

	public BigDecimal getBase() {

		return base;
	}

	public void setBase(final BigDecimal base) {

		this.base = base;
	}

}
