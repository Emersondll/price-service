package com.abinbev.b2b.price.api.converters.v3;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsIterableContaining.hasItems;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
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
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.groovy.util.Maps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abinbev.b2b.price.api.domain.v3.PriceNormalizedInfo;
import com.abinbev.b2b.price.api.rest.vo.v3.ChargeResponseVoV3;
import com.abinbev.b2b.price.api.rest.vo.v3.PriceResponseVoV3;
import com.abinbev.b2b.price.api.rest.vo.v3.TaxAmountResponseVoV3;
import com.abinbev.b2b.price.api.rest.vo.v3.TaxResponseVoV3;
import com.abinbev.b2b.price.domain.model.v2.ChargeV2;
import com.abinbev.b2b.price.domain.model.v2.PriceCompoundKeyV2;
import com.abinbev.b2b.price.domain.model.v2.PriceEntityV2;
import com.abinbev.b2b.price.domain.model.v2.PromotionalPriceV2;
import com.abinbev.b2b.price.domain.model.v2.TaxAmountV2;
import com.abinbev.b2b.price.domain.model.v2.TaxBaseChangesCompareWithV2;
import com.abinbev.b2b.price.domain.model.v2.TaxBaseChangesV2;
import com.abinbev.b2b.price.domain.model.v2.TaxConditionV2;
import com.abinbev.b2b.price.domain.model.v2.TaxDeductionBaseChangesV2;
import com.abinbev.b2b.price.domain.model.v2.TaxDeductionV2;
import com.abinbev.b2b.price.domain.model.v2.TaxOrderSubTotalV2;
import com.abinbev.b2b.price.domain.model.v2.TaxV2;
import com.abinbev.b2b.price.domain.model.v2.enums.PriceMeasureUnitType;
import com.abinbev.b2b.price.domain.model.v2.enums.TaxBaseChangesType;

@ExtendWith(MockitoExtension.class)
class PriceNormalizedInfoToPriceResponseVoV3ConverterTest {

	private static final String CONTRACT_ID = "contractId1";
	private static final String ITEM_ID = "itemId1";
	private static final String DELIVERY_CENTER_ID = "deliveryCenterId1";

	@InjectMocks
	private PriceNormalizedInfoToPriceResponseVoV3Converter priceNormalizedInfoToPriceResponseVoV3Converter;

	@Test
	void shouldTryConvertPriceEntityV2WhenHasNoOptionalFieldsProcessCorrectly() {

		final PriceEntityV2 price1 = buildDefaultPriceEntityV2WithoutOptionalFields();
		final PriceNormalizedInfo priceNormalizedInfo = buildPriceNormalizedInfo(price1);

		final PriceResponseVoV3 priceResponseVoV3 = priceNormalizedInfoToPriceResponseVoV3Converter.convert(priceNormalizedInfo);

		assertRequiredFieldsOfPrice(priceResponseVoV3);
		assertPriceNormalizedFields(priceResponseVoV3);

		assertThat(priceResponseVoV3.getValidFrom(), is(nullValue()));
		assertThat(priceResponseVoV3.getDeposit(), is(nullValue()));
		assertThat(priceResponseVoV3.getConsignment(), is(nullValue()));
		assertThat(priceResponseVoV3.getQuantityPerPallet(), is(nullValue()));
		assertThat(priceResponseVoV3.getPromotionalPrice(), is(nullValue()));
		assertThat(priceResponseVoV3.getTaxes(), is(nullValue()));
		assertThat(priceResponseVoV3.getCharges(), is(nullValue()));
	}

