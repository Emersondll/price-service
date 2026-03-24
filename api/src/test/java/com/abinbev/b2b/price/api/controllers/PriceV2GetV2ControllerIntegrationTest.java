package com.abinbev.b2b.price.api.controllers;

import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_JWT_M2M;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.text.IsEmptyString.emptyString;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.abinbev.b2b.price.api.domain.v2.PriceResultListV2;
import com.abinbev.b2b.price.api.rest.vo.v2.PaginationResponseVoV2;
import com.abinbev.b2b.price.api.services.v2.PriceServiceV2;
import com.abinbev.b2b.price.api.testhelpers.IntegrationTestsResourceHelper;
import com.fasterxml.jackson.core.type.TypeReference;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-integration-tests.yml")
class PriceV2GetV2ControllerIntegrationTest {

	private static final String GET_V2_RESOURCE_PATH = "get_v2";

	private static final String AUTHORIZATION_HEADER = "authorization";
	private static final String REQUEST_TRACE_ID_HEADER = "requestTraceId";
	private static final String COUNTRY_HEADER = "country";
	private static final String VENDOR_ID_HEADER = "vendorId";
	private static final String VENDOR_ACCOUNT_ID_HEADER = "vendorAccountId";
	private static final String VENDOR_ITEM_IDS_PARAM = "vendorItemIds";
	private static final String PRICE_LIST_ID_HEADER = "priceListId";
	private static final String PAGE_PARAM = "page";
	private static final String SIZE_PARAM = "size";

	private static final String DEFAULT_AUTHORIZATION = MOCKED_JWT_M2M;
	private static final String NO_PRICE_UP_FRONT_COUNTRY = "US";
	private static final String UNSUPPORTED_COUNTRY = "CO";
	private static final String DEFAULT_REQUEST_TRACE_ID = "0a2f9f71-f96d-47c9-9ce1-c7af73a449d5";
	private static final String DEFAULT_VENDOR_ID = "VENDOR_ID-1";
	private static final String DEFAULT_VENDOR_ACCOUNT_ID = "VENDOR_ACCOUNT_ID-1";
	private static final String DEFAULT_VENDOR_ITEM_ID = "VENDOR_ITEM_ID-1";
	private static final String DEFAULT_PAGE = "0";
	private static final String DEFAULT_SIZE = "10";
	private static final String DEFAULT_PRICE_LIST_ID = "abc";


	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private PriceServiceV2 priceServiceV2;

	private Object getResponseDataFileContent(final String fileName) throws IOException {

		return IntegrationTestsResourceHelper.getResponseDataFileContent(GET_V2_RESOURCE_PATH, fileName);
	}

	private Object getResponseDataFileContent(final String fileName, final Object... args) throws IOException {

		return IntegrationTestsResourceHelper.getResponseDataFileContent(GET_V2_RESOURCE_PATH, fileName, args);
	}

