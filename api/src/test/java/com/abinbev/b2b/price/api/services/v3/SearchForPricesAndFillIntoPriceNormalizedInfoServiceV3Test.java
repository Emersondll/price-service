package com.abinbev.b2b.price.api.services.v3;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsIterableContaining.hasItems;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abinbev.b2b.price.api.config.properties.PriceUpFrontProperties;
import com.abinbev.b2b.price.api.config.properties.ToggleConfigurationProperties;
import com.abinbev.b2b.price.api.domain.v3.PriceNormalizedInfo;
import com.abinbev.b2b.price.api.helpers.CalculateDaysBehindToShowValidUntilHelper;
import com.abinbev.b2b.price.api.repository.PriceEntityV2Repository;
import com.abinbev.b2b.price.domain.model.v2.PriceCompoundKeyV2;
import com.abinbev.b2b.price.domain.model.v2.PriceEntityV2;
import com.abinbev.b2b.price.domain.model.v2.PromotionalPriceV2;
import com.abinbev.b2b.price.domain.model.v2.enums.PriceMeasureUnitType;

@ExtendWith(MockitoExtension.class)
class SearchForPricesAndFillIntoPriceNormalizedInfoServiceV3Test {

	private static final String CODE_TOGGLE_19558 = "BEESPR_19558";
	private static final String COUNTRY_AR = "AR";
	private static final String COUNTRY_BR = "BR";

	@Mock
	private PriceEntityV2Repository priceEntityV2Repository;
	@Mock
	private PriceUpFrontProperties priceUpFrontProperties;
	@Mock
	private ObtainPricesWithClosestDatesService obtainPricesWithClosestDatesService;
	@Mock
	private PrioritizePriceByTypeServiceV3 prioritizePriceByTypeServiceV3;
	@Mock
	private CalculateDaysBehindToShowValidUntilHelper calculateDaysBehindToShowValidUntilHelper;
	@Mock
	private ToggleConfigurationProperties toggleConfigurationProperties;
	@InjectMocks
	private SearchForPricesAndFillIntoPriceNormalizedInfoServiceV3 getPricesServiceV3;

	@Test
	void shouldFillThePriceNormalizedInfoListCorrectlyWhenTheCountryIsNotPriceUpFrontAndShouldShowValidUntilAndToggle19558Off() {

		doReturn(false).when(toggleConfigurationProperties).isEnabledCodeToggle(CODE_TOGGLE_19558);
		final PriceNormalizedInfo pni1 = mock(PriceNormalizedInfo.class);
		final PriceNormalizedInfo pni2 = mock(PriceNormalizedInfo.class);
		final PriceNormalizedInfo pni3 = mock(PriceNormalizedInfo.class);
		final PriceNormalizedInfo pni4 = mock(PriceNormalizedInfo.class);
		final List<PriceNormalizedInfo> priceNormalizedInfoList = Arrays.asList(pni1, pni2, pni3, pni4);

		when(priceUpFrontProperties.getCountriesEnabled()).thenReturn("AR,PY,UY");

		final String validUntil = "2050-04-23";
		final String timezone = "America/Sao_Paulo";

		final PriceEntityV2 price1 = mock(PriceEntityV2.class);
		final PriceEntityV2 price2 = mock(PriceEntityV2.class);
		final PriceEntityV2 price3 = mock(PriceEntityV2.class);
		final PriceEntityV2 price4 = mock(PriceEntityV2.class);
		mockTimezoneAndCreatePromotionalPrice(price4, validUntil, timezone);
		final PriceEntityV2 price5 = mock(PriceEntityV2.class);
		final List<PriceEntityV2> pricesReturnMock = Arrays.asList(price1, price2, price3, price4, price5);
		when(priceEntityV2Repository.findPricesByAccountPriceForContractAndContractlessList(
				eq(priceNormalizedInfoList), eq(COUNTRY_BR), any(Instant.class), any(Boolean.class))).thenReturn(pricesReturnMock);

		doAnswer(invocation -> {
			when(pni1.getSelectedPrice()).thenReturn(price1);
			when(pni2.getSelectedPrice()).thenReturn(price4);
			when(pni4.getSelectedPrice()).thenReturn(price4);
			return null;
		}).when(prioritizePriceByTypeServiceV3).execute(pricesReturnMock, priceNormalizedInfoList);

		when(calculateDaysBehindToShowValidUntilHelper.shouldShowValidUntil(validUntil, COUNTRY_BR, timezone)).thenReturn(true);

		getPricesServiceV3.execute(priceNormalizedInfoList, COUNTRY_BR, false);

		assertThat(pricesReturnMock, hasSize(5));
		assertThat(pricesReturnMock, hasItems(price1, price2, price3, price4, price5));

		assertThat(priceNormalizedInfoList, hasSize(4));
		assertThat(priceNormalizedInfoList, hasItems(pni1, pni2, pni3, pni4));

		assertThat(pni1.getSelectedPrice(), is(price1));
		assertThat(pni2.getSelectedPrice(), is(price4));
		assertThat(pni3.getSelectedPrice(), is(nullValue()));
		assertThat(pni4.getSelectedPrice(), is(price4));

		assertThat(price4.getPromotionalPrice().getValidUntil(), is(validUntil));

		verify(obtainPricesWithClosestDatesService, never()).execute(any());
		verify(calculateDaysBehindToShowValidUntilHelper, times(2)).shouldShowValidUntil(validUntil, COUNTRY_BR, timezone);
	}

