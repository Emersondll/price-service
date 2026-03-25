package com.abinbev.b2b.price.api.services.v2;

import static com.abinbev.b2b.price.api.testhelpers.TestConstants.CHARGE;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_ACCOUNT_ID;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_COUNTRY_AR;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_COUNTRY_BR;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_EXTERNAL_ID;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_PRICE_LIST_ID;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_TIME_ZONE_ID;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_VALID_UNTIL;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_VENDOR_ACCOUNT_ID;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_VENDOR_ID;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_VENDOR_ITEM_ID_1;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_VENDOR_ITEM_ID_2;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
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
import com.abinbev.b2b.price.api.config.properties.ToggleConfigurationProperties;
import com.abinbev.b2b.price.api.domain.v2.PriceResultListV2;
import com.abinbev.b2b.price.api.exceptions.Issue;
import com.abinbev.b2b.price.api.exceptions.IssueEnum;
import com.abinbev.b2b.price.api.exceptions.NotFoundException;
import com.abinbev.b2b.price.api.helpers.CalculateDaysBehindToShowValidUntilHelper;
import com.abinbev.b2b.price.api.repository.PriceEntityV2Repository;
import com.abinbev.b2b.price.api.rest.vo.v2.PaginationResponseVoV2;
import com.abinbev.b2b.price.api.testhelpers.AssertHelper;
import com.abinbev.b2b.price.api.validators.v2.PaginationValidatorV2;
import com.abinbev.b2b.price.domain.model.v2.ChargeV2;
import com.abinbev.b2b.price.domain.model.v2.PriceCompoundKeyV2;
import com.abinbev.b2b.price.domain.model.v2.PriceEntityV2;
import com.abinbev.b2b.price.domain.model.v2.PromotionalPriceV2;
import com.abinbev.b2b.price.domain.model.v2.TaxV2;

@ExtendWith(MockitoExtension.class)
class PriceServiceV2Test {

	// Remove this constant when removing BEESPR_19558
	private static final String CODE_TOGGLE_19558 = "BEESPR_19558";
	private static final List<String> VENDOR_ITEM_IDS = Arrays.asList(MOCKED_VENDOR_ITEM_ID_1, MOCKED_VENDOR_ITEM_ID_2);
	private static final PaginationResponseVoV2 PAGINATION = new PaginationResponseVoV2(1, 10);
	private static final PaginationResponseVoV2 PAGINATION_PAGE = new PaginationResponseVoV2(0, null);
	private static final PaginationResponseVoV2 PAGINATION_PAGE_TOTAL_ELEMENTS = new PaginationResponseVoV2(0, null, 0);
	private static final PaginationResponseVoV2 NO_PAGINATION = null;
	private static final String PRICE_LIST_ID = "PRICE_LIST_ID_001";
	@Mock
	private PriceEntityV2Repository priceRepository;
	@Mock
	private PriceUpFrontProperties priceUpFrontProperties;
	@Mock
	private PaginationValidatorV2 paginationValidator;
	@Mock
	private CalculateDaysBehindToShowValidUntilHelper calculateDaysBehindToShowValidUntilHelper;
	@Mock
	private PricePriorityByTypeService pricePriorityByTypeService;
	@InjectMocks
	private PriceServiceV2 service;
	// Remove this mock when removing BEESPR_19558
	@Mock
	private ToggleConfigurationProperties toggleConfigurationProperties;

	@Test
	void shouldGetAllPricesWhenNotFoundThrowNotFoundException() {

		doReturn(MOCKED_COUNTRY_AR).when(priceUpFrontProperties).getCountriesEnabled();

		mockPriceResult(new PriceResultListV2(List.of(), null));
		doNothing().when(paginationValidator).validatePaginationV2(PAGINATION);

		final NotFoundException notFoundException = assertThrows(NotFoundException.class,
				() -> service.getAllPrices(MOCKED_VENDOR_ID, MOCKED_VENDOR_ACCOUNT_ID, MOCKED_COUNTRY_BR, VENDOR_ITEM_IDS, isNull(),
						PAGINATION));

		final Issue issue = new Issue(IssueEnum.PRICES_NOT_FOUND_V2, MOCKED_VENDOR_ID, MOCKED_VENDOR_ACCOUNT_ID, MOCKED_COUNTRY_BR);
		assertThat(notFoundException.getIssue().getMessage(), is(equalTo(issue.getMessage())));
	}

	@Test
	void shouldGetAllPricesWithPriceListIdInformedWhenNotFoundThrowNotFoundException() {

		doReturn(MOCKED_COUNTRY_AR).when(priceUpFrontProperties).getCountriesEnabled();

		mockPriceResult(new PriceResultListV2(List.of(), null));
		doNothing().when(paginationValidator).validatePaginationV2(PAGINATION);

		final NotFoundException notFoundException = assertThrows(NotFoundException.class,
				() -> service.getAllPrices(MOCKED_VENDOR_ID, MOCKED_VENDOR_ACCOUNT_ID, MOCKED_COUNTRY_BR, VENDOR_ITEM_IDS, PRICE_LIST_ID,
						PAGINATION));

		final Issue issue = new Issue(IssueEnum.PRICES_WITH_PRICE_LIST_ID_NOT_FOUND_V2, MOCKED_VENDOR_ID, MOCKED_VENDOR_ACCOUNT_ID,
				PRICE_LIST_ID, MOCKED_COUNTRY_BR);
		assertThat(notFoundException.getIssue().getMessage(), is(equalTo(issue.getMessage())));
	}

