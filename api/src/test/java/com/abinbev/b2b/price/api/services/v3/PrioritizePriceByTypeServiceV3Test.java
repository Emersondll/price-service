package com.abinbev.b2b.price.api.services.v3;

import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import com.abinbev.b2b.price.api.domain.v3.PriceNormalizedInfo;
import com.abinbev.b2b.price.domain.model.v2.EntityTypeEnum;
import com.abinbev.b2b.price.domain.model.v2.PriceCompoundKeyV2;
import com.abinbev.b2b.price.domain.model.v2.PriceEntityV2;

class PrioritizePriceByTypeServiceV3Test {

	private final PrioritizePriceByTypeServiceV3 prioritizePriceByTypeServiceV3 = new PrioritizePriceByTypeServiceV3();

	@Test
	void shouldPrioritizePriceCorrectlyWhenHasOnlyPriceByVendorForNormalizedPrices() {

		final PriceNormalizedInfo priceNormalizedInfo1 = mockPriceNormalizedInfo("V-1", "VI-1", null, null, null);
		final PriceNormalizedInfo priceNormalizedInfo2 = mockPriceNormalizedInfo("V-1", "VI-1", "VA-1", null, null);
		final PriceNormalizedInfo priceNormalizedInfo3 = mockPriceNormalizedInfo("V-1", "VI-1", "VA-1", null, "PL-1");
		final PriceNormalizedInfo priceNormalizedInfo4 = mockPriceNormalizedInfo("V-1", "VI-1", null, "VDC-1", null);

		final PriceEntityV2 priceByVendor = mockPriceEntityV2(EntityTypeEnum.VENDOR, "V-1", "V-1", "VI-1");

		final List<PriceEntityV2> priceEntityV2s = singletonList(priceByVendor);

		prioritizePriceByTypeServiceV3.execute(priceEntityV2s, singletonList(priceNormalizedInfo1));
		assertThat(priceNormalizedInfo1.getSelectedPrice(), is(priceByVendor));

		prioritizePriceByTypeServiceV3.execute(priceEntityV2s, singletonList(priceNormalizedInfo2));
		assertThat(priceNormalizedInfo2.getSelectedPrice(), is(priceByVendor));

		prioritizePriceByTypeServiceV3.execute(priceEntityV2s, singletonList(priceNormalizedInfo3));
		assertThat(priceNormalizedInfo3.getSelectedPrice(), is(priceByVendor));

		prioritizePriceByTypeServiceV3.execute(priceEntityV2s, singletonList(priceNormalizedInfo4));
		assertThat(priceNormalizedInfo4.getSelectedPrice(), is(priceByVendor));
	}

	@Test
	void shouldPrioritizeNoPriceWhenHasInvalidPriceType() {

		final PriceNormalizedInfo priceNormalizedInfo = mockPriceNormalizedInfo("V-1", "VI-1", null, null, null);

		final PriceEntityV2 priceByInvalidType = mockPriceEntityV2(null, "V-1", "V-1", "VI-1");
		priceByInvalidType.getId().setType("INVALID_TYPE");

		final List<PriceEntityV2> priceEntityV2s = singletonList(priceByInvalidType);

		prioritizePriceByTypeServiceV3.execute(priceEntityV2s, singletonList(priceNormalizedInfo));
		assertThat(priceNormalizedInfo.getSelectedPrice(), is(nullValue()));
	}

	@Test
	void shouldPrioritizePriceCorrectlyWhenHasOnlyPriceByAccountForNormalizedPrices() {

		final PriceNormalizedInfo priceNormalizedInfo1 = mockPriceNormalizedInfo("V-1", "VI-1", null, null, null);
		final PriceNormalizedInfo priceNormalizedInfo2 = mockPriceNormalizedInfo("V-1", "VI-1", "VA-1", null, null);
		final PriceNormalizedInfo priceNormalizedInfo3 = mockPriceNormalizedInfo("V-1", "VI-1", "VA-1", null, "PL-1");
		final PriceNormalizedInfo priceNormalizedInfo4 = mockPriceNormalizedInfo("V-1", "VI-1", null, "VDC-1", null);

		final PriceEntityV2 priceByAccount = mockPriceEntityV2(EntityTypeEnum.ACCOUNT, "VA-1", "V-1", "VI-1");

		final List<PriceEntityV2> priceEntityV2s = singletonList(priceByAccount);

		prioritizePriceByTypeServiceV3.execute(priceEntityV2s, singletonList(priceNormalizedInfo1));
		assertThat(priceNormalizedInfo1.getSelectedPrice(), is(nullValue()));

		prioritizePriceByTypeServiceV3.execute(priceEntityV2s, singletonList(priceNormalizedInfo2));
		assertThat(priceNormalizedInfo2.getSelectedPrice(), is(priceByAccount));

		prioritizePriceByTypeServiceV3.execute(priceEntityV2s, singletonList(priceNormalizedInfo3));
		assertThat(priceNormalizedInfo3.getSelectedPrice(), is(priceByAccount));

		prioritizePriceByTypeServiceV3.execute(priceEntityV2s, singletonList(priceNormalizedInfo4));
		assertThat(priceNormalizedInfo4.getSelectedPrice(), is(nullValue()));
	}

