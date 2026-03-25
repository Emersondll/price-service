package com.abinbev.b2b.price.api.validators;

import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_JWT_B2B;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_JWT_WITHOUT_COUNTRY_B2B;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_SKU_1;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_SKU_2;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_VENDOR_ITEM_1;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_VENDOR_ITEM_2;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_VENDOR_ITEM_3;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abinbev.b2b.price.api.config.properties.OffersPointToV2;
import com.abinbev.b2b.price.api.exceptions.IssueEnum;
import com.abinbev.b2b.price.api.exceptions.MismatchedAccountValuesException;
import com.abinbev.b2b.price.api.exceptions.MismatchedSkusAndVendorItemsValuesException;
import com.abinbev.b2b.price.api.exceptions.MissingAccountHeaderException;
import com.abinbev.b2b.price.api.exceptions.MissingHeaderException;
import com.abinbev.b2b.price.api.helpers.ApiConstants;

@ExtendWith(MockitoExtension.class)
class PriceOffersRequestValidatorTest {

	private static final String VALID_ACCOUNT_ID_001 = "ACC-001";
	private static final String VALID_VENDOR_ACCOUNT_ID_001 = "ACC-001";
	private static final String INVALID_VENDOR_ACCOUNT_ID_001 = "VENDOR-ACC-001";
	private static final String VENDOR_ID_001 = "VENDOR-001";
	private static final String ENABLED_COUNTRY = "AR";
	private static final String DISABLED_COUNTRY = "US";
	private static final String SUPPORTED_COUNTRIES = "AR";
	private static final List<String> HEADERS_TO_VALIDATE = Arrays.asList("country", "requestTraceId", "authorization");
	private static final List<String> EMPTY_SET = Collections.emptyList();

	@Mock
	private RequestValidatorHelper requestValidatorHelper;

	@Mock
	private OffersPointToV2 offersPointToV2;

	@InjectMocks
	private PriceOffersRequestValidator priceOffersRequestValidator;

	@Test
	void shouldThrowMissingHeaderExceptionWhenCountryIsNotInformed() {

		final MissingHeaderException missingHeaderException = assertThrows(MissingHeaderException.class, () -> priceOffersRequestValidator
				.validatePriceOffersAndCheckVersion(VALID_ACCOUNT_ID_001, VALID_VENDOR_ACCOUNT_ID_001, EMPTY_SET, EMPTY_SET, VENDOR_ID_001,
						" ", MOCKED_JWT_WITHOUT_COUNTRY_B2B));

		assertThat(missingHeaderException.getIssue().getMessage(), is(equalTo(IssueEnum.BAD_REQUEST.getFormattedMessage())));
		assertThat(missingHeaderException.getIssue().getDetails().get(0),
				is(equalTo(IssueEnum.REQUIRED_HEADER_MISSING.getFormattedMessage(ApiConstants.COUNTRY_HEADER, " "))));

		verify(requestValidatorHelper, never()).validateHeaders(anyList(), anyString(), anyString(), anyString());
		verify(requestValidatorHelper, never()).validateHeadersV2(anyString(), anyString(), anyString(), anyString());
		verify(offersPointToV2, never()).getEnabledCountries();
	}

	@Test
	void shouldThrowMissingAccountHeaderExceptionWhenAccountIdAndVendorAccountIdHeadersAreNotInformed() {

		when(offersPointToV2.getEnabledCountries()).thenReturn(SUPPORTED_COUNTRIES);

		final MissingAccountHeaderException missingAccountHeaderException = assertThrows(MissingAccountHeaderException.class,
				() -> priceOffersRequestValidator
						.validatePriceOffersAndCheckVersion(" ", " ", EMPTY_SET, EMPTY_SET, VENDOR_ID_001, ENABLED_COUNTRY,
								MOCKED_JWT_B2B));

		assertThat(missingAccountHeaderException.getIssue().getMessage(), is(equalTo(IssueEnum.BAD_REQUEST.getFormattedMessage())));
		assertThat(missingAccountHeaderException.getIssue().getDetails().get(0),
				is(equalTo(IssueEnum.MISSING_ACCOUNT_ID_OR_VENDOR_ACCOUNT_ID_HEADER.getFormattedMessage())));

		verify(requestValidatorHelper, never()).validateHeaders(anyList(), anyString(), anyString(), anyString());
		verify(requestValidatorHelper, never()).validateHeadersV2(anyString(), anyString(), anyString(), anyString());
		verify(offersPointToV2).getEnabledCountries();
	}

