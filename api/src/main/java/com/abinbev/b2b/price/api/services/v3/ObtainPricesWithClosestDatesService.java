package com.abinbev.b2b.price.api.services.v3;

import static java.util.Comparator.comparingLong;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abinbev.b2b.price.api.config.properties.ToggleConfigurationProperties;
import com.abinbev.b2b.price.domain.model.v2.PriceEntityV2;
import com.newrelic.api.agent.Trace;

@Service
public class ObtainPricesWithClosestDatesService {

	// Remove this constant when removing BEESPR_19558
	private static final String CODE_TOGGLE_BEESPR_19558 = "BEESPR_19558";
	private final ToggleConfigurationProperties toggleConfigurationProperties;

	@Autowired
	public ObtainPricesWithClosestDatesService(final ToggleConfigurationProperties toggleConfigurationProperties) {

		this.toggleConfigurationProperties = toggleConfigurationProperties;
	}

	@Trace(dispatcher = true)
	public List<PriceEntityV2> execute(final List<PriceEntityV2> priceEntities) {

		final Instant currentDate = Instant.now();
		final Map<Object, List<PriceEntityV2>> pricesGroupedById = getPricesGroupedById(priceEntities);

		final List<PriceEntityV2> closestDatePrices = new ArrayList<>();

		for (final List<PriceEntityV2> entities : pricesGroupedById.values()) {

			if (entities.size() == NumberUtils.INTEGER_ONE) {
				closestDatePrices.add(entities.get(0));
				continue;
			}

			final Optional<PriceEntityV2> priceWithValidFromAndValidToNull = entities.stream()
					.filter(price -> isNull(price.getId().getValidFrom()) && isNull(price.getValidTo())).findFirst();

			// Remove this constant when removing BEESPR_19558
			final boolean isCodeToggleDisabled = !toggleConfigurationProperties.isEnabledCodeToggle(CODE_TOGGLE_BEESPR_19558);

			if (priceWithValidFromAndValidToNull.isEmpty() || isCodeToggleDisabled) {
				final Optional<PriceEntityV2> priceEntity = entities.stream()
						.filter(price -> nonNull(price.getId().getValidFrom()))
						.max(comparingLong(entity -> ChronoUnit.HOURS.between(currentDate, entity.getId().getValidFrom())));

				priceEntity.ifPresent(closestDatePrices::add);
			} else {
				closestDatePrices.add(priceWithValidFromAndValidToNull.get());
			}

		}
		return closestDatePrices;
	}

	private Map<Object, List<PriceEntityV2>> getPricesGroupedById(final List<PriceEntityV2> priceEntities) {

		final Function<PriceEntityV2, List<Object>> compositeKey = price ->
				Arrays.asList(price.getId().getId(), price.getId().getType(), price.getId().getVendorItemId(), price.getId().getVendorId());

		return priceEntities.stream().collect(Collectors.groupingBy(compositeKey, Collectors.toList()));
	}

}
