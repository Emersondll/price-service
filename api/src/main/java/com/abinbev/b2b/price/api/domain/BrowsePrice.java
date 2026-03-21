package com.abinbev.b2b.price.api.domain;

import java.math.BigDecimal;

public class BrowsePrice { 

	private String sku;
	private BigDecimal price;
	private BigDecimal originalPrice;
	private String validUntil;
	private String vendorItemId;

	public String getSku() {

		return sku;
	}

	public void setSku(final String sku) {

		this.sku = sku;
	}

	public BigDecimal getPrice() {

		return price;
	}

	public void setPrice(final BigDecimal price) {

		this.price = price;
	}

	public BigDecimal getOriginalPrice() {

		return originalPrice;
	}

	public void setOriginalPrice(final BigDecimal originalPrice) {

		this.originalPrice = originalPrice;
	}

	public String getValidUntil() {

		return validUntil;
	}

	public void setValidUntil(final String validUntil) {

		this.validUntil = validUntil;
	}

	public String getVendorItemId() {
		return vendorItemId;
	}

	public void setVendorItemId(final String vendorItemId) {
		this.vendorItemId = vendorItemId;
	}
}
