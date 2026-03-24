package com.abinbev.b2b.price.api.converters.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsIterableContaining.hasItems;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.groovy.util.Maps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abinbev.b2b.price.api.domain.v2.PriceResultListV2;
import com.abinbev.b2b.price.api.rest.vo.v2.ChargeResponseVoV2;
import com.abinbev.b2b.price.api.rest.vo.v2.PaginationResponseVoV2;
import com.abinbev.b2b.price.api.rest.vo.v2.PriceResponseVoV2;
import com.abinbev.b2b.price.api.rest.vo.v2.PriceVoV2;
import com.abinbev.b2b.price.api.rest.vo.v2.TaxResponseVoV2;
import com.abinbev.b2b.price.domain.model.v2.ChargeV2;
import com.abinbev.b2b.price.domain.model.v2.PriceCompoundKeyV2;
import com.abinbev.b2b.price.domain.model.v2.PriceEntityV2;
import com.abinbev.b2b.price.domain.model.v2.PromotionalPriceV2;
import com.abinbev.b2b.price.domain.model.v2.TaxConditionV2;
import com.abinbev.b2b.price.domain.model.v2.TaxOrderSubTotalV2;
import com.abinbev.b2b.price.domain.model.v2.TaxV2;
import com.abinbev.b2b.price.domain.model.v2.enums.PriceMeasureUnitType;

@ExtendWith(MockitoExtension.class)
class PriceResultListV2ToPriceResponseVoV2ConverterTest {

	@InjectMocks
	private PriceResultListV2ToPriceResponseVoV2Converter priceResultListV2ToPriceResponseVoV2Converter;

	private Instant createZonedValidFromDateTime() {

		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		final LocalDateTime d = LocalDate.parse("2030-04-01", formatter).atTime(0, 0);
		final ZoneId zone = ZoneId.of("America/Sao_Paulo");

		return ZonedDateTime.of(d, zone).withZoneSameInstant(ZoneId.of("UTC")).toInstant();
	}

