package com.abinbev.b2b.price.api.converters.v2;

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
import com.abinbev.b2b.price.domain.model.v2.ChargeV2;
import com.abinbev.b2b.price.domain.model.v2.EntityTypeEnum;
import com.abinbev.b2b.price.domain.model.v2.PriceCompoundKeyV2;
import com.abinbev.b2b.price.domain.model.v2.PriceEntityV2;
import com.abinbev.b2b.price.domain.model.v2.PromotionalPriceV2;
import com.abinbev.b2b.price.domain.model.v2.TaxConditionV2;
import com.abinbev.b2b.price.domain.model.v2.TaxOrderSubTotalV2;
import com.abinbev.b2b.price.domain.model.v2.TaxV2;

@ExtendWith(MockitoExtension.class)
class PriceEntityV2ToPriceFactConverterTest {

	@InjectMocks
	private PriceEntityV2ToPriceFactConverter priceEntityV2ToPriceFactConverter;

	@Test
	void shouldConvertPriceEntityWhenHasNoOptionalFieldsToPriceFactSuccessfully() {

		final PriceEntityV2 priceEntityV2 = buildDefaultPriceEntityWithoutOptionalFields(null);
		final PriceFact priceFact = priceEntityV2ToPriceFactConverter.convert(priceEntityV2);

		assertRequiredFieldsOfPriceFact(priceFact);
		assertThat(priceFact.getValidFrom(), is(nullValue()));
		assertThat(priceFact.getDeposit(), is(nullValue()));
		assertThat(priceFact.getConsignment(), is(nullValue()));
		assertThat(priceFact.getQuantityPerPallet(), is(nullValue()));
		assertThat(priceFact.getPromotionalPrice(), is(nullValue()));
		assertThat(priceFact.getTaxes(), is(nullValue()));
		assertThat(priceFact.getCharges(), is(nullValue()));
	}

	@Test
	void shouldConvertPriceEntityWhenHasNoTaxAndChargeSubOptionalFieldsToPriceFactSuccessfully() {

		final PromotionalPriceV2 promotionalPriceV2 = buildDefaultPromotionalPriceWithOptionalFields();
		final PriceEntityV2 priceEntityV2 = buildDefaultPriceEntityWithOptionalFields(promotionalPriceV2, Maps.of("TAX_ID-1", null),
				Maps.of("CHARGE_ID-1", null));

		final PriceFact priceFact = priceEntityV2ToPriceFactConverter.convert(priceEntityV2);
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
	}

	@Test
	void shouldConvertPriceEntityWhenHasNoTaxConditionOptionalFieldsToPriceFactSuccessfully() {

		final PromotionalPriceV2 promotionalPriceV2 = buildDefaultPromotionalPriceWithOptionalFields();
		final TaxV2 taxV2 = buildDefaultTaxWithoutOptionalFields();
		final ChargeV2 chargeV2 = buildDefaultChargeWithOptionalFields();
		final PriceEntityV2 priceEntityV2 = buildDefaultPriceEntityWithOptionalFields(promotionalPriceV2, Maps.of("TAX_ID-1", taxV2),
				Maps.of("CHARGE_ID-1", chargeV2));

		final PriceFact priceFact = priceEntityV2ToPriceFactConverter.convert(priceEntityV2);
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
	}

	@Test
	void shouldConvertPriceEntityWhenHasNoTaxOrderSubTotalOptionalFieldsToPriceFactSuccessfully() {

		final PromotionalPriceV2 promotionalPriceV2 = buildDefaultPromotionalPriceWithOptionalFields();
		final TaxV2 taxV2 = buildTaxWithoutTaxOrderSubTotalFields();
		final ChargeV2 chargeV2 = buildDefaultChargeWithOptionalFields();
		final PriceEntityV2 priceEntityV2 = buildDefaultPriceEntityWithOptionalFields(promotionalPriceV2, Maps.of("TAX_ID-1", taxV2),
				Maps.of("CHARGE_ID-1", chargeV2));

		final PriceFact priceFact = priceEntityV2ToPriceFactConverter.convert(priceEntityV2);
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
	}

