package com.abinbev.b2b.price.api.services;

import static com.abinbev.b2b.price.api.testhelpers.TestConstants.CHARGE;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_ACCOUNT_ID;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_COUNTRY_AR;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_COUNTRY_BR;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_COUNTRY_US;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_EXTERNAL_ID;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_SKU_1;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_SKU_2;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_TIME_ZONE_ID;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_VALID_UNTIL;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_VALID_UNTIL_US;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.PERCENT_TYPE;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.TAX_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abinbev.b2b.price.api.config.properties.PriceUpFrontProperties;
import com.abinbev.b2b.price.api.domain.PriceResultList;
import com.abinbev.b2b.price.api.exceptions.GlobalException;
import com.abinbev.b2b.price.api.helpers.CalculateDaysBehindToShowValidUntilHelper;
import com.abinbev.b2b.price.api.repository.PriceRepository;
import com.abinbev.b2b.price.api.rest.vo.Pagination;
import com.abinbev.b2b.price.api.rest.vo.PriceResponseVo;
import com.abinbev.b2b.price.api.testhelpers.AssertHelper;
import com.abinbev.b2b.price.api.validators.PaginationValidator;
import com.abinbev.b2b.price.domain.model.Charge;
import com.abinbev.b2b.price.domain.model.PriceCompoundKey;
import com.abinbev.b2b.price.domain.model.PriceEntity;
import com.abinbev.b2b.price.domain.model.PromotionalPrice;
import com.abinbev.b2b.price.domain.model.Tax;

@ExtendWith(MockitoExtension.class)
class PriceServiceTest {

	private static final List<String> SKUS = Arrays.asList(MOCKED_SKU_1, MOCKED_SKU_2);
	private static final Pagination PAGINATION = new Pagination(1, 10);

	@Mock
	private PriceRepository priceDAO;
	@Mock
	private PriceUpFrontProperties priceUpFrontProperties;
	@Mock
	private PaginationValidator paginationValidator;
	@Mock
	private CalculateDaysBehindToShowValidUntilHelper calculateDaysBehindToShowValidUntilHelper;
	@InjectMocks
	private PriceService service;

	@Test
	void shouldGetAllPricesWhenNotFoundThrowNotFoundException() {

		when(priceUpFrontProperties.getCountriesEnabled()).thenReturn(MOCKED_COUNTRY_AR);

		mockPriceResult(new PriceResultList());
		doNothing().when(paginationValidator).validatePagination(PAGINATION);

		assertThrows(GlobalException.class, () -> service.getAllPrices(MOCKED_ACCOUNT_ID, MOCKED_COUNTRY_BR, SKUS, PAGINATION));
	}

	@Test
	void shouldGetAllPricesReturnNullWhenThrowNotFoundException() {

		when(priceUpFrontProperties.getCountriesEnabled()).thenReturn(MOCKED_COUNTRY_AR);

		mockPriceResult(null);
		doNothing().when(paginationValidator).validatePagination(PAGINATION);

		assertThrows(GlobalException.class, () -> service.getAllPrices(MOCKED_ACCOUNT_ID, MOCKED_COUNTRY_BR, SKUS, PAGINATION));
	}

	@Test
	void shouldGetAllPricesWithSuccessfullyWhenHaveValidParameters() {

		when(priceUpFrontProperties.getCountriesEnabled()).thenReturn(MOCKED_COUNTRY_AR);

		final Integer listQty = 2;
		mockPriceResult(mockPriceResultList(listQty));

		final PriceResponseVo priceResponseVo = service.getAllPrices(MOCKED_ACCOUNT_ID, MOCKED_COUNTRY_BR, SKUS, PAGINATION);

		assertPriceResponse(priceResponseVo, listQty);
	}

	@Test
	void shouldGetAllPricesWhenPriceUpFrontEnabledOneData() {

		when(priceUpFrontProperties.getCountriesEnabled()).thenReturn(MOCKED_COUNTRY_AR);

		final Integer listQty = 1;
		mockPriceResult(mockPriceResultList(listQty));

		final PriceResponseVo priceResponseVo = service.getAllPrices(MOCKED_ACCOUNT_ID, MOCKED_COUNTRY_AR, SKUS, PAGINATION);

		assertPriceResponse(priceResponseVo, listQty);
	}

