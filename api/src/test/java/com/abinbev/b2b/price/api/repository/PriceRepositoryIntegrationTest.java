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
import com.abinbev.b2b.price.api.config.properties.DatabaseCollectionProperties;
import com.abinbev.b2b.price.api.domain.PriceResultList;
import com.abinbev.b2b.price.api.rest.vo.Pagination;
import com.fasterxml.jackson.core.type.TypeReference;

@Testcontainers
@ContextConfiguration(classes = PriceApiApplication.class)
@DataMongoTest
@Import({ PriceRepository.class, DatabaseCollectionProperties.class })
@TestPropertySource(locations = "classpath:repository-data/priceRepository/application-integration-tests.yml")
class PriceRepositoryIntegrationTest {

	private static final String RESOURCE_PATH = "priceRepository";
	private static final String DEFAULT_COUNTRY = "US";
	private static final String UPCOMING_PRICE_COUNTRY = "AR";
	private static final String DEFAULT_COUNTRY_COLLECTION = "USPrices";
	private static final String UPCOMING_PRICE_COUNTRY_COLLECTION = "ARPrices";

	private static final String DOCKER_COMPOSE_YML = Objects.requireNonNull(
			Thread.currentThread().getContextClassLoader().getResource("repository-data/priceRepository/docker-compose.yml")).getPath();

	@Container
	private static final DockerComposeContainer<?> DOCKER_COMPOSE_CONTAINER = new DockerComposeContainer<>(new File(DOCKER_COMPOSE_YML));
	private static boolean dataLoaded = false;

	static {
		DOCKER_COMPOSE_CONTAINER
				.withLocalCompose(true)
				.waitingFor("repository_api_integration_tests_price_repository_mongo", Wait.forHealthcheck());
	}

	@Autowired
	private MongoOperations mongoOperations;

	@Autowired
	private PriceRepository priceRepository;

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
		assertThat(findIntoMongoByFilter(Map.of("_id.accountId", "ACCOUNT_ID-1"), DEFAULT_COUNTRY_COLLECTION), hasSize(5));
		assertThat(findIntoMongoByFilter(Map.of("_id.accountId", "ACCOUNT_ID-2"), DEFAULT_COUNTRY_COLLECTION), hasSize(2));

		IntegrationTestsRepositoryHelper.insertIntoMongo(mongoOperations, createMongoMockData("scenario-for-search-AR.json"),
				UPCOMING_PRICE_COUNTRY_COLLECTION);
		assertThat(findIntoMongoByFilter(Map.of("_id.accountId", "ACCOUNT_ID-1"), UPCOMING_PRICE_COUNTRY_COLLECTION), hasSize(6));
		assertThat(findIntoMongoByFilter(Map.of("_id.accountId", "ACCOUNT_ID-2"), UPCOMING_PRICE_COUNTRY_COLLECTION), hasSize(1));

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

	private PriceResultList createPriceResultListMock(final String fileName) throws IOException {

		return IntegrationTestsResourceHelper.getJavaMockFileContent(IntegrationPathEnum.REPOSITORY, RESOURCE_PATH, fileName,
				new TypeReference<>() {
				});
	}

	@Test
	@Execution(ExecutionMode.SAME_THREAD)
	void shouldSearchForPriceUsWhenFilteringByAccountIdThenTheRegistersAreReturnedSuccessfully() throws Exception {

		final Instant currentDate = Instant.now();
		final PriceResultList result = priceRepository.findPriceByIdFilteringSkus("ACCOUNT_ID-1", DEFAULT_COUNTRY, null, null, false,
				currentDate);

		// assert data
		final PriceResultList priceResultList = createPriceResultListMock(
				"PriceResultList-search-for-price-US-filtering-by-accountId.json");

		assertThat(result, is(equalToAsString(priceResultList)));
	}

	@Test
	@Execution(ExecutionMode.SAME_THREAD)
	void shouldSearchForPriceUsWhenFilteringBySkusThenTheRegistersAreReturnedSuccessfully() throws Exception {

		final Instant currentDate = Instant.now();
		final List<String> skusFilter = Arrays.asList("SKU_1-2", "SKU_1-4");
		final PriceResultList result = priceRepository.findPriceByIdFilteringSkus("ACCOUNT_ID-1", DEFAULT_COUNTRY, skusFilter, null, false,
				currentDate);

		// assert data
		final PriceResultList priceResultList = createPriceResultListMock("PriceResultList-search-for-price-US-filtering-by-skus.json");

		assertThat(result, is(equalToAsString(priceResultList)));
	}

	@Test
	@Execution(ExecutionMode.SAME_THREAD)
	void shouldSearchForPriceUsWhenFilteringByPaginationThenTheRegistersAreReturnedSuccessfully() throws Exception {

		final Instant currentDate = Instant.now();
		final PriceResultList result = priceRepository.findPriceByIdFilteringSkus("ACCOUNT_ID-1", DEFAULT_COUNTRY, null,
				new Pagination(1, 2), false, currentDate);

		// assert data
		final PriceResultList priceResultList = createPriceResultListMock(
				"PriceResultList-search-for-price-US-filtering-by-pagination.json");

		assertThat(result, is(equalToAsString(priceResultList)));
	}

	@Test
	@Execution(ExecutionMode.SAME_THREAD)
	void shouldSearchForPriceUsWhenFilteringBySkusAndPaginationThenTheRegistersAreReturnedSuccessfully() throws Exception {

		final Instant currentDate = Instant.now();
		final List<String> skusFilter = Arrays.asList("SKU_1-2", "SKU_1-4", "SKU_1-5");
		final PriceResultList result = priceRepository.findPriceByIdFilteringSkus("ACCOUNT_ID-1", DEFAULT_COUNTRY, skusFilter,
				new Pagination(2, 1), false, currentDate);

		// assert data
		final PriceResultList priceResultList = createPriceResultListMock(
				"PriceResultList-search-for-price-US-filtering-by-skus-and-pagination.json");

		assertThat(result, is(equalToAsString(priceResultList)));
	}

	@Test
	@Execution(ExecutionMode.SAME_THREAD)
	void shouldSearchForPriceArWhenFilteringBySkusAndPaginationThenTheRegistersAreReturnedSuccessfully() throws Exception {

		final Instant currentDate = InstantHelper.convertStringFormattedAsIsoOffsetDateTimeToInstant("2038-12-30T00:00:00+00:00");
		final List<String> skusFilter = Arrays.asList("SKU_1-1", "SKU_1-2", "SKU_1-3");
		final PriceResultList result = priceRepository.findPriceByIdFilteringSkus("ACCOUNT_ID-1", UPCOMING_PRICE_COUNTRY, skusFilter,
				new Pagination(0, 2), true, currentDate);

		// assert data
		final PriceResultList priceResultList = createPriceResultListMock(
				"PriceResultList-search-for-price-AR-filtering-by-skus-and-pagination.json");

		assertThat(result, is(equalToAsString(priceResultList)));
	}

}
