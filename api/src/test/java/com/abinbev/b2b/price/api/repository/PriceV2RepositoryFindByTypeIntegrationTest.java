package com.abinbev.b2b.price.api.repository;

import static com.abinbev.b2b.price.api.common.IsEqualAsStringMatcher.equalToAsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.abinbev.b2b.price.api.PriceApiApplication;
import com.abinbev.b2b.price.api.common.DataAdaptationHelper;
import com.abinbev.b2b.price.api.common.InstantHelper;
import com.abinbev.b2b.price.api.common.IntegrationPathEnum;
import com.abinbev.b2b.price.api.common.IntegrationTestsRepositoryHelper;
import com.abinbev.b2b.price.api.common.IntegrationTestsResourceHelper;
import com.abinbev.b2b.price.api.config.ApiConfig;
import com.abinbev.b2b.price.api.config.properties.DatabaseCollectionPropertiesV2;
import com.abinbev.b2b.price.api.config.properties.ToggleConfigurationProperties;
import com.abinbev.b2b.price.api.domain.v3.PriceNormalizedInfo;
import com.abinbev.b2b.price.domain.model.v2.PriceEntityV2;
import com.fasterxml.jackson.core.type.TypeReference;

@Testcontainers
@ContextConfiguration(classes = PriceApiApplication.class)
@DataMongoTest
@Import({ PriceEntityV2Repository.class, DatabaseCollectionPropertiesV2.class, ToggleConfigurationProperties.class, ApiConfig.class}) //remove ToggleConfigurationProperties.class when toggle BEESPR_22389 is removed
@TestPropertySource(locations = "classpath:repository-data/priceV2RepositoryFindByType/application-integration-tests.yml")
class PriceV2RepositoryFindByTypeIntegrationTest {

	private static final String RESOURCE_PATH = "priceV2RepositoryFindByType";
	private static final String COUNTRY_BR = "BR";
	private static final String COUNTRY_AR = "AR";
	private static final String COLLECTION_BR = "BRPrices_V2";
	private static final String COLLECTION_AR = "ARPrices_V2";
	private static final String ACCOUNT_ID_1 = "ACCOUNT_ID_1";
	private static final String ACCOUNT_ID_2 = "ACCOUNT_ID_2";
	private static final String ACCOUNT_ID_3 = "ACCOUNT_ID_3";
	private static final String ACCOUNT_ID_4 = "ACCOUNT_ID_4";
	private static final String DELETED_ACCOUNT_ID_5 = "DELETED_ACCOUNT_ID_5";
	private static final String PRICE_LIST_ID_1 = "PRICE_LIST_ID_1";
	private static final String PRICE_LIST_ID_2 = "PRICE_LIST_ID_2";
	private static final String PRICE_LIST_ID_3 = "PRICE_LIST_ID_3";
	private static final String PRICE_LIST_ID_4 = "PRICE_LIST_ID_4";
	private static final String DELIVERY_CENTER_ID_1 = "DELIVERY_CENTER_ID_1";
	private static final String DELIVERY_CENTER_ID_2 = "DELIVERY_CENTER_ID_2";
	private static final String DELIVERY_CENTER_ID_3 = "DELIVERY_CENTER_ID_3";
	private static final String DELETED_DELIVERY_CENTER_ID_3 = "DELETED_DELIVERY_CENTER_ID_3";
	private static final String VENDOR_ID_1 = "VENDOR_ID_1";
	private static final String VENDOR_ID_2 = "VENDOR_ID_2";
	private static final String VENDOR_ID_3 = "VENDOR_ID_3";
	private static final String VENDOR_ID_4 = "VENDOR_ID_4";
	private static final String VENDOR_ID_5 = "VENDOR_ID_5";
	private static final String VENDOR_ITEM_1 = "VENDOR_ITEM_1";
	private static final String VENDOR_ITEM_2 = "VENDOR_ITEM_2";
	private static final String VENDOR_ITEM_3 = "VENDOR_ITEM_3";
	private static final String CONTRACT_ID_1 = "CONTRACT_ID_1";
	private static final String CONTRACT_ID_2 = "CONTRACT_ID_2";
	private static final String CONTRACT_ID_3 = "CONTRACT_ID_3";
	private static final String CONTRACT_ID_4 = "CONTRACT_ID_4";
	private static final String CONTRACT_ID_5 = "CONTRACT_ID_5";

