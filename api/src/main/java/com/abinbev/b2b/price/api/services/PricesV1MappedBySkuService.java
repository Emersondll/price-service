package com.abinbev.b2b.price.api.services;

import static org.apache.commons.collections4.MapUtils.isEmpty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abinbev.b2b.price.api.converters.PriceEntityToPriceFactConverter;
import com.abinbev.b2b.price.api.domain.PriceFact;
import com.abinbev.b2b.price.api.exceptions.NotFoundException;
import com.abinbev.b2b.price.api.rest.vo.PriceResponseVo;
import com.abinbev.b2b.price.domain.model.PriceEntity;
import com.newrelic.api.agent.Trace;

@Service
public class PricesV1MappedBySkuService {

	private final PriceService priceService;
	private final PriceEntityToPriceFactConverter priceEntityToPriceFactConverter;

	@Autowired
	public PricesV1MappedBySkuService(final PriceService priceService,
			final PriceEntityToPriceFactConverter priceEntityToPriceFactConverter) {

		this.priceService = priceService;
		this.priceEntityToPriceFactConverter = priceEntityToPriceFactConverter;
	}

	@Trace(dispatcher = true)
	public Map<String, PriceFact> execute(final String accountId, final String country, final List<String> skus) {

		final PriceResponseVo allPrices = priceService.getAllPrices(accountId, country, skus, null);

		final Map<String, PriceFact> mappedPrices = new HashMap<>();

		for (final PriceEntity priceEntity : allPrices.getPrices()) {
			mappedPrices.put(priceEntity.getSkuPrice(), priceEntityToPriceFactConverter.convert(priceEntity));
		}

		mappedPrices.values().removeIf(this::shouldBeRemoved);

		if (isEmpty(mappedPrices)) {
			throw NotFoundException.customerPricesNotFound(accountId, country);
		}

		return mappedPrices;
	}

	private boolean shouldBeRemoved(final PriceFact priceFact) {

		return priceFact.getBasePrice() == null;
	}
}
