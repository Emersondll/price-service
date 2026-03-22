package com.abinbev.b2b.price.api.helpers;

public abstract class ApiConstants {

	public static final String API_NAME_PRICES = "Prices API";
	public static final String API_NAME_PRICES_OFFERS = "Prices Offers API";
	public static final String API_PRICES_DESCRIPTION = "Retrieves prices by customer ID. Taxes are not included";
	public static final String API_PRICES_OFFERS_DESCRIPTION = "Retrieves prices offers by customer ID.";
	public static final String HEADER = "header";
	public static final String SUPPORTED_COUNTRIES = "ar,br,co,do,mx,za,ec,pe";
	public static final String ACCOUNT_DESCRIPTION = "AccountID for filtering by account";
	public static final String COUNTRY_DESCRIPTION = "Country/region. ISO 3166 alpha-2 country code";
	public static final String COUNTRY_HEADER = "country";
	public static final String PAGE_HEADER = "page";
	public static final String SIZE_HEADER = "size";
	public static final String ACCOUNT_ID_PARAM = "accountId";
	public static final String REQUEST_TRACE_ID_HEADER = "requestTraceId";
	public static final String REQUEST_TRACE_ID_DESCRIPTION = "Cross transaction unique ID";
	public static final String UNAUTHORIZED_ACCESS = "Unauthorized access";
	public static final String ACCESS_DENIED = "Access denied";
	public static final String API_TITLE = "AB-Inbev B2B Pricing Conditions Service";
	public static final String API_DESCRIPTION = "\"REST API for AB-Inbev Pricing Conditions Service Microservice\"";
	public static final String API_LICENSE = "Anheuser-Busch InBev © ";
	public static final String API_VERSION_1_LABEL = "API v1";
	public static final String API_VERSION_2_LABEL = "API v2";
	public static final String API_VERSION_3_LABEL = "API v3";
	public static final String SKUS_FILTER_HEADER = "skus";
	public static final String SKUS_FILTER_DESCRIPTION = "Set of SKUs to filter e.g: ?skus=SKU0001,SKU0002,SKU0003,..N";
	public static final String VENDOR_ID_DESCRIPTION = "Vendor ID for filtering by vendor";
	public static final String VENDOR_ACCOUNT_ID_DESCRIPTION = "Vendor Account ID for filtering by vendor account";
	public static final String VENDOR_ITEM_ID_DESCRIPTION = "Vendor Item ID for filtering by vendor account item";
	public static final String PAGE_DESCRIPTION = "The page selected";
	public static final String SIZE_DESCRIPTION = "The quantity of how many items per page";
	public static final String AUTHORIZATION_DESCRIPTION = "JWT Token";
	public static final String AUTHORIZATION_HEADER = "authorization";
	public static final String ACCOUNTS = "accounts";
	public static final String SERVICE_NAME_LABEL = "ServiceName";
	public static final String PRICE_SERVICE = "price-service";
	public static final String EXTENSION_ACCOUNTS = "extension_accountids";
	public static final String CLAIM_APP = "app";
	public static final String WHEN_APPLY_ROUNDING_ON_TAXES = "whenApplyRoundingOnTaxes";
	public static final String ROUND_TAXES_AND_DISCOUNT_BEFORE_SUBTOTAL_AND_TOTAL = "roundTaxesAndDiscountBeforeSubtotalAndTotal";
	public static final String PRICE_INCLUDE_DEPOSIT = "priceIncludeDeposit";
	public static final String PRICES_IGNORE_TAX_CONDITION = "pricesIgnoreTaxCondition";
	public static final String TAX_INCLUDE_DEPOSIT = "taxIncludesDeposit";
	public static final String TAX_PERCENT_TYPE = "%";
	public static final String DEFAULT_CONFIGURATION = "default";
	public static final String EXCLUDE_TAXES_FROM_BROWSE_PRICE = "excludeTaxesFromBrowsePrice";
	public static final String EXCLUDE_CHARGE_FROM_BROWSE_PRICE = "excludeChargeFromBrowsePrice";
	public static final String EXCLUDE_CONDITIONAL_TAXES_FROM_BROWSE_PRICE = "excludeConditionalTaxesFromBrowsePrice";
	public static final String ROLES = "roles";
	public static final String ROLE_READ = "READ";
	public static final String ROLE_WRITE = "WRITE";
	public static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";
	public static final String APP_B2B = "b2b";
	public static final String APP_B2B_UPPERCASE = "B2B";
	public static final String TOKEN_INVALID = "This token is invalid.";
	public static final String BEARER_PREFIX = "Bearer";
	public static final String VENDOR_ID_HEADER = "vendorId";
	public static final String VENDOR_ACCOUNT_ID_HEADER = "vendorAccountId";
	public static final String VENDOR_ITEM_IDS_HEADER = "vendorItemIds";
	public static final String DAYS_BEHIND_TO_SHOW_VALID_UNTIL = "daysBehindToShowValidUntil";
	public static final String PRICE_LIST_ID_DESCRIPTION = "PRICE LIST ID for filtering by price list";
	public static final String PRICE_LIST_ID_HEADER = "priceListId";
	public static final String IGNORE_VALID_FROM = "ignoreValidFrom";
	public static final String IGNORE_VALID_FROM_DESCRIPTION = "Filter to ignore field validFrom, if true it will return future prices";

	private ApiConstants() {

		throw new AssertionError();
	}
}
