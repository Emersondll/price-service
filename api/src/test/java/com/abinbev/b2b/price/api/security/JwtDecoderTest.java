package com.abinbev.b2b.price.api.security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import com.abinbev.b2b.price.api.testhelpers.JwtBuilder;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.abinbev.b2b.price.api.exceptions.JWTException;
import com.abinbev.b2b.price.api.rest.vo.JwtDataVo;
import com.abinbev.b2b.price.api.security.jwt.JwtDecoder;

import java.util.List;

class JwtDecoderTest {

	public static final String MOCKED_COUNTRY = "BR";
	public static final String MOCKED_INVALID_COUNTRY = "XX";
	public static final String MOCKED_ACCOUNT_ID_RSA = "27431273000147";
	public static final String MOCKED_INVALID_ACCOUNT_ID = "0000000000";
	public static final String MOCKED_VALID_APP = "b2b";
	public static final String MOCKED_INVALID_APP = "mock_app";
	public static final String ROLE_READ = "Read";
	public static final String ROLE_WRITE = "Write";
	public static final String MOCKED_EXTENSION_ACCOUNT_IDS = "[27431273000147,26812643000123,38904063949,15128458000106,52299325115]";
	public static final List<String> MOCKED_ACCOUNTS = List.of("12451517","12451515",	"9980890901","12451514","9717834400");
	public static final String MOCKED_EMPTY_JWT = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.e30.yXvILkvUUCBqAFlAv6wQ1Q-QRAjfe3eSosO949U73Vo";
	public static final String MOCKED_RSA_JWT_B2B = JwtBuilder.getBuilder().withCountry(MOCKED_COUNTRY).withExtensionAccountIds(MOCKED_EXTENSION_ACCOUNT_IDS).withApp(MOCKED_VALID_APP).build();
	public static final String MOCKED_RSA_JWT_MOCK_APP = JwtBuilder.getBuilder().withCountry(MOCKED_COUNTRY).withExtensionAccountIds(MOCKED_EXTENSION_ACCOUNT_IDS).withApp(MOCKED_INVALID_APP).build();
	public static final String MOCKED_RSA_JWT_WITHOUT_APP = JwtBuilder.getBuilder().withCountry(MOCKED_COUNTRY).withExtensionAccountIds(MOCKED_EXTENSION_ACCOUNT_IDS).build();
	public static final String MOCKED_RSA_JWT_WITHOUT_EXTENSION_ACCOUNTS = JwtBuilder.getBuilder().	withCountry(MOCKED_COUNTRY).withApp(MOCKED_VALID_APP).build();
	public static final String MOCKED_RSA_JWT_WITHOUT_COUNTRY = JwtBuilder.getBuilder().withExtensionAccountIds(MOCKED_EXTENSION_ACCOUNT_IDS).withApp(MOCKED_VALID_APP).build();;
	public static final String MOCKED_HMAC_JWT = JwtBuilder.getBuilder().withAccounts(MOCKED_ACCOUNTS).build();
	public static final String MOCKED_ACCOUNT_ID_HMAC = "12451517";
	public static final String MOCKED_RSA_JWT_M2M = JwtBuilder.getBuilder().withRoles(List.of("Read", "Write")).build();
	public static final String MOCKED_RSA_JWT_M2M_JUST_READ = JwtBuilder.getBuilder().withRoles(List.of("Read")).build();
	public static final String MOCKED_RSA_JWT_M2M_JUST_WRITE = JwtBuilder.getBuilder().withRoles(List.of("Write")).build();
	public static final String INVALID_JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlcyI6WyJXcml0ZSJdfQ.yf1CKfId9W6gZRCpcCnET7oCAalKKGvJ_mtwDnmiP90";

	@Test
	void shouldSimulateWhenAccountsInExtensionAccounts() {

		final JwtDataVo jwtDataVo = JwtDecoder.decodeToken(MOCKED_RSA_JWT_B2B);
		assertThat(jwtDataVo.getCountry(), is(MOCKED_COUNTRY));
		assertThat(jwtDataVo.getApp(), is(MOCKED_VALID_APP));
		assertThat(jwtDataVo.getExtensionAccountIds(), hasSize(5));
		assertThat(jwtDataVo.getExtensionAccountIds(), containsInRelativeOrder(MOCKED_ACCOUNT_ID_RSA));
		assertThat(jwtDataVo.getExtensionAccountIds().contains(MOCKED_INVALID_ACCOUNT_ID), is(false));
	}