	@Test
	void shouldPrioritizePriceCorrectlyWhenHasOnlyPriceByPriceListForNormalizedPrices() {

		final PriceNormalizedInfo priceNormalizedInfo1 = mockPriceNormalizedInfo("V-1", "VI-1", null, null, null);
		final PriceNormalizedInfo priceNormalizedInfo2 = mockPriceNormalizedInfo("V-1", "VI-1", "VA-1", null, null);
		final PriceNormalizedInfo priceNormalizedInfo3 = mockPriceNormalizedInfo("V-1", "VI-1", "VA-1", null, "PL-1");
		final PriceNormalizedInfo priceNormalizedInfo4 = mockPriceNormalizedInfo("V-1", "VI-1", null, "VDC-1", null);

		final PriceEntityV2 priceByAccount = mockPriceEntityV2(EntityTypeEnum.PRICE_LIST, "PL-1", "V-1", "VI-1");

		final List<PriceEntityV2> priceEntityV2s = singletonList(priceByAccount);

		prioritizePriceByTypeServiceV3.execute(priceEntityV2s, singletonList(priceNormalizedInfo1));
		assertThat(priceNormalizedInfo1.getSelectedPrice(), is(nullValue()));

		prioritizePriceByTypeServiceV3.execute(priceEntityV2s, singletonList(priceNormalizedInfo2));
		assertThat(priceNormalizedInfo2.getSelectedPrice(), is(nullValue()));

		prioritizePriceByTypeServiceV3.execute(priceEntityV2s, singletonList(priceNormalizedInfo3));
		assertThat(priceNormalizedInfo3.getSelectedPrice(), is(priceByAccount));

		prioritizePriceByTypeServiceV3.execute(priceEntityV2s, singletonList(priceNormalizedInfo4));
		assertThat(priceNormalizedInfo4.getSelectedPrice(), is(nullValue()));
	}

	@Test
	void shouldPrioritizePriceCorrectlyWhenHasOnlyPriceByDeliveryCenterForNormalizedPrices() {

		final PriceNormalizedInfo priceNormalizedInfo1 = mockPriceNormalizedInfo("V-1", "VI-1", null, null, null);
		final PriceNormalizedInfo priceNormalizedInfo2 = mockPriceNormalizedInfo("V-1", "VI-1", "VA-1", null, null);
		final PriceNormalizedInfo priceNormalizedInfo3 = mockPriceNormalizedInfo("V-1", "VI-1", "VA-1", null, "PL-1");
		final PriceNormalizedInfo priceNormalizedInfo4 = mockPriceNormalizedInfo("V-1", "VI-1", null, "VDC-1", null);

		final PriceEntityV2 priceByDeliveryCenter = mockPriceEntityV2(EntityTypeEnum.DELIVERY_CENTER, "VDC-1", "V-1", "VI-1");

		final List<PriceEntityV2> priceEntityV2s = singletonList(priceByDeliveryCenter);

		prioritizePriceByTypeServiceV3.execute(priceEntityV2s, singletonList(priceNormalizedInfo1));
		assertThat(priceNormalizedInfo1.getSelectedPrice(), is(nullValue()));

		prioritizePriceByTypeServiceV3.execute(priceEntityV2s, singletonList(priceNormalizedInfo2));
		assertThat(priceNormalizedInfo2.getSelectedPrice(), is(nullValue()));

		prioritizePriceByTypeServiceV3.execute(priceEntityV2s, singletonList(priceNormalizedInfo3));
		assertThat(priceNormalizedInfo3.getSelectedPrice(), is(nullValue()));

		prioritizePriceByTypeServiceV3.execute(priceEntityV2s, singletonList(priceNormalizedInfo4));
		assertThat(priceNormalizedInfo4.getSelectedPrice(), is(priceByDeliveryCenter));
	}