	@Test
	void shouldGetAllPricesWhenPriceUpFrontNullOneData() {

		when(priceUpFrontProperties.getCountriesEnabled()).thenReturn(null);

		final Integer listQty = 1;
		mockPriceResult(mockPriceResultList(listQty));

		final PriceResponseVo priceResponseVo = service.getAllPrices(MOCKED_ACCOUNT_ID, MOCKED_COUNTRY_AR, SKUS, PAGINATION);

		assertPriceResponse(priceResponseVo, listQty);
	}

	@Test
	void shouldGetAllPricesShouldBeReturnWhenThereIsNoZoneConfiguredToPriceUpFront() {

		when(priceUpFrontProperties.getCountriesEnabled()).thenReturn(MOCKED_COUNTRY_AR);

		final Integer listQty = 2;
		mockPriceResult(mockPriceResultList(listQty));

		doNothing().when(paginationValidator).validatePagination(PAGINATION);

		final PriceResponseVo priceResponseVo = service.getAllPrices(MOCKED_ACCOUNT_ID, MOCKED_COUNTRY_BR, SKUS, PAGINATION);

		assertPriceResponse(priceResponseVo, listQty);
	}

	@Test
	void shouldGetAllPricesWhenPriceUpFrontEnabledClosestValidFrom() {

		when(priceUpFrontProperties.getCountriesEnabled()).thenReturn(MOCKED_COUNTRY_AR);

		final List<PriceEntity> priceEntities = new ArrayList<>();

		priceEntities.add(createPriceEntity(1, 1, null, null, new GregorianCalendar(2020, Calendar.OCTOBER, 27).toInstant(),
				new GregorianCalendar(2020, Calendar.OCTOBER, 30).getTime(), BigDecimal.valueOf(12.3)));
		priceEntities.add(createPriceEntity(1, 1, null, null, new GregorianCalendar(2020, Calendar.OCTOBER, 29).toInstant(),
				new GregorianCalendar(2020, Calendar.NOVEMBER, 10).getTime(), BigDecimal.valueOf(21.9)));

		final PriceResultList priceResultList = new PriceResultList(priceEntities, new Pagination(1, 10));

		mockPriceResult(priceResultList);

		final PriceResponseVo priceResponseVo = service.getAllPrices(MOCKED_ACCOUNT_ID, MOCKED_COUNTRY_AR, SKUS, PAGINATION);

		assertThat(priceResponseVo, is(notNullValue()));
		assertThat(priceResponseVo.getPrices().size(), equalTo(1));
		assertThat(priceResponseVo.getPrices().get(0).getBasePrice(), equalTo(BigDecimal.valueOf(21.9)));
	}

