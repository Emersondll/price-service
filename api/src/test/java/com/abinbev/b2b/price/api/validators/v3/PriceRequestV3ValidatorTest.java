package com.abinbev.b2b.price.api.validators.v3;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import au.com.dius.pact.provider.junit.Provider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abinbev.b2b.price.api.config.ApiConfig;
import com.abinbev.b2b.price.api.exceptions.BadRequestException;
import com.abinbev.b2b.price.api.rest.vo.v3.PriceRequestV3;

@ExtendWith(MockitoExtension.class)
class PriceRequestV3ValidatorTest {

	private static final String ITEM_ID = "VWxVZVJMNVFURE9NVHpVeC8xdEJZdz09O1ZJLTE=";
	private static final String CONTRACT_ID = "VWxVZVJMNVFURE9NVHpVeC8xdEJZdz09O0EtMQ==";
	private static final String DELIVERY_CENTER_ID = "VWxVZVJMNVFURE9NVHpVeC8xdEJZdz09O1ZEQy0x";
	private static final String PRICE_LIST_ID = "PL-1";
	private final Integer PRICE_V3_REQUEST_BODY_ITEMS_LIMIT = 50;
	@InjectMocks
	private PriceRequestV3Validator priceRequestV3Validator;

	@Mock
	private ApiConfig apiConfig;

	private static Stream<Arguments> invalidPriceRequestV3Provider() {

		final PriceRequestV3 priceRequestV3WithoutContractId = new PriceRequestV3();
		priceRequestV3WithoutContractId.setItemId(ITEM_ID);
		priceRequestV3WithoutContractId.setPriceListId(PRICE_LIST_ID);

		final PriceRequestV3 priceRequestV3WithBlankContractId = new PriceRequestV3();
		priceRequestV3WithBlankContractId.setItemId(ITEM_ID);
		priceRequestV3WithBlankContractId.setPriceListId(PRICE_LIST_ID);
		priceRequestV3WithBlankContractId.setContractId("");

		return Stream.of(Arguments.of(priceRequestV3WithoutContractId), Arguments.of(priceRequestV3WithBlankContractId));
	}

	@Test
	void shouldDoNothingWhenHasValidParameters() {

		when(apiConfig.getPriceV3RequestBodyItemsLimit()).thenReturn(PRICE_V3_REQUEST_BODY_ITEMS_LIMIT);

		assertDoesNotThrow(() -> priceRequestV3Validator.validate(
				singletonList(buildPriceRequestV3(ITEM_ID, null, null, null))));

		assertDoesNotThrow(() -> priceRequestV3Validator.validate(
				singletonList(buildPriceRequestV3(ITEM_ID, CONTRACT_ID, null, null))));

		assertDoesNotThrow(() -> priceRequestV3Validator.validate(
				singletonList(buildPriceRequestV3(ITEM_ID, null, DELIVERY_CENTER_ID, null))));

		assertDoesNotThrow(() -> priceRequestV3Validator.validate(
				singletonList(buildPriceRequestV3(ITEM_ID, CONTRACT_ID, DELIVERY_CENTER_ID, null))));

		assertDoesNotThrow(() -> priceRequestV3Validator.validate(
				singletonList(buildPriceRequestV3(ITEM_ID, CONTRACT_ID, null, PRICE_LIST_ID))));

		assertDoesNotThrow(() -> priceRequestV3Validator.validate(
				singletonList(buildPriceRequestV3(ITEM_ID, CONTRACT_ID, DELIVERY_CENTER_ID, PRICE_LIST_ID))));
	}

	@ParameterizedTest
	@MethodSource("invalidPriceRequestV3Provider")
	void shouldValidatePriceRequestV3WhenHasPriceListIdAndHasNotContractId(final PriceRequestV3 priceRequestV3) {

		final List<PriceRequestV3> priceRequestV3List = singletonList(priceRequestV3);
		assertThrows(BadRequestException.class, () -> priceRequestV3Validator.validate(
				priceRequestV3List));
	}

	@Test
	void shouldVDoNothingWhenHas50ValidItems() {

		when(apiConfig.getPriceV3RequestBodyItemsLimit()).thenReturn(PRICE_V3_REQUEST_BODY_ITEMS_LIMIT);

		assertDoesNotThrow(() -> priceRequestV3Validator.validate(mockItems(PRICE_V3_REQUEST_BODY_ITEMS_LIMIT)));
	}

	@Test
	void shouldValidatePriceRequestV3WhenHas51ValidItems() {

		final List<PriceRequestV3> priceRequestV3List = mockItems(51);
		assertThrows(BadRequestException.class, () -> priceRequestV3Validator.validate(priceRequestV3List));
	}

	@Test
	void shouldValidatePriceRequestV3WhenHasNoItems() {

		final List<PriceRequestV3> emptyList = emptyList();
		assertThrows(BadRequestException.class, () -> priceRequestV3Validator.validate(emptyList));
	}

	private PriceRequestV3 buildPriceRequestV3(final String itemId, final String contractId, final String deliveryCenterId,
			final String priceListId) {

		final PriceRequestV3 priceRequestV3 = new PriceRequestV3();
		priceRequestV3.setItemId(itemId);
		priceRequestV3.setContractId(contractId);
		priceRequestV3.setDeliveryCenterId(deliveryCenterId);
		priceRequestV3.setPriceListId(priceListId);

		return priceRequestV3;
	}

	private List<PriceRequestV3> mockItems(final int quantity) {

		return IntStream.range(0, quantity).mapToObj(index -> buildPriceRequestV3("I-" + index, "C" + index, "DC-" + index, "PL" + index))
				.collect(Collectors.toList());
	}

}