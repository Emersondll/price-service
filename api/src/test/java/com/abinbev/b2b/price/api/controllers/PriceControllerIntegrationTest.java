package com.abinbev.b2b.price.api.controllers;

import static com.abinbev.b2b.price.api.testhelpers.TestConstants.ACCOUNT_ID_PARAM_NAME;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.AUTHORIZATION_HEADER_NAME;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.COUNTRY_HEADER_NAME;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.JWT_INVALID;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_COUNTRY_BR;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_COUNTRY_ZA;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_EMPTY_JWT;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_JWT_B2B;
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
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_PARAM_ACCOUNT_ID;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_REQUEST_TRACE_ID;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.REQUEST_TRACE_ID_HEADER_NAME;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.abinbev.b2b.price.api.config.properties.SecurityProperties;
import com.abinbev.b2b.price.api.rest.vo.Pagination;
import com.abinbev.b2b.price.api.services.PriceService;

@SpringBootTest
@AutoConfigureMockMvc
class PriceControllerIntegrationTest {

	private static final String MOCKED_ACCOUNT_ID = "27431273000147";
	private static final String MOCKED_ACCOUNT_ID1 = "123";
	private static final String PATH = "/v1";

	@MockBean
	private SecurityProperties securityProperties;

	@MockBean
	private PriceService priceService;

	@Autowired
	private MockMvc mockMvc;

