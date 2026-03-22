package com.abinbev.b2b.price.api.rest.vo.v2;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TaxOrderSubTotalResponseVoV2")
public class TaxOrderSubTotalResponseVoV2 {

	@JsonProperty
	@Schema(description = "The minimum value represents the minimum order subtotal to this tax to be applied. It's a financial value with the max number of decimal places as 15", example = "2000.99")
	private BigDecimal minimumValue;

	public BigDecimal getMinimumValue() {

		return minimumValue;
	}

	public void setMinimumValue(final BigDecimal minimumValue) {

		this.minimumValue = minimumValue;
	}

}
