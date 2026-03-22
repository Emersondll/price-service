package com.abinbev.b2b.price.api.rest.vo.v3;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PriceRequestV3")
public class PriceRequestV3 {

	@NotBlank
	@Schema(description = "The unique identifier of the Item.", required = true)
	private String itemId;

	@Schema(description = "The identifier of the Vendor and Account. Is required if priceListId is sent")
	private String contractId;

	@Schema(description = "The identifier of the Vendor´s Delivery Center")
	private String deliveryCenterId;

	@Schema(description = "PRICE LIST ID for filtering by price list")
	private String priceListId;

	public String getItemId() {

		return itemId;
	}

	public void setItemId(final String itemId) {

		this.itemId = itemId;
	}

	public String getContractId() {

		return contractId;
	}

	public void setContractId(final String contractId) {

		this.contractId = contractId;
	}

	public String getDeliveryCenterId() {

		return deliveryCenterId;
	}

	public void setDeliveryCenterId(final String deliveryCenterId) {

		this.deliveryCenterId = deliveryCenterId;
	}

	public String getPriceListId() {

		return priceListId;
	}

	public void setPriceListId(final String priceListId) {

		this.priceListId = priceListId;
	}
}