	private PriceEntityV2 buildDefaultPriceEntityV2WithoutOptionalFields() {

		final PriceEntityV2 priceEntityV2 = new PriceEntityV2();
		final PriceCompoundKeyV2 id = new PriceCompoundKeyV2();
		id.setVendorId("VENDOR_ID-1");
		id.setId("VENDOR_ACCOUNT_ID-1");
		id.setVendorItemId("VENDOR_ITEM_ID-1");
		id.setValidFrom(null);

		priceEntityV2.setValidFrom(null);// this fields must not exist in the
		// entity
		priceEntityV2.setVendorItemId(null);// this fields must not exist in the
		// entity
		priceEntityV2.setSku("SKU-1");
		priceEntityV2.setBasePrice(new BigDecimal("10.00"));
		priceEntityV2.setMeasureUnit("CS");
		priceEntityV2.setMinimumPrice(new BigDecimal("8.40"));
		priceEntityV2.setType(null);
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

	private PromotionalPriceV2 buildDefaultPromotionalPriceV2WithoutOptionalFields(final String externalId) {

		final PromotionalPriceV2 promotionalPrice = new PromotionalPriceV2();
		promotionalPrice.setPrice(new BigDecimal("9.40"));
		promotionalPrice.setExternalId(externalId);
		promotionalPrice.setValidUntil(null);
		return promotionalPrice;
	}

	private TaxV2 buildDefaultTaxV2WithoutOptionalFields(final String taxId) {

		final TaxV2 taxV2 = new TaxV2();
		taxV2.setTaxId(taxId);
		taxV2.setType("$");
		taxV2.setValue(new BigDecimal("3.15"));
		taxV2.setTaxBaseInclusionIds(null);
		taxV2.setHidden(false);
		taxV2.setBase(null);
		taxV2.setConditions(null);
		return taxV2;
	}

	private TaxConditionV2 buildDefaultTaxConditionV2WithoutOptionalFields() {

		final TaxConditionV2 taxConditionV2 = new TaxConditionV2();
		taxConditionV2.setOrderSubTotal(null);
		return taxConditionV2;
	}

	private ChargeV2 buildDefaultChargeV2WithoutOptionalFields(final String chargeId) {

		final ChargeV2 chargeV2 = new ChargeV2();
		chargeV2.setChargeId(chargeId);
		chargeV2.setType("$");
		chargeV2.setValue(new BigDecimal("2.01"));
		chargeV2.setBase(null);
		return chargeV2;
	}

	private PromotionalPriceV2 buildDefaultPromotionalPriceV2WithOptionalFields(final String externalId) {

		final PromotionalPriceV2 promotionalPrice = buildDefaultPromotionalPriceV2WithoutOptionalFields(externalId);
		promotionalPrice.setValidUntil("2021-12-31");
		return promotionalPrice;
	}

	private TaxV2 buildDefaultTaxV2WithOptionalFields(final String taxId) {

		final TaxOrderSubTotalV2 orderSubTotal = new TaxOrderSubTotalV2();
		orderSubTotal.setMinimumValue(new BigDecimal("1.05"));

		final TaxConditionV2 taxConditionV2 = buildDefaultTaxConditionV2WithoutOptionalFields();
		taxConditionV2.setOrderSubTotal(orderSubTotal);

		final TaxV2 taxV2 = buildDefaultTaxV2WithoutOptionalFields(taxId);
		taxV2.setTaxBaseInclusionIds(Arrays.asList("TAX_ID-2", "TAX_ID-3"));
		taxV2.setBase(new BigDecimal("1.70"));
		taxV2.setConditions(taxConditionV2);
		taxV2.setProportional(true);
		return taxV2;
	}

	private ChargeV2 buildDefaultChargeV2WithOptionalFields(final String chargeId) {

		final ChargeV2 chargeV2 = buildDefaultChargeV2WithoutOptionalFields(chargeId);
		chargeV2.setBase(new BigDecimal("2.40"));
		return chargeV2;
	}

	private PriceEntityV2 buildDefaultPriceEntityV2WithOptionalFields(final PromotionalPriceV2 promotionalPrice,
			final Map<String, TaxV2> taxes, final Map<String, ChargeV2> charges) {

		final PriceEntityV2 priceEntityV2 = buildDefaultPriceEntityV2WithoutOptionalFields();
		priceEntityV2.getId().setValidFrom(createZonedValidFromDateTime());

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

	private PriceResultListV2 buildPriceResultListV2ByPriceEntitiesArray(final PriceEntityV2... priceEntitiesArray) {

		final List<PriceEntityV2> priceEntities = Arrays.asList(priceEntitiesArray);
		final PaginationResponseVoV2 pagination = new PaginationResponseVoV2();
		pagination.setPage(3);
		pagination.setSize(priceEntities.size());
		pagination.setTotalPages(5);
		pagination.setTotalElements(priceEntities.size() * 5);
		return new PriceResultListV2(priceEntities, pagination);
	}

	private void assertBasicInformationOfPagination(final PriceResponseVoV2 priceResponseVoV2, final int paginationSize) {

		assertThat(priceResponseVoV2, is(not(nullValue())));
		assertThat(priceResponseVoV2.getPagination(), is(not(nullValue())));
		assertThat(priceResponseVoV2.getPagination().getPage(), is(equalTo(3)));
		assertThat(priceResponseVoV2.getPagination().getSize(), is(equalTo(paginationSize)));
		assertThat(priceResponseVoV2.getPagination().getTotalPages(), is(equalTo(5)));
		assertThat(priceResponseVoV2.getPagination().getTotalElements(), is(equalTo(paginationSize * 5L)));
		assertThat(priceResponseVoV2.getPrices(), is(not(nullValue())));
		assertThat(priceResponseVoV2.getPrices(), hasSize(paginationSize));
	}

	private void assertRequiredFieldsOfPrice(final PriceVoV2 priceVoV2) {

		assertThat(priceVoV2.getVendorId(), is(equalTo("VENDOR_ID-1")));
		assertThat(priceVoV2.getVendorAccountId(), is(equalTo("VENDOR_ACCOUNT_ID-1")));
		assertThat(priceVoV2.getVendorItemId(), is(equalTo("VENDOR_ITEM_ID-1")));
		assertThat(priceVoV2.getSku(), is(equalTo("SKU-1")));
		assertThat(priceVoV2.getBasePrice(), is(equalTo(new BigDecimal("10.00"))));
		assertThat(priceVoV2.getMinimumPrice(), is(equalTo(new BigDecimal("8.40"))));
		assertThat(priceVoV2.getMeasureUnit(), is(equalTo("CS")));
		assertThat(priceVoV2.getTimezone(), is(equalTo("America/Sao_Paulo")));
		assertThat(priceVoV2.getType(), is(equalTo("PER_UNIT")));
	}

	@Test
	void shouldTryConvertPriceEntityV2WhenHasNoOptionalFieldsProcessCorrectly() {

		final PriceEntityV2 price1 = buildDefaultPriceEntityV2WithoutOptionalFields();
		final PriceResultListV2 priceResultListV2 = buildPriceResultListV2ByPriceEntitiesArray(price1);

		final PriceResponseVoV2 priceResponseVoV2 = priceResultListV2ToPriceResponseVoV2Converter.convert(priceResultListV2);
		assertBasicInformationOfPagination(priceResponseVoV2, 1);

		final PriceVoV2 priceVoV2 = priceResponseVoV2.getPrices().get(0);
		assertRequiredFieldsOfPrice(priceVoV2);
		assertThat(priceVoV2.getValidFrom(), is(nullValue()));
		assertThat(priceVoV2.getDeposit(), is(nullValue()));
		assertThat(priceVoV2.getConsignment(), is(nullValue()));
		assertThat(priceVoV2.getQuantityPerPallet(), is(nullValue()));
		assertThat(priceVoV2.getPromotionalPrice(), is(nullValue()));
		assertThat(priceVoV2.getTaxes(), is(nullValue()));
		assertThat(priceVoV2.getCharges(), is(nullValue()));
	}

	@Test
	void shouldTryConvertPriceEntityV2WhenHasNoOptionalFieldsIntoPromotionalPriceProcessCorrectly() {

		final PromotionalPriceV2 promotionalPrice = buildDefaultPromotionalPriceV2WithoutOptionalFields("PP_ID-1");
		final PriceEntityV2 price1 = buildDefaultPriceEntityV2WithoutOptionalFields();
		price1.setPromotionalPrice(promotionalPrice);
		final PriceResultListV2 priceResultListV2 = buildPriceResultListV2ByPriceEntitiesArray(price1);

		final PriceResponseVoV2 priceResponseVoV2 = priceResultListV2ToPriceResponseVoV2Converter.convert(priceResultListV2);
		assertBasicInformationOfPagination(priceResponseVoV2, 1);

		final PriceVoV2 priceVoV2 = priceResponseVoV2.getPrices().get(0);
		assertRequiredFieldsOfPrice(priceVoV2);
		assertThat(priceVoV2.getValidFrom(), is(nullValue()));
		assertThat(priceVoV2.getDeposit(), is(nullValue()));
		assertThat(priceVoV2.getConsignment(), is(nullValue()));
		assertThat(priceVoV2.getQuantityPerPallet(), is(nullValue()));

		assertThat(priceVoV2.getPromotionalPrice(), is(not(nullValue())));
		assertThat(priceVoV2.getPromotionalPrice().getPrice(), is(equalTo(new BigDecimal("9.40"))));
		assertThat(priceVoV2.getPromotionalPrice().getExternalId(), is(equalTo("PP_ID-1")));
		assertThat(priceVoV2.getPromotionalPrice().getValidUntil(), is(nullValue()));

		assertThat(priceVoV2.getTaxes(), is(nullValue()));
		assertThat(priceVoV2.getCharges(), is(nullValue()));
	}

	@Test
	void shouldTryConvertPriceEntityV2WhenHasNoOptionalFieldsIntoTaxesProcessCorrectly() {

		final TaxV2 tax = buildDefaultTaxV2WithoutOptionalFields("TAX_ID-1");
		final PriceEntityV2 price1 = buildDefaultPriceEntityV2WithoutOptionalFields();
		price1.setTaxes(Maps.of("TAX_ID-1", tax));
		final PriceResultListV2 priceResultListV2 = buildPriceResultListV2ByPriceEntitiesArray(price1);

		final PriceResponseVoV2 priceResponseVoV2 = priceResultListV2ToPriceResponseVoV2Converter.convert(priceResultListV2);
		assertBasicInformationOfPagination(priceResponseVoV2, 1);

		final PriceVoV2 priceVoV2 = priceResponseVoV2.getPrices().get(0);
		assertRequiredFieldsOfPrice(priceVoV2);
		assertThat(priceVoV2.getValidFrom(), is(nullValue()));
		assertThat(priceVoV2.getDeposit(), is(nullValue()));
		assertThat(priceVoV2.getConsignment(), is(nullValue()));
		assertThat(priceVoV2.getQuantityPerPallet(), is(nullValue()));
		assertThat(priceVoV2.getPromotionalPrice(), is(nullValue()));

		assertThat(priceVoV2.getTaxes(), is(not(nullValue())));
		assertThat(priceVoV2.getTaxes().entrySet(), hasSize(1));
		final TaxResponseVoV2 taxResponseVoV2 = priceVoV2.getTaxes().values().iterator().next();
		assertThat(taxResponseVoV2.getTaxId(), is(equalTo("TAX_ID-1")));
		assertThat(taxResponseVoV2.getType(), is(equalTo("$")));
		assertThat(taxResponseVoV2.getValue(), is(equalTo(new BigDecimal("3.15"))));
		assertThat(taxResponseVoV2.getTaxBaseInclusionIds(), is(nullValue()));
		assertThat(taxResponseVoV2.isHidden(), is(false));
		assertThat(taxResponseVoV2.getBase(), is(nullValue()));
		assertThat(taxResponseVoV2.getConditions(), is(nullValue()));
		assertThat(taxResponseVoV2.getProportional(), is(nullValue()));

		assertThat(priceVoV2.getCharges(), is(nullValue()));
	}

	@Test
	void shouldTryConvertPriceEntityV2WhenHasNoOptionalFieldsIntoTaxesConditionsProcessCorrectly() {

		final TaxV2 tax = buildDefaultTaxV2WithoutOptionalFields("TAX_ID-1");
		tax.setConditions(buildDefaultTaxConditionV2WithoutOptionalFields());
		final PriceEntityV2 price1 = buildDefaultPriceEntityV2WithoutOptionalFields();
		price1.setTaxes(Maps.of("TAX_ID-1", tax));
		final PriceResultListV2 priceResultListV2 = buildPriceResultListV2ByPriceEntitiesArray(price1);

		final PriceResponseVoV2 priceResponseVoV2 = priceResultListV2ToPriceResponseVoV2Converter.convert(priceResultListV2);
		assertBasicInformationOfPagination(priceResponseVoV2, 1);

		final PriceVoV2 priceVoV2 = priceResponseVoV2.getPrices().get(0);
		assertRequiredFieldsOfPrice(priceVoV2);
		assertThat(priceVoV2.getValidFrom(), is(nullValue()));
		assertThat(priceVoV2.getDeposit(), is(nullValue()));
		assertThat(priceVoV2.getConsignment(), is(nullValue()));
		assertThat(priceVoV2.getQuantityPerPallet(), is(nullValue()));
		assertThat(priceVoV2.getPromotionalPrice(), is(nullValue()));

		assertThat(priceVoV2.getTaxes(), is(not(nullValue())));
		assertThat(priceVoV2.getTaxes().entrySet(), hasSize(1));
		final TaxResponseVoV2 taxResponseVoV2 = priceVoV2.getTaxes().values().iterator().next();
		assertThat(taxResponseVoV2.getTaxId(), is(equalTo("TAX_ID-1")));
		assertThat(taxResponseVoV2.getType(), is(equalTo("$")));
		assertThat(taxResponseVoV2.getValue(), is(equalTo(new BigDecimal("3.15"))));
		assertThat(taxResponseVoV2.getTaxBaseInclusionIds(), is(nullValue()));
		assertThat(taxResponseVoV2.isHidden(), is(false));
		assertThat(taxResponseVoV2.getBase(), is(nullValue()));
		assertThat(taxResponseVoV2.getConditions(), is(not(nullValue())));
		assertThat(taxResponseVoV2.getConditions().getOrderSubTotal(), is(nullValue()));
		assertThat(taxResponseVoV2.getProportional(), is(nullValue()));

		assertThat(priceVoV2.getCharges(), is(nullValue()));
	}

	@Test
	void shouldTryConvertPriceEntityV2WhenHasNoOptionalFieldsIntoChargesProcessCorrectly() {

		final ChargeV2 charge = buildDefaultChargeV2WithoutOptionalFields("CHARGE_ID-1");
		final PriceEntityV2 price1 = buildDefaultPriceEntityV2WithoutOptionalFields();
		price1.setCharges(Maps.of("CHARGE_ID-1", charge));
		final PriceResultListV2 priceResultListV2 = buildPriceResultListV2ByPriceEntitiesArray(price1);

		final PriceResponseVoV2 priceResponseVoV2 = priceResultListV2ToPriceResponseVoV2Converter.convert(priceResultListV2);
		assertBasicInformationOfPagination(priceResponseVoV2, 1);

		final PriceVoV2 priceVoV2 = priceResponseVoV2.getPrices().get(0);
		assertRequiredFieldsOfPrice(priceVoV2);
		assertThat(priceVoV2.getValidFrom(), is(nullValue()));
		assertThat(priceVoV2.getDeposit(), is(nullValue()));
		assertThat(priceVoV2.getConsignment(), is(nullValue()));
		assertThat(priceVoV2.getQuantityPerPallet(), is(nullValue()));
		assertThat(priceVoV2.getPromotionalPrice(), is(nullValue()));
		assertThat(priceVoV2.getTaxes(), is(nullValue()));

		assertThat(priceVoV2.getCharges(), is(not(nullValue())));
		assertThat(priceVoV2.getCharges().entrySet(), hasSize(1));
		final ChargeResponseVoV2 chargeResponseVoV2 = priceVoV2.getCharges().values().iterator().next();
		assertThat(chargeResponseVoV2.getChargeId(), is(equalTo("CHARGE_ID-1")));
		assertThat(chargeResponseVoV2.getType(), is(equalTo("$")));
		assertThat(chargeResponseVoV2.getValue(), is(equalTo(new BigDecimal("2.01"))));
		assertThat(chargeResponseVoV2.getBase(), is(nullValue()));
	}

	@Test
	void shouldTryConvertPriceEntityV2WhenOptionalFieldsFillThePriceVoV2Correctly() {

		final PromotionalPriceV2 promotionalPrice = buildDefaultPromotionalPriceV2WithOptionalFields("PP_ID-1");
		final TaxV2 tax = buildDefaultTaxV2WithOptionalFields("TAX_ID-1");
		final ChargeV2 charge = buildDefaultChargeV2WithOptionalFields("CHARGE_ID-1");
		final PriceEntityV2 price1 = buildDefaultPriceEntityV2WithOptionalFields(promotionalPrice, Maps.of("TAX_ID-1", tax),
				Maps.of("CHARGE_ID-1", charge));
		final PriceResultListV2 priceResultListV2 = buildPriceResultListV2ByPriceEntitiesArray(price1);

		final PriceResponseVoV2 priceResponseVoV2 = priceResultListV2ToPriceResponseVoV2Converter.convert(priceResultListV2);
		assertBasicInformationOfPagination(priceResponseVoV2, 1);

		final PriceVoV2 priceVoV2 = priceResponseVoV2.getPrices().get(0);
		assertRequiredFieldsOfPrice(priceVoV2);
		assertThat(priceVoV2.getValidFrom(), is(equalTo("2030-04-01")));
		assertThat(priceVoV2.getDeposit(), is(equalTo(new BigDecimal("1.80"))));
		assertThat(priceVoV2.getConsignment(), is(equalTo(new BigDecimal("2.00"))));
		assertThat(priceVoV2.getQuantityPerPallet(), is(equalTo(4)));

		assertThat(priceVoV2.getPromotionalPrice(), is(not(nullValue())));
		assertThat(priceVoV2.getTaxes(), is(not(nullValue())));
		assertThat(priceVoV2.getCharges(), is(not(nullValue())));
	}

	@Test
	void shouldTryConvertPriceEntityV2WhenOptionalFieldsFillTheInternalObjectsCorrectly() {

		final PromotionalPriceV2 promotionalPrice = buildDefaultPromotionalPriceV2WithOptionalFields("PP_ID-1");
		final TaxV2 tax = buildDefaultTaxV2WithOptionalFields("TAX_ID-1");
		final ChargeV2 charge = buildDefaultChargeV2WithOptionalFields("CHARGE_ID-1");
		final PriceEntityV2 price1 = buildDefaultPriceEntityV2WithOptionalFields(promotionalPrice, Maps.of("TAX_ID-1", tax),
				Maps.of("CHARGE_ID-1", charge));
		final PriceResultListV2 priceResultListV2 = buildPriceResultListV2ByPriceEntitiesArray(price1);

		final PriceResponseVoV2 priceResponseVoV2 = priceResultListV2ToPriceResponseVoV2Converter.convert(priceResultListV2);
		assertBasicInformationOfPagination(priceResponseVoV2, 1);

		final PriceVoV2 priceVoV2 = priceResponseVoV2.getPrices().get(0);

		assertThat(priceVoV2.getPromotionalPrice(), is(not(nullValue())));
		assertThat(priceVoV2.getPromotionalPrice().getPrice(), is(equalTo(new BigDecimal("9.40"))));
		assertThat(priceVoV2.getPromotionalPrice().getExternalId(), is(equalTo("PP_ID-1")));
		assertThat(priceVoV2.getPromotionalPrice().getValidUntil(), is(equalTo("2021-12-31")));

		final TaxResponseVoV2 taxResponseVoV2 = priceVoV2.getTaxes().values().iterator().next();
		assertThat(taxResponseVoV2.getTaxId(), is(equalTo("TAX_ID-1")));
		assertThat(taxResponseVoV2.getType(), is(equalTo("$")));
		assertThat(taxResponseVoV2.getValue(), is(equalTo(new BigDecimal("3.15"))));
		assertThat(taxResponseVoV2.getTaxBaseInclusionIds(), is(equalTo(Arrays.asList("TAX_ID-2", "TAX_ID-3"))));
		assertThat(taxResponseVoV2.isHidden(), is(false));
		assertThat(taxResponseVoV2.getBase(), is(equalTo(new BigDecimal("1.70"))));
		assertThat(taxResponseVoV2.getConditions(), is(not(nullValue())));
		assertThat(taxResponseVoV2.getConditions().getOrderSubTotal(), is(not(nullValue())));
		assertThat(taxResponseVoV2.getConditions().getOrderSubTotal().getMinimumValue(), is(equalTo(new BigDecimal("1.05"))));
		assertThat(taxResponseVoV2.getProportional(), is(equalTo(true)));

		assertThat(priceVoV2.getCharges(), is(not(nullValue())));
		assertThat(priceVoV2.getCharges().entrySet(), hasSize(1));
		final ChargeResponseVoV2 chargeResponseVoV2 = priceVoV2.getCharges().values().iterator().next();
		assertThat(chargeResponseVoV2.getChargeId(), is(equalTo("CHARGE_ID-1")));
		assertThat(chargeResponseVoV2.getType(), is(equalTo("$")));
		assertThat(chargeResponseVoV2.getValue(), is(equalTo(new BigDecimal("2.01"))));
		assertThat(chargeResponseVoV2.getBase(), is(equalTo(new BigDecimal("2.40"))));
	}

	@Test
	void shouldTryConvertManyPriceEntityV2WhenManyInternalObjectsGenerateTheSameQuantityOfObjects() {

		final PromotionalPriceV2 promotionalPrice1 = buildDefaultPromotionalPriceV2WithOptionalFields("PP_ID-1");
		final TaxV2 tax1_1 = buildDefaultTaxV2WithOptionalFields("TAX_ID-1_1");
		final TaxV2 tax1_2 = buildDefaultTaxV2WithOptionalFields("TAX_ID-1_2");
		final ChargeV2 charge1_1 = buildDefaultChargeV2WithOptionalFields("CHARGE_ID-1_1");
		final ChargeV2 charge1_2 = buildDefaultChargeV2WithOptionalFields("CHARGE_ID-1_2");
		final PriceEntityV2 price1 = buildDefaultPriceEntityV2WithOptionalFields(promotionalPrice1,
				Maps.of("TAX_ID-1_1", tax1_1, "TAX_ID-1_2", tax1_2), Maps.of("CHARGE_ID-1_1", charge1_1, "CHARGE_ID-1_2", charge1_2));

		final PromotionalPriceV2 promotionalPrice2 = buildDefaultPromotionalPriceV2WithOptionalFields("PP_ID-2");
		final TaxV2 tax2 = buildDefaultTaxV2WithOptionalFields("TAX_ID-2");
		final ChargeV2 charge2 = buildDefaultChargeV2WithOptionalFields("CHARGE_ID-2");
		final PriceEntityV2 price2 = buildDefaultPriceEntityV2WithOptionalFields(promotionalPrice2, Maps.of("TAX_ID-2", tax2),
				Maps.of("CHARGE_ID-2", charge2));

		final PriceResultListV2 priceResultListV2 = buildPriceResultListV2ByPriceEntitiesArray(price1, price2);

		final PriceResponseVoV2 priceResponseVoV2 = priceResultListV2ToPriceResponseVoV2Converter.convert(priceResultListV2);
		assertBasicInformationOfPagination(priceResponseVoV2, 2);

		final PriceVoV2 priceVoV2_1 = priceResponseVoV2.getPrices().get(0);

		assertThat(priceVoV2_1.getPromotionalPrice(), is(not(nullValue())));
		assertThat(priceVoV2_1.getPromotionalPrice().getExternalId(), is(equalTo("PP_ID-1")));

		assertThat(priceVoV2_1.getTaxes(), is(not(nullValue())));
		assertThat(priceVoV2_1.getTaxes().entrySet(), hasSize(2));
		final List<String> taxesIds1 = priceVoV2_1.getTaxes().values().stream().map(TaxResponseVoV2::getTaxId).collect(Collectors.toList());
		assertThat(taxesIds1, hasItems("TAX_ID-1_1", "TAX_ID-1_2"));

		assertThat(priceVoV2_1.getCharges(), is(not(nullValue())));
		assertThat(priceVoV2_1.getCharges().entrySet(), hasSize(2));
		final List<String> chargeIds1 = priceVoV2_1.getCharges().values().stream().map(ChargeResponseVoV2::getChargeId)
				.collect(Collectors.toList());
		assertThat(chargeIds1, hasItems("CHARGE_ID-1_1", "CHARGE_ID-1_2"));

		final PriceVoV2 priceVoV2_2 = priceResponseVoV2.getPrices().get(1);

		assertThat(priceVoV2_2.getPromotionalPrice(), is(not(nullValue())));
		assertThat(priceVoV2_2.getPromotionalPrice().getExternalId(), is(equalTo("PP_ID-2")));

		assertThat(priceVoV2_2.getTaxes(), is(not(nullValue())));
		assertThat(priceVoV2_2.getTaxes().entrySet(), hasSize(1));
		assertThat(priceVoV2_2.getTaxes().values().iterator().next().getTaxId(), is(equalTo("TAX_ID-2")));

		assertThat(priceVoV2_2.getCharges(), is(not(nullValue())));
		assertThat(priceVoV2_2.getCharges().entrySet(), hasSize(1));
		assertThat(priceVoV2_2.getCharges().values().iterator().next().getChargeId(), is(equalTo("CHARGE_ID-2")));
	}

	@Test
	void shouldTryConvertPriceEntityV2WhenTypeFieldIsFilled() {

		final PriceEntityV2 price1 = buildDefaultPriceEntityV2WithoutOptionalFields();
		price1.setType(PriceMeasureUnitType.PER_UOM);
		final PriceResultListV2 priceResultListV2 = buildPriceResultListV2ByPriceEntitiesArray(price1);

		final PriceResponseVoV2 priceResponseVoV2 = priceResultListV2ToPriceResponseVoV2Converter.convert(priceResultListV2);
		assertBasicInformationOfPagination(priceResponseVoV2, 1);

		assertThat(priceResponseVoV2.getPrices().get(0).getType(), is(equalTo("PER_UOM")));
	}

}