package com.abinbev.b2b.price.api.converters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.apache.groovy.util.Maps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abinbev.b2b.price.api.domain.PriceFact;
import com.abinbev.b2b.price.domain.model.Charge;
import com.abinbev.b2b.price.domain.model.PriceCompoundKey;
import com.abinbev.b2b.price.domain.model.PriceEntity;
import com.abinbev.b2b.price.domain.model.PromotionalPrice;
import com.abinbev.b2b.price.domain.model.Tax;
import com.abinbev.b2b.price.domain.model.TaxCondition;
import com.abinbev.b2b.price.domain.model.TaxOrderSubTotal;

@ExtendWith(MockitoExtension.class)
class PriceEntityToPriceFactConverterTest {

	@InjectMocks
	private PriceEntityToPriceFactConverter priceEntityToPriceFactConverter;

	@Test
	void shouldConvertPriceEntityWhenHasNoOptionalFieldsToPriceFactSuccessfully() {

		final PriceEntity priceEntity = buildDefaultPriceEntityWithoutOptionalFields(null);
		final PriceFact priceFact = priceEntityToPriceFactConverter.convert(priceEntity);

		assertRequiredFieldsOfPriceFact(priceFact);
		assertThat(priceFact.getValidFrom(), is(nullValue()));
		assertThat(priceFact.getDeposit(), is(nullValue()));
		assertThat(priceFact.getConsignment(), is(nullValue()));
		assertThat(priceFact.getQuantityPerPallet(), is(nullValue()));
		assertThat(priceFact.getPromotionalPrice(), is(nullValue()));
		assertThat(priceFact.getTaxes(), is(nullValue()));
		assertThat(priceFact.getCharges(), is(nullValue()));
		assertThat(priceFact.getVendorItemId(), is(nullValue()));
	}

	@Test
	void shouldConvertPriceEntityWhenHasNoTaxAndChargeSubOptionalFieldsToPriceFactSuccessfully() {

		final PromotionalPrice promotionalPrice = buildDefaultPromotionalPriceWithOptionalFields("PP_ID-1");
		final PriceEntity priceEntity = buildDefaultPriceEntityWithOptionalFields(promotionalPrice, Maps.of("TAX_ID-1", null),
				Maps.of("CHARGE_ID-1", null));

		final PriceFact priceFact = priceEntityToPriceFactConverter.convert(priceEntity);
		assertRequiredFieldsOfPriceFact(priceFact);
		assertThat(priceFact.getValidFrom(), is(equalTo("2030-04-01")));
		assertThat(priceFact.getDeposit(), is(equalTo(new BigDecimal("1.80"))));
		assertThat(priceFact.getConsignment(), is(equalTo(new BigDecimal("2.00"))));
		assertThat(priceFact.getQuantityPerPallet(), is(equalTo(4)));

		assertThat(priceFact.getPromotionalPrice(), is(not(nullValue())));
		assertThat(priceFact.getTaxes(), is(not(nullValue())));
		assertThat(priceFact.getTaxes().get("TAX_ID-1"), is(nullValue()));
		assertThat(priceFact.getCharges(), is(not(nullValue())));
		assertThat(priceFact.getCharges().get("CHARGE_ID-1"), is(nullValue()));
		assertThat(priceFact.getVendorItemId(), is(nullValue()));
	}

	@Test
	void shouldConvertPriceEntityWhenHasNoTaxConditionOptionalFieldsToPriceFactSuccessfully() {

		final PromotionalPrice promotionalPrice = buildDefaultPromotionalPriceWithOptionalFields("PP_ID-1");
		final Tax tax = buildDefaultTaxWithoutOptionalFields("TAX_ID-1");
		final Charge charge = buildDefaultChargeWithOptionalFields("CHARGE_ID-1");
		final PriceEntity priceEntity = buildDefaultPriceEntityWithOptionalFields(promotionalPrice, Maps.of("TAX_ID-1", tax),
				Maps.of("CHARGE_ID-1", charge));

		final PriceFact priceFact = priceEntityToPriceFactConverter.convert(priceEntity);
		assertRequiredFieldsOfPriceFact(priceFact);
		assertThat(priceFact.getValidFrom(), is(equalTo("2030-04-01")));
		assertThat(priceFact.getDeposit(), is(equalTo(new BigDecimal("1.80"))));
		assertThat(priceFact.getConsignment(), is(equalTo(new BigDecimal("2.00"))));
		assertThat(priceFact.getQuantityPerPallet(), is(equalTo(4)));

		assertThat(priceFact.getPromotionalPrice(), is(not(nullValue())));
		assertThat(priceFact.getTaxes(), is(not(nullValue())));
		assertThat(priceFact.getTaxes().get("TAX_ID-1"), is(not(nullValue())));
		assertThat(priceFact.getTaxes().get("TAX_ID-1").getConditions(), is(nullValue()));
		assertThat(priceFact.getCharges(), is(not(nullValue())));
		assertThat(priceFact.getVendorItemId(), is(nullValue()));
	}

