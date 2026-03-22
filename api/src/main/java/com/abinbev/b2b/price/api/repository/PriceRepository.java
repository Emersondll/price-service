package com.abinbev.b2b.price.api.repository;

import java.time.Instant;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.abinbev.b2b.price.api.config.properties.DatabaseCollectionProperties;
import com.abinbev.b2b.price.api.domain.PriceResultList;
import com.abinbev.b2b.price.api.rest.vo.Pagination;
import com.abinbev.b2b.price.domain.model.PriceEntity;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.Trace;

@Repository
public class PriceRepository {

	private static final String PRICE_ID_FIELD_NAME = "_id";
	private static final String PRICE_SKU_FIELD_NAME = "sku";
	private static final String PRICE_ACCOUNT_ID_FIELD_NAME = "accountId";
	private static final String PRICE_DELETED_FIELD_NAME = "deleted";
	private static final String FILTER_PATTERN = "%s.%s";
	private static final Integer DEFAULT_PAGE_SIZE = 0;
	private static final String VALID_TO_FIELD_NAME = "validTo";
	private static final String VALID_FROM_FIELD_NAME = "validFrom";

	private final MongoOperations mongoOperations;
	private final DatabaseCollectionProperties databaseCollectionProperties;

	@Autowired
	public PriceRepository(final MongoOperations mongoOperations, final DatabaseCollectionProperties databaseCollectionProperties) {

		this.databaseCollectionProperties = databaseCollectionProperties;
		this.mongoOperations = mongoOperations;
	}

	@Trace(dispatcher = true)
	public PriceResultList findPriceByIdFilteringSkus(final String accountId, final String country, final List<String> skus,
			final Pagination pagination, final boolean priceUpFrontEnabled, final Instant currentDate) {

		final Token token = NewRelic.getAgent().getTransaction().getToken();
		return findPriceByIdFilteringSkus(accountId, country, skus, pagination, token, priceUpFrontEnabled, currentDate);
	}

	private PriceResultList findPriceByIdFilteringSkus(final String accountId, final String country, final List<String> skus,
			final Pagination paginationFilter, final Token token, final boolean priceUpFrontEnabled, final Instant currentDate) {

		try {
			token.link();

			final Query query = new Query();

			Pagination pagination = null;

			final Criteria mainFilter = Criteria.where(String.format(FILTER_PATTERN, PRICE_ID_FIELD_NAME, PRICE_ACCOUNT_ID_FIELD_NAME))
					.is(accountId);

			query.addCriteria(Criteria.where(PRICE_DELETED_FIELD_NAME).is(false));

			if (CollectionUtils.isNotEmpty(skus)) {
				query.addCriteria(Criteria.where(String.format(FILTER_PATTERN, PRICE_ID_FIELD_NAME, PRICE_SKU_FIELD_NAME)).in(skus));
			}

			createPriceUpFrontFilter(priceUpFrontEnabled, currentDate, mainFilter);

			query.addCriteria(mainFilter);

			final String collectionByCountry = databaseCollectionProperties.getCollectionByCountry(country);

			if (paginationFilter != null) {
				pagination = new Pagination(paginationFilter.getPage(), paginationFilter.getSize(),
						countByFilter(collectionByCountry, query));

				query.with(PageRequest.of(pagination.getPage(), pagination.getSize()));
			}
			query.maxTimeMsec(databaseCollectionProperties.getQueryTimeoutMS());

			final List<PriceEntity> priceEntityList = mongoOperations.find(query, PriceEntity.class, collectionByCountry);

			return new PriceResultList(priceEntityList,
					pagination != null ? pagination : new Pagination(DEFAULT_PAGE_SIZE, priceEntityList.size(), priceEntityList.size()));

		} finally {
			token.expire();
		}
	}

	private int countByFilter(final String collectionName, final Query query) {

		return Math.toIntExact(mongoOperations.count(query, collectionName));
	}

	private void createPriceUpFrontFilter(final boolean priceUpFrontEnabled, final Instant currentDate, final Criteria mainFilter) {

		if (priceUpFrontEnabled) {

			mainFilter.andOperator(new Criteria()
							.orOperator(Criteria.where(String.format(FILTER_PATTERN, PRICE_ID_FIELD_NAME, VALID_FROM_FIELD_NAME)).lte(currentDate),
									Criteria.where(String.format(FILTER_PATTERN, PRICE_ID_FIELD_NAME, VALID_FROM_FIELD_NAME)).exists(false)),
					new Criteria().orOperator(Criteria.where(VALID_TO_FIELD_NAME).gte(currentDate),
							Criteria.where(VALID_TO_FIELD_NAME).exists(false)));
		}
	}
}