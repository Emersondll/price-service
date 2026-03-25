package com.abinbev.b2b.price.api.testhelpers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JwtBuilder {

	private static final String HMAC_SHA_256 = "HmacSHA256";
	private static final String SECRET_KEY = "secret";
	private static final String JWT_HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
	private static final String COUNTRY_FIELD = "country";
	private static final String EXTENSION_ACCOUNT_IDS_FIELD = "extension_accountids";
	private static final String APP_FIELD = "app";
	private static final String ROLES_FIELD = "roles";
	private static final String ACCOUNTS_FIELD = "accounts";

	private final JSONObject payload = new JSONObject();

	private JwtBuilder() {

	}

	public static JwtBuilder getBuilder() {

		return new JwtBuilder();
	}

	public JwtBuilder withCountry(final String country) {

		addValue(COUNTRY_FIELD, country);
		return this;
	}

	public JwtBuilder withExtensionAccountIds(final String extensionAccountIds) {

		addValue(EXTENSION_ACCOUNT_IDS_FIELD, extensionAccountIds);
		return this;
	}

	public JwtBuilder withApp(final String app) {

		addValue(APP_FIELD, app);
		return this;
	}

	public JwtBuilder withRoles(final List<String> roles) {

		final JSONArray rolesJson = new JSONArray();
		for (final String role : roles) {
			rolesJson.put(role);
		}
		addValue(ROLES_FIELD, rolesJson);
		return this;
	}

	public JwtBuilder withAccounts(final List<String> accounts) {

		final JSONArray accountsJson = new JSONArray();
		for (final String account : accounts) {
			accountsJson.put(account);
		}
		addValue(ACCOUNTS_FIELD, accountsJson);
		return this;
	}

	private void addValue(final String key, final Object value) {

		try {
			this.payload.put(key, value);
		} catch (JSONException e) {
			throw new RuntimeException();
		}
	}

	private String encode(final JSONObject obj) {

		return encode(obj.toString().getBytes(StandardCharsets.UTF_8));
	}

	private String encode(final byte[] bytes) {

		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private String hmacSha256(final String data, final String secret) {

		try {
			final byte[] hash = secret.getBytes(StandardCharsets.UTF_8);

			final Mac sha256Hmac = Mac.getInstance(HMAC_SHA_256);
			final SecretKeySpec secretKey = new SecretKeySpec(hash, HMAC_SHA_256);
			sha256Hmac.init(secretKey);

			final byte[] signedBytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
			return encode(signedBytes);
		} catch (final NoSuchAlgorithmException | InvalidKeyException ex) {
			Logger.getLogger(JwtBuilder.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
			return null;
		}
	}

	private String toString(final String encodedHeader, final JSONObject payload, final String signature) {

		return "Bearer " + encodedHeader + "." + encode(payload) + "." + signature;
	}

	public String build() {

		final String encodedHeader;
		try {
			encodedHeader = encode(new JSONObject(JWT_HEADER));
		} catch (JSONException e) {
			throw new RuntimeException();
		}

		return toString(encodedHeader, payload, hmacSha256(encodedHeader + "." + encode(payload), SECRET_KEY));
	}
}
