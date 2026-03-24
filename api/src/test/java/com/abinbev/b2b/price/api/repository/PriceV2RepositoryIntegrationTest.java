package com.abinbev.b2b.price.api.repository;

import static com.abinbev.b2b.price.api.common.IsEqualAsStringMatcher.equalToAsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
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
import com.abinbev.b2b.price.api.domain.v2.PriceResultListV2;
import com.abinbev.b2b.price.api.rest.vo.v2.PaginationResponseVoV2;
import com.fasterxml.jackson.core.type.TypeReference;

@Testcontainers
@ContextConfiguration(classes = PriceApiApplication.class)
@DataMongoTest
@Import({ PriceEntityV2Repository.class, DatabaseCollectionPropertiesV2.class, ToggleConfigurationProperties.class, ApiConfig.class }) //remove ToggleConfigurationProperties.class when toggle BEESPR_22389 is removed
@TestPropertySource(locations = "classpath:repository-data/priceV2Repository/application-integration-tests.yml")
class PriceV2RepositoryIntegrationTest {

	private static final String RESOURCE_PATH = "priceV2Repository";
	private static final String DEFAULT_COUNTRY = "US";
	private static final String UPCOMING_PRICE_COUNTRY = "AR";
	private static final String DEFAULT_COUNTRY_COLLECTION = "USPrices_V2";
	private static final String UPCOMING_PRICE_COUNTRY_COLLECTION = "ARPrices_V2";
	private static final String PRICE_ID_FIELD_NAME = "_id.id";
	private static final String PRICE_ID_TYPE_FIELD_NAME = "_id.type";
	private static final String PRICE_LIST_ID_1 = "PRICE_LIST-ID-1";
	private static final String PRICE_LIST_ID_2 = "PRICE_LIST-ID-2";
	private static final String PRICE_VENDOR_ACCOUNT_ID_1 = "ACCOUNT_ID-1";
	private static final String PRICE_VENDOR_ACCOUNT_ID_2 = "ACCOUNT_ID-2";
	private static final String PRICE_VENDOR_ACCOUNT_ID_20 = "ACCOUNT_ID-20";
	private static final String PRICE_LIST_TYPE = "PRICE_LIST";
	private static final String PRICE_VENDOR_ID = "Vendor-1";
	private static final String PRICE_SKU_1_1 = "SKU_1-1";
	private static final String PRICE_SKU_1_2 = "SKU_1-2";
	private static final String PRICE_SKU_1_3 = "SKU_1-3";
	private static final String PRICE_SKU_1_4 = "SKU_1-4";
	private static final String PRICE_SKU_1_5 = "SKU_1-5";
	private static final String PRICE_SKU_2_1 = "SKU_2-1";

	private static final String DOCKER_COMPOSE_YML = Objects.requireNonNull(
			Thread.currentThread().getContextClassLoader().getResource("repository-data/priceV2Repository/docker-compose.yml")).getPath();

	@Container
	private static final DockerComposeContainer<?> DOCKER_COMPOSE_CONTAINER = new DockerComposeContainer<>(new File(DOCKER_COMPOSE_YML));

	private static boolean dataLoaded = false;

