package com.abinbev.b2b.price.api.repository;

import static com.abinbev.b2b.price.domain.model.v2.EntityTypeEnum.ACCOUNT;
import static com.abinbev.b2b.price.domain.model.v2.EntityTypeEnum.DELIVERY_CENTER;
import static com.abinbev.b2b.price.domain.model.v2.EntityTypeEnum.PRICE_LIST;
import static com.abinbev.b2b.price.domain.model.v2.EntityTypeEnum.VENDOR;
import static java.lang.Boolean.FALSE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.MapUtils.isNotEmpty;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.abinbev.b2b.price.api.config.ApiConfig;
import com.abinbev.b2b.price.api.config.properties.DatabaseCollectionPropertiesV2;
import com.abinbev.b2b.price.api.config.properties.ToggleConfigurationProperties;
import com.abinbev.b2b.price.api.domain.v2.PriceResultListV2;
import com.abinbev.b2b.price.api.domain.v3.PriceNormalizedInfo;
import com.abinbev.b2b.price.api.helpers.ToggleValues;
import com.abinbev.b2b.price.api.rest.vo.v2.PaginationResponseVoV2;
import com.abinbev.b2b.price.domain.model.v2.EntityTypeEnum;
import com.abinbev.b2b.price.domain.model.v2.PriceEntityV2;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.Trace;

@Repository
public class PriceEntityV2Repository {
	private static final Logger LOGGER = LoggerFactory.getLogger(PriceEntityV2Repository.class);

	private static final String PRICE_VENDOR_ID_FIELD_NAME = "_id.vendorId";
	private static final String PRICE_VENDOR_ITEM_ID_FIELD_NAME = "_id.vendorItemId";
	private static final String PRICE_ID_FIELD_NAME = "_id.id";
	private static final String PRICE_DELETED_FIELD_NAME = "deleted";
	private static final Integer DEFAULT_PAGE_SIZE = 0;
	private static final String VALID_TO_FIELD_NAME = "validTo";
	private static final String VALID_FROM_FIELD_NAME = "_id.validFrom";
	private static final String PRICE_ID_TYPE_FIELD_NAME = "_id.type";

	private final MongoOperations mongoOperations;
	private final DatabaseCollectionPropertiesV2 databaseCollectionProperties;

	private final ToggleConfigurationProperties toggleConfigurationProperties;
	private final ApiConfig apiConfig;

	@Autowired
	public PriceEntityV2Repository(final MongoOperations mongoOperations,
			final DatabaseCollectionPropertiesV2 databaseCollectionProperties, final ToggleConfigurationProperties toggleConfigurationProperties,
			ApiConfig apiConfig) {

		this.mongoOperations = mongoOperations;
		this.databaseCollectionProperties = databaseCollectionProperties;
		this.toggleConfigurationProperties = toggleConfigurationProperties;
		this.apiConfig = apiConfig;
	}

	@Trace(dispatcher = true)
	public PriceResultListV2 findPriceByIdFilteringVendorItemId(final String vendorId, final String vendorAccountId, final String country,
			final List<String> vendorItemId, final PaginationResponseVoV2 paginationFilter, final Instant currentDate,
			final String priceListId) {

		final Token token = NewRelic.getAgent().getTransaction().getToken();

		try {
			token.link();
			PaginationResponseVoV2 pagination = null;

			final Query query = addCriteria(vendorId, vendorAccountId, vendorItemId, currentDate, priceListId);
			query.maxTimeMsec(databaseCollectionProperties.getQueryTimeoutMS());

			final String collectionByCountry = databaseCollectionProperties.getCollectionByCountryV2(country);
			if (paginationFilter != null) {
				pagination = new PaginationResponseVoV2(paginationFilter.getPage(), paginationFilter.getSize(),
						countByFilter(collectionByCountry, query));

				query.with(PageRequest.of(pagination.getPage(), pagination.getSize()));
			}

			LOGGER.info("[PriceEntityV2Repository] Starting finding prices by id filtering vendorItemId");

			final List<PriceEntityV2> priceEntityList = mongoOperations.find(query, PriceEntityV2.class, collectionByCountry);

			LOGGER.info("[PriceEntityV2Repository] Finished finding prices by id filtering vendorItemId returning {} items", priceEntityList.size());

			return new PriceResultListV2(priceEntityList, pagination != null ?
					pagination :
					new PaginationResponseVoV2(DEFAULT_PAGE_SIZE, priceEntityList.size(), priceEntityList.size()));

		} finally {
			token.expire();
		}
	}