	@Test
	void shouldFillThePriceNormalizedInfoListCorrectlyWhenTheCountryIsNotPriceUpFrontAndShouldShowValidUntil() {

		doReturn(true).when(toggleConfigurationProperties).isEnabledCodeToggle(CODE_TOGGLE_19558);
		final PriceNormalizedInfo pni1 = mock(PriceNormalizedInfo.class);
		final PriceNormalizedInfo pni2 = mock(PriceNormalizedInfo.class);
		final PriceNormalizedInfo pni3 = mock(PriceNormalizedInfo.class);
		final PriceNormalizedInfo pni4 = mock(PriceNormalizedInfo.class);
		final List<PriceNormalizedInfo> priceNormalizedInfoList = Arrays.asList(pni1, pni2, pni3, pni4);

		when(priceUpFrontProperties.getCountriesEnabled()).thenReturn("AR,PY,UY");

		final String validUntil = "2050-04-23";
		final String timezone = "America/Sao_Paulo";

		final PriceEntityV2 price1 = mock(PriceEntityV2.class);
		final PriceEntityV2 price2 = mock(PriceEntityV2.class);
		final PriceEntityV2 price3 = mock(PriceEntityV2.class);
		final PriceEntityV2 price4 = mock(PriceEntityV2.class);
		mockTimezoneAndCreatePromotionalPrice(price4, validUntil, timezone);
		final PriceEntityV2 price5 = mock(PriceEntityV2.class);
		final List<PriceEntityV2> pricesReturnMock = Arrays.asList(price1, price2, price3, price4, price5);
		when(priceEntityV2Repository.findPricesByAccountPriceForContractAndContractlessList(
				eq(priceNormalizedInfoList), eq(COUNTRY_BR), any(Instant.class), any(Boolean.class))).thenReturn(pricesReturnMock);

		final List<PriceEntityV2> pricesWithClosestDatesReturnMock = Arrays.asList(price1, price2, price3, price4, price5);
		when(obtainPricesWithClosestDatesService.execute(pricesReturnMock)).thenReturn(pricesWithClosestDatesReturnMock);

		doAnswer(invocation -> {
			when(pni1.getSelectedPrice()).thenReturn(price1);
			when(pni2.getSelectedPrice()).thenReturn(price4);
			when(pni4.getSelectedPrice()).thenReturn(price4);
			return null;
		}).when(prioritizePriceByTypeServiceV3).execute(pricesWithClosestDatesReturnMock, priceNormalizedInfoList);

		when(calculateDaysBehindToShowValidUntilHelper.shouldShowValidUntil(validUntil, COUNTRY_BR, timezone)).thenReturn(true);

		getPricesServiceV3.execute(priceNormalizedInfoList, COUNTRY_BR, false);

		assertThat(pricesReturnMock, hasSize(5));
		assertThat(pricesReturnMock, hasItems(price1, price2, price3, price4, price5));

		assertThat(priceNormalizedInfoList, hasSize(4));
		assertThat(priceNormalizedInfoList, hasItems(pni1, pni2, pni3, pni4));

		assertThat(pni1.getSelectedPrice(), is(price1));
		assertThat(pni2.getSelectedPrice(), is(price4));
		assertThat(pni3.getSelectedPrice(), is(nullValue()));
		assertThat(pni4.getSelectedPrice(), is(price4));

		assertThat(price4.getPromotionalPrice().getValidUntil(), is(validUntil));

		verify(calculateDaysBehindToShowValidUntilHelper, times(2)).shouldShowValidUntil(validUntil, COUNTRY_BR, timezone);
	}

