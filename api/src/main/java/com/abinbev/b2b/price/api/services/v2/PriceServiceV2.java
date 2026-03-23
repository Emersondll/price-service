package com.abinbev.b2b.price.api.services.v2;

import static java.util.Objects.isNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import com.abinbev.b2b.price.api.config.properties.ToggleConfigurationProperties;
import com.abinbev.b2b.price.api.domain.v2.PriceResultListV2;
import com.abinbev.b2b.price.api.exceptions.NotFoundException;
import com.abinbev.b2b.price.api.helpers.CalculateDaysBehindToShowValidUntilHelper;
import com.abinbev.b2b.price.api.repository.PriceEntityV2Repository;
import com.abinbev.b2b.price.api.rest.vo.v2.PaginationResponseVoV2;
import com.abinbev.b2b.price.api.validators.v2.PaginationValidatorV2;
import com.abinbev.b2b.price.domain.model.v2.PriceEntityV2;
import com.newrelic.api.agent.Trace;

@Service
public class PriceServiceV2 {

	// Remove this constant when removing BEESPR_19558
	private static final String CODE_TOGGLE_19558 = "BEESPR_19558";

	private static final Logger LOGGER = LoggerFactory.getLogger(PriceServiceV2.class);
	private static final Integer DEFAULT_PAGE_SIZE = 0;
	private final PriceEntityV2Repository priceRepository;
	private final PriceUpFrontProperties priceUpFrontProperties;
	private final PaginationValidatorV2 paginationValidatorV2;
	private final CalculateDaysBehindToShowValidUntilHelper calculateDaysBehindToShowValidUntilHelper;
	private final PricePriorityByTypeService pricePriorityByTypeService;
	private final ToggleConfigurationProperties toggleConfigurationProperties;

	@Autowired
	public PriceServiceV2(final PriceEntityV2Repository priceRepository, final PriceUpFrontProperties priceUpFrontProperties,
			final PaginationValidatorV2 paginationValidatorV2,
			final CalculateDaysBehindToShowValidUntilHelper calculateDaysBehindToShowValidUntilHelper,
			final PricePriorityByTypeService pricePriorityByTypeService,
			final ToggleConfigurationProperties toggleConfigurationProperties) {

		this.priceRepository = priceRepository;
		this.priceUpFrontProperties = priceUpFrontProperties;
		this.paginationValidatorV2 = paginationValidatorV2;
		this.calculateDaysBehindToShowValidUntilHelper = calculateDaysBehindToShowValidUntilHelper;
		this.pricePriorityByTypeService = pricePriorityByTypeService;
		this.toggleConfigurationProperties = toggleConfigurationProperties;
	}

	@Trace(dispatcher = true)
	public PriceResultListV2 getAllPrices(final String vendorId, final String vendorAccountId, final String country,
			final List<String> vendorItemIds, final String priceListId, final PaginationResponseVoV2 pagination) {

		LOGGER.info("[PriceServiceV2] Starting find the Prices by vendorId {} and vendorAccountId {} for country {}", vendorId, vendorAccountId, country);

		paginationValidatorV2.validatePaginationV2(pagination);

		final boolean priceUpFrontEnabled = priceUpFrontEnabledToCountry(country);

		final Instant currentDate = Instant.now();

		PriceResultListV2 priceResult = priceRepository
				.findPriceByIdFilteringVendorItemId(vendorId, vendorAccountId, country, vendorItemIds, pagination,
						currentDate, priceListId);

		if (CollectionUtils.isEmpty(priceResult.getPriceEntities())) {
			if (StringUtils.isBlank(priceListId)) {
				throw NotFoundException.customerPricesNotFoundV2(vendorId, vendorAccountId, country);
			} else {
				throw NotFoundException.customerPricesWithPriceListIdNotFoundV2(vendorId, vendorAccountId, priceListId, country);
			}
		}
		priceResult = pricePriorityByTypeService.execute(priceResult, country);

		final boolean toggle19558 = toggleConfigurationProperties.isEnabledCodeToggle(CODE_TOGGLE_19558);

		if (priceUpFrontEnabled || toggle19558) {
			priceResult.setPriceEntities(getPriceWithClosestDate(priceResult, currentDate));
			priceResult.getPagination().setTotalElements(priceResult.getPriceEntities().size());
			priceResult.getPagination().setSize(priceResult.getPriceEntities().size());
		}

		for (final PriceEntityV2 priceEntity : priceResult.getPriceEntities()) {
			showValidUntilBasedOnSimulationDateTime(priceEntity);
		}

		LOGGER.info("[PriceServiceV2] {} prices successfully retrieved", priceResult.getPriceEntities().size());

		if (Objects.nonNull(priceListId)) {
			priceResult = adjustPagination(pagination, priceResult);
		}
		return priceResult;
	}