	@Trace(dispatcher = true)
	public List<PriceEntityV2> findPricesByAccountPriceForContractAndContractlessList(final List<PriceNormalizedInfo> priceNormalizedInfoList, final String country,
																					  final Instant currentDate, final Boolean ignoreValidFrom) {

		final Token token = NewRelic.getAgent().getTransaction().getToken();

		try {
			token.link();

			final Query query = createFilterByAccountPriceListForContractAndContractlessCriteria(priceNormalizedInfoList, currentDate, ignoreValidFrom);
			query.maxTimeMsec(databaseCollectionProperties.getQueryTimeoutMS());

			final String collectionByCountry = databaseCollectionProperties.getCollectionByCountryV2(country);

			return mongoOperations.find(query, PriceEntityV2.class, collectionByCountry);
		}
		catch( IllegalArgumentException e) {
			LOGGER.info(e.getMessage());
			return new ArrayList<>();
		}
		finally {
			token.expire();
		}
	}

	@Trace(dispatcher = true)
	public List<PriceEntityV2> findPricesByDDCVendorList(final List<PriceNormalizedInfo> priceNormalizedInfoList, final String country,
															final Instant currentDate, final Boolean ignoreValidFrom) {

		final Token token = NewRelic.getAgent().getTransaction().getToken();

		try {
			token.link();

			final Query query = createFilterByDDCVendor(priceNormalizedInfoList, currentDate, ignoreValidFrom);
			query.maxTimeMsec(databaseCollectionProperties.getQueryTimeoutMS());

			final String collectionByCountry = databaseCollectionProperties.getCollectionByCountryV2(country);

			return mongoOperations.find(query, PriceEntityV2.class, collectionByCountry);

		}
		catch( IllegalArgumentException e) {
			LOGGER.info(e.getMessage());
			return new ArrayList<>();
		}
		finally {
			token.expire();
		}
	}

