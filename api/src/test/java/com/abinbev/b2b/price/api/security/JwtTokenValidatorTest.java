package com.abinbev.b2b.price.api.security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
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
import com.abinbev.b2b.price.api.security.jwt.JwtTokenValidator;

@ExtendWith(MockitoExtension.class)
class JwtTokenValidatorTest {

	private static final String VALID_ACCOUNT_ID = "00004236";
	private static final String VALID_EXTENSION_ACCOUNT_ID = "000423";
	private static final String INVALID_ACCOUNT_ID = "00001618";
	private static final String VALID_COUNTRY = "BR";
	private static final String INVALID_COUNTRY = "ZZ";
	private static final String INVALID_APP = "ABC";
	private static final String SUPPORTED_APPS = "oncustomer,ontap,supplier";
	private static final String VALID_SUPPORTED_APP = "ontap";

	@InjectMocks
	private JwtTokenValidator jwtTokenValidator;

	@Test
	void shouldBeInvalidWhenHmacDoesNotContainRoles() {

		assertThat(jwtTokenValidator.isValid(new JwtDataVo(), VALID_ACCOUNT_ID, VALID_COUNTRY, SUPPORTED_APPS), is(false));
	}

	@Test
	void shouldBeInvalidWhenHmacDoesNotContainCustomerRole() {

		final JwtDataVo jwtDataVO = new JwtDataVo();
		jwtDataVO.setRoles(Collections.singletonList(ApiConstants.ROLE_READ));

		assertThat(jwtTokenValidator.isValid(jwtDataVO, null, VALID_COUNTRY, SUPPORTED_APPS), is(false));
	}

	@Test
	void shouldBeInvalidWhenHmacDoesNotContainTheAccount() {

		final JwtDataVo jwtDataVO = new JwtDataVo();
		jwtDataVO.setRoles(Collections.singletonList(ApiConstants.ROLE_CUSTOMER));
		jwtDataVO.setAccounts(Collections.singletonList(INVALID_ACCOUNT_ID));

		assertThat(jwtTokenValidator.isValid(jwtDataVO, VALID_ACCOUNT_ID, VALID_COUNTRY, SUPPORTED_APPS), is(false));
	}

	@Test
	void shouldBeValidWhenHmacContainsCustomerRoleAndAccount() {

		final JwtDataVo jwtDataVO = new JwtDataVo();
		jwtDataVO.setRoles(Collections.singletonList(ApiConstants.ROLE_CUSTOMER));
		jwtDataVO.setAccounts(Collections.singletonList(VALID_ACCOUNT_ID));

		assertThat(jwtTokenValidator.isValid(jwtDataVO, VALID_ACCOUNT_ID, VALID_COUNTRY, SUPPORTED_APPS), is(true));
	}

	@Test
	void shouldBeInvalidWhenB2cRsaDoesNotMatchTheCountry() {

		final JwtDataVo jwtDataVO = new JwtDataVo();
		jwtDataVO.setCountry(INVALID_COUNTRY);

		assertThat(jwtTokenValidator.isValid(jwtDataVO, null, VALID_COUNTRY, SUPPORTED_APPS), is(false));
	}

	@Test
	void shouldBeInvalidWhenB2cRsaDoesNotMatchTheApp() {

		final JwtDataVo jwtDataVO = new JwtDataVo();
		jwtDataVO.setCountry(VALID_COUNTRY);
		jwtDataVO.setApp(INVALID_APP);

		assertThat(jwtTokenValidator.isValid(jwtDataVO, null, VALID_COUNTRY, SUPPORTED_APPS), is(false));
	}

	@Test
	void shouldBeInvalidWhenB2cRsaDoesNotContainsTheAccount() {

		final JwtDataVo jwtDataVO = new JwtDataVo();
		jwtDataVO.setCountry(VALID_COUNTRY);
		jwtDataVO.setApp(ApiConstants.APP_B2B);
		jwtDataVO.setAccounts(new ArrayList<>());
		jwtDataVO.setExtensionAccountIds(new ArrayList<>());

		assertThat(jwtTokenValidator.isValid(jwtDataVO, VALID_ACCOUNT_ID, VALID_COUNTRY, SUPPORTED_APPS), is(false));
	}

