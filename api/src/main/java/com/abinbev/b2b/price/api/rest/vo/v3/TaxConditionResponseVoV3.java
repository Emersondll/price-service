package com.abinbev.b2b.price.api.rest.vo.v3;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "TaxConditionResponseVoV3")
public class TaxConditionResponseVoV3 {

	@Schema(description = "The order subtotal")
	private TaxOrderSubTotalResponseVoV3 orderSubTotal;

	@Schema(description = "The tax amount condition for the order")
	private TaxAmountResponseVoV3 taxAmount;

	public TaxOrderSubTotalResponseVoV3 getOrderSubTotal() {

		return orderSubTotal;
	}

	public void setOrderSubTotal(final TaxOrderSubTotalResponseVoV3 orderSubTotal) {

		this.orderSubTotal = orderSubTotal;
	}

	public TaxAmountResponseVoV3 getTaxAmount() {

		return taxAmount;
	}

	public void setTaxAmount(TaxAmountResponseVoV3 taxAmount) {

		this.taxAmount = taxAmount;
	}
}
