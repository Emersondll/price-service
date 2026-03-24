package com.abinbev.b2b.price.api.services;

import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abinbev.b2b.price.api.converters.PriceEntityToPriceFactConverter;
import com.abinbev.b2b.price.api.domain.PriceFact;
import com.abinbev.b2b.price.api.exceptions.NotFoundException;
import com.abinbev.b2b.price.api.rest.vo.PriceResponseVo;
import com.abinbev.b2b.price.domain.model.PriceCompoundKey;
import com.abinbev.b2b.price.domain.model.PriceEntity;

@ExtendWith(MockitoExtension.class)
class PricesV1MappedBySkuServiceTest {

	private static final String ACCOUNT_ID = "123";
	private static final String COUNTRY = "BR";
	private static final String SKU_0001 = "SKU_0001";
	private static final String SKU_0002 = "SKU_0002";

	@Mock
	private PriceService priceService;

	@Mock
	private PriceEntityToPriceFactConverter priceEntityToPriceFactConverter;

	@InjectMocks
	private PricesV1MappedBySkuService service;

	@Test
	void shouldExecuteWithSuccessWhenPricesAreFound() {

		final List<String> mockedSkus = Arrays.asList(SKU_0001);

		doReturn(getValidMockedPriceFact()).when(priceService).getAllPrices(ACCOUNT_ID, COUNTRY, mockedSkus, null);

		doReturn(mockedPriceFactWithBasePrice()).when(priceEntityToPriceFactConverter).convert(any());

		final Map<String, PriceFact> actualResponse = service.execute(ACCOUNT_ID, COUNTRY, mockedSkus);

		assertThat(actualResponse, is(aMapWithSize(2)));
		assertThat(actualResponse, hasKey(SKU_0001));
		assertThat(actualResponse.values(), everyItem(is(notNullValue())));

		verify(priceService).getAllPrices(ACCOUNT_ID, COUNTRY, mockedSkus, null);
	}

	@Test
	void shouldThrowNotFoundExceptionWhenAllItemsHasNullBasePriceValues() {

		final List<String> mockedSkus = Arrays.asList(SKU_0001, SKU_0002);

		doReturn(getPriceFactWithoutBasePrice()).when(priceService).getAllPrices(ACCOUNT_ID, COUNTRY, mockedSkus, null);
		doReturn(mockedEmptyPriceFact()).when(priceEntityToPriceFactConverter).convert(any());

		assertThrows(NotFoundException.class, () -> service.execute(ACCOUNT_ID, COUNTRY, mockedSkus));

		verify(priceService).getAllPrices(ACCOUNT_ID, COUNTRY, mockedSkus, null);
	}

	private PriceFact mockedEmptyPriceFact() {

		return new PriceFact();
	}

	private PriceFact mockedPriceFactWithBasePrice() {

		final PriceFact priceFact = new PriceFact();
		priceFact.setBasePrice(BigDecimal.ONE);

		return priceFact;
	}

	private PriceResponseVo getValidMockedPriceFact() {

		final PriceCompoundKey priceCompoundKey1 = new PriceCompoundKey(ACCOUNT_ID, SKU_0001, null);
		final PriceCompoundKey priceCompoundKey2 = new PriceCompoundKey(ACCOUNT_ID, SKU_0002, null);

		final PriceEntity priceEntity1 = new PriceEntity();
		priceEntity1.setId(priceCompoundKey1);
		priceEntity1.setDeleted(false);
		priceEntity1.setBasePrice(BigDecimal.ONE);

		final PriceEntity priceEntity2 = new PriceEntity();
		priceEntity2.setId(priceCompoundKey2);
		priceEntity2.setDeleted(false);
		priceEntity2.setBasePrice(BigDecimal.TEN);

		final List<PriceEntity> priceEntityList = new ArrayList<>();
		priceEntityList.add(priceEntity1);
		priceEntityList.add(priceEntity2);

		return new PriceResponseVo(priceEntityList, null);
	}

	private PriceResponseVo getPriceFactWithoutBasePrice() {

		final PriceCompoundKey priceCompoundKey1 = new PriceCompoundKey(ACCOUNT_ID, SKU_0001, null);
		final PriceCompoundKey priceCompoundKey2 = new PriceCompoundKey(ACCOUNT_ID, SKU_0002, null);

		final PriceEntity priceEntity1 = new PriceEntity();
		priceEntity1.setId(priceCompoundKey1);
		priceEntity1.setDeleted(true);

		final PriceEntity priceEntity2 = new PriceEntity();
		priceEntity2.setId(priceCompoundKey2);
		priceEntity2.setDeleted(false);

		final List<PriceEntity> priceEntityList = new ArrayList<>();
		priceEntityList.add(priceEntity1);
		priceEntityList.add(priceEntity2);

		return new PriceResponseVo(priceEntityList, null);
	}

}
