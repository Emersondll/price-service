package com.abinbev.b2b.price.api.services.v2;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abinbev.b2b.price.api.config.properties.PriceListProperties;
import com.abinbev.b2b.price.api.domain.v2.PriceResultListV2;
import com.abinbev.b2b.price.api.testhelpers.PriceEntityV2Factory;
import com.abinbev.b2b.price.domain.model.v2.EntityTypeEnum;
import com.abinbev.b2b.price.domain.model.v2.PriceEntityV2;

@ExtendWith(MockitoExtension.class)
class PricePriorityByTypeServiceTest {

	private static final String COUNTRY_BR = "BR";
	private static final String COUNTRY_US = "US";
	private static final String COUNTRIES = "US,BR";
	private static final String OTHER_COUNTRIES = "FR,AR";

	@Mock
	private PriceListProperties priceListProperties;

	@InjectMocks
	private PricePriorityByTypeService pricePriorityByTypeService;

	@Test
	void shouldFilterPricesByPriceListWhenContainsCountry() {

		Mockito.when(priceListProperties.getCountriesEnabled()).thenReturn(COUNTRIES);

		final List<PriceEntityV2> mockedList = new ArrayList<>();
		mockedList.add(PriceEntityV2Factory.generatePriceEntity1());
		mockedList.add(PriceEntityV2Factory.generatePriceEntity2());
		mockedList.add(PriceEntityV2Factory.generatePriceEntity3());
		mockedList.add(PriceEntityV2Factory.generatePriceEntity4());

		final PriceResultListV2 priceResultListV2 = new PriceResultListV2(mockedList, null);

		final PriceResultListV2 result = pricePriorityByTypeService.execute(priceResultListV2, COUNTRY_US);

		assertThat(result.getPriceEntities().size(), is(3));

		final List<PriceEntityV2> expectedList = new ArrayList<>();

		expectedList.add(PriceEntityV2Factory.generatePriceEntity1());
		expectedList.add(PriceEntityV2Factory.generatePriceEntity3());
		expectedList.add(PriceEntityV2Factory.generatePriceEntity4());

		assertThat(result.getPriceEntities().get(0).getId().getType(), is(EntityTypeEnum.PRICE_LIST.toString()));
		assertThat(result.getPriceEntities().get(1).getId().getType(), is(EntityTypeEnum.PRICE_LIST.toString()));
		assertThat(result.getPriceEntities().get(2).getId().getType(), is(EntityTypeEnum.ACCOUNT.toString()));

		assertThat(result.getPriceEntities().get(0).getId().getVendorItemId(), is(expectedList.get(0).getId().getVendorItemId()));
		assertThat(result.getPriceEntities().get(1).getId().getVendorItemId(), is(expectedList.get(1).getId().getVendorItemId()));
		assertThat(result.getPriceEntities().get(2).getId().getVendorItemId(), is(expectedList.get(2).getId().getVendorItemId()));

	}

	@Test
	void shouldFilterPricesByAccountWhenNotContainsCountry() {

		Mockito.when(priceListProperties.getCountriesEnabled()).thenReturn(OTHER_COUNTRIES);

		final List<PriceEntityV2> mockedList = new ArrayList<>();
		mockedList.add(PriceEntityV2Factory.generatePriceEntity1());
		mockedList.add(PriceEntityV2Factory.generatePriceEntity2());
		mockedList.add(PriceEntityV2Factory.generatePriceEntity3());
		mockedList.add(PriceEntityV2Factory.generatePriceEntity4());

		final PriceResultListV2 priceResultListV2 = new PriceResultListV2(mockedList, null);

		final PriceResultListV2 result = pricePriorityByTypeService.execute(priceResultListV2, COUNTRY_BR);

		final List<PriceEntityV2> expectedList = new ArrayList<>();

		expectedList.add(PriceEntityV2Factory.generatePriceEntity1());
		expectedList.add(PriceEntityV2Factory.generatePriceEntity4());
		expectedList.add(PriceEntityV2Factory.generatePriceEntity3());

		assertThat(result.getPriceEntities().size(), is(3));
		assertThat(result.getPriceEntities().get(0).getId().getType(), is(EntityTypeEnum.ACCOUNT.toString()));
		assertThat(result.getPriceEntities().get(1).getId().getType(), is(EntityTypeEnum.ACCOUNT.toString()));
		assertThat(result.getPriceEntities().get(2).getId().getType(), is(EntityTypeEnum.PRICE_LIST.toString()));

		assertThat(result.getPriceEntities().get(0).getId().getVendorItemId(), is(expectedList.get(0).getId().getVendorItemId()));
		assertThat(result.getPriceEntities().get(1).getId().getVendorItemId(), is(expectedList.get(1).getId().getVendorItemId()));
		assertThat(result.getPriceEntities().get(2).getId().getVendorItemId(), is(expectedList.get(2).getId().getVendorItemId()));
	}

