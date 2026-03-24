package com.abinbev.b2b.price.api.controllers;

import static com.abinbev.b2b.price.api.testhelpers.TestConstants.ACCOUNT_ID_PARAM_NAME;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.AUTHORIZATION_HEADER_NAME;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.COUNTRY_HEADER_NAME;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_COUNTRY_ZA;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_JWT_B2B;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_JWT_B2B_ZA_COUNTRY;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_JWT_HMAC;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_JWT_INVALID_APP_B2B;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_JWT_JUST_READ_M2M;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_JWT_JUST_WRITE_M2M;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_JWT_M2M;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_JWT_WITHOUT_APP_B2B;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_JWT_WITHOUT_COUNTRY_B2B;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_JWT_WITHOUT_CUSTOMER_ROLE_HMCA;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_JWT_WITHOUT_EXTENSION_ACCOUNTS_B2B;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_JWT_WITH_SUPPORTED_APP_ONCUSTOMER;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_JWT_WITH_UNSUPPORTED_APP;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_REQUEST_TRACE_ID;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.REQUEST_TRACE_ID_HEADER_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsIterableContaining.hasItems;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.text.IsEmptyString.emptyString;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.abinbev.b2b.price.api.domain.BrowsePrice;
import com.abinbev.b2b.price.api.exceptions.NotFoundException;
import com.abinbev.b2b.price.api.services.CalculateBrowsePricesV1OrchestratorService;
import com.abinbev.b2b.price.api.services.v2.CalculateBrowsePricesV2OrchestratorService;
import com.abinbev.b2b.price.api.testhelpers.IntegrationTestsResourceHelper;
import com.abinbev.b2b.price.api.testhelpers.TestConstants;
import com.fasterxml.jackson.core.type.TypeReference;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-integration-tests.yml")
class PriceOffersControllerGetV1OffersIntegrationTest {

	private static final String SIMPLE_V2_OFFERS_RESPONSE = "correct-simple-v2-offers-response.json";
	private static final String SIMPLE_OFFERS_RESPONSE = "correct-simple-offers-response.json";
	private static final String SIMPLE_OFFERS_MOCK = "simple-offers.json";
	private static final String SIMPLE_V2_OFFERS_MOCK = "simple-v2-offers.json";
	private static final String DATABASE_ACCESS_ERROR_RESPONSE = "serviceUnavailable-database-access-failed.json";
	private static final String PATH = "/v1/offers";
	private static final String GET_V1_OFFERS_RESOURCE_PATH = "get_v1_offers";

	private static final String COUNTRY_HEADER = "country";
	private static final String REQUEST_TRACE_ID_HEADER = "requestTraceId";
	private static final String AUTHORIZATION_HEADER = "authorization";
	private static final String VENDOR_ID_HEADER = "vendorId";
	private static final String VENDOR_ACCOUNT_ID_HEADER = "vendorAccountId";

	private static final String ACCOUNT_ID_PARAM = "accountId";
	private static final String SKUS_PARAM = "skus";
	private static final String VENDOR_ITEM_IDS_PARAM = "vendorItemIds";

	private static final String ACCOUNT_ID = "12345678901234";
	private static final String MOCKED_ACCOUNT_ID = "26812643000123";
	private static final String MOCKED_ACCOUNT_ID1 = "12451514";
	private static final String REQUEST_TRACE_ID = "0a2f9f71-f96d-47c9-9ce1-c7af73a449d5";
	private static final String MOCKED_VENDOR_ID = "VENDOR_ID_1";
	private static final String MOCKED_VENDOR_ACCOUNT_ID = "VENDOR_ACCOUNT_ID_1";
	private static final String MOCKED_COUNTRY_US = "us";
	private static final String MOCKED_COUNTRY_BR = "br";
	private static final String MOCKED_COUNTRY_CA = "ca";
	private static final String MOCKED_COUNTRY_PE = "PE";
	private static final String MOCKED_VENDOR_ITEM_ID_1 = "VENDOR_ITEM_ID_001";
	private static final String MOCKED_VENDOR_ITEM_ID_2 = "VENDOR_ITEM_ID_002";
	private static final String MOCKED_VENDOR_ITEM_ID_3 = "VENDOR_ITEM_ID_003";
	private static final String DEFAULT_AUTHORIZATION = MOCKED_JWT_M2M;
	private static final String DEFAULT_VENDOR_ID_CA = "86e691a4-16a8-4478-bc43-461c76fad14b";
	private static final String DEFAULT_VENDOR_ID_PE = "1769e9ef-722b-4c19-b136-b3fc69ea15e8";