	@Test
	void shouldConvertPriceEntityWhenOptionalFieldsToPriceFactSuccessfully() {

		final PromotionalPriceV2 promotionalPriceV2 = buildDefaultPromotionalPriceWithOptionalFields();
		final TaxV2 taxV2 = buildDefaultTaxWithOptionalFields();
		final ChargeV2 chargeV2 = buildDefaultChargeWithOptionalFields();
		final PriceEntityV2 priceEntityV2 = buildDefaultPriceEntityWithOptionalFields(promotionalPriceV2, Maps.of("TAX_ID-1", taxV2),
				Maps.of("CHARGE_ID-1", chargeV2));

		final PriceFact priceFact = priceEntityV2ToPriceFactConverter.convert(priceEntityV2);
		assertRequiredFieldsOfPriceFact(priceFact);
		assertThat(priceFact.getValidFrom(), is(equalTo("2030-04-01")));
		assertThat(priceFact.getDeposit(), is(equalTo(new BigDecimal("1.80"))));
		assertThat(priceFact.getConsignment(), is(equalTo(new BigDecimal("2.00"))));
		assertThat(priceFact.getQuantityPerPallet(), is(equalTo(4)));

		assertThat(priceFact.getPromotionalPrice(), is(not(nullValue())));
		assertThat(priceFact.getTaxes(), is(not(nullValue())));
		assertThat(priceFact.getCharges(), is(not(nullValue())));

	}

	private void assertRequiredFieldsOfPriceFact(final PriceFact priceFact) {

		assertThat(priceFact.getVendorItemId(), is(equalTo("ITEM_ID-1")));
		assertThat(priceFact.getSku(), is(equalTo("SKU-1")));
		assertThat(priceFact.getBasePrice(), is(equalTo(new BigDecimal("10.00"))));
		assertThat(priceFact.getMinimumPrice(), is(equalTo(new BigDecimal("8.40"))));
		assertThat(priceFact.getMeasureUnit(), is(equalTo("CS")));
	}

	private ChargeV2 buildDefaultChargeWithOptionalFields() {

		final ChargeV2 chargeV2 = buildDefaultChargeWithoutOptionalFields();
		chargeV2.setBase(new BigDecimal("2.40"));
		return chargeV2;
	}

	private ChargeV2 buildDefaultChargeWithoutOptionalFields() {

		final ChargeV2 chargeV2 = new ChargeV2();
		chargeV2.setChargeId("CHARGE_ID-1");
		chargeV2.setType("$");
		chargeV2.setValue(new BigDecimal("2.01"));
		chargeV2.setBase(null);
		return chargeV2;
	}

	private TaxV2 buildDefaultTaxWithOptionalFields() {

		final TaxOrderSubTotalV2 orderSubTotalV2 = new TaxOrderSubTotalV2();
		orderSubTotalV2.setMinimumValue(new BigDecimal("1.05"));

		final TaxConditionV2 taxConditionV2 = buildDefaultTaxConditionWithoutOptionalFields();
		taxConditionV2.setOrderSubTotal(orderSubTotalV2);

		final TaxV2 taxV2 = buildDefaultTaxWithoutOptionalFields();
		taxV2.setTaxBaseInclusionIds(Arrays.asList("TAX_ID-2", "TAX_ID-3"));
		taxV2.setBase(new BigDecimal("1.70"));
		taxV2.setConditions(taxConditionV2);
		return taxV2;
	}

	private TaxV2 buildTaxWithoutTaxOrderSubTotalFields() {

		final TaxConditionV2 taxConditionV2 = buildDefaultTaxConditionWithoutOptionalFields();
		taxConditionV2.setOrderSubTotal(null);

		final TaxV2 taxV2 = buildDefaultTaxWithoutOptionalFields();
		taxV2.setTaxBaseInclusionIds(Arrays.asList("TAX_ID-2", "TAX_ID-3"));
		taxV2.setBase(new BigDecimal("1.70"));
		taxV2.setConditions(taxConditionV2);
		return taxV2;
	}

	private TaxConditionV2 buildDefaultTaxConditionWithoutOptionalFields() {

		final TaxConditionV2 taxConditionV2 = new TaxConditionV2();
		taxConditionV2.setOrderSubTotal(null);
		return taxConditionV2;
	}

