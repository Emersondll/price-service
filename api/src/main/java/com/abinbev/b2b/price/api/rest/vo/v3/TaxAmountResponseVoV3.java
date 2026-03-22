package com.abinbev.b2b.price.api.rest.vo.v3;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "TaxAmountResponseVoV3")
public class TaxAmountResponseVoV3 {

	@Schema(description = "The id of the tax that is in the condition", example = "IVA")
	private String taxId;

	@Schema(description = "The value that will be used to compare the sum of the given tax id", example = "1000.00")
	private BigDecimal threshold;

	@Schema(description = "Level which the Amount of tax will be compared to the threshold", example = "ORDER")
	private String scope;

	@Schema(description = "This is the operator indicating the type of comparison that is necessary to be done between scope and threshold", example = "GE")
	private String operator;

	public String getTaxId() {

		return taxId;
	}

	public void setTaxId(String taxId) {

		this.taxId = taxId;
	}

	public BigDecimal getThreshold() {

		return threshold;
	}

	public void setThreshold(BigDecimal threshold) {

		this.threshold = threshold;
	}

	public String getScope() {

		return scope;
	}

	public void setScope(String scope) {

		this.scope = scope;
	}

	public String getOperator() {

		return operator;
	}

	public void setOperator(String operator) {

		this.operator = operator;
	}
}