	@ParameterizedTest
	@ValueSource(strings = { ApiConstants.APP_B2B, ApiConstants.APP_B2B_UPPERCASE })
	void shouldBeValidWhenB2cRsaMatchesCountryAndAppAndContainsAccount(final String app) {

		final JwtDataVo jwtDataVO = new JwtDataVo();
		jwtDataVO.setCountry(VALID_COUNTRY);
		jwtDataVO.setApp(app);
		jwtDataVO.setExtensionAccountIds(Collections.singletonList(VALID_EXTENSION_ACCOUNT_ID));

		assertThat(jwtTokenValidator.isValid(jwtDataVO, VALID_EXTENSION_ACCOUNT_ID, VALID_COUNTRY, SUPPORTED_APPS), is(true));
	}

	@Test
	void shouldBeValidWhenB2cRsaMatchesCountryAndIsEmptyAppAndContainsAccount() {

		final JwtDataVo jwtDataVO = new JwtDataVo();
		jwtDataVO.setCountry(VALID_COUNTRY);
		jwtDataVO.setExtensionAccountIds(Collections.singletonList(VALID_EXTENSION_ACCOUNT_ID));

		assertThat(jwtTokenValidator.isValid(jwtDataVO, VALID_EXTENSION_ACCOUNT_ID, VALID_COUNTRY, SUPPORTED_APPS), is(true));
	}

	@Test
	void shouldBeInvalidWhenM2mRsaDoesNotHaveRoles() {

		assertThat(jwtTokenValidator.isValid(new JwtDataVo(), null, VALID_COUNTRY, SUPPORTED_APPS), is(false));
	}

	@Test
	void shouldBeInvalidWhenM2mRsaDoesNotMatchRoles() {

		final JwtDataVo jwtDataVO = new JwtDataVo();
		jwtDataVO.setRoles(Collections.singletonList(ApiConstants.ROLE_READ));
		assertThat(jwtTokenValidator.isValid(jwtDataVO, null, VALID_COUNTRY, SUPPORTED_APPS), is(false));
	}

	@Test
	void shouldBeInvalidWhenM2mRsaDoesNotMatchTheRoles() {

		final JwtDataVo jwtDataVO = new JwtDataVo();
		jwtDataVO.setRoles(Arrays.asList(ApiConstants.ROLE_READ, ApiConstants.ROLE_WRITE));
		assertThat(jwtTokenValidator.isValid(jwtDataVO, null, VALID_COUNTRY, SUPPORTED_APPS), is(true));
	}

	@Test
	void shouldBeInvalidWhenB2cHasNoAccountSet() {

		final JwtDataVo jwtDataVO = new JwtDataVo();
		jwtDataVO.setRoles(Arrays.asList(ApiConstants.ROLE_READ, ApiConstants.ROLE_WRITE));
		assertThat(jwtTokenValidator.isValid(jwtDataVO, null, VALID_COUNTRY, SUPPORTED_APPS), is(true));
	}

	@Test
	void shouldBeInvalidWhenB2cHasNoAppSet() {

		final JwtDataVo jwtDataVO = new JwtDataVo();
		assertThat(jwtTokenValidator.isValid(jwtDataVO, null, VALID_COUNTRY, SUPPORTED_APPS), is(false));
	}

	@Test
	void shouldBeInvalidWhenB2cHasNoSupportedAppSet() {

		final JwtDataVo jwtDataVO = new JwtDataVo();
		jwtDataVO.setApp(INVALID_APP);
		assertThat(jwtTokenValidator.isValid(jwtDataVO, null, VALID_COUNTRY, SUPPORTED_APPS), is(false));
	}

	@Test
	void shouldValidWhenB2cHasSupportedAppSet() {

		final JwtDataVo jwtDataVO = new JwtDataVo();
		jwtDataVO.setApp(VALID_SUPPORTED_APP);
		assertThat(jwtTokenValidator.isValid(jwtDataVO, null, VALID_COUNTRY, SUPPORTED_APPS), is(true));
	}
}
