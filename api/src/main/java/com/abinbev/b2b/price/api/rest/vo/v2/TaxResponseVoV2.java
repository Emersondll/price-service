package com.abinbev.b2b.price.api.rest.vo.v2;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TaxResponseVoV2")
public class TaxResponseVoV2 {

	@JsonProperty
	@Schema(description = "The taxId is an alphanumeric identifier for the current tax", required = true, example = "IVA")
	private String taxId;
	@JsonProperty
	@Schema(description = "The tax type can be percent or cash and define how this tax will be calculated and applied", required = true, example = "$ or %")
	private String type;
	@JsonProperty
	@Schema(description = "The value of the tax is a financial amount with the max number of decimal places as 15. Its application depends on the type", required = true, example = "20.99")
	private BigDecimal value;
	@JsonProperty
	@ArraySchema(arraySchema = @Schema(description = "The tax base inclusion ids is a list of the dependent taxes. If all the taxes inside it are eligible to be applied then the current tax has this condition satisfied", required = true, example = "[\"IVA\"]"), schema = @Schema(description = "tax base inclusion id"))
	private List<String> taxBaseInclusionIds;
	@JsonProperty
	@Schema(description = "Indicates whether the tax is hidden", required = true, example = "false")
	private boolean hidden;
	@JsonProperty
	@Schema(description = "The base value replaces the base price of the sku in the tax calculation. It's a financial value with the max number of decimal places as 15", example = "10.99")
	private BigDecimal base;
	@JsonProperty
	@Schema(description = "The tax conditions")
	private TaxConditionResponseVoV2 conditions;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty
	@Schema(description = "this property show when the tax should be reduced proportionately when a discount is applied")
	private Boolean proportional;

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

	public TaxConditionResponseVoV2 getConditions() {

		return conditions;
	}

	public void setConditions(final TaxConditionResponseVoV2 conditions) {

		this.conditions = conditions;
	}

	public Boolean getProportional() {

		return proportional;
	}

	public void setProportional(final Boolean proportional) {

		this.proportional = proportional;
	}
}