	private Query addCriteria(final String vendorId, final String vendorAccountId, final List<String> vendorItemId,
			final Instant currentDate, final String priceListId) {

		final Query query = new Query();

		final List<Criteria> criteriaList = new ArrayList<>();
		criteriaList.add(criteriaType(vendorAccountId, priceListId));

		query.addCriteria(Criteria.where(PRICE_DELETED_FIELD_NAME).is(false).and(PRICE_VENDOR_ID_FIELD_NAME).is(vendorId));

		if (CollectionUtils.isNotEmpty(vendorItemId)) {
			query.addCriteria(Criteria.where(PRICE_VENDOR_ITEM_ID_FIELD_NAME).in(vendorItemId));
		}

		createPriceUpFrontFilter(currentDate, criteriaList);
		query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));

		//Remove the if and keep the query.limit(1) when the toggle BEESPR_22389 is removed
		if (toggleConfigurationProperties.isEnabledCodeToggle(ToggleValues.BEESPR_22389)) {
			query.limit(1);
		}

		return query;
	}


	private Criteria criteriaType(final String vendorAccountId, final String priceListId) {

		final Criteria typeFilter = new Criteria();
		if (Objects.nonNull(priceListId)) {
			typeFilter.orOperator(criteriaForVendorAccount(vendorAccountId), criteriaForPriceList(priceListId));
		} else {
			typeFilter.andOperator(criteriaForVendorAccount(vendorAccountId));
		}
		return typeFilter;
	}

	private Criteria criteriaForVendorAccount(final String vendorAccountId) {

		return new Criteria().andOperator(Criteria.where(PRICE_ID_FIELD_NAME).
				is(vendorAccountId), Criteria.where(PRICE_ID_TYPE_FIELD_NAME).is(ACCOUNT.toString()));
	}

	private Criteria criteriaForPriceList(final String priceListId) {

		return new Criteria().andOperator(Criteria.where(PRICE_ID_FIELD_NAME).is(priceListId),
				Criteria.where(PRICE_ID_TYPE_FIELD_NAME).is(EntityTypeEnum.PRICE_LIST.toString()));
	}

	private int countByFilter(final String collectionName, final Query query) {

		return Math.toIntExact(mongoOperations.count(query, collectionName));
	}

	private void createPriceUpFrontFilter(final Instant currentDate, final List<Criteria> criteriaList) {

		criteriaList.add(new Criteria().andOperator(new Criteria().orOperator(Criteria.where(VALID_FROM_FIELD_NAME).lte(currentDate),
				Criteria.where(VALID_FROM_FIELD_NAME).exists(false)), new Criteria()
				.orOperator(Criteria.where(VALID_TO_FIELD_NAME).gte(currentDate), Criteria.where(VALID_TO_FIELD_NAME).exists(false))));
	}

	private Query createFilterByAccountPriceListForContractAndContractlessCriteria(final List<PriceNormalizedInfo> priceNormalizedInfoList, final Instant currentDate,
																				   final Boolean ignoreValidFrom) {

		final Query query = new Query();
		final List<Criteria> criteriaList = new ArrayList<>();

		final Map<String, List<PriceNormalizedInfo>> pricesByContract = priceNormalizedInfoList.stream()
				.filter(price -> nonNull(price.getContractId())).collect(Collectors.groupingBy(PriceNormalizedInfo::getContractId));

		if (isNotEmpty(pricesByContract)) {

			for (final List<PriceNormalizedInfo> priceNormalizedInfoListByContract : pricesByContract.values()) {

				final List<String> vendorItemIds = priceNormalizedInfoListByContract.stream().map(PriceNormalizedInfo::getVendorItemId)
						.collect(Collectors.toList());

				criteriaList.addAll(createCriteriaByPriceAccountPriceList(priceNormalizedInfoListByContract.get(0), vendorItemIds));
			}
		}

		final Map<String, List<PriceNormalizedInfo>> pricesByDeliveryCenter = priceNormalizedInfoList.stream()
				.filter(price -> nonNull(price.getDeliveryCenterId()) && isNull(price.getContractId()))
				.collect(Collectors.groupingBy(PriceNormalizedInfo::getDeliveryCenterId));

		buildCriteriaWithVendorItemFromDDCRequest(pricesByDeliveryCenter, criteriaList);

		if (criteriaList.isEmpty()) {
			throw new IllegalArgumentException("query with no keys skipped");
		}

		final Criteria criteriaDefinition = getPricesUpfrontCriteria(criteriaList, currentDate, ignoreValidFrom, query);

		return query.addCriteria(criteriaDefinition);
	}

	private Query createFilterByDDCVendor(final List<PriceNormalizedInfo> priceNormalizedInfoList, final Instant currentDate,
											 final Boolean ignoreValidFrom) {

		final Query query = new Query();
		final List<Criteria> criteriaList = new ArrayList<>();

		final Map<String, List<PriceNormalizedInfo>> pricesByDeliveryCenter = priceNormalizedInfoList.stream()
				.filter(price -> nonNull(price.getContractId()))
				.collect(Collectors.groupingBy(PriceNormalizedInfo::getContractId));

		buildCriteriaWithVendorItemFromDDCRequest(pricesByDeliveryCenter, criteriaList);

		if (criteriaList.isEmpty()) {
			throw new IllegalArgumentException("query with no keys skipped");
		}

		final Criteria criteriaDefinition = getPricesUpfrontCriteria(criteriaList, currentDate, ignoreValidFrom, query);

		return query.addCriteria(criteriaDefinition);
	}

	private void buildCriteriaWithVendorItemFromDDCRequest(Map<String, List<PriceNormalizedInfo>> pricesByDeliveryCenter, List<Criteria> criteriaList) {

		if (isNotEmpty(pricesByDeliveryCenter) ) {

			for (final List<PriceNormalizedInfo> priceNormalizedInfoListByDeliveryCenter : pricesByDeliveryCenter.values()) {

				if (shouldNotHaveQueryWithDDCAndVendorIdForSomeVendors(priceNormalizedInfoListByDeliveryCenter)) {
					continue;
				}

				final List<String> vendorItemIds = priceNormalizedInfoListByDeliveryCenter.stream()
						.map(PriceNormalizedInfo::getVendorItemId)
						.collect(Collectors.toList());

				criteriaList.addAll(
						createCriteriaByPriceDDCVendor(priceNormalizedInfoListByDeliveryCenter.get(0), vendorItemIds));
			}
		}
	}

	private boolean shouldNotHaveQueryWithDDCAndVendorIdForSomeVendors(List<PriceNormalizedInfo> priceNormalizedInfoListByDeliveryCenter) {

		String vendorId = priceNormalizedInfoListByDeliveryCenter.get(0).getVendorId();
		return apiConfig.getExcludedDDCAndVendorIdFromQueryAsList().contains(vendorId);
	}

	@NotNull
	private Criteria getPricesUpfrontCriteria(List<Criteria> pCriteriaList, Instant currentDate, Boolean ignoreValidFrom, Query query) {

		List<Criteria> criteriaList = new ArrayList<>();
		criteriaList.add(new Criteria().orOperator(pCriteriaList));
		criteriaList.add(Criteria.where(PRICE_DELETED_FIELD_NAME).is(false));
		criteriaList.add(new Criteria().orOperator(Criteria.where(VALID_TO_FIELD_NAME).gte(currentDate),Criteria.where(VALID_TO_FIELD_NAME).exists(false)));

		final Criteria criteriaDefinition = new Criteria().andOperator( criteriaList );

		if (isNull(ignoreValidFrom) || FALSE.equals(ignoreValidFrom)) {
			criteriaDefinition.orOperator(new Criteria().orOperator(Criteria.where(VALID_FROM_FIELD_NAME).lte(currentDate),
					Criteria.where(VALID_FROM_FIELD_NAME).exists(false)));
		}

		//Remove the if and keep the query.limit(1) when the toggle BEESPR_22389 is removed
		if (toggleConfigurationProperties.isEnabledCodeToggle(ToggleValues.BEESPR_22389)) {
			query.limit(1);
		}
		return criteriaDefinition;
	}

	private List<Criteria> createCriteriaByPriceAccountPriceList(final PriceNormalizedInfo priceNormalizedInfo,
														  final List<String> vendorItemsIds) {

		final List<Criteria> criteriaList = new ArrayList<>();

		createCriteriaAccountPriceList(priceNormalizedInfo, vendorItemsIds, criteriaList);

		return criteriaList;
	}

	private List<Criteria> createCriteriaByPriceDDCVendor(final PriceNormalizedInfo priceNormalizedInfo,
																 final List<String> vendorItemsIds) {

		final List<Criteria> criteriaList = new ArrayList<>();

		createCriteriaDDCVendor(priceNormalizedInfo, vendorItemsIds, criteriaList);

		return criteriaList;
	}

	private void createCriteriaDDCVendor(PriceNormalizedInfo priceNormalizedInfo, List<String> vendorItemsIds, List<Criteria> criteriaList) {

		if (Objects.nonNull(priceNormalizedInfo.getVendorDeliveryCenterId())) {
			final Criteria criteriaByTypeDeliveryCenter = Criteria.where(PRICE_ID_FIELD_NAME)
					.is(priceNormalizedInfo.getVendorDeliveryCenterId())
					.and(PRICE_VENDOR_ITEM_ID_FIELD_NAME).in(vendorItemsIds)
					.and(PRICE_VENDOR_ID_FIELD_NAME).is(priceNormalizedInfo.getVendorId())
					.and(PRICE_ID_TYPE_FIELD_NAME).is(DELIVERY_CENTER.name());

			criteriaList.add(criteriaByTypeDeliveryCenter);
		}

		final Criteria criteriaByTypeVendorId = Criteria.where(PRICE_ID_FIELD_NAME).is(priceNormalizedInfo.getVendorId())
				.and(PRICE_VENDOR_ITEM_ID_FIELD_NAME).in(vendorItemsIds)
				.and(PRICE_VENDOR_ID_FIELD_NAME).is(priceNormalizedInfo.getVendorId())
				.and(PRICE_ID_TYPE_FIELD_NAME).is(VENDOR.name());

		criteriaList.add(criteriaByTypeVendorId);
	}

	private void createCriteriaAccountPriceList(PriceNormalizedInfo priceNormalizedInfo, List<String> vendorItemsIds, List<Criteria> criteriaList) {

		if (Objects.nonNull(priceNormalizedInfo.getAccountId())) {
			final Criteria criteriaByTypeAccount = Criteria.where(PRICE_ID_FIELD_NAME).is(priceNormalizedInfo.getAccountId())
					.and(PRICE_VENDOR_ITEM_ID_FIELD_NAME).in(vendorItemsIds)
					.and(PRICE_VENDOR_ID_FIELD_NAME).is(priceNormalizedInfo.getVendorId())
					.and(PRICE_ID_TYPE_FIELD_NAME).is(ACCOUNT.name());

			criteriaList.add(criteriaByTypeAccount);
		}

		if (Objects.nonNull(priceNormalizedInfo.getPriceListId())) {
			final Criteria criteriaByTypePriceList = Criteria.where(PRICE_ID_FIELD_NAME).is(priceNormalizedInfo.getPriceListId())
					.and(PRICE_VENDOR_ITEM_ID_FIELD_NAME).in(vendorItemsIds)
					.and(PRICE_VENDOR_ID_FIELD_NAME).is(priceNormalizedInfo.getVendorId())
					.and(PRICE_ID_TYPE_FIELD_NAME).is(PRICE_LIST.name());

			criteriaList.add(criteriaByTypePriceList);
		}
	}

}