	static {
		DOCKER_COMPOSE_CONTAINER
				.withLocalCompose(true)
				.waitingFor("repository_api_integration_tests_price_repository_v2_mongo", Wait.forHealthcheck());
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

		IntegrationTestsRepositoryHelper.insertIntoMongo(mongoOperations, createMongoMockData("scenario-for-search-US.json"),
				DEFAULT_COUNTRY_COLLECTION);
		assertThat(findIntoMongoByFilter(Map.of(PRICE_ID_FIELD_NAME, PRICE_LIST_ID_1, PRICE_ID_TYPE_FIELD_NAME, PRICE_LIST_TYPE),
				DEFAULT_COUNTRY_COLLECTION), hasSize(4));
		assertThat(findIntoMongoByFilter(Map.of(PRICE_ID_FIELD_NAME, PRICE_LIST_ID_2, PRICE_ID_TYPE_FIELD_NAME, PRICE_LIST_TYPE),
				DEFAULT_COUNTRY_COLLECTION), hasSize(1));

		IntegrationTestsRepositoryHelper.insertIntoMongo(mongoOperations, createMongoMockData("scenario-for-search-AR.json"),
				UPCOMING_PRICE_COUNTRY_COLLECTION);
		assertThat(findIntoMongoByFilter(Map.of(PRICE_ID_FIELD_NAME, PRICE_VENDOR_ACCOUNT_ID_1), UPCOMING_PRICE_COUNTRY_COLLECTION),
				hasSize(6));
		assertThat(findIntoMongoByFilter(Map.of(PRICE_ID_FIELD_NAME, PRICE_VENDOR_ACCOUNT_ID_2), UPCOMING_PRICE_COUNTRY_COLLECTION),
				hasSize(1));

		dataLoaded = true;
	}

