package com.abinbev.b2b.price.api.validators;

import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_ACCOUNT_ID;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_COUNTRY_ZA;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_JWT_B2B_INVALID_ACCOUNT;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_JWT_B2B_WITHOUT_EXTENSION_IDS;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_JWT_B2B_ZA_COUNTRY;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_JWT_INVALID_COUNTRY_B2B;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_JWT_M2M;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_JWT_WITHOUT_COUNTRY_B2B;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abinbev.b2b.price.api.config.properties.RequestHeaderData;
import com.abinbev.b2b.price.api.config.properties.SecurityProperties;
import com.abinbev.b2b.price.api.config.properties.SupportedProperties;
import com.abinbev.b2b.price.api.exceptions.InvalidHeaderException;
import com.abinbev.b2b.price.api.exceptions.InvalidParamException;
import com.abinbev.b2b.price.api.exceptions.IssueEnum;
import com.abinbev.b2b.price.api.exceptions.JWTException;
import com.abinbev.b2b.price.api.exceptions.MissingHeaderException;
import com.abinbev.b2b.price.api.helpers.ApiConstants;
import com.abinbev.b2b.price.api.security.jwt.JwtTokenValidator;
import com.abinbev.b2b.price.api.security.jwt.JwtTokenValidatorV3;

@ExtendWith(MockitoExtension.class)
class RequestValidatorHelperTest {

	private static final String MOCKED_VENDOR_ID = "VENDOR_ID_1";
	private static final String MOCKED_VENDOR_ACCOUNT_ID = "VENDOR_ACCOUNT_ID_1";
	private static final String MOCKED_COUNTRY_US = "US";
	private static final Object SUPPORTED_COUNTRIES = "us,br,ar,ca";
	private static final String SUPPORTED_APPS = "oncustomer,ontap,supplier";
	private static final String INVALID_COUNTRY = "INVALID_COUNTRY";
	private static final String VENDOR_ID_HEADER = "vendorId";
	private static final String VENDOR_ACCOUNT_ID_HEADER = "vendorAccountId";
	private static final String REQUEST_TRACE_ID_HEADER = "requestTraceId";
	private static final String COUNTRY_HEADER = "country";

	@Mock
	private RequestHeaderData requestHeaderData;

	@Mock
	private SupportedProperties supportedProperties;

	@Mock
	private SecurityProperties securityProperties;

	@Mock
	private JwtTokenValidator jwtTokenValidator;

	@Mock
	private JwtTokenValidatorV3 jwtTokenValidatorV3;

	@InjectMocks
	private RequestValidatorHelper requestValidatorHelper;

	// validateHeaders method tests
	@Test
	void shouldValidateHeadersWhenValidHeadersAndValidParamAndB2bUser() {

		mockSupportedPropertiesToReturn();

		mockSecurityPropertiesIsEnabledToReturn(true);
		mockSecurityPropertiesSupportedAppsToReturn();

		Mockito.when(requestHeaderData.getRequestTraceId()).thenReturn(UUID.randomUUID().toString());
		Mockito.when(requestHeaderData.getCountry()).thenReturn(MOCKED_COUNTRY_ZA);
		Mockito.when(jwtTokenValidator.isValid(any(), anyString(), anyString(), anyString())).thenReturn(true);

		final List<String> headersToValidate = Arrays
				.asList(ApiConstants.COUNTRY_HEADER, ApiConstants.REQUEST_TRACE_ID_HEADER, ApiConstants.AUTHORIZATION_HEADER);

		assertDoesNotThrow(() -> requestValidatorHelper
				.validateHeaders(headersToValidate, MOCKED_ACCOUNT_ID, MOCKED_COUNTRY_ZA, MOCKED_JWT_B2B_ZA_COUNTRY));
	}

