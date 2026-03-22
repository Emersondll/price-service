package com.abinbev.b2b.price.api.domain.v3;

import com.abinbev.b2b.price.domain.model.v2.PriceEntityV2;

public class PriceNormalizedInfo {

	private String itemId;
	private String contractId;
	private String deliveryCenterId;
	private String priceListId;
	private String accountId;
	private String vendorId;
	private String vendorItemId;
	private String vendorDeliveryCenterId;
	private PriceEntityV2 selectedPrice;

	public PriceNormalizedInfo() {

	}

	public PriceNormalizedInfo(PriceNormalizedInfo priceNormalizedInfo, PriceEntityV2 priceEntityV2) {
		itemId = priceNormalizedInfo.getItemId();
		contractId = priceNormalizedInfo.getContractId();
		deliveryCenterId = priceNormalizedInfo.getDeliveryCenterId();
		priceListId = priceNormalizedInfo.getPriceListId();
		accountId = priceNormalizedInfo.getAccountId();
		vendorId = priceNormalizedInfo.getVendorId();
		vendorItemId = priceNormalizedInfo.getVendorItemId();
		vendorDeliveryCenterId = priceNormalizedInfo.getVendorDeliveryCenterId();
		selectedPrice = priceEntityV2;
	}

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

	public String getAccountId() {

		return accountId;
	}

	public void setAccountId(final String accountId) {

		this.accountId = accountId;
	}

	public String getVendorId() {

		return vendorId;
	}

	public void setVendorId(final String vendorId) {

		this.vendorId = vendorId;
	}

	public String getVendorItemId() {

		return vendorItemId;
	}

	public void setVendorItemId(final String vendorItemId) {

		this.vendorItemId = vendorItemId;
	}

	public String getVendorDeliveryCenterId() {

		return vendorDeliveryCenterId;
	}

	public void setVendorDeliveryCenterId(final String vendorDeliveryCenterId) {

		this.vendorDeliveryCenterId = vendorDeliveryCenterId;
	}

	public PriceEntityV2 getSelectedPrice() {

		return selectedPrice;
	}

	public void setSelectedPrice(final PriceEntityV2 selectedPrice) {

		this.selectedPrice = selectedPrice;
	}
}
