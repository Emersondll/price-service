package com.abinbev.b2b.price.api.services.v2;

import static org.apache.commons.collections4.MapUtils.isEmpty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abinbev.b2b.price.api.converters.v2.PriceEntityV2ToPriceFactConverter;
import com.abinbev.b2b.price.api.domain.PriceFact;
import com.abinbev.b2b.price.api.domain.v2.PriceResultListV2;
import com.abinbev.b2b.price.api.exceptions.NotFoundException;
import com.abinbev.b2b.price.domain.model.v2.PriceEntityV2;

@Service
public class PricesV2MappedByVendorItemIdService {

	private final PriceServiceV2 priceServiceV2;
	private final PriceEntityV2ToPriceFactConverter priceEntityV2ToPriceFactConverter;

	@Autowired
	public PricesV2MappedByVendorItemIdService(final PriceServiceV2 priceServiceV2,
			final PriceEntityV2ToPriceFactConverter priceEntityV2ToPriceFactConverter) {

		this.priceServiceV2 = priceServiceV2;
		this.priceEntityV2ToPriceFactConverter = priceEntityV2ToPriceFactConverter;
	}

	public Map<String, PriceFact> execute(final String vendorId, final String vendorAccountId, final String country,
			final List<String> vendorItemIds) {

		final PriceResultListV2 allPrices = priceServiceV2.getAllPrices(vendorId, vendorAccountId, country, vendorItemIds, null, null);

		final Map<String, PriceFact> mappedPrices = new HashMap<>();

		for (final PriceEntityV2 priceEntityV2 : allPrices.getPriceEntities()) {
			mappedPrices.put(priceEntityV2.getId().getVendorItemId(), priceEntityV2ToPriceFactConverter.convert(priceEntityV2));
		}

		mappedPrices.values().removeIf(this::shouldBeRemoved);

		if (isEmpty(mappedPrices)) {
			throw NotFoundException.customerPricesNotFoundV2(vendorId, vendorAccountId, country);
		}

		return mappedPrices;
	}

	private boolean shouldBeRemoved(final PriceFact priceFact) {

		return priceFact.getBasePrice() == null;
	}
}
