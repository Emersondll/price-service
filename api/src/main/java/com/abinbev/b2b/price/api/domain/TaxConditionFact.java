package com.abinbev.b2b.price.api.domain;

public class TaxConditionFact { 

	private TaxOrderSubTotalFact orderSubTotal;

	public TaxConditionFact() {

		super();
	}

	public TaxConditionFact(final TaxOrderSubTotalFact orderSubTotal) {

		this.orderSubTotal = orderSubTotal;
	}

	public TaxOrderSubTotalFact getOrderSubTotal() {

		return orderSubTotal;
	}

	public void setOrderSubTotal(final TaxOrderSubTotalFact orderSubTotal) {

		this.orderSubTotal = orderSubTotal;
	}

}