	@Test
	void shouldFillThePriceNormalizedInfoListCorrectlyWhenTheCountryIsPriceUpFrontAndShouldNotShowValidUntil() {

		doReturn(true).when(toggleConfigurationProperties).isEnabledCodeToggle(CODE_TOGGLE_19558);
		final PriceNormalizedInfo pni1 = mock(PriceNormalizedInfo.class);
		final PriceNormalizedInfo pni2 = mock(PriceNormalizedInfo.class);
		final PriceNormalizedInfo pni3 = mock(PriceNormalizedInfo.class);
		final PriceNormalizedInfo pni4 = mock(PriceNormalizedInfo.class);
		final List<PriceNormalizedInfo> priceNormalizedInfoList = Arrays.asList(pni1, pni2, pni3, pni4);

		when(priceUpFrontProperties.getCountriesEnabled()).thenReturn("AR,PY,UY");

		final String validUntil = "2050-04-28";
		final String timezone = "Argentina/Buenos_Aires";

		final PriceEntityV2 price1 = mock(PriceEntityV2.class);
		final PriceEntityV2 price2 = mock(PriceEntityV2.class);
		mockTimezoneAndCreatePromotionalPrice(price2, validUntil, timezone);
		final PriceEntityV2 price3 = mock(PriceEntityV2.class);
		final PriceEntityV2 price4 = mock(PriceEntityV2.class);
		final PriceEntityV2 price5 = mock(PriceEntityV2.class);
		final List<PriceEntityV2> pricesReturnMock = Arrays.asList(price1, price2, price3, price4, price5);
		when(priceEntityV2Repository.findPricesByAccountPriceForContractAndContractlessList(
				eq(priceNormalizedInfoList), eq(COUNTRY_AR), any(Instant.class), any(Boolean.class))).thenReturn(pricesReturnMock);

		final List<PriceEntityV2> pricesWithClosestDatesReturnMock = Arrays.asList(price2, price3, price5);
		when(obtainPricesWithClosestDatesService.execute(pricesReturnMock)).thenReturn(pricesWithClosestDatesReturnMock);

		doAnswer(invocation -> {
			when(pni1.getSelectedPrice()).thenReturn(price2);
			when(pni2.getSelectedPrice()).thenReturn(price5);
			return null;
		}).when(prioritizePriceByTypeServiceV3).execute(pricesWithClosestDatesReturnMock, priceNormalizedInfoList);

		when(calculateDaysBehindToShowValidUntilHelper.shouldShowValidUntil(validUntil, COUNTRY_AR, timezone)).thenReturn(false);

		assertThat(price2.getPromotionalPrice().getValidUntil(), is(validUntil));
		getPricesServiceV3.execute(priceNormalizedInfoList, COUNTRY_AR, false);

		assertThat(pricesReturnMock, hasSize(5));
		assertThat(pricesReturnMock, hasItems(price1, price2, price3, price4, price5));

		assertThat(priceNormalizedInfoList, hasSize(4));
		assertThat(priceNormalizedInfoList, hasItems(pni1, pni2, pni3, pni4));

		assertThat(pni1.getSelectedPrice(), is(price2));
		assertThat(pni2.getSelectedPrice(), is(price5));
		assertThat(pni3.getSelectedPrice(), is(nullValue()));
		assertThat(pni4.getSelectedPrice(), is(nullValue()));

		assertThat(price2.getPromotionalPrice().getValidUntil(), is(nullValue()));

		verify(calculateDaysBehindToShowValidUntilHelper).shouldShowValidUntil(validUntil, COUNTRY_AR, timezone);
	}

