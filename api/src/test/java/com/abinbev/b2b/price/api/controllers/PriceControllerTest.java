package com.abinbev.b2b.price.api.controllers;

import static com.abinbev.b2b.price.api.testhelpers.TestConstants.AUTHORIZATION_HEADER_NAME;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.COUNTRY_HEADER_NAME;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_ACCOUNT_ID;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_BASE_PRICE;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_CONSIGNMENT;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_COUNTRY_ZA;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_DEPOSIT;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_JWT_B2B;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_MEASURE_UNIT;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_MINIMUM_PRICE;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_PARAM_ACCOUNT_ID;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_SKU_1;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_TIMESTAMP;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_TIME_ZONE_ID;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.REQUEST_TRACE_ID_HEADER_NAME;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.abinbev.b2b.price.api.exceptions.GlobalExceptionHandler;
import com.abinbev.b2b.price.api.exceptions.InvalidHeaderException;
import com.abinbev.b2b.price.api.exceptions.InvalidParamException;
import com.abinbev.b2b.price.api.helpers.ApiConstants;
import com.abinbev.b2b.price.api.rest.vo.Pagination;
import com.abinbev.b2b.price.api.rest.vo.PriceResponseVo;
import com.abinbev.b2b.price.api.services.PriceService;
import com.abinbev.b2b.price.api.testhelpers.TestConstants;
import com.abinbev.b2b.price.api.validators.RequestValidatorHelper;
import com.abinbev.b2b.price.domain.model.PriceCompoundKey;
import com.abinbev.b2b.price.domain.model.PriceEntity;

@ExtendWith(SpringExtension.class)
class PriceControllerTest {

	private static final String PATH = "/v1";
	private static final String PARAM_SKUS = "skus";
	private static final String PARAM_SIZE = "size";
	private static final String PARAM_PAGE = "page";

	@Mock
	RequestValidatorHelper requestValidatorHelper;

	@Mock
	private PriceService priceService;

	private MockMvc mockMvc;

	@InjectMocks
	private PriceController priceController;