	@Test
	void shouldGetAllPricesWhenPriceUpFrontEnabledClosestValidFromDifferentSkus() {

		when(priceUpFrontProperties.getCountriesEnabled()).thenReturn(MOCKED_COUNTRY_AR);

		final List<PriceEntity> priceEntities = new ArrayList<>();

		priceEntities.add(createPriceEntity(1, 1, null, null, new GregorianCalendar(2020, Calendar.OCTOBER, 27).toInstant(),
				new GregorianCalendar(2020, Calendar.OCTOBER, 30).getTime(), BigDecimal.valueOf(12.3)));
		priceEntities.add(createPriceEntity(1, 1, null, null, new GregorianCalendar(2020, Calendar.OCTOBER, 29).toInstant(),
				new GregorianCalendar(2020, Calendar.NOVEMBER, 10).getTime(), BigDecimal.valueOf(21.9)));
		priceEntities.add(createPriceEntity(1, 2, null, null, new GregorianCalendar(2020, Calendar.OCTOBER, 27).toInstant(),
				new GregorianCalendar(2020, Calendar.NOVEMBER, 1).getTime(), BigDecimal.valueOf(10.5)));
		priceEntities.add(createPriceEntity(1, 2, null, null, new GregorianCalendar(2020, Calendar.NOVEMBER, 01).toInstant(),
				new GregorianCalendar(2020, Calendar.NOVEMBER, 10).getTime(), BigDecimal.valueOf(19.9)));

		priceEntities.add(new PriceEntity(BigDecimal.valueOf(19.9), "6-PACK", BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, 1,
				new PromotionalPrice(BigDecimal.TEN, MOCKED_EXTERNAL_ID, MOCKED_VALID_UNTIL), null, null,
				new PriceCompoundKey("Account0", "SKU2", new GregorianCalendar(2020, Calendar.NOVEMBER, 01).toInstant()), false,
				MOCKED_COUNTRY_BR, 1245555L, MOCKED_TIME_ZONE_ID, new Date(), new Date(),
				new GregorianCalendar(2020, Calendar.NOVEMBER, 10).getTime()));

		final PriceResultList priceResultList = new PriceResultList(priceEntities, new Pagination(1, 10));

		mockPriceResult(priceResultList);

		final PriceResponseVo priceResponseVo = service.getAllPrices(MOCKED_ACCOUNT_ID, MOCKED_COUNTRY_AR, SKUS, PAGINATION);

		assertThat(priceResponseVo, is(notNullValue()));
		assertThat(priceResponseVo.getPrices().size(), equalTo(2));
		assertThat(priceResponseVo.getPrices().get(0).getBasePrice(), equalTo(BigDecimal.valueOf(21.9)));
		assertThat(priceResponseVo.getPrices().get(1).getBasePrice(), equalTo(BigDecimal.valueOf(19.9)));
	}

	@Test
	void shouldGetCorrectPriceWhenPriceUpFrontEnabledClosestValidFromIncludingFutureDate() {

		when(priceUpFrontProperties.getCountriesEnabled()).thenReturn(MOCKED_COUNTRY_AR);

		final List<PriceEntity> priceEntities = new ArrayList<>();

		priceEntities.add(createPriceEntity(1, 1, null, null, new GregorianCalendar(2020, Calendar.OCTOBER, 27).toInstant(),
				new GregorianCalendar(2020, Calendar.OCTOBER, 30).getTime(), BigDecimal.valueOf(12.3)));
		priceEntities.add(createPriceEntity(1, 1, null, null, new GregorianCalendar(2020, Calendar.OCTOBER, 29).toInstant(),
				new GregorianCalendar(2020, Calendar.NOVEMBER, 10).getTime(), BigDecimal.valueOf(21.9)));
		priceEntities.add(createPriceEntity(1, 1, null, null, new GregorianCalendar(2100, Calendar.OCTOBER, 29).toInstant(),
				new GregorianCalendar(2100, Calendar.NOVEMBER, 10).getTime(), BigDecimal.valueOf(33.9)));

		final PriceResultList priceResultList = new PriceResultList(priceEntities, new Pagination(1, 10));

		mockPriceResult(priceResultList);

		final PriceResponseVo priceResponseVo = service.getAllPrices(MOCKED_ACCOUNT_ID, MOCKED_COUNTRY_AR, SKUS, PAGINATION);

		assertThat(priceResponseVo, is(notNullValue()));
		assertThat(priceResponseVo.getPrices().size(), equalTo(1));
		assertThat(priceResponseVo.getPrices().get(0).getBasePrice(), equalTo(BigDecimal.valueOf(21.9)));
	}