	@Test
	void shouldGetAllPricesSuccessfullyWhenHasValidParameters() {

		doReturn(true).when(toggleConfigurationProperties).isEnabledCodeToggle(CODE_TOGGLE_19558);
		doReturn(MOCKED_COUNTRY_AR).when(priceUpFrontProperties).getCountriesEnabled();

		final Integer qty = 2;
		final PriceResultListV2 priceResultListV2 = mockPriceResultList(2);
		doReturn(priceResultListV2).when(pricePriorityByTypeService).execute(priceResultListV2, MOCKED_COUNTRY_BR);

		mockPriceResult(priceResultListV2);

		final PriceResultListV2 priceResponseVo = service
				.getAllPrices(MOCKED_VENDOR_ID, MOCKED_VENDOR_ACCOUNT_ID, MOCKED_COUNTRY_BR, VENDOR_ITEM_IDS, isNull(), PAGINATION);

		assertPriceResponse(priceResponseVo, qty);
	}

	// Remove this test when the toggle 19558 is removed
	@Test
	void shouldGetAllPricesSuccessfullyWhenHasValidParametersAndToggle19558Off() {

		doReturn(false).when(toggleConfigurationProperties).isEnabledCodeToggle(CODE_TOGGLE_19558);
		doReturn(MOCKED_COUNTRY_AR).when(priceUpFrontProperties).getCountriesEnabled();

		final Integer qty = 2;
		final PriceResultListV2 priceResultListV2 = mockPriceResultList(2);
		doReturn(priceResultListV2).when(pricePriorityByTypeService).execute(priceResultListV2, MOCKED_COUNTRY_BR);

		mockPriceResult(priceResultListV2);

		final PriceResultListV2 priceResponseVo = service
				.getAllPrices(MOCKED_VENDOR_ID, MOCKED_VENDOR_ACCOUNT_ID, MOCKED_COUNTRY_BR, VENDOR_ITEM_IDS, isNull(), PAGINATION);

		assertPriceResponse(priceResponseVo, qty);
	}

	@Test
	void shouldGetAllPricesSuccessfullyWhenHasValidParametersIncludePriceListId() {

		doReturn(true).when(toggleConfigurationProperties).isEnabledCodeToggle(CODE_TOGGLE_19558);
		doReturn(MOCKED_COUNTRY_AR).when(priceUpFrontProperties).getCountriesEnabled();

		final Integer qty = 2;
		final PriceResultListV2 priceResultListV2 = mockPriceResultList(qty);
		doReturn(priceResultListV2).when(pricePriorityByTypeService).execute(priceResultListV2, MOCKED_COUNTRY_BR);

		mockPriceResult(priceResultListV2);

		final PriceResultListV2 priceResponseVo = service
				.getAllPrices(MOCKED_VENDOR_ID, MOCKED_VENDOR_ACCOUNT_ID, MOCKED_COUNTRY_BR, VENDOR_ITEM_IDS, MOCKED_PRICE_LIST_ID,
						PAGINATION);

		assertPriceResponse(priceResponseVo, PAGINATION.getSize());
	}

	@Test
	void shouldGetAllPricesSuccessfullyWhenHasValidParametersIncludePriceListIdNoPagination() {

		doReturn(true).when(toggleConfigurationProperties).isEnabledCodeToggle(CODE_TOGGLE_19558);
		doReturn(MOCKED_COUNTRY_AR).when(priceUpFrontProperties).getCountriesEnabled();

		final PriceResultListV2 priceResultListV2 = mockPriceResultList(2);
		doReturn(priceResultListV2).when(pricePriorityByTypeService).execute(priceResultListV2, MOCKED_COUNTRY_BR);

		mockPriceResult(priceResultListV2);

		final PriceResultListV2 priceResponseVo = service
				.getAllPrices(MOCKED_VENDOR_ID, MOCKED_VENDOR_ACCOUNT_ID, MOCKED_COUNTRY_BR, VENDOR_ITEM_IDS, MOCKED_PRICE_LIST_ID,
						NO_PAGINATION);

		assertThat(priceResponseVo, is(notNullValue()));
		assertThat(priceResponseVo.getPagination().getPage(), equalTo(0));
		assertThat(priceResponseVo.getPagination().getSize(), equalTo(2));
	}

	@Test
	void shouldGetAllPricesSuccessfullyWhenHasValidParametersIncludePriceListIdPaginationWithPage() {

		doReturn(true).when(toggleConfigurationProperties).isEnabledCodeToggle(CODE_TOGGLE_19558);
		doReturn(MOCKED_COUNTRY_AR).when(priceUpFrontProperties).getCountriesEnabled();

		final PriceResultListV2 priceResultListV2 = mockPriceResultList(2);
		doReturn(priceResultListV2).when(pricePriorityByTypeService).execute(priceResultListV2, MOCKED_COUNTRY_BR);

		mockPriceResult(priceResultListV2);

		final PriceResultListV2 priceResponseVo = service
				.getAllPrices(MOCKED_VENDOR_ID, MOCKED_VENDOR_ACCOUNT_ID, MOCKED_COUNTRY_BR, VENDOR_ITEM_IDS, MOCKED_PRICE_LIST_ID,
						PAGINATION_PAGE);

		assertThat(priceResponseVo, is(notNullValue()));
		assertThat(priceResponseVo.getPagination().getPage(), equalTo(0));
		assertThat(priceResponseVo.getPagination().getSize(), equalTo(50));
		assertThat(priceResponseVo.getPagination().getTotalElements(), equalTo(2L));
	}

