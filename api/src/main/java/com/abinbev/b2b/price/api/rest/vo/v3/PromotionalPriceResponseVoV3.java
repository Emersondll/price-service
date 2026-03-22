package com.abinbev.b2b.price.api.rest.vo.v3;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "PromotionalPriceResponseVoV3")
public class PromotionalPriceResponseVoV3 {

	@Schema(description = "The value for the promotional price which is represented by a financial amount with the max number of decimal places as 15", example = "10.99")
	private BigDecimal price;

	@Schema(description = "External Id for promotional price which is represented by an alphanumeric identifier", example = "ZTPM")
	private String externalId;

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