	@Test
	void shouldTryConvertPriceEntityV2WhenHasNoOptionalFieldsIntoPromotionalPriceProcessCorrectly() {

		final PromotionalPriceV2 promotionalPrice = buildDefaultPromotionalPriceV2WithoutOptionalFields("PP_ID-1");
		final PriceEntityV2 price1 = buildDefaultPriceEntityV2WithoutOptionalFields();
		price1.setPromotionalPrice(promotionalPrice);

		final PriceNormalizedInfo priceNormalizedInfo = buildPriceNormalizedInfo(price1);
		priceNormalizedInfo.setContractId(null);

		final PriceResponseVoV3 priceResponseVoV3 = priceNormalizedInfoToPriceResponseVoV3Converter.convert(priceNormalizedInfo);

		assertThat(priceResponseVoV3.getItemId(), is(equalTo(ITEM_ID)));
		assertThat(priceResponseVoV3.getContractId(), is(nullValue()));
		assertThat(priceResponseVoV3.getDeliveryCenterId(), is(equalTo(DELIVERY_CENTER_ID)));

		assertRequiredFieldsOfPrice(priceResponseVoV3);

		assertThat(priceResponseVoV3.getValidFrom(), is(nullValue()));
		assertThat(priceResponseVoV3.getDeposit(), is(nullValue()));
		assertThat(priceResponseVoV3.getConsignment(), is(nullValue()));
		assertThat(priceResponseVoV3.getQuantityPerPallet(), is(nullValue()));

		assertThat(priceResponseVoV3.getPromotionalPrice(), is(not(nullValue())));
		assertThat(priceResponseVoV3.getPromotionalPrice().getPrice(), is(equalTo(new BigDecimal("9.40"))));
		assertThat(priceResponseVoV3.getPromotionalPrice().getExternalId(), is(equalTo("PP_ID-1")));
		assertThat(priceResponseVoV3.getPromotionalPrice().getValidUntil(), is(nullValue()));

		assertThat(priceResponseVoV3.getTaxes(), is(nullValue()));
		assertThat(priceResponseVoV3.getCharges(), is(nullValue()));
	}

	@Test
	void shouldTryConvertPriceEntityV2WhenHasNoOptionalFieldsIntoTaxesProcessCorrectly() {

		final TaxV2 tax = buildDefaultTaxV2WithoutOptionalFields("TAX_ID-1");
		final PriceEntityV2 price1 = buildDefaultPriceEntityV2WithoutOptionalFields();
		price1.setTaxes(Maps.of("TAX_ID-1", tax));
		final PriceNormalizedInfo priceNormalizedInfo = buildPriceNormalizedInfo(price1);

		final PriceResponseVoV3 priceResponseVoV3 = priceNormalizedInfoToPriceResponseVoV3Converter.convert(priceNormalizedInfo);

		assertRequiredFieldsOfPrice(priceResponseVoV3);
		assertPriceNormalizedFields(priceResponseVoV3);

		assertThat(priceResponseVoV3.getValidFrom(), is(nullValue()));
		assertThat(priceResponseVoV3.getDeposit(), is(nullValue()));
		assertThat(priceResponseVoV3.getConsignment(), is(nullValue()));
		assertThat(priceResponseVoV3.getQuantityPerPallet(), is(nullValue()));
		assertThat(priceResponseVoV3.getPromotionalPrice(), is(nullValue()));

		assertThat(priceResponseVoV3.getTaxes(), is(not(nullValue())));
		assertThat(priceResponseVoV3.getTaxes(), hasSize(1));

		final TaxResponseVoV3 taxResponseVoV3 = priceResponseVoV3.getTaxes().iterator().next();
		assertThat(taxResponseVoV3.getTaxId(), is(equalTo("TAX_ID-1")));
		assertThat(taxResponseVoV3.getType(), is(equalTo("%")));
		assertThat(taxResponseVoV3.getValue(), is(equalTo(new BigDecimal("3.15"))));
		assertThat(taxResponseVoV3.getTaxBaseInclusionIds(), is(nullValue()));
		assertThat(taxResponseVoV3.isHidden(), is(false));
		assertThat(taxResponseVoV3.getBase(), is(nullValue()));
		assertThat(taxResponseVoV3.getConditions(), is(nullValue()));
		assertThat(taxResponseVoV3.getProportional(), is(nullValue()));

		assertThat(priceResponseVoV3.getCharges(), is(nullValue()));
	}

