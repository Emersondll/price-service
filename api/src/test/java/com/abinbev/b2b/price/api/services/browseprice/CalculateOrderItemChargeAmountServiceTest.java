package com.abinbev.b2b.price.api.services.browseprice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abinbev.b2b.price.api.domain.ChargeFact;
import com.abinbev.b2b.price.api.domain.TaxFact;
import com.abinbev.b2b.price.api.domain.bre.WhenApplyRoundingOnTaxesEnum;

@ExtendWith(MockitoExtension.class)
class CalculateOrderItemChargeAmountServiceTest {

	private static final String COUNTRY = "BR";
	private static final String SKU_0001 = "SKU_0001";
	private static final String SKU_0002 = "SKU_0002";
	private static final BigDecimal TAX_BASE_PRICE = BigDecimal.valueOf(15);

	@Mock
	private PricingConfigurationService pricingConfigurationService;

	@Mock
	private CalculateTaxService calculateTaxService;

	@Mock
	private RoundValueService roundValueService;

	@InjectMocks
	private CalculateOrderItemChargeAmountService calculateOrderItemChargeAmountService;

	@Test
	void shouldReturnZeroWhenChargesAreNull() {

		final BigDecimal actualResponse = calculateOrderItemChargeAmountService.execute(null, null, null, COUNTRY);

		assertThat(actualResponse, is(notNullValue()));
		assertThat(actualResponse, is(equalTo(BigDecimal.ZERO)));

		verify(pricingConfigurationService, times(0)).getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		verify(calculateTaxService, times(0)).execute(anyMap(), any(), any(), anyString());
		verify(roundValueService, times(0)).execute(any());
	}

	@Test
	void shouldReturnZeroWhenChargesAreEmpty() {

		final BigDecimal actualResponse = calculateOrderItemChargeAmountService.execute(null, null, null, COUNTRY);

		assertThat(actualResponse, is(notNullValue()));
		assertThat(calculateOrderItemChargeAmountService.execute(new HashMap<>(), null, null, COUNTRY), is(equalTo(BigDecimal.ZERO)));

		verify(pricingConfigurationService, times(0)).getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		verify(calculateTaxService, times(0)).execute(anyMap(), any(), any(), anyString());
		verify(roundValueService, times(0)).execute(any());
	}

	@Test
	void shouldReturnCalculatedChargeAmountRoundedInTheEndWhenChargesAreOk() {

		final Map<String, TaxFact> mappedTaxByIdMock = new HashMap<>();
		final BigDecimal calculateTaxMock = BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);
		final BigDecimal expectedResponse = BigDecimal.valueOf(4).setScale(2, RoundingMode.HALF_UP);

		doReturn(WhenApplyRoundingOnTaxesEnum.IN_THE_END).when(pricingConfigurationService)
				.getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		doReturn(calculateTaxMock).when(calculateTaxService).execute(anyMap(), any(), any(), anyString());
		doCallRealMethod().when(roundValueService).execute(any());

		final BigDecimal chargeAmount = calculateOrderItemChargeAmountService
				.execute(getMappedChargesById(), TAX_BASE_PRICE, mappedTaxByIdMock, COUNTRY);

		assertThat(chargeAmount, is(notNullValue()));
		assertThat(chargeAmount, is(equalTo(expectedResponse)));

