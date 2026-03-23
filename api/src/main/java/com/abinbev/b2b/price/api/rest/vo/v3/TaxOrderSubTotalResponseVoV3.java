package com.abinbev.b2b.price.api.rest.vo.v3;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "TaxOrderSubTotalResponseVoV3")
public class TaxOrderSubTotalResponseVoV3 {

	@Schema(description = "The minimum value represents the minimum order subtotal to this tax to be applied. It's a financial value with the max number of decimal places as 15", example = "2000.99")
	private BigDecimal minimumValue;

	public BigDecimal getMinimumValue() {

		return minimumValue;
	}

	public void setMinimumValue(final BigDecimal minimumValue) {

		this.minimumValue = minimumValue;
	}

}