	@Test
	void shouldTryConvertPriceEntityV2WhenHasNoOptionalFieldsIntoTaxesConditionsProcessCorrectly() {

		final TaxV2 tax = buildDefaultTaxV2WithoutOptionalFields("TAX_ID-1");
		tax.setConditions(buildDefaultTaxConditionV2WithoutOptionalFields());
		final PriceEntityV2 price1 = buildDefaultPriceEntityV2WithoutOptionalFields();
		price1.setTaxes(Maps.of("TAX_ID-1", tax));
		final PriceNormalizedInfo priceNormalizedInfo = buildPriceNormalizedInfo(price1);

		final PriceResponseVoV3 priceResponseVoV3 = priceNormalizedInfoToPriceResponseVoV3Converter.convert(priceNormalizedInfo);

		assertRequiredFieldsOfPrice(priceResponseVoV3);
		assertPriceNormalizedFields(priceResponseVoV3);

		assertThat(priceResponseVoV3.getValidFrom(), is(nullValue()));
		assertThat(priceResponseVoV3.getDeposit(), is(nullValue()));
		assertThat(priceResponseVoV3.getConsignment(), is(nullValue()));
		assertThat(priceResponseVoV3.getQuantityPerPallet(), is(nullValue()));
		assertThat(priceResponseVoV3.getPromotionalPrice(), is(nullValue()));

		assertThat(priceResponseVoV3.getTaxes(), is(not(nullValue())));
		assertThat(priceResponseVoV3.getTaxes(), hasSize(1));

		final TaxResponseVoV3 taxResponseVoV3 = priceResponseVoV3.getTaxes().iterator().next();
		assertThat(taxResponseVoV3.getTaxId(), is(equalTo("TAX_ID-1")));
		assertThat(taxResponseVoV3.getType(), is(equalTo("%")));
		assertThat(taxResponseVoV3.getValue(), is(equalTo(new BigDecimal("3.15"))));
		assertThat(taxResponseVoV3.getTaxBaseInclusionIds(), is(nullValue()));
		assertThat(taxResponseVoV3.isHidden(), is(false));
		assertThat(taxResponseVoV3.getBase(), is(nullValue()));
		assertThat(taxResponseVoV3.getConditions(), is(not(nullValue())));
		assertThat(taxResponseVoV3.getConditions().getOrderSubTotal(), is(nullValue()));
		assertThat(taxResponseVoV3.getConditions().getTaxAmount(), is(nullValue()));
		assertThat(taxResponseVoV3.getProportional(), is(nullValue()));

		assertThat(priceResponseVoV3.getCharges(), is(nullValue()));
	}

	@Test
	void shouldTryConvertPriceEntityV2WhenHasTaxesConditionsWithTaxAmountFields() {
		final TaxV2 expectedTax = buildDefaultTaxV2WithoutOptionalFields("TAX_ID-1");
		expectedTax.setConditions(buildDefaultTaxConditionV2WithoutOptionalFields());
		expectedTax.getConditions().setTaxAmount(buildTaxAmountCondition());
		final PriceEntityV2 price1 = buildDefaultPriceEntityV2WithoutOptionalFields();
		price1.setTaxes(Maps.of("TAX_ID-1", expectedTax));
		final PriceNormalizedInfo priceNormalizedInfo = buildPriceNormalizedInfo(price1);

		final PriceResponseVoV3 priceResponseVoV3 = priceNormalizedInfoToPriceResponseVoV3Converter.convert(priceNormalizedInfo);

		assertRequiredFieldsOfPrice(priceResponseVoV3);
		assertPriceNormalizedFields(priceResponseVoV3);

		assertThat(priceResponseVoV3.getTaxes(), is(not(nullValue())));
		assertThat(priceResponseVoV3.getTaxes(), hasSize(1));

		final TaxResponseVoV3 taxResponseVoV3 = priceResponseVoV3.getTaxes().iterator().next();
		assertThat(taxResponseVoV3.getTaxId(), is(equalTo("TAX_ID-1")));
		assertThat(taxResponseVoV3.getType(), is(equalTo("%")));
		assertThat(taxResponseVoV3.getValue(), is(equalTo(new BigDecimal("3.15"))));
		assertThat(taxResponseVoV3.getTaxBaseInclusionIds(), is(nullValue()));
		assertThat(taxResponseVoV3.isHidden(), is(false));
		assertThat(taxResponseVoV3.getBase(), is(nullValue()));
		assertThat(taxResponseVoV3.getConditions(), is(not(nullValue())));
		assertThat(taxResponseVoV3.getConditions().getOrderSubTotal(), is(nullValue()));
		assertThat(taxResponseVoV3.getConditions().getTaxAmount(), is(not(nullValue())));
		assertTaxAmountCondition(taxResponseVoV3.getConditions().getTaxAmount(), expectedTax.getConditions().getTaxAmount());
	}