	@Test
	void shouldCallTheRepositoryPassingTheCurrentInstantWhenThePriceUpFrontConfigurationIsEmpty() {

		doReturn(true).when(toggleConfigurationProperties).isEnabledCodeToggle(CODE_TOGGLE_19558);
		final PriceNormalizedInfo pni1 = mock(PriceNormalizedInfo.class);
		final List<PriceNormalizedInfo> priceNormalizedInfoList = List.of(pni1);

		when(priceUpFrontProperties.getCountriesEnabled()).thenReturn("");

		final ArgumentCaptor<Instant> instantArgumentCaptor = ArgumentCaptor.forClass(Instant.class);

		final PriceEntityV2 price1 = mock(PriceEntityV2.class);
		final List<PriceEntityV2> pricesReturnMock = Arrays.asList(price1);
		when(priceEntityV2Repository.findPricesByAccountPriceForContractAndContractlessList(eq(priceNormalizedInfoList), eq(COUNTRY_BR),
				instantArgumentCaptor.capture(), any(Boolean.class))).thenReturn(pricesReturnMock);

		final List<PriceEntityV2> pricesWithClosestDatesReturnMock = List.of(price1);
		when(obtainPricesWithClosestDatesService.execute(pricesReturnMock)).thenReturn(pricesWithClosestDatesReturnMock);

		doAnswer(invocation -> {
			when(pni1.getSelectedPrice()).thenReturn(price1);
			return null;
		}).when(prioritizePriceByTypeServiceV3).execute(pricesWithClosestDatesReturnMock, priceNormalizedInfoList);

		final Instant instantBeforeExecute = Instant.now();
		getPricesServiceV3.execute(priceNormalizedInfoList, COUNTRY_BR, false);
		final Instant instantAfterExecute = Instant.now();

		assertThat(pricesReturnMock, hasSize(1));
		assertThat(pricesReturnMock, hasItems(price1));

		assertThat(priceNormalizedInfoList, hasSize(1));
		assertThat(priceNormalizedInfoList, hasItems(pni1));

		assertThat(pni1.getSelectedPrice(), is(price1));

		final Instant instantOnExecute = instantArgumentCaptor.getValue();
		assertThat(instantOnExecute.toEpochMilli(), is(greaterThanOrEqualTo(instantBeforeExecute.toEpochMilli())));
		assertThat(instantOnExecute.toEpochMilli(), is(lessThanOrEqualTo(instantAfterExecute.toEpochMilli())));

		verify(calculateDaysBehindToShowValidUntilHelper, never()).shouldShowValidUntil(any(), any(), any());
	}

