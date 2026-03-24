package com.abinbev.b2b.price.api.services.browseprice;

import static com.abinbev.b2b.price.api.helpers.ApiConstants.EXCLUDE_CHARGE_FROM_BROWSE_PRICE;
import static com.abinbev.b2b.price.api.helpers.ApiConstants.EXCLUDE_TAXES_FROM_BROWSE_PRICE;
import static com.abinbev.b2b.price.api.helpers.ApiConstants.ROUND_TAXES_AND_DISCOUNT_BEFORE_SUBTOTAL_AND_TOTAL;
import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;

import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abinbev.b2b.price.api.domain.BrowsePrice;
import com.abinbev.b2b.price.api.domain.PriceFact;
import com.abinbev.b2b.price.api.domain.PromotionalPriceFact;
import com.abinbev.b2b.price.api.helpers.CalculateDaysBehindToShowValidUntilHelper;

@ExtendWith(MockitoExtension.class)
class CalculateBrowsePriceServiceTest {

	private static final BigDecimal ITEM_BASE_PRICE_VALUE = BigDecimal.valueOf(99.999).setScale(2, RoundingMode.HALF_UP);
	private static final BigDecimal ITEM_PROMOTIONAL_PRICE_VALUE = BigDecimal.valueOf(49.999).setScale(2, RoundingMode.HALF_UP);
	private static final BigDecimal ROUNDED_ITEM_BASE_PRICE_VALUE = BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP);
	private static final BigDecimal TAX_VALUE = BigDecimal.TEN.setScale(2, RoundingMode.HALF_UP);
	private static final BigDecimal ORDER_ITEM_CHARGE_AMOUNT_VALUE = BigDecimal.TEN.setScale(2, RoundingMode.HALF_UP);
	private static final BigDecimal DEPOSIT_VALUE = BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);
	private static final String MOCKED_TIME_ZONE_ID_NY = "America/New_York";
	private static final String MOCKED_TIME_ZONE_ID_SP = "America/Sao_Paulo";
	private static final String COUNTRY = "BR";
	private static final String COUNTRY_US = "US";
	private static final String MOCKED_SKU_1 = "SKU0001";
	private static final String MOCKED_VENDOR_ITEM_ID_1 = "VENDOR_ITEM_ID_0001";
	private static final String EXTERNAL_ID_01 = "EXTERNAL_ID_01";
	private static final String INVALID_VALID_UNTIL = "31-12-2021";

	@Mock
	private PricingConfigurationService pricingConfigurationService;

	@Mock
	private CalculateItemBasePriceService calculateItemBasePriceService;

	@Mock
	private CalculateTaxBasePriceService calculateTaxBasePriceService;

	@Mock
	private CalculateTaxService calculateTaxService;

	@Mock
	private CalculateOrderItemChargeAmountService calculateOrderItemChargeAmountService;

	@Mock
	private RoundValueService roundValueService;

	@Mock
	private CalculateDaysBehindToShowValidUntilHelper calculateDaysBehindToShowValidUntilHelper;

	@InjectMocks
	private CalculateBrowsePriceService calculateBrowsePriceService;

	@Test
	void shouldReturnSuccessWhenRoundPricingConfigIsFalse() {

		final BrowsePrice browsePrice = new BrowsePrice();
		browsePrice.setPrice(ITEM_BASE_PRICE_VALUE.add(TAX_VALUE).add(ORDER_ITEM_CHARGE_AMOUNT_VALUE));
		browsePrice.setOriginalPrice(ITEM_BASE_PRICE_VALUE.add(TAX_VALUE).add(ORDER_ITEM_CHARGE_AMOUNT_VALUE));
		browsePrice.setSku(MOCKED_SKU_1);
		browsePrice.setVendorItemId(MOCKED_VENDOR_ITEM_ID_1);
		browsePrice.setValidUntil(null);

		doReturn(TAX_VALUE).when(calculateTaxService).execute(anyMap(), any(), any(), anyString());
		doReturn(ORDER_ITEM_CHARGE_AMOUNT_VALUE).when(calculateOrderItemChargeAmountService)
				.execute(anyMap(), any(), anyMap(), anyString());
		doReturn(ITEM_BASE_PRICE_VALUE).when(calculateItemBasePriceService).execute(any(), anyString(), eq(true));
		doReturn(ITEM_BASE_PRICE_VALUE).when(calculateItemBasePriceService).execute(any(), anyString(), eq(false));
		doReturn(Boolean.FALSE).when(pricingConfigurationService)
				.getPriceConfig(COUNTRY, ROUND_TAXES_AND_DISCOUNT_BEFORE_SUBTOTAL_AND_TOTAL);
		doReturn(ITEM_BASE_PRICE_VALUE).when(calculateTaxBasePriceService).execute(ITEM_BASE_PRICE_VALUE, DEPOSIT_VALUE, null, COUNTRY);
		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, EXCLUDE_TAXES_FROM_BROWSE_PRICE);
		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, EXCLUDE_CHARGE_FROM_BROWSE_PRICE);
		doCallRealMethod().when(roundValueService).execute(any());

		final BrowsePrice calculatedBrowsePrice = calculateBrowsePriceService
				.execute(getMockedPriceFact(false, MOCKED_TIME_ZONE_ID_SP, COUNTRY), COUNTRY);
		assertThat(calculatedBrowsePrice.getPrice(), is(equalTo(browsePrice.getPrice())));
		assertThat(calculatedBrowsePrice.getOriginalPrice(), is(equalTo(null)));
		assertThat(calculatedBrowsePrice.getSku(), is(equalTo(browsePrice.getSku())));
		assertThat(calculatedBrowsePrice.getVendorItemId(), is(equalTo(browsePrice.getVendorItemId())));
		assertThat(calculatedBrowsePrice.getValidUntil(), is(equalTo(browsePrice.getValidUntil())));
		verify(roundValueService, times(2)).execute(any());
	}

	@Test
	void shouldReturnSuccessWhenRoundPricingConfigIsTrue() {

		final BrowsePrice browsePrice = new BrowsePrice();
		browsePrice.setPrice(ROUNDED_ITEM_BASE_PRICE_VALUE.add(TAX_VALUE).add(ORDER_ITEM_CHARGE_AMOUNT_VALUE));
		browsePrice.setOriginalPrice(ROUNDED_ITEM_BASE_PRICE_VALUE.add(TAX_VALUE).add(ORDER_ITEM_CHARGE_AMOUNT_VALUE));
		browsePrice.setSku(MOCKED_SKU_1);
		browsePrice.setVendorItemId(MOCKED_VENDOR_ITEM_ID_1);
		browsePrice.setValidUntil(null);

		doReturn(TAX_VALUE).when(calculateTaxService).execute(anyMap(), any(), any(), anyString());
		doReturn(ORDER_ITEM_CHARGE_AMOUNT_VALUE).when(calculateOrderItemChargeAmountService)
				.execute(anyMap(), any(), anyMap(), anyString());
		doReturn(ITEM_BASE_PRICE_VALUE).when(calculateItemBasePriceService).execute(any(), anyString(), anyBoolean());
		doReturn(Boolean.TRUE).when(pricingConfigurationService)
				.getPriceConfig(COUNTRY, ROUND_TAXES_AND_DISCOUNT_BEFORE_SUBTOTAL_AND_TOTAL);
		doReturn(ROUNDED_ITEM_BASE_PRICE_VALUE).when(calculateTaxBasePriceService)
				.execute(ROUNDED_ITEM_BASE_PRICE_VALUE, DEPOSIT_VALUE, null, COUNTRY);
		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, EXCLUDE_TAXES_FROM_BROWSE_PRICE);
		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, EXCLUDE_CHARGE_FROM_BROWSE_PRICE);
		doCallRealMethod().when(roundValueService).execute(any());

		final BrowsePrice calculatedBrowsePrice = calculateBrowsePriceService
				.execute(getMockedPriceFact(false, MOCKED_TIME_ZONE_ID_SP, COUNTRY), COUNTRY);
		assertThat(calculatedBrowsePrice.getPrice(), is(equalTo(browsePrice.getPrice())));
		assertThat(calculatedBrowsePrice.getOriginalPrice(), is(equalTo(null)));
		assertThat(calculatedBrowsePrice.getSku(), is(equalTo(browsePrice.getSku())));
		assertThat(calculatedBrowsePrice.getVendorItemId(), is(equalTo(browsePrice.getVendorItemId())));
		assertThat(calculatedBrowsePrice.getValidUntil(), is(equalTo(browsePrice.getValidUntil())));

		verify(roundValueService, times(4)).execute(any());
	}

	@Test
	void shouldReturnBasePriceWhenExcludeTaxesAndChargesAreEnabled() {

		final BrowsePrice browsePrice = new BrowsePrice();
		browsePrice.setPrice(ITEM_BASE_PRICE_VALUE);
		browsePrice.setOriginalPrice(ITEM_BASE_PRICE_VALUE);
		browsePrice.setSku(MOCKED_SKU_1);
		browsePrice.setVendorItemId(MOCKED_VENDOR_ITEM_ID_1);
		browsePrice.setValidUntil(null);

		doReturn(ITEM_BASE_PRICE_VALUE).when(calculateItemBasePriceService).execute(any(), anyString(), anyBoolean());
		doReturn(Boolean.FALSE).when(pricingConfigurationService)
				.getPriceConfig(COUNTRY, ROUND_TAXES_AND_DISCOUNT_BEFORE_SUBTOTAL_AND_TOTAL);
		doReturn(Boolean.TRUE).when(pricingConfigurationService).getPriceConfig(COUNTRY, EXCLUDE_TAXES_FROM_BROWSE_PRICE);
		doReturn(Boolean.TRUE).when(pricingConfigurationService).getPriceConfig(COUNTRY, EXCLUDE_CHARGE_FROM_BROWSE_PRICE);

		final BrowsePrice calculatedBrowsePrice = calculateBrowsePriceService
				.execute(getMockedPriceFact(false, MOCKED_TIME_ZONE_ID_SP, COUNTRY), COUNTRY);
		assertThat(calculatedBrowsePrice.getPrice(), is(equalTo(ROUNDED_ITEM_BASE_PRICE_VALUE)));
		assertThat(calculatedBrowsePrice.getOriginalPrice(), is(equalTo(null)));
		assertThat(calculatedBrowsePrice.getSku(), is(equalTo(browsePrice.getSku())));
		assertThat(calculatedBrowsePrice.getVendorItemId(), is(equalTo(browsePrice.getVendorItemId())));
		assertThat(calculatedBrowsePrice.getValidUntil(), is(equalTo(browsePrice.getValidUntil())));

		verify(roundValueService, times(0)).execute(any());
	}

	@Test
	void shouldReturnBasePriceWhenOnlyChargeWhenExcludeTaxesAreEnabled() {

		final BrowsePrice browsePrice = new BrowsePrice();
		browsePrice.setPrice(ROUNDED_ITEM_BASE_PRICE_VALUE.add(ORDER_ITEM_CHARGE_AMOUNT_VALUE));
		browsePrice.setOriginalPrice(ROUNDED_ITEM_BASE_PRICE_VALUE.add(ORDER_ITEM_CHARGE_AMOUNT_VALUE));
		browsePrice.setSku(MOCKED_SKU_1);
		browsePrice.setVendorItemId(MOCKED_VENDOR_ITEM_ID_1);
		browsePrice.setValidUntil(null);

		doReturn(ORDER_ITEM_CHARGE_AMOUNT_VALUE).when(calculateOrderItemChargeAmountService)
				.execute(anyMap(), any(), anyMap(), anyString());
		doReturn(ITEM_BASE_PRICE_VALUE).when(calculateItemBasePriceService).execute(any(), anyString(), anyBoolean());
		doReturn(Boolean.TRUE).when(pricingConfigurationService)
				.getPriceConfig(COUNTRY, ROUND_TAXES_AND_DISCOUNT_BEFORE_SUBTOTAL_AND_TOTAL);
		doReturn(ROUNDED_ITEM_BASE_PRICE_VALUE).when(calculateTaxBasePriceService)
				.execute(ROUNDED_ITEM_BASE_PRICE_VALUE, DEPOSIT_VALUE, null, COUNTRY);
		doReturn(Boolean.TRUE).when(pricingConfigurationService).getPriceConfig(COUNTRY, EXCLUDE_TAXES_FROM_BROWSE_PRICE);
		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, EXCLUDE_CHARGE_FROM_BROWSE_PRICE);
		doCallRealMethod().when(roundValueService).execute(any());

		final BrowsePrice calculatedBrowsePrice = calculateBrowsePriceService
				.execute(getMockedPriceFact(false, MOCKED_TIME_ZONE_ID_SP, COUNTRY), COUNTRY);
		assertThat(calculatedBrowsePrice.getPrice(), is(equalTo(browsePrice.getPrice())));
		assertThat(calculatedBrowsePrice.getOriginalPrice(), is(equalTo(null)));
		assertThat(calculatedBrowsePrice.getSku(), is(equalTo(browsePrice.getSku())));
		assertThat(calculatedBrowsePrice.getVendorItemId(), is(equalTo(browsePrice.getVendorItemId())));
		assertThat(calculatedBrowsePrice.getValidUntil(), is(equalTo(browsePrice.getValidUntil())));
		verify(roundValueService, times(4)).execute(any());
	}

	@Test
	void shouldReturnBasePriceWhenOnlyTaxesWhenExcludeChargeAreEnabled() {

		final BrowsePrice browsePrice = new BrowsePrice();
		browsePrice.setPrice(ROUNDED_ITEM_BASE_PRICE_VALUE.add(TAX_VALUE));
		browsePrice.setOriginalPrice(ROUNDED_ITEM_BASE_PRICE_VALUE.add(TAX_VALUE));
		browsePrice.setSku(MOCKED_SKU_1);
		browsePrice.setVendorItemId(MOCKED_VENDOR_ITEM_ID_1);
		browsePrice.setValidUntil(null);

		doReturn(TAX_VALUE).when(calculateTaxService).execute(anyMap(), any(), any(), anyString());
		doReturn(ITEM_BASE_PRICE_VALUE).when(calculateItemBasePriceService).execute(any(), anyString(), anyBoolean());
		doReturn(Boolean.TRUE).when(pricingConfigurationService)
				.getPriceConfig(COUNTRY, ROUND_TAXES_AND_DISCOUNT_BEFORE_SUBTOTAL_AND_TOTAL);
		doReturn(ROUNDED_ITEM_BASE_PRICE_VALUE).when(calculateTaxBasePriceService)
				.execute(ROUNDED_ITEM_BASE_PRICE_VALUE, DEPOSIT_VALUE, null, COUNTRY);
		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, EXCLUDE_TAXES_FROM_BROWSE_PRICE);
		doReturn(Boolean.TRUE).when(pricingConfigurationService).getPriceConfig(COUNTRY, EXCLUDE_CHARGE_FROM_BROWSE_PRICE);
		doCallRealMethod().when(roundValueService).execute(any());

		final BrowsePrice calculatedBrowsePrice = calculateBrowsePriceService
				.execute(getMockedPriceFact(false, MOCKED_TIME_ZONE_ID_SP, COUNTRY), COUNTRY);
		assertThat(calculatedBrowsePrice.getPrice(), is(equalTo(browsePrice.getPrice())));
		assertThat(calculatedBrowsePrice.getOriginalPrice(), is(equalTo(null)));
		assertThat(calculatedBrowsePrice.getSku(), is(equalTo(browsePrice.getSku())));
		assertThat(calculatedBrowsePrice.getVendorItemId(), is(equalTo(browsePrice.getVendorItemId())));
		assertThat(calculatedBrowsePrice.getValidUntil(), is(equalTo(browsePrice.getValidUntil())));
		verify(roundValueService, times(4)).execute(any());
	}

	@Test
	void shouldReturnItemBasePriceWhenAddedToTaxes() {

		// Given
		final String countryMock = "CA";
		final BigDecimal itemBasePriceMock = BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP);
		final BigDecimal taxBasePriceMock = BigDecimal.valueOf(99).setScale(2, RoundingMode.HALF_UP);
		final BigDecimal depositMock = BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);
		final BigDecimal calculatedTaxes = BigDecimal.TEN;

		final PriceFact priceFact = new PriceFact();
		priceFact.setDeposit(depositMock);
		priceFact.setTaxes(emptyMap());

		doReturn(itemBasePriceMock).when(calculateItemBasePriceService).execute(any(), anyString(), anyBoolean());
		doReturn(taxBasePriceMock).when(calculateTaxBasePriceService).execute(itemBasePriceMock, depositMock, null, countryMock);
		doReturn(calculatedTaxes).when(calculateTaxService).execute(anyMap(), any(), any(), anyString());

		doReturn(true).when(pricingConfigurationService).getPriceConfig(countryMock, ROUND_TAXES_AND_DISCOUNT_BEFORE_SUBTOTAL_AND_TOTAL);
		doReturn(true).when(pricingConfigurationService).getPriceConfig(countryMock, EXCLUDE_CHARGE_FROM_BROWSE_PRICE);
		doReturn(false).when(pricingConfigurationService).getPriceConfig(countryMock, EXCLUDE_TAXES_FROM_BROWSE_PRICE);

		doCallRealMethod().when(roundValueService).execute(any());

		// When
		final BrowsePrice expectedBrowsePrice = calculateBrowsePriceService.execute(priceFact, countryMock);

		// Then
		assertThat(expectedBrowsePrice.getPrice(), is(BigDecimal.valueOf(110.00).setScale(2, RoundingMode.HALF_UP)));

		verify(pricingConfigurationService, times(2)).getPriceConfig(countryMock, ROUND_TAXES_AND_DISCOUNT_BEFORE_SUBTOTAL_AND_TOTAL);
		verify(pricingConfigurationService).getPriceConfig(countryMock, EXCLUDE_CHARGE_FROM_BROWSE_PRICE);
		verify(pricingConfigurationService).getPriceConfig(countryMock, EXCLUDE_TAXES_FROM_BROWSE_PRICE);
		verify(roundValueService, times(4)).execute(any());
		verifyNoInteractions(calculateOrderItemChargeAmountService);
	}

	@Test
	void shouldReturnItemBasePriceWhenAddedToTaxesAndCharge() {

		// Given
		final String countryMock = "CA";
		final BigDecimal itemBasePriceMock = BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP);
		final BigDecimal taxBasePriceMock = BigDecimal.valueOf(99).setScale(2, RoundingMode.HALF_UP);
		final BigDecimal depositMock = BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);
		final BigDecimal calculatedTaxes = BigDecimal.TEN;
		final BigDecimal calculatedChargeAmount = BigDecimal.ONE;

		final PriceFact priceFact = new PriceFact();
		priceFact.setDeposit(depositMock);
		priceFact.setTaxes(emptyMap());
		priceFact.setCharges(emptyMap());

		doReturn(itemBasePriceMock).when(calculateItemBasePriceService).execute(any(), anyString(), anyBoolean());
		doReturn(taxBasePriceMock).when(calculateTaxBasePriceService).execute(itemBasePriceMock, depositMock, null, countryMock);
		doReturn(calculatedTaxes).when(calculateTaxService).execute(anyMap(), any(), any(), anyString());
		doReturn(calculatedChargeAmount).when(calculateOrderItemChargeAmountService).execute(anyMap(), any(), any(), anyString());

		doReturn(true).when(pricingConfigurationService).getPriceConfig(countryMock, ROUND_TAXES_AND_DISCOUNT_BEFORE_SUBTOTAL_AND_TOTAL);
		doReturn(false).when(pricingConfigurationService).getPriceConfig(countryMock, EXCLUDE_CHARGE_FROM_BROWSE_PRICE);
		doReturn(false).when(pricingConfigurationService).getPriceConfig(countryMock, EXCLUDE_TAXES_FROM_BROWSE_PRICE);

		doCallRealMethod().when(roundValueService).execute(any());

		// When
		final BrowsePrice expectedBrowsePrice = calculateBrowsePriceService.execute(priceFact, countryMock);

		// Then
		assertThat(expectedBrowsePrice.getPrice(), is(BigDecimal.valueOf(111.00).setScale(2, RoundingMode.HALF_UP)));

		verify(calculateOrderItemChargeAmountService, times(2)).execute(emptyMap(), taxBasePriceMock, emptyMap(), countryMock);
		verify(pricingConfigurationService, times(2)).getPriceConfig(countryMock, ROUND_TAXES_AND_DISCOUNT_BEFORE_SUBTOTAL_AND_TOTAL);
		verify(pricingConfigurationService).getPriceConfig(countryMock, EXCLUDE_CHARGE_FROM_BROWSE_PRICE);
		verify(pricingConfigurationService).getPriceConfig(countryMock, EXCLUDE_TAXES_FROM_BROWSE_PRICE);
		verify(roundValueService, times(4)).execute(any());
	}

	@Test
	void shouldReturnTheCorrectListWhenConsideringPromotionalPriceWithWrongValidUntil() {

		final BrowsePrice browsePrice = new BrowsePrice();
		browsePrice.setPrice(ITEM_BASE_PRICE_VALUE.add(TAX_VALUE).add(ORDER_ITEM_CHARGE_AMOUNT_VALUE));
		browsePrice.setOriginalPrice(ITEM_BASE_PRICE_VALUE.add(TAX_VALUE).add(ORDER_ITEM_CHARGE_AMOUNT_VALUE));
		browsePrice.setSku(MOCKED_SKU_1);
		browsePrice.setVendorItemId(MOCKED_VENDOR_ITEM_ID_1);
		browsePrice.setValidUntil(null);

		doReturn(TAX_VALUE).when(calculateTaxService).execute(anyMap(), any(), any(), anyString());
		doReturn(ORDER_ITEM_CHARGE_AMOUNT_VALUE).when(calculateOrderItemChargeAmountService)
				.execute(anyMap(), any(), anyMap(), anyString());
		doReturn(ITEM_BASE_PRICE_VALUE).when(calculateItemBasePriceService).execute(any(), anyString(), eq(true));
		doReturn(ITEM_BASE_PRICE_VALUE).when(calculateItemBasePriceService).execute(any(), anyString(), eq(false));
		doReturn(Boolean.FALSE).when(pricingConfigurationService)
				.getPriceConfig(COUNTRY, ROUND_TAXES_AND_DISCOUNT_BEFORE_SUBTOTAL_AND_TOTAL);
		doReturn(ITEM_BASE_PRICE_VALUE).when(calculateTaxBasePriceService).execute(ITEM_BASE_PRICE_VALUE, DEPOSIT_VALUE, null, COUNTRY);
		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, EXCLUDE_TAXES_FROM_BROWSE_PRICE);
		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, EXCLUDE_CHARGE_FROM_BROWSE_PRICE);
		doCallRealMethod().when(roundValueService).execute(any());

		final PriceFact mockedPriceFact = getMockedPriceFact(true, MOCKED_TIME_ZONE_ID_SP, COUNTRY);
		mockedPriceFact.getPromotionalPrice().setValidUntil(INVALID_VALID_UNTIL);

		final BrowsePrice calculatedBrowsePrice = calculateBrowsePriceService.execute(mockedPriceFact, COUNTRY);
		assertThat(calculatedBrowsePrice.getPrice(), is(equalTo(browsePrice.getPrice())));
		assertThat(calculatedBrowsePrice.getOriginalPrice(), is(equalTo(null)));
		assertThat(calculatedBrowsePrice.getSku(), is(equalTo(browsePrice.getSku())));
		assertThat(calculatedBrowsePrice.getVendorItemId(), is(equalTo(browsePrice.getVendorItemId())));
		assertThat(calculatedBrowsePrice.getValidUntil(), is(equalTo(browsePrice.getValidUntil())));
		verify(roundValueService, times(2)).execute(any());
	}

	@Test
	void shouldReturnTheCorrectListWhenConsideringPromotionalPriceWithExpiredValidUntil() {

		final BrowsePrice browsePrice = new BrowsePrice();
		browsePrice.setPrice(ITEM_BASE_PRICE_VALUE.add(TAX_VALUE).add(ORDER_ITEM_CHARGE_AMOUNT_VALUE));
		browsePrice.setOriginalPrice(ITEM_BASE_PRICE_VALUE.add(TAX_VALUE).add(ORDER_ITEM_CHARGE_AMOUNT_VALUE));
		browsePrice.setSku(MOCKED_SKU_1);
		browsePrice.setVendorItemId(MOCKED_VENDOR_ITEM_ID_1);
		browsePrice.setValidUntil(null);

		doReturn(true).when(calculateDaysBehindToShowValidUntilHelper)
				.shouldShowValidUntil(getDatePlusDays(-1, MOCKED_TIME_ZONE_ID_SP), COUNTRY, MOCKED_TIME_ZONE_ID_SP);

		doReturn(TAX_VALUE).when(calculateTaxService).execute(anyMap(), any(), any(), anyString());
		doReturn(ORDER_ITEM_CHARGE_AMOUNT_VALUE).when(calculateOrderItemChargeAmountService)
				.execute(anyMap(), any(), anyMap(), anyString());
		doReturn(ITEM_BASE_PRICE_VALUE).when(calculateItemBasePriceService).execute(any(), anyString(), eq(true));
		doReturn(ITEM_BASE_PRICE_VALUE).when(calculateItemBasePriceService).execute(any(), anyString(), eq(false));
		doReturn(Boolean.FALSE).when(pricingConfigurationService)
				.getPriceConfig(COUNTRY, ROUND_TAXES_AND_DISCOUNT_BEFORE_SUBTOTAL_AND_TOTAL);
		doReturn(ITEM_BASE_PRICE_VALUE).when(calculateTaxBasePriceService).execute(ITEM_BASE_PRICE_VALUE, DEPOSIT_VALUE, null, COUNTRY);
		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, EXCLUDE_TAXES_FROM_BROWSE_PRICE);
		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, EXCLUDE_CHARGE_FROM_BROWSE_PRICE);
		doCallRealMethod().when(roundValueService).execute(any());

		final PriceFact mockedPriceFact = getMockedPriceFact(true, MOCKED_TIME_ZONE_ID_SP, COUNTRY);
		mockedPriceFact.getPromotionalPrice().setValidUntil(LocalDate.now().minusDays(1).toString());

		final BrowsePrice calculatedBrowsePrice = calculateBrowsePriceService.execute(mockedPriceFact, COUNTRY);
		assertThat(calculatedBrowsePrice.getPrice(), is(equalTo(browsePrice.getPrice())));
		assertThat(calculatedBrowsePrice.getOriginalPrice(), is(equalTo(null)));
		assertThat(calculatedBrowsePrice.getSku(), is(equalTo(browsePrice.getSku())));
		assertThat(calculatedBrowsePrice.getVendorItemId(), is(equalTo(browsePrice.getVendorItemId())));
		assertThat(calculatedBrowsePrice.getValidUntil(), is(equalTo(browsePrice.getValidUntil())));
		verify(roundValueService, times(2)).execute(any());
	}

	@Test
	void shouldReturnTheCorrectListWhenConsideringPromotionalPriceWithValidUntilOneDayAhead() {

		doReturn(true).when(calculateDaysBehindToShowValidUntilHelper)
				.shouldShowValidUntil(getDatePlusDays(1, MOCKED_TIME_ZONE_ID_SP), COUNTRY, MOCKED_TIME_ZONE_ID_SP);

		final BrowsePrice browsePrice = new BrowsePrice();
		browsePrice.setPrice(ITEM_PROMOTIONAL_PRICE_VALUE.add(TAX_VALUE).add(ORDER_ITEM_CHARGE_AMOUNT_VALUE));
		browsePrice.setOriginalPrice(ITEM_BASE_PRICE_VALUE.add(TAX_VALUE).add(ORDER_ITEM_CHARGE_AMOUNT_VALUE));
		browsePrice.setSku(MOCKED_SKU_1);
		browsePrice.setVendorItemId(MOCKED_VENDOR_ITEM_ID_1);
		browsePrice.setValidUntil(LocalDate.now().plusDays(1).toString());

		doReturn(TAX_VALUE).when(calculateTaxService).execute(any(), any(), any(), anyString());
		doReturn(ORDER_ITEM_CHARGE_AMOUNT_VALUE).when(calculateOrderItemChargeAmountService).execute(any(), any(), any(), anyString());
		doReturn(ITEM_BASE_PRICE_VALUE).when(calculateItemBasePriceService).execute(any(), anyString(), eq(true));
		doReturn(ITEM_PROMOTIONAL_PRICE_VALUE).when(calculateItemBasePriceService).execute(any(), anyString(), eq(false));
		doReturn(Boolean.FALSE).when(pricingConfigurationService)
				.getPriceConfig(COUNTRY, ROUND_TAXES_AND_DISCOUNT_BEFORE_SUBTOTAL_AND_TOTAL);
		doReturn(ITEM_BASE_PRICE_VALUE).when(calculateTaxBasePriceService).execute(eq(ITEM_BASE_PRICE_VALUE), any(), any(), any());
		doReturn(ITEM_PROMOTIONAL_PRICE_VALUE).when(calculateTaxBasePriceService)
				.execute(eq(ITEM_PROMOTIONAL_PRICE_VALUE), any(), any(), any());
		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, EXCLUDE_TAXES_FROM_BROWSE_PRICE);
		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, EXCLUDE_CHARGE_FROM_BROWSE_PRICE);
		doCallRealMethod().when(roundValueService).execute(any());

		final PriceFact mockedPriceFact = getMockedPriceFact(true, MOCKED_TIME_ZONE_ID_SP, COUNTRY);
		mockedPriceFact.getPromotionalPrice().setValidUntil(LocalDate.now().plusDays(1).toString());

		final BrowsePrice calculatedBrowsePrice = calculateBrowsePriceService.execute(mockedPriceFact, COUNTRY);
		assertThat(calculatedBrowsePrice.getPrice(), is(equalTo(browsePrice.getPrice())));
		assertThat(calculatedBrowsePrice.getOriginalPrice(), is(equalTo(browsePrice.getOriginalPrice())));
		assertThat(calculatedBrowsePrice.getSku(), is(equalTo(browsePrice.getSku())));
		assertThat(calculatedBrowsePrice.getVendorItemId(), is(equalTo(browsePrice.getVendorItemId())));
		assertThat(calculatedBrowsePrice.getValidUntil(), is(equalTo(browsePrice.getValidUntil())));
		verify(roundValueService, times(2)).execute(any());
	}

	@Test
	void shouldReturnTheCorrectListWhenConsideringPromotionalPriceWithValidUntilSameDay() {

		doReturn(true).when(calculateDaysBehindToShowValidUntilHelper)
				.shouldShowValidUntil(getDatePlusDays(0, MOCKED_TIME_ZONE_ID_SP), COUNTRY, MOCKED_TIME_ZONE_ID_SP);

		final BrowsePrice browsePrice = new BrowsePrice();
		browsePrice.setPrice(ITEM_PROMOTIONAL_PRICE_VALUE.add(TAX_VALUE).add(ORDER_ITEM_CHARGE_AMOUNT_VALUE));
		browsePrice.setOriginalPrice(ITEM_BASE_PRICE_VALUE.add(TAX_VALUE).add(ORDER_ITEM_CHARGE_AMOUNT_VALUE));
		browsePrice.setSku(MOCKED_SKU_1);
		browsePrice.setVendorItemId(MOCKED_VENDOR_ITEM_ID_1);
		browsePrice.setValidUntil(LocalDate.now().toString());

		doReturn(TAX_VALUE).when(calculateTaxService).execute(any(), any(), any(), anyString());
		doReturn(ORDER_ITEM_CHARGE_AMOUNT_VALUE).when(calculateOrderItemChargeAmountService).execute(any(), any(), any(), anyString());
		doReturn(ITEM_BASE_PRICE_VALUE).when(calculateItemBasePriceService).execute(any(), anyString(), eq(true));
		doReturn(ITEM_PROMOTIONAL_PRICE_VALUE).when(calculateItemBasePriceService).execute(any(), anyString(), eq(false));
		doReturn(Boolean.FALSE).when(pricingConfigurationService)
				.getPriceConfig(COUNTRY, ROUND_TAXES_AND_DISCOUNT_BEFORE_SUBTOTAL_AND_TOTAL);
		doReturn(ITEM_BASE_PRICE_VALUE).when(calculateTaxBasePriceService).execute(eq(ITEM_BASE_PRICE_VALUE), any(), any(), any());
		doReturn(ITEM_PROMOTIONAL_PRICE_VALUE).when(calculateTaxBasePriceService)
				.execute(eq(ITEM_PROMOTIONAL_PRICE_VALUE), any(), any(), any());
		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, EXCLUDE_TAXES_FROM_BROWSE_PRICE);
		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, EXCLUDE_CHARGE_FROM_BROWSE_PRICE);
		doCallRealMethod().when(roundValueService).execute(any());

		final PriceFact mockedPriceFact = getMockedPriceFact(true, MOCKED_TIME_ZONE_ID_SP, COUNTRY);
		mockedPriceFact.getPromotionalPrice().setValidUntil(LocalDate.now().toString());

		final BrowsePrice calculatedBrowsePrice = calculateBrowsePriceService.execute(mockedPriceFact, COUNTRY);
		assertThat(calculatedBrowsePrice.getPrice(), is(equalTo(browsePrice.getPrice())));
		assertThat(calculatedBrowsePrice.getOriginalPrice(), is(equalTo(browsePrice.getOriginalPrice())));
		assertThat(calculatedBrowsePrice.getSku(), is(equalTo(browsePrice.getSku())));
		assertThat(calculatedBrowsePrice.getVendorItemId(), is(equalTo(browsePrice.getVendorItemId())));
		assertThat(calculatedBrowsePrice.getValidUntil(), is(equalTo(browsePrice.getValidUntil())));
		verify(roundValueService, times(2)).execute(any());
	}

	@Test
	void shouldReturnTheCorrectListWhenConsideringPromotionalPriceNull() {

		final BrowsePrice browsePrice = new BrowsePrice();
		browsePrice.setPrice(ITEM_BASE_PRICE_VALUE.add(TAX_VALUE).add(ORDER_ITEM_CHARGE_AMOUNT_VALUE));
		browsePrice.setOriginalPrice(ITEM_BASE_PRICE_VALUE.add(TAX_VALUE).add(ORDER_ITEM_CHARGE_AMOUNT_VALUE));
		browsePrice.setSku(MOCKED_SKU_1);
		browsePrice.setVendorItemId(MOCKED_VENDOR_ITEM_ID_1);
		browsePrice.setValidUntil(null);

		doReturn(TAX_VALUE).when(calculateTaxService).execute(any(), any(), any(), anyString());
		doReturn(ORDER_ITEM_CHARGE_AMOUNT_VALUE).when(calculateOrderItemChargeAmountService).execute(any(), any(), any(), anyString());
		doReturn(ITEM_BASE_PRICE_VALUE).when(calculateItemBasePriceService).execute(any(), anyString(), anyBoolean());
		doReturn(Boolean.FALSE).when(pricingConfigurationService)
				.getPriceConfig(COUNTRY, ROUND_TAXES_AND_DISCOUNT_BEFORE_SUBTOTAL_AND_TOTAL);
		doReturn(ITEM_BASE_PRICE_VALUE).when(calculateTaxBasePriceService).execute(any(), any(), any(), any());
		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, EXCLUDE_TAXES_FROM_BROWSE_PRICE);
		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, EXCLUDE_CHARGE_FROM_BROWSE_PRICE);
		doCallRealMethod().when(roundValueService).execute(any());

		final PriceFact mockedPriceFact = getMockedPriceFact(false, MOCKED_TIME_ZONE_ID_SP, COUNTRY);
		mockedPriceFact.setPromotionalPrice(null);

		final BrowsePrice calculatedBrowsePrice = calculateBrowsePriceService.execute(mockedPriceFact, COUNTRY);
		assertThat(calculatedBrowsePrice.getPrice(), is(equalTo(browsePrice.getPrice())));
		assertThat(calculatedBrowsePrice.getOriginalPrice(), is(equalTo(null)));
		assertThat(calculatedBrowsePrice.getSku(), is(equalTo(browsePrice.getSku())));
		assertThat(calculatedBrowsePrice.getVendorItemId(), is(equalTo(browsePrice.getVendorItemId())));
		assertThat(calculatedBrowsePrice.getValidUntil(), is(equalTo(browsePrice.getValidUntil())));
		verify(roundValueService, times(2)).execute(any());
	}

	@Test
	void shouldReturnTheCorrectListWhenConsideringPromotionalPriceFilledWithNullPrice() {

		final BrowsePrice browsePrice = new BrowsePrice();
		browsePrice.setPrice(ITEM_BASE_PRICE_VALUE.add(TAX_VALUE).add(ORDER_ITEM_CHARGE_AMOUNT_VALUE));
		browsePrice.setOriginalPrice(ITEM_BASE_PRICE_VALUE.add(TAX_VALUE).add(ORDER_ITEM_CHARGE_AMOUNT_VALUE));
		browsePrice.setSku(MOCKED_SKU_1);
		browsePrice.setVendorItemId(MOCKED_VENDOR_ITEM_ID_1);
		browsePrice.setValidUntil(null);

		doReturn(TAX_VALUE).when(calculateTaxService).execute(any(), any(), any(), anyString());
		doReturn(ORDER_ITEM_CHARGE_AMOUNT_VALUE).when(calculateOrderItemChargeAmountService).execute(any(), any(), any(), anyString());
		doReturn(ITEM_BASE_PRICE_VALUE).when(calculateItemBasePriceService).execute(any(), anyString(), anyBoolean());
		doReturn(Boolean.FALSE).when(pricingConfigurationService)
				.getPriceConfig(COUNTRY, ROUND_TAXES_AND_DISCOUNT_BEFORE_SUBTOTAL_AND_TOTAL);
		doReturn(ITEM_BASE_PRICE_VALUE).when(calculateTaxBasePriceService).execute(any(), any(), any(), any());
		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, EXCLUDE_TAXES_FROM_BROWSE_PRICE);
		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, EXCLUDE_CHARGE_FROM_BROWSE_PRICE);
		doCallRealMethod().when(roundValueService).execute(any());

		final PriceFact mockedPriceFact = getMockedPriceFact(true, MOCKED_TIME_ZONE_ID_SP, COUNTRY);
		mockedPriceFact.getPromotionalPrice().setPrice(null);

		final BrowsePrice calculatedBrowsePrice = calculateBrowsePriceService.execute(mockedPriceFact, COUNTRY);
		assertThat(calculatedBrowsePrice.getPrice(), is(equalTo(browsePrice.getPrice())));
		assertThat(calculatedBrowsePrice.getOriginalPrice(), is(equalTo(null)));
		assertThat(calculatedBrowsePrice.getSku(), is(equalTo(browsePrice.getSku())));
		assertThat(calculatedBrowsePrice.getVendorItemId(), is(equalTo(browsePrice.getVendorItemId())));
		assertThat(calculatedBrowsePrice.getValidUntil(), is(equalTo(browsePrice.getValidUntil())));
		verify(roundValueService, times(2)).execute(any());
	}

	@Test
	void shouldReturnTheCorrectListWhenConsideringPromotionalPriceWithoutValidUntil() {

		final BrowsePrice browsePrice = new BrowsePrice();
		browsePrice.setPrice(ITEM_PROMOTIONAL_PRICE_VALUE.add(TAX_VALUE).add(ORDER_ITEM_CHARGE_AMOUNT_VALUE));
		browsePrice.setOriginalPrice(ITEM_BASE_PRICE_VALUE.add(TAX_VALUE).add(ORDER_ITEM_CHARGE_AMOUNT_VALUE));
		browsePrice.setSku(MOCKED_SKU_1);
		browsePrice.setVendorItemId(MOCKED_VENDOR_ITEM_ID_1);
		browsePrice.setValidUntil(null);

		doReturn(TAX_VALUE).when(calculateTaxService).execute(any(), any(), any(), anyString());
		doReturn(ORDER_ITEM_CHARGE_AMOUNT_VALUE).when(calculateOrderItemChargeAmountService).execute(any(), any(), any(), anyString());
		doReturn(ITEM_BASE_PRICE_VALUE).when(calculateItemBasePriceService).execute(any(), anyString(), eq(true));
		doReturn(ITEM_PROMOTIONAL_PRICE_VALUE).when(calculateItemBasePriceService).execute(any(), anyString(), eq(false));
		doReturn(Boolean.FALSE).when(pricingConfigurationService)
				.getPriceConfig(COUNTRY, ROUND_TAXES_AND_DISCOUNT_BEFORE_SUBTOTAL_AND_TOTAL);
		doReturn(ITEM_BASE_PRICE_VALUE).when(calculateTaxBasePriceService).execute(eq(ITEM_BASE_PRICE_VALUE), any(), any(), any());
		doReturn(ITEM_PROMOTIONAL_PRICE_VALUE).when(calculateTaxBasePriceService)
				.execute(eq(ITEM_PROMOTIONAL_PRICE_VALUE), any(), any(), any());
		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, EXCLUDE_TAXES_FROM_BROWSE_PRICE);
		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY, EXCLUDE_CHARGE_FROM_BROWSE_PRICE);
		doCallRealMethod().when(roundValueService).execute(any());

		final PriceFact mockedPriceFact = getMockedPriceFact(true, MOCKED_TIME_ZONE_ID_SP, COUNTRY);
		mockedPriceFact.getPromotionalPrice().setValidUntil(null);

		final BrowsePrice calculatedBrowsePrice = calculateBrowsePriceService.execute(mockedPriceFact, COUNTRY);
		assertThat(calculatedBrowsePrice.getPrice(), is(equalTo(browsePrice.getPrice())));
		assertThat(calculatedBrowsePrice.getOriginalPrice(), is(equalTo(browsePrice.getOriginalPrice())));
		assertThat(calculatedBrowsePrice.getSku(), is(equalTo(browsePrice.getSku())));
		assertThat(calculatedBrowsePrice.getVendorItemId(), is(equalTo(browsePrice.getVendorItemId())));
		assertThat(calculatedBrowsePrice.getValidUntil(), is(equalTo(browsePrice.getValidUntil())));
		verify(roundValueService, times(2)).execute(any());
	}

	@Test
	void shouldReturnTheCorrectListWhenConsideringPromotionalPriceWithValidUntilOneDayValid() {

		doReturn(true).when(calculateDaysBehindToShowValidUntilHelper)
				.shouldShowValidUntil(getDatePlusDays(2, MOCKED_TIME_ZONE_ID_NY), COUNTRY_US, MOCKED_TIME_ZONE_ID_NY);

		final BrowsePrice browsePrice = new BrowsePrice();
		browsePrice.setPrice(ITEM_PROMOTIONAL_PRICE_VALUE.add(TAX_VALUE).add(ORDER_ITEM_CHARGE_AMOUNT_VALUE));
		browsePrice.setOriginalPrice(ITEM_BASE_PRICE_VALUE.add(TAX_VALUE).add(ORDER_ITEM_CHARGE_AMOUNT_VALUE));
		browsePrice.setSku(MOCKED_SKU_1);
		browsePrice.setValidUntil(LocalDate.now().plusDays(2).toString());

		doReturn(TAX_VALUE).when(calculateTaxService).execute(any(), any(), any(), anyString());
		doReturn(ORDER_ITEM_CHARGE_AMOUNT_VALUE).when(calculateOrderItemChargeAmountService).execute(any(), any(), any(), anyString());
		doReturn(ITEM_BASE_PRICE_VALUE).when(calculateItemBasePriceService).execute(any(), anyString(), eq(true));
		doReturn(ITEM_PROMOTIONAL_PRICE_VALUE).when(calculateItemBasePriceService).execute(any(), anyString(), eq(false));
		doReturn(Boolean.FALSE).when(pricingConfigurationService)
				.getPriceConfig(COUNTRY_US, ROUND_TAXES_AND_DISCOUNT_BEFORE_SUBTOTAL_AND_TOTAL);
		doReturn(ITEM_BASE_PRICE_VALUE).when(calculateTaxBasePriceService).execute(eq(ITEM_BASE_PRICE_VALUE), any(), any(), any());
		doReturn(ITEM_PROMOTIONAL_PRICE_VALUE).when(calculateTaxBasePriceService)
				.execute(eq(ITEM_PROMOTIONAL_PRICE_VALUE), any(), any(), any());
		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY_US, EXCLUDE_TAXES_FROM_BROWSE_PRICE);
		doReturn(Boolean.FALSE).when(pricingConfigurationService).getPriceConfig(COUNTRY_US, EXCLUDE_CHARGE_FROM_BROWSE_PRICE);
		doCallRealMethod().when(roundValueService).execute(any());

		final PriceFact mockedPriceFact = getMockedPriceFact(true, MOCKED_TIME_ZONE_ID_NY, COUNTRY_US);
		mockedPriceFact.getPromotionalPrice().setValidUntil(getDatePlusDays(2, MOCKED_TIME_ZONE_ID_NY));

		final BrowsePrice calculatedBrowsePrice = calculateBrowsePriceService.execute(mockedPriceFact, COUNTRY_US);
		assertThat(calculatedBrowsePrice.getPrice(), is(equalTo(browsePrice.getPrice())));
		assertThat(calculatedBrowsePrice.getOriginalPrice(), is(equalTo(browsePrice.getOriginalPrice())));
		assertThat(calculatedBrowsePrice.getSku(), is(equalTo(browsePrice.getSku())));
		assertThat(calculatedBrowsePrice.getValidUntil(), is(equalTo(browsePrice.getValidUntil())));
		verify(roundValueService, times(2)).execute(any());
	}

	@Test
	void shouldGetAllPricesWhenHasNoValidUntilAndPromotionalPriceIsNull() {

		final BrowsePrice browsePrice = new BrowsePrice();
		browsePrice.setPrice(ITEM_BASE_PRICE_VALUE);
		browsePrice.setOriginalPrice(ITEM_BASE_PRICE_VALUE);
		browsePrice.setSku(MOCKED_SKU_1);
		browsePrice.setValidUntil(null);

		doReturn(ITEM_BASE_PRICE_VALUE).when(calculateItemBasePriceService).execute(any(), anyString(), eq(true));
		doReturn(null).when(calculateItemBasePriceService).execute(any(), anyString(), eq(false));
		doReturn(Boolean.FALSE).when(pricingConfigurationService)
				.getPriceConfig(COUNTRY_US, ROUND_TAXES_AND_DISCOUNT_BEFORE_SUBTOTAL_AND_TOTAL);
		doReturn(Boolean.TRUE).when(pricingConfigurationService).getPriceConfig(COUNTRY_US, EXCLUDE_TAXES_FROM_BROWSE_PRICE);
		doReturn(Boolean.TRUE).when(pricingConfigurationService).getPriceConfig(COUNTRY_US, EXCLUDE_CHARGE_FROM_BROWSE_PRICE);

		final PriceFact mockedPriceFact = getMockedPriceFact(false, MOCKED_TIME_ZONE_ID_NY, COUNTRY_US);

		final BrowsePrice calculatedBrowsePrice = calculateBrowsePriceService.execute(mockedPriceFact, COUNTRY_US);
		assertThat(calculatedBrowsePrice, Is.is(notNullValue()));
		assertThat(calculatedBrowsePrice.getPrice(), is(equalTo(browsePrice.getPrice())));
		assertThat(calculatedBrowsePrice.getOriginalPrice(), is(equalTo(null)));
		assertThat(calculatedBrowsePrice.getSku(), is(equalTo(browsePrice.getSku())));
		assertThat(calculatedBrowsePrice.getValidUntil(), is(equalTo(browsePrice.getValidUntil())));
	}

	private PriceFact getMockedPriceFact(final boolean includePromotionalPrice, final String timezone, final String country) {

		final PriceFact priceFact = new PriceFact();

		priceFact.setSku(MOCKED_SKU_1);
		priceFact.setVendorItemId(MOCKED_VENDOR_ITEM_ID_1);
		priceFact.setDeposit(DEPOSIT_VALUE);
		priceFact.setTaxes(new HashMap<>());
		priceFact.setCharges(new HashMap<>());
		priceFact.setTimezone(timezone);
		priceFact.setCountry(country);

		if (includePromotionalPrice) {
			final PromotionalPriceFact promotionalPrice1 = new PromotionalPriceFact();
			promotionalPrice1.setPrice(BigDecimal.TEN);
			promotionalPrice1.setExternalId(EXTERNAL_ID_01);
			promotionalPrice1.setValidUntil(null);
			priceFact.setPromotionalPrice(promotionalPrice1);
		}

		return priceFact;
	}

	private String getDatePlusDays(final int daysToAdd, final String timezone) {

		final ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(timezone)).plusDays(daysToAdd);
		return zonedDateTime.toLocalDate().toString();
	}

}
