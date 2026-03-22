package com.abinbev.b2b.price.api.rest.vo;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CustomerProductPriceResponseVo")
public class CustomerProductPriceResponseVo {

	@Schema(description = "The product's alphanumeric identifier SKU for V1", example = "000000000000000099")
	private String sku;
	@Schema(description = "The product's alphanumeric item identifier for V2", example = "000000000000000099")
	private String vendorItemId;
	@Schema(description = "The price for this product represented by a financial amount.", example = "25.28")
	private BigDecimal price;
	@Schema(description = "The full price for this product. It is a financial amount and will be null when there is no promotional price.", example = "30.28")
	private final BigDecimal originalPrice;
	@Schema(description = "Indicates the date until the promotional price is valid", example = "2021-12-15")
	private final String validUntil;

	public CustomerProductPriceResponseVo(final String sku, final BigDecimal price, final BigDecimal originalPrice,
			final String validUntil, final String vendorItemId) {

		this.sku = sku;
		this.price = price;
		this.originalPrice = originalPrice;
		this.validUntil = validUntil;
		this.vendorItemId = vendorItemId;
	}

	public String getSku() {

		return sku;
	}

	public void setSku(String sku) {

		this.sku = sku;
	}

	public String getVendorItemId() {

		return vendorItemId;
	}

	public void setVendorItemId(String vendorItemId) {

		this.vendorItemId = vendorItemId;
	}

	public BigDecimal getPrice() {

		return price;
	}

	public void setPrice(BigDecimal price) {

		this.price = price;
	}

	public BigDecimal getOriginalPrice() {

		return originalPrice;
	}

	public String getValidUntil() {

		return validUntil;
	}
}
