package com.abinbev.b2b.price.api.services;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abinbev.b2b.price.api.domain.BrowsePrice;
import com.abinbev.b2b.price.api.domain.PriceFact;
import com.abinbev.b2b.price.api.services.browseprice.CalculateBrowsePriceService;

@ExtendWith(MockitoExtension.class)
class CalculatedBrowsePriceMappedBySkuServiceTest {

	private static final String COUNTRY = "BR";
	private static final String SKU_0001 = "SKU0001";
	@Mock
	private CalculateBrowsePriceService calculateBrowsePriceService;

	@InjectMocks
	private CalculatedBrowsePriceMappedBySkuService calculatedBrowsePriceMappedBySkuService;

	@Test
	void shouldReturnValidListWhenSuccessOnExecution() {

		final BrowsePrice mockedBrowsePrice = getMockedBrowsePrice();

		doReturn(mockedBrowsePrice).when(calculateBrowsePriceService).execute(any(), anyString());

		final List<BrowsePrice> browsePriceList = calculatedBrowsePriceMappedBySkuService
				.execute(COUNTRY, getValidMappedMockOfPriceFactBySkus());

		assertThat(browsePriceList, hasSize(equalTo(1)));

		assertThat(browsePriceList.stream().filter(browsePrice -> browsePrice.getSku().equals(SKU_0001)).collect(Collectors.toList()),
				hasSize(equalTo(1)));

		verify(calculateBrowsePriceService, times(1)).execute(any(), anyString());
	}

	private BrowsePrice getMockedBrowsePrice() {

		final BrowsePrice mockedBrowsePrice = new BrowsePrice();
		mockedBrowsePrice.setOriginalPrice(BigDecimal.TEN);
		mockedBrowsePrice.setPrice(BigDecimal.TEN);
		mockedBrowsePrice.setSku(SKU_0001);
		return mockedBrowsePrice;
	}

	private Map<String, PriceFact> getValidMappedMockOfPriceFactBySkus() {

		final Map<String, PriceFact> priceFactBySkuMap = new HashMap<>();

		final PriceFact priceFact = new PriceFact();
		priceFact.setSku(SKU_0001);
		priceFactBySkuMap.put(SKU_0001, priceFact);

		return priceFactBySkuMap;
	}
}