	private static final String DOCKER_COMPOSE_YML = Objects.requireNonNull(
					Thread.currentThread().getContextClassLoader().getResource("repository-data/priceV2RepositoryFindByType/docker-compose.yml"))
			.getPath();

	@Container
	private static final DockerComposeContainer<?> DOCKER_COMPOSE_CONTAINER = new DockerComposeContainer<>(new File(DOCKER_COMPOSE_YML));

	private static boolean dataLoaded = false;

	static {
		DOCKER_COMPOSE_CONTAINER
				.withLocalCompose(true)
				.waitingFor("repository_api_integration_tests_price_repository_v2_find_by_type_mongo", Wait.forHealthcheck());
	}

	@Autowired
	private MongoOperations mongoOperations;

	@Autowired
	private PriceEntityV2Repository priceV2Repository;

	@BeforeEach
	public void beforeEach() throws Exception {

		loadDataOnce();
	}

	private void loadDataOnce() throws Exception {

		if (dataLoaded) {
			return;
		}

		IntegrationTestsRepositoryHelper.insertIntoMongo(mongoOperations, createDatabaseMockData("scenario-for-search-BR.json"),
				COLLECTION_BR);

		IntegrationTestsRepositoryHelper.insertIntoMongo(mongoOperations,
				createDatabaseMockData("scenario-for-search-BR-with-deleted-values.json"), COLLECTION_BR);

		assertThat(validateDataMassCreation(COLLECTION_BR), hasSize(19));

		IntegrationTestsRepositoryHelper.insertIntoMongo(mongoOperations,
				createDatabaseMockData("scenario-for-search-AR-with-type-variation.json"), COLLECTION_AR);

		assertThat(validateDataMassCreation(COLLECTION_AR), hasSize(4));

		dataLoaded = true;
	}

	private List<Map<String, Object>> createDatabaseMockData(final String fileName) throws IOException {

		final List<Map<String, Object>> result = IntegrationTestsResourceHelper.getMongoMockDataFileContent(IntegrationPathEnum.REPOSITORY,
				RESOURCE_PATH, fileName, new TypeReference<>() {
				});

		final Map<String, Object> adaptTypes = Map.of("_id",
				Map.of("validFrom", DataAdaptationHelper.STRING_FORMATTED_AS_ISO_LOCAL_DATE_TO_DATE_FUNCTION), "timestamp",
				DataAdaptationHelper.INTEGER_TO_LONG_FUNCTION, "validTo",
				DataAdaptationHelper.STRING_FORMATTED_AS_ISO_OFFSET_DATE_TIME_TO_DATE_FUNCTION, "createdDate",
				DataAdaptationHelper.STRING_FORMATTED_AS_ISO_OFFSET_DATE_TIME_TO_DATE_FUNCTION, "updatedDate",
				DataAdaptationHelper.STRING_FORMATTED_AS_ISO_OFFSET_DATE_TIME_TO_DATE_FUNCTION, "deletedDate",
				DataAdaptationHelper.STRING_FORMATTED_AS_ISO_OFFSET_DATE_TIME_TO_DATE_FUNCTION);

		DataAdaptationHelper.adaptTypesIntoMapJsonListWithFunctions(result, adaptTypes);
		return result;
	}

	private List<Map<Object, Object>> validateDataMassCreation(final String collection) {

		return IntegrationTestsRepositoryHelper.findIntoMongo(mongoOperations, Collections.emptyMap(), collection);
	}

	private List<PriceEntityV2> createPriceResultListMock(final String fileName) throws IOException {

		return IntegrationTestsResourceHelper.getJavaMockFileContent(IntegrationPathEnum.REPOSITORY, RESOURCE_PATH, fileName,
				new TypeReference<>() {
				});
	}