	@Test
	void shouldTryConvertPriceEntityV2WhenHasNoOptionalFieldsIntoChargesProcessCorrectly() {

		final ChargeV2 charge = buildDefaultChargeV2WithoutOptionalFields("CHARGE_ID-1");
		final PriceEntityV2 price1 = buildDefaultPriceEntityV2WithoutOptionalFields();
		price1.setCharges(Maps.of("CHARGE_ID-1", charge));
		final PriceNormalizedInfo priceNormalizedInfo = buildPriceNormalizedInfo(price1);

		final PriceResponseVoV3 priceResponseVoV3 = priceNormalizedInfoToPriceResponseVoV3Converter.convert(priceNormalizedInfo);

		assertPriceNormalizedFields(priceResponseVoV3);

		assertThat(priceResponseVoV3.getValidFrom(), is(nullValue()));
		assertThat(priceResponseVoV3.getDeposit(), is(nullValue()));
		assertThat(priceResponseVoV3.getConsignment(), is(nullValue()));
		assertThat(priceResponseVoV3.getQuantityPerPallet(), is(nullValue()));
		assertThat(priceResponseVoV3.getPromotionalPrice(), is(nullValue()));
		assertThat(priceResponseVoV3.getTaxes(), is(nullValue()));

		assertThat(priceResponseVoV3.getCharges(), is(not(nullValue())));
		assertThat(priceResponseVoV3.getCharges(), hasSize(1));

		final ChargeResponseVoV3 chargeResponseVoV3 = priceResponseVoV3.getCharges().iterator().next();

		assertThat(chargeResponseVoV3.getChargeId(), is(equalTo("CHARGE_ID-1")));
		assertThat(chargeResponseVoV3.getType(), is(equalTo("$")));
		assertThat(chargeResponseVoV3.getValue(), is(equalTo(new BigDecimal("2.01"))));
		assertThat(chargeResponseVoV3.getBase(), is(nullValue()));
	}

	@Test
	void shouldTryConvertPriceEntityV2WhenOptionalFieldsFillThePriceVoV2Correctly() {

		final PromotionalPriceV2 promotionalPrice = buildDefaultPromotionalPriceV2WithOptionalFields("PP_ID-1");
		final TaxV2 tax = buildDefaultTaxV2WithOptionalFields("TAX_ID-1");
		final ChargeV2 charge = buildDefaultChargeV2WithOptionalFields("CHARGE_ID-1");
		final PriceEntityV2 price1 = buildDefaultPriceEntityV2WithOptionalFields(promotionalPrice, Maps.of("TAX_ID-1", tax),
				Maps.of("CHARGE_ID-1", charge));
		final PriceNormalizedInfo priceNormalizedInfo = buildPriceNormalizedInfo(price1);

		final PriceResponseVoV3 priceResponseVoV3 = priceNormalizedInfoToPriceResponseVoV3Converter.convert(priceNormalizedInfo);

		assertPriceNormalizedFields(priceResponseVoV3);

		assertThat(priceResponseVoV3.getValidFrom(), is(equalTo("2030-04-01")));
		assertThat(priceResponseVoV3.getDeposit(), is(equalTo(new BigDecimal("1.80"))));
		assertThat(priceResponseVoV3.getConsignment(), is(equalTo(new BigDecimal("2.00"))));
		assertThat(priceResponseVoV3.getQuantityPerPallet(), is(equalTo(4)));

		assertThat(priceResponseVoV3.getPromotionalPrice(), is(not(nullValue())));
		assertThat(priceResponseVoV3.getTaxes(), is(not(nullValue())));
		assertThat(priceResponseVoV3.getCharges(), is(not(nullValue())));
	}