	@Test
	void shouldPrioritizePriceCorrectlyWhenHasManyPricesForNormalizedPrices() {

		final PriceNormalizedInfo priceNormalizedInfo1 = mockPriceNormalizedInfo("V-1", "VI-1", null, null, null);
		final PriceNormalizedInfo priceNormalizedInfo2 = mockPriceNormalizedInfo("V-1", "VI-1", "VA-1", null, null);
		final PriceNormalizedInfo priceNormalizedInfo3 = mockPriceNormalizedInfo("V-1", "VI-1", "VA-1", null, "PL-1");
		final PriceNormalizedInfo priceNormalizedInfo4 = mockPriceNormalizedInfo("V-1", "VI-1", null, "VDC-1", null);

		final PriceEntityV2 priceEntity1 = mockPriceEntityV2(EntityTypeEnum.VENDOR, "V-1", "V-1", "VI-1");
		final PriceEntityV2 priceEntity2 = mockPriceEntityV2(EntityTypeEnum.ACCOUNT, "VA-1", "V-1", "VI-1");
		final PriceEntityV2 priceEntity3 = mockPriceEntityV2(EntityTypeEnum.PRICE_LIST, "PL-1", "V-1", "VI-1");
		final PriceEntityV2 priceEntity4 = mockPriceEntityV2(EntityTypeEnum.DELIVERY_CENTER, "VDC-1", "V-1", "VI-1");

		final List<PriceEntityV2> priceEntityV2s = Arrays.asList(priceEntity1, priceEntity2, priceEntity3, priceEntity4);

		prioritizePriceByTypeServiceV3.execute(priceEntityV2s, singletonList(priceNormalizedInfo1));
		assertThat(priceNormalizedInfo1.getSelectedPrice(), is(priceEntity1));

		prioritizePriceByTypeServiceV3.execute(priceEntityV2s, singletonList(priceNormalizedInfo2));
		assertThat(priceNormalizedInfo2.getSelectedPrice(), is(priceEntity2));

		prioritizePriceByTypeServiceV3.execute(priceEntityV2s, singletonList(priceNormalizedInfo3));
		assertThat(priceNormalizedInfo3.getSelectedPrice(), is(priceEntity2));

		prioritizePriceByTypeServiceV3.execute(priceEntityV2s, singletonList(priceNormalizedInfo4));
		assertThat(priceNormalizedInfo4.getSelectedPrice(), is(priceEntity4));
	}