	@Test
	void shouldGetAllPricesSuccessfullyWhenHasValidParametersIncludePriceListIdPaginationWithPageAndTotalElementsZero() {

		doReturn(true).when(toggleConfigurationProperties).isEnabledCodeToggle(CODE_TOGGLE_19558);
		doReturn(MOCKED_COUNTRY_AR).when(priceUpFrontProperties).getCountriesEnabled();

		final PriceResultListV2 priceResultListV2 = mockPriceResultList(2);
		doReturn(priceResultListV2).when(pricePriorityByTypeService).execute(priceResultListV2, MOCKED_COUNTRY_BR);

		mockPriceResult(priceResultListV2);

		final PriceResultListV2 priceResponseVo = service
				.getAllPrices(MOCKED_VENDOR_ID, MOCKED_VENDOR_ACCOUNT_ID, MOCKED_COUNTRY_BR, VENDOR_ITEM_IDS, MOCKED_PRICE_LIST_ID,
						PAGINATION_PAGE_TOTAL_ELEMENTS);

		assertThat(priceResponseVo, is(notNullValue()));
		assertThat(priceResponseVo.getPagination().getPage(), equalTo(0));
		assertThat(priceResponseVo.getPagination().getSize(), equalTo(2));
		assertThat(priceResponseVo.getPagination().getTotalElements(), equalTo(2L));
	}

	@Test
	void shouldGetAllPricesSuccessfullyWhenHasValidParametersIncludePriceListIdPaginationWithPageAndTotalElements() {

		doReturn(true).when(toggleConfigurationProperties).isEnabledCodeToggle(CODE_TOGGLE_19558);
		doReturn(MOCKED_COUNTRY_AR).when(priceUpFrontProperties).getCountriesEnabled();

		final PriceResultListV2 priceResultListV2 = mockPriceResultList(2);
		doReturn(priceResultListV2).when(pricePriorityByTypeService).execute(priceResultListV2, MOCKED_COUNTRY_BR);

		mockPriceResult(priceResultListV2);

		final PriceResultListV2 priceResponseVo = service
				.getAllPrices(MOCKED_VENDOR_ID, MOCKED_VENDOR_ACCOUNT_ID, MOCKED_COUNTRY_BR, VENDOR_ITEM_IDS, MOCKED_PRICE_LIST_ID,
						new PaginationResponseVoV2(null, null, 1));

		assertThat(priceResponseVo, is(notNullValue()));
		assertThat(priceResponseVo.getPagination().getPage(), equalTo(0));
		assertThat(priceResponseVo.getPagination().getSize(), equalTo(2));
		assertThat(priceResponseVo.getPagination().getTotalElements(), equalTo(1L));
	}

	@Test
	void shouldGetAllPricesWhenPriceUpFrontEnabledOneData() {

		doReturn(true).when(toggleConfigurationProperties).isEnabledCodeToggle(CODE_TOGGLE_19558);
		doReturn(MOCKED_COUNTRY_AR).when(priceUpFrontProperties).getCountriesEnabled();

		final Integer qty = 1;
		final PriceResultListV2 priceResultListV2 = mockPriceResultList(qty);
		doReturn(priceResultListV2).when(pricePriorityByTypeService).execute(priceResultListV2, MOCKED_COUNTRY_AR);

		mockPriceResult(priceResultListV2);

		final PriceResultListV2 priceResponseVo = service
				.getAllPrices(MOCKED_VENDOR_ID, MOCKED_VENDOR_ACCOUNT_ID, MOCKED_COUNTRY_AR, VENDOR_ITEM_IDS, isNull(), PAGINATION);

		assertPriceResponse(priceResponseVo, qty);
	}

	@Test
	void shouldGetAllPricesWhenPriceUpFrontDisabled() {

		doReturn(true).when(toggleConfigurationProperties).isEnabledCodeToggle(CODE_TOGGLE_19558);
		doReturn("").when(priceUpFrontProperties).getCountriesEnabled();

		final Integer qty = 1;
		final PriceResultListV2 priceResultListV2 = mockPriceResultList(qty);
		doReturn(priceResultListV2).when(pricePriorityByTypeService).execute(priceResultListV2, MOCKED_COUNTRY_AR);

		mockPriceResult(priceResultListV2);

		final PriceResultListV2 priceResponseVo = service
				.getAllPrices(MOCKED_VENDOR_ID, MOCKED_VENDOR_ACCOUNT_ID, MOCKED_COUNTRY_AR, VENDOR_ITEM_IDS, isNull(), PAGINATION);

		assertPriceResponse(priceResponseVo, qty);
	}

