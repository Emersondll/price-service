package com.abinbev.b2b.price.api.converters.v3;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abinbev.b2b.commons.platformId.core.PlatformIdEncoderDecoder;
import com.abinbev.b2b.price.api.domain.v3.PriceNormalizedInfo;
import com.abinbev.b2b.price.api.exceptions.BadRequestException;
import com.abinbev.b2b.price.api.rest.vo.v3.PriceRequestV3;

@ExtendWith(MockitoExtension.class)
class PriceRequestV3ToPriceNormalizedInfoConverterTest {

	private static final String VENDOR_ID = "52551e44-be50-4c33-8c4f-3531ff5b4163";
	private static final String VENDOR_ITEM_ID = "VI-1";
	private static final String ACCOUNT_ID = "A-1";
	private static final String VENDOR_DELIVERY_CENTER_ID = "VDC-1";

	private static final String ITEM_ID = "VWxVZVJMNVFURE9NVHpVeC8xdEJZdz09O1ZJLTE=";
	private static final String CONTRACT_ID = "VWxVZVJMNVFURE9NVHpVeC8xdEJZdz09O0EtMQ==";
	private static final String DELIVERY_CENTER_ID = "VWxVZVJMNVFURE9NVHpVeC8xdEJZdz09O1ZEQy0x";
	private static final String PRICE_LIST_ID = "PL-1";

	private static final String CONTRACT_ID_WITHOUT_VENDOR_ITEM_ID = "VWxVZVJMNVFURE9NVHpVeC8xdEJZdz09O251bGw=";

	@InjectMocks
	private PriceRequestV3ToPriceNormalizedInfoConverter priceRequestV3ToPriceNormalizedInfoConverter;

	@Spy
	private PlatformIdEncoderDecoder platformIdEncoderDecoder;

	private static Stream<Arguments> priceRequestWithInvalidContractIdProvider() {

		final PriceRequestV3 priceReqWithEmptyContractId = new PriceRequestV3();
		priceReqWithEmptyContractId.setContractId("");
		priceReqWithEmptyContractId.setItemId(ITEM_ID);
		final PriceRequestV3 priceReqWithoutContractId = new PriceRequestV3();
		priceReqWithoutContractId.setItemId(ITEM_ID);
		return Stream.of(Arguments.of(priceReqWithoutContractId), Arguments.of(priceReqWithEmptyContractId));
	}

	@Test
	void shouldConvertCorrectlyWhenHasOnlyItemId() {

		final PriceRequestV3 priceRequestV3 = new PriceRequestV3();
		priceRequestV3.setItemId(ITEM_ID);

		final PriceNormalizedInfo priceNormalizedInfo = priceRequestV3ToPriceNormalizedInfoConverter.convert(priceRequestV3);
		assertThat(priceNormalizedInfo.getItemId(), is(equalTo(ITEM_ID)));
		assertThat(priceNormalizedInfo.getContractId(), is(nullValue()));
		assertThat(priceNormalizedInfo.getPriceListId(), is(nullValue()));
		assertThat(priceNormalizedInfo.getDeliveryCenterId(), is(nullValue()));
		assertThat(priceNormalizedInfo.getVendorId(), is(equalTo(VENDOR_ID)));
		assertThat(priceNormalizedInfo.getVendorItemId(), is(equalTo(VENDOR_ITEM_ID)));
		assertThat(priceNormalizedInfo.getAccountId(), is(nullValue()));
		assertThat(priceNormalizedInfo.getVendorDeliveryCenterId(), is(nullValue()));
	}