	@BeforeEach
	void setup() {

		mockMvc = MockMvcBuilders.standaloneSetup(priceController).setControllerAdvice(new GlobalExceptionHandler()).build();
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnMessageSuccessWhenHasSimplePrice() throws Exception {

		mockMvc.perform(get(PATH).param(MOCKED_PARAM_ACCOUNT_ID, "123").headers(createHeaders("1", "BR", MOCKED_JWT_B2B))
				.contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk());

		verifyExecutionOfValidateHeaders(1);
		verify(priceService, times(1)).getAllPrices(anyString(), anyString(), isNull(), isNull());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnStatusCode400WhenAccountNotSend() throws Exception {

		doThrow(new InvalidParamException(EMPTY, EMPTY)).when(requestValidatorHelper)
				.validateHeaders(ArgumentMatchers.anyList(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
						ArgumentMatchers.anyString());

		mockMvc.perform(get(PATH).headers(createHeaders("1", "BR", MOCKED_JWT_B2B)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isBadRequest());

		verifyExecutionOfValidateHeaders(0);
		verifyExecutionOfPriceService(0);
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnStatusCode400WhenAccountIsInvalid() throws Exception {

		doThrow(new InvalidParamException(EMPTY, EMPTY)).when(requestValidatorHelper)
				.validateHeaders(anyList(), anyString(), anyString(), anyString());

		mockMvc.perform(get(PATH).param(MOCKED_PARAM_ACCOUNT_ID, EMPTY).headers(createHeaders("1", "BR", MOCKED_JWT_B2B))
				.contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isBadRequest());

		verifyExecutionOfValidateHeaders(1);
		verifyExecutionOfPriceService(0);
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnStatusCode400WhenCountryNotSend() throws Exception {

		mockMvc.perform(get(PATH).param(MOCKED_PARAM_ACCOUNT_ID, "123").header(ApiConstants.REQUEST_TRACE_ID_HEADER, "1")
				.contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isBadRequest());

		verifyExecutionOfValidateHeaders(0);
		verifyExecutionOfPriceService(0);
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnStatusCode400WhenRequestTraceIdIsNotSent() throws Exception {

		mockMvc.perform(get(PATH).param(MOCKED_PARAM_ACCOUNT_ID, "123").header(ApiConstants.COUNTRY_HEADER, MOCKED_COUNTRY_ZA)
				.contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isBadRequest());

		verifyExecutionOfValidateHeaders(0);
		verifyExecutionOfPriceService(0);
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnStatusCode400WhenCountryIsInvalid() throws Exception {

		doThrow(new InvalidHeaderException(EMPTY, EMPTY)).when(requestValidatorHelper)
				.validateHeaders(anyList(), anyString(), anyString(), anyString());

		mockMvc.perform(
				get(PATH).param(MOCKED_PARAM_ACCOUNT_ID, "123").headers(createHeaders(UUID.randomUUID().toString(), "AF", MOCKED_JWT_B2B))
						.contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isBadRequest());

		verifyExecutionOfValidateHeaders(1);
		verifyExecutionOfPriceService(0);
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldGetMessageReturnWhenHasCustomerProductWithSuccess() throws Exception {

		mockPrices();

		final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(MOCKED_PARAM_ACCOUNT_ID, MOCKED_ACCOUNT_ID);
		params.add(PARAM_SKUS, MOCKED_SKU_1);
		params.add(PARAM_SIZE, "10");
		params.add(PARAM_PAGE, "1");

		mockMvc.perform(get(PATH).params(params).headers(createHeaders("1", "BR", MOCKED_JWT_B2B)).contentType(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.prices", hasSize(1)))
				.andExpect(jsonPath("$.prices[0].sku").value(MOCKED_SKU_1))
				.andExpect(jsonPath("$.prices[0].accountId").value(MOCKED_ACCOUNT_ID)).andExpect(jsonPath("$.pagination.page").value(1))
				.andExpect(jsonPath("$.pagination.size").value(10));

		verifyExecutionOfValidateHeaders(1);
		verifyExecutionOfPriceService(1);
	}

	@Test()
	void shouldReturnStatus200WhenBuildParamInLine() throws Exception {

		mockMvc.perform(get(PATH).param(MOCKED_PARAM_ACCOUNT_ID, "123").param("skus", buildSkusFilterInLine(51))
				.headers(createHeaders("1", "BR", MOCKED_JWT_B2B)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isOk());

		verifyExecutionOfValidateHeaders(1);
		verify(priceService, times(1)).getAllPrices(anyString(), anyString(), anyList(), isNull());
	}

	@Test()
	void shouldReturnStatus200WhenBuildParamCommercial() throws Exception {

		mockMvc.perform(get(PATH).param(MOCKED_PARAM_ACCOUNT_ID, "123").param("skus", buildSkusFilterCommercial(51))
				.headers(createHeaders("1", "BR", MOCKED_JWT_B2B)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isOk());

		verifyExecutionOfValidateHeaders(1);
		verify(priceService, times(1)).getAllPrices(anyString(), anyString(), anyList(), isNull());
	}

	private String buildSkusFilterInLine(final int quantity) {

		final StringBuilder skus = new StringBuilder();

		for (int index = 1; index <= quantity; index++) {
			skus.append(String.format("SKU%04d", index));

			if (index < quantity) {
				skus.append(",");
			}

		}

		return skus.toString();

	}

	private String buildSkusFilterCommercial(final int quantity) {

		final StringBuilder skus = new StringBuilder();

		for (int index = 1; index <= quantity; index++) {
			skus.append("skus").append("=").append(String.format("SKU%04d", index));

			if (index < quantity) {
				skus.append("&");
			}

		}

		return skus.toString();

	}

	private void mockPrices() {

		final PriceEntity priceEntity = new PriceEntity(MOCKED_BASE_PRICE, MOCKED_MEASURE_UNIT, MOCKED_MINIMUM_PRICE, MOCKED_DEPOSIT,
				MOCKED_CONSIGNMENT, TestConstants.MOCKED_ITEM_QUANTITY, null, null, null,
				new PriceCompoundKey(MOCKED_ACCOUNT_ID, MOCKED_SKU_1, null), false, MOCKED_COUNTRY_ZA, MOCKED_TIMESTAMP,
				MOCKED_TIME_ZONE_ID, null, null, null);

		final PriceResponseVo priceResponseVO = new PriceResponseVo(Collections.singletonList(priceEntity), new Pagination(1, 10));

		when(priceService.getAllPrices(anyString(), anyString(), any(), any())).thenReturn(priceResponseVO);

	}

	private HttpHeaders createHeaders(final String requestTraceId, final String country, final String authorization) {

		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(REQUEST_TRACE_ID_HEADER_NAME, requestTraceId);
		httpHeaders.add(COUNTRY_HEADER_NAME, country);
		httpHeaders.add(AUTHORIZATION_HEADER_NAME, authorization);
		return httpHeaders;
	}

	private void verifyExecutionOfValidateHeaders(final int times) {

		verify(requestValidatorHelper, times(times)).validateHeaders(anyList(), anyString(), anyString(), anyString());
	}

	private void verifyExecutionOfPriceService(final int times) {

		verify(priceService, times(times)).getAllPrices(anyString(), anyString(), anyList(), any(Pagination.class));
	}
}
