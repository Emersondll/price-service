package com.abinbev.b2b.price.api.services;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abinbev.b2b.price.api.domain.BrowsePrice;
import com.abinbev.b2b.price.api.domain.PriceFact;

@ExtendWith(MockitoExtension.class)
class CalculateBrowsePricesV1OrchestratorServiceTest {

	private final static String SKU_01 = "SKU-01";
	private final static String SKU_02 = "SKU-02";
	private final static String COUNTRY = "br";

	@InjectMocks
	private CalculateBrowsePricesV1OrchestratorService calculateBrowsePricesOrchestrator;

	@Mock
	private PricesV1MappedBySkuService pricesMappedBySkuService;

	@Mock
	private CalculatedBrowsePriceMappedBySkuService calculatedBrowsePriceMappedBySkuService;

	@Test
	void shouldExecuteWhenOrchestratorWithSuccess() {

		final String accountId = "account-01";
		final List<String> skus = Arrays.asList(SKU_01, SKU_02);

		final Map<String, PriceFact> mapPriceBySku = mockMapPriceBySku(new ArrayList<>(skus));
		when(pricesMappedBySkuService.execute(accountId, COUNTRY, skus)).thenReturn(mapPriceBySku);

		when(calculatedBrowsePriceMappedBySkuService.execute(COUNTRY, mapPriceBySku)).thenReturn(mockBrowsePrices());

		final List<BrowsePrice> browsePrices = calculateBrowsePricesOrchestrator.execute(accountId, COUNTRY, skus);

		verify(pricesMappedBySkuService).execute(accountId, COUNTRY, skus);
		verify(calculatedBrowsePriceMappedBySkuService).execute(COUNTRY, mapPriceBySku);

		assertThat(browsePrices, everyItem(is(notNullValue())));
		assertThat(browsePrices, hasSize(equalTo(2)));

		assertThat(browsePrices.get(0).getSku(), is(equalTo(SKU_01)));
		assertThat(browsePrices.get(0).getPrice(), is(equalTo(BigDecimal.ONE)));
		assertThat(browsePrices.get(1).getSku(), is(equalTo(SKU_02)));
		assertThat(browsePrices.get(1).getPrice(), is(equalTo(BigDecimal.TEN)));
	}

	private List<BrowsePrice> mockBrowsePrices() {

		final List<BrowsePrice> browsePrices = new ArrayList<>();

		BrowsePrice browsePrice = new BrowsePrice();
		browsePrice.setSku(SKU_01);
		browsePrice.setPrice(BigDecimal.ONE);
		browsePrices.add(browsePrice);

		browsePrice = new BrowsePrice();
		browsePrice.setSku(SKU_02);
		browsePrice.setPrice(BigDecimal.TEN);
		browsePrices.add(browsePrice);

		return browsePrices;
	}

	private Map<String, PriceFact> mockMapPriceBySku(final List<String> skus) {

		final Map<String, PriceFact> mappedPriceBySku = new HashMap<>();
		for (final String sku : skus) {

			final PriceFact priceFact = new PriceFact();
			priceFact.setSku(sku);
			priceFact.setBasePrice(BigDecimal.valueOf(skus.indexOf(sku) + 1));

			mappedPriceBySku.put(sku, priceFact);

		}

		return mappedPriceBySku;
	}
}