	private PriceResultListV2 getPriceResultListV2Mock(final String fileName) throws IOException {

		return IntegrationTestsResourceHelper.getMockDataFileContent(GET_V2_RESOURCE_PATH, fileName, new TypeReference<>() {
		});
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetPricesWhenHasNoOptionalFieldsThenProcessCorrectly() throws Exception {

		final PriceResultListV2 priceResultListV2 = getPriceResultListV2Mock("correct-priceResultListV2-without-optional-fields.json");
		when(priceServiceV2.getAllPrices(DEFAULT_VENDOR_ID, DEFAULT_VENDOR_ACCOUNT_ID, NO_PRICE_UP_FRONT_COUNTRY, null, null, null))
				.thenReturn(priceResultListV2);

		mockMvc.perform(
				get("/v2").header(COUNTRY_HEADER, NO_PRICE_UP_FRONT_COUNTRY).header(REQUEST_TRACE_ID_HEADER, DEFAULT_REQUEST_TRACE_ID)
						.header(VENDOR_ID_HEADER, DEFAULT_VENDOR_ID).header(VENDOR_ACCOUNT_ID_HEADER, DEFAULT_VENDOR_ACCOUNT_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("correct-priceResponseV0V2-without-optional-fields.json"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetPricesWhenHasVendorItemIdParamThenProcessCorrectly() throws Exception {

		final PriceResultListV2 priceResultListV2 = getPriceResultListV2Mock("correct-priceResultListV2-with-optional-fields.json");
		when(priceServiceV2.getAllPrices(DEFAULT_VENDOR_ID, DEFAULT_VENDOR_ACCOUNT_ID, NO_PRICE_UP_FRONT_COUNTRY,
				Collections.singletonList(DEFAULT_VENDOR_ITEM_ID), null, null)).thenReturn(priceResultListV2);

		mockMvc.perform(get("/v2").param(VENDOR_ITEM_IDS_PARAM, DEFAULT_VENDOR_ITEM_ID).header(COUNTRY_HEADER, NO_PRICE_UP_FRONT_COUNTRY)
				.header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION).header(REQUEST_TRACE_ID_HEADER, DEFAULT_REQUEST_TRACE_ID)
				.header(VENDOR_ID_HEADER, DEFAULT_VENDOR_ID).header(VENDOR_ACCOUNT_ID_HEADER, DEFAULT_VENDOR_ACCOUNT_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("correct-priceResponseV0V2-with-optional-fields.json"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetPricesWhenPageParamThenProcessCorrectly() throws Exception {

		final ArgumentCaptor<PaginationResponseVoV2> paginationResponseVoV2Captor = ArgumentCaptor.forClass(PaginationResponseVoV2.class);

		final PriceResultListV2 priceResultListV2 = getPriceResultListV2Mock("correct-priceResultListV2-with-optional-fields.json");
		when(priceServiceV2
				.getAllPrices(eq(DEFAULT_VENDOR_ID), eq(DEFAULT_VENDOR_ACCOUNT_ID), eq(NO_PRICE_UP_FRONT_COUNTRY), isNull(), isNull(),
						any()))
				.thenReturn(priceResultListV2);

		mockMvc.perform(get("/v2").param(PAGE_PARAM, DEFAULT_PAGE).header(COUNTRY_HEADER, NO_PRICE_UP_FRONT_COUNTRY)
				.header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION).header(REQUEST_TRACE_ID_HEADER, DEFAULT_REQUEST_TRACE_ID)
				.header(VENDOR_ID_HEADER, DEFAULT_VENDOR_ID).header(VENDOR_ACCOUNT_ID_HEADER, DEFAULT_VENDOR_ACCOUNT_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("correct-priceResponseV0V2-with-optional-fields.json"))));

		verify(priceServiceV2).getAllPrices(eq(DEFAULT_VENDOR_ID), eq(DEFAULT_VENDOR_ACCOUNT_ID), eq(NO_PRICE_UP_FRONT_COUNTRY), isNull(),
				isNull(),
				paginationResponseVoV2Captor.capture());
		final PaginationResponseVoV2 paginationResponseVoV2 = paginationResponseVoV2Captor.getValue();
		assertThat(paginationResponseVoV2.getPage(), equalTo(Integer.valueOf(DEFAULT_PAGE)));
		assertThat(paginationResponseVoV2.getSize(), equalTo(50));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetPricesWhenSizeParamThenProcessCorrectly() throws Exception {

		final ArgumentCaptor<PaginationResponseVoV2> paginationResponseVoV2Captor = ArgumentCaptor.forClass(PaginationResponseVoV2.class);

		final PriceResultListV2 priceResultListV2 = getPriceResultListV2Mock("correct-priceResultListV2-with-optional-fields.json");
		when(priceServiceV2
				.getAllPrices(eq(DEFAULT_VENDOR_ID), eq(DEFAULT_VENDOR_ACCOUNT_ID), eq(NO_PRICE_UP_FRONT_COUNTRY), isNull(), isNull(),
						any()))
				.thenReturn(priceResultListV2);

		mockMvc.perform(get("/v2").param(SIZE_PARAM, DEFAULT_SIZE).header(COUNTRY_HEADER, NO_PRICE_UP_FRONT_COUNTRY)
				.header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION).header(REQUEST_TRACE_ID_HEADER, DEFAULT_REQUEST_TRACE_ID)
				.header(VENDOR_ID_HEADER, DEFAULT_VENDOR_ID).header(VENDOR_ACCOUNT_ID_HEADER, DEFAULT_VENDOR_ACCOUNT_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("correct-priceResponseV0V2-with-optional-fields.json"))));

		verify(priceServiceV2).getAllPrices(eq(DEFAULT_VENDOR_ID), eq(DEFAULT_VENDOR_ACCOUNT_ID), eq(NO_PRICE_UP_FRONT_COUNTRY), isNull(),
				isNull(),
				paginationResponseVoV2Captor.capture());
		final PaginationResponseVoV2 paginationResponseVoV2 = paginationResponseVoV2Captor.getValue();
		assertThat(paginationResponseVoV2.getPage(), equalTo(0));
		assertThat(paginationResponseVoV2.getSize(), equalTo(Integer.valueOf(DEFAULT_SIZE)));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetPricesWhenAllOptionalFieldsThenProcessCorrectly() throws Exception {

		final ArgumentCaptor<PaginationResponseVoV2> paginationResponseVoV2Captor = ArgumentCaptor.forClass(PaginationResponseVoV2.class);

		final PriceResultListV2 priceResultListV2 = getPriceResultListV2Mock("correct-priceResultListV2-with-optional-fields.json");
		when(priceServiceV2.getAllPrices(eq(DEFAULT_VENDOR_ID), eq(DEFAULT_VENDOR_ACCOUNT_ID), eq(NO_PRICE_UP_FRONT_COUNTRY),
				eq(Collections.singletonList(DEFAULT_VENDOR_ITEM_ID)), isNull(), any())).thenReturn(priceResultListV2);

		mockMvc.perform(get("/v2").param(VENDOR_ITEM_IDS_PARAM, DEFAULT_VENDOR_ITEM_ID).param(PAGE_PARAM, DEFAULT_PAGE)
				.param(SIZE_PARAM, DEFAULT_SIZE).header(COUNTRY_HEADER, NO_PRICE_UP_FRONT_COUNTRY)
				.header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION).header(REQUEST_TRACE_ID_HEADER, DEFAULT_REQUEST_TRACE_ID)
				.header(VENDOR_ID_HEADER, DEFAULT_VENDOR_ID).header(VENDOR_ACCOUNT_ID_HEADER, DEFAULT_VENDOR_ACCOUNT_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("correct-priceResponseV0V2-with-optional-fields.json"))));

		verify(priceServiceV2).getAllPrices(eq(DEFAULT_VENDOR_ID), eq(DEFAULT_VENDOR_ACCOUNT_ID), eq(NO_PRICE_UP_FRONT_COUNTRY),
				eq(Collections.singletonList(DEFAULT_VENDOR_ITEM_ID)), isNull(), paginationResponseVoV2Captor.capture());
		final PaginationResponseVoV2 paginationResponseVoV2 = paginationResponseVoV2Captor.getValue();
		assertThat(paginationResponseVoV2.getPage(), equalTo(Integer.valueOf(DEFAULT_PAGE)));
		assertThat(paginationResponseVoV2.getSize(), equalTo(Integer.valueOf(DEFAULT_SIZE)));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetPricesWhenEmptyCountryHeaderThenValidateWithBadRequest() throws Exception {

		mockMvc.perform(get("/v2").contentType(MediaType.APPLICATION_JSON).header(COUNTRY_HEADER, "")
				.header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION).header(REQUEST_TRACE_ID_HEADER, DEFAULT_REQUEST_TRACE_ID)
				.header(VENDOR_ID_HEADER, DEFAULT_VENDOR_ID).header(VENDOR_ACCOUNT_ID_HEADER, DEFAULT_VENDOR_ACCOUNT_ID))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("default-validation-missing-header.json", "country"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetPricesWhenBlankCountryHeaderThenValidateWithBadRequest() throws Exception {

		mockMvc.perform(get("/v2").contentType(MediaType.APPLICATION_JSON).header(COUNTRY_HEADER, "   ")
				.header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION).header(REQUEST_TRACE_ID_HEADER, DEFAULT_REQUEST_TRACE_ID)
				.header(VENDOR_ID_HEADER, DEFAULT_VENDOR_ID).header(VENDOR_ACCOUNT_ID_HEADER, DEFAULT_VENDOR_ACCOUNT_ID))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("default-validation-missing-header.json", "country", "   "))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetPricesWhenInvalidCountryHeaderThenValidateWithBadRequest() throws Exception {

		mockMvc.perform(get("/v2").contentType(MediaType.APPLICATION_JSON).header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION)
				.header(COUNTRY_HEADER, "XYZ!@#").header(REQUEST_TRACE_ID_HEADER, DEFAULT_REQUEST_TRACE_ID)
				.header(VENDOR_ID_HEADER, DEFAULT_VENDOR_ID).header(VENDOR_ACCOUNT_ID_HEADER, DEFAULT_VENDOR_ACCOUNT_ID))
				.andExpect(status().isBadRequest()).andExpect(
				jsonPath("$", equalTo(getResponseDataFileContent("default-validation-not-valid-header.json", "country", "XYZ!@#"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetPricesWhenUnsupportedCountryHeaderThenValidateWithBadRequest() throws Exception {

		mockMvc.perform(get("/v2").contentType(MediaType.APPLICATION_JSON).header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION)
				.header(COUNTRY_HEADER, UNSUPPORTED_COUNTRY).header(REQUEST_TRACE_ID_HEADER, DEFAULT_REQUEST_TRACE_ID)
				.header(VENDOR_ID_HEADER, DEFAULT_VENDOR_ID).header(VENDOR_ACCOUNT_ID_HEADER, DEFAULT_VENDOR_ACCOUNT_ID))
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("default-validation-not-valid-header.json", "country", "CO"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetPricesWhenMissingCountryHeaderThenValidateBadRequest() throws Exception {

		mockMvc.perform(get("/v2").contentType(MediaType.APPLICATION_JSON).header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION)
				.header(REQUEST_TRACE_ID_HEADER, DEFAULT_REQUEST_TRACE_ID).header(VENDOR_ID_HEADER, DEFAULT_VENDOR_ID)
				.header(VENDOR_ACCOUNT_ID_HEADER, DEFAULT_VENDOR_ACCOUNT_ID)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("default-validation-missing-header.json", "country"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetPricesWhenStrangeAuthorizationHeaderThenProcessCorrectly() throws Exception {

		mockMvc.perform(get("/v2").contentType(MediaType.APPLICATION_JSON).header(AUTHORIZATION_HEADER, "STRANGE_VALUE-!@#456")
				.header(COUNTRY_HEADER, NO_PRICE_UP_FRONT_COUNTRY).header(VENDOR_ID_HEADER, DEFAULT_VENDOR_ID)
				.header(VENDOR_ACCOUNT_ID_HEADER, DEFAULT_VENDOR_ACCOUNT_ID).header(REQUEST_TRACE_ID_HEADER, DEFAULT_REQUEST_TRACE_ID))
				.andExpect(status().isForbidden()).andExpect(content().string(emptyString()));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetPricesWhenEmptyRequestTraceIdHeaderThenValidateBadRequest() throws Exception {

		mockMvc.perform(get("/v2").contentType(MediaType.APPLICATION_JSON).header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION)
				.header(COUNTRY_HEADER, NO_PRICE_UP_FRONT_COUNTRY).header(VENDOR_ID_HEADER, DEFAULT_VENDOR_ID)
				.header(VENDOR_ACCOUNT_ID_HEADER, DEFAULT_VENDOR_ACCOUNT_ID).header(REQUEST_TRACE_ID_HEADER, ""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("default-validation-missing-header.json", "requestTraceId"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetPricesWhenBlankRequestTraceIdHeaderThenProcessCorrectly() throws Exception {

		mockMvc.perform(get("/v2").contentType(MediaType.APPLICATION_JSON).header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION)
				.header(COUNTRY_HEADER, NO_PRICE_UP_FRONT_COUNTRY).header(VENDOR_ID_HEADER, DEFAULT_VENDOR_ID)
				.header(VENDOR_ACCOUNT_ID_HEADER, DEFAULT_VENDOR_ACCOUNT_ID).header(REQUEST_TRACE_ID_HEADER, "   "))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("default-validation-missing-header.json", "requestTraceId"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetPricesWhenStrangeRequestTraceIdHeaderThenProcessCorrectly() throws Exception {

		final PriceResultListV2 priceResultListV2 = getPriceResultListV2Mock("correct-priceResultListV2-without-optional-fields.json");
		when(priceServiceV2.getAllPrices(DEFAULT_VENDOR_ID, DEFAULT_VENDOR_ACCOUNT_ID, NO_PRICE_UP_FRONT_COUNTRY, null, null, null))
				.thenReturn(priceResultListV2);

		mockMvc.perform(get("/v2").contentType(MediaType.APPLICATION_JSON).header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION)
				.header(COUNTRY_HEADER, NO_PRICE_UP_FRONT_COUNTRY).header(VENDOR_ID_HEADER, DEFAULT_VENDOR_ID)
				.header(VENDOR_ACCOUNT_ID_HEADER, DEFAULT_VENDOR_ACCOUNT_ID).header(REQUEST_TRACE_ID_HEADER, "STRANGE_VALUE-!@#456"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("correct-priceResponseV0V2-without-optional-fields.json"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetPricesWhenMissingRequestTraceIdHeaderThenValidateBadRequest() throws Exception {

		mockMvc.perform(get("/v2").contentType(MediaType.APPLICATION_JSON).header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION)
				.header(COUNTRY_HEADER, NO_PRICE_UP_FRONT_COUNTRY).header(VENDOR_ID_HEADER, DEFAULT_VENDOR_ID)
				.header(VENDOR_ACCOUNT_ID_HEADER, DEFAULT_VENDOR_ACCOUNT_ID)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("default-validation-missing-header.json", "requestTraceId"))))
				.andExpect(header().doesNotExist(REQUEST_TRACE_ID_HEADER));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetPricesWhenEmptyVendorIdHeaderThenValidateBadRequest() throws Exception {

		mockMvc.perform(get("/v2").contentType(MediaType.APPLICATION_JSON).header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION)
				.header(COUNTRY_HEADER, NO_PRICE_UP_FRONT_COUNTRY).header(VENDOR_ID_HEADER, "")
				.header(VENDOR_ACCOUNT_ID_HEADER, DEFAULT_VENDOR_ACCOUNT_ID).header(REQUEST_TRACE_ID_HEADER, DEFAULT_REQUEST_TRACE_ID))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("default-validation-missing-header.json", "vendorId"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetPricesWhenBlankVendorIdHeaderThenProcessCorrectly() throws Exception {

		mockMvc.perform(get("/v2").contentType(MediaType.APPLICATION_JSON).header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION)
				.header(COUNTRY_HEADER, NO_PRICE_UP_FRONT_COUNTRY).header(VENDOR_ID_HEADER, "   ")
				.header(VENDOR_ACCOUNT_ID_HEADER, DEFAULT_VENDOR_ACCOUNT_ID).header(REQUEST_TRACE_ID_HEADER, DEFAULT_REQUEST_TRACE_ID))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("default-validation-missing-header.json", "vendorId"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetPricesWhenStrangeVendorIdHeaderThenProcessCorrectly() throws Exception {

		final PriceResultListV2 priceResultListV2 = getPriceResultListV2Mock("correct-priceResultListV2-without-optional-fields.json");
		when(priceServiceV2.getAllPrices("STRANGE_VALUE-!@#456", DEFAULT_VENDOR_ACCOUNT_ID, NO_PRICE_UP_FRONT_COUNTRY, null, null, null))
				.thenReturn(priceResultListV2);

		mockMvc.perform(get("/v2").contentType(MediaType.APPLICATION_JSON).header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION)
				.header(COUNTRY_HEADER, NO_PRICE_UP_FRONT_COUNTRY).header(VENDOR_ID_HEADER, "STRANGE_VALUE-!@#456")
				.header(VENDOR_ACCOUNT_ID_HEADER, DEFAULT_VENDOR_ACCOUNT_ID).header(REQUEST_TRACE_ID_HEADER, DEFAULT_REQUEST_TRACE_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("correct-priceResponseV0V2-without-optional-fields.json"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetPricesWhenMissingVendorIdHeaderThenValidateBadRequest() throws Exception {

		mockMvc.perform(get("/v2").contentType(MediaType.APPLICATION_JSON).header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION)
				.header(COUNTRY_HEADER, NO_PRICE_UP_FRONT_COUNTRY).header(VENDOR_ACCOUNT_ID_HEADER, DEFAULT_VENDOR_ACCOUNT_ID)
				.header(REQUEST_TRACE_ID_HEADER, DEFAULT_REQUEST_TRACE_ID)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("default-validation-missing-header.json", "vendorId"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetPricesWhenEmptyVendorAccountIdHeaderThenValidateBadRequest() throws Exception {

		mockMvc.perform(get("/v2").contentType(MediaType.APPLICATION_JSON).header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION)
				.header(COUNTRY_HEADER, NO_PRICE_UP_FRONT_COUNTRY).header(VENDOR_ID_HEADER, DEFAULT_VENDOR_ACCOUNT_ID)
				.header(VENDOR_ACCOUNT_ID_HEADER, "").header(REQUEST_TRACE_ID_HEADER, DEFAULT_REQUEST_TRACE_ID))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("default-validation-missing-header.json", "vendorAccountId"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetPricesWhenBlankVendorAccountIdHeaderThenProcessCorrectly() throws Exception {

		mockMvc.perform(get("/v2").contentType(MediaType.APPLICATION_JSON).header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION)
				.header(COUNTRY_HEADER, NO_PRICE_UP_FRONT_COUNTRY).header(VENDOR_ID_HEADER, DEFAULT_VENDOR_ACCOUNT_ID)
				.header(VENDOR_ACCOUNT_ID_HEADER, "   ").header(REQUEST_TRACE_ID_HEADER, DEFAULT_REQUEST_TRACE_ID))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("default-validation-missing-header.json", "vendorAccountId"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetPricesWhenStrangeVendorAccountIdHeaderThenProcessCorrectly() throws Exception {

		final PriceResultListV2 priceResultListV2 = getPriceResultListV2Mock("correct-priceResultListV2-without-optional-fields.json");
		when(priceServiceV2.getAllPrices(DEFAULT_VENDOR_ID, "STRANGE_VALUE-!@#456", NO_PRICE_UP_FRONT_COUNTRY, null, null, null))
				.thenReturn(priceResultListV2);

		mockMvc.perform(get("/v2").contentType(MediaType.APPLICATION_JSON).header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION)
				.header(COUNTRY_HEADER, NO_PRICE_UP_FRONT_COUNTRY).header(VENDOR_ID_HEADER, DEFAULT_VENDOR_ID)
				.header(VENDOR_ACCOUNT_ID_HEADER, "STRANGE_VALUE-!@#456").header(REQUEST_TRACE_ID_HEADER, DEFAULT_REQUEST_TRACE_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("correct-priceResponseV0V2-without-optional-fields.json"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetPricesWhenMissingVendorAccountIdHeaderThenValidateBadRequest() throws Exception {

		mockMvc.perform(get("/v2").contentType(MediaType.APPLICATION_JSON).header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION)
				.header(COUNTRY_HEADER, NO_PRICE_UP_FRONT_COUNTRY).header(VENDOR_ID_HEADER, DEFAULT_VENDOR_ID)
				.header(REQUEST_TRACE_ID_HEADER, DEFAULT_REQUEST_TRACE_ID)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("default-validation-missing-header.json", "vendorAccountId"))));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetPricesWhenWithPriceListIdHeaderIsInformedThenProcessCorrectly() throws Exception {

		final PriceResultListV2 priceResultListV2 = getPriceResultListV2Mock("correct-priceResultListV2-without-optional-fields.json");

		when(priceServiceV2.getAllPrices(DEFAULT_VENDOR_ID, DEFAULT_VENDOR_ACCOUNT_ID, NO_PRICE_UP_FRONT_COUNTRY, null,
				DEFAULT_PRICE_LIST_ID, null)).thenReturn(priceResultListV2);

		mockMvc.perform(get("/v2").contentType(MediaType.APPLICATION_JSON).header(AUTHORIZATION_HEADER, DEFAULT_AUTHORIZATION)
						.header(COUNTRY_HEADER, NO_PRICE_UP_FRONT_COUNTRY).header(VENDOR_ID_HEADER, DEFAULT_VENDOR_ID)
						.header(VENDOR_ACCOUNT_ID_HEADER, DEFAULT_VENDOR_ACCOUNT_ID).header(REQUEST_TRACE_ID_HEADER, DEFAULT_REQUEST_TRACE_ID)
						.header(PRICE_LIST_ID_HEADER, DEFAULT_PRICE_LIST_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", equalTo(getResponseDataFileContent("correct-priceResponseV0V2-without-optional-fields.json"))));
	}

}