	@MockBean
	private CalculateBrowsePricesV1OrchestratorService calculateBrowsePricesV1OrchestratorService;

	@MockBean
	private CalculateBrowsePricesV2OrchestratorService calculateBrowsePricesV2OrchestratorService;

	@Autowired
	private MockMvc mockMvc;

	@Captor
	private ArgumentCaptor<List<String>> skusCaptor;

	@Captor
	private ArgumentCaptor<List<String>> vendorItemIdsCaptor;

	@Captor
	private ArgumentCaptor<String> vendorIdCaptor;

	private Object getResponseDataFileContent(final String fileName) throws IOException {

		return IntegrationTestsResourceHelper.getResponseDataFileContent(GET_V1_OFFERS_RESOURCE_PATH, fileName);
	}

	private Object getResponseDataFileContent(final String fileName, final Object... args) throws IOException {

		return IntegrationTestsResourceHelper.getResponseDataFileContent(GET_V1_OFFERS_RESOURCE_PATH, fileName, args);
	}

	private List<BrowsePrice> getBrowsePriceMock(final String mockOffersFile) throws IOException {

		return IntegrationTestsResourceHelper.getMockDataFileContent(GET_V1_OFFERS_RESOURCE_PATH, mockOffersFile, new TypeReference<>() {
		});
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersV2WhenHaveVendorIdAndVendorAccountIdProcessCorrectly() throws Exception {

		doReturn(getBrowsePriceMock(SIMPLE_V2_OFFERS_MOCK)).when(calculateBrowsePricesV2OrchestratorService)
				.execute(eq(MOCKED_VENDOR_ID), eq(MOCKED_VENDOR_ACCOUNT_ID), eq(MOCKED_COUNTRY_US), vendorItemIdsCaptor.capture());

		mockMvc.perform(get(PATH).header(VENDOR_ID_HEADER, MOCKED_VENDOR_ID).header(COUNTRY_HEADER, MOCKED_COUNTRY_US)
				.header(VENDOR_ACCOUNT_ID_HEADER, MOCKED_VENDOR_ACCOUNT_ID).header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)
				.header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION)).andExpect(status().isOk())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent(SIMPLE_V2_OFFERS_RESPONSE))));

		assertThat(vendorItemIdsCaptor.getValue(), nullValue());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersV2CorrectlyWhenSendingNoVendorId() throws Exception {

		doReturn(getBrowsePriceMock(SIMPLE_V2_OFFERS_MOCK)).when(calculateBrowsePricesV2OrchestratorService)
				.execute(vendorIdCaptor.capture(), eq(MOCKED_VENDOR_ACCOUNT_ID), eq(MOCKED_COUNTRY_CA), vendorItemIdsCaptor.capture());

		mockMvc.perform(get(PATH).header(COUNTRY_HEADER, MOCKED_COUNTRY_CA).header(VENDOR_ACCOUNT_ID_HEADER, MOCKED_VENDOR_ACCOUNT_ID)
				.header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION))
				.andExpect(status().isOk()).andExpect(jsonPath("$", equalTo(getResponseDataFileContent(SIMPLE_V2_OFFERS_RESPONSE))));

		assertThat(vendorIdCaptor.getValue(), is(DEFAULT_VENDOR_ID_CA));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersV2UsingDefaultVendorIdAlwaysWhenItsConfigured() throws Exception {

		doReturn(getBrowsePriceMock(SIMPLE_V2_OFFERS_MOCK)).when(calculateBrowsePricesV2OrchestratorService)
				.execute(vendorIdCaptor.capture(), eq(MOCKED_VENDOR_ACCOUNT_ID), eq(MOCKED_COUNTRY_PE), vendorItemIdsCaptor.capture());

		mockMvc.perform(get(PATH).header(VENDOR_ID_HEADER, "XPTO").header(COUNTRY_HEADER, MOCKED_COUNTRY_PE)
				.header(VENDOR_ACCOUNT_ID_HEADER, MOCKED_VENDOR_ACCOUNT_ID).header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)
				.header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION)).andExpect(status().isOk())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent(SIMPLE_V2_OFFERS_RESPONSE))));

		assertThat(vendorIdCaptor.getValue(), is(DEFAULT_VENDOR_ID_PE));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersV2WhenHaveHeaderVendorIdAndAccountIdParamProcessCorrectly() throws Exception {

		doReturn(getBrowsePriceMock(SIMPLE_V2_OFFERS_MOCK)).when(calculateBrowsePricesV2OrchestratorService)
				.execute(eq(MOCKED_VENDOR_ID), eq(MOCKED_VENDOR_ACCOUNT_ID), eq(MOCKED_COUNTRY_US), vendorItemIdsCaptor.capture());

		mockMvc.perform(get(PATH).header(VENDOR_ID_HEADER, MOCKED_VENDOR_ID).param(ACCOUNT_ID_PARAM, MOCKED_VENDOR_ACCOUNT_ID)
				.header(COUNTRY_HEADER, MOCKED_COUNTRY_US).header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)
				.header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION)).andExpect(status().isOk())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent(SIMPLE_V2_OFFERS_RESPONSE))));

		assertThat(vendorItemIdsCaptor.getValue(), nullValue());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersV2WhenHeaderVendorIdAndVendorAccountIdHeaderAndAccountIdParamProcessCorrectly() throws Exception {

		doReturn(getBrowsePriceMock(SIMPLE_V2_OFFERS_MOCK)).when(calculateBrowsePricesV2OrchestratorService)
				.execute(eq(MOCKED_VENDOR_ID), eq(MOCKED_VENDOR_ACCOUNT_ID), eq(MOCKED_COUNTRY_US), vendorItemIdsCaptor.capture());

		mockMvc.perform(get(PATH).header(VENDOR_ID_HEADER, MOCKED_VENDOR_ID).param(ACCOUNT_ID_PARAM, MOCKED_VENDOR_ACCOUNT_ID)
				.header(COUNTRY_HEADER, MOCKED_COUNTRY_US).header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)
				.header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION).header(VENDOR_ACCOUNT_ID_HEADER, MOCKED_VENDOR_ACCOUNT_ID))
				.andExpect(status().isOk()).andExpect(jsonPath("$", equalTo(getResponseDataFileContent(SIMPLE_V2_OFFERS_RESPONSE))));

		assertThat(vendorItemIdsCaptor.getValue(), nullValue());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersV2WhenHaveVendorIdAndVendorAccountIdAndVendorItemIdsProcessCorrectly() throws Exception {

		doReturn(getBrowsePriceMock(SIMPLE_V2_OFFERS_MOCK)).when(calculateBrowsePricesV2OrchestratorService)
				.execute(eq(MOCKED_VENDOR_ID), eq(MOCKED_VENDOR_ACCOUNT_ID), eq(MOCKED_COUNTRY_US), vendorItemIdsCaptor.capture());

		mockMvc.perform(get(PATH).header(VENDOR_ID_HEADER, MOCKED_VENDOR_ID).header(COUNTRY_HEADER, MOCKED_COUNTRY_US)
				.header(VENDOR_ACCOUNT_ID_HEADER, MOCKED_VENDOR_ACCOUNT_ID).param(VENDOR_ITEM_IDS_PARAM, MOCKED_VENDOR_ITEM_ID_1)
				.param(VENDOR_ITEM_IDS_PARAM, MOCKED_VENDOR_ITEM_ID_2).param(VENDOR_ITEM_IDS_PARAM, MOCKED_VENDOR_ITEM_ID_3)
				.header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION))
				.andExpect(status().isOk()).andExpect(jsonPath("$", equalTo(getResponseDataFileContent(SIMPLE_V2_OFFERS_RESPONSE))));

		final List<String> vendorItemIds = vendorItemIdsCaptor.getValue();
		assertThat(vendorItemIds, hasSize(3));
		assertThat(vendorItemIds, hasItems(MOCKED_VENDOR_ITEM_ID_1, MOCKED_VENDOR_ITEM_ID_2, MOCKED_VENDOR_ITEM_ID_3));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersV2WhenHaveVendorIdAndVendorAccountIdAndSkusProcessCorrectly() throws Exception {

		doReturn(getBrowsePriceMock(SIMPLE_V2_OFFERS_MOCK)).when(calculateBrowsePricesV2OrchestratorService)
				.execute(eq(MOCKED_VENDOR_ID), eq(MOCKED_VENDOR_ACCOUNT_ID), eq(MOCKED_COUNTRY_US), vendorItemIdsCaptor.capture());

		mockMvc.perform(get(PATH).header(VENDOR_ID_HEADER, MOCKED_VENDOR_ID).header(COUNTRY_HEADER, MOCKED_COUNTRY_US)
				.header(VENDOR_ACCOUNT_ID_HEADER, MOCKED_VENDOR_ACCOUNT_ID).param(SKUS_PARAM, MOCKED_VENDOR_ITEM_ID_1)
				.param(SKUS_PARAM, MOCKED_VENDOR_ITEM_ID_2).param(SKUS_PARAM, MOCKED_VENDOR_ITEM_ID_3)
				.header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION))
				.andExpect(status().isOk()).andExpect(jsonPath("$", equalTo(getResponseDataFileContent(SIMPLE_V2_OFFERS_RESPONSE))));

		final List<String> vendorItemIds = vendorItemIdsCaptor.getValue();
		assertThat(vendorItemIds, hasSize(3));
		assertThat(vendorItemIds, hasItems(MOCKED_VENDOR_ITEM_ID_1, MOCKED_VENDOR_ITEM_ID_2, MOCKED_VENDOR_ITEM_ID_3));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersV2ByVendorIdVendorAccountIdWhenVendorItemIdsCanNotBeFoundReturnsNotFound() throws Exception {

		doThrow(NotFoundException.customerPricesNotFound(MOCKED_VENDOR_ACCOUNT_ID, MOCKED_COUNTRY_US))
				.when(calculateBrowsePricesV2OrchestratorService)
				.execute(eq(MOCKED_VENDOR_ID), eq(MOCKED_VENDOR_ACCOUNT_ID), eq(MOCKED_COUNTRY_US), vendorItemIdsCaptor.capture());

		mockMvc.perform(get(PATH).header(VENDOR_ID_HEADER, MOCKED_VENDOR_ID).header(COUNTRY_HEADER, MOCKED_COUNTRY_US)
				.header(VENDOR_ACCOUNT_ID_HEADER, MOCKED_VENDOR_ACCOUNT_ID).param(VENDOR_ITEM_IDS_PARAM, MOCKED_VENDOR_ITEM_ID_1)
				.param(VENDOR_ITEM_IDS_PARAM, MOCKED_VENDOR_ITEM_ID_2).param(VENDOR_ITEM_IDS_PARAM, MOCKED_VENDOR_ITEM_ID_3)
				.header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("notFound-no-price-v2-found.json"))));

		final List<String> vendorItemIds = vendorItemIdsCaptor.getValue();
		assertThat(vendorItemIds, hasSize(3));
		assertThat(vendorItemIds, hasItems(MOCKED_VENDOR_ITEM_ID_1, MOCKED_VENDOR_ITEM_ID_2, MOCKED_VENDOR_ITEM_ID_3));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersV2WhenHasHeaderVendorIdAndDifferentVendorAccountIdHeaderAndAccountIdParamReturnsBadRequest() throws Exception {

		mockMvc.perform(get(PATH).header(VENDOR_ID_HEADER, MOCKED_VENDOR_ID).param(ACCOUNT_ID_PARAM, ACCOUNT_ID)
				.header(COUNTRY_HEADER, MOCKED_COUNTRY_US).header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)
				.header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION).header(VENDOR_ACCOUNT_ID_HEADER, MOCKED_VENDOR_ACCOUNT_ID))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("badRequest-accountId-and-vendorAccountId-different.json"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersV2WhenInvalidHeaderCountryReturnsBadRequest() throws Exception {

		mockMvc.perform(get(PATH).header(VENDOR_ID_HEADER, MOCKED_VENDOR_ID).header(COUNTRY_HEADER, "XXX")
				.header(VENDOR_ACCOUNT_ID_HEADER, MOCKED_VENDOR_ACCOUNT_ID).header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)
				.header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("badRequest-country-not-valid.json", "XXX"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersV2WhenHasNoHeaderVendorIdReturnsBadRequest() throws Exception {

		mockMvc.perform(get(PATH).header(COUNTRY_HEADER, MOCKED_COUNTRY_US).header(VENDOR_ACCOUNT_ID_HEADER, MOCKED_VENDOR_ACCOUNT_ID)
				.header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("badRequest-vendorId-not-present.json"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersV2WhenHasEmptyHeaderVendorIdReturnsBadRequest() throws Exception {

		mockMvc.perform(get(PATH).header(VENDOR_ID_HEADER, "").header(COUNTRY_HEADER, MOCKED_COUNTRY_US)
				.header(VENDOR_ACCOUNT_ID_HEADER, MOCKED_VENDOR_ACCOUNT_ID).header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)
				.header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("badRequest-vendorId-not-present.json"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersV2WhenHasNoHeaderVendorAccountIdReturnsBadRequest() throws Exception {

		mockMvc.perform(get(PATH).header(VENDOR_ID_HEADER, MOCKED_VENDOR_ID).header(COUNTRY_HEADER, MOCKED_COUNTRY_US)
				.header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("badRequest-accountId-and-vendorAccountId-not-present.json"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersV2WhenHasEmptyHeaderVendorAccountIdReturnsBadRequest() throws Exception {

		mockMvc.perform(get(PATH).header(VENDOR_ID_HEADER, MOCKED_VENDOR_ID).header(COUNTRY_HEADER, MOCKED_COUNTRY_US)
				.header(VENDOR_ACCOUNT_ID_HEADER, "").header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)
				.header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("badRequest-accountId-and-vendorAccountId-not-present.json"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersV2WhenHeaderVendorIdAndSkusParamReturnsBadRequest() throws Exception {

		mockMvc.perform(get(PATH).header(VENDOR_ID_HEADER, MOCKED_VENDOR_ID).param(SKUS_PARAM, "SKU_001").param(SKUS_PARAM, "SKU_002")
				.param(SKUS_PARAM, "SKU_003").header(COUNTRY_HEADER, MOCKED_COUNTRY_US).header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)
				.header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("badRequest-accountId-and-vendorAccountId-not-present.json"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersV2WhenVendorIdAndVendorAccountIdAndDifferentSkusAndVendorItemIdsReturnsBadRequest() throws Exception {

		mockMvc.perform(get(PATH).header(VENDOR_ID_HEADER, MOCKED_VENDOR_ID).header(COUNTRY_HEADER, MOCKED_COUNTRY_US)
				.header(VENDOR_ACCOUNT_ID_HEADER, MOCKED_VENDOR_ACCOUNT_ID).param(VENDOR_ITEM_IDS_PARAM, MOCKED_VENDOR_ITEM_ID_1)
				.param(VENDOR_ITEM_IDS_PARAM, MOCKED_VENDOR_ITEM_ID_2).param(VENDOR_ITEM_IDS_PARAM, MOCKED_VENDOR_ITEM_ID_3)
				.param(SKUS_PARAM, "SKU_001").param(SKUS_PARAM, "SKU_002").param(SKUS_PARAM, "SKU_003")
				.header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("badRequest-skus-and-vendorItemIds-different.json"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersByParamAccountWhenProcessCorrectly() throws Exception {

		doReturn(getBrowsePriceMock(SIMPLE_OFFERS_MOCK)).when(calculateBrowsePricesV1OrchestratorService)
				.execute(eq(ACCOUNT_ID), eq(MOCKED_COUNTRY_ZA), skusCaptor.capture());

		mockMvc.perform(get(PATH).param(ACCOUNT_ID_PARAM, ACCOUNT_ID).header(COUNTRY_HEADER, MOCKED_COUNTRY_ZA)
				.header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER, MOCKED_JWT_B2B_ZA_COUNTRY))
				.andExpect(status().isOk()).andExpect(jsonPath("$", equalTo(getResponseDataFileContent(SIMPLE_OFFERS_RESPONSE))));

		assertThat(skusCaptor.getValue(), nullValue());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersByHeaderVendorAccountIdWhenProcessCorrectly() throws Exception {

		doReturn(getBrowsePriceMock(SIMPLE_OFFERS_MOCK)).when(calculateBrowsePricesV1OrchestratorService)
				.execute(eq(ACCOUNT_ID), eq(MOCKED_COUNTRY_ZA), skusCaptor.capture());

		mockMvc.perform(get(PATH).header(VENDOR_ACCOUNT_ID_HEADER, ACCOUNT_ID).header(COUNTRY_HEADER, MOCKED_COUNTRY_ZA)
				.header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER, MOCKED_JWT_B2B_ZA_COUNTRY))
				.andExpect(status().isOk()).andExpect(jsonPath("$", equalTo(getResponseDataFileContent(SIMPLE_OFFERS_RESPONSE))));

		assertThat(skusCaptor.getValue(), nullValue());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersByParamAccountAndHeaderVendorAccountIdWhenProcessCorrectly() throws Exception {

		doReturn(getBrowsePriceMock(SIMPLE_OFFERS_MOCK)).when(calculateBrowsePricesV1OrchestratorService)
				.execute(eq(ACCOUNT_ID), eq(MOCKED_COUNTRY_ZA), skusCaptor.capture());

		mockMvc.perform(get(PATH).param(ACCOUNT_ID_PARAM, ACCOUNT_ID).header(VENDOR_ACCOUNT_ID_HEADER, ACCOUNT_ID)
				.header(COUNTRY_HEADER, MOCKED_COUNTRY_ZA).header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)
				.header(AUTHORIZATION_HEADER, MOCKED_JWT_B2B_ZA_COUNTRY)).andExpect(status().isOk())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent(SIMPLE_OFFERS_RESPONSE))));

		assertThat(skusCaptor.getValue(), nullValue());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersByAccountIdAndSkusWhenProcessCorrectly() throws Exception {

		doReturn(getBrowsePriceMock(SIMPLE_OFFERS_MOCK)).when(calculateBrowsePricesV1OrchestratorService)
				.execute(eq(ACCOUNT_ID), eq(MOCKED_COUNTRY_ZA), skusCaptor.capture());

		mockMvc.perform(get(PATH).param(ACCOUNT_ID_PARAM, ACCOUNT_ID).param(SKUS_PARAM, "SKU_001").param(SKUS_PARAM, "SKU_002")
				.param(SKUS_PARAM, "SKU_003").header(COUNTRY_HEADER, MOCKED_COUNTRY_ZA).header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)
				.header(AUTHORIZATION_HEADER, MOCKED_JWT_B2B_ZA_COUNTRY)).andExpect(status().isOk())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent(SIMPLE_OFFERS_RESPONSE))));

		final List<String> skus = skusCaptor.getValue();
		assertThat(skus, hasSize(3));
		assertThat(skus, hasItems("SKU_001", "SKU_002", "SKU_003"));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersByAccountIdAndVendorItemIdsWhenProcessCorrectly() throws Exception {

		doReturn(getBrowsePriceMock(SIMPLE_OFFERS_MOCK)).when(calculateBrowsePricesV1OrchestratorService)
				.execute(eq(ACCOUNT_ID), eq(MOCKED_COUNTRY_ZA), skusCaptor.capture());

		mockMvc.perform(get(PATH).param(ACCOUNT_ID_PARAM, ACCOUNT_ID).param(VENDOR_ITEM_IDS_PARAM, "SKU_001")
				.param(VENDOR_ITEM_IDS_PARAM, "SKU_002").param(VENDOR_ITEM_IDS_PARAM, "SKU_003").header(COUNTRY_HEADER, MOCKED_COUNTRY_ZA)
				.header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER, MOCKED_JWT_B2B_ZA_COUNTRY))
				.andExpect(status().isOk()).andExpect(jsonPath("$", equalTo(getResponseDataFileContent(SIMPLE_OFFERS_RESPONSE))));

		final List<String> skus = skusCaptor.getValue();
		assertThat(skus, hasSize(3));
		assertThat(skus, hasItems("SKU_001", "SKU_002", "SKU_003"));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersByAccountIdAndSkusWhenSkusCanNotBeFoundReturnsNotFound() throws Exception {

		doThrow(NotFoundException.customerPricesNotFound(ACCOUNT_ID, MOCKED_COUNTRY_ZA)).when(calculateBrowsePricesV1OrchestratorService)
				.execute(eq(ACCOUNT_ID), eq(MOCKED_COUNTRY_ZA), skusCaptor.capture());

		mockMvc.perform(get(PATH).param(ACCOUNT_ID_PARAM, ACCOUNT_ID).param(SKUS_PARAM, "SKU_001").param(SKUS_PARAM, "SKU_002")
				.param(SKUS_PARAM, "SKU_003").header(COUNTRY_HEADER, MOCKED_COUNTRY_ZA).header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)
				.header(AUTHORIZATION_HEADER, MOCKED_JWT_B2B_ZA_COUNTRY)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("notFound-no-price-found.json"))));

		final List<String> skus = skusCaptor.getValue();
		assertThat(skus, hasSize(3));
		assertThat(skus, hasItems("SKU_001", "SKU_002", "SKU_003"));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersWhenHaveDifferentParamAccountAndHeaderVendorAccountIdReturnsBadRequest() throws Exception {

		mockMvc.perform(get(PATH).param(ACCOUNT_ID_PARAM, ACCOUNT_ID).header(VENDOR_ACCOUNT_ID_HEADER, MOCKED_VENDOR_ACCOUNT_ID)
				.header(COUNTRY_HEADER, MOCKED_COUNTRY_ZA).header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)
				.header(AUTHORIZATION_HEADER, MOCKED_JWT_B2B_ZA_COUNTRY)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("badRequest-accountId-and-vendorAccountId-different.json"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersWhenHaveAccountIdAndDifferentSkusAndVendorItemIdsReturnsBadRequest() throws Exception {

		mockMvc.perform(get(PATH).param(ACCOUNT_ID_PARAM, ACCOUNT_ID).header(COUNTRY_HEADER, MOCKED_COUNTRY_ZA)
				.param(VENDOR_ITEM_IDS_PARAM, MOCKED_VENDOR_ITEM_ID_1).param(VENDOR_ITEM_IDS_PARAM, MOCKED_VENDOR_ITEM_ID_2)
				.param(VENDOR_ITEM_IDS_PARAM, MOCKED_VENDOR_ITEM_ID_3).param(SKUS_PARAM, "SKU_001").param(SKUS_PARAM, "SKU_002")
				.param(SKUS_PARAM, "SKU_003").header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)
				.header(AUTHORIZATION_HEADER, MOCKED_JWT_B2B_ZA_COUNTRY)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("badRequest-skus-and-vendorItemIds-different.json"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersWhenHaveNoParamAccountIdReturnsBadRequest() throws Exception {

		mockMvc.perform(get(PATH).header(COUNTRY_HEADER, MOCKED_COUNTRY_ZA).header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)
				.header(AUTHORIZATION_HEADER, MOCKED_JWT_B2B_ZA_COUNTRY)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("badRequest-accountId-and-vendorAccountId-not-present.json"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersWhenHaveEmptyAccountIdReturnsBadRequest() throws Exception {

		mockMvc.perform(get(PATH).param(ACCOUNT_ID_PARAM, "").header(COUNTRY_HEADER, MOCKED_COUNTRY_ZA)
				.header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER, MOCKED_JWT_B2B_ZA_COUNTRY))
				.andExpect(status().isBadRequest()).andExpect(
				jsonPath("$", equalTo(getResponseDataFileContent("badRequest-accountId-and-vendorAccountId-not-present.json", ""))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersWhenHaveNoHeaderCountryReturnsBadRequest() throws Exception {

		mockMvc.perform(get(PATH).param(ACCOUNT_ID_PARAM, ACCOUNT_ID).header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)
				.header(AUTHORIZATION_HEADER, MOCKED_JWT_B2B_ZA_COUNTRY)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("badRequest-country-missing.json"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersWhenHasEmptyHeaderCountryReturnsBadRequest() throws Exception {

		mockMvc.perform(
				get(PATH).param(ACCOUNT_ID_PARAM, ACCOUNT_ID).header(COUNTRY_HEADER, "").header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)
						.header(AUTHORIZATION_HEADER, MOCKED_JWT_B2B_ZA_COUNTRY)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("badRequest-country-missing.json"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersWhenHasInvalidHeaderCountryReturnsBadRequest() throws Exception {

		mockMvc.perform(get(PATH).param(ACCOUNT_ID_PARAM, ACCOUNT_ID).header(COUNTRY_HEADER, "XXX")
				.header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER, MOCKED_JWT_B2B_ZA_COUNTRY))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("badRequest-country-not-valid.json", "XXX"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersWhenHasInvalidHeaderV2CountryReturnsBadRequest() throws Exception {

		mockMvc.perform(get(PATH).param(ACCOUNT_ID_PARAM, ACCOUNT_ID).header(COUNTRY_HEADER, MOCKED_COUNTRY_US)
				.header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER, MOCKED_JWT_B2B_ZA_COUNTRY))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("badRequest-vendorId-not-present.json"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersWhenHasNoHeaderRequestTraceIdReturnsBadRequest() throws Exception {

		mockMvc.perform(get(PATH).param(ACCOUNT_ID_PARAM, ACCOUNT_ID).header(COUNTRY_HEADER, MOCKED_COUNTRY_ZA)
				.header(AUTHORIZATION_HEADER, MOCKED_JWT_B2B_ZA_COUNTRY)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("badRequest-requestTraceId-missing.json"))))
				.andExpect(header().doesNotExist(REQUEST_TRACE_ID_HEADER));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersWhenHaveEmptyRequestTraceIdReturnsBadRequest() throws Exception {

		mockMvc.perform(
				get(PATH).param(ACCOUNT_ID_PARAM, ACCOUNT_ID).header(COUNTRY_HEADER, MOCKED_COUNTRY_ZA).header(REQUEST_TRACE_ID_HEADER, "")
						.header(AUTHORIZATION_HEADER, MOCKED_JWT_B2B_ZA_COUNTRY)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("badRequest-requestTraceId-missing.json"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus200WhenUsingValidB2bToken() throws Exception {

		mockMvc.perform(
				get(PATH).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR).header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID)
						.header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_B2B).param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID))
				.andExpect(status().isOk());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus200WhenB2bTokenDoesNotHaveApp() throws Exception {

		mockMvc.perform(
				get(PATH).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR).header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID)
						.header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_WITHOUT_APP_B2B).param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID))
				.andExpect(status().isOk());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus403WhenB2bTokenDoesNotHaveCountry() throws Exception {

		mockMvc.perform(
				get(PATH).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR).header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID)
						.header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_WITHOUT_COUNTRY_B2B).param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID))
				.andExpect(status().isForbidden());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus403WhenB2bTokenDoesNotHaveExtensionsAccountIds() throws Exception {

		mockMvc.perform(
				get(PATH).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR).header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID)
						.header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_WITHOUT_EXTENSION_ACCOUNTS_B2B)
						.param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID)).andExpect(status().isForbidden());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus403WhenB2bTokenHasInvalidApp() throws Exception {

		mockMvc.perform(
				get(PATH).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR).header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID)
						.header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_INVALID_APP_B2B).param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID))
				.andExpect(status().isForbidden());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus403WhenAccountFromB2bTokenDoesNotMatchAccountFromRequest() throws Exception {

		mockMvc.perform(
				get(PATH).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR).header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID)
						.header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_B2B).param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID1))
				.andExpect(status().isForbidden());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus403WhenCountryFromB2bTokenDoesNotMatchCountryFromRequest() throws Exception {

		mockMvc.perform(get(PATH).header(COUNTRY_HEADER_NAME, TestConstants.MOCKED_COUNTRY_ZA)
				.header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_B2B)
				.param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID)).andExpect(status().isForbidden());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus200WhenUsingValidM2mToken() throws Exception {

		mockMvc.perform(
				get(PATH).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR).header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID)
						.header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_M2M).param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID))
				.andExpect(status().isOk());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus403WhenUsingInvalidM2mWithOnlyReadRoleToken() throws Exception {

		mockMvc.perform(
				get(PATH).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR).header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID)
						.header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_JUST_READ_M2M).param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID))
				.andExpect(status().isForbidden());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus403WhenUsingInvalidM2mWithOnlyWriteRoleToken() throws Exception {

		mockMvc.perform(
				get(PATH).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR).header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID)
						.header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_JUST_WRITE_M2M).param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID))
				.andExpect(status().isForbidden());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus200WhenUsingValidHmacToken() throws Exception {

		mockMvc.perform(
				get(PATH).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR).header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID)
						.header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_HMAC).param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID1))
				.andExpect(status().isOk());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus403WhenUsingHmacTokenWithInvalidAccount() throws Exception {

		mockMvc.perform(
				get(PATH).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR).header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID)
						.header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_HMAC).param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID))
				.andExpect(status().isForbidden());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus403WhenUsingHmacTokenWithoutCustomerRole() throws Exception {

		mockMvc.perform(
				get(PATH).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR).header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID)
						.header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_WITHOUT_CUSTOMER_ROLE_HMCA)
						.param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID1)).andExpect(status().isForbidden());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturn200WhenRequestGetOffersWithoutHeaderAuthorization() throws Exception {

		mockMvc.perform(get(PATH).param(ACCOUNT_ID_PARAM, ACCOUNT_ID).header(COUNTRY_HEADER, MOCKED_COUNTRY_ZA)
				.header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)).andExpect(status().isOk());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturn200WhenRequestGetOffersWithEmptyHeaderAuthorization() throws Exception {

		mockMvc.perform(get(PATH).param(ACCOUNT_ID_PARAM, ACCOUNT_ID).header(COUNTRY_HEADER, MOCKED_COUNTRY_ZA)
				.header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER, "")).andExpect(status().isOk());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetOffersWhenInvalidHeaderAuthorizationReturnsBadRequest() throws Exception {

		mockMvc.perform(get(PATH).param(ACCOUNT_ID_PARAM, ACCOUNT_ID).header(COUNTRY_HEADER, MOCKED_COUNTRY_ZA)
				.header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER, "INVALID"))
				.andExpect(status().isForbidden()).andExpect(content().string(emptyString()));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus403WhenUsingB2cTokenWithNonSupportedApp() throws Exception {

		mockMvc.perform(
				get(PATH).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR).header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID)
						.header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_WITH_UNSUPPORTED_APP)
						.param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID1)).andExpect(status().isForbidden());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus200WhenUsingB2cTokenWithSupportedApp() throws Exception {

		mockMvc.perform(
				get(PATH).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR).header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID)
						.header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_WITH_SUPPORTED_APP_ONCUSTOMER)
						.param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID1)).andExpect(status().isOk());
	}

	@Test
	void shouldReturnHttpStatus503WhenMongoIsNotAvailable() throws Exception {

		doThrow(DataAccessResourceFailureException.class).when(calculateBrowsePricesV1OrchestratorService)
				.execute(eq(ACCOUNT_ID), eq(MOCKED_COUNTRY_ZA), skusCaptor.capture());

		mockMvc.perform(get(PATH).param(ACCOUNT_ID_PARAM, ACCOUNT_ID).header(COUNTRY_HEADER, MOCKED_COUNTRY_ZA)
				.header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)).andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent(DATABASE_ACCESS_ERROR_RESPONSE))));
	}
}