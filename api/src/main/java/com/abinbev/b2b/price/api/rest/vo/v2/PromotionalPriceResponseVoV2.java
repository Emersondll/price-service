package com.abinbev.b2b.price.api.rest.vo.v2;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PromotionalPriceResponseVoV2")
public class PromotionalPriceResponseVoV2 {

	@JsonProperty
	@Schema(description = "The value for the promotional price which is represented by a financial amount with the max number of decimal places as 15", example = "10.99")
	private BigDecimal price;
	@JsonProperty
	@Schema(description = "External Id for promotional price which is represented by an alphanumeric identifier", example = "ZTPM")
	private String externalId;
	@JsonProperty
	@Schema(description = "String to inform the expiration promotion date on format 'yyyy-MM-dd'.", example = "2021-12-31")
	private String validUntil;

	public BigDecimal getPrice() {

		return price;
	}

	public void setPrice(final BigDecimal price) {

		this.price = price;
	}

	public String getExternalId() {

		return externalId;
	}

	public void setExternalId(final String externalId) {

		this.externalId = externalId;
	}

	public String getValidUntil() {

		return validUntil;
	}

	public void setValidUntil(final String validUntil) {

		this.validUntil = validUntil;
	}

}
