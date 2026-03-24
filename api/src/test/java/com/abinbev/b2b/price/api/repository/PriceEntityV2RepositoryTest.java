package com.abinbev.b2b.price.api.repository;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mock.Strictness.LENIENT;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;

import com.abinbev.b2b.price.api.config.ApiConfig;
import com.abinbev.b2b.price.api.config.properties.DatabaseCollectionPropertiesV2;
import com.abinbev.b2b.price.api.config.properties.ToggleConfigurationProperties;
import com.abinbev.b2b.price.api.domain.v2.PriceResultListV2;
import com.abinbev.b2b.price.api.domain.v3.PriceNormalizedInfo;
import com.abinbev.b2b.price.api.helpers.ToggleValues;
import com.abinbev.b2b.price.api.rest.vo.v2.PaginationResponseVoV2;
import com.abinbev.b2b.price.domain.model.v2.EntityTypeEnum;
import com.abinbev.b2b.price.domain.model.v2.PriceEntityV2;

@ExtendWith(MockitoExtension.class)
class PriceEntityV2RepositoryTest {

	private static final String PRICE_VENDOR_ID_FIELD_NAME = "vendorId";
	private static final String PRICE_LIST_ID = "priceListID";
	private static final String PRICE_VENDOR_ACCOUNT_ID_FIELD_NAME = "vendorAccountId";
	private static final String MOCK_COUNTRY_US = "US";
	private static final String ACCOUNT_ID_1 = "ACCOUNT_ID_1";
	private static final String VENDOR_ID_1 = "VENDOR_ID_1";
	private static final String VENDOR_ITEM_1 = "VENDOR_ITEM_1";
	private static final String CONTRACT_ID_1 = "CONTRACT_ID_1";
	private static final String DELIVERY_CENTER_ID_1 = "DELIVERY_CENTER_ID_1";
	private final List<String> MOCKED_VENDOR_ID = Arrays.asList("12345", "32141");
	@InjectMocks
	PriceEntityV2Repository repository;
	@Mock
	private MongoOperations mongoOperations;
	@Mock
	private ApiConfig apiConfig;
	@Captor
	ArgumentCaptor<Query> captor;

	@Mock(strictness = LENIENT)
	private DatabaseCollectionPropertiesV2 databaseCollectionProperties;

	@Mock
	private ToggleConfigurationProperties toggleConfigurationProperties;

	@BeforeEach
	void setup() {

		when(databaseCollectionProperties.getCollectionByCountryV2(MOCK_COUNTRY_US)).thenReturn(MOCK_COUNTRY_US);
	}

	//Remove parameters from test when BEESPR-22389 is done
	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	void shouldBeSuccessWhenNotInformedPagination(final Boolean codeToggleBeespr22389Value) {

		doReturn(mockedResultList()).when(mongoOperations).find(any(), eq(PriceEntityV2.class), eq(MOCK_COUNTRY_US));
		doReturn(codeToggleBeespr22389Value).when(toggleConfigurationProperties).isEnabledCodeToggle(ToggleValues.BEESPR_22389);

		final PriceResultListV2 resultList = repository
				.findPriceByIdFilteringVendorItemId(PRICE_VENDOR_ID_FIELD_NAME, PRICE_VENDOR_ACCOUNT_ID_FIELD_NAME, MOCK_COUNTRY_US,
						MOCKED_VENDOR_ID, null, Instant.now(), null);

		assertThat(resultList.getPagination().getPage(), is(0));
		assertThat(resultList.getPagination().getSize(), is(1));
		assertThat(resultList.getPagination().getTotalElements(), is(1L));
	}

