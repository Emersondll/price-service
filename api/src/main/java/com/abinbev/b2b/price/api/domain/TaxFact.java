package com.abinbev.b2b.price.api.domain;

import java.math.BigDecimal;
import java.util.List;

public class TaxFact { 

	private String taxId;
	private String type;
	private BigDecimal value;
	private List<String> taxBaseInclusionIds;
	private boolean hidden;
	private BigDecimal base;
	private TaxConditionFact conditions;

	public TaxFact() {

		super();
	}

	public TaxFact(final String taxId, final String type, final BigDecimal value, final List<String> taxBaseInclusionIds, final boolean hidden,
			final BigDecimal base, final TaxConditionFact conditions) {

		this.taxId = taxId;
		this.type = type;
		this.value = value;
		this.taxBaseInclusionIds = taxBaseInclusionIds;
		this.hidden = hidden;
		this.base = base;
		this.conditions = conditions;
	}

	public String getTaxId() {

		return taxId;
	}

	public void setTaxId(final String taxId) {

		this.taxId = taxId;
	}

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

	public List<String> getTaxBaseInclusionIds() {

		return taxBaseInclusionIds;
	}

	public void setTaxBaseInclusionIds(final List<String> taxBaseInclusionIds) {

		this.taxBaseInclusionIds = taxBaseInclusionIds;
	}

	public boolean isHidden() {

		return hidden;
	}

	public void setHidden(final boolean hidden) {

		this.hidden = hidden;
	}

	public BigDecimal getBase() {

		return base;
	}

	public void setBase(final BigDecimal base) {

		this.base = base;
	}

	public TaxConditionFact getConditions() {

		return conditions;
	}

	public void setConditions(final TaxConditionFact conditions) {

		this.conditions = conditions;
	}
}