	@Test
	void shouldCallTheRepositoryWhenTheParameterIgnoreValidFromWasPassed() {
		PriceNormalizedInfo priceNormalizedInfo = new PriceNormalizedInfo();
		priceNormalizedInfo.setItemId("V0ljTXZIZ0pUaGk2V1poclNaTElRZz09O0JSQUhNQS1aRVJP");
		priceNormalizedInfo.setContractId("V0ljTXZIZ0pUaGk2V1poclNaTElRZz09O0FDQ09VTlQtQkVFU1BSLTIwMzU0");
		priceNormalizedInfo.setAccountId("ACCOUNT-BEESPR-20354");
		priceNormalizedInfo.setVendorId("58870cbc-7809-4e18-ba59-986b4992c842");
		priceNormalizedInfo.setVendorItemId("BRAHMA-ZERO");

		final List<PriceNormalizedInfo> priceNormalizedInfoList = List.of(priceNormalizedInfo);

		Instant instant = Instant.now();
		PriceCompoundKeyV2 priceCompoundKeyV2 = new PriceCompoundKeyV2();
		priceCompoundKeyV2.setId("ACCOUNT-BEESPR-20354");
		priceCompoundKeyV2.setType("ACCOUNT");
		priceCompoundKeyV2.setVendorItemId("BRAHMA-ZERO");
		priceCompoundKeyV2.setVendorId("58870cbc-7809-4e18-ba59-986b4992c842");
		priceCompoundKeyV2.setValidFrom(instant);

		PriceEntityV2 price1 = new PriceEntityV2();
		price1.setId(priceCompoundKeyV2);
		price1.setBasePrice(new BigDecimal("20.0"));
		price1.setConsignment(BigDecimal.ZERO);
		price1.setCountry("AR");
		price1.setType(PriceMeasureUnitType.PER_UNIT);

		PriceCompoundKeyV2 priceCompoundKeyV2_2 = new PriceCompoundKeyV2();
		priceCompoundKeyV2_2.setId("ACCOUNT-BEESPR-20354");
		priceCompoundKeyV2_2.setType("ACCOUNT");
		priceCompoundKeyV2_2.setVendorItemId("BRAHMA-ZERO");
		priceCompoundKeyV2_2.setVendorId("58870cbc-7809-4e18-ba59-986b4992c842");
		priceCompoundKeyV2_2.setValidFrom(instant.plus(30, ChronoUnit.DAYS));

		PriceEntityV2 price2 = new PriceEntityV2();
		price2.setId(priceCompoundKeyV2_2);
		price2.setBasePrice(BigDecimal.TEN);
		price2.setConsignment(new BigDecimal("30.0"));
		price2.setCountry("AR");
		price2.setType(PriceMeasureUnitType.PER_UNIT);

		final List<PriceEntityV2> pricesReturnMock = Arrays.asList(price1, price2);
		when(priceEntityV2Repository.findPricesByAccountPriceForContractAndContractlessList(
				eq(priceNormalizedInfoList), eq(COUNTRY_BR), any(Instant.class), any(Boolean.class))).thenReturn(pricesReturnMock);

		List<PriceNormalizedInfo> priceNormalizedInfos = getPricesServiceV3.execute(priceNormalizedInfoList, COUNTRY_BR, true);

		Assertions.assertNotNull(priceNormalizedInfos);
		assertThat(pricesReturnMock, hasItems(price1, price2));
		assertThat(priceNormalizedInfos, hasSize(2));
	}

	@Test
	void shouldCallTheRepositoryWhenTheParameterIgnoreValidFromWasNotPassed() {
		PriceNormalizedInfo priceNormalizedInfo = new PriceNormalizedInfo();
		priceNormalizedInfo.setItemId("V0ljTXZIZ0pUaGk2V1poclNaTElRZz09O0JSQUhNQS1aRVJP");
		priceNormalizedInfo.setContractId("V0ljTXZIZ0pUaGk2V1poclNaTElRZz09O0FDQ09VTlQtQkVFU1BSLTIwMzU0");
		priceNormalizedInfo.setAccountId("ACCOUNT-BEESPR-20354");
		priceNormalizedInfo.setVendorId("58870cbc-7809-4e18-ba59-986b4992c842");
		priceNormalizedInfo.setVendorItemId("BRAHMA-ZERO");

		final List<PriceNormalizedInfo> priceNormalizedInfoList = List.of(priceNormalizedInfo);

		Instant instant = Instant.now();
		PriceEntityV2 price1 = getPriceMock(instant, new BigDecimal("20.0"));
		PriceEntityV2 price2 = getPriceMock(instant.plus(30, ChronoUnit.DAYS), new BigDecimal("30.0"));
		final List<PriceEntityV2> pricesReturnMock = Arrays.asList(price1, price2);

		when(priceEntityV2Repository.findPricesByAccountPriceForContractAndContractlessList(
				eq(priceNormalizedInfoList), eq(COUNTRY_AR), any(Instant.class), any(Boolean.class))).thenReturn(pricesReturnMock);

		List<PriceNormalizedInfo> priceNormalizedInfos = getPricesServiceV3.execute(priceNormalizedInfoList, COUNTRY_AR, false);

		Assertions.assertNotNull(priceNormalizedInfos);
		assertThat(pricesReturnMock, hasItems(price1, price2));
		assertThat(priceNormalizedInfos, hasSize(1));
	}