	@Test
	@Execution(ExecutionMode.SAME_THREAD)
	void shouldFindPricesBrWhenFilteringByType() throws Exception {

		// Given
		final Instant currentDate = Instant.now();
		final List<PriceNormalizedInfo> priceNormalizedInfoList = mockPriceNormalizedInfoForEachCombination();

		// When
		final List<PriceEntityV2> result = findPrices(priceNormalizedInfoList, COUNTRY_BR, currentDate, false);

		// Then
		final List<PriceEntityV2> expectedResult = createPriceResultListMock("successful-search-for-price-BR-filtering-by-type.json");

		assertThat(result, is(equalToAsString(expectedResult)));
	}

	@Test
	@Execution(ExecutionMode.SAME_THREAD)
	void shouldFindPricesARWhenFilteringIgnoreValidFromIsNull() throws Exception {

		// Given
		final Instant currentDate = Instant.now();
		final List<PriceNormalizedInfo> priceNormalizedInfoList = mockPriceNormalizedInfoForEachCombination();

		// When
		final List<PriceEntityV2> result = findPrices(priceNormalizedInfoList, COUNTRY_BR, currentDate, null);

		// Then
		final List<PriceEntityV2> expectedResult = createPriceResultListMock("successful-search-for-price-BR-filtering-by-type.json");

		assertThat(result, is(equalToAsString(expectedResult)));
	}

	@Test
	@Execution(ExecutionMode.SAME_THREAD)
	void shouldFindPricesARWhenFilteringIgnoreValidFromIsTrue() throws Exception {

		// Given
		final Instant currentDate = Instant.now();
		final List<PriceNormalizedInfo> priceNormalizedInfoList = mockPriceNormalizedInfoForEachCombination();

		// When
		final List<PriceEntityV2> result = findPrices(priceNormalizedInfoList, COUNTRY_BR, currentDate, true);

		// Then
		final List<PriceEntityV2> expectedResult = createPriceResultListMock("successful-search-for-price-BR-filtering-by-type.json");

		assertThat(result, is(equalToAsString(expectedResult)));
	}

	@Test
	@Execution(ExecutionMode.SAME_THREAD)
	void shouldFindPricesBrWhenFilteringByTypeAndDeletedPrices() throws Exception {

		// Given
		final PriceNormalizedInfo priceNormalizedInfo = new PriceNormalizedInfo();
		priceNormalizedInfo.setContractId(CONTRACT_ID_5);
		priceNormalizedInfo.setDeliveryCenterId(DELIVERY_CENTER_ID_3);
		priceNormalizedInfo.setAccountId(DELETED_ACCOUNT_ID_5);
		priceNormalizedInfo.setPriceListId(PRICE_LIST_ID_4);
		priceNormalizedInfo.setVendorDeliveryCenterId(DELETED_DELIVERY_CENTER_ID_3);
		priceNormalizedInfo.setVendorItemId(VENDOR_ITEM_1);
		priceNormalizedInfo.setVendorId(VENDOR_ID_5);

		final List<PriceNormalizedInfo> priceNormalizedInfoList = Collections.singletonList(priceNormalizedInfo);
		final Instant currentDate = Instant.now();

		// When
		final List<PriceEntityV2> result = priceV2Repository.findPricesByAccountPriceForContractAndContractlessList(priceNormalizedInfoList, COUNTRY_BR, currentDate, false);

		// Then
		final List<PriceEntityV2> expectedResult = createPriceResultListMock(
				"successful-search-for-price-BR-filtering-by-type-without-deleted-values.json");

		assertThat(result, is(equalToAsString(expectedResult)));
	}

