package com.abinbev.b2b.price.api.rest.vo.v3;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TaxBaseChangesResponseVoV3")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaxBaseChangesResponseVoV3 {

	@Schema(description = "A percentage to decrease the value of the tax base mentioned on \"taxes.baseChanges\". Ex.: if sent 5, it will decrease the base by 5%.", example = "5")
	private BigDecimal reduction;

	@Schema(description = "A percentage to increase the value of the tax base mentioned on \"taxes.baseChanges\". Ex.: if sent 5, it will increase the base by 5%.", example = "5")
	private BigDecimal increase;

	@Schema(description = "In case vendors want to compare tax bases before applying the tax percentage.")
	private TaxBaseChangesCompareWithResponseVoV3 compareWith;

	public BigDecimal getReduction() {

		return reduction;
	}

	public void setReduction(final BigDecimal reduction) {

		this.reduction = reduction;
	}

	public BigDecimal getIncrease() {

		return increase;
	}

	public void setIncrease(final BigDecimal increase) {

		this.increase = increase;
	}

	public TaxBaseChangesCompareWithResponseVoV3 getCompareWith() {

		return compareWith;
	}

	public void setCompareWith(final TaxBaseChangesCompareWithResponseVoV3 compareWith) {

		this.compareWith = compareWith;
	}
}
