package com.abinbev.b2b.price.api.rest.vo.v2;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PriceResponseVoV2")
public class PriceResponseVoV2 {

	@JsonProperty
	@ArraySchema(arraySchema = @Schema(description = "List of products", required = true), schema = @Schema(description = "PriceVoV2"))
	private List<PriceVoV2> prices;

	@JsonProperty
	@Schema(description = "pagination")
	private PaginationResponseVoV2 pagination;

	public List<PriceVoV2> getPrices() {

		return prices;
	}

	public void setPrices(final List<PriceVoV2> prices) {

		this.prices = prices;
	}

	public PaginationResponseVoV2 getPagination() {

		return pagination;
	}

	public void setPagination(final PaginationResponseVoV2 pagination) {

		this.pagination = pagination;
	}

}