	@Test
	void shouldTryConvertPriceEntityV2WhenOptionalFieldsFillTheInternalObjectsCorrectly() {

		final PromotionalPriceV2 promotionalPrice = buildDefaultPromotionalPriceV2WithOptionalFields("PP_ID-1");
		final TaxV2 tax = buildDefaultTaxV2WithOptionalFields("TAX_ID-1");
		final ChargeV2 charge = buildDefaultChargeV2WithOptionalFields("CHARGE_ID-1");
		final PriceEntityV2 price1 = buildDefaultPriceEntityV2WithOptionalFields(promotionalPrice, Maps.of("TAX_ID-1", tax),
				Maps.of("CHARGE_ID-1", charge));
		final PriceNormalizedInfo priceNormalizedInfo = buildPriceNormalizedInfo(price1);

		final PriceResponseVoV3 priceResponseVoV3 = priceNormalizedInfoToPriceResponseVoV3Converter.convert(priceNormalizedInfo);

		assertPriceNormalizedFields(priceResponseVoV3);

		assertThat(priceResponseVoV3.getPromotionalPrice(), is(not(nullValue())));
		assertThat(priceResponseVoV3.getPromotionalPrice().getPrice(), is(equalTo(new BigDecimal("9.40"))));
		assertThat(priceResponseVoV3.getPromotionalPrice().getExternalId(), is(equalTo("PP_ID-1")));
		assertThat(priceResponseVoV3.getPromotionalPrice().getValidUntil(), is(equalTo("2021-12-31")));

		final TaxResponseVoV3 taxResponseVoV3 = priceResponseVoV3.getTaxes().iterator().next();
		assertThat(taxResponseVoV3.getTaxId(), is(equalTo("TAX_ID-1")));
		assertThat(taxResponseVoV3.getType(), is(equalTo("$")));
		assertThat(taxResponseVoV3.getValue(), is(equalTo(new BigDecimal("3.15"))));
		assertThat(taxResponseVoV3.getTaxBaseInclusionIds(), is(equalTo(Arrays.asList("TAX_ID-2", "TAX_ID-3"))));
		assertThat(taxResponseVoV3.isHidden(), is(false));
		assertThat(taxResponseVoV3.getBase(), is(equalTo(new BigDecimal("1.70"))));
		assertThat(taxResponseVoV3.getConditions(), is(not(nullValue())));
		assertThat(taxResponseVoV3.getConditions().getOrderSubTotal(), is(not(nullValue())));
		assertThat(taxResponseVoV3.getConditions().getOrderSubTotal().getMinimumValue(), is(equalTo(new BigDecimal("1.05"))));
		assertThat(taxResponseVoV3.getProportional(), is(equalTo(true)));

		assertThat(priceResponseVoV3.getCharges(), is(not(nullValue())));
		assertThat(priceResponseVoV3.getCharges(), hasSize(1));

		final ChargeResponseVoV3 chargeResponseVoV3 = priceResponseVoV3.getCharges().iterator().next();

		assertThat(chargeResponseVoV3.getChargeId(), is(equalTo("CHARGE_ID-1")));
		assertThat(chargeResponseVoV3.getType(), is(equalTo("$")));
		assertThat(chargeResponseVoV3.getValue(), is(equalTo(new BigDecimal("2.01"))));
		assertThat(chargeResponseVoV3.getBase(), is(equalTo(new BigDecimal("2.40"))));
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

		final PriceNormalizedInfo priceNormalizedInfo1 = buildPriceNormalizedInfo(price1);
		final PriceResponseVoV3 priceResponseVoV3Price1 = priceNormalizedInfoToPriceResponseVoV3Converter.convert(priceNormalizedInfo1);

		assertPriceNormalizedFields(priceResponseVoV3Price1);

		assertThat(priceResponseVoV3Price1.getPromotionalPrice(), is(not(nullValue())));
		assertThat(priceResponseVoV3Price1.getPromotionalPrice().getExternalId(), is(equalTo("PP_ID-1")));

		assertThat(priceResponseVoV3Price1.getTaxes(), is(not(nullValue())));
		assertThat(priceResponseVoV3Price1.getTaxes(), hasSize(2));
		final List<String> taxesIds1 = priceResponseVoV3Price1.getTaxes().stream().map(TaxResponseVoV3::getTaxId)
				.collect(Collectors.toList());
		assertThat(taxesIds1, hasItems("TAX_ID-1_1", "TAX_ID-1_2"));

		assertThat(priceResponseVoV3Price1.getCharges(), is(not(nullValue())));
		assertThat(priceResponseVoV3Price1.getCharges(), hasSize(2));
		final List<String> chargeIds1 = priceResponseVoV3Price1.getCharges().stream().map(ChargeResponseVoV3::getChargeId)
				.collect(Collectors.toList());
		assertThat(chargeIds1, hasItems("CHARGE_ID-1_1", "CHARGE_ID-1_2"));

		final PriceNormalizedInfo priceNormalizedInfo2 = buildPriceNormalizedInfo(price2);
		final PriceResponseVoV3 priceResponseVoV3Price2 = priceNormalizedInfoToPriceResponseVoV3Converter.convert(priceNormalizedInfo2);

		assertPriceNormalizedFields(priceResponseVoV3Price2);

		assertThat(priceResponseVoV3Price2.getPromotionalPrice(), is(not(nullValue())));
		assertThat(priceResponseVoV3Price2.getPromotionalPrice().getExternalId(), is(equalTo("PP_ID-2")));

		assertThat(priceResponseVoV3Price2.getTaxes(), is(not(nullValue())));
		assertThat(priceResponseVoV3Price2.getTaxes(), hasSize(1));
		assertThat(priceResponseVoV3Price2.getTaxes().iterator().next().getTaxId(), is(equalTo("TAX_ID-2")));

		assertThat(priceResponseVoV3Price2.getCharges(), is(not(nullValue())));
		assertThat(priceResponseVoV3Price2.getCharges(), hasSize(1));
		assertThat(priceResponseVoV3Price2.getCharges().iterator().next().getChargeId(), is(equalTo("CHARGE_ID-2")));
	}