	private List<Map<String, Object>> createMongoMockData(final String fileName) throws IOException {

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

	private List<Map<Object, Object>> findIntoMongoByFilter(final Map<String, String> filter, final String collection) {

		return IntegrationTestsRepositoryHelper.findIntoMongo(mongoOperations, filter, collection);
	}

	private PriceResultListV2 createPriceResultListMock(final String fileName) throws IOException {

		return IntegrationTestsResourceHelper.getJavaMockFileContent(IntegrationPathEnum.REPOSITORY, RESOURCE_PATH, fileName,
				new TypeReference<>() {
				});
	}

	@Test
	@Execution(ExecutionMode.SAME_THREAD)
	void shouldSearchForPriceUsWhenFilteringByAccountIdThenTheRegistersAreReturnedSuccessfully() throws Exception {

		final Instant currentDate = Instant.now();
		final PriceResultListV2 result = priceV2Repository.findPriceByIdFilteringVendorItemId(PRICE_VENDOR_ID, PRICE_VENDOR_ACCOUNT_ID_1,
				DEFAULT_COUNTRY, null, null, currentDate, null);
		// assert data
		final PriceResultListV2 priceResultList = createPriceResultListMock(
				"PriceResultList-search-for-price-US-filtering-by-accountId.json");

		assertThat(result, is(equalToAsString(priceResultList)));
	}

	@Test
	@Execution(ExecutionMode.SAME_THREAD)
	void shouldSearchForPriceUsWhenFilteringByPriceListIdThenTheRegistersAreReturnedSuccessfully() throws Exception {

		final Instant currentDate = Instant.now();
		final PriceResultListV2 result = priceV2Repository.findPriceByIdFilteringVendorItemId(PRICE_VENDOR_ID, PRICE_VENDOR_ACCOUNT_ID_20,
				DEFAULT_COUNTRY, null, null, currentDate, PRICE_LIST_ID_1);
		// assert data
		final PriceResultListV2 priceResultList = createPriceResultListMock(
				"PriceResultList-search-for-price-US-filtering-by-priceListId.json");

		assertThat(result, is(equalToAsString(priceResultList)));
	}

	@Test
	@Execution(ExecutionMode.SAME_THREAD)
	void shouldSearchForPriceUsWhenFilteringBySkusAndVendorAccountIdAndPriceListIdThenReturnedSuccessfully() throws Exception {

		final Instant currentDate = Instant.now();
		final List<String> skusFilter = Arrays.asList(PRICE_SKU_1_2, PRICE_SKU_1_4, PRICE_SKU_1_5);
		final PriceResultListV2 result = priceV2Repository.findPriceByIdFilteringVendorItemId(PRICE_VENDOR_ID, PRICE_VENDOR_ACCOUNT_ID_1,
				DEFAULT_COUNTRY, skusFilter, null, currentDate, PRICE_LIST_ID_1);
		// assert data
		final PriceResultListV2 priceResultList = createPriceResultListMock("PriceResultList-search-for-price-US-filtering-by-skus.json");

		assertThat(result, is(equalToAsString(priceResultList)));
	}

	@Test
	@Execution(ExecutionMode.SAME_THREAD)
	void shouldSearchForPriceUsWhenFilteringByPaginationThenTheRegistersAreReturnedSuccessfully() throws Exception {

		final Instant currentDate = Instant.now();
		final PriceResultListV2 result = priceV2Repository.findPriceByIdFilteringVendorItemId(PRICE_VENDOR_ID, PRICE_VENDOR_ACCOUNT_ID_1,
				DEFAULT_COUNTRY, null, new PaginationResponseVoV2(0, 2, 2), currentDate, null);
		// assert data
		final PriceResultListV2 priceResultList = createPriceResultListMock(
				"PriceResultList-search-for-price-US-filtering-by-pagination.json");

		assertThat(result, is(equalToAsString(priceResultList)));
	}

	@Test
	@Execution(ExecutionMode.SAME_THREAD)
	void shouldSearchForPriceUsWhenFilteringBySkusAndPaginationThenTheRegistersAreReturnedSuccessfully() throws Exception {

		final Instant currentDate = Instant.now();
		final List<String> skusFilter = Arrays.asList(PRICE_SKU_1_2, PRICE_SKU_1_4, PRICE_SKU_1_5);
		final PriceResultListV2 result = priceV2Repository.findPriceByIdFilteringVendorItemId(PRICE_VENDOR_ID, PRICE_VENDOR_ACCOUNT_ID_1,
				DEFAULT_COUNTRY, skusFilter, new PaginationResponseVoV2(0, 2, 2), currentDate, null);
		// assert data
		final PriceResultListV2 priceResultList = createPriceResultListMock(
				"PriceResultList-search-for-price-US-filtering-by-skus-and-pagination.json");

		assertThat(result, is(equalToAsString(priceResultList)));
	}

	@Test
	@Execution(ExecutionMode.SAME_THREAD)
	void shouldSearchForPriceArWhenFilteringBySkusAndPaginationThenTheRegistersAreReturnedSuccessfully() throws Exception {

		final Instant currentDate = InstantHelper.convertStringFormattedAsIsoOffsetDateTimeToInstant("2032-12-30T00:00:00+00:00");
		final List<String> skusFilter = Arrays.asList(PRICE_SKU_1_1, PRICE_SKU_1_2, PRICE_SKU_1_3);
		final PriceResultListV2 result = priceV2Repository.findPriceByIdFilteringVendorItemId(PRICE_VENDOR_ID, PRICE_VENDOR_ACCOUNT_ID_1,
				UPCOMING_PRICE_COUNTRY, skusFilter, new PaginationResponseVoV2(0, 2, 3), currentDate, null);
		// assert data
		final PriceResultListV2 priceResultList = createPriceResultListMock(
				"PriceResultList-search-for-price-AR-filtering-by-skus-and-pagination.json");

		assertThat(result, is(equalToAsString(priceResultList)));
	}

	@Test
	@Execution(ExecutionMode.SAME_THREAD)
	void shouldNotFoundPriceWhenTheValidFromDateIsAheadOfCurrentDate() throws Exception {

		final Instant currentDate = InstantHelper.convertStringFormattedAsIsoOffsetDateTimeToInstant("2032-12-30T00:00:00+00:00");
		final List<String> skusFilter = List.of(PRICE_SKU_2_1);
		final PriceResultListV2 result = priceV2Repository.findPriceByIdFilteringVendorItemId(PRICE_VENDOR_ID, PRICE_VENDOR_ACCOUNT_ID_2,
				UPCOMING_PRICE_COUNTRY, skusFilter, null, currentDate, null);
		// assert data
		final PriceResultListV2 priceResultList = createPriceResultListMock("PriceResultList-search-for-price-AR-validFrom-ahead.json");

		assertThat(result, is(equalToAsString(priceResultList)));
	}

}