	@Test
	void shouldGetAllPricesShouldBeReturnWhenThereIsNoZoneConfiguredToPriceUpFront() {

		doReturn(true).when(toggleConfigurationProperties).isEnabledCodeToggle(CODE_TOGGLE_19558);
		doReturn(MOCKED_COUNTRY_AR).when(priceUpFrontProperties).getCountriesEnabled();

		final Integer qty = 2;
		final PriceResultListV2 priceResultListV2 = mockPriceResultList(qty);
		doReturn(priceResultListV2).when(pricePriorityByTypeService).execute(priceResultListV2, MOCKED_COUNTRY_BR);

		mockPriceResult(priceResultListV2);

		doNothing().when(paginationValidator).validatePaginationV2(PAGINATION);

		final PriceResultListV2 priceResponseVo = service
				.getAllPrices(MOCKED_VENDOR_ID, MOCKED_VENDOR_ACCOUNT_ID, MOCKED_COUNTRY_BR, VENDOR_ITEM_IDS, isNull(), PAGINATION);

		assertPriceResponse(priceResponseVo, qty);
	}

	@Test
	void shouldGetAllPricesWhenPriceUpFrontEnabledClosestValidFrom() {
		// Remove this mock when removing BEESPR_19558
		doReturn(true).when(toggleConfigurationProperties).isEnabledCodeToggle(CODE_TOGGLE_19558);

		doReturn(MOCKED_COUNTRY_AR).when(priceUpFrontProperties).getCountriesEnabled();

		final List<PriceEntityV2> priceEntities = new ArrayList<>();

		priceEntities.add(createPriceEntity(1, 1, null, null, null, new GregorianCalendar(2020, Calendar.OCTOBER, 27).toInstant(),
				new GregorianCalendar(2020, Calendar.OCTOBER, 30).getTime(), BigDecimal.valueOf(12.3)));
		priceEntities.add(createPriceEntity(1, 1, null, null, null, new GregorianCalendar(2020, Calendar.OCTOBER, 29).toInstant(),
				new GregorianCalendar(2020, Calendar.NOVEMBER, 10).getTime(), BigDecimal.valueOf(21.9)));

		final PriceResultListV2 priceResultList = new PriceResultListV2(priceEntities, new PaginationResponseVoV2(1, 10));

		doReturn(priceResultList).when(pricePriorityByTypeService).execute(priceResultList, MOCKED_COUNTRY_AR);

		mockPriceResult(priceResultList);

		final PriceResultListV2 priceResponseVo = service
				.getAllPrices(MOCKED_VENDOR_ID, MOCKED_VENDOR_ACCOUNT_ID, MOCKED_COUNTRY_AR, VENDOR_ITEM_IDS, isNull(), PAGINATION);

		assertThat(priceResponseVo, is(notNullValue()));
		assertThat(priceResponseVo.getPriceEntities().size(), equalTo(1));
		assertThat(priceResponseVo.getPriceEntities().get(0).getBasePrice(), equalTo(BigDecimal.valueOf(21.9)));
	}

	@Test
	void shouldGetCorrectPriceWhenPriceUpFrontEnabledClosestValidFromIncludingFutureDate() {
		// Remove this mock when removing BEESPR_19558
		doReturn(true).when(toggleConfigurationProperties).isEnabledCodeToggle(CODE_TOGGLE_19558);

		doReturn(MOCKED_COUNTRY_AR).when(priceUpFrontProperties).getCountriesEnabled();

		final List<PriceEntityV2> priceEntities = new ArrayList<>();

		priceEntities.add(createPriceEntity(1, 1, null, null, null, new GregorianCalendar(2020, Calendar.OCTOBER, 27).toInstant(),
				new GregorianCalendar(2020, Calendar.OCTOBER, 30).getTime(), BigDecimal.valueOf(12.3)));
		priceEntities.add(createPriceEntity(1, 1, null, null, null, new GregorianCalendar(2020, Calendar.OCTOBER, 29).toInstant(),
				new GregorianCalendar(2020, Calendar.NOVEMBER, 10).getTime(), BigDecimal.valueOf(21.9)));
		priceEntities.add(createPriceEntity(1, 1, null, null, null, new GregorianCalendar(2100, Calendar.OCTOBER, 29).toInstant(),
				new GregorianCalendar(2100, Calendar.NOVEMBER, 10).getTime(), BigDecimal.valueOf(35.9)));

		final PriceResultListV2 priceResultList = new PriceResultListV2(priceEntities, new PaginationResponseVoV2(1, 10));

		doReturn(priceResultList).when(pricePriorityByTypeService).execute(priceResultList, MOCKED_COUNTRY_AR);

		mockPriceResult(priceResultList);

		final PriceResultListV2 priceResponseVo = service
				.getAllPrices(MOCKED_VENDOR_ID, MOCKED_VENDOR_ACCOUNT_ID, MOCKED_COUNTRY_AR, VENDOR_ITEM_IDS, isNull(), PAGINATION);

		assertThat(priceResponseVo, is(notNullValue()));
		assertThat(priceResponseVo.getPriceEntities().size(), equalTo(1));
		assertThat(priceResponseVo.getPriceEntities().get(0).getBasePrice(), equalTo(BigDecimal.valueOf(21.9)));
	}