	@Test
	void shouldPrioritizePriceCorrectlyWhenHasNormalizedPricesWithSameOrDifferentItemIdForDifferentTypes() {

		final PriceNormalizedInfo priceNormalizedInfo1 = mockPriceNormalizedInfo("V-1", "VI-1", null, null, null);
		final PriceNormalizedInfo priceNormalizedInfo2 = mockPriceNormalizedInfo("V-1", "VI-1", "VA-1", null, null);
		final PriceNormalizedInfo priceNormalizedInfo3 = mockPriceNormalizedInfo("V-1", "VI-1", "VA-1", null, "PL-1");
		final PriceNormalizedInfo priceNormalizedInfo4 = mockPriceNormalizedInfo("V-1", "VI-1", null, "VDC-1", null);

		final PriceNormalizedInfo priceNormalizedInfo5 = mockPriceNormalizedInfo("V-1", "VI-2", null, null, null);
		final PriceNormalizedInfo priceNormalizedInfo6 = mockPriceNormalizedInfo("V-1", "VI-2", "VA-1", null, null);
		final PriceNormalizedInfo priceNormalizedInfo7 = mockPriceNormalizedInfo("V-1", "VI-2", "VA-1", null, "PL-1");
		final PriceNormalizedInfo priceNormalizedInfo8 = mockPriceNormalizedInfo("V-1", "VI-2", null, "VDC-1", null);

		final PriceNormalizedInfo priceNormalizedInfo9 = mockPriceNormalizedInfo("V-2", "VI-1", null, null, null);
		final PriceNormalizedInfo priceNormalizedInfo10 = mockPriceNormalizedInfo("V-2", "VI-1", "VA-1", null, null);
		final PriceNormalizedInfo priceNormalizedInfo11 = mockPriceNormalizedInfo("V-2", "VI-1", "VA-1", null, "PL-1");
		final PriceNormalizedInfo priceNormalizedInfo12 = mockPriceNormalizedInfo("V-2", "VI-1", null, "VDC-1", null);

		final PriceEntityV2 priceEntity1 = mockPriceEntityV2(EntityTypeEnum.VENDOR, "V-1", "V-1", "VI-1");
		final PriceEntityV2 priceEntity2 = mockPriceEntityV2(EntityTypeEnum.ACCOUNT, "VA-1", "V-1", "VI-1");
		final PriceEntityV2 priceEntity3 = mockPriceEntityV2(EntityTypeEnum.PRICE_LIST, "PL-1", "V-1", "VI-1");
		final PriceEntityV2 priceEntity4 = mockPriceEntityV2(EntityTypeEnum.DELIVERY_CENTER, "VDC-1", "V-1", "VI-1");

		final PriceEntityV2 priceEntity5 = mockPriceEntityV2(EntityTypeEnum.VENDOR, "V-1", "V-1", "VI-2");
		final PriceEntityV2 priceEntity6 = mockPriceEntityV2(EntityTypeEnum.ACCOUNT, "VA-1", "V-1", "VI-2");
		final PriceEntityV2 priceEntity7 = mockPriceEntityV2(EntityTypeEnum.PRICE_LIST, "PL-1", "V-1", "VI-2");
		final PriceEntityV2 priceEntity8 = mockPriceEntityV2(EntityTypeEnum.DELIVERY_CENTER, "VDC-1", "V-1", "VI-2");

		final PriceEntityV2 priceEntity9 = mockPriceEntityV2(EntityTypeEnum.VENDOR, "V-2", "V-2", "VI-1");
		final PriceEntityV2 priceEntity10 = mockPriceEntityV2(EntityTypeEnum.ACCOUNT, "VA-1", "V-2", "VI-1");
		final PriceEntityV2 priceEntity11 = mockPriceEntityV2(EntityTypeEnum.PRICE_LIST, "PL-1", "V-2", "VI-1");
		final PriceEntityV2 priceEntity12 = mockPriceEntityV2(EntityTypeEnum.DELIVERY_CENTER, "VDC-1", "V-2", "VI-1");

		final List<PriceNormalizedInfo> priceNormalizedInfos = Arrays.asList(priceNormalizedInfo1, priceNormalizedInfo2,
				priceNormalizedInfo3, priceNormalizedInfo4, priceNormalizedInfo5, priceNormalizedInfo6, priceNormalizedInfo7,
				priceNormalizedInfo8, priceNormalizedInfo9, priceNormalizedInfo10, priceNormalizedInfo11, priceNormalizedInfo12);

		final List<PriceEntityV2> priceEntityV2s = Arrays.asList(priceEntity1, priceEntity2, priceEntity3, priceEntity4, priceEntity5,
				priceEntity6, priceEntity7, priceEntity8, priceEntity9, priceEntity10, priceEntity11, priceEntity12);

		prioritizePriceByTypeServiceV3.execute(priceEntityV2s, priceNormalizedInfos);

		assertThat(priceNormalizedInfo1.getSelectedPrice(), is(priceEntity1));
		assertThat(priceNormalizedInfo2.getSelectedPrice(), is(priceEntity2));
		assertThat(priceNormalizedInfo3.getSelectedPrice(), is(priceEntity2));
		assertThat(priceNormalizedInfo4.getSelectedPrice(), is(priceEntity4));
		assertThat(priceNormalizedInfo5.getSelectedPrice(), is(priceEntity5));
		assertThat(priceNormalizedInfo6.getSelectedPrice(), is(priceEntity6));
		assertThat(priceNormalizedInfo7.getSelectedPrice(), is(priceEntity6));
		assertThat(priceNormalizedInfo8.getSelectedPrice(), is(priceEntity8));
		assertThat(priceNormalizedInfo9.getSelectedPrice(), is(priceEntity9));
		assertThat(priceNormalizedInfo10.getSelectedPrice(), is(priceEntity10));
		assertThat(priceNormalizedInfo11.getSelectedPrice(), is(priceEntity10));
		assertThat(priceNormalizedInfo12.getSelectedPrice(), is(priceEntity12));
	}