	@Test
	void shouldValidateHeadersWhenHaveNoSecurityProperties() {

		mockSecurityPropertiesIsEnabledToReturn(false);

		final List<String> headersToValidate = List.of(ApiConstants.AUTHORIZATION_HEADER);

		assertDoesNotThrow(() -> requestValidatorHelper
				.validateHeaders(headersToValidate, MOCKED_ACCOUNT_ID, MOCKED_COUNTRY_ZA, MOCKED_JWT_B2B_ZA_COUNTRY));
	}

	@Test
	void shouldValidateHeadersWhenHasNoAuthorization() {

		final List<String> headersToValidate = List.of(ApiConstants.AUTHORIZATION_HEADER);

		assertDoesNotThrow(() -> requestValidatorHelper.validateHeaders(headersToValidate, MOCKED_ACCOUNT_ID, MOCKED_COUNTRY_ZA, null));
	}

	@Test
	void shouldValidateHeadersWhenHasNoAuthorizationHeaders() {

		mockSecurityPropertiesIsEnabledToReturn(true);

		final List<String> headersToValidate = List.of();

		assertDoesNotThrow(() -> requestValidatorHelper
				.validateHeaders(headersToValidate, MOCKED_ACCOUNT_ID, MOCKED_COUNTRY_ZA, MOCKED_JWT_B2B_ZA_COUNTRY));
	}

	@Test
	void shouldValidateHeadersWhenInvalidCountryExceptionCalled() {

		mockSupportedPropertiesToReturn();

		Mockito.when(requestHeaderData.getCountry()).thenReturn(INVALID_COUNTRY);

		final List<String> headersToValidate = Arrays.asList(ApiConstants.COUNTRY_HEADER, ApiConstants.REQUEST_TRACE_ID_HEADER);

		final InvalidHeaderException invalidHeaderException = assertThrows(InvalidHeaderException.class, () -> requestValidatorHelper
				.validateHeaders(headersToValidate, MOCKED_ACCOUNT_ID, INVALID_COUNTRY, MOCKED_JWT_INVALID_COUNTRY_B2B));

		assertEquals(invalidHeaderException.getIssue().getMessage(), IssueEnum.BAD_REQUEST.getFormattedMessage());
		assertEquals(invalidHeaderException.getIssue().getDetails().get(0),
				IssueEnum.REQUEST_HEADER_NOT_VALID.getFormattedMessage(ApiConstants.COUNTRY_HEADER, INVALID_COUNTRY));

	}

	@Test
	void shouldValidateHeadersWhenEmptyCountryExceptionCalled() {

		mockSupportedPropertiesToReturn();

		Mockito.when(requestHeaderData.getCountry()).thenReturn(StringUtils.EMPTY);

		final List<String> headersToValidate = Arrays.asList(ApiConstants.COUNTRY_HEADER, ApiConstants.REQUEST_TRACE_ID_HEADER);

		final MissingHeaderException missingHeaderException = assertThrows(MissingHeaderException.class, () -> requestValidatorHelper
				.validateHeaders(headersToValidate, MOCKED_ACCOUNT_ID, StringUtils.EMPTY, MOCKED_JWT_WITHOUT_COUNTRY_B2B));

		assertEquals(missingHeaderException.getIssue().getMessage(), IssueEnum.BAD_REQUEST.getFormattedMessage());
		assertEquals(missingHeaderException.getIssue().getDetails().get(0),
				IssueEnum.REQUIRED_HEADER_MISSING.getFormattedMessage(ApiConstants.COUNTRY_HEADER, StringUtils.EMPTY));

	}

