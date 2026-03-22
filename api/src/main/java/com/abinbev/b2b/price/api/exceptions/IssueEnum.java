package com.abinbev.b2b.price.api.exceptions;

import java.util.IllegalFormatException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum IssueEnum {

	// Note: the comments are placed just to prevent auto formatting add
	// the constants to the same line.
	// General error codes 1~50.
	BAD_REQUEST(1, "Malformed Request"), //
	REQUEST_HEADER_NOT_VALID(2, "The header %s '%s' is not valid"), //
	REQUIRED_HEADER_MISSING(3, "Required header '%s' is missing"), //
	JSON_DESERIALIZE_ERROR(4, "Can not deserialize JSON."), //
	METHOD_NOT_ALLOWED(5, "%s method is not supported for this request. Supported methods are [%s]"), //
	REQUEST_PARAM_NOT_VALID(6, "The param %s '%s' is not valid"), //
	UNEXPECTED_ERROR(7, "Unexpected error. Please contact system administrator."), //
	PRICES_NOT_FOUND(8, "No Price found for account '%s' and country '%s'"), //
	PRICES_NOT_FOUND_V2(9, "No Price found for vendor '%s', vendor account '%s' and country '%s'"), //
	PRICES_WITH_PRICE_LIST_ID_NOT_FOUND_V2(24, "No Price found for vendor '%s', vendor account '%s' or price list '%s' and country '%s'"),
	INVALID_PAGE_SIZE(14, "Page size should be greater than zero."), //
	INVALID_PAGE_NUMBER(15, "Page number can not be negative."), //
	PRICING_CONFIGURATION_NOT_FOUND(17, "Could not retrieve pricing configuration for country '%s'"), //
	JWT_DECODE_ERROR(4, "Error while decoding the token"), //
	JWT_TOKEN_INVALID(5, "Invalid Token"), //
	MISSING_ACCOUNT_ID_OR_VENDOR_ACCOUNT_ID_HEADER(20, "Please provide accountId or vendorAccountId header."), //
	MISMATCHED_ACCOUNT_ID_AND_VENDOR_ACCOUNT_ID_HEADER(21, "The values of accountId and vendorAccountId cannot be different."), //
	MISMATCHED_SKUS_AND_VENDOR_ITEM_IDS_HEADER(22, "The values of skus and vendorItemIds cannot be different."), //
	DATABASE_ACCESS_ERROR(23, "Database access failed.  Please contact system administrator."), //
	REQUEST_DECODING_ERROR(24, "Error decoding '%s' from request body"), //
	CONTRACT_ID_IS_REQUIRED_FOR_PRICE_LIST(25, "The contractId is required for Price List."), //
	EXCEEDED_LIMIT_OF_ITEMS_REQUEST(26, "Exceeded limit of %d items."), //
	REQUEST_BODY_MUST_BE_A_NONEMPTY_ARRAY(27, "The request body must be a nonempty array."); //

	// not static because ENUMS are initialized before static fields by JVM
	private final Logger logger = LoggerFactory.getLogger(IssueEnum.class);
	private final int code;
	private final String message;

	IssueEnum(final int code, final String message) {

		this.code = code;
		this.message = message;
	}

	public int getCode() {

		return code;
	}

	public String getFormattedMessage(final Object... args) {

		if (message == null) {
			return "";
		}

		try {
			return String.format(message, args);
		} catch (final IllegalFormatException e) {
			logger.warn(e.getMessage(), e);
			return message.replace("%s", "");
		}
	}
}