	@Test
	void shouldPrioritizePriceCorrectlyWhenHasNormalizedPricesWithSameOrDifferentPriceListForDifferentAccountsHavingPricesOfTypeAccountAndPriceList() {

		final PriceNormalizedInfo priceNormalizedInfo1 = mockPriceNormalizedInfo("V-1", "VI-1", "VA-1", null, "PL-1");
		final PriceNormalizedInfo priceNormalizedInfo2 = mockPriceNormalizedInfo("V-1", "VI-1", "VA-2", null, "PL-1");
		final PriceNormalizedInfo priceNormalizedInfo3 = mockPriceNormalizedInfo("V-1", "VI-1", "VA-3", null, "PL-2");

		final PriceNormalizedInfo priceNormalizedInfo4 = mockPriceNormalizedInfo("V-1", "VI-2", "VA-1", null, "PL-1");
		final PriceNormalizedInfo priceNormalizedInfo5 = mockPriceNormalizedInfo("V-1", "VI-2", "VA-2", null, "PL-1");
		final PriceNormalizedInfo priceNormalizedInfo6 = mockPriceNormalizedInfo("V-1", "VI-2", "VA-3", null, "PL-2");

		final PriceNormalizedInfo priceNormalizedInfo7 = mockPriceNormalizedInfo("V-2", "VI-3", "VA-1", null, "PL-1");
		final PriceNormalizedInfo priceNormalizedInfo8 = mockPriceNormalizedInfo("V-2", "VI-3", "VA-2", null, "PL-1");
		final PriceNormalizedInfo priceNormalizedInfo9 = mockPriceNormalizedInfo("V-2", "VI-3", "VA-3", null, "PL-2");

		final PriceEntityV2 priceEntity1 = mockPriceEntityV2(EntityTypeEnum.VENDOR, "V-1", "V-1", "VI-1");
		final PriceEntityV2 priceEntity2 = mockPriceEntityV2(EntityTypeEnum.ACCOUNT, "VA-1", "V-1", "VI-1");
		final PriceEntityV2 priceEntity3 = mockPriceEntityV2(EntityTypeEnum.ACCOUNT, "VA-2", "V-1", "VI-1");
		final PriceEntityV2 priceEntity4 = mockPriceEntityV2(EntityTypeEnum.ACCOUNT, "VA-3", "V-1", "VI-1");
		final PriceEntityV2 priceEntity5 = mockPriceEntityV2(EntityTypeEnum.PRICE_LIST, "PL-1", "V-1", "VI-1");
		final PriceEntityV2 priceEntity6 = mockPriceEntityV2(EntityTypeEnum.PRICE_LIST, "PL-2", "V-1", "VI-1");

		final PriceEntityV2 priceEntity7 = mockPriceEntityV2(EntityTypeEnum.VENDOR, "V-1", "V-1", "VI-2");
		final PriceEntityV2 priceEntity8 = mockPriceEntityV2(EntityTypeEnum.ACCOUNT, "VA-1", "V-1", "VI-2");
		final PriceEntityV2 priceEntity9 = mockPriceEntityV2(EntityTypeEnum.ACCOUNT, "VA-2", "V-1", "VI-2");
		final PriceEntityV2 priceEntity10 = mockPriceEntityV2(EntityTypeEnum.ACCOUNT, "VA-3", "V-1", "VI-2");
		final PriceEntityV2 priceEntity11 = mockPriceEntityV2(EntityTypeEnum.PRICE_LIST, "PL-1", "V-1", "VI-2");
		final PriceEntityV2 priceEntity12 = mockPriceEntityV2(EntityTypeEnum.PRICE_LIST, "PL-2", "V-1", "VI-2");

		final PriceEntityV2 priceEntity13 = mockPriceEntityV2(EntityTypeEnum.VENDOR, "V-2", "V-2", "VI-3");
		final PriceEntityV2 priceEntity14 = mockPriceEntityV2(EntityTypeEnum.ACCOUNT, "VA-1", "V-2", "VI-3");
		final PriceEntityV2 priceEntity15 = mockPriceEntityV2(EntityTypeEnum.ACCOUNT, "VA-2", "V-2", "VI-3");
		final PriceEntityV2 priceEntity16 = mockPriceEntityV2(EntityTypeEnum.ACCOUNT, "VA-3", "V-2", "VI-3");
		final PriceEntityV2 priceEntity17 = mockPriceEntityV2(EntityTypeEnum.PRICE_LIST, "PL-1", "V-2", "VI-3");
		final PriceEntityV2 priceEntity18 = mockPriceEntityV2(EntityTypeEnum.PRICE_LIST, "PL-2", "V-2", "VI-3");

		final List<PriceNormalizedInfo> priceNormalizedInfos = Arrays.asList(priceNormalizedInfo1, priceNormalizedInfo2,
				priceNormalizedInfo3, priceNormalizedInfo4, priceNormalizedInfo5, priceNormalizedInfo6, priceNormalizedInfo7,
				priceNormalizedInfo8, priceNormalizedInfo9);

		final List<PriceEntityV2> priceEntityV2s = Arrays.asList(priceEntity1, priceEntity2, priceEntity3, priceEntity4, priceEntity5,
				priceEntity6, priceEntity7, priceEntity8, priceEntity9, priceEntity10, priceEntity11, priceEntity12, priceEntity13,
				priceEntity14, priceEntity15, priceEntity16, priceEntity17, priceEntity18);

		prioritizePriceByTypeServiceV3.execute(priceEntityV2s, priceNormalizedInfos);

		assertThat(priceNormalizedInfo1.getSelectedPrice(), is(priceEntity2));
		assertThat(priceNormalizedInfo2.getSelectedPrice(), is(priceEntity3));
		assertThat(priceNormalizedInfo3.getSelectedPrice(), is(priceEntity4));
		assertThat(priceNormalizedInfo4.getSelectedPrice(), is(priceEntity8));
		assertThat(priceNormalizedInfo5.getSelectedPrice(), is(priceEntity9));
		assertThat(priceNormalizedInfo6.getSelectedPrice(), is(priceEntity10));
		assertThat(priceNormalizedInfo7.getSelectedPrice(), is(priceEntity14));
		assertThat(priceNormalizedInfo8.getSelectedPrice(), is(priceEntity15));
		assertThat(priceNormalizedInfo9.getSelectedPrice(), is(priceEntity16));
	}

