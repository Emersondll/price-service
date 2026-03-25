package com.abinbev.b2b.price.api.services.v3;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abinbev.b2b.price.api.config.properties.ToggleConfigurationProperties;
import com.abinbev.b2b.price.domain.model.v2.PriceCompoundKeyV2;
import com.abinbev.b2b.price.domain.model.v2.PriceEntityV2;

@ExtendWith(MockitoExtension.class)
public class ObtainPricesWithClosestDatesServiceTest {

	private static final String ID_1 = "ID_1";
	private static final String ID_2 = "ID_2";

	private static final String VENDOR_ID_1 = "VENDOR_ID_1";
	private static final String VENDOR_ID_2 = "VENDOR_ID_2";

	private static final String VENDOR_ITEM_ID_1 = "VENDOR_ITEM_ID_1";
	private static final String VENDOR_ITEM_ID_2 = "VENDOR_ITEM_ID_2";

	private static final String ACCOUNT_TYPE = "ACCOUNT";
	private static final String DELIVERY_CENTER_TYPE = "DELIVERY_CENTER";
	private static final String PRICE_LIST_TYPE = "PRICE_LIST";
	private static final String VENDOR_TYPE = "VENDOR";
	// Remove this constant when removing BEESPR_19558
	private static final String CODE_TOGGLE_BEESPR_19558 = "BEESPR_19558";

	private static final ChronoUnit CRON_UNIT_HOURS = ChronoUnit.HOURS;

	@InjectMocks
	private ObtainPricesWithClosestDatesService pricesUpfrontService;

	// Remove this mock when removing BEESPR_19558
	@Mock
	private ToggleConfigurationProperties toggleConfigurationProperties;

	@Test
	void shouldGetClosestDatePricesWhenThereAreMoreThanOneForTheSameKeys() {
		// Remove this mock when removing BEESPR_19558
		when(toggleConfigurationProperties.isEnabledCodeToggle(CODE_TOGGLE_BEESPR_19558)).thenReturn(true);

		final Instant now = Instant.now();

		final PriceEntityV2 price1 = mockPriceEntityV2(ID_1, ACCOUNT_TYPE, VENDOR_ID_1, VENDOR_ITEM_ID_1, getValidFrom(now, 5));
		final PriceEntityV2 price2 = mockPriceEntityV2(ID_1, ACCOUNT_TYPE, VENDOR_ID_1, VENDOR_ITEM_ID_1, getValidFrom(now, 10));
		final PriceEntityV2 price3 = mockPriceEntityV2(ID_1, ACCOUNT_TYPE, VENDOR_ID_1, VENDOR_ITEM_ID_1, getValidFrom(now, 3));

		final PriceEntityV2 price4 = mockPriceEntityV2(ID_1, ACCOUNT_TYPE, VENDOR_ID_2, VENDOR_ITEM_ID_1, getValidFrom(now, 3));
		final PriceEntityV2 price5 = mockPriceEntityV2(ID_1, ACCOUNT_TYPE, VENDOR_ID_2, VENDOR_ITEM_ID_1, getValidFrom(now, 4));
		final PriceEntityV2 price6 = mockPriceEntityV2(ID_1, ACCOUNT_TYPE, VENDOR_ID_2, VENDOR_ITEM_ID_1, getValidFrom(now, 1));

		final PriceEntityV2 price7 = mockPriceEntityV2(ID_1, DELIVERY_CENTER_TYPE, VENDOR_ID_2, VENDOR_ITEM_ID_2, getValidFrom(now, 8));
		final PriceEntityV2 price8 = mockPriceEntityV2(ID_1, DELIVERY_CENTER_TYPE, VENDOR_ID_2, VENDOR_ITEM_ID_2, getValidFrom(now, 7));
		final PriceEntityV2 price9 = mockPriceEntityV2(ID_1, DELIVERY_CENTER_TYPE, VENDOR_ID_2, VENDOR_ITEM_ID_2, getValidFrom(now, 9));

		final PriceEntityV2 price10 = mockPriceEntityV2(ID_2, DELIVERY_CENTER_TYPE, VENDOR_ID_2, VENDOR_ITEM_ID_2, getValidFrom(now, 40));
		final PriceEntityV2 price11 = mockPriceEntityV2(ID_2, DELIVERY_CENTER_TYPE, VENDOR_ID_2, VENDOR_ITEM_ID_2, getValidFrom(now, 20));
		final PriceEntityV2 price12 = mockPriceEntityV2(ID_2, DELIVERY_CENTER_TYPE, VENDOR_ID_2, VENDOR_ITEM_ID_2, getValidFrom(now, 60));

		final List<PriceEntityV2> pricesAfterSelection = pricesUpfrontService.execute(
				List.of(price1, price2, price3, price4, price5, price6, price7, price8, price9, price10, price11, price12));

		assertThat(pricesAfterSelection, hasSize(4));
		assertTrue(pricesAfterSelection.containsAll(List.of(price3, price6, price11, price8)));
	}