	@Test
	void shouldCallTheRepositoryWhenTheParameterIgnoreValidFromWasPassedAsNull() {
		PriceNormalizedInfo priceNormalizedInfo = new PriceNormalizedInfo();
		priceNormalizedInfo.setItemId("V0ljTXZIZ0pUaGk2V1poclNaTElRZz09O0JSQUhNQS1aRVJP");
		priceNormalizedInfo.setContractId("V0ljTXZIZ0pUaGk2V1poclNaTElRZz09O0FDQ09VTlQtQkVFU1BSLTIwMzU0");
		priceNormalizedInfo.setAccountId("ACCOUNT-BEESPR-20354");
		priceNormalizedInfo.setVendorId("58870cbc-7809-4e18-ba59-986b4992c842");
		priceNormalizedInfo.setVendorItemId("BRAHMA-ZERO");

		final List<PriceNormalizedInfo> priceNormalizedInfoList = List.of(priceNormalizedInfo);

		Instant instant = Instant.now();
		PriceEntityV2 price1 = getPriceMock(instant, new BigDecimal("20.0"));
		PriceEntityV2 price2 = getPriceMock(instant.plus(30, ChronoUnit.DAYS), new BigDecimal("30.0"));
		final List<PriceEntityV2> pricesReturnMock = Arrays.asList(price1, price2);

		when(priceEntityV2Repository.findPricesByAccountPriceForContractAndContractlessList(
				eq(priceNormalizedInfoList), eq(COUNTRY_AR), any(Instant.class), any())).thenReturn(pricesReturnMock);

		List<PriceNormalizedInfo> priceNormalizedInfos = getPricesServiceV3.execute(priceNormalizedInfoList, COUNTRY_AR, null);

		Assertions.assertNotNull(priceNormalizedInfos);
		assertThat(pricesReturnMock, hasItems(price1, price2));
		assertThat(priceNormalizedInfos, hasSize(1));
	}

