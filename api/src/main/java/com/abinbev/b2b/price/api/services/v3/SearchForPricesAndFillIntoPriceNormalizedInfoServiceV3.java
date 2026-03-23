package com.abinbev.b2b.price.api.services.v3;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.MapUtils.isNotEmpty;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abinbev.b2b.price.api.config.properties.PriceUpFrontProperties;
import com.abinbev.b2b.price.api.config.properties.ToggleConfigurationProperties;
import com.abinbev.b2b.price.api.domain.v3.PriceNormalizedInfo;
import com.abinbev.b2b.price.api.helpers.CalculateDaysBehindToShowValidUntilHelper;
import com.abinbev.b2b.price.api.repository.PriceEntityV2Repository;
import com.abinbev.b2b.price.domain.model.v2.PriceEntityV2;
import com.abinbev.b2b.price.domain.model.v2.PromotionalPriceV2;

@Service
public class SearchForPricesAndFillIntoPriceNormalizedInfoServiceV3 {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchForPricesAndFillIntoPriceNormalizedInfoServiceV3.class);
	private static final String CODE_TOGGLE_19558 = "BEESPR_19558";

	private final PriceEntityV2Repository priceEntityV2Repository;
	private final PriceUpFrontProperties priceUpFrontProperties;
	private final ObtainPricesWithClosestDatesService obtainPricesWithClosestDatesService;
	private final PrioritizePriceByTypeServiceV3 prioritizePriceByTypeServiceV3;
	private final CalculateDaysBehindToShowValidUntilHelper calculateDaysBehindToShowValidUntilHelper;
	private final ToggleConfigurationProperties toggleConfigurationProperties;

	@Autowired
	public SearchForPricesAndFillIntoPriceNormalizedInfoServiceV3(final PriceEntityV2Repository priceEntityV2Repository,
			final PriceUpFrontProperties priceUpFrontProperties,
			final ObtainPricesWithClosestDatesService obtainPricesWithClosestDatesService,
			final PrioritizePriceByTypeServiceV3 prioritizePriceByTypeServiceV3,
			final CalculateDaysBehindToShowValidUntilHelper calculateDaysBehindToShowValidUntilHelper,
			final ToggleConfigurationProperties toggleConfigurationProperties) {

		this.priceEntityV2Repository = priceEntityV2Repository;
		this.priceUpFrontProperties = priceUpFrontProperties;
		this.obtainPricesWithClosestDatesService = obtainPricesWithClosestDatesService;
		this.prioritizePriceByTypeServiceV3 = prioritizePriceByTypeServiceV3;
		this.calculateDaysBehindToShowValidUntilHelper = calculateDaysBehindToShowValidUntilHelper;
		this.toggleConfigurationProperties = toggleConfigurationProperties;
	}

	public List<PriceNormalizedInfo> execute(final List<PriceNormalizedInfo> priceNormalizedInfoList, final String country, Boolean ignoreValidFrom) {

		final boolean isPriceUpFrontEnabled = isPriceUpFrontEnabledToCountry(country);

		LOGGER.info("[SearchForPricesAndFillIntoPriceNormalizedInfoServiceV3] Starting finding prices");

		List<PriceEntityV2> pricesList = priceEntityV2Repository.findPricesByAccountPriceForContractAndContractlessList(priceNormalizedInfoList, country, Instant.now(), ignoreValidFrom);

		List<PriceEntityV2> finalPricesList = pricesList;

		List<PriceNormalizedInfo> priceNormalizedInfoNotFoundList = getPriceNormalizedInfoNotFoundList(priceNormalizedInfoList, finalPricesList);

		if(!priceNormalizedInfoNotFoundList.isEmpty()){
			List<PriceEntityV2> pricesDDCVendorPriceList = priceEntityV2Repository.findPricesByDDCVendorList(priceNormalizedInfoNotFoundList, country, Instant.now(), ignoreValidFrom);
			pricesList.addAll(pricesDDCVendorPriceList);
		}

		LOGGER.info("[SearchForPricesAndFillIntoPriceNormalizedInfoServiceV3] Finished finding prices and returning {} items", pricesList.size());

		if (nonNull(ignoreValidFrom) && TRUE.equals(ignoreValidFrom)) {
            return getPriceNormalizedInfoIgnoringValidFrom(priceNormalizedInfoList, pricesList);
		}

		final boolean toggle19558 = toggleConfigurationProperties.isEnabledCodeToggle(CODE_TOGGLE_19558);

		if (isPriceUpFrontEnabled || toggle19558) {
			pricesList = obtainPricesWithClosestDatesService.execute(pricesList);
		}
		prioritizePriceByTypeServiceV3.execute(pricesList, priceNormalizedInfoList);

		priceNormalizedInfoList.forEach(priceNormalizedInfo -> ofNullable(priceNormalizedInfo.getSelectedPrice()).ifPresent(
				priceEntityV2 -> showValidUntilBasedOnSimulationDateTime(priceEntityV2, country)));

		LOGGER.info("[SearchForPricesAndFillIntoPriceNormalizedInfoServiceV3] {} prices successfully retrieved", priceNormalizedInfoList.size());

		return priceNormalizedInfoList;
	}

	List<PriceNormalizedInfo> getPriceNormalizedInfoNotFoundList(
			List<PriceNormalizedInfo> priceNormalizedInfoList,
			List<PriceEntityV2> finalPricesList) {

		return priceNormalizedInfoList
				.stream()
				.filter( priceNormalizedInfo -> finalPricesList
						.stream()
						.filter(priceEntityV2 -> nonNull(priceEntityV2.getId()))
						.noneMatch( priceEntityV2 ->
								( priceNormalizedInfo.getVendorId().equals( priceEntityV2.getId().getVendorId()))
								&
								( priceNormalizedInfo.getVendorItemId().equals(priceEntityV2.getId().getVendorItemId()))
						)
				).collect(Collectors.toList());
	}

	private List<PriceNormalizedInfo> getPriceNormalizedInfoIgnoringValidFrom(List<PriceNormalizedInfo> priceNormalizedInfoList, List<PriceEntityV2> prices) {
        List<PriceNormalizedInfo> priceNormalizedInfoListResult = new ArrayList<>();

        priceNormalizedInfoList.forEach(priceNormalizedInfo ->
                    priceNormalizedInfoListResult.addAll(
                            prices.stream()
                                    .filter(Objects::nonNull)
                                    .filter(priceEntityV2 -> priceEntityV2.getId().getVendorItemId().equals(priceNormalizedInfo.getVendorItemId()))
                                    .map(priceEntityV2 -> new PriceNormalizedInfo(priceNormalizedInfo, priceEntityV2))
                                    .collect(Collectors.toList()))
                );
        return priceNormalizedInfoListResult;
    }

	private boolean isPriceUpFrontEnabledToCountry(final String country) {

		final String countriesEnabled = priceUpFrontProperties.getCountriesEnabled();
		if (StringUtils.isNotEmpty(countriesEnabled)) {
			return Arrays.stream(countriesEnabled.split(",")).anyMatch(c -> c.equalsIgnoreCase(country));
		}
		return false;
	}

	private void showValidUntilBasedOnSimulationDateTime(final PriceEntityV2 priceEntity, final String country) {

		if (hasPromotionalPriceAndValidUntil(priceEntity) && !calculateDaysBehindToShowValidUntilHelper.shouldShowValidUntil(
				priceEntity.getPromotionalPrice().getValidUntil(), country, priceEntity.getTimezone())) {
			priceEntity.getPromotionalPrice().setValidUntil(null);
		}
	}

	private boolean hasPromotionalPriceAndValidUntil(final PriceEntityV2 priceEntity) {

		return ofNullable(priceEntity.getPromotionalPrice()).map(PromotionalPriceV2::getValidUntil).isPresent();
	}
}