	@Test
	void shouldValidateHeadersWhenEmptyAccountIdExceptionCalled() {

		mockSupportedPropertiesToReturn();

		Mockito.when(requestHeaderData.getRequestTraceId()).thenReturn(UUID.randomUUID().toString());
		Mockito.when(requestHeaderData.getCountry()).thenReturn(MOCKED_COUNTRY_ZA);

		final List<String> headersToValidate = Arrays.asList(ApiConstants.COUNTRY_HEADER, ApiConstants.REQUEST_TRACE_ID_HEADER);

		final InvalidParamException invalidParamException = assertThrows(InvalidParamException.class, () -> requestValidatorHelper
				.validateHeaders(headersToValidate, StringUtils.EMPTY, MOCKED_COUNTRY_ZA, MOCKED_JWT_B2B_WITHOUT_EXTENSION_IDS));

		assertEquals(invalidParamException.getIssue().getMessage(), IssueEnum.BAD_REQUEST.getFormattedMessage());
		assertEquals(invalidParamException.getIssue().getDetails().get(0),
				IssueEnum.REQUEST_PARAM_NOT_VALID.getFormattedMessage(ApiConstants.ACCOUNT_ID_PARAM, StringUtils.EMPTY));

	}

	@Test
	void shouldValidateMissingRequestTraceIdHeaderWhenTraceIdExceptionCalled() {

		Mockito.when(requestHeaderData.getRequestTraceId()).thenReturn(null);
		Mockito.when(requestHeaderData.getCountry()).thenReturn(MOCKED_COUNTRY_ZA);
		mockSupportedPropertiesToReturn();

		final List<String> headersToValidate = Arrays.asList(ApiConstants.COUNTRY_HEADER, ApiConstants.REQUEST_TRACE_ID_HEADER);

		final MissingHeaderException missingHeaderException = assertThrows(MissingHeaderException.class, () -> requestValidatorHelper
				.validateHeaders(headersToValidate, MOCKED_ACCOUNT_ID, MOCKED_COUNTRY_ZA, MOCKED_JWT_B2B_ZA_COUNTRY));

		assertEquals(missingHeaderException.getIssue().getMessage(), IssueEnum.BAD_REQUEST.getFormattedMessage());
		assertEquals(missingHeaderException.getIssue().getDetails().get(0),
				IssueEnum.REQUIRED_HEADER_MISSING.getFormattedMessage(ApiConstants.REQUEST_TRACE_ID_HEADER, StringUtils.EMPTY));
	}

	@Test
	void shouldValidateMissingCountryHeaderWhenCountryExceptionCalled() {

		Mockito.when(requestHeaderData.getCountry()).thenReturn(null);
		mockSupportedPropertiesToReturn();

		final List<String> headersToValidate = Arrays.asList(ApiConstants.COUNTRY_HEADER, ApiConstants.REQUEST_TRACE_ID_HEADER,
				ApiConstants.AUTHORIZATION_HEADER);

		final MissingHeaderException missingHeaderException = assertThrows(MissingHeaderException.class,
				() -> requestValidatorHelper.validateHeaders(headersToValidate, MOCKED_ACCOUNT_ID, null, MOCKED_JWT_WITHOUT_COUNTRY_B2B));

		assertEquals(missingHeaderException.getIssue().getMessage(), IssueEnum.BAD_REQUEST.getFormattedMessage());
		assertEquals(missingHeaderException.getIssue().getDetails().get(0),
				IssueEnum.REQUIRED_HEADER_MISSING.getFormattedMessage(ApiConstants.COUNTRY_HEADER, StringUtils.EMPTY));
	}

	@Test
	void shouldValidateWhenAnyHeaderShouldNotThrowException() {

		assertDoesNotThrow(() -> requestValidatorHelper.validateHeaders(Collections.emptyList(), MOCKED_ACCOUNT_ID, MOCKED_COUNTRY_ZA,
				MOCKED_JWT_B2B_ZA_COUNTRY));
	}

	@Test
	void shouldValidateWithoutAnyHeaderWhenSecurityFalseShouldNotThrowException() {

		mockSecurityPropertiesIsEnabledToReturn(false);

		assertDoesNotThrow(() -> requestValidatorHelper.validateHeaders(Collections.emptyList(), MOCKED_ACCOUNT_ID, MOCKED_COUNTRY_ZA,
				MOCKED_JWT_B2B_ZA_COUNTRY));
	}