	@Test
	void shouldSimulateWhenJwtWithoutCountry() {

		final JwtDataVo jwtDataVo = JwtDecoder.decodeToken(MOCKED_RSA_JWT_WITHOUT_COUNTRY);
		assertThat(jwtDataVo.getCountry(), nullValue());
	}

	@Test
	void shouldSimulateWhenCountryNotEqualJwtCountry() {

		final JwtDataVo jwtDataVo = JwtDecoder.decodeToken(MOCKED_RSA_JWT_B2B);
		assertThat(jwtDataVo.getCountry(), not(MOCKED_INVALID_COUNTRY));
	}

	@Test
	void shouldSimulateJwtWhenHaveNoExtensionAccounts() {

		final JwtDataVo jwtDataVo = JwtDecoder.decodeToken(MOCKED_RSA_JWT_WITHOUT_EXTENSION_ACCOUNTS);
		assertThat(jwtDataVo.getExtensionAccountIds(), hasSize(0));
	}

	@Test
	void shouldSimulateJwtWhenHasNoApp() {

		final JwtDataVo jwtDataVo = JwtDecoder.decodeToken(MOCKED_RSA_JWT_WITHOUT_APP);
		assertThat(jwtDataVo.getApp(), nullValue());
	}

	@Test
	void shouldSimulateWhenJwtWitInvalidApp() {

		final JwtDataVo jwtDataVo = JwtDecoder.decodeToken(MOCKED_RSA_JWT_MOCK_APP);
		assertThat(jwtDataVo.getApp(), is(MOCKED_INVALID_APP));
	}

	@Test
	void shouldSimulateWhenAccountsInJwtAccountsHmac() {

		final JwtDataVo jwtDataVo = JwtDecoder.decodeToken(MOCKED_HMAC_JWT);
		assertThat(jwtDataVo.getAccounts(), hasSize(5));
		assertThat(jwtDataVo.getAccounts(), containsInRelativeOrder(MOCKED_ACCOUNT_ID_HMAC));
	}

	@Test
	void shouldSimulateWhenAccountsNotInJwtAccountsHmac() {

		final JwtDataVo jwtDataVo = JwtDecoder.decodeToken(MOCKED_HMAC_JWT);
		assertThat(jwtDataVo.getAccounts(), hasSize(5));
		assertThat(jwtDataVo.getAccounts().contains(MOCKED_INVALID_ACCOUNT_ID), is(false));
	}

	@Test
	void shouldSimulateJwtHmacWhenHaveNoAccounts() {

		final JwtDataVo jwtDataVo = JwtDecoder.decodeToken(MOCKED_EMPTY_JWT);
		assertThat(jwtDataVo.getAccounts(), hasSize(0));
	}

	@Test
	void shouldSimulateWhenInvalidRolesM2m() {

		final JwtDataVo jwtDataVo = JwtDecoder.decodeToken(MOCKED_EMPTY_JWT);
		assertThat(jwtDataVo.getRoles(), hasSize(0));
	}

	@Test
	void shouldSimulateWhenValidRolesM2m() {

		final JwtDataVo jwtDataVo = JwtDecoder.decodeToken(MOCKED_RSA_JWT_M2M);
		assertThat(jwtDataVo.getRoles(), hasSize(2));
		assertThat(jwtDataVo.getRoles(), containsInRelativeOrder(ROLE_READ));
		assertThat(jwtDataVo.getRoles(), containsInRelativeOrder(ROLE_WRITE));
	}

	@Test
	void shouldSimulateWhenJustReadRoleM2m() {

		final JwtDataVo jwtDataVo = JwtDecoder.decodeToken(MOCKED_RSA_JWT_M2M_JUST_READ);
		assertThat(jwtDataVo.getRoles(), hasSize(1));
		assertThat(jwtDataVo.getRoles(), containsInRelativeOrder(ROLE_READ));
	}

	@Test
	void shouldSimulateWhenJustWriteRoleM2m() {

		final JwtDataVo jwtDataVo = JwtDecoder.decodeToken(MOCKED_RSA_JWT_M2M_JUST_WRITE);
		assertThat(jwtDataVo.getRoles(), hasSize(1));
		assertThat(jwtDataVo.getRoles(), containsInRelativeOrder(ROLE_WRITE));
	}

	@Test
	void shouldNotDecodeWhenJwtIsEmpty() {

		Assert.assertThrows(JWTException.class, () -> JwtDecoder.decodeToken(""));
	}

	@Test
	void shouldNotDecodeWhenJwtHasNoBearerPrefix() {

		Assert.assertThrows(JWTException.class, () -> JwtDecoder.decodeToken(INVALID_JWT));
	}

}