	//Remove parameters from test when BEESPR-22389 is done
	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	void shouldBeSuccessfulWhenAllParametersAreInformed(final Boolean codeToggleBeespr22389Value) {

		doReturn(mockedResultList()).when(mongoOperations).find(any(), eq(PriceEntityV2.class), eq(MOCK_COUNTRY_US));
		doReturn(codeToggleBeespr22389Value).when(toggleConfigurationProperties).isEnabledCodeToggle(ToggleValues.BEESPR_22389);


		final PriceResultListV2 resultList = repository
				.findPriceByIdFilteringVendorItemId(PRICE_VENDOR_ID_FIELD_NAME, PRICE_VENDOR_ACCOUNT_ID_FIELD_NAME, MOCK_COUNTRY_US,
						MOCKED_VENDOR_ID, new PaginationResponseVoV2(1, 1, 1), Instant.now(), PRICE_LIST_ID);

		assertThat(resultList.getPagination().getPage(), is(1));
		assertThat(resultList.getPagination().getSize(), is(1));
		assertThat(resultList.getPagination().getTotalPages(), is(0));
	}

	//Remove parameters from test when BEESPR-22389 is done
	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	void shouldBeSuccessfulWhenPaginationIsInformed(final Boolean codeToggleBeespr22389Value) {

		doReturn(mockedResultList()).when(mongoOperations).find(any(), eq(PriceEntityV2.class), eq(MOCK_COUNTRY_US));
		doReturn(codeToggleBeespr22389Value).when(toggleConfigurationProperties).isEnabledCodeToggle(ToggleValues.BEESPR_22389);

		final PriceResultListV2 resultList = repository
				.findPriceByIdFilteringVendorItemId(PRICE_VENDOR_ID_FIELD_NAME, PRICE_VENDOR_ACCOUNT_ID_FIELD_NAME, MOCK_COUNTRY_US,
						MOCKED_VENDOR_ID, new PaginationResponseVoV2(1, 1, 1), Instant.now(), null);

		assertThat(resultList.getPagination().getPage(), is(1));
		assertThat(resultList.getPagination().getSize(), is(1));
		assertThat(resultList.getPagination().getTotalPages(), is(0));
	}

	//Remove parameters from test when BEESPR-22389 is done
	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	void shouldBeSuccessfulWhenNotInformedVendorItemId(final Boolean codeToggleBeespr22389Value) {

		doReturn(mockedResultList()).when(mongoOperations).find(any(), eq(PriceEntityV2.class), eq(MOCK_COUNTRY_US));
		doReturn(codeToggleBeespr22389Value).when(toggleConfigurationProperties).isEnabledCodeToggle(ToggleValues.BEESPR_22389);


		final PriceResultListV2 resultList = repository
				.findPriceByIdFilteringVendorItemId(PRICE_VENDOR_ID_FIELD_NAME, PRICE_VENDOR_ACCOUNT_ID_FIELD_NAME, MOCK_COUNTRY_US, null,
						new PaginationResponseVoV2(1, 1, 1), Instant.now(), null);

		final List<PriceEntityV2> priceEntities = resultList.getPriceEntities();

		assertThat(priceEntities.size(), is(equalTo(1)));
	}

	//Remove parameters from test when BEESPR-22389 is done
	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	void shouldFindPricesWhenReceivesAListOfPriceNormalizedInfo(final Boolean codeToggleBeespr22389Value) {

		doReturn(mockedResultList()).when(mongoOperations).find(any(), eq(PriceEntityV2.class), eq(MOCK_COUNTRY_US));
		doReturn(codeToggleBeespr22389Value).when(toggleConfigurationProperties).isEnabledCodeToggle(ToggleValues.BEESPR_22389);
		final Instant currentDate = Instant.now();

		final List<PriceNormalizedInfo> priceNormalizedInfoList = mockPriceNormalizedInfoForEachCombination();

		final List<PriceEntityV2> priceEntities = repository.findPricesByAccountPriceForContractAndContractlessList(priceNormalizedInfoList, MOCK_COUNTRY_US, currentDate, true);

		assertThat(priceEntities.size(), is(equalTo(1)));
	}

