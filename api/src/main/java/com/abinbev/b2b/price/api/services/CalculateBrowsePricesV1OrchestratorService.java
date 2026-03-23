package com.abinbev.b2b.price.api.services;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abinbev.b2b.price.api.domain.BrowsePrice;
import com.abinbev.b2b.price.api.domain.PriceFact;
import com.newrelic.api.agent.Trace;

@Service
public class CalculateBrowsePricesV1OrchestratorService {

	private final PricesV1MappedBySkuService pricesMappedBySkuService;

	private final CalculatedBrowsePriceMappedBySkuService calculatedBrowsePriceMappedBySkuService;

	@Autowired
	public CalculateBrowsePricesV1OrchestratorService(final PricesV1MappedBySkuService pricesMappedBySkuService,
			final CalculatedBrowsePriceMappedBySkuService calculatedBrowsePriceMappedBySkuService) {

		this.pricesMappedBySkuService = pricesMappedBySkuService;
		this.calculatedBrowsePriceMappedBySkuService = calculatedBrowsePriceMappedBySkuService;
	}

	@Trace(dispatcher = true)
	public List<BrowsePrice> execute(final String accountId, final String country, final List<String> skus) {

		final Map<String, PriceFact> mappedPriceBySku = pricesMappedBySkuService.execute(accountId, country, skus);

		return calculatedBrowsePriceMappedBySkuService.execute(country, mappedPriceBySku);
	}
}
