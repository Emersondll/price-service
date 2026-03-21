package com.abinbev.b2b.price.api.domain;

import java.util.List;

import com.abinbev.b2b.price.api.rest.vo.Pagination;
import com.abinbev.b2b.price.domain.model.PriceEntity;

public class PriceResultList {

	private Pagination pagination;
	private List<PriceEntity> priceEntities;

	public PriceResultList() {

		super();
	}

	public PriceResultList(final List<PriceEntity> priceEntities, final Pagination pagination) {

		this.priceEntities = priceEntities;
		this.pagination = pagination;
	}

	public List<PriceEntity> getPriceEntities() {

		return priceEntities;
	}

	public void setPriceEntities(final List<PriceEntity> priceEntities) {

		this.priceEntities = priceEntities;
	}

	public Pagination getPagination() {

		return pagination;
	}

	public void setPagination(final Pagination pagination) {

		this.pagination = pagination;
	}
}