	@Test
	void shouldFindPricesByDDCVendorWhenReceiveDDC() {
		doReturn(mockedResultList()).when(mongoOperations).find(any(), any(), any());

		List<PriceNormalizedInfo> priceIdList = new ArrayList<>();
		PriceNormalizedInfo priceId = new PriceNormalizedInfo();
		priceId.setContractId(CONTRACT_ID_1);
		priceId.setAccountId(ACCOUNT_ID_1);
		priceId.setVendorItemId(VENDOR_ITEM_1);
		priceId.setVendorId(VENDOR_ID_1);
		priceId.setDeliveryCenterId(DELIVERY_CENTER_ID_1);
		priceId.setVendorDeliveryCenterId(DELIVERY_CENTER_ID_1);
		priceIdList.add(priceId);

		final List<PriceEntityV2> priceEntities =
				repository.findPricesByDDCVendorList(
						priceIdList,
						MOCK_COUNTRY_US,
						Instant.now(),
						true);

		verify(mongoOperations).find(captor.capture(), any(), any());
		var query = captor.getValue().getQueryObject();
		assertThat( query.toString(), containsString(EntityTypeEnum.DELIVERY_CENTER.name()));
	}

	@Test
	void shouldNotFindPricesByDDCVendorWhenReceiveDDCWithVendorInConfigList() {
		doReturn(List.of(VENDOR_ID_1)).when(apiConfig).getExcludedDDCAndVendorIdFromQueryAsList();

		List<PriceNormalizedInfo> priceIdList = new ArrayList<>();
		PriceNormalizedInfo priceId = new PriceNormalizedInfo();
		priceId.setContractId(CONTRACT_ID_1);
		priceId.setAccountId(ACCOUNT_ID_1);
		priceId.setVendorItemId(VENDOR_ITEM_1);
		priceId.setVendorId(VENDOR_ID_1);
		priceId.setDeliveryCenterId(DELIVERY_CENTER_ID_1);
		priceId.setVendorDeliveryCenterId(DELIVERY_CENTER_ID_1);
		priceIdList.add(priceId);

		final List<PriceEntityV2> priceEntities =
				repository.findPricesByDDCVendorList(
						priceIdList,
						MOCK_COUNTRY_US,
						Instant.now(),
						true);
		assertThat(priceEntities.size(), is(0));

	}

	@Test
	void shouldFindPricesContractLessWhenReceiveOnlyDDC() {
		doReturn(mockedResultList()).when(mongoOperations).find(any(), any(), any());
		doReturn(emptyList()).when(apiConfig).getExcludedDDCAndVendorIdFromQueryAsList();

		List<PriceNormalizedInfo> priceIdList = new ArrayList<>();
		PriceNormalizedInfo priceId = new PriceNormalizedInfo();
		priceId.setVendorItemId(VENDOR_ITEM_1);
		priceId.setVendorId(VENDOR_ID_1);
		priceId.setDeliveryCenterId(DELIVERY_CENTER_ID_1);
		priceId.setVendorDeliveryCenterId(DELIVERY_CENTER_ID_1);
		priceIdList.add(priceId);

		final List<PriceEntityV2> priceEntities =
				repository.findPricesByAccountPriceForContractAndContractlessList(
						priceIdList,
						MOCK_COUNTRY_US,
						Instant.now(),
						true);

		verify(mongoOperations).find(captor.capture(), any(), any());
		var query = captor.getValue().getQueryObject();
		assertThat( query.toString(), containsString(EntityTypeEnum.DELIVERY_CENTER.name()));
	}

	@Test
	void shouldNotBuildQueryWithDDCForPricesContractLessWhenApiConfigListHasTheVendorId() {
		doReturn(mockedResultList()).when(mongoOperations).find(any(), any(), any());
		doReturn(List.of(VENDOR_ID_1)).when(apiConfig).getExcludedDDCAndVendorIdFromQueryAsList();

		List<PriceNormalizedInfo> priceIdList = new ArrayList<>();
		PriceNormalizedInfo priceIdWithDDC  = new PriceNormalizedInfo();
		priceIdWithDDC.setVendorItemId(VENDOR_ITEM_1);
		priceIdWithDDC.setVendorId(VENDOR_ID_1);
		priceIdWithDDC.setDeliveryCenterId(DELIVERY_CENTER_ID_1);
		priceIdWithDDC.setVendorDeliveryCenterId(DELIVERY_CENTER_ID_1);

		PriceNormalizedInfo priceIdWithContract = new PriceNormalizedInfo();
		priceIdWithContract.setVendorItemId(VENDOR_ITEM_1);
		priceIdWithContract.setVendorId(VENDOR_ID_1);
		priceIdWithContract.setContractId(CONTRACT_ID_1);
		priceIdWithContract.setAccountId(ACCOUNT_ID_1);

		priceIdList.add(priceIdWithDDC);
		priceIdList.add(priceIdWithContract);

		final List<PriceEntityV2> priceEntities =
				repository.findPricesByAccountPriceForContractAndContractlessList(
						priceIdList,
						MOCK_COUNTRY_US,
						Instant.now(),
						true);

		verify(mongoOperations).find(captor.capture(), any(), any());
		var query = captor.getValue().getQueryObject();
		assertThat( query.toString(), not(containsString(EntityTypeEnum.DELIVERY_CENTER.name())));
		assertThat(priceEntities.size(), is(equalTo(1)));
	}