	@Test
	void shouldFilterPricesByAccountWhenCountriesIsNotFilled() {

		Mockito.when(priceListProperties.getCountriesEnabled()).thenReturn("");

		final List<PriceEntityV2> mockedList = new ArrayList<>();
		mockedList.add(PriceEntityV2Factory.generatePriceEntity1());
		mockedList.add(PriceEntityV2Factory.generatePriceEntity2());
		mockedList.add(PriceEntityV2Factory.generatePriceEntity3());
		mockedList.add(PriceEntityV2Factory.generatePriceEntity4());

		final PriceResultListV2 priceResultListV2 = new PriceResultListV2(mockedList, null);

		final PriceResultListV2 result = pricePriorityByTypeService.execute(priceResultListV2, COUNTRY_BR);

		final List<PriceEntityV2> expectedList = new ArrayList<>();

		expectedList.add(PriceEntityV2Factory.generatePriceEntity1());
		expectedList.add(PriceEntityV2Factory.generatePriceEntity4());
		expectedList.add(PriceEntityV2Factory.generatePriceEntity3());

		assertThat(result.getPriceEntities().size(), is(3));
		assertThat(result.getPriceEntities().get(0).getId().getType(), is(EntityTypeEnum.ACCOUNT.toString()));
		assertThat(result.getPriceEntities().get(1).getId().getType(), is(EntityTypeEnum.ACCOUNT.toString()));
		assertThat(result.getPriceEntities().get(2).getId().getType(), is(EntityTypeEnum.PRICE_LIST.toString()));

		assertThat(result.getPriceEntities().get(0).getId().getVendorItemId(), is(expectedList.get(0).getId().getVendorItemId()));
		assertThat(result.getPriceEntities().get(1).getId().getVendorItemId(), is(expectedList.get(1).getId().getVendorItemId()));
		assertThat(result.getPriceEntities().get(2).getId().getVendorItemId(), is(expectedList.get(2).getId().getVendorItemId()));
	}

	@Test
	void shouldNotFilterPricesWhenValidFromIsFilled() {

		Mockito.when(priceListProperties.getCountriesEnabled()).thenReturn("");

		final List<PriceEntityV2> mockedList = new ArrayList<>();
		final Instant currentDate = Instant.now();

		final PriceEntityV2 price1 = PriceEntityV2Factory.generatePriceEntity2();
		price1.getId().setValidFrom(currentDate);

		final PriceEntityV2 price2 = PriceEntityV2Factory.generatePriceEntity2();
		price2.getId().setValidFrom(currentDate);

		mockedList.add(price1);
		mockedList.add(price2);

		final PriceResultListV2 priceResultListV2 = new PriceResultListV2(mockedList, null);

		final PriceResultListV2 result = pricePriorityByTypeService.execute(priceResultListV2, COUNTRY_BR);

		final List<PriceEntityV2> expectedList = new ArrayList<>();

		expectedList.add(price1);
		expectedList.add(price2);

		assertThat(result.getPriceEntities().size(), is(2));
		assertThat(result.getPriceEntities().get(0).getId().getType(), is(EntityTypeEnum.ACCOUNT.toString()));
		assertThat(result.getPriceEntities().get(1).getId().getType(), is(EntityTypeEnum.ACCOUNT.toString()));

		assertThat(result.getPriceEntities().get(0).getId().getVendorItemId(), is(expectedList.get(0).getId().getVendorItemId()));
		assertThat(result.getPriceEntities().get(1).getId().getVendorItemId(), is(expectedList.get(1).getId().getVendorItemId()));
	}
}