	@Test
	void shouldValidateHeadersWhenInvalidAccountIdTokenAndB2b() {

		mockSupportedPropertiesToReturn();

		mockSecurityPropertiesIsEnabledToReturn(true);

		Mockito.when(requestHeaderData.getRequestTraceId()).thenReturn(UUID.randomUUID().toString());
		Mockito.when(requestHeaderData.getCountry()).thenReturn(MOCKED_COUNTRY_ZA);

		final List<String> headersToValidate = Arrays
				.asList(ApiConstants.COUNTRY_HEADER, ApiConstants.REQUEST_TRACE_ID_HEADER, ApiConstants.AUTHORIZATION_HEADER);

		final JWTException jwtException = assertThrows(JWTException.class, () -> requestValidatorHelper
				.validateHeaders(headersToValidate, MOCKED_ACCOUNT_ID, MOCKED_COUNTRY_ZA, MOCKED_JWT_B2B_INVALID_ACCOUNT));

		assertEquals(jwtException.getIssue().getMessage(), IssueEnum.JWT_TOKEN_INVALID.getFormattedMessage());
	}

	@Test
	void shouldValidateHeadersWhenInvalidExtensionIdsToken() {

		mockSupportedPropertiesToReturn();
		mockSecurityPropertiesIsEnabledToReturn(true);

		Mockito.when(requestHeaderData.getRequestTraceId()).thenReturn(UUID.randomUUID().toString());
		Mockito.when(requestHeaderData.getCountry()).thenReturn(MOCKED_COUNTRY_ZA);

		final List<String> headersToValidate = Arrays
				.asList(ApiConstants.COUNTRY_HEADER, ApiConstants.REQUEST_TRACE_ID_HEADER, ApiConstants.AUTHORIZATION_HEADER);

		final JWTException jwtException = assertThrows(JWTException.class, () -> requestValidatorHelper
				.validateHeaders(headersToValidate, MOCKED_ACCOUNT_ID, MOCKED_COUNTRY_ZA, MOCKED_JWT_B2B_INVALID_ACCOUNT));

		assertEquals(jwtException.getIssue().getMessage(), IssueEnum.JWT_TOKEN_INVALID.getFormattedMessage());
	}

	@Test
	void shouldValidateHeadersWithInvalidExtensionIdsTokenWhenSecurityIsDisabled() {

		mockSupportedPropertiesToReturn();
		mockSecurityPropertiesIsEnabledToReturn(false);

		Mockito.when(requestHeaderData.getRequestTraceId()).thenReturn(UUID.randomUUID().toString());
		Mockito.when(requestHeaderData.getCountry()).thenReturn(MOCKED_COUNTRY_ZA);

		final List<String> headersToValidate = Arrays
				.asList(ApiConstants.COUNTRY_HEADER, ApiConstants.REQUEST_TRACE_ID_HEADER, ApiConstants.AUTHORIZATION_HEADER);

		assertDoesNotThrow(() -> requestValidatorHelper
				.validateHeaders(headersToValidate, MOCKED_ACCOUNT_ID, MOCKED_COUNTRY_ZA, MOCKED_JWT_B2B_INVALID_ACCOUNT));
	}

	@Test
	void shouldValidateWhenOtherAppsByPassJwtToken() {

		mockSupportedPropertiesToReturn();
		mockSecurityPropertiesIsEnabledToReturn(true);
		mockSecurityPropertiesSupportedAppsToReturn();

		Mockito.when(requestHeaderData.getRequestTraceId()).thenReturn(UUID.randomUUID().toString());
		Mockito.when(requestHeaderData.getCountry()).thenReturn(MOCKED_COUNTRY_ZA);
		Mockito.when(jwtTokenValidator.isValid(any(), anyString(), anyString(), anyString())).thenReturn(true);

		final List<String> headersToValidate = Arrays
				.asList(ApiConstants.COUNTRY_HEADER, ApiConstants.REQUEST_TRACE_ID_HEADER, ApiConstants.AUTHORIZATION_HEADER);

		assertDoesNotThrow(
				() -> requestValidatorHelper.validateHeaders(headersToValidate, MOCKED_ACCOUNT_ID, MOCKED_COUNTRY_ZA, MOCKED_JWT_M2M));
	}