		verify(pricingConfigurationService, times(3)).getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		verify(calculateTaxService, times(2)).execute(anyMap(), any(), any(), anyString());
		verify(roundValueService).execute(any());
	}

	@Test
	void shouldReturnCalculatedChargeAmountRoundedInTheEndWhenChargesHasBaseNull() {

		final Map<String, TaxFact> mappedTaxByIdMock = new HashMap<>();
		final BigDecimal calculateTaxMock = BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);
		final BigDecimal expectedResponse = BigDecimal.valueOf(5).setScale(2, RoundingMode.HALF_UP);

		doReturn(WhenApplyRoundingOnTaxesEnum.IN_THE_END).when(pricingConfigurationService)
				.getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		doReturn(calculateTaxMock).when(calculateTaxService).execute(anyMap(), any(), any(), anyString());
		doCallRealMethod().when(roundValueService).execute(any());

		final BigDecimal chargeAmount = calculateOrderItemChargeAmountService
				.execute(getMappedChargesByIdWhichHasTypePercentageAndBaseNull(), TAX_BASE_PRICE, mappedTaxByIdMock, COUNTRY);

		assertThat(chargeAmount, is(notNullValue()));
		assertThat(chargeAmount, is(equalTo(expectedResponse)));

		verify(pricingConfigurationService, times(3)).getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		verify(calculateTaxService, times(2)).execute(anyMap(), any(), any(), anyString());
		verify(roundValueService).execute(any());
	}

	@Test
	void shouldReturnCalculatedChargeAmountRoundedInTheEndWhenChargesAreOkAndPaymentTypeIsMoney() {

		final Map<String, TaxFact> mappedTaxByIdMock = new HashMap<>();
		final BigDecimal calculateTaxMock = BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);
		final BigDecimal expectedResponse = BigDecimal.valueOf(22).setScale(2, RoundingMode.HALF_UP);

		doReturn(WhenApplyRoundingOnTaxesEnum.IN_THE_END).when(pricingConfigurationService)
				.getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		doReturn(calculateTaxMock).when(calculateTaxService).execute(anyMap(), any(), any(), anyString());
		doCallRealMethod().when(roundValueService).execute(any());

		final BigDecimal chargeAmount = calculateOrderItemChargeAmountService
				.execute(getMappedChargesByIdWhichHasTypeMoney(), TAX_BASE_PRICE, mappedTaxByIdMock, COUNTRY);

		assertThat(chargeAmount, is(notNullValue()));
		assertThat(chargeAmount, is(equalTo(expectedResponse)));

		verify(pricingConfigurationService, times(3)).getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		verify(calculateTaxService, times(2)).execute(anyMap(), any(), any(), anyString());
		verify(roundValueService).execute(any());
	}

	@Test
	void shouldReturnCalculatedChargeAmountRoundedInTheEndWhenChargesHasValueNull() {

		final Map<String, TaxFact> mappedTaxByIdMock = new HashMap<>();
		final BigDecimal calculateTaxMock = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		final BigDecimal expectedResponse = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

		doReturn(WhenApplyRoundingOnTaxesEnum.IN_THE_END).when(pricingConfigurationService)
				.getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		doReturn(calculateTaxMock).when(calculateTaxService).execute(anyMap(), any(), any(), anyString());
		doCallRealMethod().when(roundValueService).execute(any());

		final BigDecimal chargeAmount = calculateOrderItemChargeAmountService
				.execute(getMappedChargesByIdWhichHasTypeMoneyAndValueNull(), TAX_BASE_PRICE, mappedTaxByIdMock, COUNTRY);

		assertThat(chargeAmount, is(notNullValue()));
		assertThat(chargeAmount, is(equalTo(expectedResponse)));

		verify(pricingConfigurationService, times(3)).getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		verify(calculateTaxService, times(2)).execute(anyMap(), any(), any(), anyString());
		verify(roundValueService).execute(any());
	}

	@Test
	void shouldReturnCalculatedChargeAmountRoundedOnEachTaxTaxWhenChargesAreOk() {

		final Map<String, TaxFact> mappedTaxByIdMock = new HashMap<>();
		final BigDecimal calculateTaxMock = BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);
		final BigDecimal expectedResponse = BigDecimal.valueOf(4).setScale(2, RoundingMode.HALF_UP);

		doReturn(WhenApplyRoundingOnTaxesEnum.ON_EACH_TAX).when(pricingConfigurationService)
				.getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		doReturn(calculateTaxMock).when(calculateTaxService).execute(anyMap(), any(), any(), anyString());
		doCallRealMethod().when(roundValueService).execute(any());

		final BigDecimal chargeAmount = calculateOrderItemChargeAmountService
				.execute(getMappedChargesById(), TAX_BASE_PRICE, mappedTaxByIdMock, COUNTRY);

		assertThat(chargeAmount, is(notNullValue()));
		assertThat(chargeAmount, is(equalTo(expectedResponse)));

		verify(pricingConfigurationService, times(3)).getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		verify(calculateTaxService, times(2)).execute(anyMap(), any(), any(), anyString());
		verify(roundValueService, times(2)).execute(any());
	}

	@Test
	void shouldReturnCalculatedChargeAmountRoundedOnEachTaxWhenChargesHasBaseNull() {

		final Map<String, TaxFact> mappedTaxByIdMock = new HashMap<>();
		final BigDecimal calculateTaxMock = BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);
		final BigDecimal expectedResponse = BigDecimal.valueOf(5).setScale(2, RoundingMode.HALF_UP);

		doReturn(WhenApplyRoundingOnTaxesEnum.ON_EACH_TAX).when(pricingConfigurationService)
				.getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		doReturn(calculateTaxMock).when(calculateTaxService).execute(anyMap(), any(), any(), anyString());
		doCallRealMethod().when(roundValueService).execute(any());

		final BigDecimal chargeAmount = calculateOrderItemChargeAmountService
				.execute(getMappedChargesByIdWhichHasTypePercentageAndBaseNull(), TAX_BASE_PRICE, mappedTaxByIdMock, COUNTRY);

		assertThat(chargeAmount, is(notNullValue()));
		assertThat(chargeAmount, is(equalTo(expectedResponse)));

		verify(pricingConfigurationService, times(3)).getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		verify(calculateTaxService, times(2)).execute(anyMap(), any(), any(), anyString());
		verify(roundValueService, times(2)).execute(any());
	}

	@Test
	void shouldReturnCalculatedChargeAmountRoundedOnEachTaxWhenChargesAreOkAndPaymentTypeIsMoney() {

		final Map<String, TaxFact> mappedTaxByIdMock = new HashMap<>();
		final BigDecimal calculateTaxMock = BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);
		final BigDecimal expectedResponse = BigDecimal.valueOf(22).setScale(2, RoundingMode.HALF_UP);

		doReturn(WhenApplyRoundingOnTaxesEnum.ON_EACH_TAX).when(pricingConfigurationService)
				.getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		doReturn(calculateTaxMock).when(calculateTaxService).execute(anyMap(), any(), any(), anyString());
		doCallRealMethod().when(roundValueService).execute(any());

		final BigDecimal chargeAmount = calculateOrderItemChargeAmountService
				.execute(getMappedChargesByIdWhichHasTypeMoney(), TAX_BASE_PRICE, mappedTaxByIdMock, COUNTRY);

		assertThat(chargeAmount, is(notNullValue()));
		assertThat(chargeAmount, is(equalTo(expectedResponse)));

		verify(pricingConfigurationService, times(3)).getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		verify(calculateTaxService, times(2)).execute(anyMap(), any(), any(), anyString());
		verify(roundValueService, times(2)).execute(any());
	}

	@Test
	void shouldReturnCalculatedChargeAmountRoundedOnEachTaxWhenChargesHasValueNull() {

		final Map<String, TaxFact> mappedTaxByIdMock = new HashMap<>();
		final BigDecimal calculateTaxMock = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		final BigDecimal expectedResponse = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

		doReturn(WhenApplyRoundingOnTaxesEnum.ON_EACH_TAX).when(pricingConfigurationService)
				.getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		doReturn(calculateTaxMock).when(calculateTaxService).execute(anyMap(), any(), any(), anyString());
		doCallRealMethod().when(roundValueService).execute(any());

		final BigDecimal chargeAmount = calculateOrderItemChargeAmountService
				.execute(getMappedChargesByIdWhichHasTypeMoneyAndValueNull(), TAX_BASE_PRICE, mappedTaxByIdMock, COUNTRY);

		assertThat(chargeAmount, is(notNullValue()));
		assertThat(chargeAmount, is(equalTo(expectedResponse)));

		verify(pricingConfigurationService, times(3)).getPriceConfigWhenApplyRoundingOnTaxesEnum(COUNTRY);
		verify(calculateTaxService, times(2)).execute(anyMap(), any(), any(), anyString());
		verify(roundValueService, times(2)).execute(any());
	}

	private Map<String, ChargeFact> getMappedChargesById() {

		final Map<String, ChargeFact> mappedChargesById = new HashMap<>();

		final ChargeFact charge1 = new ChargeFact();
		charge1.setType("%");
		charge1.setBase(BigDecimal.TEN);
		charge1.setValue(BigDecimal.TEN);

		final ChargeFact charge2 = new ChargeFact();
		charge2.setType("%");
		charge2.setBase(BigDecimal.TEN);
		charge2.setValue(BigDecimal.TEN);

		mappedChargesById.put(SKU_0001, charge1);
		mappedChargesById.put(SKU_0002, charge2);

		return mappedChargesById;
	}

	private Map<String, ChargeFact> getMappedChargesByIdWhichHasTypePercentageAndBaseNull() {

		final Map<String, ChargeFact> mappedChargesById = new HashMap<>();

		final ChargeFact charge1 = new ChargeFact();
		charge1.setType("%");
		charge1.setValue(BigDecimal.TEN);

		final ChargeFact charge2 = new ChargeFact();
		charge2.setType("%");
		charge2.setValue(BigDecimal.TEN);

		mappedChargesById.put(SKU_0001, charge1);
		mappedChargesById.put(SKU_0002, charge2);

		return mappedChargesById;
	}

	private Map<String, ChargeFact> getMappedChargesByIdWhichHasTypeMoney() {

		final Map<String, ChargeFact> mappedChargesById = new HashMap<>();

		final ChargeFact charge1 = new ChargeFact();
		charge1.setType("$");
		charge1.setBase(BigDecimal.TEN);
		charge1.setValue(BigDecimal.TEN);

		final ChargeFact charge2 = new ChargeFact();
		charge2.setType("$");
		charge2.setBase(BigDecimal.TEN);
		charge2.setValue(BigDecimal.TEN);

		mappedChargesById.put(SKU_0001, charge1);
		mappedChargesById.put(SKU_0002, charge2);

		return mappedChargesById;
	}

	private Map<String, ChargeFact> getMappedChargesByIdWhichHasTypeMoneyAndValueNull() {

		final Map<String, ChargeFact> mappedChargesById = new HashMap<>();

		final ChargeFact charge1 = new ChargeFact();
		charge1.setType("$");
		charge1.setBase(BigDecimal.TEN);

		final ChargeFact charge2 = new ChargeFact();
		charge2.setType("$");
		charge2.setBase(BigDecimal.TEN);

		mappedChargesById.put(SKU_0001, charge1);
		mappedChargesById.put(SKU_0002, charge2);

		return mappedChargesById;
	}

}
