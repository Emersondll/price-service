package com.abinbev.b2b.price.api.services.browseprice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abinbev.b2b.price.api.domain.TaxConditionFact;
import com.abinbev.b2b.price.api.domain.TaxFact;
import com.abinbev.b2b.price.api.domain.TaxOrderSubTotalFact;
import com.abinbev.b2b.price.api.domain.bre.WhenApplyRoundingOnTaxesEnum;
import com.abinbev.b2b.price.api.helpers.ApiConstants;

@ExtendWith(MockitoExtension.class)
class CalculateTaxServiceTest {

	private static final String COUNTRY = "BR";
	private static final String TAX_ID_01 = "TAX_01";
	private static final String TAX_ID_02 = "TAX_02";
	private static final String TAX_ID_03 = "TAX_03";
	private static final BigDecimal ITEM_BASE_PRICE = BigDecimal.TEN;

	@Mock
	private PricingConfigurationService pricingConfigurationService;

	@Mock
	private RoundValueService roundValueService;

	@InjectMocks
	private CalculateTaxService calculateTaxService;

	@Test
	void shouldReturnZeroWhenChargesAreNull() {

		final BigDecimal actualResponse = calculateTaxService.execute(null, null, null, COUNTRY);

		assertThat(actualResponse, is(notNullValue()));
		assertThat(actualResponse, is(equalTo(BigDecimal.ZERO)));

		verify(pricingConfigurationService, times(0)).getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		verify(roundValueService, times(0)).execute(any());
	}

	@Test
	void shouldReturnZeroWhenChargesAreEmpty() {

		final BigDecimal actualResponse = calculateTaxService.execute(new HashMap<>(), null, null, COUNTRY);

		assertThat(actualResponse, is(notNullValue()));
		assertThat(actualResponse, is(equalTo(BigDecimal.ZERO)));

		verify(pricingConfigurationService, times(0)).getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		verify(roundValueService, times(0)).execute(any());
	}

	@Test
	void shouldReturnZeroWhenTaxesAreNotApplicable() {

		doReturn(Boolean.FALSE).when(pricingConfigurationService)
				.getPriceConfig(COUNTRY, ApiConstants.EXCLUDE_CONDITIONAL_TAXES_FROM_BROWSE_PRICE);
		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.PRICES_IGNORE_TAX_CONDITION);

		final BigDecimal actualResponse = calculateTaxService
				.execute(getMapTaxWithMinimumValueGreaterThanBasePriceById(), BigDecimal.ONE, ITEM_BASE_PRICE, COUNTRY);

		assertThat(actualResponse, is(notNullValue()));
		assertThat(actualResponse, is(equalTo(BigDecimal.ZERO)));