	@BeforeEach
	void setup() {

		when(securityProperties.isEnabled()).thenReturn(Boolean.TRUE);
		when(securityProperties.getJwtApps()).thenReturn("oncustomer,ontap,supplier");
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus200WhenUsingValidB2bToken() throws Exception {

		mockMvc.perform(get(PATH).contentType(MediaType.APPLICATION_JSON).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR)
				.header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_B2B)
				.param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID)).andExpect(status().isOk());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus200WhenHasNoAuthorization() throws Exception {

		mockMvc.perform(get(PATH).contentType(MediaType.APPLICATION_JSON).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR)
				.header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID).param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID))
				.andExpect(status().isOk());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus200WhenUsingValidB2bTokenFromOtherSupportedApp() throws Exception {

		mockMvc.perform(get(PATH).contentType(MediaType.APPLICATION_JSON).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR)
				.header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID)
				.header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_WITH_SUPPORTED_APP_ONCUSTOMER)
				.param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID)).andExpect(status().isOk());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus403WhenUsingInvalidB2bTokenFromOtherSupportedApp() throws Exception {

		mockMvc.perform(get(PATH).contentType(MediaType.APPLICATION_JSON).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR)
				.header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID)
				.header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_WITH_UNSUPPORTED_APP).param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID))
				.andExpect(status().isForbidden());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus200WhenB2bTokenDoesNotHaveApp() throws Exception {

		mockMvc.perform(get(PATH).contentType(MediaType.APPLICATION_JSON).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR)
				.header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_WITHOUT_APP_B2B)
				.param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID)).andExpect(status().isOk());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus403WhenB2bTokenDoesNotHaveCountry() throws Exception {

		mockMvc.perform(get(PATH).contentType(MediaType.APPLICATION_JSON).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR)
				.header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID)
				.header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_WITHOUT_COUNTRY_B2B).param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID))
				.andExpect(status().isForbidden());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus403WhenB2bTokenDoesNotHaveExtensionsAccountIds() throws Exception {

		mockMvc.perform(get(PATH).contentType(MediaType.APPLICATION_JSON).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR)
				.header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID)
				.header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_WITHOUT_EXTENSION_ACCOUNTS_B2B)
				.param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID)).andExpect(status().isForbidden());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus403WhenB2bTokenHasInvalidApp() throws Exception {

		mockMvc.perform(get(PATH).contentType(MediaType.APPLICATION_JSON).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR)
				.header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_INVALID_APP_B2B)
				.param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID)).andExpect(status().isForbidden());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus403WhenAccountFromB2bTokenDoesNotMatchAccountFromRequest() throws Exception {

		mockMvc.perform(get(PATH).contentType(MediaType.APPLICATION_JSON).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR)
				.header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_B2B)
				.param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID1)).andExpect(status().isForbidden());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus403WhenCountryFromB2bTokenDoesNotMatchCountryFromRequest() throws Exception {

		mockMvc.perform(get(PATH).contentType(MediaType.APPLICATION_JSON).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_ZA)
				.header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_B2B)
				.param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID)).andExpect(status().isForbidden());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus200WhenUsingValidM2mToken() throws Exception {

		mockMvc.perform(get(PATH).contentType(MediaType.APPLICATION_JSON).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR)
				.header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_M2M)
				.param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID)).andExpect(status().isOk());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus403WhenUsingInvalidM2mWithOnlyReadRoleToken() throws Exception {

		mockMvc.perform(get(PATH).contentType(MediaType.APPLICATION_JSON).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR)
				.header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_JUST_READ_M2M)
				.param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID)).andExpect(status().isForbidden());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus403WhenUsingInvalidM2mWithOnlyWriteRoleToken() throws Exception {

		mockMvc.perform(get(PATH).contentType(MediaType.APPLICATION_JSON).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR)
				.header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_JUST_WRITE_M2M)
				.param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID)).andExpect(status().isForbidden());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus200WhenUsingValidHmacToken() throws Exception {

		mockMvc.perform(get(PATH).contentType(MediaType.APPLICATION_JSON).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR)
				.header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_HMAC)
				.param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID)).andExpect(status().isOk());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus403WhenUsingHmacTokenWithInvalidAccount() throws Exception {

		mockMvc.perform(get(PATH).contentType(MediaType.APPLICATION_JSON).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR)
				.header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_HMAC)
				.param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID1)).andExpect(status().isForbidden());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus403WhenUsingHmacTokenWithoutCustomerRole() throws Exception {

		mockMvc.perform(get(PATH).contentType(MediaType.APPLICATION_JSON).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR)
				.header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID)
				.header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_WITHOUT_CUSTOMER_ROLE_HMCA).param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID))
				.andExpect(status().isForbidden());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus400WhenUsingInvalidJwtToken() throws Exception {

		mockMvc.perform(get(PATH).contentType(MediaType.APPLICATION_JSON).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR)
				.header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER_NAME, JWT_INVALID)
				.param(ACCOUNT_ID_PARAM_NAME, MOCKED_ACCOUNT_ID)).andExpect(status().isForbidden());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnHttpStatus403WhenTokenJwtIsEmpty() throws Exception {

		mockMvc.perform(get(PATH).contentType(MediaType.APPLICATION_JSON).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR)
				.header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER_NAME, JWT_INVALID)
				.param(ACCOUNT_ID_PARAM_NAME, MOCKED_EMPTY_JWT)).andExpect(status().isForbidden());
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnSuccessMessageWhenGetSimplePrice() throws Exception {

		mockMvc.perform(get(PATH).contentType(MediaType.APPLICATION_JSON).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR)
				.header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_B2B)
				.param(MOCKED_PARAM_ACCOUNT_ID, MOCKED_ACCOUNT_ID).param("skus", "123,456").param("page", "0").param("size", "10"))
				.andDo(print()).andExpect(status().isOk());

		verify(priceService, times(1)).getAllPrices(anyString(), anyString(), anyList(), any(Pagination.class));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnSuccessMessageWhenGetSimplePriceAndNullPage() throws Exception {

		mockMvc.perform(get(PATH).contentType(MediaType.APPLICATION_JSON).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR)
						.header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_B2B)
						.param(MOCKED_PARAM_ACCOUNT_ID, MOCKED_ACCOUNT_ID).param("skus", "123,456").param("page", "0"))
				.andDo(print()).andExpect(status().isOk());

		verify(priceService, times(1)).getAllPrices(anyString(), anyString(), anyList(), any(Pagination.class));
	}

	@Test
	@Execution(SAME_THREAD)
	void shouldReturnSuccessMessageWhenGetSimplePriceAndNullSize() throws Exception {

		mockMvc.perform(get(PATH).contentType(MediaType.APPLICATION_JSON).header(COUNTRY_HEADER_NAME, MOCKED_COUNTRY_BR)
						.header(REQUEST_TRACE_ID_HEADER_NAME, MOCKED_REQUEST_TRACE_ID).header(AUTHORIZATION_HEADER_NAME, MOCKED_JWT_B2B)
						.param(MOCKED_PARAM_ACCOUNT_ID, MOCKED_ACCOUNT_ID).param("skus", "123,456").param("size", "10"))
				.andDo(print()).andExpect(status().isOk());

		verify(priceService, times(1)).getAllPrices(anyString(), anyString(), anyList(), any(Pagination.class));
	}
}
