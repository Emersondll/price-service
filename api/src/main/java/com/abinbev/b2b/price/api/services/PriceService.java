package com.abinbev.b2b.price.api.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abinbev.b2b.price.api.config.properties.PriceUpFrontProperties;
import com.abinbev.b2b.price.api.domain.PriceResultList;
import com.abinbev.b2b.price.api.exceptions.NotFoundException;
import com.abinbev.b2b.price.api.helpers.CalculateDaysBehindToShowValidUntilHelper;
import com.abinbev.b2b.price.api.repository.PriceRepository;
import com.abinbev.b2b.price.api.rest.vo.Pagination;
import com.abinbev.b2b.price.api.rest.vo.PriceResponseVo;
import com.abinbev.b2b.price.api.validators.PaginationValidator;
import com.abinbev.b2b.price.domain.model.PriceEntity;
import com.newrelic.api.agent.Trace;

@Service
public class PriceService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PriceService.class);

	private final PriceRepository priceDAO;
	private final PriceUpFrontProperties priceUpFrontProperties;
	private final PaginationValidator paginationValidator;
	private final CalculateDaysBehindToShowValidUntilHelper calculateDaysBehindToShowValidUntilHelper;
	@Autowired
	public PriceService(final PriceRepository priceDAO, final PriceUpFrontProperties priceUpFrontProperties,
			final PaginationValidator paginationValidator,
			final CalculateDaysBehindToShowValidUntilHelper calculateDaysBehindToShowValidUntilHelper) {

		this.priceDAO = priceDAO;
		this.priceUpFrontProperties = priceUpFrontProperties;
		this.paginationValidator = paginationValidator;
		this.calculateDaysBehindToShowValidUntilHelper = calculateDaysBehindToShowValidUntilHelper;
	}

	@Trace(dispatcher = true)
	public PriceResponseVo getAllPrices(final String accountId, final String country, final List<String> skus,
			final Pagination pagination) {

		LOGGER.debug("Starting find the Prices by accountId {} for country {}", accountId, country);

		paginationValidator.validatePagination(pagination);

		final boolean priceUpFrontEnabled = priceUpFrontEnabledToCountry(country);

		final Instant currentDate = Instant.now();

		final PriceResultList priceResult = priceDAO
				.findPriceByIdFilteringSkus(accountId, country, skus, pagination, priceUpFrontEnabled, currentDate);

		if (Objects.isNull(priceResult) || CollectionUtils.isEmpty(priceResult.getPriceEntities())) {
			throw NotFoundException.customerPricesNotFound(accountId, country);
		}

		if (priceUpFrontEnabled) {
			priceResult.setPriceEntities(getPriceWithClosestDate(priceResult, currentDate));
			priceResult.getPagination().setTotalElements(priceResult.getPriceEntities().size());
			priceResult.getPagination().setSize(priceResult.getPriceEntities().size());
		}

		for (final PriceEntity priceEntity : priceResult.getPriceEntities()) {
			showValidUntilBasedOnSimulationDateTime(priceEntity);
		}

		LOGGER.debug("All prices successfully retrieved");

		return createPriceResponseVo(priceResult);
	}

	private PriceResponseVo createPriceResponseVo(final PriceResultList priceResultList) {

		return new PriceResponseVo(priceResultList.getPriceEntities(), priceResultList.getPagination());
	}

	private boolean priceUpFrontEnabledToCountry(final String country) {

		final String countriesEnabled = priceUpFrontProperties.getCountriesEnabled();

		if (StringUtils.isNotEmpty(countriesEnabled)) {
			return Arrays.stream(countriesEnabled.split(",")).anyMatch(c -> c.equalsIgnoreCase(country));
		}

		return false;
	}

	private List<PriceEntity> getPriceWithClosestDate(final PriceResultList priceResult, final Instant currentDate) {

		final Map<String, List<PriceEntity>> priceGroupById = priceResult.getPriceEntities().stream()
				.collect(Collectors.groupingBy(entity -> entity.getId().getSku()));

		final List<PriceEntity> closestDatePrice = new ArrayList<>();

		for (final List<PriceEntity> entities : priceGroupById.values()) {

			if (entities.size() > NumberUtils.INTEGER_ONE) {

				final Optional<PriceEntity> priceEntity = entities.stream().filter(price -> price.getId().getValidFrom() != null)
						.filter(entity -> ChronoUnit.HOURS.between(currentDate, entity.getId().getValidFrom()) <= 0)
						.max(Comparator.comparingLong(entity -> ChronoUnit.HOURS.between(currentDate, entity.getId().getValidFrom())));

				priceEntity.ifPresent(closestDatePrice::add);
			} else {
				closestDatePrice.add(entities.get(0));
			}
		}

		return closestDatePrice;
	}

	private void showValidUntilBasedOnSimulationDateTime(final PriceEntity priceEntity) {

		if (priceEntity.getPromotionalPrice() != null && priceEntity.getPromotionalPrice().getValidUntil() != null
				&& !calculateDaysBehindToShowValidUntilHelper
				.shouldShowValidUntil(priceEntity.getPromotionalPrice().getValidUntil(), priceEntity.getCountry(),
						priceEntity.getTimezone())) {
			priceEntity.getPromotionalPrice().setValidUntil(null);
		}
	}
}
