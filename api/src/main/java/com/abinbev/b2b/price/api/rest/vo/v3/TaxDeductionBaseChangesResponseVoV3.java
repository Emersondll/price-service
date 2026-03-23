package com.abinbev.b2b.price.api.rest.vo.v3;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TaxDeductionBaseChangesResponseVoV3")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaxDeductionBaseChangesResponseVoV3 {

	@Schema(description = "The percentage to decrease the deduction base before calculating the actual deduction value when sent \"taxes.deduction.type\":\"%\".", example = "5", required = true)
	private final BigDecimal reduction;

	public TaxDeductionBaseChangesResponseVoV3(final BigDecimal reduction) {

		this.reduction = reduction;
	}

	public BigDecimal getReduction() {

		return reduction;
	}
}
