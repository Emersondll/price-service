package com.abinbev.b2b.price.api.rest.vo.v3;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TaxDeductionResponseVoV3")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaxDeductionResponseVoV3 {

	@Schema(description = "Two possible types, Amount($) or Percentage(%). When sent in Amount, the value is applied directly, with no secondary calculations. While percentage will be calculated based on the item subtotal and charges. ", required = true, example = "$")
	private String type;

	@Schema(description = "The value deducted when sent \"taxes.deduction.type\":\"$\"; when \"taxes.deduction.type\":\"%\", the percentage to be applied over the base explained on the \"taxes.deduction\".", example = "5", required = true)
	private BigDecimal value;

	@Schema(description = "When the base for the deduction is meant to change before calculating the actual deduction value, when sent \"taxes.deduction.type\":\"%\".")
	private TaxDeductionBaseChangesResponseVoV3 baseChanges;

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

	public TaxDeductionBaseChangesResponseVoV3 getBaseChanges() {

		return baseChanges;
	}

	public void setBaseChanges(final TaxDeductionBaseChangesResponseVoV3 baseChanges) {

		this.baseChanges = baseChanges;
	}
}