	@Test
	void shouldGetAllPricesWhenPriceUpFrontEnabledClosestValidFromDifferentSkus() {
		// Remove this mock when removing BEESPR_19558
		doReturn(true).when(toggleConfigurationProperties).isEnabledCodeToggle(CODE_TOGGLE_19558);

		doReturn(MOCKED_COUNTRY_AR).when(priceUpFrontProperties).getCountriesEnabled();

		final List<PriceEntityV2> priceEntities = new ArrayList<>();

		priceEntities.add(createPriceEntity(1, 1, null, null, null, new GregorianCalendar(2020, Calendar.OCTOBER, 27).toInstant(),
				new GregorianCalendar(2020, Calendar.OCTOBER, 30).getTime(), BigDecimal.valueOf(12.3)));
		priceEntities.add(createPriceEntity(1, 1, null, null, null, new GregorianCalendar(2020, Calendar.OCTOBER, 29).toInstant(),
				new GregorianCalendar(2020, Calendar.NOVEMBER, 10).getTime(), BigDecimal.valueOf(21.9)));
		priceEntities.add(createPriceEntity(1, 2, null, null, null, new GregorianCalendar(2020, Calendar.OCTOBER, 27).toInstant(),
				new GregorianCalendar(2020, Calendar.NOVEMBER, 1).getTime(), BigDecimal.valueOf(10.5)));
		priceEntities.add(createPriceEntity(1, 2, null, null, null, new GregorianCalendar(2020, Calendar.NOVEMBER, 01).toInstant(),
				new GregorianCalendar(2020, Calendar.NOVEMBER, 10).getTime(), BigDecimal.valueOf(19.9)));

		final PriceEntityV2 entityV2 = new PriceEntityV2();
		entityV2.setBasePrice(BigDecimal.valueOf(19.9));
		entityV2.setMeasureUnit("6-PACK");
		entityV2.setMinimumPrice(BigDecimal.ONE);
		entityV2.setDeposit(BigDecimal.ONE);
		entityV2.setConsignment(BigDecimal.ONE);
		entityV2.setQuantityPerPallet(1);
		entityV2.setPromotionalPrice(new PromotionalPriceV2(BigDecimal.TEN, MOCKED_EXTERNAL_ID, MOCKED_VALID_UNTIL));
		entityV2.setTaxes(null);
		entityV2.setCharges(null);
		entityV2.setId(createCompoundKeyV2(0, 2, new GregorianCalendar(2020, Calendar.NOVEMBER, 01).toInstant()));
		entityV2.setDeleted(false);
		entityV2.setCountry(MOCKED_COUNTRY_BR);
		entityV2.setTimestamp(1245555L);
		entityV2.setTimezone(MOCKED_TIME_ZONE_ID);
		entityV2.setCreatedDate(new Date());
		entityV2.setUpdatedDate(new Date());
		entityV2.setValidTo(new GregorianCalendar(2020, Calendar.NOVEMBER, 10).getTime());
		priceEntities.add(entityV2);

		final PriceResultListV2 priceResultList = new PriceResultListV2(priceEntities, new PaginationResponseVoV2(1, 10));
		mockPriceResult(priceResultList);

		doReturn(priceResultList).when(pricePriorityByTypeService).execute(priceResultList, MOCKED_COUNTRY_AR);

		final PriceResultListV2 priceResponseVo = service
				.getAllPrices(MOCKED_VENDOR_ID, MOCKED_VENDOR_ACCOUNT_ID, MOCKED_COUNTRY_AR, VENDOR_ITEM_IDS, isNull(), PAGINATION);

		assertThat(priceResponseVo, is(notNullValue()));
		assertThat(priceResponseVo.getPriceEntities().size(), equalTo(2));
		assertThat(priceResponseVo.getPriceEntities().get(1).getBasePrice(), equalTo(BigDecimal.valueOf(21.9)));
		assertThat(priceResponseVo.getPriceEntities().get(0).getBasePrice(), equalTo(BigDecimal.valueOf(19.9)));
	}

	@Test
	void shouldGetAllPricesWhenPriceUpFrontEnabledClosestThereIsValidFromNull() {
		// Remove this mock when removing BEESPR_19558
		doReturn(true).when(toggleConfigurationProperties).isEnabledCodeToggle(CODE_TOGGLE_19558);

		doReturn(MOCKED_COUNTRY_AR).when(priceUpFrontProperties).getCountriesEnabled();

		final List<PriceEntityV2> priceEntities = new ArrayList<>();

		priceEntities.add(createPriceEntity(1, 1, null, null, null, null, new GregorianCalendar(2020, Calendar.OCTOBER, 30).getTime(),
				BigDecimal.valueOf(12.3)));
		priceEntities.add(createPriceEntity(1, 1, null, null, null, new GregorianCalendar(2020, Calendar.OCTOBER, 29).toInstant(),
				new GregorianCalendar(2020, Calendar.NOVEMBER, 10).getTime(), BigDecimal.valueOf(21.9)));

		final PriceResultListV2 priceResultList = new PriceResultListV2(priceEntities, new PaginationResponseVoV2(1, 10));

		mockPriceResult(priceResultList);

		doReturn(priceResultList).when(pricePriorityByTypeService).execute(priceResultList, MOCKED_COUNTRY_AR);

		final PriceResultListV2 priceResponseVo = service
				.getAllPrices(MOCKED_VENDOR_ID, MOCKED_VENDOR_ACCOUNT_ID, MOCKED_COUNTRY_AR, VENDOR_ITEM_IDS, isNull(), PAGINATION);

		assertThat(priceResponseVo, is(notNullValue()));
		assertThat(priceResponseVo.getPriceEntities(), hasSize(1));
		assertThat(priceResponseVo.getPriceEntities().get(0).getBasePrice(), equalTo(BigDecimal.valueOf(21.9)));
	}