	@Test
	void shouldFindPricesContractLessWhenReceiveOnlyPriceListId() {
		doReturn(mockedResultList()).when(mongoOperations).find(any(), any(), any());

		List<PriceNormalizedInfo> priceIdList = new ArrayList<>();
		PriceNormalizedInfo priceId = new PriceNormalizedInfo();
		priceId.setContractId(CONTRACT_ID_1);
		priceId.setPriceListId(PRICE_LIST_ID);
		priceId.setVendorItemId(VENDOR_ITEM_1);
		priceId.setVendorId(VENDOR_ID_1);
		priceIdList.add(priceId);

		final List<PriceEntityV2> priceEntities =
				repository.findPricesByAccountPriceForContractAndContractlessList(
						priceIdList,
						MOCK_COUNTRY_US,
						Instant.now(),
						true);

		verify(mongoOperations).find(captor.capture(), any(), any());
		var query = captor.getValue().getQueryObject();
		assertThat( query.toString(), containsString(EntityTypeEnum.PRICE_LIST.name()));
	}

	@Test
	void shouldNotQueryWhenReceiveEmptyPriceList() {

		repository.findPricesByAccountPriceForContractAndContractlessList(emptyList(),MOCK_COUNTRY_US,Instant.now(),true);
		repository.findPricesByDDCVendorList(emptyList(),MOCK_COUNTRY_US,Instant.now(),true);

		verifyNoInteractions(mongoOperations);
	}

	private List<PriceResultListV2> mockedResultList() {

		final PriceEntityV2 priceEntity = new PriceEntityV2();

		priceEntity.setBasePrice(BigDecimal.TEN);
		priceEntity.setCountry("US");

		final List<PriceEntityV2> priceEntities = new ArrayList<>();

		priceEntities.add(priceEntity);

		final PriceResultListV2 price = new PriceResultListV2(priceEntities, null);

		final List<PriceResultListV2> prices = new ArrayList<>();

		prices.add(price);

		return prices;
	}

	private List<PriceNormalizedInfo> mockPriceNormalizedInfoForEachCombination() {

		final List<PriceNormalizedInfo> priceNormalizedInfoList = new ArrayList<>();

		final PriceNormalizedInfo onlyAccountPriceNormalizedInfo = new PriceNormalizedInfo();
		onlyAccountPriceNormalizedInfo.setContractId(CONTRACT_ID_1);
		onlyAccountPriceNormalizedInfo.setAccountId(ACCOUNT_ID_1);
		onlyAccountPriceNormalizedInfo.setVendorItemId(VENDOR_ITEM_1);
		onlyAccountPriceNormalizedInfo.setVendorId(VENDOR_ID_1);
		onlyAccountPriceNormalizedInfo.setVendorDeliveryCenterId(DELIVERY_CENTER_ID_1);
		onlyAccountPriceNormalizedInfo.setDeliveryCenterId(DELIVERY_CENTER_ID_1);
		onlyAccountPriceNormalizedInfo.setPriceListId(PRICE_LIST_ID);

		priceNormalizedInfoList.add(onlyAccountPriceNormalizedInfo);

		return priceNormalizedInfoList;
	}
}