	@Test
	@Execution(ExecutionMode.SAME_THREAD)
	void shouldFindPricesBrWhenFilteringByTypeWhenPriceUpFrontIsEnabled() throws Exception {

		// Given
		final PriceNormalizedInfo priceNormalizedInfo = new PriceNormalizedInfo();
		priceNormalizedInfo.setContractId("CONTRACT_ID_1");
		priceNormalizedInfo.setAccountId("ACCOUNT_ID_1-VALID_FROM_AND_VALID_TO_VALID");
		priceNormalizedInfo.setPriceListId("PRICE_LIST_ID_1-VALID_FROM_INVALID");
		priceNormalizedInfo.setVendorDeliveryCenterId("DELIVERY_CENTER_ID_1-VALID_FROM_AND-WITHOUT_VALID_TO");
		priceNormalizedInfo.setVendorItemId(VENDOR_ITEM_1);
		priceNormalizedInfo.setVendorId(VENDOR_ID_1);

		final List<PriceNormalizedInfo> priceNormalizedInfoList = Collections.singletonList(priceNormalizedInfo);
		final Instant currentDate = InstantHelper.convertStringFormattedAsIsoOffsetDateTimeToInstant("2050-04-05T12:00:00+00:00");

		// When
		final List<PriceEntityV2> result = priceV2Repository.findPricesByAccountPriceForContractAndContractlessList(priceNormalizedInfoList, COUNTRY_AR, currentDate, false);

		// Then
		final List<PriceEntityV2> expectedResult = createPriceResultListMock(
				"successful-search-for-price-AR-filtering-by-type-and-price-up-front.json");

		assertThat(result, is(equalToAsString(expectedResult)));
	}

	@Test
	@Execution(ExecutionMode.SAME_THREAD)
	void shouldValidateQueryWhenReceivingMaxPriceNormalizedInfoLimit() {

		// Given
		final List<PriceNormalizedInfo> priceNormalizedInfoList = mockPriceNormalizedInfoUntilTheLimit();
		final Instant currentDate = Instant.now();

		// When/Then
		assertDoesNotThrow(() -> priceV2Repository.findPricesByAccountPriceForContractAndContractlessList(priceNormalizedInfoList, COUNTRY_BR, currentDate, false));
	}