	// validateHeadersV2 method tests
	@Test
	void shouldValidateHeadersV2WhenValidHeaders() {

		doReturn(MOCKED_COUNTRY_US).when(requestHeaderData).getCountry();
		doReturn(UUID.randomUUID().toString()).when(requestHeaderData).getRequestTraceId();
		doReturn(SUPPORTED_COUNTRIES).when(supportedProperties).getCountriesV2();

		assertDoesNotThrow(() -> requestValidatorHelper
				.validateHeadersV2(MOCKED_VENDOR_ID, MOCKED_VENDOR_ACCOUNT_ID, MOCKED_COUNTRY_ZA, MOCKED_JWT_M2M));
	}

	@Test
	void shouldValidateHeadersV2WhenInvalidCountryHeader() {

		doReturn(INVALID_COUNTRY).when(requestHeaderData).getCountry();
		doReturn(SUPPORTED_COUNTRIES).when(supportedProperties).getCountriesV2();

		final InvalidHeaderException invalidHeaderException = assertThrows(InvalidHeaderException.class, () -> requestValidatorHelper
				.validateHeadersV2(MOCKED_VENDOR_ID, MOCKED_VENDOR_ACCOUNT_ID, MOCKED_COUNTRY_ZA, MOCKED_JWT_M2M));

		assertEquals(invalidHeaderException.getIssue().getMessage(), IssueEnum.BAD_REQUEST.getFormattedMessage());
		assertEquals(invalidHeaderException.getIssue().getDetails().get(0),
				IssueEnum.REQUEST_HEADER_NOT_VALID.getFormattedMessage(COUNTRY_HEADER, INVALID_COUNTRY));
	}

	@Test
	void shouldValidateHeadersV2WhenNullRequestTraceIdHeader() {

		doReturn(MOCKED_COUNTRY_US).when(requestHeaderData).getCountry();
		doReturn(null).when(requestHeaderData).getRequestTraceId();
		doReturn(SUPPORTED_COUNTRIES).when(supportedProperties).getCountriesV2();

		final MissingHeaderException missingHeaderException = assertThrows(MissingHeaderException.class, () -> requestValidatorHelper
				.validateHeadersV2(MOCKED_VENDOR_ID, MOCKED_VENDOR_ACCOUNT_ID, MOCKED_COUNTRY_ZA, MOCKED_JWT_M2M));

		assertEquals(missingHeaderException.getIssue().getMessage(), IssueEnum.BAD_REQUEST.getFormattedMessage());
		assertEquals(missingHeaderException.getIssue().getDetails().get(0),
				IssueEnum.REQUIRED_HEADER_MISSING.getFormattedMessage(REQUEST_TRACE_ID_HEADER, null));
	}

	@Test
	void shouldValidateHeadersV2WhenEmptyVendorIdHeader() {

		final String emptyVendorId = "";

		final MissingHeaderException missingHeaderException = assertThrows(MissingHeaderException.class,
				() -> requestValidatorHelper.validateHeadersV2(emptyVendorId, MOCKED_VENDOR_ACCOUNT_ID, MOCKED_COUNTRY_ZA, MOCKED_JWT_M2M));

		assertEquals(missingHeaderException.getIssue().getMessage(), IssueEnum.BAD_REQUEST.getFormattedMessage());
		assertEquals(missingHeaderException.getIssue().getDetails().get(0),
				IssueEnum.REQUIRED_HEADER_MISSING.getFormattedMessage(VENDOR_ID_HEADER, emptyVendorId));
	}

