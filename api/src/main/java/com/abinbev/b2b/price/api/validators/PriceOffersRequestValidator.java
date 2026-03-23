package com.abinbev.b2b.price.api.validators;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.abinbev.b2b.price.api.config.properties.OffersPointToV2;
import com.abinbev.b2b.price.api.domain.OffersAliasParameters;
import com.abinbev.b2b.price.api.exceptions.MismatchedAccountValuesException;
import com.abinbev.b2b.price.api.exceptions.MismatchedSkusAndVendorItemsValuesException;
import com.abinbev.b2b.price.api.exceptions.MissingAccountHeaderException;
import com.abinbev.b2b.price.api.exceptions.MissingHeaderException;
import com.abinbev.b2b.price.api.helpers.ApiConstants;
import com.abinbev.b2b.price.api.helpers.offers.enums.VersionFlag;

@Component
public class PriceOffersRequestValidator {

	private final RequestValidatorHelper requestValidatorHelper;
	private final OffersPointToV2 offersPointToV2;

	@Autowired
	public PriceOffersRequestValidator(final RequestValidatorHelper requestValidatorHelper, final OffersPointToV2 offersPointToV2) {

		this.requestValidatorHelper = requestValidatorHelper;
		this.offersPointToV2 = offersPointToV2;
	}

	public OffersAliasParameters validatePriceOffersAndCheckVersion(final String accountId, final String vendorAccountId,
			final List<String> skus, final List<String> vendorItemIds, final String vendorId, final String country,
			final String authorization) {

		final OffersAliasParameters offersAliasParameters = new OffersAliasParameters();

		if (isBlank(country)) {
			throw new MissingHeaderException(ApiConstants.COUNTRY_HEADER, country);
		}

		final String[] enabledCountriesV2 = offersPointToV2.getEnabledCountries().split(",");
		final String vendorAccountIdAlias = validateAndGetAccountAlias(accountId, vendorAccountId);

		validateVendorItemIds(skus, vendorItemIds);

		if (Arrays.stream(enabledCountriesV2).anyMatch(country::equalsIgnoreCase)) {
			requestValidatorHelper.validateHeadersV2(vendorId, vendorAccountIdAlias, country, authorization);
			offersAliasParameters.setRequestVersion(VersionFlag.V2);
		} else {
			final List<String> headersToValidate = new ArrayList<>();
			headersToValidate.add(ApiConstants.COUNTRY_HEADER);
			headersToValidate.add(ApiConstants.REQUEST_TRACE_ID_HEADER);

			if (isNotEmpty(authorization)) {
				headersToValidate.add(ApiConstants.AUTHORIZATION_HEADER);
			}

			requestValidatorHelper.validateHeaders(headersToValidate, vendorAccountIdAlias, country, authorization);
			offersAliasParameters.setRequestVersion(VersionFlag.V1);
		}

		offersAliasParameters.setRequestAccountId(vendorAccountIdAlias);
		offersAliasParameters.setRequestItems(getItemSkusAlias(skus, vendorItemIds));

		return offersAliasParameters;
	}

	private List<String> getItemSkusAlias(final List<String> skus, final List<String> vendorItemIds) {

		return CollectionUtils.isEmpty(skus) ? vendorItemIds : skus;
	}

	private String validateAndGetAccountAlias(final String accountId, final String vendorAccountId) {

		if (isBlank(accountId) && isBlank(vendorAccountId)) {
			throw new MissingAccountHeaderException();
		}

		if (isNotBlank(accountId) && isNotBlank(vendorAccountId) && !accountId.equalsIgnoreCase(vendorAccountId)) {
			throw new MismatchedAccountValuesException();
		}

		return isNotBlank(accountId) ? accountId : vendorAccountId;
	}

	private void validateVendorItemIds(final List<String> skus, final List<String> vendorItemIds) {

		if (isNotEmpty(skus) && isNotEmpty(vendorItemIds)) {
			checkEqualityOfVendorItemIds(skus, vendorItemIds);
		}
	}

	private void checkEqualityOfVendorItemIds(final List<String> skus, final List<String> vendorItemIds) {

		if (!skus.equals(vendorItemIds)) {
			throw new MismatchedSkusAndVendorItemsValuesException();
		}
	}
}
