package com.abinbev.b2b.price.api.services.v2;

import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;
import static org.hamcrest.core.IsEqual.equalTo;
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

import com.abinbev.b2b.price.api.converters.v2.PriceEntityV2ToPriceFactConverter;
import com.abinbev.b2b.price.api.domain.PriceFact;
import com.abinbev.b2b.price.api.domain.v2.PriceResultListV2;
import com.abinbev.b2b.price.api.exceptions.Issue;
import com.abinbev.b2b.price.api.exceptions.IssueEnum;
import com.abinbev.b2b.price.api.exceptions.NotFoundException;
import com.abinbev.b2b.price.domain.model.v2.EntityTypeEnum;
import com.abinbev.b2b.price.domain.model.v2.PriceCompoundKeyV2;
import com.abinbev.b2b.price.domain.model.v2.PriceEntityV2;

@ExtendWith(MockitoExtension.class)
class PricesV2MappedByVendorItemIdServiceTest {

	private static final String VENDOR_ID = "VENDOR_ID_01";
	private static final String VENDOR_ACCOUNT_ID = "VENDOR_ACCOUNT_ID_01";
	private static final String COUNTRY = "BR";
	private static final String VENDOR_ITEM_ID_0001 = "VENDOR_ITEM_ID_0001";
	private static final String VENDOR_ITEM_ID_0002 = "VENDOR_ITEM_ID_0002";

	@Mock
	private PriceServiceV2 priceServiceV2;

	@Mock
	private PriceEntityV2ToPriceFactConverter priceEntityV2ToPriceFactConverter;

	@InjectMocks
	private PricesV2MappedByVendorItemIdService service;

	@Test
	void shouldExecuteWithSuccessWhenPricesAreFound() {

		final List<String> mockedVendorItemIds = Arrays.asList(VENDOR_ITEM_ID_0001);

		doReturn(getValidMockedPriceFact()).when(priceServiceV2)
				.getAllPrices(VENDOR_ID, VENDOR_ACCOUNT_ID, COUNTRY, mockedVendorItemIds, null, null);

		doReturn(mockedPriceFactWithBasePrice()).when(priceEntityV2ToPriceFactConverter).convert(any());

		final Map<String, PriceFact> actualResponse = service.execute(VENDOR_ID, VENDOR_ACCOUNT_ID, COUNTRY, mockedVendorItemIds);

		assertThat(actualResponse, is(aMapWithSize(2)));
		assertThat(actualResponse, hasKey(VENDOR_ITEM_ID_0001));
		assertThat(actualResponse.values(), everyItem(is(notNullValue())));

		verify(priceServiceV2).getAllPrices(VENDOR_ID, VENDOR_ACCOUNT_ID, COUNTRY, mockedVendorItemIds, null, null);
	}

	@Test
	void shouldThrowNotFoundExceptionWhenAllItemsHasNullBasePriceValues() {

		final List<String> mockedVendorItemIds = Arrays.asList(VENDOR_ITEM_ID_0001, VENDOR_ITEM_ID_0002);

		doReturn(getPriceFactWithoutBasePrice()).when(priceServiceV2)
				.getAllPrices(VENDOR_ID, VENDOR_ACCOUNT_ID, COUNTRY, mockedVendorItemIds, null, null);
		doReturn(mockedEmptyPriceFact()).when(priceEntityV2ToPriceFactConverter).convert(any());

		final NotFoundException notFoundException = assertThrows(NotFoundException.class,
				() -> service.execute(VENDOR_ID, VENDOR_ACCOUNT_ID, COUNTRY, mockedVendorItemIds));

		final Issue issue = new Issue(IssueEnum.PRICES_NOT_FOUND_V2, VENDOR_ID, VENDOR_ACCOUNT_ID, COUNTRY);
		assertThat(notFoundException.getIssue().getMessage(), is(equalTo(issue.getMessage())));

		verify(priceServiceV2).getAllPrices(VENDOR_ID, VENDOR_ACCOUNT_ID, COUNTRY, mockedVendorItemIds, null, null);
	}

	private PriceFact mockedEmptyPriceFact() {

		return new PriceFact();
	}

	private PriceFact mockedPriceFactWithBasePrice() {

		final PriceFact priceFact = new PriceFact();
		priceFact.setBasePrice(BigDecimal.ONE);

		return priceFact;
	}

	private PriceResultListV2 getValidMockedPriceFact() {

		final PriceCompoundKeyV2 priceCompoundKey1 = new PriceCompoundKeyV2();
		priceCompoundKey1.setVendorId(VENDOR_ID);
		priceCompoundKey1.setId(VENDOR_ACCOUNT_ID);
		priceCompoundKey1.setType(EntityTypeEnum.ACCOUNT.toString());
		priceCompoundKey1.setVendorItemId(VENDOR_ITEM_ID_0001);

		final PriceCompoundKeyV2 priceCompoundKey2 = new PriceCompoundKeyV2();
		priceCompoundKey2.setVendorId(VENDOR_ID);
		priceCompoundKey2.setId(VENDOR_ACCOUNT_ID);
		priceCompoundKey2.setType(EntityTypeEnum.ACCOUNT.toString());
		priceCompoundKey2.setVendorItemId(VENDOR_ITEM_ID_0002);

		final PriceEntityV2 priceEntity1 = new PriceEntityV2();
		priceEntity1.setId(priceCompoundKey1);
		priceEntity1.setDeleted(false);
		priceEntity1.setBasePrice(BigDecimal.ONE);

		final PriceEntityV2 priceEntity2 = new PriceEntityV2();
		priceEntity2.setId(priceCompoundKey2);
		priceEntity2.setDeleted(false);
		priceEntity2.setBasePrice(BigDecimal.TEN);

		final List<PriceEntityV2> priceEntityV2List = new ArrayList<>();
		priceEntityV2List.add(priceEntity1);
		priceEntityV2List.add(priceEntity2);

		return new PriceResultListV2(priceEntityV2List, null);
	}

	private PriceResultListV2 getPriceFactWithoutBasePrice() {

		final PriceCompoundKeyV2 priceCompoundKey1 = new PriceCompoundKeyV2();
		priceCompoundKey1.setVendorId(VENDOR_ID);
		priceCompoundKey1.setId(VENDOR_ACCOUNT_ID);
		priceCompoundKey1.setType(EntityTypeEnum.ACCOUNT.toString());
		priceCompoundKey1.setVendorItemId(VENDOR_ITEM_ID_0001);

		final PriceCompoundKeyV2 priceCompoundKey2 = new PriceCompoundKeyV2();
		priceCompoundKey2.setVendorId(VENDOR_ID);
		priceCompoundKey2.setId(VENDOR_ACCOUNT_ID);
		priceCompoundKey2.setType(EntityTypeEnum.ACCOUNT.toString());
		priceCompoundKey2.setVendorItemId(VENDOR_ITEM_ID_0002);

		final PriceEntityV2 priceEntity1 = new PriceEntityV2();
		priceEntity1.setId(priceCompoundKey1);
		priceEntity1.setDeleted(true);

		final PriceEntityV2 priceEntity2 = new PriceEntityV2();
		priceEntity2.setId(priceCompoundKey2);
		priceEntity2.setDeleted(false);

		final List<PriceEntityV2> priceEntityList = new ArrayList<>();
		priceEntityList.add(priceEntity1);
		priceEntityList.add(priceEntity2);

		return new PriceResultListV2(priceEntityList, null);
	}

}