	@Test
	void shouldGetAllPricesWhenPriceUpFrontEnabledClosestThereIsValidFromNull() {

		when(priceUpFrontProperties.getCountriesEnabled()).thenReturn(MOCKED_COUNTRY_AR);

		final List<PriceEntity> priceEntities = new ArrayList<>();

		priceEntities.add(createPriceEntity(1, 1, null, null, null, new GregorianCalendar(2020, Calendar.OCTOBER, 30).getTime(),
				BigDecimal.valueOf(12.3)));
		priceEntities.add(createPriceEntity(1, 1, null, null, new GregorianCalendar(2020, Calendar.OCTOBER, 29).toInstant(),
				new GregorianCalendar(2020, Calendar.NOVEMBER, 10).getTime(), BigDecimal.valueOf(21.9)));

		final PriceResultList priceResultList = new PriceResultList(priceEntities, new Pagination(1, 10));

		mockPriceResult(priceResultList);

		final PriceResponseVo priceResponseVo = service.getAllPrices(MOCKED_ACCOUNT_ID, MOCKED_COUNTRY_AR, SKUS, PAGINATION);

		assertThat(priceResponseVo, is(notNullValue()));
		assertThat(priceResponseVo.getPrices(), hasSize(1));
		assertThat(priceResponseVo.getPrices().get(0).getBasePrice(), equalTo(BigDecimal.valueOf(21.9)));
	}

	@Test
	void shouldGetAllPricesWhenPriceUpFrontEnabledAndValidUntilNull() {

		doReturn(false).when(calculateDaysBehindToShowValidUntilHelper)
				.shouldShowValidUntil(MOCKED_VALID_UNTIL_US, MOCKED_COUNTRY_US, MOCKED_TIME_ZONE_ID);

		when(priceUpFrontProperties.getCountriesEnabled()).thenReturn(MOCKED_COUNTRY_US);

		final List<PriceEntity> priceEntities = new ArrayList<>();

		priceEntities.add(createPriceEntity(1, 1, null, null, null, new GregorianCalendar(2020, Calendar.OCTOBER, 30).getTime(),
				BigDecimal.valueOf(12.3), MOCKED_VALID_UNTIL_US, MOCKED_COUNTRY_US));

		priceEntities.add(createPriceEntity(1, 1, null, null, new GregorianCalendar(2020, Calendar.OCTOBER, 29).toInstant(),
				new GregorianCalendar(2020, Calendar.NOVEMBER, 10).getTime(), BigDecimal.valueOf(21.9), MOCKED_VALID_UNTIL_US,
				MOCKED_COUNTRY_US));

		final PriceResultList priceResultList = new PriceResultList(priceEntities, new Pagination(1, 10));

		mockPriceResult(priceResultList);

		final PriceResponseVo priceResponseVo = service.getAllPrices(MOCKED_ACCOUNT_ID, MOCKED_COUNTRY_US, SKUS, PAGINATION);

		assertThat(priceResponseVo, is(notNullValue()));
		assertThat(priceResponseVo.getPrices(), hasSize(1));
		assertThat(priceResponseVo.getPrices().get(0).getBasePrice(), equalTo(BigDecimal.valueOf(21.9)));
		assertThat(priceResponseVo.getPrices().get(0).getPromotionalPrice().getValidUntil(), is(equalTo(null)));
	}

	@Test
	void shouldGetAllPricesWhenValidUntil() {

		doReturn(MOCKED_COUNTRY_AR).when(priceUpFrontProperties).getCountriesEnabled();
		doReturn(true).when(calculateDaysBehindToShowValidUntilHelper).shouldShowValidUntil(anyString(), anyString(), anyString());

		mockPriceResult(mockPriceResultList(2));

		final PriceResponseVo priceResponseVo = service.getAllPrices(MOCKED_ACCOUNT_ID, MOCKED_COUNTRY_US, SKUS, PAGINATION);

		assertThat(priceResponseVo, is(notNullValue()));
		assertThat(priceResponseVo.getPrices().get(0).getPromotionalPrice().getValidUntil(), is(notNullValue()));
		assertThat(priceResponseVo.getPrices().get(1).getPromotionalPrice().getValidUntil(), is(notNullValue()));
	}

	@Test
	void shouldGetAllPricesWhenHasNoValidUntil() {

		doReturn(MOCKED_COUNTRY_AR).when(priceUpFrontProperties).getCountriesEnabled();
		doReturn(false).when(calculateDaysBehindToShowValidUntilHelper).shouldShowValidUntil(anyString(), anyString(), anyString());

		mockPriceResult(mockPriceResultList(2));

		final PriceResponseVo priceResponseVo = service.getAllPrices(MOCKED_ACCOUNT_ID, MOCKED_COUNTRY_US, SKUS, PAGINATION);

		assertThat(priceResponseVo, is(notNullValue()));
		assertThat(priceResponseVo.getPrices().get(0).getPromotionalPrice().getValidUntil(), is(nullValue()));
		assertThat(priceResponseVo.getPrices().get(1).getPromotionalPrice().getValidUntil(), is(nullValue()));
	}