	@Test
	void shouldGetAllPricesWhenValidUntil() {

		doReturn(true).when(toggleConfigurationProperties).isEnabledCodeToggle(CODE_TOGGLE_19558);
		doReturn(MOCKED_COUNTRY_AR).when(priceUpFrontProperties).getCountriesEnabled();
		doReturn(true).when(calculateDaysBehindToShowValidUntilHelper).shouldShowValidUntil(anyString(), anyString(), anyString());

		final PriceResultListV2 priceResultListV2 = mockPriceResultList(2);
		doReturn(priceResultListV2).when(pricePriorityByTypeService).execute(priceResultListV2, MOCKED_COUNTRY_BR);

		mockPriceResult(priceResultListV2);

		final PriceResultListV2 priceResponseVo = service
				.getAllPrices(MOCKED_VENDOR_ID, MOCKED_VENDOR_ACCOUNT_ID, MOCKED_COUNTRY_BR, VENDOR_ITEM_IDS, isNull(), PAGINATION);

		assertThat(priceResponseVo, is(notNullValue()));
		assertThat(priceResponseVo.getPriceEntities().get(0).getPromotionalPrice().getValidUntil(), is(notNullValue()));
		assertThat(priceResponseVo.getPriceEntities().get(1).getPromotionalPrice().getValidUntil(), is(notNullValue()));
	}

	@Test
	void shouldGetAllPricesWhenHasNoValidUntil() {

		doReturn(true).when(toggleConfigurationProperties).isEnabledCodeToggle(CODE_TOGGLE_19558);
		doReturn(MOCKED_COUNTRY_AR).when(priceUpFrontProperties).getCountriesEnabled();
		doReturn(false).when(calculateDaysBehindToShowValidUntilHelper).shouldShowValidUntil(anyString(), anyString(), anyString());

		final PriceResultListV2 priceResultListV2 = mockPriceResultList(2);
		doReturn(priceResultListV2).when(pricePriorityByTypeService).execute(priceResultListV2, MOCKED_COUNTRY_BR);

		mockPriceResult(priceResultListV2);

		final PriceResultListV2 priceResponseVo = service
				.getAllPrices(MOCKED_VENDOR_ID, MOCKED_VENDOR_ACCOUNT_ID, MOCKED_COUNTRY_BR, VENDOR_ITEM_IDS, isNull(), PAGINATION);

		assertThat(priceResponseVo, is(notNullValue()));
		assertThat(priceResponseVo.getPriceEntities().get(0).getPromotionalPrice().getValidUntil(), is(nullValue()));
		assertThat(priceResponseVo.getPriceEntities().get(1).getPromotionalPrice().getValidUntil(), is(nullValue()));
	}

	@Test
	void shouldGetAllPricesWhenHasNoValidUntilAndPromotionalPriceIsNull() {

		doReturn(true).when(toggleConfigurationProperties).isEnabledCodeToggle(CODE_TOGGLE_19558);
		doReturn(MOCKED_COUNTRY_AR).when(priceUpFrontProperties).getCountriesEnabled();

		final PriceResultListV2 priceResultListV2 = mockPriceResultList(1);
		doReturn(priceResultListV2).when(pricePriorityByTypeService).execute(priceResultListV2, MOCKED_COUNTRY_BR);

		priceResultListV2.getPriceEntities().get(0).setPromotionalPrice(null);
		mockPriceResult(priceResultListV2);

		final PriceResultListV2 priceResponseVo = service
				.getAllPrices(MOCKED_VENDOR_ID, MOCKED_VENDOR_ACCOUNT_ID, MOCKED_COUNTRY_BR, VENDOR_ITEM_IDS, isNull(), PAGINATION);

		assertThat(priceResponseVo, is(notNullValue()));
		assertThat(priceResponseVo.getPriceEntities().get(0).getPromotionalPrice(), is(nullValue()));
	}

	@Test
	void shouldGetAllPricesWhenValidUntilAndValidUntilIsNull() {

		doReturn(true).when(toggleConfigurationProperties).isEnabledCodeToggle(CODE_TOGGLE_19558);
		doReturn(MOCKED_COUNTRY_AR).when(priceUpFrontProperties).getCountriesEnabled();

		final PriceResultListV2 priceResultListV2 = mockPriceResultList(1);
		doReturn(priceResultListV2).when(pricePriorityByTypeService).execute(priceResultListV2, MOCKED_COUNTRY_BR);

		priceResultListV2.getPriceEntities().get(0).getPromotionalPrice().setValidUntil(null);
		mockPriceResult(priceResultListV2);

		final PriceResultListV2 priceResponseVo = service
				.getAllPrices(MOCKED_VENDOR_ID, MOCKED_VENDOR_ACCOUNT_ID, MOCKED_COUNTRY_BR, VENDOR_ITEM_IDS, isNull(), PAGINATION);

		assertThat(priceResponseVo, is(notNullValue()));
		assertThat(priceResponseVo.getPriceEntities().get(0).getPromotionalPrice().getValidUntil(), is(nullValue()));
	}

