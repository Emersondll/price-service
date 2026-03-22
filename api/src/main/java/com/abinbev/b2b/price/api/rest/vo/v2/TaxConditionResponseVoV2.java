package com.abinbev.b2b.price.api.rest.vo.v2;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TaxConditionResponseVoV2")
public class TaxConditionResponseVoV2 {

	@JsonProperty
	@Schema(description = "The order subtotal")
	private TaxOrderSubTotalResponseVoV2 orderSubTotal;

	public TaxOrderSubTotalResponseVoV2 getOrderSubTotal() {

		return orderSubTotal;
	}

	public void setOrderSubTotal(final TaxOrderSubTotalResponseVoV2 orderSubTotal) {

		this.orderSubTotal = orderSubTotal;
	}

}