	@Test
	void shouldGetAllPricesWhenHasNoValidUntilAndPromotionalPriceIsNull() {

		doReturn(MOCKED_COUNTRY_AR).when(priceUpFrontProperties).getCountriesEnabled();

		final var mockedPrice = mockPriceResultList(1);
		mockedPrice.getPriceEntities().get(0).setPromotionalPrice(null);
		mockPriceResult(mockedPrice);

		final PriceResponseVo priceResponseVo = service.getAllPrices(MOCKED_ACCOUNT_ID, MOCKED_COUNTRY_US, SKUS, PAGINATION);

		assertThat(priceResponseVo, is(notNullValue()));
		assertThat(priceResponseVo.getPrices().get(0).getPromotionalPrice(), is(nullValue()));
	}

	@Test
	void shouldGetAllPricesWhenValidUntilAndValidUntilIsNull() {

		doReturn(MOCKED_COUNTRY_AR).when(priceUpFrontProperties).getCountriesEnabled();

		final var mockedPrice = mockPriceResultList(1);
		mockedPrice.getPriceEntities().get(0).getPromotionalPrice().setValidUntil(null);
		mockPriceResult(mockedPrice);

		final PriceResponseVo priceResponseVo = service.getAllPrices(MOCKED_ACCOUNT_ID, MOCKED_COUNTRY_US, SKUS, PAGINATION);

		assertThat(priceResponseVo, is(notNullValue()));
		assertThat(priceResponseVo.getPrices().get(0).getPromotionalPrice().getValidUntil(), is(nullValue()));
	}

	@Test
	void shouldGetAllPricesAndUpdatePaginationWhenPricesAreGroupedBySku() {

		doReturn(MOCKED_COUNTRY_AR).when(priceUpFrontProperties).getCountriesEnabled();

		final PriceEntity price = createPriceEntity(1, 1, null, null, Instant.now(), null, BigDecimal.TEN);
		final PriceEntity price2 = createPriceEntity(1, 1, null, null, Instant.now().minus(1L, ChronoUnit.DAYS), null, BigDecimal.TEN);

		final List<PriceEntity> priceEntities = new ArrayList<>();
		priceEntities.add(price);
		priceEntities.add(price2);

		mockPriceResult(new PriceResultList(priceEntities, new Pagination(1, 2)));

		final PriceResponseVo priceResponseVo = service.getAllPrices(MOCKED_ACCOUNT_ID, MOCKED_COUNTRY_AR, SKUS, PAGINATION);

		assertThat(priceResponseVo.getPrices().size(), is(1));
		assertThat(priceResponseVo.getPagination().getSize(), is(1));
		assertThat(priceResponseVo.getPagination().getTotalElements(), is(1L));
	}

	private void assertPriceResponse(final PriceResponseVo priceResponseVo, final Integer expectedPaginationSize) {

		assertThat(priceResponseVo, is(notNullValue()));
		assertThat(priceResponseVo.getPagination().getPage(), equalTo(1));
		assertThat(priceResponseVo.getPagination().getSize(), equalTo(expectedPaginationSize));

		for (int i = 0; i < priceResponseVo.getPrices().size(); i++) {
			final PriceEntity price = priceResponseVo.getPrices().get(i);
			assertThat(price.getAccountIdPrice(), equalTo("Account" + i));
			assertThat(price.getSkuPrice(), equalTo("SKU" + i));
			assertThat(price.getCountry(), equalTo(MOCKED_COUNTRY_BR));
			assertThat(price.getTimezone(), equalTo(MOCKED_TIME_ZONE_ID));
			assertThat(price.getTimezone(), equalTo(MOCKED_TIME_ZONE_ID));
			assertThat(price.isDeleted(), is(false));
			assertThat(price.getPromotionalPrice(), is(notNullValue()));
			assertThat(price.getPromotionalPrice().getExternalId(), equalTo(MOCKED_EXTERNAL_ID));
			assertThat(price.getPromotionalPrice().getPrice(), equalTo(BigDecimal.TEN));

			assertThat(price.getCharges(), is(notNullValue()));
			assertCharges(price.getCharges(), PERCENT_TYPE, BigDecimal.valueOf(12.5), BigDecimal.valueOf(2));
			assertThat(price.getTaxes(), is(notNullValue()));
			AssertHelper.assertTaxes(TAX_ID, price.getTaxes(), false, PERCENT_TYPE, BigDecimal.valueOf(12.1), BigDecimal.valueOf(15));

			assertThat(price.getUpdatedDate(), is(notNullValue()));
			assertThat(price.getCreatedDate(), is(notNullValue()));
			assertThat(price.getMeasureUnit(), is(notNullValue()));
		}
	}

