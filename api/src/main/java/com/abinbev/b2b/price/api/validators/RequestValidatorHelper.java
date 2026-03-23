package com.abinbev.b2b.price.api.validators;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.abinbev.b2b.price.api.config.properties.RequestHeaderData;
import com.abinbev.b2b.price.api.config.properties.SecurityProperties;
import com.abinbev.b2b.price.api.config.properties.SupportedProperties;
import com.abinbev.b2b.price.api.exceptions.InvalidHeaderException;
import com.abinbev.b2b.price.api.exceptions.InvalidParamException;
import com.abinbev.b2b.price.api.exceptions.JWTException;
import com.abinbev.b2b.price.api.exceptions.MissingHeaderException;
import com.abinbev.b2b.price.api.helpers.ApiConstants;
import com.abinbev.b2b.price.api.rest.vo.JwtDataVo;
import com.abinbev.b2b.price.api.security.jwt.JwtDecoder;
import com.abinbev.b2b.price.api.security.jwt.JwtTokenValidator;
import com.abinbev.b2b.price.api.security.jwt.JwtTokenValidatorV3;

@Component
public class RequestValidatorHelper {

	private final SupportedProperties supportedProperties;
	private final SecurityProperties securityProperties;
	private final RequestHeaderData requestHeaderData;
	private final JwtTokenValidator jwtTokenValidator;
	private final JwtTokenValidatorV3 jwtTokenValidatorV3;

	@Autowired
	public RequestValidatorHelper(final SupportedProperties supportedProperties, final SecurityProperties securityProperties,
			final RequestHeaderData requestHeaderData, final JwtTokenValidator jwtTokenValidator,
			final JwtTokenValidatorV3 jwtTokenValidatorV3) {

		this.supportedProperties = supportedProperties;
		this.securityProperties = securityProperties;
		this.requestHeaderData = requestHeaderData;
		this.jwtTokenValidator = jwtTokenValidator;
		this.jwtTokenValidatorV3 = jwtTokenValidatorV3;
	}

	public void validateHeaders(final List<String> headersToValidate, final String accountId, final String country,
			final String authorization) {

		if (headersToValidate.contains(ApiConstants.COUNTRY_HEADER)) {
			final String[] supportedCountriesArray = supportedProperties.getCountries().split(",");
			validateHeader(requestHeaderData.getCountry(), supportedCountriesArray, ApiConstants.COUNTRY_HEADER);
		}
		if (headersToValidate.contains(ApiConstants.REQUEST_TRACE_ID_HEADER)) {
			validateHeader(requestHeaderData.getRequestTraceId(), null, ApiConstants.REQUEST_TRACE_ID_HEADER);
		}
		validateAccountIdParam(accountId);

		if (isNotEmpty(authorization) && securityProperties.isEnabled() && headersToValidate.contains(ApiConstants.AUTHORIZATION_HEADER)) {
			validateJwt(authorization, accountId, country);
		}
	}

	public void validateHeadersV2(final String vendorId, final String vendorAccountId, final String country, final String authorization) {

		validateHeader(vendorId, null, ApiConstants.VENDOR_ID_HEADER);
		validateHeader(vendorAccountId, null, ApiConstants.VENDOR_ACCOUNT_ID_HEADER);
		validateHeader(requestHeaderData.getCountry(), supportedProperties.getCountriesV2().split(","), ApiConstants.COUNTRY_HEADER);
		validateHeader(requestHeaderData.getRequestTraceId(), null, ApiConstants.REQUEST_TRACE_ID_HEADER);

		if (isNotEmpty(authorization) && securityProperties.isEnabled()) {
			validateJwt(authorization, vendorAccountId, country);
		}
	}

	public void validateHeadersV3(final String authorization, final String country) {

		validateHeader(requestHeaderData.getCountry(), supportedProperties.getCountriesV3().split(","), ApiConstants.COUNTRY_HEADER);
		validateHeader(requestHeaderData.getRequestTraceId(), null, ApiConstants.REQUEST_TRACE_ID_HEADER);

		if (isNotEmpty(authorization) && securityProperties.isEnabled()) {
			validateJwtV3(authorization, country);
		}
	}

	private void validateJwt(final String authorization, final String accountId, final String country) {

		final JwtDataVo jwtDataVo = JwtDecoder.decodeToken(authorization);

		if (!jwtTokenValidator.isValid(jwtDataVo, accountId, country, securityProperties.getJwtApps())) {
			throw JWTException.invalidToken();
		}
	}

	private void validateJwtV3(final String authorization, final String country) {

		final JwtDataVo jwtDataVo = JwtDecoder.decodeToken(authorization);

		if (!jwtTokenValidatorV3.isValid(jwtDataVo, country, securityProperties.getJwtApps())) {
			throw JWTException.invalidToken();
		}
	}

	private void validateHeader(final String header, final String[] collection, final String apiConstant) {

		if (StringUtils.isNotBlank(header)) {
			if (ArrayUtils.isNotEmpty(collection)) {
				for (final String collectionHeader : collection) {
					if (collectionHeader.equalsIgnoreCase(header)) {
						return;
					}
				}
				throw new InvalidHeaderException(apiConstant, header);
			}
		} else {
			throw new MissingHeaderException(apiConstant, header);
		}
	}

	private void validateAccountIdParam(final String accountId) {

		if (StringUtils.isNotEmpty(accountId)) {
			return;
		}
		throw new InvalidParamException(ApiConstants.ACCOUNT_ID_PARAM, accountId);
	}

}
