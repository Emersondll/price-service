package com.abinbev.b2b.price.api.security.jwt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abinbev.b2b.price.api.helpers.ApiConstants;
import com.abinbev.b2b.price.api.rest.vo.JwtDataVo;

@ExtendWith(MockitoExtension.class)
class JwtTokenValidatorV3Test {

	private static final String VALID_EXTENSION_ACCOUNT_ID = "000423";
	private static final String VALID_COUNTRY = "BR";
	private static final String INVALID_COUNTRY = "ZZ";
	private static final String INVALID_APP = "ABC";
	private static final String SUPPORTED_APPS = "oncustomer,ontap,supplier";
	private static final String VALID_SUPPORTED_APP = "ontap";

	@InjectMocks
	private JwtTokenValidatorV3 jwtTokenValidatorV3;

	@Test
	void shouldBeInvalidWhenHMACDoesNotCustomerRole() {

		assertThat(jwtTokenValidatorV3.isValid(new JwtDataVo(), VALID_COUNTRY, SUPPORTED_APPS), is(false));
	}

	@Test
	void shouldBeValidWhenHMACContainsCustomerRole() {

		final JwtDataVo jwtDataVO = new JwtDataVo();
		jwtDataVO.setRoles(Collections.singletonList(ApiConstants.ROLE_CUSTOMER));

		assertThat(jwtTokenValidatorV3.isValid(jwtDataVO, VALID_COUNTRY, SUPPORTED_APPS), is(true));
	}

	@Test
	void shouldBeInvalidWhenB2CRSADoesNotMatchTheCountry() {

		final JwtDataVo jwtDataVO = new JwtDataVo();
		jwtDataVO.setCountry(VALID_COUNTRY);

		assertThat(jwtTokenValidatorV3.isValid(jwtDataVO, INVALID_COUNTRY, SUPPORTED_APPS), is(false));
	}

	@Test
	void shouldBeInvalidWhenB2CRSADoesNotMatchTheApp() {

		final JwtDataVo jwtDataVO = new JwtDataVo();
		jwtDataVO.setCountry(VALID_COUNTRY);
		jwtDataVO.setApp(INVALID_APP);

		assertThat(jwtTokenValidatorV3.isValid(jwtDataVO, VALID_COUNTRY, SUPPORTED_APPS), is(false));
	}

	@ParameterizedTest
	@ValueSource(strings = { ApiConstants.APP_B2B, ApiConstants.APP_B2B_UPPERCASE })
	void shouldBeValidWhenB2CRSAMatchesCountryAndApp(final String app) {

		final JwtDataVo jwtDataVO = new JwtDataVo();
		jwtDataVO.setCountry(VALID_COUNTRY);
		jwtDataVO.setApp(app);
		jwtDataVO.setExtensionAccountIds(Collections.singletonList(VALID_EXTENSION_ACCOUNT_ID));

		assertThat(jwtTokenValidatorV3.isValid(jwtDataVO, VALID_COUNTRY, SUPPORTED_APPS), is(true));
	}

	@Test
	void shouldBeValidWhenB2CRSAMatchesCountryAndAppIsEmpty() {

		final JwtDataVo jwtDataVO = new JwtDataVo();
		jwtDataVO.setCountry(VALID_COUNTRY);
		jwtDataVO.setExtensionAccountIds(Collections.singletonList(VALID_EXTENSION_ACCOUNT_ID));

		assertThat(jwtTokenValidatorV3.isValid(jwtDataVO, VALID_COUNTRY, SUPPORTED_APPS), is(true));
	}

	@Test
	void shouldBeInvalidWhenM2MRSADoesNotHaveRoles() {

		assertThat(jwtTokenValidatorV3.isValid(new JwtDataVo(), VALID_COUNTRY, SUPPORTED_APPS), is(false));
	}

	@Test
	void shouldBeInvalidWhenM2MRSADoesNotMatchRoles() {

		final JwtDataVo jwtDataVO = new JwtDataVo();
		jwtDataVO.setRoles(Collections.singletonList(ApiConstants.ROLE_READ));
		assertThat(jwtTokenValidatorV3.isValid(jwtDataVO, VALID_COUNTRY, SUPPORTED_APPS), is(false));
	}

	@Test
	void shouldBeInvalidWhenM2MRSADoesNotMatchTheRoles() {

		final JwtDataVo jwtDataVO = new JwtDataVo();
		jwtDataVO.setRoles(Arrays.asList(ApiConstants.ROLE_READ, ApiConstants.ROLE_WRITE));
		assertThat(jwtTokenValidatorV3.isValid(jwtDataVO, VALID_COUNTRY, SUPPORTED_APPS), is(true));
	}

	@Test
	void shouldBeInvalidWhenB2CHasNoAppSet() {

		final JwtDataVo jwtDataVO = new JwtDataVo();
		assertThat(jwtTokenValidatorV3.isValid(jwtDataVO, VALID_COUNTRY, SUPPORTED_APPS), is(false));
	}

	@Test
	void shouldBeInvalidWhenB2CHasNoSupportedAppSet() {

		final JwtDataVo jwtDataVO = new JwtDataVo();
		jwtDataVO.setApp(INVALID_APP);
		assertThat(jwtTokenValidatorV3.isValid(jwtDataVO, VALID_COUNTRY, SUPPORTED_APPS), is(false));
	}

	@Test
	void shouldValidWhenB2CHasSupportedAppSet() {

		final JwtDataVo jwtDataVO = new JwtDataVo();
		jwtDataVO.setApp(VALID_SUPPORTED_APP);
		assertThat(jwtTokenValidatorV3.isValid(jwtDataVO, VALID_COUNTRY, SUPPORTED_APPS), is(true));
	}

}