	private PriceResultListV2 adjustPagination(final PaginationResponseVoV2 pagination, PriceResultListV2 priceResult) {

		if (pagination != null) {
			priceResult = new PriceResultListV2(priceResult.getPriceEntities(),
					new PaginationResponseVoV2(pagination.getPage() != null ? pagination.getPage() : DEFAULT_PAGE_SIZE,
							pagination.getSize() != null ? pagination.getSize() : priceResult.getPriceEntities().size(),
							pagination.getTotalElements() != 0 ?
									(int) pagination.getTotalElements() :
									priceResult.getPriceEntities().size()));
		} else {
			priceResult = new PriceResultListV2(priceResult.getPriceEntities(),
					new PaginationResponseVoV2(DEFAULT_PAGE_SIZE, priceResult.getPriceEntities().size(),
							priceResult.getPriceEntities().size()));
		}
		return priceResult;
	}

	// Remove this method when the toggle BEESPR_19558 is removed.
	private boolean priceUpFrontEnabledToCountry(final String country) {

		final String countriesEnabled = priceUpFrontProperties.getCountriesEnabled();

		if (StringUtils.isNotEmpty(countriesEnabled)) {
			return Arrays.stream(countriesEnabled.split(",")).anyMatch(c -> c.equalsIgnoreCase(country));
		}
		return false;
	}

	private List<PriceEntityV2> getPriceWithClosestDate(final PriceResultListV2 priceResult, final Instant currentDate) {

		final Map<Object, List<PriceEntityV2>> priceGroupById = priceResult.getPriceEntities().stream()
				.collect(Collectors.groupingBy(entity -> entity.getId().getVendorItemId()));

		final List<PriceEntityV2> closestDatePrices = new ArrayList<>();

		for (final Entry<Object, List<PriceEntityV2>> entry : priceGroupById.entrySet()) {

			final List<PriceEntityV2> entities = entry.getValue();

			if (entities.size() == NumberUtils.INTEGER_ONE) {
				closestDatePrices.add(entities.get(0));
				continue;
			}

			final Optional<PriceEntityV2> priceWithValidFromAndValidToNull = entities.stream()
					.filter(price -> isNull(price.getId().getValidFrom()) && isNull(price.getValidTo())).findFirst();

			// Remove this constant when removing BEESPR_19558
			final boolean isCodeToggleDisabled = !toggleConfigurationProperties.isEnabledCodeToggle(CODE_TOGGLE_19558);

			if (priceWithValidFromAndValidToNull.isEmpty() || isCodeToggleDisabled) {
				final Optional<PriceEntityV2> priceEntity = entities.stream().filter(price -> price.getId().getValidFrom() != null)
						.filter(entity -> ChronoUnit.HOURS.between(currentDate, entity.getId().getValidFrom()) <= 0)
						.max(Comparator.comparingLong(entity -> ChronoUnit.HOURS.between(currentDate, entity.getId().getValidFrom())));

				priceEntity.ifPresent(closestDatePrices::add);
			} else {
				closestDatePrices.add(priceWithValidFromAndValidToNull.get());
			}
		}
		return closestDatePrices;
	}

	private void showValidUntilBasedOnSimulationDateTime(final PriceEntityV2 priceEntity) {

		if (priceEntity.getPromotionalPrice() != null && priceEntity.getPromotionalPrice().getValidUntil() != null
				&& !calculateDaysBehindToShowValidUntilHelper
				.shouldShowValidUntil(priceEntity.getPromotionalPrice().getValidUntil(), priceEntity.getCountry(),
						priceEntity.getTimezone())) {
			priceEntity.getPromotionalPrice().setValidUntil(null);
		}
	}
}