	@Test
	void shouldCallTheRepositoryWhenContractByAccountAndPriceListAndContractless() {
		PriceNormalizedInfo priceNormalizedInfo = new PriceNormalizedInfo();
		priceNormalizedInfo.setItemId("V0ljTXZIZ0pUaGk2V1poclNaTElRZz09O0JSQUhNQS1aRVJP");
		priceNormalizedInfo.setContractId("V0ljTXZIZ0pUaGk2V1poclNaTElRZz09O0FDQ09VTlQtQkVFU1BSLTIwMzU0");
		priceNormalizedInfo.setAccountId("ACCOUNT-BEESPR-20354");
		priceNormalizedInfo.setVendorId("58870cbc-7809-4e18-ba59-986b4992c842");
		priceNormalizedInfo.setVendorItemId("BRAHMA-ZERO");

		PriceNormalizedInfo priceNormalizedInfoContractless = new PriceNormalizedInfo();
		priceNormalizedInfoContractless.setItemId("V0ljTXZIZ0pUaGk2V1poclNaTElRZz09O0JSQUhNQS1aRVJP");
		priceNormalizedInfoContractless.setDeliveryCenterId("V0ljTXZIZ0pUaGk2V1poclNaTElRZz09O0FDQ09VTlQtQkVFU1BSLTIwMzU0");
		priceNormalizedInfoContractless.setVendorId("58870cbc-7809-4e18-ba59-986b4992c842");
		priceNormalizedInfoContractless.setVendorItemId("BRAHMA-ZERO");

		final List<PriceNormalizedInfo> priceNormalizedInfoList = List.of(priceNormalizedInfo,priceNormalizedInfoContractless );

		Instant instant = Instant.now();
		PriceEntityV2 price1 = getPriceMock(instant, new BigDecimal("20.0"));
		PriceEntityV2 price2 = getPriceContractlessMock(instant, new BigDecimal("20.0"));
		final List<PriceEntityV2> pricesReturnMock = Arrays.asList(price1, price2);

		when(priceEntityV2Repository.findPricesByAccountPriceForContractAndContractlessList(
				eq(priceNormalizedInfoList), eq(COUNTRY_AR), any(Instant.class), any())).thenReturn(pricesReturnMock);

		List<PriceNormalizedInfo> priceNormalizedInfos = getPricesServiceV3.execute(priceNormalizedInfoList, COUNTRY_AR, null);

		Assertions.assertNotNull(priceNormalizedInfos);
		assertThat(pricesReturnMock, hasItems(price1, price2));
		assertThat(priceNormalizedInfos, hasSize(2));
	}

	@Test
	void shouldCallTheRepositoryWhenContractByDDCAndContractless() {
		PriceNormalizedInfo priceNormalizedInfo = new PriceNormalizedInfo();
		priceNormalizedInfo.setItemId("V0ljTXZIZ0pUaGk2V1poclNaTElRZz09O0JSQUhNQS1aRVJP");
		priceNormalizedInfo.setContractId("V0ljTXZIZ0pUaGk2V1poclNaTElRZz09O0FDQ09VTlQtQkVFU1BSLTIwMzU0");
		priceNormalizedInfo.setAccountId("ACCOUNT-BEESPR-20354");
		priceNormalizedInfo.setVendorId("58870cbc-7809-4e18-ba59-986b4992c842");
		priceNormalizedInfo.setVendorItemId("BRAHMA-ZERO");

		PriceNormalizedInfo priceNormalizedInfoContractless = new PriceNormalizedInfo();
		priceNormalizedInfoContractless.setItemId("NXVoQWUwamNURTYrWXpCYUUxVk5PQT09O0JSQUhNQS1aRVJP");
		priceNormalizedInfoContractless.setDeliveryCenterId("NXVoQWUwamNURTYrWXpCYUUxVk5PQT09O0NPTlRSQUNUTEVTU19EREM=");
		priceNormalizedInfoContractless.setVendorId("e6e8407b-48dc-4c4e-be63-305a13554d38");
		priceNormalizedInfoContractless.setVendorItemId("BRAHMA-ZERO");

		final List<PriceNormalizedInfo> priceNormalizedInfoList = List.of(priceNormalizedInfo,priceNormalizedInfoContractless );

		Instant instant = Instant.now();
		PriceEntityV2 priceContract = getPriceMockContractDDC(instant, new BigDecimal("10.0"));
		PriceEntityV2 priceContracless = getPriceContractlessMock(instant, new BigDecimal("20.0"));

		final List<PriceEntityV2> pricesReturnMockContractless = new ArrayList<>();
		final List<PriceEntityV2> pricesReturnMockContract = new ArrayList<>();

		pricesReturnMockContractless.add(priceContracless);
		pricesReturnMockContract.add(priceContract);

		when(priceEntityV2Repository.findPricesByAccountPriceForContractAndContractlessList(
				eq(priceNormalizedInfoList), eq(COUNTRY_AR), any(Instant.class), any())).thenReturn(pricesReturnMockContractless);

		final List<PriceNormalizedInfo> priceNormalizedContractInfoList = new ArrayList<>();
		priceNormalizedContractInfoList.add(priceNormalizedInfo);


		when(priceEntityV2Repository.findPricesByDDCVendorList(
				eq(priceNormalizedContractInfoList), eq(COUNTRY_AR), any(Instant.class), any())).thenReturn(pricesReturnMockContract);

		List<PriceNormalizedInfo> priceNormalizedInfos = getPricesServiceV3.execute(priceNormalizedInfoList, COUNTRY_AR, null);

		Assertions.assertNotNull(priceNormalizedInfos);
		assertThat(priceNormalizedInfos, hasSize(2));
	}

