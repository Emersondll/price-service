package com.abinbev.b2b.price.api.rest.vo.v3;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TaxBaseChangesCompareWithResponseVoV3")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaxBaseChangesCompareWithResponseVoV3 {

	@Schema(description = "If it is an amount per unit of measurement or per item.", example = "$", required = true)
	private String type;

	@Schema(description = "The value of the base to be compared against the original tax base.", example = "5.99", required = true)
	private BigDecimal base;

	public String getType() {

		return type;
	}

	public void setType(final String type) {

		this.type = type;
	}

	public BigDecimal getBase() {

		return base;
	}

	public void setBase(final BigDecimal base) {

		this.base = base;
	}

}