	@Test
	void shouldGetClosestDatePricesWhenThereAreMoreThanOneForTheSameKeysAndNotReturnInvalidValues() {
		// Remove this mock when removing BEESPR_19558
		when(toggleConfigurationProperties.isEnabledCodeToggle(CODE_TOGGLE_BEESPR_19558)).thenReturn(true);

		final Instant now = Instant.now();

		final PriceEntityV2 price1 = mockPriceEntityV2(ID_1, ACCOUNT_TYPE, VENDOR_ID_1, VENDOR_ITEM_ID_1, getValidFrom(now, 5));
		final PriceEntityV2 price2 = mockPriceEntityV2(ID_1, PRICE_LIST_TYPE, VENDOR_ID_1, VENDOR_ITEM_ID_1, getValidFrom(now, 10));
		final PriceEntityV2 price3 = mockPriceEntityV2(ID_1, VENDOR_TYPE, VENDOR_ID_1, VENDOR_ITEM_ID_1, getValidFrom(now, 3));

		final PriceEntityV2 price4 = mockPriceEntityV2(ID_2, VENDOR_TYPE, VENDOR_ID_1, VENDOR_ITEM_ID_1, null);
		price4.setValidTo(new Date());
		final PriceEntityV2 price5 = mockPriceEntityV2(ID_2, VENDOR_TYPE, VENDOR_ID_1, VENDOR_ITEM_ID_1, now);

		final PriceEntityV2 price6 = mockPriceEntityV2(ID_2, PRICE_LIST_TYPE, VENDOR_ID_1, VENDOR_ITEM_ID_1, null);
		final PriceEntityV2 price7 = mockPriceEntityV2(ID_2, PRICE_LIST_TYPE, VENDOR_ID_1, VENDOR_ITEM_ID_1, now);

		final List<PriceEntityV2> pricesAfterSelection = pricesUpfrontService.execute(
				List.of(price1, price2, price3, price4, price5, price6, price7));

		assertThat(pricesAfterSelection, hasSize(5));
		assertTrue(pricesAfterSelection.containsAll(List.of(price1, price2, price3, price5, price6)));
	}

	// Remove this test when removing BEESPR_19558
	@Test
	void shouldGetClosestDatePricesWhenCodeToggleBeespr19558IsDisabled() {

		when(toggleConfigurationProperties.isEnabledCodeToggle(CODE_TOGGLE_BEESPR_19558)).thenReturn(false);

		final Instant now = Instant.now();

		final PriceEntityV2 price1 = mockPriceEntityV2(ID_1, ACCOUNT_TYPE, VENDOR_ID_1, VENDOR_ITEM_ID_1, getValidFrom(now, 5));
		final PriceEntityV2 price2 = mockPriceEntityV2(ID_1, PRICE_LIST_TYPE, VENDOR_ID_1, VENDOR_ITEM_ID_1, getValidFrom(now, 10));
		final PriceEntityV2 price3 = mockPriceEntityV2(ID_1, VENDOR_TYPE, VENDOR_ID_1, VENDOR_ITEM_ID_1, getValidFrom(now, 3));

		final PriceEntityV2 price4 = mockPriceEntityV2(ID_2, VENDOR_TYPE, VENDOR_ID_1, VENDOR_ITEM_ID_1, null);
		price4.setValidTo(new Date());
		final PriceEntityV2 price5 = mockPriceEntityV2(ID_2, VENDOR_TYPE, VENDOR_ID_1, VENDOR_ITEM_ID_1, now);

		final PriceEntityV2 price6 = mockPriceEntityV2(ID_2, PRICE_LIST_TYPE, VENDOR_ID_1, VENDOR_ITEM_ID_1, null);
		final PriceEntityV2 price7 = mockPriceEntityV2(ID_2, PRICE_LIST_TYPE, VENDOR_ID_1, VENDOR_ITEM_ID_1, now);

		final List<PriceEntityV2> pricesAfterSelection = pricesUpfrontService.execute(
				List.of(price1, price2, price3, price4, price5, price6, price7));

		assertThat(pricesAfterSelection, hasSize(5));
		assertTrue(pricesAfterSelection.containsAll(List.of(price1, price2, price3, price5, price7)));
	}

	private PriceEntityV2 mockPriceEntityV2(final String id, final String type, final String vendorId, final String vendorItemId,
			final Instant validFrom) {

		final PriceEntityV2 priceEntityV2 = new PriceEntityV2();
		final PriceCompoundKeyV2 priceCompoundKeyV2 = new PriceCompoundKeyV2();

		priceCompoundKeyV2.setId(id);
		priceCompoundKeyV2.setType(type);
		priceCompoundKeyV2.setVendorId(vendorId);
		priceCompoundKeyV2.setVendorItemId(vendorItemId);

		priceCompoundKeyV2.setValidFrom(validFrom);

		priceEntityV2.setId(priceCompoundKeyV2);

		return priceEntityV2;
	}

	private Instant getValidFrom(final Instant now, final int hoursToSubtract) {

		return now.minus(hoursToSubtract, CRON_UNIT_HOURS);
	}

}