	private PriceResultList mockPriceResultList(final Integer qty) {

		final Map<String, Charge> charges = mockCharges(PERCENT_TYPE, BigDecimal.valueOf(2), BigDecimal.valueOf(12.5), 2);

		final Map<String, Tax> taxes = AssertHelper
				.mockTaxes(false, BigDecimal.valueOf(12.1), BigDecimal.valueOf(15), TAX_ID, PERCENT_TYPE, null);

		final List<PriceEntity> priceEntities = new ArrayList<>();

		for (int i = 0; i < qty; i++) {
			priceEntities.add(createPriceEntity(i, i, taxes, charges, null, null, BigDecimal.TEN));
		}

		return new PriceResultList(priceEntities, new Pagination(1, qty));
	}

	private PriceEntity createPriceEntity(final int id, final int sku, final Map<String, Tax> taxes, final Map<String, Charge> charges,
			final Instant validFrom, final Date validTo, final BigDecimal basePrice, final String validUntil, final String country) {

		return new PriceEntity(basePrice, "6-PACK", BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, 1,
				new PromotionalPrice(BigDecimal.TEN, MOCKED_EXTERNAL_ID, validUntil), taxes, charges,
				new PriceCompoundKey("Account" + id, "SKU" + sku, validFrom), false, country, 1245555L, MOCKED_TIME_ZONE_ID, new Date(),
				new Date(), validTo);
	}

	private PriceEntity createPriceEntity(final int id, final int sku, final Map<String, Tax> taxes, final Map<String, Charge> charges,
			final Instant validFrom, final Date validTo, final BigDecimal basePrice) {

		return new PriceEntity(basePrice, "6-PACK", BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, 1,
				new PromotionalPrice(BigDecimal.TEN, MOCKED_EXTERNAL_ID, MOCKED_VALID_UNTIL), taxes, charges,
				new PriceCompoundKey("Account" + id, "SKU" + sku, validFrom), false, MOCKED_COUNTRY_BR, 1245555L, MOCKED_TIME_ZONE_ID,
				new Date(), new Date(), validTo);
	}

	private void assertCharges(final Map<String, Charge> charges, final String type, final BigDecimal base, final BigDecimal value) {

		charges.values().forEach(
				charge -> assertAll("Should return all the correct values", () -> assertThat(charge.getType(), equalTo(type)),
						() -> assertThat(charge.getBase(), equalTo(base)), () -> assertThat(charge.getValue(), equalTo(value))));
	}

	private Map<String, Charge> mockCharges(final String type, final BigDecimal value, final BigDecimal base, final Integer quantity) {

		final Map<String, Charge> charges = new HashMap<>();

		for (int i = 0; i < quantity; i++) {
			final Charge chargeFact = new Charge(CHARGE + i, type, value, base);
			charges.put(CHARGE + i, chargeFact);
		}

		return charges;
	}

	private void mockPriceResult(final PriceResultList result) {

		Mockito.when(priceDAO.findPriceByIdFilteringSkus(any(), any(), any(), any(), anyBoolean(), any(Instant.class))).thenReturn(result);
	}
}