	@Test
	void shouldValidateHeadersV2WhenBlankVendorAccountIdHeader() {

		final String blankVendorAccountId = "   ";

		final MissingHeaderException missingHeaderException = assertThrows(MissingHeaderException.class,
				() -> requestValidatorHelper.validateHeadersV2(MOCKED_VENDOR_ID, blankVendorAccountId, MOCKED_COUNTRY_ZA, MOCKED_JWT_M2M));

		assertEquals(missingHeaderException.getIssue().getMessage(), IssueEnum.BAD_REQUEST.getFormattedMessage());
		assertEquals(missingHeaderException.getIssue().getDetails().get(0),
				IssueEnum.REQUIRED_HEADER_MISSING.getFormattedMessage(VENDOR_ACCOUNT_ID_HEADER, blankVendorAccountId));
	}

	@Test
	void shouldValidateHeadersV2WhenEmptyAuthorizationHeader() {

		doReturn(MOCKED_COUNTRY_US).when(requestHeaderData).getCountry();
		doReturn(UUID.randomUUID().toString()).when(requestHeaderData).getRequestTraceId();
		doReturn(SUPPORTED_COUNTRIES).when(supportedProperties).getCountriesV2();

		requestValidatorHelper.validateHeadersV2(MOCKED_VENDOR_ID, MOCKED_VENDOR_ACCOUNT_ID, MOCKED_COUNTRY_ZA, StringUtils.EMPTY);

		verify(securityProperties, times(0)).isEnabled();
	}

	// validateHeadersV3 method tests
	@Test
	void shouldValidateHeadersV3WhenAllHeadersAreValid() {

		// Given
		mockSecurityPropertiesSupportedAppsToReturn();
		mockSecurityPropertiesIsEnabledToReturn(true);
		doReturn(MOCKED_COUNTRY_US).when(requestHeaderData).getCountry();
		doReturn(SUPPORTED_COUNTRIES).when(supportedProperties).getCountriesV3();
		doReturn(UUID.randomUUID().toString()).when(requestHeaderData).getRequestTraceId();
		doReturn(true).when(jwtTokenValidatorV3).isValid(any(), eq(MOCKED_COUNTRY_ZA), eq(SUPPORTED_APPS));

		// When / Then
		assertDoesNotThrow(() -> requestValidatorHelper.validateHeadersV3(MOCKED_JWT_M2M, MOCKED_COUNTRY_ZA));
	}

	@Test
	void shouldNotValidateAuthorizationV3WhenTokenIsEmpty() {

		// Given
		doReturn(MOCKED_COUNTRY_US).when(requestHeaderData).getCountry();
		doReturn(SUPPORTED_COUNTRIES).when(supportedProperties).getCountriesV3();
		doReturn(UUID.randomUUID().toString()).when(requestHeaderData).getRequestTraceId();

		// When / Then
		assertDoesNotThrow(() -> requestValidatorHelper.validateHeadersV3("", MOCKED_COUNTRY_ZA));
	}

	@Test
	void shouldNotValidateAuthorizationV3WhenSecurityPropertiesIsNotEnabled() {

		// Given
		mockSecurityPropertiesIsEnabledToReturn(false);
		doReturn(MOCKED_COUNTRY_US).when(requestHeaderData).getCountry();
		doReturn(SUPPORTED_COUNTRIES).when(supportedProperties).getCountriesV3();
		doReturn(UUID.randomUUID().toString()).when(requestHeaderData).getRequestTraceId();

		// When / Then
		assertDoesNotThrow(() -> requestValidatorHelper.validateHeadersV3(MOCKED_JWT_M2M, MOCKED_COUNTRY_ZA));
	}