	private TaxV2 buildDefaultTaxWithoutOptionalFields() {

		final TaxV2 taxV2 = new TaxV2();
		taxV2.setTaxId("TAX_ID-1");
		taxV2.setType("%");
		taxV2.setValue(new BigDecimal("3.15"));
		taxV2.setTaxBaseInclusionIds(null);
		taxV2.setHidden(false);
		taxV2.setBase(null);
		taxV2.setConditions(null);
		return taxV2;
	}

	private PromotionalPriceV2 buildDefaultPromotionalPriceWithOptionalFields() {

		final PromotionalPriceV2 promotionalPriceV2 = buildDefaultPromotionalPriceWithoutOptionalFields();
		promotionalPriceV2.setValidUntil("2021-12-31");
		return promotionalPriceV2;
	}

	private PromotionalPriceV2 buildDefaultPromotionalPriceWithoutOptionalFields() {

		final PromotionalPriceV2 promotionalPriceV2 = new PromotionalPriceV2();
		promotionalPriceV2.setPrice(new BigDecimal("9.40"));
		promotionalPriceV2.setExternalId("PP_ID-1");
		promotionalPriceV2.setValidUntil(null);
		return promotionalPriceV2;
	}

	private PriceEntityV2 buildDefaultPriceEntityWithoutOptionalFields(final Instant validFrom) {

		final PriceEntityV2 priceEntityV2 = new PriceEntityV2();
		final PriceCompoundKeyV2 id = new PriceCompoundKeyV2();
		id.setId("ACCOUNT_ID-1");
		id.setType(EntityTypeEnum.ACCOUNT.toString());
		id.setVendorItemId("ITEM_ID-1");
		id.setValidFrom(validFrom);

		priceEntityV2.setValidFrom(validFrom == null ? null : "2030-04-01");
		priceEntityV2.setSku("SKU-1");
		priceEntityV2.setBasePrice(new BigDecimal("10.00"));
		priceEntityV2.setMeasureUnit("CS");
		priceEntityV2.setMinimumPrice(new BigDecimal("8.40"));
		priceEntityV2.setDeposit(null);
		priceEntityV2.setConsignment(null);
		priceEntityV2.setQuantityPerPallet(null);
		priceEntityV2.setPromotionalPrice(null);
		priceEntityV2.setTaxes(null);
		priceEntityV2.setCharges(null);

		priceEntityV2.setId(id);
		priceEntityV2.setDeleted(false);
		priceEntityV2.setCountry("BR");
		priceEntityV2.setTimestamp(1617036174L);
		priceEntityV2.setTimezone("America/Sao_Paulo");
		priceEntityV2.setCreatedDate(Date.from(Instant.ofEpochSecond(1617036177L)));
		priceEntityV2.setUpdatedDate(Date.from(Instant.ofEpochSecond(1617036177L)));
		priceEntityV2.setDeletedDate(null);
		priceEntityV2.setValidTo(null);
		return priceEntityV2;
	}

	private PriceEntityV2 buildDefaultPriceEntityWithOptionalFields(final PromotionalPriceV2 promotionalPrice,
			final Map<String, TaxV2> taxes, final Map<String, ChargeV2> charges) {

		final PriceEntityV2 priceEntityV2 = buildDefaultPriceEntityWithoutOptionalFields(createZonedValidFromDateTime());

		priceEntityV2.setDeposit(new BigDecimal("1.80"));
		priceEntityV2.setConsignment(new BigDecimal("2.00"));
		priceEntityV2.setQuantityPerPallet(4);
		priceEntityV2.setPromotionalPrice(promotionalPrice);
		priceEntityV2.setTaxes(taxes);
		priceEntityV2.setCharges(charges);

		priceEntityV2.setDeletedDate(Date.from(Instant.ofEpochSecond(1617036477L)));
		priceEntityV2.setValidTo(Date.from(Instant.ofEpochSecond(1617036377L)));

		return priceEntityV2;
	}

	private Instant createZonedValidFromDateTime() {

		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		final LocalDateTime d = LocalDate.parse("2030-04-01", formatter).atTime(0, 0);
		final ZoneId zone = ZoneId.of("America/Sao_Paulo");

		return ZonedDateTime.of(d, zone).withZoneSameInstant(ZoneId.of("UTC")).toInstant();
	}

}