	@Test
	void shouldPrioritizePriceCorrectlyWhenHasNormalizedPricesWithSameOrDifferentPriceListForDifferentAccountsHavingNoPricesOfTypeAccount() {

		final PriceNormalizedInfo priceNormalizedInfo1 = mockPriceNormalizedInfo("V-1", "VI-1", "VA-1", null, "PL-1");
		final PriceNormalizedInfo priceNormalizedInfo2 = mockPriceNormalizedInfo("V-1", "VI-1", "VA-2", null, "PL-1");
		final PriceNormalizedInfo priceNormalizedInfo3 = mockPriceNormalizedInfo("V-1", "VI-1", "VA-3", null, "PL-2");

		final PriceNormalizedInfo priceNormalizedInfo4 = mockPriceNormalizedInfo("V-1", "VI-2", "VA-1", null, "PL-1");
		final PriceNormalizedInfo priceNormalizedInfo5 = mockPriceNormalizedInfo("V-1", "VI-2", "VA-2", null, "PL-1");
		final PriceNormalizedInfo priceNormalizedInfo6 = mockPriceNormalizedInfo("V-1", "VI-2", "VA-3", null, "PL-2");

		final PriceNormalizedInfo priceNormalizedInfo7 = mockPriceNormalizedInfo("V-2", "VI-3", "VA-1", null, "PL-1");
		final PriceNormalizedInfo priceNormalizedInfo8 = mockPriceNormalizedInfo("V-2", "VI-3", "VA-2", null, "PL-1");
		final PriceNormalizedInfo priceNormalizedInfo9 = mockPriceNormalizedInfo("V-2", "VI-3", "VA-3", null, "PL-2");

		final PriceEntityV2 priceEntity1 = mockPriceEntityV2(EntityTypeEnum.VENDOR, "V-1", "V-1", "VI-1");
		final PriceEntityV2 priceEntity2 = mockPriceEntityV2(EntityTypeEnum.PRICE_LIST, "PL-1", "V-1", "VI-1");
		final PriceEntityV2 priceEntity3 = mockPriceEntityV2(EntityTypeEnum.PRICE_LIST, "PL-2", "V-1", "VI-1");

		final PriceEntityV2 priceEntity4 = mockPriceEntityV2(EntityTypeEnum.VENDOR, "V-1", "V-1", "VI-2");
		final PriceEntityV2 priceEntity5 = mockPriceEntityV2(EntityTypeEnum.PRICE_LIST, "PL-1", "V-1", "VI-2");
		final PriceEntityV2 priceEntity6 = mockPriceEntityV2(EntityTypeEnum.PRICE_LIST, "PL-2", "V-1", "VI-2");

		final PriceEntityV2 priceEntity7 = mockPriceEntityV2(EntityTypeEnum.VENDOR, "V-2", "V-2", "VI-3");
		final PriceEntityV2 priceEntity8 = mockPriceEntityV2(EntityTypeEnum.PRICE_LIST, "PL-1", "V-2", "VI-3");
		final PriceEntityV2 priceEntity9 = mockPriceEntityV2(EntityTypeEnum.PRICE_LIST, "PL-2", "V-2", "VI-3");

		final List<PriceNormalizedInfo> priceNormalizedInfos = Arrays.asList(priceNormalizedInfo1, priceNormalizedInfo2,
				priceNormalizedInfo3, priceNormalizedInfo4, priceNormalizedInfo5, priceNormalizedInfo6, priceNormalizedInfo7,
				priceNormalizedInfo8, priceNormalizedInfo9);

		final List<PriceEntityV2> priceEntityV2s = Arrays.asList(priceEntity1, priceEntity2, priceEntity3, priceEntity4, priceEntity5,
				priceEntity6, priceEntity7, priceEntity8, priceEntity9);

		prioritizePriceByTypeServiceV3.execute(priceEntityV2s, priceNormalizedInfos);

		assertThat(priceNormalizedInfo1.getSelectedPrice(), is(priceEntity2));
		assertThat(priceNormalizedInfo2.getSelectedPrice(), is(priceEntity2));
		assertThat(priceNormalizedInfo3.getSelectedPrice(), is(priceEntity3));
		assertThat(priceNormalizedInfo4.getSelectedPrice(), is(priceEntity5));
		assertThat(priceNormalizedInfo5.getSelectedPrice(), is(priceEntity5));
		assertThat(priceNormalizedInfo6.getSelectedPrice(), is(priceEntity6));
		assertThat(priceNormalizedInfo7.getSelectedPrice(), is(priceEntity8));
		assertThat(priceNormalizedInfo8.getSelectedPrice(), is(priceEntity8));
		assertThat(priceNormalizedInfo9.getSelectedPrice(), is(priceEntity9));
	}