	@Test
	void shouldValidateHeadersV3WhenHeaderCountryIsInvalid() {

		// Given
		doReturn(INVALID_COUNTRY).when(requestHeaderData).getCountry();
		doReturn(SUPPORTED_COUNTRIES).when(supportedProperties).getCountriesV3();

		// When / Then
		final InvalidHeaderException invalidHeaderException = assertThrows(InvalidHeaderException.class,
				() -> requestValidatorHelper.validateHeadersV3(MOCKED_COUNTRY_ZA, MOCKED_JWT_M2M));

		assertEquals(invalidHeaderException.getIssue().getMessage(), IssueEnum.BAD_REQUEST.getFormattedMessage());
		assertEquals(invalidHeaderException.getIssue().getDetails().get(0),
				IssueEnum.REQUEST_HEADER_NOT_VALID.getFormattedMessage(COUNTRY_HEADER, INVALID_COUNTRY));
	}

	@Test
	void shouldValidateHeadersV3WhenHeaderRequestTraceIdIsNull() {

		// Given
		doReturn(MOCKED_COUNTRY_US).when(requestHeaderData).getCountry();
		doReturn(null).when(requestHeaderData).getRequestTraceId();
		doReturn(SUPPORTED_COUNTRIES).when(supportedProperties).getCountriesV3();

		// When / Then
		final MissingHeaderException missingHeaderException = assertThrows(MissingHeaderException.class,
				() -> requestValidatorHelper.validateHeadersV3(MOCKED_JWT_M2M, MOCKED_COUNTRY_ZA));

		assertEquals(missingHeaderException.getIssue().getMessage(), IssueEnum.BAD_REQUEST.getFormattedMessage());
		assertEquals(missingHeaderException.getIssue().getDetails().get(0),
				IssueEnum.REQUIRED_HEADER_MISSING.getFormattedMessage(REQUEST_TRACE_ID_HEADER, null));
	}

	@Test
	void shouldValidateHeadersExceptAuthorizationV3WhenHeaderAuthorizationIsEmpty() {

		// Given
		doReturn(MOCKED_COUNTRY_US).when(requestHeaderData).getCountry();
		doReturn(UUID.randomUUID().toString()).when(requestHeaderData).getRequestTraceId();
		doReturn(SUPPORTED_COUNTRIES).when(supportedProperties).getCountriesV3();

		// When
		requestValidatorHelper.validateHeadersV3(StringUtils.EMPTY, MOCKED_COUNTRY_ZA);

		// Then
		verify(securityProperties, times(0)).isEnabled();
	}

	@Test
	void shouldThrowJWTExceptionWhenReceiveAnInvalidToken() {

		// Given
		mockSecurityPropertiesSupportedAppsToReturn();
		mockSecurityPropertiesIsEnabledToReturn(true);
		doReturn(false).when(jwtTokenValidatorV3).isValid(any(), eq(MOCKED_COUNTRY_ZA), eq(SUPPORTED_APPS));
		doReturn(MOCKED_COUNTRY_US).when(requestHeaderData).getCountry();
		doReturn(UUID.randomUUID().toString()).when(requestHeaderData).getRequestTraceId();
		doReturn(SUPPORTED_COUNTRIES).when(supportedProperties).getCountriesV3();

		// When / Then
		assertThrows(JWTException.class, () -> requestValidatorHelper.validateHeadersV3(MOCKED_JWT_M2M, MOCKED_COUNTRY_ZA));
	}

	private void mockSupportedPropertiesToReturn() {

		Mockito.when(supportedProperties.getCountries()).thenReturn(ApiConstants.SUPPORTED_COUNTRIES);
	}

	private void mockSecurityPropertiesSupportedAppsToReturn() {

		Mockito.when(securityProperties.getJwtApps()).thenReturn(RequestValidatorHelperTest.SUPPORTED_APPS);
	}

	private void mockSecurityPropertiesIsEnabledToReturn(final boolean isEnable) {

		Mockito.when(securityProperties.isEnabled()).thenReturn(isEnable);
	}

}