	private List<PriceNormalizedInfo> mockPriceNormalizedInfoForEachCombination() {

		final List<PriceNormalizedInfo> priceNormalizedInfoList = new ArrayList<>();

		final PriceNormalizedInfo onlyAccountPriceNormalizedInfo = new PriceNormalizedInfo();
		onlyAccountPriceNormalizedInfo.setContractId(CONTRACT_ID_1);
		onlyAccountPriceNormalizedInfo.setAccountId(ACCOUNT_ID_1);
		onlyAccountPriceNormalizedInfo.setVendorItemId(VENDOR_ITEM_1);
		onlyAccountPriceNormalizedInfo.setVendorId(VENDOR_ID_1);

		final PriceNormalizedInfo onlyDeliveryCenterPriceNormalizedInfo = new PriceNormalizedInfo();
		onlyDeliveryCenterPriceNormalizedInfo.setDeliveryCenterId(DELIVERY_CENTER_ID_1);
		onlyDeliveryCenterPriceNormalizedInfo.setVendorDeliveryCenterId(DELIVERY_CENTER_ID_1);
		onlyDeliveryCenterPriceNormalizedInfo.setVendorItemId(VENDOR_ITEM_1);
		onlyDeliveryCenterPriceNormalizedInfo.setVendorId(VENDOR_ID_1);

		final PriceNormalizedInfo onlyVendorPriceNormalizedInfo = new PriceNormalizedInfo();
		onlyVendorPriceNormalizedInfo.setVendorItemId(VENDOR_ITEM_1);
		onlyVendorPriceNormalizedInfo.setVendorId(VENDOR_ID_2);

		final PriceNormalizedInfo accountAndPriceListPriceNormalizedInfo = new PriceNormalizedInfo();
		accountAndPriceListPriceNormalizedInfo.setContractId(CONTRACT_ID_2);
		accountAndPriceListPriceNormalizedInfo.setAccountId(ACCOUNT_ID_2);
		accountAndPriceListPriceNormalizedInfo.setPriceListId(PRICE_LIST_ID_2);
		accountAndPriceListPriceNormalizedInfo.setVendorItemId(VENDOR_ITEM_2);
		accountAndPriceListPriceNormalizedInfo.setVendorId(VENDOR_ID_3);

		final PriceNormalizedInfo accountAndDeliveryCenterPriceNormalizedInfo = new PriceNormalizedInfo();
		accountAndDeliveryCenterPriceNormalizedInfo.setContractId(CONTRACT_ID_3);
		accountAndDeliveryCenterPriceNormalizedInfo.setAccountId(ACCOUNT_ID_3);
		accountAndDeliveryCenterPriceNormalizedInfo.setVendorDeliveryCenterId(DELIVERY_CENTER_ID_2);
		accountAndDeliveryCenterPriceNormalizedInfo.setVendorItemId(VENDOR_ITEM_2);
		accountAndDeliveryCenterPriceNormalizedInfo.setVendorId(VENDOR_ID_3);

		final PriceNormalizedInfo accountAndPriceListAndDeliveryCenterPriceNormalizedInfo = new PriceNormalizedInfo();
		accountAndPriceListAndDeliveryCenterPriceNormalizedInfo.setContractId(CONTRACT_ID_4);
		accountAndPriceListAndDeliveryCenterPriceNormalizedInfo.setAccountId(ACCOUNT_ID_4);
		accountAndPriceListAndDeliveryCenterPriceNormalizedInfo.setPriceListId(PRICE_LIST_ID_3);
		accountAndPriceListAndDeliveryCenterPriceNormalizedInfo.setVendorDeliveryCenterId(DELIVERY_CENTER_ID_3);
		accountAndPriceListAndDeliveryCenterPriceNormalizedInfo.setVendorItemId(VENDOR_ITEM_3);
		accountAndPriceListAndDeliveryCenterPriceNormalizedInfo.setVendorId(VENDOR_ID_4);

		priceNormalizedInfoList.add(onlyAccountPriceNormalizedInfo);
		priceNormalizedInfoList.add(onlyDeliveryCenterPriceNormalizedInfo);
		priceNormalizedInfoList.add(onlyVendorPriceNormalizedInfo);
		priceNormalizedInfoList.add(accountAndPriceListPriceNormalizedInfo);
		priceNormalizedInfoList.add(accountAndDeliveryCenterPriceNormalizedInfo);
		priceNormalizedInfoList.add(accountAndPriceListAndDeliveryCenterPriceNormalizedInfo);

		return priceNormalizedInfoList;
	}

	private List<PriceNormalizedInfo> mockPriceNormalizedInfoUntilTheLimit() {

		final List<PriceNormalizedInfo> priceNormalizedInfoList = new ArrayList<>();

		for (int i = 1; i <= 50; i++) {
			final PriceNormalizedInfo priceNormalizedInfo = new PriceNormalizedInfo();
			priceNormalizedInfo.setAccountId("ACCOUNT_ID_" + i);
			priceNormalizedInfo.setPriceListId("PRICE_LIST_ID_" + i);
			priceNormalizedInfo.setVendorDeliveryCenterId("DELIVERY_CENTER_ID_" + i);
			priceNormalizedInfo.setVendorItemId("VENDOR_ITEM_ID" + i);
			priceNormalizedInfo.setVendorId("VENDOR_ID_" + i);

			priceNormalizedInfoList.add(priceNormalizedInfo);
		}

		return priceNormalizedInfoList;
	}

	private List<PriceEntityV2> findPrices(List<PriceNormalizedInfo> priceNormalizedInfoList, String country, Instant currentDate,
			Boolean ignoreValidFrom) {

		final List<PriceEntityV2> result =
				priceV2Repository.findPricesByAccountPriceForContractAndContractlessList(
						priceNormalizedInfoList, country, currentDate, ignoreValidFrom);
		result.addAll( priceV2Repository.findPricesByDDCVendorList(priceNormalizedInfoList, country, currentDate, ignoreValidFrom) );
		return result;
	}
}
