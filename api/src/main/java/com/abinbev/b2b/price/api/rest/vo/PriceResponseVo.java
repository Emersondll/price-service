package com.abinbev.b2b.price.api.rest.vo;

import java.util.List;

import com.abinbev.b2b.price.domain.model.PriceEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PriceResponseVo")
public class PriceResponseVo {

	@JsonProperty
	@JsonIgnoreProperties({ "id", "validTo" })
	@ArraySchema(arraySchema = @Schema(description = "List of products", required = true), schema = @Schema(description = "product"))
	private List<PriceEntity> prices;

	@JsonProperty
	@Schema(description = "pagination")
	private Pagination pagination;

	public PriceResponseVo(final List<PriceEntity> prices, final Pagination pagination) {

		this.prices = prices;
		this.pagination = pagination;
	}

	public List<PriceEntity> getPrices() {

		return prices;
	}

	public void setPrices(final List<PriceEntity> prices) {

		this.prices = prices;
	}

	public Pagination getPagination() {

		return pagination;
	}

	public void setPagination(final Pagination pagination) {

		this.pagination = pagination;
	}
}
