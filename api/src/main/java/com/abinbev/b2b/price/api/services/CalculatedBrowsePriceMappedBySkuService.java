package com.abinbev.b2b.price.api.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abinbev.b2b.price.api.domain.BrowsePrice;
import com.abinbev.b2b.price.api.domain.PriceFact;
import com.abinbev.b2b.price.api.services.browseprice.CalculateBrowsePriceService;
import com.newrelic.api.agent.Trace;

@Service
public class CalculatedBrowsePriceMappedBySkuService {

	private final CalculateBrowsePriceService calculateBrowsePriceService;

	@Autowired
	public CalculatedBrowsePriceMappedBySkuService(final CalculateBrowsePriceService calculateBrowsePriceService) {

		this.calculateBrowsePriceService = calculateBrowsePriceService;
	}

	@Trace(dispatcher = true)
	public List<BrowsePrice> execute(final String country, final Map<String, PriceFact> mappedPriceBySku) {

		final List<BrowsePrice> browsePriceList = new ArrayList<>();

		for (final var priceBySku : mappedPriceBySku.entrySet()) {
			browsePriceList.add(calculateBrowsePriceService.execute(priceBySku.getValue(), country));
		}

		return browsePriceList;
	}
}