	@Test
	void shouldConvertCorrectlyWhenHasItemIdAndContractId() {

		final PriceRequestV3 priceRequestV3 = new PriceRequestV3();
		priceRequestV3.setItemId(ITEM_ID);
		priceRequestV3.setContractId(CONTRACT_ID);

		final PriceNormalizedInfo priceNormalizedInfo = priceRequestV3ToPriceNormalizedInfoConverter.convert(priceRequestV3);
		assertThat(priceNormalizedInfo.getItemId(), is(equalTo(ITEM_ID)));
		assertThat(priceNormalizedInfo.getContractId(), is(equalTo(CONTRACT_ID)));
		assertThat(priceNormalizedInfo.getPriceListId(), is(nullValue()));
		assertThat(priceNormalizedInfo.getDeliveryCenterId(), is(nullValue()));
		assertThat(priceNormalizedInfo.getVendorId(), is(equalTo(VENDOR_ID)));
		assertThat(priceNormalizedInfo.getVendorItemId(), is(equalTo(VENDOR_ITEM_ID)));
		assertThat(priceNormalizedInfo.getAccountId(), is(equalTo(ACCOUNT_ID)));
		assertThat(priceNormalizedInfo.getVendorDeliveryCenterId(), is(nullValue()));
	}

	@Test
	void shouldConvertCorrectlyWhenHasItemIdAndDeliveryCenterId() {

		final PriceRequestV3 priceRequestV3 = new PriceRequestV3();
		priceRequestV3.setItemId(ITEM_ID);
		priceRequestV3.setDeliveryCenterId(DELIVERY_CENTER_ID);

		final PriceNormalizedInfo priceNormalizedInfo = priceRequestV3ToPriceNormalizedInfoConverter.convert(priceRequestV3);
		assertThat(priceNormalizedInfo.getItemId(), is(equalTo(ITEM_ID)));
		assertThat(priceNormalizedInfo.getContractId(), is(nullValue()));
		assertThat(priceNormalizedInfo.getPriceListId(), is(nullValue()));
		assertThat(priceNormalizedInfo.getDeliveryCenterId(), is(equalTo(DELIVERY_CENTER_ID)));
		assertThat(priceNormalizedInfo.getVendorId(), is(equalTo(VENDOR_ID)));
		assertThat(priceNormalizedInfo.getVendorItemId(), is(equalTo(VENDOR_ITEM_ID)));
		assertThat(priceNormalizedInfo.getAccountId(), is(nullValue()));
		assertThat(priceNormalizedInfo.getVendorDeliveryCenterId(), is(equalTo(VENDOR_DELIVERY_CENTER_ID)));
	}

	@Test
	void shouldConvertCorrectlyWhenHasItemIdAndContractIdAndPriceListId() {

		final PriceRequestV3 priceRequestV3 = new PriceRequestV3();
		priceRequestV3.setItemId(ITEM_ID);
		priceRequestV3.setContractId(CONTRACT_ID);
		priceRequestV3.setPriceListId(PRICE_LIST_ID);

		final PriceNormalizedInfo priceNormalizedInfo = priceRequestV3ToPriceNormalizedInfoConverter.convert(priceRequestV3);
		assertThat(priceNormalizedInfo.getItemId(), is(equalTo(ITEM_ID)));
		assertThat(priceNormalizedInfo.getContractId(), is(equalTo(CONTRACT_ID)));
		assertThat(priceNormalizedInfo.getPriceListId(), is(equalTo(PRICE_LIST_ID)));
		assertThat(priceNormalizedInfo.getDeliveryCenterId(), is(nullValue()));
		assertThat(priceNormalizedInfo.getVendorId(), is(equalTo(VENDOR_ID)));
		assertThat(priceNormalizedInfo.getVendorItemId(), is(equalTo(VENDOR_ITEM_ID)));
		assertThat(priceNormalizedInfo.getAccountId(), is(equalTo(ACCOUNT_ID)));
		assertThat(priceNormalizedInfo.getVendorDeliveryCenterId(), is(nullValue()));
	}