	@Test
	void shouldThrowMismatchedAccountValuesExceptionWhenAccountIdAndVendorAccountIdHeadersWasBothInformedButAreNotTheEquals() {

		when(offersPointToV2.getEnabledCountries()).thenReturn(SUPPORTED_COUNTRIES);

		final MismatchedAccountValuesException mismatchedAccountValuesException = assertThrows(MismatchedAccountValuesException.class,
				() -> priceOffersRequestValidator
						.validatePriceOffersAndCheckVersion(VALID_ACCOUNT_ID_001, INVALID_VENDOR_ACCOUNT_ID_001, EMPTY_SET, EMPTY_SET,
								VENDOR_ID_001, ENABLED_COUNTRY, MOCKED_JWT_B2B));

		assertThat(mismatchedAccountValuesException.getIssue().getMessage(), is(equalTo(IssueEnum.BAD_REQUEST.getFormattedMessage())));
		assertThat(mismatchedAccountValuesException.getIssue().getDetails().get(0),
				is(equalTo(IssueEnum.MISMATCHED_ACCOUNT_ID_AND_VENDOR_ACCOUNT_ID_HEADER.getFormattedMessage())));

		verify(requestValidatorHelper, never()).validateHeaders(anyList(), anyString(), anyString(), anyString());
		verify(requestValidatorHelper, never()).validateHeadersV2(anyString(), anyString(), anyString(), anyString());
		verify(offersPointToV2).getEnabledCountries();
	}

	@Test
	void shouldThrowMismatchedSkusAndVendorItemsValuesExceptionWhenSkusAndVendorItemIdsAreInformedButHasDifferentSizes() {

		when(offersPointToV2.getEnabledCountries()).thenReturn(SUPPORTED_COUNTRIES);

		final List<String> skus = Arrays.asList(MOCKED_SKU_1, MOCKED_SKU_2);
		final List<String> vendorItemIds = Arrays.asList(MOCKED_VENDOR_ITEM_1, MOCKED_VENDOR_ITEM_2, MOCKED_VENDOR_ITEM_3);

		final MismatchedSkusAndVendorItemsValuesException mismatchedSkusAndVendorItemsValuesException = assertThrows(
				MismatchedSkusAndVendorItemsValuesException.class, () -> priceOffersRequestValidator
						.validatePriceOffersAndCheckVersion(VALID_ACCOUNT_ID_001, VALID_VENDOR_ACCOUNT_ID_001, skus, vendorItemIds,
								VENDOR_ID_001, ENABLED_COUNTRY, MOCKED_JWT_B2B));

		assertThat(mismatchedSkusAndVendorItemsValuesException.getIssue().getMessage(),
				is(equalTo(IssueEnum.BAD_REQUEST.getFormattedMessage())));
		assertThat(mismatchedSkusAndVendorItemsValuesException.getIssue().getDetails().get(0),
				is(equalTo(IssueEnum.MISMATCHED_SKUS_AND_VENDOR_ITEM_IDS_HEADER.getFormattedMessage())));

		verify(requestValidatorHelper, never()).validateHeaders(anyList(), anyString(), anyString(), anyString());
		verify(requestValidatorHelper, never()).validateHeadersV2(anyString(), anyString(), anyString(), anyString());
		verify(offersPointToV2).getEnabledCountries();
	}

	@Test
	void shouldValidateForV1WhenAccountIdIsInformed() {

		when(offersPointToV2.getEnabledCountries()).thenReturn(SUPPORTED_COUNTRIES);

		priceOffersRequestValidator
				.validatePriceOffersAndCheckVersion(VALID_ACCOUNT_ID_001, " ", Collections.emptyList(), Collections.emptyList(),
						VENDOR_ID_001, DISABLED_COUNTRY, MOCKED_JWT_B2B);

		verify(requestValidatorHelper).validateHeaders(HEADERS_TO_VALIDATE, VALID_ACCOUNT_ID_001, DISABLED_COUNTRY, MOCKED_JWT_B2B);
		verify(requestValidatorHelper, never()).validateHeadersV2(anyString(), anyString(), anyString(), anyString());
		verify(offersPointToV2).getEnabledCountries();
	}

	@Test
	void shouldValidateForV2WhenAccountIdIsInformed() {

		when(offersPointToV2.getEnabledCountries()).thenReturn(SUPPORTED_COUNTRIES);

		priceOffersRequestValidator
				.validatePriceOffersAndCheckVersion(VALID_ACCOUNT_ID_001, " ", Collections.emptyList(), Collections.emptyList(),
						VENDOR_ID_001, ENABLED_COUNTRY, MOCKED_JWT_B2B);

		verify(requestValidatorHelper, never()).validateHeaders(anyList(), anyString(), anyString(), anyString());
		verify(requestValidatorHelper).validateHeadersV2(VENDOR_ID_001, VALID_VENDOR_ACCOUNT_ID_001, ENABLED_COUNTRY, MOCKED_JWT_B2B);
		verify(offersPointToV2).getEnabledCountries();
	}

