package com.abinbev.b2b.price.api.services.v2;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abinbev.b2b.price.api.config.properties.PriceListProperties;
import com.abinbev.b2b.price.api.domain.v2.PriceResultListV2;
import com.abinbev.b2b.price.domain.model.v2.EntityTypeEnum;
import com.abinbev.b2b.price.domain.model.v2.PriceEntityV2;
import com.newrelic.api.agent.Trace;

@Service
public class PricePriorityByTypeService {

	private final PriceListProperties priceListProperties;

	@Autowired
	public PricePriorityByTypeService(final PriceListProperties priceListProperties) {

		this.priceListProperties = priceListProperties;
	}

	@Trace(dispatcher = true)
	public PriceResultListV2 execute(final PriceResultListV2 priceList, final String country) {

		final String type = getPriorityTypeConfigurationByCountry(country);

		final List<PriceEntityV2> listFilteredByToggleType = priceList.getPriceEntities().stream()
				.filter(p -> p.getId().getType().equalsIgnoreCase(type)).collect(Collectors.toList());

		final List<PriceEntityV2> listFilteredDifferentFromToggle = priceList.getPriceEntities().stream()
				.filter(p -> !p.getId().getType().equalsIgnoreCase(type)).collect(Collectors.toList());

		final List<PriceEntityV2> union = ListUtils.union(listFilteredByToggleType, listFilteredDifferentFromToggle);

		final HashSet<Object> seen = new HashSet<>();
		union.removeIf(e -> !seen.add(e.getId().getVendorItemId()) && Objects.isNull(e.getId().getValidFrom()));

		priceList.setPriceEntities(union);

		return priceList;
	}

	protected String getPriorityTypeConfigurationByCountry(final String country) {

		if (List.of(priceListProperties.getCountriesEnabled().toLowerCase().split(",")).contains(country.toLowerCase())) {
			return EntityTypeEnum.PRICE_LIST.toString();
		}

		return EntityTypeEnum.ACCOUNT.toString();
	}
}