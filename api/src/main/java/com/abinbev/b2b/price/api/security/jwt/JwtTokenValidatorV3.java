package com.abinbev.b2b.price.api.security.jwt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.abinbev.b2b.price.api.helpers.ApiConstants;
import com.abinbev.b2b.price.api.rest.vo.JwtDataVo;

@Component
public class JwtTokenValidatorV3 {

	private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenValidatorV3.class);
	private static final String HMAC_TYPE = "HMAC";
	private static final String M2M_TYPE = "M2M";

	private JwtTokenValidatorV3() {

	}

	public boolean isValid(final JwtDataVo jwtDataVo, final String country, final String supportedJwtApps) {

		final List<String> validationMessages = new ArrayList<>();
		final boolean isValid = isValidHMAC(jwtDataVo, validationMessages) ||
				isValidB2CRSA(jwtDataVo, country, validationMessages) ||
				isValidM2MRSA(jwtDataVo, Arrays.asList(ApiConstants.ROLE_READ, ApiConstants.ROLE_WRITE), validationMessages) ||
				isValidB2COtherApps(jwtDataVo, supportedJwtApps, validationMessages);

		if (!isValid) {
			final String messages = String.join("\n", validationMessages);
			LOGGER.error(messages);
		}
		return isValid;
	}

	private boolean isValidHMAC(final JwtDataVo jwtDataVo, final List<String> validationMessages) {

		return isRolesAuthorized(jwtDataVo, Collections.singletonList(ApiConstants.ROLE_CUSTOMER), HMAC_TYPE, validationMessages);
	}

	private boolean isValidB2CRSA(final JwtDataVo jwtDataVo, final String country, final List<String> validationMessagesList) {

		if (StringUtils.isNotEmpty(jwtDataVo.getCountry()) && country.equalsIgnoreCase(jwtDataVo.getCountry())) {
			if ((StringUtils.isNotEmpty(jwtDataVo.getApp()) && ApiConstants.APP_B2B.equalsIgnoreCase(jwtDataVo.getApp())) || StringUtils
					.isEmpty(jwtDataVo.getApp())) {
				return true;
			}
			validationMessagesList.add(String.format("B2CRSA Token does not match the app: %s", ApiConstants.APP_B2B));
			return false;
		}
		validationMessagesList.add(String.format("B2CRSA Token does not match the request country: %s", country));
		return false;
	}

	private boolean isValidM2MRSA(final JwtDataVo jwtDataVo, final List<String> roles, final List<String> validationMessages) {

		return isRolesAuthorized(jwtDataVo, roles, M2M_TYPE, validationMessages);
	}

	private boolean isValidB2COtherApps(final JwtDataVo jwtDataVo, final String supportedJwtApps,
			final List<String> validationMessagesList) {

		if (CollectionUtils.isEmpty(jwtDataVo.getAccounts()) && CollectionUtils.isEmpty(jwtDataVo.getExtensionAccountIds())) {
			if (StringUtils.isNotEmpty(jwtDataVo.getApp())) {
				final String[] supportedApps = supportedJwtApps.split(",");
				if (Arrays.stream(supportedApps).anyMatch(app -> app.equalsIgnoreCase(jwtDataVo.getApp()))) {
					return true;
				}
				validationMessagesList.add("B2C Token does not contain any supported app.");
				return false;
			}
			validationMessagesList.add("B2C Token does not have any app.");
			return false;
		}
		validationMessagesList.add("Token is not a B2C from other apps once it contains some account.");
		return false;

	}

	private boolean isRolesAuthorized(final JwtDataVo jwtDataVo, final List<String> roles, final String tokenType,
			final List<String> validationMessages) {

		if (CollectionUtils.isNotEmpty(jwtDataVo.getRoles()) && jwtDataVo.getRoles().stream().map(String::toUpperCase)
				.collect(Collectors.toList()).containsAll(roles)) {
			return true;
		}
		validationMessages.add(String.format("Token %s does not contain the roles: %s ", tokenType, roles));
		return false;
	}
}
