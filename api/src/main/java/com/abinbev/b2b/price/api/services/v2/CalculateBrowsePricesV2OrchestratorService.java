package com.abinbev.b2b.price.api.services.v2;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abinbev.b2b.price.api.domain.BrowsePrice;
import com.abinbev.b2b.price.api.domain.PriceFact;
import com.abinbev.b2b.price.api.services.CalculatedBrowsePriceMappedBySkuService;

@Service
public class CalculateBrowsePricesV2OrchestratorService {

	private final PricesV2MappedByVendorItemIdService pricesV2MappedBySkuService;

	private final CalculatedBrowsePriceMappedBySkuService calculatedBrowsePriceMappedBySkuService;

	@Autowired
	public CalculateBrowsePricesV2OrchestratorService(final PricesV2MappedByVendorItemIdService pricesMappedBySkuService,
			final CalculatedBrowsePriceMappedBySkuService calculatedBrowsePriceMappedBySkuService) {

		pricesV2MappedBySkuService = pricesMappedBySkuService;
		this.calculatedBrowsePriceMappedBySkuService = calculatedBrowsePriceMappedBySkuService;
	}

	public List<BrowsePrice> execute(final String vendorId, final String vendorAccountId, final String country,
			final List<String> vendorItemIds) {

		final Map<String, PriceFact> mappedPrice = pricesV2MappedBySkuService.execute(vendorId, vendorAccountId, country, vendorItemIds);

		return calculatedBrowsePriceMappedBySkuService.execute(country, mappedPrice);
	}
}
