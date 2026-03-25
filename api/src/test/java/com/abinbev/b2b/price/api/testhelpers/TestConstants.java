package com.abinbev.b2b.price.api.testhelpers;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class TestConstants {

	public static final Integer MOCKED_ITEM_QUANTITY = 2;
	public static final String MOCKED_MEASURE_UNIT = "CASE";
	public static final String MOCKED_COUNTRY_B2B_APP_NAME = "b2b";
	public static final String MOCKED_COUNTRY_BR = "br";
	public static final String MOCKED_COUNTRY_AR = "ar";
	public static final String MOCKED_COUNTRY_ZA = "za";
	public static final String MOCKED_SKU_1 = "0010";
	public static final String MOCKED_SKU_2 = "0020";
	public static final String MOCKED_VENDOR_ITEM_1 = "0010";
	public static final String MOCKED_VENDOR_ITEM_2 = "0020";
	public static final String MOCKED_VENDOR_ITEM_3 = "0030";
	public static final String MOCKED_TIME_ZONE_ID = "Africa/Johannesburg";
	public static final String MOCKED_PARAM_ACCOUNT_ID = "accountId";
	public static final String MOCKED_ACCOUNT_ID = "ACCOUNT_ID_1";
	public static final String MOCKED_EXTERNAL_ID = "EXTERNAL_ID_1";
	public static final String TAX_ID = "TAX_1";
	public static final String PERCENT_TYPE = "%";
	public static final BigDecimal MOCKED_DEPOSIT = BigDecimal.valueOf(8.5463);
	public static final BigDecimal MOCKED_CONSIGNMENT = BigDecimal.valueOf(2.450);
	public static final BigDecimal MOCKED_BASE_PRICE = BigDecimal.valueOf(10);
	public static final BigDecimal MOCKED_MINIMUM_PRICE = BigDecimal.valueOf(0);
	public static final Long MOCKED_TIMESTAMP = 1000L;
	public static final String CHARGE = "CHARGE_";
	public static final String MOCKED_VALID_UNTIL = "2021/03/31";
	public static final String MOCKED_EXTENSION_ACCOUNT_IDS = "[27431273000147,26812643000123,38904063949,15128458000106,52299325115,ACCOUNT_ID_1]";
	public static final String MOCKED_EXTENSION_ACCOUNT_IDS_2 = "[27431273000147,26812643000123,38904063949,15128458000106,52299325115]";
	public static final String MOCKED_EXTENSION_ACCOUNT_IDS_3 = "[27431273000147,26812643000123,38904063949,15128458000106,52299325115,12345678901234,ACCOUNT_ID_1]";
	public static final List<String> MOCKED_ACCOUNTS = List.of("12451517", "12451515", "9980890901", "12451514", "9717834400","27431273000147");
	public static final List<String> MOCKED_ACCOUNTS_2 = List.of("12451517","12451515","9980890901","27431273000147");
	public static final String MOCKED_EMPTY_JWT = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.e30.yXvILkvUUCBqAFlAv6wQ1Q-QRAjfe3eSosO949U73Vo";
	public static final String MOCKED_JWT_B2B = JwtBuilder.getBuilder().withExtensionAccountIds(MOCKED_EXTENSION_ACCOUNT_IDS).withCountry(MOCKED_COUNTRY_BR).withApp(MOCKED_COUNTRY_B2B_APP_NAME).build();
	public static final String MOCKED_JWT_INVALID_COUNTRY_B2B = JwtBuilder.getBuilder().withExtensionAccountIds(MOCKED_EXTENSION_ACCOUNT_IDS).withCountry("INVALID_COUNTRY").withApp(MOCKED_COUNTRY_B2B_APP_NAME).build();
	public static final String MOCKED_JWT_B2B_ZA_COUNTRY = JwtBuilder.getBuilder().withExtensionAccountIds(MOCKED_EXTENSION_ACCOUNT_IDS_3).withCountry(MOCKED_COUNTRY_ZA).withApp(MOCKED_COUNTRY_B2B_APP_NAME).build();
	public static final String MOCKED_JWT_B2B_INVALID_ACCOUNT = JwtBuilder.getBuilder().withExtensionAccountIds("[TESTE]").withCountry(MOCKED_COUNTRY_ZA).withApp(MOCKED_COUNTRY_B2B_APP_NAME).build();
	public static final String MOCKED_JWT_B2B_WITHOUT_EXTENSION_IDS =  JwtBuilder.getBuilder().withExtensionAccountIds("[]").withCountry(MOCKED_COUNTRY_ZA).withApp(MOCKED_COUNTRY_B2B_APP_NAME).build();
	public static final String MOCKED_JWT_INVALID_APP_B2B =  JwtBuilder.getBuilder().withExtensionAccountIds(MOCKED_EXTENSION_ACCOUNT_IDS_2).withCountry(MOCKED_COUNTRY_ZA).withApp("mock_app").build();
	public static final String MOCKED_JWT_WITHOUT_APP_B2B = JwtBuilder.getBuilder().withExtensionAccountIds(MOCKED_EXTENSION_ACCOUNT_IDS_2).withCountry(MOCKED_COUNTRY_BR).build();
	public static final String MOCKED_JWT_WITHOUT_EXTENSION_ACCOUNTS_B2B = JwtBuilder.getBuilder().withCountry(MOCKED_COUNTRY_BR).withApp(MOCKED_COUNTRY_B2B_APP_NAME).build();
	public static final String MOCKED_JWT_WITHOUT_COUNTRY_B2B = JwtBuilder.getBuilder().withExtensionAccountIds(MOCKED_EXTENSION_ACCOUNT_IDS_2).withApp(MOCKED_COUNTRY_B2B_APP_NAME).build();
	public static final String MOCKED_JWT_HMAC = JwtBuilder.getBuilder().withRoles(Arrays.asList("ROLE_CUSTOMER")).withAccounts(MOCKED_ACCOUNTS).build();
	public static final String MOCKED_JWT_WITHOUT_CUSTOMER_ROLE_HMCA = JwtBuilder.getBuilder().withAccounts(MOCKED_ACCOUNTS_2).build();
	public static final String MOCKED_JWT_M2M = JwtBuilder.getBuilder().withRoles(Arrays.asList("Read", "Write")).build();
	public static final String MOCKED_JWT_JUST_READ_M2M = JwtBuilder.getBuilder().withRoles(Arrays.asList("Read")).build();
	public static final String MOCKED_JWT_JUST_WRITE_M2M = JwtBuilder.getBuilder().withRoles(Arrays.asList("Write")).build();
	public static final String MOCKED_JWT_WITH_SUPPORTED_APP_ONCUSTOMER =  JwtBuilder.getBuilder().withCountry(MOCKED_COUNTRY_BR).withApp("oncustomer").build();
	public static final String MOCKED_JWT_WITH_UNSUPPORTED_APP = JwtBuilder.getBuilder().withCountry(MOCKED_COUNTRY_BR).withApp("collApp").build();;
	public static final String JWT_INVALID = "Bearer ABCD";
	public static final String MOCKED_REQUEST_TRACE_ID = "654546-545465-564564";
	public static final String REQUEST_TRACE_ID_HEADER_NAME = "requestTraceId";
	public static final String COUNTRY_HEADER_NAME = "country";
	public static final String AUTHORIZATION_HEADER_NAME = "Authorization";
	public static final String ACCOUNT_ID_PARAM_NAME = "accountId";
	public static final String MOCKED_VENDOR_ID = "VENDOR_ID_1";
	public static final String MOCKED_VENDOR_ACCOUNT_ID = "VENDOR_ACCOUNT_ID_1";
	public static final String MOCKED_PRICE_LIST_ID = "PRICE_LIST_ID_1";
	public static final String MOCKED_VENDOR_ITEM_ID_1 = "0010";
	public static final String MOCKED_VENDOR_ITEM_ID_2 = "0020";
	public static final String MOCKED_COUNTRY_US = "us";
	public static final String MOCKED_TIME_ZONE_ID_NY = "America/New_York";
	public static final String MOCKED_TIME_ZONE_ID_SP = "America/Sao_Paulo";
	public static final String MOCKED_VALID_UNTIL_US = "2099/05/15";

	private TestConstants() {

	}
}
