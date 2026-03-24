package com.abinbev.b2b.price.api.services.browseprice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abinbev.b2b.price.api.domain.PriceFact;
import com.abinbev.b2b.price.api.domain.PromotionalPriceFact;
import com.abinbev.b2b.price.api.helpers.ApiConstants;

@ExtendWith(MockitoExtension.class)
class CalculateItemBasePriceServiceTest {

	private static final String COUNTRY = "BR";
	private static final BigDecimal DEPOSIT_VALUE = BigDecimal.ONE;
	private static final BigDecimal CONSIGNMENT_VALUE = BigDecimal.ONE;
	private static final BigDecimal BASE_PRICE_VALUE = BigDecimal.valueOf(100);
	private static final BigDecimal PROMOTIONAL_PRICE_GREATER_VALUE = BigDecimal.valueOf(200);
	private static final BigDecimal PROMOTIONAL_PRICE_LESSER_VALUE = BigDecimal.valueOf(150);
	private static final BigDecimal MINIMUM_PRICE_GREATER_VALUE = BigDecimal.valueOf(300);
	private static final BigDecimal MINIMUM_PRICE_LESSER_VALUE = BigDecimal.valueOf(100);

	@Mock
	private PricingConfigurationService pricingConfigurationService;

	@InjectMocks
	private CalculateItemBasePriceService calculateItemBasePriceService;

	@Test
	void shouldReturnBasePriceWhenPromotionalPriceIsNullAndPriceDoesNotIncludeDeposit() {

		final BigDecimal expectedValue = BigDecimal.valueOf(100);

		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.PRICE_INCLUDE_DEPOSIT);

		assertThat(calculateItemBasePriceService.execute(getBasePriceFact(), COUNTRY, true), is(equalTo(expectedValue)));
	}

	@Test
	void shouldReturnPromotionalPriceWhenItIsEqualOrGreaterThanMinimumPriceAndDoesNotIncludeDeposit() {

		final BigDecimal expectedValue = BigDecimal.valueOf(200);

		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.PRICE_INCLUDE_DEPOSIT);

		assertThat(calculateItemBasePriceService.execute(getPriceFactWithPromotionalPriceGreaterThanMinimumPrice(), COUNTRY, false),
				is(equalTo(expectedValue)));
	}

	@Test
	void shouldReturnIgnorePromotionalPriceWhenItIsEqualOrGreaterThanMinimumPriceAndDoesNotIncludeDeposit() {

		final BigDecimal expectedValue = BigDecimal.valueOf(100);

		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.PRICE_INCLUDE_DEPOSIT);

		assertThat(calculateItemBasePriceService.execute(getPriceFactWithPromotionalPriceGreaterThanMinimumPrice(), COUNTRY, true),
				is(equalTo(expectedValue)));
	}

	@Test
	void shouldReturnMinimumPriceWhenItIsGreaterThanPromotionalPriceAndDoesNotIncludeDeposit() {

		final BigDecimal expectedValue = BigDecimal.valueOf(300);

		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.PRICE_INCLUDE_DEPOSIT);

		assertThat(calculateItemBasePriceService.execute(getPriceFactWithPromotionalPriceLesserThanMinimumPrice(), COUNTRY, false),
				is(equalTo(expectedValue)));
	}

	@Test
	void shouldReturnBasePriceWhenPriceIncludeDepositAndDepositValueIsNull() {

		final BigDecimal expectedValue = BigDecimal.valueOf(100);

		doReturn(Boolean.TRUE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.PRICE_INCLUDE_DEPOSIT);

		assertThat(calculateItemBasePriceService.execute(getBasePriceFact(), COUNTRY, true), is(equalTo(expectedValue)));
	}

	@Test
	void shouldReturnSummedBasePriceWithDepositWhenPriceIncludeDepositAndDepositValueIsFilled() {

		final BigDecimal expectedValue = BigDecimal.valueOf(101);

		doReturn(Boolean.TRUE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.PRICE_INCLUDE_DEPOSIT);

		assertThat(calculateItemBasePriceService.execute(getPriceFactWithDeposit(), COUNTRY, true), is(equalTo(expectedValue)));
	}

	@Test
	void shouldReturnSummedBasePriceWithDepositWhenPriceIncludeDepositIsTrueDepositAndConsignmentAreFilled() {

		final BigDecimal expectedValue = BigDecimal.valueOf(102);

		doReturn(Boolean.TRUE).when(pricingConfigurationService).getPriceConfig(COUNTRY, ApiConstants.PRICE_INCLUDE_DEPOSIT);
		final PriceFact priceFact = getPriceFactWithDeposit();
		priceFact.setConsignment(CONSIGNMENT_VALUE);

		assertThat(calculateItemBasePriceService.execute(priceFact, COUNTRY, true), is(equalTo(expectedValue)));
	}

	private PriceFact getBasePriceFact() {

		final PriceFact priceFact = new PriceFact();

		priceFact.setBasePrice(BASE_PRICE_VALUE);

		return priceFact;
	}

	private PriceFact getPriceFactWithPromotionalPriceGreaterThanMinimumPrice() {

		final PromotionalPriceFact promotionalPrice = new PromotionalPriceFact();
		final PriceFact priceFact = getBasePriceFact();

		promotionalPrice.setPrice(PROMOTIONAL_PRICE_GREATER_VALUE);

		priceFact.setPromotionalPrice(promotionalPrice);
		priceFact.setMinimumPrice(MINIMUM_PRICE_LESSER_VALUE);

		return priceFact;
	}

	private PriceFact getPriceFactWithPromotionalPriceLesserThanMinimumPrice() {

		final PromotionalPriceFact promotionalPrice = new PromotionalPriceFact();
		final PriceFact priceFact = getBasePriceFact();

		promotionalPrice.setPrice(PROMOTIONAL_PRICE_LESSER_VALUE);

		priceFact.setPromotionalPrice(promotionalPrice);
		priceFact.setMinimumPrice(MINIMUM_PRICE_GREATER_VALUE);

		return priceFact;
	}

	private PriceFact getPriceFactWithDeposit() {

		final PriceFact priceFact = getBasePriceFact();

		priceFact.setDeposit(DEPOSIT_VALUE);

		return priceFact;
	}

}
