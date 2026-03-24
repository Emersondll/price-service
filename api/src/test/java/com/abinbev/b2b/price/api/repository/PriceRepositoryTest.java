package com.abinbev.b2b.price.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.abinbev.b2b.price.api.config.properties.DatabaseCollectionProperties;
import com.abinbev.b2b.price.api.domain.PriceResultList;
import com.abinbev.b2b.price.api.rest.vo.Pagination;
import com.abinbev.b2b.price.api.testhelpers.TestConstants;
import com.abinbev.b2b.price.domain.model.PriceCompoundKey;
import com.abinbev.b2b.price.domain.model.PriceEntity;

@ExtendWith(MockitoExtension.class)
class PriceRepositoryTest {

	private static final String MOCKED_ACCOUNT_ID = "001";
	private static final String MOCKED_COUNTRY = "ZA";
	private static final String MOCKED_COLLECTION = "ZAPrices";
	private static final List<String> MOCKED_ALL_SKU_LIST = Arrays.asList("SKU1", "SKU2", "SKU3", "SKU4", "SKU5");
	private static final String PRICE_ID_FIELD_NAME = "_id";
	private static final String PRICE_SKU_FIELD_NAME = "sku";
	private static final String PRICE_ACCOUNT_ID_FIELD_NAME = "accountId";
	private static final String PRICE_DELETED_FIELD_NAME = "deleted";
	private static final String FILTER_PATTERN = "%s.%s";
	private static final String VALID_TO_FIELD_NAME = "validTo";
	private static final String VALID_FROM_FIELD_NAME = "validFrom";

	@Mock
	private MongoOperations mongoOperations;

	@Mock
	private DatabaseCollectionProperties databaseCollectionProperties;

	@InjectMocks
	private PriceRepository priceDAO;

	@Test
	void shouldFindByValidAccountIdWhenFiveSkus() {

		final List<PriceEntity> mockedPriceEntityList = mockPriceEntities(5);
		final ArgumentCaptor<Query> argumentCaptor = ArgumentCaptor.forClass(Query.class);

		mocks(mockedPriceEntityList, argumentCaptor);

		final PriceResultList priceResultList = priceDAO
				.findPriceByIdFilteringSkus(MOCKED_ACCOUNT_ID, MOCKED_COUNTRY, MOCKED_ALL_SKU_LIST, new Pagination(0, 10), false,
						Instant.now());

		defaultAsserts(priceResultList, mockedPriceEntityList);

		final Query query = new Query();
		query.addCriteria(
				Criteria.where(String.format(FILTER_PATTERN, PRICE_ID_FIELD_NAME, PRICE_ACCOUNT_ID_FIELD_NAME)).is(MOCKED_ACCOUNT_ID))
				.addCriteria(
						Criteria.where(String.format(FILTER_PATTERN, PRICE_ID_FIELD_NAME, PRICE_SKU_FIELD_NAME)).in(MOCKED_ALL_SKU_LIST))
				.addCriteria(Criteria.where(PRICE_DELETED_FIELD_NAME).is(false)).with(PageRequest.of(0, 5));

		assertEquals(argumentCaptor.getValue().getQueryObject(), query.getQueryObject());
	}

	@Test
	void shouldFindByValidAccountIdWhenHaveNoSkus() {

		final List<PriceEntity> mockedPriceEntityList = mockPriceEntities(5);
		final ArgumentCaptor<Query> argumentCaptor = ArgumentCaptor.forClass(Query.class);

		mocks(mockedPriceEntityList, argumentCaptor);

		final PriceResultList priceResultList = priceDAO
				.findPriceByIdFilteringSkus(MOCKED_ACCOUNT_ID, MOCKED_COUNTRY, null, new Pagination(0, 5), false, Instant.now());

		defaultAsserts(priceResultList, mockedPriceEntityList);

		final Query query = new Query();
		query.addCriteria(
				Criteria.where(String.format(FILTER_PATTERN, PRICE_ID_FIELD_NAME, PRICE_ACCOUNT_ID_FIELD_NAME)).is(MOCKED_ACCOUNT_ID))
				.addCriteria(Criteria.where(PRICE_DELETED_FIELD_NAME).is(false)).with(PageRequest.of(0, 5));

		assertEquals(argumentCaptor.getValue().getQueryObject(), query.getQueryObject());

	}

