package com.abinbev.b2b.price.api.services.browseprice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abinbev.b2b.price.api.helpers.ApiConstants;

@ExtendWith(MockitoExtension.class)
class CalculateTaxBasePriceServiceTest {

	private static final String COUNTRY = "BR";
	private static final BigDecimal ITEM_BASE_PRICE = BigDecimal.TEN;
	private static final BigDecimal DEPOSIT_VALUE = BigDecimal.ONE;
	private static final BigDecimal CONSIGNMENT_VALUE = BigDecimal.ONE;

	@Mock
	private PricingConfigurationService pricingConfigurationService;

	@InjectMocks
	private CalculateTaxBasePriceService calculateTaxBasePriceService;

	@Test
	void shouldNotSubtractDepositWhenTaxIncludeDeposit() {

		doReturn(Boolean.TRUE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.PRICE_INCLUDE_DEPOSIT);
		doReturn(Boolean.TRUE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.TAX_INCLUDE_DEPOSIT);

		assertThat(calculateTaxBasePriceService.execute(ITEM_BASE_PRICE, DEPOSIT_VALUE, null, COUNTRY), is(equalTo(ITEM_BASE_PRICE)));
		verify(pricingConfigurationService, times(2)).getPriceConfig(anyString(), anyString());
	}

	@Test
	void shouldNotSubtractDepositWhenDepositValueIsNull() {

		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.TAX_INCLUDE_DEPOSIT);
		doReturn(Boolean.TRUE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.PRICE_INCLUDE_DEPOSIT);

		assertThat(calculateTaxBasePriceService.execute(ITEM_BASE_PRICE, null, null, COUNTRY), is(equalTo(ITEM_BASE_PRICE)));
		verify(pricingConfigurationService, times(2)).getPriceConfig(anyString(), anyString());
	}

	@Test
	void shouldNotSubtractDepositWhenPriceDoesNotIncludeDeposit() {

		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.TAX_INCLUDE_DEPOSIT);
		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.PRICE_INCLUDE_DEPOSIT);

		assertThat(calculateTaxBasePriceService.execute(ITEM_BASE_PRICE, DEPOSIT_VALUE, null, COUNTRY), is(equalTo(ITEM_BASE_PRICE)));
		verify(pricingConfigurationService, times(2)).getPriceConfig(anyString(), anyString());
	}

	@Test
	void shouldSubtractDepositWhenAllConditionsMatch() {

		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.TAX_INCLUDE_DEPOSIT);
		doReturn(Boolean.TRUE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.PRICE_INCLUDE_DEPOSIT);

		assertThat(calculateTaxBasePriceService.execute(ITEM_BASE_PRICE, DEPOSIT_VALUE, null, COUNTRY),
				is(equalTo(ITEM_BASE_PRICE.subtract(DEPOSIT_VALUE))));
		verify(pricingConfigurationService, times(2)).getPriceConfig(anyString(), anyString());
	}

	@Test
	void shouldSubtractDepositAndConsignmentWhenAllConditionsMatch() {

		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.TAX_INCLUDE_DEPOSIT);
		doReturn(Boolean.TRUE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.PRICE_INCLUDE_DEPOSIT);

		assertThat(calculateTaxBasePriceService.execute(ITEM_BASE_PRICE, DEPOSIT_VALUE, CONSIGNMENT_VALUE, COUNTRY),
				is(equalTo(ITEM_BASE_PRICE.subtract(DEPOSIT_VALUE).subtract(CONSIGNMENT_VALUE))));
		verify(pricingConfigurationService, times(2)).getPriceConfig(anyString(), anyString());
	}

	@Test
	void shouldAddDepositAndConsignmentWhenAllConditionsMatch() {

		doReturn(Boolean.TRUE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.TAX_INCLUDE_DEPOSIT);
		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.PRICE_INCLUDE_DEPOSIT);

		assertThat(calculateTaxBasePriceService.execute(ITEM_BASE_PRICE, DEPOSIT_VALUE, CONSIGNMENT_VALUE, COUNTRY),
				is(equalTo(ITEM_BASE_PRICE.add(DEPOSIT_VALUE).add(CONSIGNMENT_VALUE))));
		verify(pricingConfigurationService, times(2)).getPriceConfig(anyString(), anyString());
	}

}
