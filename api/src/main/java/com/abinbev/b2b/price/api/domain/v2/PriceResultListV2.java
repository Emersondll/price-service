package com.abinbev.b2b.price.api.domain.v2;

import java.util.List;

import com.abinbev.b2b.price.api.rest.vo.v2.PaginationResponseVoV2;
import com.abinbev.b2b.price.domain.model.v2.PriceEntityV2;

public class PriceResultListV2 {

	private PaginationResponseVoV2 pagination;
	private List<PriceEntityV2> priceEntities;

	public PriceResultListV2(final List<PriceEntityV2> priceEntities, final PaginationResponseVoV2 pagination) {

		this.priceEntities = priceEntities;
		this.pagination = pagination;

	}

	public PriceResultListV2() {

		super();
	}

	public PaginationResponseVoV2 getPagination() {

		return pagination;
	}

	public void setPagination(final PaginationResponseVoV2 pagination) {

		this.pagination = pagination;
	}

	public List<PriceEntityV2> getPriceEntities() {

		return priceEntities;
	}

	public void setPriceEntities(final List<PriceEntityV2> priceEntities) {

		this.priceEntities = priceEntities;
	}

}