	@Test
	void shouldFindByValidAccountIdWhenHaveNoSkusAndNullPaginationFilter() {

		final List<PriceEntity> mockedPriceEntityList = mockPriceEntities(5);
		final ArgumentCaptor<Query> argumentCaptor = ArgumentCaptor.forClass(Query.class);

		when(databaseCollectionProperties.getCollectionByCountry(MOCKED_COUNTRY)).thenReturn(MOCKED_COLLECTION);
		Mockito.doReturn(mockedPriceEntityList).when(mongoOperations)
				.find(argumentCaptor.capture(), eq(PriceEntity.class), eq(MOCKED_COLLECTION));

		final PriceResultList priceResultList = priceDAO
				.findPriceByIdFilteringSkus(MOCKED_ACCOUNT_ID, MOCKED_COUNTRY, null, null, false, Instant.now());

		defaultAsserts(priceResultList, mockedPriceEntityList);

		final Query query = new Query();
		query.addCriteria(
						Criteria.where(String.format(FILTER_PATTERN, PRICE_ID_FIELD_NAME, PRICE_ACCOUNT_ID_FIELD_NAME)).is(MOCKED_ACCOUNT_ID))
				.addCriteria(Criteria.where(PRICE_DELETED_FIELD_NAME).is(false)).with(PageRequest.of(0, 5));

		assertEquals(argumentCaptor.getValue().getQueryObject(), query.getQueryObject());

	}

	@Test
	void shouldFindByValidAccountIdWhenFilterPriceUpFront() {

		final Instant currentDate = Instant.now();

		final List<PriceEntity> mockedPriceEntityList = mockPriceEntities(5);
		final ArgumentCaptor<Query> argumentCaptor = ArgumentCaptor.forClass(Query.class);

		mocks(mockedPriceEntityList, argumentCaptor);

		final PriceResultList priceResultList = priceDAO
				.findPriceByIdFilteringSkus(MOCKED_ACCOUNT_ID, MOCKED_COUNTRY, null, new Pagination(0, 5), true, currentDate);

		defaultAsserts(priceResultList, mockedPriceEntityList);

		final Query query = new Query();

		final Criteria mainFilter = Criteria.where(String.format(FILTER_PATTERN, PRICE_ID_FIELD_NAME, PRICE_ACCOUNT_ID_FIELD_NAME))
				.is(MOCKED_ACCOUNT_ID);

		mainFilter.andOperator(new Criteria()
						.orOperator(Criteria.where(String.format(FILTER_PATTERN, PRICE_ID_FIELD_NAME, VALID_FROM_FIELD_NAME)).lte(currentDate),
								Criteria.where(String.format(FILTER_PATTERN, PRICE_ID_FIELD_NAME, VALID_FROM_FIELD_NAME)).exists(false)),
				new Criteria().orOperator(Criteria.where(VALID_TO_FIELD_NAME).gte(currentDate),
						Criteria.where(VALID_TO_FIELD_NAME).exists(false)));

		query.addCriteria(mainFilter).addCriteria(Criteria.where(PRICE_DELETED_FIELD_NAME).is(false)).with(PageRequest.of(0, 5));

		assertEquals(argumentCaptor.getValue().getQueryObject(), query.getQueryObject());
	}

	private void defaultAsserts(final PriceResultList priceResultList, final List<PriceEntity> mockedPriceEntityList) {

		assertEquals(priceResultList.getPriceEntities(), mockedPriceEntityList);
		assertEquals(5, priceResultList.getPriceEntities().size());

		priceResultList.getPriceEntities().forEach(priceEntity -> {
			assertEquals(MOCKED_ACCOUNT_ID, priceEntity.getId().getAccountId());
			assertTrue(MOCKED_ALL_SKU_LIST.contains(priceEntity.getId().getSku()));
		});

		assertEquals(5, priceResultList.getPagination().getTotalElements());
		assertEquals(1, priceResultList.getPagination().getTotalPages().intValue());
	}

	private void mocks(final List<PriceEntity> mockedPriceEntityList, final ArgumentCaptor<Query> argumentCaptor) {

		when(mongoOperations.count(any(), anyString())).thenReturn(5L);
		when(databaseCollectionProperties.getCollectionByCountry(MOCKED_COUNTRY)).thenReturn(MOCKED_COLLECTION);
		Mockito.doReturn(mockedPriceEntityList).when(mongoOperations)
				.find(argumentCaptor.capture(), eq(PriceEntity.class), eq(MOCKED_COLLECTION));
	}

	private List<PriceEntity> mockPriceEntities(final int qty) {

		final List<PriceEntity> priceEntities = new ArrayList<>();

		for (int i = 1; i <= qty; i++) {
			priceEntities.add(new PriceEntity(TestConstants.MOCKED_BASE_PRICE, TestConstants.MOCKED_MEASURE_UNIT,
					TestConstants.MOCKED_MINIMUM_PRICE, TestConstants.MOCKED_DEPOSIT, TestConstants.MOCKED_CONSIGNMENT,
					TestConstants.MOCKED_ITEM_QUANTITY, null, null, null, new PriceCompoundKey(MOCKED_ACCOUNT_ID, "SKU" + i, null), false,
					MOCKED_COUNTRY, TestConstants.MOCKED_TIMESTAMP, TestConstants.MOCKED_TIME_ZONE_ID, null, null, null));
		}

		return priceEntities;
	}
}