	@Test
	void shouldValidateForV1WhenVendorAccountIdIsInformed() {

		when(offersPointToV2.getEnabledCountries()).thenReturn(SUPPORTED_COUNTRIES);

		priceOffersRequestValidator
				.validatePriceOffersAndCheckVersion(" ", VALID_VENDOR_ACCOUNT_ID_001, Collections.emptyList(), Collections.emptyList(),
						VENDOR_ID_001, DISABLED_COUNTRY, MOCKED_JWT_B2B);

		verify(requestValidatorHelper).validateHeaders(HEADERS_TO_VALIDATE, VALID_ACCOUNT_ID_001, DISABLED_COUNTRY, MOCKED_JWT_B2B);
		verify(requestValidatorHelper, never()).validateHeadersV2(anyString(), anyString(), anyString(), anyString());
		verify(offersPointToV2).getEnabledCountries();
	}

	@Test
	void shouldValidateForV2WhenVendorAccountIdIsInformed() {

		when(offersPointToV2.getEnabledCountries()).thenReturn(SUPPORTED_COUNTRIES);

		priceOffersRequestValidator
				.validatePriceOffersAndCheckVersion(" ", VALID_VENDOR_ACCOUNT_ID_001, Collections.emptyList(), Collections.emptyList(),
						VENDOR_ID_001, ENABLED_COUNTRY, MOCKED_JWT_B2B);

		verify(requestValidatorHelper, never()).validateHeaders(anyList(), anyString(), anyString(), anyString());
		verify(requestValidatorHelper).validateHeadersV2(VENDOR_ID_001, VALID_VENDOR_ACCOUNT_ID_001, ENABLED_COUNTRY, MOCKED_JWT_B2B);
		verify(offersPointToV2).getEnabledCountries();
	}

	@Test
	void shouldValidateWhenOnlySkusAreInformed() {

		when(offersPointToV2.getEnabledCountries()).thenReturn(SUPPORTED_COUNTRIES);

		final List<String> skus = Arrays.asList(MOCKED_SKU_1, MOCKED_SKU_2);

		priceOffersRequestValidator
				.validatePriceOffersAndCheckVersion(VALID_ACCOUNT_ID_001, VALID_VENDOR_ACCOUNT_ID_001, skus, Collections.emptyList(),
						VENDOR_ID_001, ENABLED_COUNTRY, MOCKED_JWT_B2B);

		verify(requestValidatorHelper, never()).validateHeaders(anyList(), anyString(), anyString(), anyString());
		verify(requestValidatorHelper).validateHeadersV2(VENDOR_ID_001, VALID_VENDOR_ACCOUNT_ID_001, ENABLED_COUNTRY, MOCKED_JWT_B2B);
		verify(offersPointToV2).getEnabledCountries();
	}

	@Test
	void shouldValidateWhenOnlyVendorItemIdsAreInformed() {

		when(offersPointToV2.getEnabledCountries()).thenReturn(SUPPORTED_COUNTRIES);

		final List<String> vendorItemIds = Arrays.asList(MOCKED_VENDOR_ITEM_1, MOCKED_VENDOR_ITEM_2);

		priceOffersRequestValidator
				.validatePriceOffersAndCheckVersion(VALID_ACCOUNT_ID_001, VALID_VENDOR_ACCOUNT_ID_001, Collections.emptyList(),
						vendorItemIds, VENDOR_ID_001, ENABLED_COUNTRY, MOCKED_JWT_B2B);

		verify(requestValidatorHelper, never()).validateHeaders(anyList(), anyString(), anyString(), anyString());
		verify(requestValidatorHelper).validateHeadersV2(VENDOR_ID_001, VALID_VENDOR_ACCOUNT_ID_001, ENABLED_COUNTRY, MOCKED_JWT_B2B);
		verify(offersPointToV2).getEnabledCountries();
	}

	@Test
	void shouldValidateWhenSkusAndVendorItemIdsAreInformedAndAreEquals() {

		when(offersPointToV2.getEnabledCountries()).thenReturn(SUPPORTED_COUNTRIES);

		final List<String> skus = Arrays.asList(MOCKED_SKU_1, MOCKED_SKU_2);
		final List<String> vendorItemIds = Arrays.asList(MOCKED_VENDOR_ITEM_1, MOCKED_VENDOR_ITEM_2);

		priceOffersRequestValidator
				.validatePriceOffersAndCheckVersion(VALID_ACCOUNT_ID_001, VALID_VENDOR_ACCOUNT_ID_001, skus, vendorItemIds, VENDOR_ID_001,
						ENABLED_COUNTRY, MOCKED_JWT_B2B);

		verify(requestValidatorHelper, never()).validateHeaders(anyList(), anyString(), anyString(), anyString());
		verify(requestValidatorHelper).validateHeadersV2(VENDOR_ID_001, VALID_VENDOR_ACCOUNT_ID_001, ENABLED_COUNTRY, MOCKED_JWT_B2B);
		verify(offersPointToV2).getEnabledCountries();
	}

}