	private PriceEntityV2 getPriceMock(Instant instant, BigDecimal basePrice) {
		PriceCompoundKeyV2 priceCompoundKeyV2 = new PriceCompoundKeyV2();
		priceCompoundKeyV2.setId("ACCOUNT-BEESPR");
		priceCompoundKeyV2.setType("ACCOUNT");
		priceCompoundKeyV2.setVendorItemId("BRAHMA-ZERO");
		priceCompoundKeyV2.setVendorId("58870cbc-7809-4e18-ba59-986b4992c842");
		priceCompoundKeyV2.setValidFrom(instant);

		PriceEntityV2 price = new PriceEntityV2();
		price.setId(priceCompoundKeyV2);
		price.setBasePrice(basePrice);
		price.setConsignment(BigDecimal.ZERO);
		price.setCountry(COUNTRY_AR);
		price.setType(PriceMeasureUnitType.PER_UNIT);

		return price;
	}

	private PriceEntityV2 getPriceMockContractDDC(Instant instant, BigDecimal basePrice) {
		PriceCompoundKeyV2 priceCompoundKeyV2 = new PriceCompoundKeyV2();
		priceCompoundKeyV2.setId("DDC1");
		priceCompoundKeyV2.setType("DELIVERY_CENTER");
		priceCompoundKeyV2.setVendorItemId("BRAHMA-ZERO");
		priceCompoundKeyV2.setVendorId("58870cbc-7809-4e18-ba59-986b4992c842");
		priceCompoundKeyV2.setValidFrom(instant);

		PriceEntityV2 price = new PriceEntityV2();
		price.setId(priceCompoundKeyV2);
		price.setBasePrice(basePrice);
		price.setConsignment(BigDecimal.ZERO);
		price.setCountry(COUNTRY_AR);
		price.setType(PriceMeasureUnitType.PER_UNIT);

		return price;
	}

	private PriceEntityV2 getPriceContractlessMock(Instant instant, BigDecimal basePrice) {
		PriceCompoundKeyV2 priceCompoundKeyV2 = new PriceCompoundKeyV2();
		priceCompoundKeyV2.setId("CONTRACTLESS_DDC");
		priceCompoundKeyV2.setType("DELIVERY_CENTER");
		priceCompoundKeyV2.setVendorItemId("BRAHMA-ZERO");
		priceCompoundKeyV2.setVendorId("e6e8407b-48dc-4c4e-be63-305a13554d38");
		priceCompoundKeyV2.setValidFrom(instant);

		PriceEntityV2 price = new PriceEntityV2();
		price.setId(priceCompoundKeyV2);
		price.setBasePrice(basePrice);
		price.setConsignment(BigDecimal.ZERO);
		price.setCountry(COUNTRY_AR);
		price.setType(PriceMeasureUnitType.PER_UNIT);

		return price;
	}

	private void mockTimezoneAndCreatePromotionalPrice(final PriceEntityV2 priceEntityV2, final String validUntil,
			final String timezone) {

		final PromotionalPriceV2 promotionalPriceV2 = new PromotionalPriceV2();
		promotionalPriceV2.setValidUntil(validUntil);

		when(priceEntityV2.getPromotionalPrice()).thenReturn(promotionalPriceV2);
		when(priceEntityV2.getTimezone()).thenReturn(timezone);
	}

}