	@Test
	void shouldPrioritizePriceCorrectlyWhenHasNormalizedPricesWithSameOrDifferentDeliveryCenterForDifferentItems() {

		final PriceNormalizedInfo priceNormalizedInfo1 = mockPriceNormalizedInfo("V-1", "VI-1", null, "VDC-1", null);
		final PriceNormalizedInfo priceNormalizedInfo2 = mockPriceNormalizedInfo("V-1", "VI-1", null, "VDC-2", null);

		final PriceNormalizedInfo priceNormalizedInfo3 = mockPriceNormalizedInfo("V-1", "VI-2", null, "VDC-1", null);
		final PriceNormalizedInfo priceNormalizedInfo4 = mockPriceNormalizedInfo("V-1", "VI-2", null, "VDC-2", null);

		final PriceNormalizedInfo priceNormalizedInfo5 = mockPriceNormalizedInfo("V-2", "VI-1", null, "VDC-1", null);
		final PriceNormalizedInfo priceNormalizedInfo6 = mockPriceNormalizedInfo("V-2", "VI-1", null, "VDC-2", null);

		final PriceEntityV2 priceEntity1 = mockPriceEntityV2(EntityTypeEnum.VENDOR, "V-1", "V-1", "VI-1");
		final PriceEntityV2 priceEntity2 = mockPriceEntityV2(EntityTypeEnum.DELIVERY_CENTER, "VDC-1", "V-1", "VI-1");
		final PriceEntityV2 priceEntity3 = mockPriceEntityV2(EntityTypeEnum.DELIVERY_CENTER, "VDC-2", "V-1", "VI-1");

		final PriceEntityV2 priceEntity4 = mockPriceEntityV2(EntityTypeEnum.VENDOR, "V-1", "V-1", "VI-2");
		final PriceEntityV2 priceEntity5 = mockPriceEntityV2(EntityTypeEnum.DELIVERY_CENTER, "VDC-1", "V-1", "VI-2");
		final PriceEntityV2 priceEntity6 = mockPriceEntityV2(EntityTypeEnum.DELIVERY_CENTER, "VDC-2", "V-1", "VI-2");

		final PriceEntityV2 priceEntity7 = mockPriceEntityV2(EntityTypeEnum.VENDOR, "V-2", "V-2", "VI-1");
		final PriceEntityV2 priceEntity8 = mockPriceEntityV2(EntityTypeEnum.DELIVERY_CENTER, "VDC-1", "V-2", "VI-1");
		final PriceEntityV2 priceEntity9 = mockPriceEntityV2(EntityTypeEnum.DELIVERY_CENTER, "VDC-2", "V-2", "VI-1");

		final List<PriceNormalizedInfo> priceNormalizedInfos = Arrays.asList(priceNormalizedInfo1, priceNormalizedInfo2,
				priceNormalizedInfo3, priceNormalizedInfo4, priceNormalizedInfo5, priceNormalizedInfo6);

		final List<PriceEntityV2> priceEntityV2s = Arrays.asList(priceEntity1, priceEntity2, priceEntity3, priceEntity4, priceEntity5,
				priceEntity6, priceEntity7, priceEntity8, priceEntity9);

		prioritizePriceByTypeServiceV3.execute(priceEntityV2s, priceNormalizedInfos);

		assertThat(priceNormalizedInfo1.getSelectedPrice(), is(priceEntity2));
		assertThat(priceNormalizedInfo2.getSelectedPrice(), is(priceEntity3));
		assertThat(priceNormalizedInfo3.getSelectedPrice(), is(priceEntity5));
		assertThat(priceNormalizedInfo4.getSelectedPrice(), is(priceEntity6));
		assertThat(priceNormalizedInfo5.getSelectedPrice(), is(priceEntity8));
		assertThat(priceNormalizedInfo6.getSelectedPrice(), is(priceEntity9));
	}

	@Test
	void shouldPrioritizePriceCorrectlyWhenHas1NormalizedPricesAnd4Prices() {

		shouldPrioritizePriceCorrectlyWhenHasRangeNormalizedPricesAnd4PlusRangePrices(1);
	}

	@Test
	void shouldPrioritizePriceCorrectlyWhenHas50NormalizedPricesAnd200Prices() {

		shouldPrioritizePriceCorrectlyWhenHasRangeNormalizedPricesAnd4PlusRangePrices(50);
	}

	private void shouldPrioritizePriceCorrectlyWhenHasRangeNormalizedPricesAnd4PlusRangePrices(final int range) {

		final List<PriceNormalizedInfo> priceNormalizedInfos = createManyPriceNormalizedInfos(range);

		final List<PriceEntityV2> expectedSelectedPrices = new ArrayList<>();
		final List<PriceEntityV2> priceEntityV2s = createManyPriceEntityV2s(range, expectedSelectedPrices);

		prioritizePriceByTypeServiceV3.execute(priceEntityV2s, priceNormalizedInfos);

		for (int index = 0; index < 50; index++) {
			assertThat(priceNormalizedInfos.get(0).getSelectedPrice(), is(expectedSelectedPrices.get(0)));
		}
	}