	@Test
	void shouldConvertPriceEntityWhenHasNoTaxOrderSubTotalOptionalFieldsToPriceFactSuccessfully() {

		final PromotionalPrice promotionalPrice = buildDefaultPromotionalPriceWithOptionalFields("PP_ID-1");
		final Tax tax = buildTaxWithoutTaxOrderSubTotalFields("TAX_ID-1");
		final Charge charge = buildDefaultChargeWithOptionalFields("CHARGE_ID-1");
		final PriceEntity priceEntity = buildDefaultPriceEntityWithOptionalFields(promotionalPrice, Maps.of("TAX_ID-1", tax),
				Maps.of("CHARGE_ID-1", charge));

		final PriceFact priceFact = priceEntityToPriceFactConverter.convert(priceEntity);
		assertRequiredFieldsOfPriceFact(priceFact);
		assertThat(priceFact.getValidFrom(), is(equalTo("2030-04-01")));
		assertThat(priceFact.getDeposit(), is(equalTo(new BigDecimal("1.80"))));
		assertThat(priceFact.getConsignment(), is(equalTo(new BigDecimal("2.00"))));
		assertThat(priceFact.getQuantityPerPallet(), is(equalTo(4)));

		assertThat(priceFact.getPromotionalPrice(), is(not(nullValue())));
		assertThat(priceFact.getTaxes(), is(not(nullValue())));
		assertThat(priceFact.getTaxes().get("TAX_ID-1"), is(not(nullValue())));
		assertThat(priceFact.getTaxes().get("TAX_ID-1").getConditions(), is(not(nullValue())));
		assertThat(priceFact.getTaxes().get("TAX_ID-1").getConditions().getOrderSubTotal(), is(nullValue()));
		assertThat(priceFact.getCharges(), is(not(nullValue())));
		assertThat(priceFact.getVendorItemId(), is(nullValue()));
	}

	@Test
	void shouldConvertPriceEntityWhenOptionalFieldsToPriceFactSuccessfully() {

		final PromotionalPrice promotionalPrice = buildDefaultPromotionalPriceWithOptionalFields("PP_ID-1");
		final Tax tax = buildDefaultTaxWithOptionalFields("TAX_ID-1");
		final Charge charge = buildDefaultChargeWithOptionalFields("CHARGE_ID-1");
		final PriceEntity priceEntity = buildDefaultPriceEntityWithOptionalFields(promotionalPrice, Maps.of("TAX_ID-1", tax),
				Maps.of("CHARGE_ID-1", charge));

		final PriceFact priceFact = priceEntityToPriceFactConverter.convert(priceEntity);
		assertRequiredFieldsOfPriceFact(priceFact);
		assertThat(priceFact.getValidFrom(), is(equalTo("2030-04-01")));
		assertThat(priceFact.getDeposit(), is(equalTo(new BigDecimal("1.80"))));
		assertThat(priceFact.getConsignment(), is(equalTo(new BigDecimal("2.00"))));
		assertThat(priceFact.getQuantityPerPallet(), is(equalTo(4)));

		assertThat(priceFact.getPromotionalPrice(), is(not(nullValue())));
		assertThat(priceFact.getTaxes(), is(not(nullValue())));
		assertThat(priceFact.getCharges(), is(not(nullValue())));
		assertThat(priceFact.getVendorItemId(), is(nullValue()));

	}

	private void assertRequiredFieldsOfPriceFact(final PriceFact priceFact) {

		assertThat(priceFact.getSku(), is(equalTo("SKU-1")));
		assertThat(priceFact.getBasePrice(), is(equalTo(new BigDecimal("10.00"))));
		assertThat(priceFact.getMinimumPrice(), is(equalTo(new BigDecimal("8.40"))));
		assertThat(priceFact.getMeasureUnit(), is(equalTo("CS")));
	}

	private Charge buildDefaultChargeWithOptionalFields(final String chargeId) {

		final Charge charge = buildDefaultChargeWithoutOptionalFields(chargeId);
		charge.setBase(new BigDecimal("2.40"));
		return charge;
	}

	private Charge buildDefaultChargeWithoutOptionalFields(final String chargeId) {

		final Charge charge = new Charge();
		charge.setChargeId(chargeId);
		charge.setType("$");
		charge.setValue(new BigDecimal("2.01"));
		charge.setBase(null);
		return charge;
	}

	private Tax buildDefaultTaxWithOptionalFields(final String taxId) {

		final TaxOrderSubTotal orderSubTotal = new TaxOrderSubTotal();
		orderSubTotal.setMinimumValue(new BigDecimal("1.05"));

		final TaxCondition taxCondition = buildDefaultTaxConditionWithoutOptionalFields();
		taxCondition.setOrderSubTotal(orderSubTotal);

		final Tax tax = buildDefaultTaxWithoutOptionalFields(taxId);
		tax.setTaxBaseInclusionIds(Arrays.asList("TAX_ID-2", "TAX_ID-3"));
		tax.setBase(new BigDecimal("1.70"));
		tax.setConditions(taxCondition);
		return tax;
	}

