package helpers;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JwtGenerator {

	public static final String HMAC_SHA_256 = "HmacSHA256";
	public static final String ROLES = "roles";
	public static final String VENDOR_ID = "vendorId";
	public static final String ACCOUNTS = "accounts";
	public static final String COUNTRY_HEADER = "country";
	private static final String SECRET_KEY = "secret";
	private static final String JWT_HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

	private JwtGenerator() {

	}

	public static String generateB2C(final String account, final String country) throws JSONException {

		final String encodedHeader = encode(new JSONObject(JWT_HEADER));

		final JSONObject payload = new JSONObject();
		payload.put("extension_accountids", "[" + account + "]");
		payload.put("country", country);
		payload.put("app", "b2b");

		return toString(encodedHeader, payload, hmacSha256(encodedHeader + "." + encode(payload), SECRET_KEY));
	}

	public static String generateM2M(final List<String> roles) throws JSONException {

		final String encodedHeader = encode(new JSONObject(JWT_HEADER));

		final JSONObject payload = new JSONObject();
		final JSONArray rolesJson = new JSONArray();

		for (final String role : roles) {
			rolesJson.put(role);
		}

		payload.put(ROLES, rolesJson);

		return toString(encodedHeader, payload, hmacSha256(encodedHeader + "." + encode(payload), SECRET_KEY));
	}

	public static String generateM2M(final List<String> roles, final String vendorId) throws JSONException {

		final String encodedHeader = encode(new JSONObject(JWT_HEADER));

		final JSONObject payload = new JSONObject();
		final JSONArray rolesJson = new JSONArray();

		for (final String role : roles) {
			rolesJson.put(role);
		}

		payload.put(ROLES, rolesJson);
		payload.put(VENDOR_ID, vendorId);

		return toString(encodedHeader, payload, hmacSha256(encodedHeader + "." + encode(payload), SECRET_KEY));
	}

	public static String generateHMAC(final List<String> accounts, final String country) throws JSONException {

		final String encodedHeader = encode(new JSONObject(JWT_HEADER));

		final JSONObject payload = new JSONObject();
		final JSONArray rolesHMAC = new JSONArray();
		rolesHMAC.put("ROLE_CUSTOMER");

		final JSONArray accountsHMAC = new JSONArray();
		for (final String account : accounts) {
			accountsHMAC.put(account);
		}

		payload.put(ROLES, rolesHMAC);
		payload.put(ACCOUNTS, accountsHMAC);
		payload.put(COUNTRY_HEADER, country);

		return toString(encodedHeader, payload, hmacSha256(encodedHeader + "." + encode(payload), SECRET_KEY));
	}

	private static String encode(final JSONObject obj) {

		return encode(obj.toString().getBytes(StandardCharsets.UTF_8));
	}

	private static String encode(final byte[] bytes) {

		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private static String hmacSha256(final String data, final String secret) {

		try {
			final byte[] hash = secret.getBytes(StandardCharsets.UTF_8);

			final Mac sha256Hmac = Mac.getInstance(HMAC_SHA_256);
			final SecretKeySpec secretKey = new SecretKeySpec(hash, HMAC_SHA_256);
			sha256Hmac.init(secretKey);

			final byte[] signedBytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
			return encode(signedBytes);
		} catch (final NoSuchAlgorithmException | InvalidKeyException ex) {
			Logger.getLogger(JwtGenerator.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
			return null;
		}
	}

	private static String toString(final String encodedHeader, final JSONObject payload, final String signature) {

		return "Bearer " + encodedHeader + "." + encode(payload) + "." + signature;
	}
}