	private void assertPriceResponse(final PriceResultListV2 priceResponseVo, final Integer size) {

		assertThat(priceResponseVo, is(notNullValue()));
		assertThat(priceResponseVo.getPagination().getPage(), equalTo(1));
		assertThat(priceResponseVo.getPagination().getSize(), equalTo(size));

		for (int i = 0; i < priceResponseVo.getPriceEntities().size(); i++) {
			final PriceEntityV2 price = priceResponseVo.getPriceEntities().get(i);
			assertThat(price.getVendorAccountIdPrice(), equalTo("VendorAccountId" + i));
			assertThat(price.getVendorIdPrice(), equalTo("VendorId" + i));
			assertThat(price.getVendorItemIdPrice(), equalTo("VendorItemId" + i));
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
			AssertHelper.assertTaxesV2(TAX_ID, price.getTaxes(), false, PERCENT_TYPE, BigDecimal.valueOf(12.1), BigDecimal.valueOf(15));

			assertThat(price.getUpdatedDate(), is(notNullValue()));
			assertThat(price.getCreatedDate(), is(notNullValue()));
			assertThat(price.getMeasureUnit(), is(notNullValue()));
		}
	}

	@Test
	void shouldGetAllPricesAndUpdatePaginationWhenPricesAreGroupedBySku() {

		doReturn(true).when(toggleConfigurationProperties).isEnabledCodeToggle(CODE_TOGGLE_19558);
		doReturn(MOCKED_COUNTRY_AR).when(priceUpFrontProperties).getCountriesEnabled();

		final PriceEntityV2 price = createPriceEntity(1, 1, null, null, null, Instant.now(),
				null, BigDecimal.TEN);
		final PriceEntityV2 price2 = createPriceEntity(1, 1, null, null, null,
				Instant.now().minus(1L, ChronoUnit.DAYS), null, BigDecimal.TEN);

		final List<PriceEntityV2> priceEntities = new ArrayList<>();
		priceEntities.add(price);
		priceEntities.add(price2);

		final PriceResultListV2 priceResultListV2 = new PriceResultListV2(priceEntities, new PaginationResponseVoV2(1, 2));
		mockPriceResult(priceResultListV2);
		doReturn(priceResultListV2).when(pricePriorityByTypeService).execute(priceResultListV2, MOCKED_COUNTRY_AR);

		final PriceResultListV2 priceResultListResponse = service.getAllPrices(MOCKED_VENDOR_ID, MOCKED_ACCOUNT_ID,
				MOCKED_COUNTRY_AR, VENDOR_ITEM_IDS, isNull(), PAGINATION);

		assertThat(priceResultListResponse.getPriceEntities().size(), is(1));
		assertThat(priceResultListResponse.getPagination().getSize(), is(1));
		assertThat(priceResultListResponse.getPagination().getTotalElements(), is(1L));
	}

	private PriceResultListV2 mockPriceResultList(final Integer qty) {

		final Map<String, ChargeV2> charges = mockCharges(PERCENT_TYPE, BigDecimal.valueOf(2), BigDecimal.valueOf(12.5), 2);

		final Map<String, TaxV2> taxes = AssertHelper
				.mockTaxesV2(false, BigDecimal.valueOf(12.1), BigDecimal.valueOf(15), TAX_ID, PERCENT_TYPE, null);

		final Map<String, Integer> measureUnit = new HashMap<>();
		measureUnit.put("6PACK", 6);
		measureUnit.put("LITER", 1);
		measureUnit.put("CASE", 30);

		final List<PriceEntityV2> priceEntitiesV2 = new ArrayList<>();

		for (int i = 0; i < qty; i++) {
			priceEntitiesV2.add(createPriceEntity(i, i, taxes, charges, measureUnit, null, null, BigDecimal.TEN));
		}

		return new PriceResultListV2(priceEntitiesV2, new PaginationResponseVoV2(1, qty));
	}

	@Test
	void shouldGetAllPricesWhenPriceUpFrontEnabledClosestValidFromAndValidToAreNull() {
		// Remove this mock when removing BEESPR_19558
		when(toggleConfigurationProperties.isEnabledCodeToggle(CODE_TOGGLE_19558)).thenReturn(true);

		final List<PriceEntityV2> priceEntities = new ArrayList<>();

		priceEntities.add(createPriceEntity(1, 1, null, null, null, null, null, BigDecimal.valueOf(12.3)));
		priceEntities.add(createPriceEntity(1, 1, null, null, null, new GregorianCalendar(2020, Calendar.OCTOBER, 29).toInstant(),
				new GregorianCalendar(2020, Calendar.NOVEMBER, 10).getTime(), BigDecimal.valueOf(21.9)));

		final PriceResultListV2 priceResultList = new PriceResultListV2(priceEntities, new PaginationResponseVoV2(1, 10));

		doReturn(priceResultList).when(pricePriorityByTypeService).execute(priceResultList, MOCKED_COUNTRY_AR);

		mockPriceResult(priceResultList);

		final PriceResultListV2 priceResponseVo = service
				.getAllPrices(MOCKED_VENDOR_ID, MOCKED_VENDOR_ACCOUNT_ID, MOCKED_COUNTRY_AR, VENDOR_ITEM_IDS, isNull(), PAGINATION);

		assertThat(priceResponseVo, is(notNullValue()));
		assertThat(priceResponseVo.getPriceEntities().size(), equalTo(1));
		assertThat(priceResponseVo.getPriceEntities().get(0).getBasePrice(), equalTo(BigDecimal.valueOf(12.3)));
	}

