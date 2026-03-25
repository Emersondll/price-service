package com.abinbev.b2b.price.api.services.v2;

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
import com.abinbev.b2b.price.api.services.CalculatedBrowsePriceMappedBySkuService;

@ExtendWith(MockitoExtension.class)
class CalculateBrowsePricesV2OrchestratorServiceTest {

	private final static String VENDOR_ITEM_ID_01 = "VENDOR_ITEM_ID_01";
	private final static String VENDOR_ITEM_ID_02 = "VENDOR_ITEM_ID_02";
	private final static String COUNTRY = "br";

	@InjectMocks
	private CalculateBrowsePricesV2OrchestratorService calculateBrowsePricesV2OrchestratorService;

	@Mock
	private PricesV2MappedByVendorItemIdService pricesMappedByVendorItemIdService;

	@Mock
	private CalculatedBrowsePriceMappedBySkuService calculatedBrowsePriceMappedBySkuService;

	@Test
	void shouldExecuteOrchestratorWithSuccessWhenHasValidParameters() {

		final String vendorId = "vendor-01";
		final String vendorAccountId = "vendorAccount-01";
		final List<String> vendorItemIds = Arrays.asList(VENDOR_ITEM_ID_01, VENDOR_ITEM_ID_02);

		final Map<String, PriceFact> mapPriceBySku = mockMapPriceByVendorItemId(new ArrayList<>(vendorItemIds));
		when(pricesMappedByVendorItemIdService.execute(vendorId, vendorAccountId, COUNTRY, vendorItemIds)).thenReturn(mapPriceBySku);

		when(calculatedBrowsePriceMappedBySkuService.execute(COUNTRY, mapPriceBySku)).thenReturn(mockBrowsePrices());

		final List<BrowsePrice> browsePrices = calculateBrowsePricesV2OrchestratorService
				.execute(vendorId, vendorAccountId, COUNTRY, vendorItemIds);

		verify(pricesMappedByVendorItemIdService).execute(vendorId, vendorAccountId, COUNTRY, vendorItemIds);
		verify(calculatedBrowsePriceMappedBySkuService).execute(COUNTRY, mapPriceBySku);

		assertThat(browsePrices, everyItem(is(notNullValue())));
		assertThat(browsePrices, hasSize(equalTo(2)));

		assertThat(browsePrices.get(0).getVendorItemId(), is(equalTo(VENDOR_ITEM_ID_01)));
		assertThat(browsePrices.get(0).getPrice(), is(equalTo(BigDecimal.ONE)));
		assertThat(browsePrices.get(1).getVendorItemId(), is(equalTo(VENDOR_ITEM_ID_02)));
		assertThat(browsePrices.get(1).getPrice(), is(equalTo(BigDecimal.TEN)));
	}

	private List<BrowsePrice> mockBrowsePrices() {

		final List<BrowsePrice> browsePrices = new ArrayList<>();

		BrowsePrice browsePrice = new BrowsePrice();
		browsePrice.setVendorItemId(VENDOR_ITEM_ID_01);
		browsePrice.setPrice(BigDecimal.ONE);
		browsePrices.add(browsePrice);

		browsePrice = new BrowsePrice();
		browsePrice.setVendorItemId(VENDOR_ITEM_ID_02);
		browsePrice.setPrice(BigDecimal.TEN);
		browsePrices.add(browsePrice);

		return browsePrices;
	}

	private Map<String, PriceFact> mockMapPriceByVendorItemId(final List<String> vendorItemIds) {

		final Map<String, PriceFact> mappedPriceBySku = new HashMap<>();
		for (final String vendorItemId : vendorItemIds) {

			final PriceFact priceFact = new PriceFact();
			priceFact.setVendorItemId(vendorItemId);
			priceFact.setBasePrice(BigDecimal.valueOf(vendorItemIds.indexOf(vendorItemId) + 1));

			mappedPriceBySku.put(vendorItemId, priceFact);

		}

		return mappedPriceBySku;
	}
}
