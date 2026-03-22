package com.abinbev.b2b.price.api.rest.vo.v3;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "PriceResponseVoV3")
public class PriceResponseVoV3 {

	@Schema(description = "The vendor item identifier. It can have special characters", required = true, example = "SKU0001-CASE")
	private String vendorItemId;

	@Schema(description = "The sku alphanumeric identifier. It can have special characters but not dot", example = "SKU0001")
	private String sku;

	@Schema(description = "The unique identifier of the Item", example = "VkVORE9SX0lEO0lURU1fSUQ=")
	private String itemId;

	@Schema(description = "The identifier of the Vendor and Account", example = "VkVORE9SX0lEO0NPTlRSQUNUX0lE")
	private String contractId;

	@Schema(description = "The identifier of the Vendor´s Delivery Center", example = "VkVORE9SX0lEO0RFTElWRVJZX0NFTlRFUl9JRA==")
	private String deliveryCenterId;

	@Schema(description = "The base price for the current sku represented by a financial amount. The max number of decimal places is 15", required = true, example = "20.99")
	private BigDecimal basePrice;

	@Schema(description = "The minimum price for the current sku represented by a financial amount. The max number of decimal places is 15", example = "5.99")
	private BigDecimal minimumPrice;

	@Schema(description = "The measure unit for the current sku", example = "CS")
	private String measureUnit;

	@Schema(description = "The type of the price measure unit", example = "PER_UNIT or PER_UOM")
	private String type;

	@Schema(description = "The deposit represents the can/cask price. It's represented by a financial amount and the max number of decimal places is 15", example = "3.99")
	private BigDecimal deposit;

	@Schema(description = "The deposit fee for kegs.", example = "0.75")
	private BigDecimal consignment;

	@Schema(description = "The quantity of the current sku that fit in a pallet. Used for the calculations of pallet deals", example = "88")
	private Integer quantityPerPallet;

	@Schema(description = "The suggested sales price for the POC applies to the final consumer, also known as PTC. The zone or partner define/ingest it and the Pricing ms´s doesn´t make any calculation. It´s also used to calculate the profitMargin parameter", example = "39.99")
	private BigDecimal suggestedRetailPrice;

	@Schema(description = "A geographic region within which the same standard time is used.", example = "America/Los_Angeles")
	private String timezone;

	@Schema(description = "The valid from date on format 'yyyy-MM-dd'. Indicates when this price starts to be used in the calculation process.", example = "2020-12-31")
	private String validFrom;

	@ArraySchema(arraySchema = @Schema(description = "List of taxes"), schema = @Schema(description = "TaxResponseVoV3"))
	private List<TaxResponseVoV3> taxes;

	@Schema(description = "The promotional price")
	private PromotionalPriceResponseVoV3 promotionalPrice;

	@ArraySchema(arraySchema = @Schema(description = "List of charges"), schema = @Schema(description = "ChargeResponseVoV3"))
	private List<ChargeResponseVoV3> charges;

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

	public List<TaxResponseVoV3> getTaxes() {

		return taxes;
	}

	public void setTaxes(final List<TaxResponseVoV3> taxes) {

		this.taxes = taxes;
	}

	public PromotionalPriceResponseVoV3 getPromotionalPrice() {

		return promotionalPrice;
	}

	public void setPromotionalPrice(final PromotionalPriceResponseVoV3 promotionalPrice) {

		this.promotionalPrice = promotionalPrice;
	}

	public List<ChargeResponseVoV3> getCharges() {

		return charges;
	}

	public void setCharges(final List<ChargeResponseVoV3> charges) {

		this.charges = charges;
	}

	public BigDecimal getSuggestedRetailPrice() {
		return suggestedRetailPrice;
	}

	public void setSuggestedRetailPrice(BigDecimal suggestedRetailPrice) {
		this.suggestedRetailPrice = suggestedRetailPrice;
	}
}