	@Test
	void shouldConvertCorrectlyWhenHasItemIdAndContractIdAndPriceListIdAndDeliveryCenterId() {

		final PriceRequestV3 priceRequestV3 = new PriceRequestV3();
		priceRequestV3.setItemId(ITEM_ID);
		priceRequestV3.setContractId(CONTRACT_ID);
		priceRequestV3.setPriceListId(PRICE_LIST_ID);
		priceRequestV3.setDeliveryCenterId(DELIVERY_CENTER_ID);

		final PriceNormalizedInfo priceNormalizedInfo = priceRequestV3ToPriceNormalizedInfoConverter.convert(priceRequestV3);
		assertThat(priceNormalizedInfo.getItemId(), is(equalTo(ITEM_ID)));
		assertThat(priceNormalizedInfo.getContractId(), is(equalTo(CONTRACT_ID)));
		assertThat(priceNormalizedInfo.getPriceListId(), is(equalTo(PRICE_LIST_ID)));
		assertThat(priceNormalizedInfo.getDeliveryCenterId(), is(equalTo(DELIVERY_CENTER_ID)));
		assertThat(priceNormalizedInfo.getVendorId(), is(equalTo(VENDOR_ID)));
		assertThat(priceNormalizedInfo.getVendorItemId(), is(equalTo(VENDOR_ITEM_ID)));
		assertThat(priceNormalizedInfo.getAccountId(), is(equalTo(ACCOUNT_ID)));
		assertThat(priceNormalizedInfo.getVendorDeliveryCenterId(), is(equalTo(VENDOR_DELIVERY_CENTER_ID)));
	}

	@Test
	void shouldValidateWhenHasWrongItemId() {

		final PriceRequestV3 priceRequestV3 = new PriceRequestV3();
		priceRequestV3.setItemId("Wrong-itemId");
		priceRequestV3.setContractId(CONTRACT_ID);
		priceRequestV3.setPriceListId(PRICE_LIST_ID);
		priceRequestV3.setDeliveryCenterId(DELIVERY_CENTER_ID);

		Assert.assertThrows("Error decoding itemId from request body", BadRequestException.class,
				() -> priceRequestV3ToPriceNormalizedInfoConverter.convert(priceRequestV3));
	}

	@Test
	void shouldValidateWhenHasWrongContractId() {

		final PriceRequestV3 priceRequestV3 = new PriceRequestV3();
		priceRequestV3.setItemId(ITEM_ID);
		priceRequestV3.setContractId("Wrong-contractId");
		priceRequestV3.setPriceListId(PRICE_LIST_ID);
		priceRequestV3.setDeliveryCenterId(DELIVERY_CENTER_ID);

		Assert.assertThrows("Error decoding contractId from request body", BadRequestException.class,
				() -> priceRequestV3ToPriceNormalizedInfoConverter.convert(priceRequestV3));
	}

	@Test
	void shouldValidateWhenHasWrongDeliveryCenterId() {

		final PriceRequestV3 priceRequestV3 = new PriceRequestV3();
		priceRequestV3.setItemId(ITEM_ID);
		priceRequestV3.setContractId(CONTRACT_ID);
		priceRequestV3.setPriceListId(PRICE_LIST_ID);
		priceRequestV3.setDeliveryCenterId("Wrong-deliveryCenterId");

		Assert.assertThrows("Error decoding deliveryCenterId from request body", BadRequestException.class,
				() -> priceRequestV3ToPriceNormalizedInfoConverter.convert(priceRequestV3));
	}

	@Test
	void shouldValidateWhenTheItemIdContainsOnlyTheVendorId() {

		final PriceRequestV3 priceRequestV3 = new PriceRequestV3();
		priceRequestV3.setItemId(ITEM_ID);
		priceRequestV3.setContractId(CONTRACT_ID_WITHOUT_VENDOR_ITEM_ID);

		Assert.assertThrows("Error decoding contractId from request body", BadRequestException.class,
				() -> priceRequestV3ToPriceNormalizedInfoConverter.convert(priceRequestV3));
	}

	@ParameterizedTest
	@MethodSource("priceRequestWithInvalidContractIdProvider")
	void shouldNotSetAccountIdWhenContractIdIsNullOrEmpty(final PriceRequestV3 priceRequestV3) {

		final PriceNormalizedInfo priceNormalizedInfo = priceRequestV3ToPriceNormalizedInfoConverter.convert(priceRequestV3);
		assertThat(priceNormalizedInfo.getAccountId(), is(nullValue()));
	}
}