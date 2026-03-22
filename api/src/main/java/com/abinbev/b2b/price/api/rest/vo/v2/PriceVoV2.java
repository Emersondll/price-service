package com.abinbev.b2b.price.api.rest.vo.v2;

import java.math.BigDecimal;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PriceVoV2")
public class PriceVoV2 {

	@JsonProperty
	@Schema(description = "The vendor account identifier.", example = "123456")
	private String vendorAccountId;

	@JsonProperty
	@Schema(description = "The vendor identifier.", example = "123456")
	private String vendorId;

	@JsonProperty
	@Schema(description = "The vendor item identifier. It can have special characters", required = true, example = "SKU0001-CASE")
	private String vendorItemId;

	@JsonProperty
	@Schema(description = "The sku alphanumeric identifier. It can have special characters but not dot", example = "SKU0001")
	private String sku;

	@JsonProperty
	@Schema(description = "The base price for the current sku represented by a financial amount. The max number of decimal places is 15", required = true, example = "20.99")
	private BigDecimal basePrice;

	@JsonProperty
	@Schema(description = "The minimum price for the current sku represented by a financial amount. The max number of decimal places is 15", example = "5.99")
	private BigDecimal minimumPrice;

	@JsonProperty
	@Schema(description = "The measure unit for the current sku", example = "CS")
	private String measureUnit;

	@JsonProperty
	@Schema(description = "The type of the price measure unit", example = "PER_UNIT or PER_UOM")
	private String type;

	@JsonProperty
	@Schema(description = "The deposit represents the can/cask price. It's represented by a financial amount and the max number of decimal places is 15", example = "3.99")
	private BigDecimal deposit;

	@JsonProperty
	@Schema(description = "The deposit fee for kegs.", example = "0.75")
	private BigDecimal consignment;

	@JsonProperty
	@Schema(description = "The quantity of the current sku that fit in a pallet. Used for the calculations of pallet deals", example = "88")
	private Integer quantityPerPallet;

	@JsonProperty
	@Schema(description = "A geographic region within which the same standard time is used.", example = "America/Los_Angeles")
	private String timezone;

	@JsonProperty
	@Schema(description = "The valid from date on format 'yyyy-MM-dd'. Indicates when this price starts to be used in the calculation process.", example = "2020-12-31")
	private String validFrom;

	@JsonProperty
	@Schema(description = "List of taxes")
	private Map<String, TaxResponseVoV2> taxes;

	@JsonProperty
	@Schema(description = "The promotional price")
	private PromotionalPriceResponseVoV2 promotionalPrice;

	@JsonProperty
	@Schema(description = "List of charges")
	private Map<String, ChargeResponseVoV2> charges;

	public String getVendorAccountId() {

		return vendorAccountId;
	}

	public void setVendorAccountId(final String vendorAccountId) {

		this.vendorAccountId = vendorAccountId;
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

	public String getSku() {

		return sku;
	}

	public void setSku(final String sku) {

		this.sku = sku;
	}

	public BigDecimal getBasePrice() {

		return basePrice;
	}

	public void setBasePrice(final BigDecimal basePrice) {

		this.basePrice = basePrice;
	}

	public BigDecimal getMinimumPrice() {

		return minimumPrice;
	}

	public void setMinimumPrice(final BigDecimal minimumPrice) {

		this.minimumPrice = minimumPrice;
	}

	public String getMeasureUnit() {

		return measureUnit;
	}

	public void setMeasureUnit(final String measureUnit) {

		this.measureUnit = measureUnit;
	}

	public String getType() {

		return type;
	}

	public void setType(final String type) {

		this.type = type;
	}

	public BigDecimal getDeposit() {

		return deposit;
	}

	public void setDeposit(final BigDecimal deposit) {

		this.deposit = deposit;
	}

	public BigDecimal getConsignment() {

		return consignment;
	}

	public void setConsignment(final BigDecimal consignment) {

		this.consignment = consignment;
	}

	public Integer getQuantityPerPallet() {

		return quantityPerPallet;
	}

	public void setQuantityPerPallet(final Integer quantityPerPallet) {

		this.quantityPerPallet = quantityPerPallet;
	}

	public String getTimezone() {

		return timezone;
	}

	public void setTimezone(final String timezone) {

		this.timezone = timezone;
	}

	public String getValidFrom() {

		return validFrom;
	}

	public void setValidFrom(final String validFrom) {

		this.validFrom = validFrom;
	}

	public Map<String, TaxResponseVoV2> getTaxes() {

		return taxes;
	}

	public void setTaxes(final Map<String, TaxResponseVoV2> taxes) {

		this.taxes = taxes;
	}

	public PromotionalPriceResponseVoV2 getPromotionalPrice() {

		return promotionalPrice;
	}

	public void setPromotionalPrice(final PromotionalPriceResponseVoV2 promotionalPrice) {

		this.promotionalPrice = promotionalPrice;
	}

	public Map<String, ChargeResponseVoV2> getCharges() {

		return charges;
	}

	public void setCharges(final Map<String, ChargeResponseVoV2> charges) {

		this.charges = charges;
	}
}