	private List<PriceEntityV2> createManyPriceEntityV2s(final int range, final List<PriceEntityV2> expectedSelectedPrices) {

		return IntStream.range(0, range).mapToObj(index -> {
			// vendorId values: 0, 1, ..., (range/10)
			final String vendorId = "V-".concat(Integer.valueOf(index % 10).toString());

			// vendorId values: 0, 1, ..., range
			final String vendorItemId = "V-".concat(Integer.valueOf(index).toString());

			final PriceEntityV2 priceEntity1 = mockPriceEntityV2(EntityTypeEnum.VENDOR, vendorId, vendorId, vendorItemId);
			final PriceEntityV2 priceEntity2 = mockPriceEntityV2(EntityTypeEnum.ACCOUNT, "VA-".concat(Integer.valueOf(index).toString()),
					vendorId, vendorItemId);
			final PriceEntityV2 priceEntity3 = mockPriceEntityV2(EntityTypeEnum.PRICE_LIST, "PL-".concat(Integer.valueOf(index).toString()),
					vendorId, vendorItemId);
			final PriceEntityV2 priceEntity4 = mockPriceEntityV2(EntityTypeEnum.DELIVERY_CENTER,
					"VDC-".concat(Integer.valueOf(index).toString()), vendorId, vendorItemId);

			final int entityTypeEnum = range % 4;
			switch (entityTypeEnum) {
				case 0: // ACCOUNT
				case 1: // PRICE_LIST
					expectedSelectedPrices.add(priceEntity2);
					break;
				case 2: // DELIVERY_CENTER
					expectedSelectedPrices.add(priceEntity4);
					break;
				case 3: // VENDOR
					expectedSelectedPrices.add(priceEntity1);
					break;
			}

			return Arrays.asList(priceEntity1, priceEntity2, priceEntity3, priceEntity4);
		}).flatMap(Collection::stream).collect(Collectors.toList());
	}

	private List<PriceNormalizedInfo> createManyPriceNormalizedInfos(final int range) {

		return IntStream.range(0, range).mapToObj(index -> {
			// vendorId values: 0, 1, ..., (range/10)
			final String vendorId = "V-".concat(Integer.valueOf(index % 10).toString());

			// vendorId values: 0, 1, ..., range
			final String vendorItemId = "V-".concat(Integer.valueOf(index).toString());

			final int entityTypeEnum = range % 4;

			final String vendorAccountId = "VA-".concat(Integer.valueOf(index).toString());
			switch (entityTypeEnum) {
				case 0: // ACCOUNT
					return mockPriceNormalizedInfo(vendorId, vendorItemId, vendorAccountId, null, null);
				case 1: // PRICE_LIST
					return mockPriceNormalizedInfo(vendorId, vendorItemId, vendorAccountId, null,
							"PL-".concat(Integer.valueOf(index).toString()));
				case 2: // DELIVERY_CENTER
					return mockPriceNormalizedInfo(vendorId, vendorItemId, null, "VDC-".concat(Integer.valueOf(index).toString()), null);
				case 3: // VENDOR
					return mockPriceNormalizedInfo(vendorId, vendorItemId, null, null, null);
				default:
					return null;
			}
		}).collect(Collectors.toList());
	}

	private PriceNormalizedInfo mockPriceNormalizedInfo(final String vendorId, final String vendorItemId, final String vendorAccountId,
			final String vendorDeliveryCenterId, final String priceListId) {

		final PriceNormalizedInfo priceNormalizedInfo = new PriceNormalizedInfo();
		priceNormalizedInfo.setVendorId(vendorId);
		priceNormalizedInfo.setVendorItemId(vendorItemId);
		priceNormalizedInfo.setAccountId(vendorAccountId);
		priceNormalizedInfo.setVendorDeliveryCenterId(vendorDeliveryCenterId);
		priceNormalizedInfo.setPriceListId(priceListId);
		return priceNormalizedInfo;
	}

	private PriceEntityV2 mockPriceEntityV2(final EntityTypeEnum type, final String id, final String vendorId, final String vendorItemId) {

		final PriceEntityV2 priceEntityV2 = new PriceEntityV2();
		final PriceCompoundKeyV2 priceId = new PriceCompoundKeyV2();
		priceId.setType(ofNullable(type).map(Enum::name).orElse(null));
		priceId.setId(id);
		priceId.setVendorId(vendorId);
		priceId.setVendorItemId(vendorItemId);
		priceEntityV2.setId(priceId);
		return priceEntityV2;
	}

}