	@Test
	void shouldTryConvertPriceEntityV2WhenTypeFieldIsFilled() {

		final PriceEntityV2 price1 = buildDefaultPriceEntityV2WithoutOptionalFields();
		price1.setType(PriceMeasureUnitType.PER_UOM);
		final PriceNormalizedInfo priceNormalizedInfo = buildPriceNormalizedInfo(price1);

		final PriceResponseVoV3 priceResponseVoV2 = priceNormalizedInfoToPriceResponseVoV3Converter.convert(priceNormalizedInfo);

		assertThat(priceResponseVoV2.getType(), is(equalTo("PER_UOM")));
	}

	@Test
	void shouldReturnNullWhenHasNoSelectedPrice() {

		final PriceNormalizedInfo priceNormalizedInfo = buildPriceNormalizedInfo(null);

		final PriceResponseVoV3 priceResponseVoV3 = priceNormalizedInfoToPriceResponseVoV3Converter.convert(priceNormalizedInfo);

		assertThat(priceResponseVoV3, is(nullValue()));
	}

	private TaxV2 buildTaxV2(final String taxId) {

		final TaxV2 taxV2 = new TaxV2();
		taxV2.setTaxId(taxId);
		taxV2.setValue(BigDecimal.valueOf(1));
		taxV2.setType("#");

		return taxV2;
	}

