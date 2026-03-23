package com.abinbev.b2b.price.api.security.jwt;

import static com.abinbev.b2b.price.api.helpers.ApiConstants.ACCOUNTS;
import static com.abinbev.b2b.price.api.helpers.ApiConstants.BEARER_PREFIX;
import static com.abinbev.b2b.price.api.helpers.ApiConstants.CLAIM_APP;
import static com.abinbev.b2b.price.api.helpers.ApiConstants.COUNTRY_HEADER;
import static com.abinbev.b2b.price.api.helpers.ApiConstants.EXTENSION_ACCOUNTS;
import static com.abinbev.b2b.price.api.helpers.ApiConstants.ROLES;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.abinbev.b2b.price.api.exceptions.JWTException;
import com.abinbev.b2b.price.api.helpers.ApiConstants;
import com.abinbev.b2b.price.api.rest.vo.JwtDataVo;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

public class JwtDecoder {

	private static final Logger LOGGER = LoggerFactory.getLogger(JwtDecoder.class);

	private JwtDecoder() {

	}

	private static DecodedJWT decode(final String receivedToken) {

		final String token = readToken(receivedToken);

		try {
			return JWT.decode(token);
		} catch (final JWTDecodeException exception) {
			LOGGER.error(exception.getMessage(), exception);
			throw JWTException.decodeException();
		}
	}

	private static String readToken(final String receivedToken) {

		if (isNotEmpty(receivedToken) && receivedToken.startsWith(BEARER_PREFIX)) {
			return receivedToken.substring(7);
		}
		LOGGER.error(ApiConstants.TOKEN_INVALID);
		throw JWTException.invalidToken();
	}

	public static JwtDataVo decodeToken(final String token) {

		final DecodedJWT decodedJwt = decode(token);

		return JwtDataVo.builder().withRoles(findRoles(decodedJwt)).withAccounts(findAccountIdsInJwt(decodedJwt))
				.withExtensionAccountIds(findExtensionAccountIds(decodedJwt)).withApp(findFieldInClaim(decodedJwt, CLAIM_APP))
				.withCountry(findFieldInClaim(decodedJwt, COUNTRY_HEADER)).build();
	}

	private static List<String> findRoles(final DecodedJWT decodedJWT) {

		final List<String> roles = decodedJWT.getClaim(ROLES).asList(String.class);
		return CollectionUtils.isEmpty(roles) ? emptyList() : roles;
	}

	private static List<String> findAccountIdsInJwt(final DecodedJWT decodedJwt) {

		final Claim magentoAccountIds = decodedJwt.getClaim(ACCOUNTS);
		final List<String> magentoAccountIdsAsList = magentoAccountIds.asList(String.class);

		if (!magentoAccountIds.isNull() && CollectionUtils.isNotEmpty(magentoAccountIdsAsList)) {
			return magentoAccountIdsAsList;
		}
		return emptyList();
	}

	private static List<String> findExtensionAccountIds(final DecodedJWT decodedJwt) {

		final Claim extensionAccountIds = decodedJwt.getClaim(EXTENSION_ACCOUNTS);
		final String extensionAccountIdsAsString = extensionAccountIds.asString();

		if (!extensionAccountIds.isNull() && isNotBlank(extensionAccountIdsAsString)) {
			final String accounts = extensionAccountIdsAsString.replaceAll("[\\[\\]]", "");
			return Arrays.asList(accounts.split(","));
		}
		return emptyList();
	}

	private static String findFieldInClaim(final DecodedJWT decodedJwt, final String field) {

		final Claim fieldClaim = decodedJwt.getClaim(field);
		return fieldClaim.isNull() ? null : fieldClaim.asString();
	}

}