	private Tax buildTaxWithoutTaxOrderSubTotalFields(final String taxId) {

		final TaxCondition taxCondition = buildDefaultTaxConditionWithoutOptionalFields();
		taxCondition.setOrderSubTotal(null);

		final Tax tax = buildDefaultTaxWithoutOptionalFields(taxId);
		tax.setTaxBaseInclusionIds(Arrays.asList("TAX_ID-2", "TAX_ID-3"));
		tax.setBase(new BigDecimal("1.70"));
		tax.setConditions(taxCondition);
		return tax;
	}

	private TaxCondition buildDefaultTaxConditionWithoutOptionalFields() {

		final TaxCondition taxCondition = new TaxCondition();
		taxCondition.setOrderSubTotal(null);
		return taxCondition;
	}

	private Tax buildDefaultTaxWithoutOptionalFields(final String taxId) {

		final Tax tax = new Tax();
		tax.setTaxId(taxId);
		tax.setType("%");
		tax.setValue(new BigDecimal("3.15"));
		tax.setTaxBaseInclusionIds(null);
		tax.setHidden(false);
		tax.setBase(null);
		tax.setConditions(null);
		return tax;
	}

	private PromotionalPrice buildDefaultPromotionalPriceWithoutOptionalFields(final String externalId) {

		final PromotionalPrice promotionalPrice = new PromotionalPrice();
		promotionalPrice.setPrice(new BigDecimal("9.40"));
		promotionalPrice.setExternalId(externalId);
		promotionalPrice.setValidUntil(null);
		return promotionalPrice;
	}

	private PromotionalPrice buildDefaultPromotionalPriceWithOptionalFields(final String externalId) {

		final PromotionalPrice promotionalPrice = buildDefaultPromotionalPriceWithoutOptionalFields(externalId);
		promotionalPrice.setValidUntil("2021-12-31");
		return promotionalPrice;
	}

	private PriceEntity buildDefaultPriceEntityWithoutOptionalFields(final Instant validFrom) {

		final PriceEntity priceEntity = new PriceEntity();
		final PriceCompoundKey id = new PriceCompoundKey("ACCOUNT_ID-1", "SKU-1", validFrom);

		priceEntity.setValidFrom(validFrom == null ? null : "2030-04-01");
		priceEntity.setSku("SKU-1");
		priceEntity.setBasePrice(new BigDecimal("10.00"));
		priceEntity.setMeasureUnit("CS");
		priceEntity.setMinimumPrice(new BigDecimal("8.40"));
		priceEntity.setDeposit(null);
		priceEntity.setConsignment(null);
		priceEntity.setQuantityPerPallet(null);
		priceEntity.setPromotionalPrice(null);
		priceEntity.setTaxes(null);
		priceEntity.setCharges(null);

		priceEntity.setId(id);
		priceEntity.setDeleted(false);
		priceEntity.setCountry("BR");
		priceEntity.setTimestamp(1617036174L);
		priceEntity.setTimezone("America/Sao_Paulo");
		priceEntity.setCreatedDate(Date.from(Instant.ofEpochSecond(1617036177L)));
		priceEntity.setUpdatedDate(Date.from(Instant.ofEpochSecond(1617036177L)));
		priceEntity.setDeletedDate(null);
		priceEntity.setValidTo(null);
		return priceEntity;
	}

	private PriceEntity buildDefaultPriceEntityWithOptionalFields(final PromotionalPrice promotionalPrice, final Map<String, Tax> taxes,
			final Map<String, Charge> charges) {

		final PriceEntity priceEntity = buildDefaultPriceEntityWithoutOptionalFields(createZonedValidFromDateTime());

		priceEntity.setDeposit(new BigDecimal("1.80"));
		priceEntity.setConsignment(new BigDecimal("2.00"));
		priceEntity.setQuantityPerPallet(4);
		priceEntity.setPromotionalPrice(promotionalPrice);
		priceEntity.setTaxes(taxes);
		priceEntity.setCharges(charges);

		priceEntity.setDeletedDate(Date.from(Instant.ofEpochSecond(1617036477L)));
		priceEntity.setValidTo(Date.from(Instant.ofEpochSecond(1617036377L)));

		return priceEntity;
	}

	private Instant createZonedValidFromDateTime() {

		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		final LocalDateTime d = LocalDate.parse("2030-04-01", formatter).atTime(0, 0);
		final ZoneId zone = ZoneId.of("America/Sao_Paulo");

		return ZonedDateTime.of(d, zone).withZoneSameInstant(ZoneId.of("UTC")).toInstant();
	}

}