	@Test
	void shouldConvertPriceEntityV2WhenHasDeductionAndBaseChangesVariation() {

		final PriceEntityV2 price = buildDefaultPriceEntityV2WithoutOptionalFields();

		final TaxV2 tax1 = buildTaxV2("TAX_WITH_BASE_CHANGES");
		final TaxV2 tax2 = buildTaxV2("TAX_WITHOUT_BASE_CHANGES");
		final TaxV2 tax3 = buildTaxV2("TAX_WITH_DEDUCTION_WITHOUT_BASE_CHANGES");
		final TaxV2 tax4 = buildTaxV2("TAX_WITH_DEDUCTION_WITH_BASE_CHANGES");
		final TaxV2 tax5 = buildTaxV2("TAX_WITH_BASE_CHANGES_AND_COMPARE_WITH");

		final TaxBaseChangesV2 taxBaseChangesV2 = new TaxBaseChangesV2();
		taxBaseChangesV2.setIncrease(BigDecimal.valueOf(1));
		taxBaseChangesV2.setReduction(BigDecimal.valueOf(2));
		tax1.setBaseChanges(taxBaseChangesV2);

		final TaxBaseChangesV2 taxBaseChangesV2WithCompareWith = new TaxBaseChangesV2();
		taxBaseChangesV2WithCompareWith.setIncrease(BigDecimal.valueOf(1));
		taxBaseChangesV2WithCompareWith.setReduction(BigDecimal.valueOf(2));

		final TaxBaseChangesCompareWithV2 taxBaseChangesCompareWithV2 = new TaxBaseChangesCompareWithV2();
		taxBaseChangesCompareWithV2.setType(TaxBaseChangesType.AMOUNT);
		taxBaseChangesCompareWithV2.setBase(BigDecimal.valueOf(6));
		taxBaseChangesV2WithCompareWith.setCompareWith(taxBaseChangesCompareWithV2);
		tax5.setBaseChanges(taxBaseChangesV2WithCompareWith);

		final TaxDeductionV2 deductionV2 = new TaxDeductionV2();
		deductionV2.setValue(BigDecimal.valueOf(3));
		deductionV2.setType("$");
		tax3.setDeduction(deductionV2);

		final TaxDeductionV2 deductionV2WithBaseChanges = new TaxDeductionV2();
		deductionV2WithBaseChanges.setValue(BigDecimal.valueOf(3));
		deductionV2WithBaseChanges.setType("$");
		final TaxDeductionBaseChangesV2 baseChanges = new TaxDeductionBaseChangesV2();
		baseChanges.setReduction(BigDecimal.valueOf(4));
		deductionV2WithBaseChanges.setBaseChanges(baseChanges);
		tax4.setDeduction(deductionV2WithBaseChanges);

		price.setTaxes(
				Map.of("TAX_WITH_BASE_CHANGES", tax1, "TAX_WITHOUT_BASE_CHANGES", tax2, "TAX_WITH_DEDUCTION_WITHOUT_BASE_CHANGES", tax3,
						"TAX_WITH_DEDUCTION_WITH_BASE_CHANGES", tax4, "TAX_WITH_BASE_CHANGES_AND_COMPARE_WITH", tax5));

		final PriceNormalizedInfo priceNormalizedInfo = buildPriceNormalizedInfo(price);
		final PriceResponseVoV3 priceResponseVoV3 = priceNormalizedInfoToPriceResponseVoV3Converter.convert(priceNormalizedInfo);

		assertThat(priceResponseVoV3.getItemId(), is(equalTo(ITEM_ID)));

		assertRequiredFieldsOfPrice(priceResponseVoV3);

		final List<TaxResponseVoV3> taxes = priceResponseVoV3.getTaxes();

		assertThat(taxes, is(notNullValue()));

		final TaxResponseVoV3 taxWithBaseChanges = getTaxResponseVoV3ByTaxId(taxes, "TAX_WITH_BASE_CHANGES");
		final TaxResponseVoV3 taxWithoutBaseChanges = getTaxResponseVoV3ByTaxId(taxes, "TAX_WITHOUT_BASE_CHANGES");
		final TaxResponseVoV3 taxWithDeductionWithoutBaseChanges = getTaxResponseVoV3ByTaxId(taxes,
				"TAX_WITH_DEDUCTION_WITHOUT_BASE_CHANGES");
		final TaxResponseVoV3 taxWithDeductionWithBaseChanges = getTaxResponseVoV3ByTaxId(taxes, "TAX_WITH_DEDUCTION_WITH_BASE_CHANGES");
		final TaxResponseVoV3 taxWithBaseChangesAndCompareWith = getTaxResponseVoV3ByTaxId(taxes, "TAX_WITH_BASE_CHANGES_AND_COMPARE_WITH");

		assertThat(taxWithBaseChanges, is(notNullValue()));
		assertThat(taxWithBaseChanges.getBaseChanges().getIncrease(), is(equalTo(BigDecimal.valueOf(1))));
		assertThat(taxWithBaseChanges.getBaseChanges().getReduction(), is(equalTo(BigDecimal.valueOf(2))));

		assertThat(taxWithoutBaseChanges.getBaseChanges(), is(nullValue()));

		assertThat(taxWithDeductionWithoutBaseChanges.getDeduction(), is(notNullValue()));
		assertThat(taxWithDeductionWithoutBaseChanges.getDeduction().getType(), is(equalTo("$")));
		assertThat(taxWithDeductionWithoutBaseChanges.getDeduction().getValue(), is(equalTo(BigDecimal.valueOf(3))));
		assertThat(taxWithDeductionWithoutBaseChanges.getDeduction().getBaseChanges(), is(nullValue()));

		assertThat(taxWithDeductionWithBaseChanges.getDeduction(), is(notNullValue()));
		assertThat(taxWithDeductionWithBaseChanges.getDeduction().getBaseChanges().getReduction(), is(equalTo(BigDecimal.valueOf(4))));

		assertThat(taxWithBaseChangesAndCompareWith, is(notNullValue()));
		assertThat(taxWithBaseChangesAndCompareWith.getBaseChanges().getIncrease(), is(equalTo(BigDecimal.valueOf(1))));
		assertThat(taxWithBaseChangesAndCompareWith.getBaseChanges().getReduction(), is(equalTo(BigDecimal.valueOf(2))));
		assertThat(taxWithBaseChangesAndCompareWith.getBaseChanges().getCompareWith(), is(notNullValue()));
		assertThat(taxWithBaseChangesAndCompareWith.getBaseChanges().getCompareWith().getBase(), is(equalTo(BigDecimal.valueOf(6))));
		assertThat(taxWithBaseChangesAndCompareWith.getBaseChanges().getCompareWith().getType(), is(equalTo("AMOUNT")));

	}

	private TaxResponseVoV3 getTaxResponseVoV3ByTaxId(final List<TaxResponseVoV3> taxResponseVoV3s, final String taxId) {

		final Optional<TaxResponseVoV3> taxResponseVoV3 = taxResponseVoV3s.stream().filter(taxV2 -> taxV2.getTaxId().equals(taxId))
				.findFirst();

		return taxResponseVoV3.orElse(null);
	}