	// Remove this test when removing BEESPR_19558
	@Test
	void shouldGetAllPricesWhenPriceUpFrontEnabledClosestValidFromAndValidToAreNullAndCodeToggleBeespr19558IsDisabled() {

		doReturn(MOCKED_COUNTRY_AR).when(priceUpFrontProperties).getCountriesEnabled();

		final List<PriceEntityV2> priceEntities = new ArrayList<>();

		priceEntities.add(createPriceEntity(1, 1, null, null, null, null, null, BigDecimal.valueOf(12.3)));
		priceEntities.add(createPriceEntity(1, 1, null, null, null, new GregorianCalendar(2020, Calendar.OCTOBER, 29).toInstant(),
				new GregorianCalendar(2020, Calendar.NOVEMBER, 10).getTime(), BigDecimal.valueOf(21.9)));
		priceEntities.add(createPriceEntity(1, 1, null, null, null, new GregorianCalendar(2020, Calendar.OCTOBER, 29).toInstant(),
				new GregorianCalendar(2021, Calendar.NOVEMBER, 10).getTime(), BigDecimal.valueOf(28.5)));

		final PriceResultListV2 priceResultList = new PriceResultListV2(priceEntities, new PaginationResponseVoV2(1, 10));

		doReturn(priceResultList).when(pricePriorityByTypeService).execute(priceResultList, MOCKED_COUNTRY_AR);

		mockPriceResult(priceResultList);

		final PriceResultListV2 priceResponseVo = service
				.getAllPrices(MOCKED_VENDOR_ID, MOCKED_VENDOR_ACCOUNT_ID, MOCKED_COUNTRY_AR, VENDOR_ITEM_IDS, isNull(), PAGINATION);

		assertThat(priceResponseVo, is(notNullValue()));
		assertThat(priceResponseVo.getPriceEntities().size(), equalTo(1));
		assertThat(priceResponseVo.getPriceEntities().get(0).getBasePrice(), equalTo(BigDecimal.valueOf(21.9)));
	}

	private PriceEntityV2 createPriceEntity(final int id, final int itemId, final Map<String, TaxV2> taxes,
			final Map<String, ChargeV2> charges, final Map<String, Integer> measureUnit, final Instant validFrom, final Date validTo,
			final BigDecimal basePrice) {

		final PriceEntityV2 entityV2 = new PriceEntityV2();
		entityV2.setBasePrice(basePrice);
		entityV2.setMeasureUnit("6-PACK");
		entityV2.setMinimumPrice(BigDecimal.ONE);
		entityV2.setDeposit(BigDecimal.ONE);
		entityV2.setConsignment(BigDecimal.ONE);
		entityV2.setQuantityPerPallet(1);
		entityV2.setPromotionalPrice(new PromotionalPriceV2(BigDecimal.TEN, MOCKED_EXTERNAL_ID, MOCKED_VALID_UNTIL));
		entityV2.setTaxes(taxes);
		entityV2.setCharges(charges);
		entityV2.setId(createCompoundKeyV2(id, itemId, validFrom));
		entityV2.setDeleted(false);
		entityV2.setCountry(MOCKED_COUNTRY_BR);
		entityV2.setTimestamp(1245555L);
		entityV2.setTimezone(MOCKED_TIME_ZONE_ID);
		entityV2.setCreatedDate(new Date());
		entityV2.setUpdatedDate(new Date());
		entityV2.setValidTo(validTo);

		return entityV2;
	}

	private PriceCompoundKeyV2 createCompoundKeyV2(final int id, final int itemId, final Instant validFrom) {

		final PriceCompoundKeyV2 compoundKeyV2 = new PriceCompoundKeyV2();
		compoundKeyV2.setVendorId("VendorId" + id);
		compoundKeyV2.setId("VendorAccountId" + id);
		compoundKeyV2.setVendorItemId("VendorItemId" + itemId);
		compoundKeyV2.setValidFrom(validFrom);
		return compoundKeyV2;
	}

	private void assertCharges(final Map<String, ChargeV2> map, final String type, final BigDecimal base, final BigDecimal value) {

		map.values().forEach(charge -> assertAll("Should return all the correct values", () -> assertThat(charge.getType(), equalTo(type)),
				() -> assertThat(charge.getBase(), equalTo(base)), () -> assertThat(charge.getValue(), equalTo(value))));
	}

	private Map<String, ChargeV2> mockCharges(final String type, final BigDecimal value, final BigDecimal base, final Integer quantity) {

		final Map<String, ChargeV2> charges = new HashMap<>();

		for (int i = 0; i < quantity; i++) {
			final ChargeV2 chargeFact = new ChargeV2(CHARGE + i, type, value, base);
			charges.put(CHARGE + i, chargeFact);
		}

		return charges;
	}

	private void mockPriceResult(final PriceResultListV2 result) {

		Mockito.when(
						priceRepository.findPriceByIdFilteringVendorItemId(any(), any(), any(), any(), any(),
								any(Instant.class), any()))
				.thenReturn(result);
	}
}