		verify(pricingConfigurationService, times(2)).getPriceConfig(COUNTRY, ApiConstants.PRICES_IGNORE_TAX_CONDITION);
		verify(pricingConfigurationService, times(0)).getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		verify(roundValueService, times(0)).execute(any());
	}

	@Test
	void shouldReturnZeroWhenTaxesAreNotApplicableWithOrderMinimumPriceNull() {

		doReturn(Boolean.FALSE).when(pricingConfigurationService)
				.getPriceConfig(COUNTRY, ApiConstants.EXCLUDE_CONDITIONAL_TAXES_FROM_BROWSE_PRICE);
		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.PRICES_IGNORE_TAX_CONDITION);

		final BigDecimal actualResponse = calculateTaxService
				.execute(getMapTaxByIdWithMinimumPriceNull(), BigDecimal.ONE, ITEM_BASE_PRICE, COUNTRY);

		assertThat(actualResponse, is(notNullValue()));
		assertThat(actualResponse, is(equalTo(BigDecimal.ZERO)));

		verify(pricingConfigurationService, times(2)).getPriceConfig(COUNTRY, ApiConstants.PRICES_IGNORE_TAX_CONDITION);
		verify(pricingConfigurationService, times(0)).getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		verify(roundValueService, times(0)).execute(any());
	}

	@Test
	void shouldReturnZeroWhenTaxesAreNotApplicableWithItemBasePriceNull() {

		doReturn(Boolean.FALSE).when(pricingConfigurationService)
				.getPriceConfig(COUNTRY, ApiConstants.EXCLUDE_CONDITIONAL_TAXES_FROM_BROWSE_PRICE);
		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.PRICES_IGNORE_TAX_CONDITION);

		final BigDecimal actualResponse = calculateTaxService.execute(getMapTaxById(), BigDecimal.ONE, null, COUNTRY);

		assertThat(actualResponse, is(notNullValue()));
		assertThat(actualResponse, is(equalTo(BigDecimal.ZERO)));

		verify(pricingConfigurationService, times(3)).getPriceConfig(COUNTRY, ApiConstants.PRICES_IGNORE_TAX_CONDITION);
		verify(pricingConfigurationService, times(0)).getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		verify(roundValueService, times(0)).execute(any());
	}

	@Test
	void shouldReturnCalculatedTaxAmountRoundedOnEachTaxWhenHasOnEachTaxProperty() {

		final BigDecimal expectedResponse = BigDecimal.valueOf(3.10).setScale(2, RoundingMode.HALF_UP);

		doReturn(Boolean.FALSE).when(pricingConfigurationService)
				.getPriceConfig(COUNTRY, ApiConstants.EXCLUDE_CONDITIONAL_TAXES_FROM_BROWSE_PRICE);
		doReturn(Boolean.TRUE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.PRICES_IGNORE_TAX_CONDITION);
		doReturn(WhenApplyRoundingOnTaxesEnum.ON_EACH_TAX).when(pricingConfigurationService)
				.getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		doCallRealMethod().when(roundValueService).execute(any());

		final BigDecimal actualResponse = calculateTaxService.execute(getMapTaxById(), BigDecimal.ONE, ITEM_BASE_PRICE, COUNTRY);

		assertThat(actualResponse, is(notNullValue()));
		assertThat(actualResponse, is(equalTo(expectedResponse)));

		verify(pricingConfigurationService, times(3)).getPriceConfig(COUNTRY, ApiConstants.PRICES_IGNORE_TAX_CONDITION);
		verify(pricingConfigurationService, times(4)).getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		verify(roundValueService, times(3)).execute(any());
	}

	@Test
	void shouldNotComputeConditionalTaxWhenExcludeConditionalTaxesParamIsTrue() {

		final BigDecimal expectedResponse = BigDecimal.ZERO;

		doReturn(Boolean.TRUE).when(pricingConfigurationService)
				.getPriceConfig(COUNTRY, ApiConstants.EXCLUDE_CONDITIONAL_TAXES_FROM_BROWSE_PRICE);

		final BigDecimal actualResponse = calculateTaxService.execute(getMapTaxById(), BigDecimal.ONE, ITEM_BASE_PRICE, COUNTRY);

		assertThat(actualResponse, is(notNullValue()));
		assertThat(actualResponse, is(equalTo(expectedResponse)));
	}

	@Test
	void shouldComputeJustNotConditionalTaxWhenExcludeConditionalTaxesParamIsTrue() {

		final BigDecimal expectedResponse = BigDecimal.valueOf(1).setScale(2, RoundingMode.HALF_UP);

		doReturn(Boolean.TRUE).when(pricingConfigurationService)
				.getPriceConfig(COUNTRY, ApiConstants.EXCLUDE_CONDITIONAL_TAXES_FROM_BROWSE_PRICE);
		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.PRICES_IGNORE_TAX_CONDITION);
		doReturn(WhenApplyRoundingOnTaxesEnum.ON_EACH_TAX).when(pricingConfigurationService)
				.getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		doCallRealMethod().when(roundValueService).execute(any());

		final Map<String, TaxFact> taxes = getMapTaxById();
		taxes.get(TAX_ID_01).setConditions(null);
		final BigDecimal actualResponse = calculateTaxService.execute(taxes, BigDecimal.ONE, ITEM_BASE_PRICE, COUNTRY);

		assertThat(actualResponse, is(notNullValue()));
		assertThat(actualResponse, is(equalTo(expectedResponse)));

		verify(pricingConfigurationService, times(1)).getPriceConfig(COUNTRY, ApiConstants.PRICES_IGNORE_TAX_CONDITION);
		verify(pricingConfigurationService, times(2)).getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		verify(roundValueService, times(1)).execute(any());
	}

	@Test
	void shouldReturnCalculatedTaxAmountRoundedOnEachTaxWhenContainsTaxDependency() {

		final BigDecimal expectedResponse = BigDecimal.valueOf(3.2).setScale(2, RoundingMode.HALF_UP);

		doReturn(Boolean.FALSE).when(pricingConfigurationService)
				.getPriceConfig(COUNTRY, ApiConstants.EXCLUDE_CONDITIONAL_TAXES_FROM_BROWSE_PRICE);
		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.PRICES_IGNORE_TAX_CONDITION);
		doReturn(WhenApplyRoundingOnTaxesEnum.ON_EACH_TAX).when(pricingConfigurationService)
				.getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		doCallRealMethod().when(roundValueService).execute(any());

		final BigDecimal actualResponse = calculateTaxService
				.execute(getMapTaxWithDependencyById(), BigDecimal.ONE, ITEM_BASE_PRICE, COUNTRY);

		assertThat(actualResponse, is(notNullValue()));
		assertThat(actualResponse, is(equalTo(expectedResponse)));

		verify(pricingConfigurationService, times(2)).getPriceConfig(COUNTRY, ApiConstants.PRICES_IGNORE_TAX_CONDITION);
		verify(pricingConfigurationService, times(4)).getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		verify(roundValueService, times(3)).execute(any());
	}

	@Test
	void shouldReturnCalculatedTaxAmountRoundedOnEachTaxWhenContainsTaxDependencyButPaymentMethodIsMoney() {

		final BigDecimal expectedResponse = BigDecimal.valueOf(20).setScale(2, RoundingMode.HALF_UP);

		doReturn(Boolean.FALSE).when(pricingConfigurationService)
				.getPriceConfig(COUNTRY, ApiConstants.EXCLUDE_CONDITIONAL_TAXES_FROM_BROWSE_PRICE);
		doReturn(Boolean.TRUE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.PRICES_IGNORE_TAX_CONDITION);
		doReturn(WhenApplyRoundingOnTaxesEnum.ON_EACH_TAX).when(pricingConfigurationService)
				.getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		doCallRealMethod().when(roundValueService).execute(any());

		final BigDecimal actualResponse = calculateTaxService
				.execute(getMapTaxWithDependencyAndTypeMoneyById(), BigDecimal.ONE, ITEM_BASE_PRICE, COUNTRY);

		assertThat(actualResponse, is(notNullValue()));
		assertThat(actualResponse, is(equalTo(expectedResponse)));

		verify(pricingConfigurationService, times(2)).getPriceConfig(COUNTRY, ApiConstants.PRICES_IGNORE_TAX_CONDITION);
		verify(pricingConfigurationService, times(4)).getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		verify(roundValueService, times(3)).execute(any());
	}

	@Test
	void shouldReturnCalculatedTaxAmountRoundedWhenHasPropertyInTheEnd() {

		final BigDecimal expectedResponse = BigDecimal.valueOf(3.10).setScale(2, RoundingMode.HALF_UP);

		doReturn(Boolean.FALSE).when(pricingConfigurationService)
				.getPriceConfig(COUNTRY, ApiConstants.EXCLUDE_CONDITIONAL_TAXES_FROM_BROWSE_PRICE);
		doReturn(Boolean.TRUE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.PRICES_IGNORE_TAX_CONDITION);
		doReturn(WhenApplyRoundingOnTaxesEnum.IN_THE_END).when(pricingConfigurationService)
				.getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		doCallRealMethod().when(roundValueService).execute(any());

		final BigDecimal actualResponse = calculateTaxService.execute(getMapTaxById(), BigDecimal.ONE, ITEM_BASE_PRICE, COUNTRY);

		assertThat(actualResponse, is(notNullValue()));
		assertThat(actualResponse, is(equalTo(expectedResponse)));

		verify(pricingConfigurationService, times(3)).getPriceConfig(COUNTRY, ApiConstants.PRICES_IGNORE_TAX_CONDITION);
		verify(pricingConfigurationService, times(4)).getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		verify(roundValueService, times(2)).execute(any());
	}

	@Test
	void shouldReturnCalculatedTaxAmountRoundedInTheEndWhenContainsTaxDependency() {

		final BigDecimal expectedResponse = BigDecimal.valueOf(3.2).setScale(2, RoundingMode.HALF_UP);

		doReturn(Boolean.FALSE).when(pricingConfigurationService)
				.getPriceConfig(COUNTRY, ApiConstants.EXCLUDE_CONDITIONAL_TAXES_FROM_BROWSE_PRICE);
		doReturn(Boolean.TRUE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.PRICES_IGNORE_TAX_CONDITION);
		doReturn(WhenApplyRoundingOnTaxesEnum.IN_THE_END).when(pricingConfigurationService)
				.getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		doCallRealMethod().when(roundValueService).execute(any());

		final BigDecimal actualResponse = calculateTaxService
				.execute(getMapTaxWithDependencyById(), BigDecimal.ONE, ITEM_BASE_PRICE, COUNTRY);

		assertThat(actualResponse, is(notNullValue()));
		assertThat(actualResponse, is(equalTo(expectedResponse)));

		verify(pricingConfigurationService, times(2)).getPriceConfig(COUNTRY, ApiConstants.PRICES_IGNORE_TAX_CONDITION);
		verify(pricingConfigurationService, times(4)).getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		verify(roundValueService, times(2)).execute(any());
	}

	@Test
	void shouldReturnCalculatedTaxAmountRoundedInTheEndWhenContainsTaxDependencyButPaymentMethodIsMoney() {

		final BigDecimal expectedResponse = BigDecimal.valueOf(20).setScale(2, RoundingMode.HALF_UP);

		doReturn(Boolean.FALSE).when(pricingConfigurationService)
				.getPriceConfig(COUNTRY, ApiConstants.EXCLUDE_CONDITIONAL_TAXES_FROM_BROWSE_PRICE);
		doReturn(Boolean.TRUE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.PRICES_IGNORE_TAX_CONDITION);
		doReturn(WhenApplyRoundingOnTaxesEnum.IN_THE_END).when(pricingConfigurationService)
				.getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		doCallRealMethod().when(roundValueService).execute(any());

		final BigDecimal actualResponse = calculateTaxService
				.execute(getMapTaxWithDependencyAndTypeMoneyById(), BigDecimal.ONE, ITEM_BASE_PRICE, COUNTRY);

		assertThat(actualResponse, is(notNullValue()));
		assertThat(actualResponse, is(equalTo(expectedResponse)));

		verify(pricingConfigurationService, times(2)).getPriceConfig(COUNTRY, ApiConstants.PRICES_IGNORE_TAX_CONDITION);
		verify(pricingConfigurationService, times(4)).getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		verify(roundValueService, times(2)).execute(any());
	}

	@Test
	void shouldReturnCalculatedTaxAmountRoundedInTheEndWhenContainsHiddenTax() {

		final BigDecimal expectedResponse = BigDecimal.valueOf(6).setScale(2, RoundingMode.HALF_UP);

		doReturn(Boolean.FALSE).when(pricingConfigurationService)
				.getPriceConfig(COUNTRY, ApiConstants.EXCLUDE_CONDITIONAL_TAXES_FROM_BROWSE_PRICE);
		doReturn(Boolean.TRUE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.PRICES_IGNORE_TAX_CONDITION);
		doReturn(WhenApplyRoundingOnTaxesEnum.IN_THE_END).when(pricingConfigurationService)
				.getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		doCallRealMethod().when(roundValueService).execute(any());

		final BigDecimal actualResponse = calculateTaxService.execute(getMapTaxWithHiddenById(), BigDecimal.ONE, ITEM_BASE_PRICE, COUNTRY);

		assertThat(actualResponse, is(notNullValue()));
		assertThat(actualResponse, is(equalTo(expectedResponse)));

		verify(pricingConfigurationService, times(3)).getPriceConfig(COUNTRY, ApiConstants.PRICES_IGNORE_TAX_CONDITION);
		verify(pricingConfigurationService, times(4)).getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		verify(roundValueService, times(2)).execute(any());
	}

	@Test
	void shouldReturnCalculatedTaxAmountRoundedInTheEndWhenContainsTaxDependencyAndHiddenTax() {

		final BigDecimal expectedResponse = BigDecimal.valueOf(13).setScale(2, RoundingMode.HALF_UP);

		doReturn(Boolean.FALSE).when(pricingConfigurationService)
				.getPriceConfig(COUNTRY, ApiConstants.EXCLUDE_CONDITIONAL_TAXES_FROM_BROWSE_PRICE);
		doReturn(Boolean.TRUE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.PRICES_IGNORE_TAX_CONDITION);
		doReturn(WhenApplyRoundingOnTaxesEnum.IN_THE_END).when(pricingConfigurationService)
				.getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		doCallRealMethod().when(roundValueService).execute(any());

		final BigDecimal actualResponse = calculateTaxService
				.execute(getMapTaxWithHiddenAndTypeMoneyById(), BigDecimal.ONE, ITEM_BASE_PRICE, COUNTRY);

		assertThat(actualResponse, is(notNullValue()));
		assertThat(actualResponse, is(equalTo(expectedResponse)));

		verify(pricingConfigurationService, times(3)).getPriceConfig(COUNTRY, ApiConstants.PRICES_IGNORE_TAX_CONDITION);
		verify(pricingConfigurationService, times(4)).getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		verify(roundValueService, times(2)).execute(any());
	}

	@Test
	void shouldReturnCalculatedTaxAmountRoundedOnEachTaxWhenContainsTaxHiddenButPaymentMethodIsMoney() {

		final BigDecimal expectedResponse = BigDecimal.valueOf(30).setScale(2, RoundingMode.HALF_UP);

		doReturn(Boolean.FALSE).when(pricingConfigurationService)
				.getPriceConfig(COUNTRY, ApiConstants.EXCLUDE_CONDITIONAL_TAXES_FROM_BROWSE_PRICE);
		doReturn(Boolean.TRUE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.PRICES_IGNORE_TAX_CONDITION);
		doReturn(WhenApplyRoundingOnTaxesEnum.ON_EACH_TAX).when(pricingConfigurationService)
				.getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		doCallRealMethod().when(roundValueService).execute(any());

		final BigDecimal actualResponse = calculateTaxService
				.execute(getMapTaxWithHiddenAndAllTaxesTypeMoneyById(), BigDecimal.ONE, ITEM_BASE_PRICE, COUNTRY);

		assertThat(actualResponse, is(notNullValue()));
		assertThat(actualResponse, is(equalTo(expectedResponse)));

		verify(pricingConfigurationService, times(3)).getPriceConfig(COUNTRY, ApiConstants.PRICES_IGNORE_TAX_CONDITION);
		verify(pricingConfigurationService, times(4)).getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		verify(roundValueService, times(3)).execute(any());
	}

	@Test
	void shouldReturnCalculatedTaxAmountRoundedOnEachTaxWhenContainsTaxHiddenButPaymentMethodIsMoneyAndHasTaxDependency() {

		final BigDecimal expectedResponse = BigDecimal.valueOf(30).setScale(2, RoundingMode.HALF_UP);

		doReturn(Boolean.FALSE).when(pricingConfigurationService)
				.getPriceConfig(COUNTRY, ApiConstants.EXCLUDE_CONDITIONAL_TAXES_FROM_BROWSE_PRICE);
		doReturn(Boolean.TRUE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.PRICES_IGNORE_TAX_CONDITION);
		doReturn(WhenApplyRoundingOnTaxesEnum.ON_EACH_TAX).when(pricingConfigurationService)
				.getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		doCallRealMethod().when(roundValueService).execute(any());

		final BigDecimal actualResponse = calculateTaxService
				.execute(getMapTaxWithDependencyAndTypeMoneyAndHiddenTaxWithTypePorcentageById(), BigDecimal.ONE, ITEM_BASE_PRICE, COUNTRY);

		assertThat(actualResponse, is(notNullValue()));
		assertThat(actualResponse, is(equalTo(expectedResponse)));

		verify(pricingConfigurationService, times(3)).getPriceConfig(COUNTRY, ApiConstants.PRICES_IGNORE_TAX_CONDITION);
		verify(pricingConfigurationService, times(5)).getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		verify(roundValueService, times(4)).execute(any());
	}

	@Test
	void shouldReturnCalculatedTaxAmountRoundedInTheEndWhenContainsTaxHiddenButPaymentMethodIsMoneyAndHasTaxDependency() {

		final BigDecimal expectedResponse = BigDecimal.valueOf(30).setScale(2, RoundingMode.HALF_UP);

		doReturn(Boolean.FALSE).when(pricingConfigurationService)
				.getPriceConfig(COUNTRY, ApiConstants.EXCLUDE_CONDITIONAL_TAXES_FROM_BROWSE_PRICE);
		doReturn(Boolean.TRUE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.PRICES_IGNORE_TAX_CONDITION);
		doReturn(WhenApplyRoundingOnTaxesEnum.IN_THE_END).when(pricingConfigurationService)
				.getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		doCallRealMethod().when(roundValueService).execute(any());

		final BigDecimal actualResponse = calculateTaxService
				.execute(getMapTaxWithDependencyAndTypeMoneyAndHiddenTaxWithTypePorcentageById(), BigDecimal.ONE, ITEM_BASE_PRICE, COUNTRY);

		assertThat(actualResponse, is(notNullValue()));
		assertThat(actualResponse, is(equalTo(expectedResponse)));

		verify(pricingConfigurationService, times(3)).getPriceConfig(COUNTRY, ApiConstants.PRICES_IGNORE_TAX_CONDITION);
		verify(pricingConfigurationService, times(5)).getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		verify(roundValueService, times(2)).execute(any());
	}

	private Map<String, TaxFact> getMapTaxWithMinimumValueGreaterThanBasePriceById() {

		final TaxFact tax1 = new TaxFact();
		tax1.setConditions(getTaxConditionWithMinimumValueGreaterThanItemBasePrice());

		final TaxFact tax2 = new TaxFact();
		tax2.setConditions(getTaxConditionWithMinimumValueGreaterThanItemBasePrice());

		final Map<String, TaxFact> mappedTaxById = new HashMap<>();

		mappedTaxById.put(TAX_ID_01, tax1);
		mappedTaxById.put(TAX_ID_02, tax2);

		return mappedTaxById;
	}

	private Map<String, TaxFact> getMapTaxByIdWithMinimumPriceNull() {

		final TaxFact tax1 = new TaxFact();
		tax1.setConditions(getTaxConditionWithMinimumValueNull());

		final TaxFact tax2 = new TaxFact();
		tax2.setConditions(getTaxConditionWithMinimumValueNull());

		final Map<String, TaxFact> mappedTaxById = new HashMap<>();

		mappedTaxById.put(TAX_ID_01, tax1);
		mappedTaxById.put(TAX_ID_02, tax2);

		return mappedTaxById;
	}

	private Map<String, TaxFact> getMapTaxById() {

		final Map<String, TaxFact> mappedTaxById = new HashMap<>();

		mappedTaxById.put(TAX_ID_01, getTaxTypePercentage(TAX_ID_01, BigDecimal.valueOf(10)));
		mappedTaxById.put(TAX_ID_02, getTaxTypePercentage(TAX_ID_02, BigDecimal.valueOf(20)));
		mappedTaxById.put(TAX_ID_03, getTaxTypePercentageWithoutBase(TAX_ID_03, BigDecimal.valueOf(30)));

		return mappedTaxById;
	}

	private Map<String, TaxFact> getMapTaxWithDependencyById() {

		final Map<String, TaxFact> mappedTaxById = new HashMap<>();

		mappedTaxById.put(TAX_ID_01, getTaxTypePercentageWithDependency(BigDecimal.valueOf(10)));
		mappedTaxById.put(TAX_ID_02, getTaxTypePercentage(TAX_ID_02, BigDecimal.valueOf(20)));

		return mappedTaxById;
	}

	private Map<String, TaxFact> getMapTaxWithDependencyAndTypeMoneyById() {

		final Map<String, TaxFact> mappedTaxById = new HashMap<>();

		mappedTaxById.put(TAX_ID_01, getTaxTypeMoneyWithDependency(BigDecimal.valueOf(10)));
		mappedTaxById.put(TAX_ID_02, getTaxTypeMoney(TAX_ID_02, BigDecimal.valueOf(20)));

		return mappedTaxById;
	}

	private Map<String, TaxFact> getMapTaxWithHiddenById() {

		final Map<String, TaxFact> mappedTaxById = new HashMap<>();

		mappedTaxById.put(TAX_ID_01, getTaxTypePercentage(TAX_ID_01, BigDecimal.valueOf(10)));
		mappedTaxById.put(TAX_ID_02, getTaxTypePercentage(TAX_ID_02, BigDecimal.valueOf(20)));
		mappedTaxById.put(TAX_ID_03, getTaxTypePercentageWithHiddenTax(BigDecimal.valueOf(30)));

		return mappedTaxById;
	}

	private Map<String, TaxFact> getMapTaxWithHiddenAndTypeMoneyById() {

		final Map<String, TaxFact> mappedTaxById = new HashMap<>();

		mappedTaxById.put(TAX_ID_01, getTaxTypePercentage(TAX_ID_01, BigDecimal.valueOf(10)));
		mappedTaxById.put(TAX_ID_02, getTaxTypePercentage(TAX_ID_02, BigDecimal.valueOf(20)));
		mappedTaxById.put(TAX_ID_03, getTaxTypeMoneyWithHiddenTax(BigDecimal.valueOf(30)));

		return mappedTaxById;
	}

	private Map<String, TaxFact> getMapTaxWithHiddenAndAllTaxesTypeMoneyById() {

		final Map<String, TaxFact> mappedTaxById = new HashMap<>();

		mappedTaxById.put(TAX_ID_01, getTaxTypeMoney(TAX_ID_01, BigDecimal.valueOf(10)));
		mappedTaxById.put(TAX_ID_02, getTaxTypeMoney(TAX_ID_02, BigDecimal.valueOf(20)));
		mappedTaxById.put(TAX_ID_03, getTaxTypeMoneyWithHiddenTax(BigDecimal.valueOf(30)));

		return mappedTaxById;
	}

	private Map<String, TaxFact> getMapTaxWithDependencyAndTypeMoneyAndHiddenTaxWithTypePorcentageById() {

		final Map<String, TaxFact> mappedTaxById = new HashMap<>();

		mappedTaxById.put(TAX_ID_01, getTaxTypeMoneyWithDependency(BigDecimal.valueOf(10)));
		mappedTaxById.put(TAX_ID_02, getTaxTypeMoney(TAX_ID_02, BigDecimal.valueOf(20)));
		mappedTaxById.put(TAX_ID_03, getTaxTypeMoneyWithHiddenTax(BigDecimal.valueOf(30)));

		return mappedTaxById;
	}

	private TaxFact getTaxTypePercentage(final String taxId, final BigDecimal baseValue) {

		final TaxFact tax = new TaxFact();

		tax.setTaxId(taxId);
		tax.setType("%");
		tax.setBase(baseValue);
		tax.setValue(BigDecimal.TEN);
		tax.setConditions(getDefaultTaxCondition());

		return tax;
	}

	private TaxFact getTaxTypePercentageWithoutBase(final String taxId, final BigDecimal baseValue) {

		final TaxFact tax = new TaxFact();

		tax.setTaxId(taxId);
		tax.setType("%");
		tax.setBase(null);
		tax.setValue(BigDecimal.TEN);
		tax.setConditions(getDefaultTaxCondition());

		return tax;
	}

	private TaxFact getTaxTypeMoney(final String taxId, final BigDecimal baseValue) {

		final TaxFact tax = new TaxFact();

		tax.setTaxId(taxId);
		tax.setType("$");
		tax.setBase(baseValue);
		tax.setValue(BigDecimal.TEN);
		tax.setConditions(getDefaultTaxCondition());

		return tax;
	}

	private TaxFact getTaxTypePercentageWithDependency(final BigDecimal baseValue) {

		final TaxFact tax = new TaxFact();

		tax.setTaxId(TAX_ID_01);
		tax.setType("%");
		tax.setBase(baseValue);
		tax.setValue(BigDecimal.TEN);
		tax.setConditions(getDefaultTaxCondition());
		tax.setTaxBaseInclusionIds(Collections.singletonList(TAX_ID_02));

		return tax;
	}

	private TaxFact getTaxTypeMoneyWithDependency(final BigDecimal baseValue) {

		final TaxFact tax = new TaxFact();

		tax.setTaxId(TAX_ID_01);
		tax.setType("$");
		tax.setBase(baseValue);
		tax.setValue(BigDecimal.TEN);
		tax.setConditions(getDefaultTaxCondition());
		tax.setTaxBaseInclusionIds(Collections.singletonList(TAX_ID_02));

		return tax;
	}

	private TaxFact getTaxTypePercentageWithHiddenTax(final BigDecimal baseValue) {

		final TaxFact tax = new TaxFact();

		tax.setTaxId(TAX_ID_03);
		tax.setType("%");
		tax.setBase(baseValue);
		tax.setValue(BigDecimal.TEN);
		tax.setHidden(Boolean.TRUE);
		tax.setConditions(getDefaultTaxCondition());

		return tax;
	}

	private TaxFact getTaxTypeMoneyWithHiddenTax(final BigDecimal baseValue) {

		final TaxFact tax = new TaxFact();

		tax.setTaxId(TAX_ID_03);
		tax.setType("$");
		tax.setBase(baseValue);
		tax.setValue(BigDecimal.TEN);
		tax.setHidden(Boolean.TRUE);
		tax.setConditions(getDefaultTaxCondition());

		return tax;
	}

	private TaxConditionFact getDefaultTaxCondition() {

		final TaxOrderSubTotalFact taxOrderSubTotal = new TaxOrderSubTotalFact();
		taxOrderSubTotal.setMinimumValue(BigDecimal.valueOf(1));

		final TaxConditionFact taxCondition = new TaxConditionFact();
		taxCondition.setOrderSubTotal(taxOrderSubTotal);

		return taxCondition;
	}

	private TaxConditionFact getTaxConditionWithMinimumValueGreaterThanItemBasePrice() {

		final TaxOrderSubTotalFact taxOrderSubTotal = new TaxOrderSubTotalFact();
		taxOrderSubTotal.setMinimumValue(BigDecimal.valueOf(100));

		final TaxConditionFact taxCondition = new TaxConditionFact();
		taxCondition.setOrderSubTotal(taxOrderSubTotal);

		return taxCondition;
	}

	private TaxConditionFact getTaxConditionWithMinimumValueNull() {

		final TaxOrderSubTotalFact taxOrderSubTotal = new TaxOrderSubTotalFact();

		final TaxConditionFact taxCondition = new TaxConditionFact();
		taxCondition.setOrderSubTotal(taxOrderSubTotal);

		return taxCondition;
	}

}