	private void assertRequiredFieldsOfPrice(final PriceResponseVoV3 priceResponseVoV3) {

		assertThat(priceResponseVoV3.getVendorItemId(), is(equalTo("VENDOR_ITEM_ID-1")));
		assertThat(priceResponseVoV3.getSku(), is(equalTo("SKU-1")));
		assertThat(priceResponseVoV3.getBasePrice(), is(equalTo(new BigDecimal("10.00"))));
		assertThat(priceResponseVoV3.getMinimumPrice(), is(equalTo(new BigDecimal("8.40"))));
		assertThat(priceResponseVoV3.getMeasureUnit(), is(equalTo("CS")));
		assertThat(priceResponseVoV3.getTimezone(), is(equalTo("America/Sao_Paulo")));
		assertThat(priceResponseVoV3.getType(), is(equalTo("PER_UNIT")));

	}

	private void assertPriceNormalizedFields(final PriceResponseVoV3 priceResponseVoV3) {

		assertThat(priceResponseVoV3.getItemId(), is(equalTo(ITEM_ID)));
		assertThat(priceResponseVoV3.getContractId(), is(equalTo(CONTRACT_ID)));
		assertThat(priceResponseVoV3.getDeliveryCenterId(), is(nullValue()));

	}

	private void assertTaxAmountCondition(TaxAmountResponseVoV3 taxAmountResult, TaxAmountV2 expectedTaxAmount) {
		assertThat(taxAmountResult.getTaxId(), is(equalTo(expectedTaxAmount.getTaxId())));
		assertThat(taxAmountResult.getThreshold(), is(equalTo(expectedTaxAmount.getThreshold())));
		assertThat(taxAmountResult.getScope(), is(equalTo(expectedTaxAmount.getScope())));
		assertThat(taxAmountResult.getOperator(), is(equalTo(expectedTaxAmount.getOperator())));

	}

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

		priceEntityV2.setSku("SKU-1");
		priceEntityV2.setBasePrice(new BigDecimal("10.00"));
		priceEntityV2.setMeasureUnit("CS");
		priceEntityV2.setMinimumPrice(new BigDecimal("8.40"));

		priceEntityV2.setId(id);
		priceEntityV2.setDeleted(false);
		priceEntityV2.setCountry("BR");
		priceEntityV2.setTimestamp(1617036174L);
		priceEntityV2.setTimezone("America/Sao_Paulo");
		priceEntityV2.setCreatedDate(Date.from(Instant.ofEpochSecond(1617036177L)));
		priceEntityV2.setUpdatedDate(Date.from(Instant.ofEpochSecond(1617036177L)));
		return priceEntityV2;
	}

	private PromotionalPriceV2 buildDefaultPromotionalPriceV2WithoutOptionalFields(final String externalId) {

		final PromotionalPriceV2 promotionalPrice = new PromotionalPriceV2();
		promotionalPrice.setPrice(new BigDecimal("9.40"));
		promotionalPrice.setExternalId(externalId);
		return promotionalPrice;
	}

	private TaxV2 buildDefaultTaxV2WithoutOptionalFields(final String taxId) {

		final TaxV2 taxV2 = new TaxV2();
		taxV2.setTaxId(taxId);
		taxV2.setType("%");
		taxV2.setValue(new BigDecimal("3.15"));
		taxV2.setHidden(false);
		taxV2.setProportional(null);
		return taxV2;
	}

	private TaxConditionV2 buildDefaultTaxConditionV2WithoutOptionalFields() {

		return new TaxConditionV2();
	}

	private ChargeV2 buildDefaultChargeV2WithoutOptionalFields(final String chargeId) {

		final ChargeV2 chargeV2 = new ChargeV2();
		chargeV2.setChargeId(chargeId);
		chargeV2.setType("$");
		chargeV2.setValue(new BigDecimal("2.01"));
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
		taxV2.setType("$");
		taxV2.setProportional(true);
		return taxV2;
	}

	private TaxAmountV2 buildTaxAmountCondition() {

		final TaxAmountV2 taxAmountV2 = new TaxAmountV2();
		taxAmountV2.setTaxId("TAX_ID-1");
		taxAmountV2.setThreshold(new BigDecimal("1000.00"));
		taxAmountV2.setScope("ORDER");
		taxAmountV2.setOperator("GE");
		return taxAmountV2;

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

	private PriceNormalizedInfo buildPriceNormalizedInfo(final PriceEntityV2 priceEntityV2) {

		final PriceNormalizedInfo priceNormalizedInfo = new PriceNormalizedInfo();
		priceNormalizedInfo.setContractId(CONTRACT_ID);
		priceNormalizedInfo.setDeliveryCenterId(DELIVERY_CENTER_ID);
		priceNormalizedInfo.setItemId(ITEM_ID);
		priceNormalizedInfo.setSelectedPrice(priceEntityV2);

		return priceNormalizedInfo;
	}

}