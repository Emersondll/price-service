package com.abinbev.b2b.price.api.controllers.v3;

import static com.abinbev.b2b.price.api.testhelpers.IntegrationTestsResourceHelper.getRequestDataFileContent;
import static com.abinbev.b2b.price.api.testhelpers.IntegrationTestsResourceHelper.getResponseDataFileContent;
import static com.abinbev.b2b.price.api.testhelpers.TestConstants.MOCKED_JWT_M2M;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.abinbev.b2b.price.api.domain.v3.PriceNormalizedInfo;
import com.abinbev.b2b.price.api.services.v3.SearchForPricesAndFillIntoPriceNormalizedInfoServiceV3;
import com.abinbev.b2b.price.domain.model.v2.ChargeV2;
import com.abinbev.b2b.price.domain.model.v2.PriceCompoundKeyV2;
import com.abinbev.b2b.price.domain.model.v2.PriceEntityV2;
import com.abinbev.b2b.price.domain.model.v2.PromotionalPriceV2;
import com.abinbev.b2b.price.domain.model.v2.TaxConditionV2;
import com.abinbev.b2b.price.domain.model.v2.TaxOrderSubTotalV2;
import com.abinbev.b2b.price.domain.model.v2.TaxV2;
import com.abinbev.b2b.price.domain.model.v2.enums.PriceMeasureUnitType;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(value = "classpath:application-integration-tests.yml")
class PriceControllerV3IntegrationTest {

	private static final String FILES_PATH = "get_prices_v3";

	private static final String ENDPOINT = "/v3";

	private static final String COUNTRY_HEADER = "country";
	private static final String REQUEST_TRACE_ID_HEADER = "requestTraceId";
	private static final String AUTHORIZATION_HEADER = "authorization";

	private static final String COUNTRY_BR = "BR";
	private static final String REQUEST_TRACE_ID = "4d11e925-9f21-4b35-a2ad-1fe16a6094b4";

	@Autowired
	private MockMvc mvc;

	@MockBean
	private SearchForPricesAndFillIntoPriceNormalizedInfoServiceV3 searchForPricesAndFillIntoPriceNormalizedInfoServiceV3;

	@Test
	@DisplayName("[POST /v3] 200 - Should return status 200 with found prices when database return with success a list of prices")
	void shouldReturnStatus200WithFoundPricesWhenDatabaseReturnWithSuccessAListOfPrices() throws Exception {

		// Given
		doAnswer(invocation -> {
			final List<PriceNormalizedInfo> priceNormalizedInfoList = invocation.getArgument(0);
			final PriceNormalizedInfo priceNormalizedInfo = priceNormalizedInfoList.get(0);
			priceNormalizedInfo.setSelectedPrice(mockPriceEntityV2());

			return List.of(priceNormalizedInfo);
		}).when(searchForPricesAndFillIntoPriceNormalizedInfoServiceV3).execute(anyList(), anyString(), any(Boolean.class));

		//When / Then
		mvc.perform(post(ENDPOINT).header(COUNTRY_HEADER, COUNTRY_BR)
						.header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)
						.content(getRequestData("get-prices-full-request.json"))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", equalTo(getResponseData("success-get-prices-response.json"))));

	}

	@Test
	@DisplayName("[POST /v3] 200 - Should return status 200 with found prices when database return with success a list of prices with proportional tax")
	void shouldReturnStatus200WithFoundPricesWhenDatabaseReturnWithSuccessAListOfPricesWithProportionalTax() throws Exception {

		// Given
		doAnswer(invocation -> {
			final List<PriceNormalizedInfo> priceNormalizedInfoList = invocation.getArgument(0);
			final PriceNormalizedInfo priceNormalizedInfo = priceNormalizedInfoList.get(0);
			priceNormalizedInfo.setSelectedPrice(mockPriceEntityV2());
			priceNormalizedInfo.getSelectedPrice().getTaxes().get("TAX_1").setType("$");
			priceNormalizedInfo.getSelectedPrice().getTaxes().get("TAX_1").setProportional(true);

			return List.of(priceNormalizedInfo);
		}).when(searchForPricesAndFillIntoPriceNormalizedInfoServiceV3).execute(anyList(), anyString(), any(Boolean.class));

		//When / Then
		mvc.perform(post(ENDPOINT).header(COUNTRY_HEADER, COUNTRY_BR)
						.header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)
						.content(getRequestData("get-prices-full-request.json"))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", equalTo(getResponseData("success-get-prices-response-with-proportional-tax.json"))));

	}

	@Test
	@DisplayName("[POST /v3] 204 - Should return status 204 when list of found prices is empty")
	void shouldReturnStatus204WhenListOfFoundPricesIsEmpty() throws Exception {

		doReturn(new ArrayList<>())
				.when(searchForPricesAndFillIntoPriceNormalizedInfoServiceV3).execute(anyList(), anyString(), anyBoolean());

		//When / Then
		mvc.perform(post(ENDPOINT).header(COUNTRY_HEADER, COUNTRY_BR)
						.header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)
						.content(getRequestData("get-prices-full-request-for-list-of-found-price-is-empty.json"))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());

	}

	@Test
	@DisplayName("[POST /v3] 403 - Should return status 400 when receive invalid Authorization token")
	void shouldReturnStatus400WhenReceiveInvalidAuthorizationToken() throws Exception {

		// Given
		final String invalidAuthorization = "Bearer INVALID";

		//When / Then
		mvc.perform(post(ENDPOINT).header(COUNTRY_HEADER, COUNTRY_BR)
						.header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)
						.header(AUTHORIZATION_HEADER, invalidAuthorization)
						.content(getRequestData("get-prices-request-only-one-itemId.json"))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden());

	}

	@Test
	@DisplayName("[POST /v3] 400 - Should return status 400 when RequestTraceId header is missing")
	void shouldReturnStatus400WhenRequestTraceIdIsMissing() throws Exception {

		//When / Then
		mvc.perform(post(ENDPOINT).header(COUNTRY_HEADER, COUNTRY_BR)
						.header(AUTHORIZATION_HEADER, MOCKED_JWT_M2M)
						.content(getRequestData("get-prices-request-only-one-itemId.json"))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseData("failed-response-request-trace-id-header-missing.json"))));

	}

	@Test
	@DisplayName("[POST /v3] 400 - Should return status 400 when Country header is missing")
	void shouldReturnStatus400WhenCountryIsMissing() throws Exception {

		//When / Then
		mvc.perform(post(ENDPOINT).header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)
						.header(AUTHORIZATION_HEADER, MOCKED_JWT_M2M)
						.content(getRequestData("get-prices-request-only-one-itemId.json"))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseData("failed-response-country-header-missing.json"))));

	}

	@Test
	@DisplayName("[POST /v3] 400 - Should return status 400 when Country header is invalid")
	void shouldReturnStatus400WhenCountryIsInvalid() throws Exception {

		//When / Then
		mvc.perform(post(ENDPOINT).header(COUNTRY_HEADER, "INVALID")
						.header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)
						.header(AUTHORIZATION_HEADER, MOCKED_JWT_M2M)
						.content(getRequestData("get-prices-request-only-one-itemId.json"))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseData("failed-response-received-invalid-country-header.json"))));

	}

	@Test
	@DisplayName("[POST /v3] 400 - Should return status 400 when body is invalid - Payload without itemId")
	void shouldReturnStatus400WhenBodyIsInvalidContractIdWithoutItemId() throws Exception {

		//When / Then
		mvc.perform(post(ENDPOINT).header(COUNTRY_HEADER, COUNTRY_BR)
						.header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)
						.header(AUTHORIZATION_HEADER, MOCKED_JWT_M2M)
						.content(getRequestData("get-prices-invalid-request-without-itemId.json"))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseData("failed-response-payload-without-itemId.json"))));

	}

	@Test
	@DisplayName("[POST /v3] 400 - Should return status 400 when body is invalid - Error decoding itemId")
	void shouldReturnStatus400WhenBodyIsInvalidErrorDecodingItemId() throws Exception {

		//When / Then
		mvc.perform(post(ENDPOINT).header(COUNTRY_HEADER, COUNTRY_BR)
						.header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)
						.header(AUTHORIZATION_HEADER, MOCKED_JWT_M2M)
						.content(getRequestData("get-prices-request-invalid-itemId.json"))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseData("failed-response-error-decoding-itemId.json"))));

	}

	@Test
	@DisplayName("[POST /v3] 400 - Should return status 400 when body is invalid - Error decoding contractId")
	void shouldReturnStatus400WhenBodyIsInvalidErrorDecodingContractId() throws Exception {

		//When / Then
		mvc.perform(post(ENDPOINT).header(COUNTRY_HEADER, COUNTRY_BR)
						.header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)
						.header(AUTHORIZATION_HEADER, MOCKED_JWT_M2M)
						.content(getRequestData("get-prices-request-invalid-contractId.json"))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseData("failed-response-error-decoding-contractId.json"))));

	}

	@Test
	@DisplayName("[POST /v3] 400 - Should return status 400 when body is invalid - Error decoding deliveryCenterId")
	void shouldReturnStatus400WhenBodyIsInvalidErrorDecodingDeliveryCenterId() throws Exception {

		//When / Then
		mvc.perform(post(ENDPOINT).header(COUNTRY_HEADER, COUNTRY_BR)
						.header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)
						.header(AUTHORIZATION_HEADER, MOCKED_JWT_M2M)
						.content(getRequestData("get-prices-request-invalid-deliveryCenterId.json"))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseData("failed-response-error-decoding-deliveryCenterId.json"))));

	}

	@Test
	@DisplayName("[POST /v3] 400 - Should return status 400 when body is invalid - PriceListId without contractId")
	void shouldReturnStatus400WhenBodyIsInvalidPriceListIdWithoutContractId() throws Exception {

		//When / Then
		mvc.perform(post(ENDPOINT).header(COUNTRY_HEADER, COUNTRY_BR)
						.header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)
						.header(AUTHORIZATION_HEADER, MOCKED_JWT_M2M)
						.content(getRequestData("get-prices-request-invalid-priceListId-without-contractId.json"))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseData("failed-response-invalid-body-priceListId-without-contractId.json"))));

	}

	@Test
	@DisplayName("[POST /v3] 400 - Should return status 400 when body is an empty array - must be a nonempty array")
	void shouldReturnStatus400WhenBodyIsEmptyPriceList() throws Exception {

		//When / Then
		mvc.perform(post(ENDPOINT).header(COUNTRY_HEADER, COUNTRY_BR)
						.header(REQUEST_TRACE_ID_HEADER, REQUEST_TRACE_ID)
						.header(AUTHORIZATION_HEADER, MOCKED_JWT_M2M)
						.content(getRequestData("get-prices-empty-array.json"))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$", equalTo(getResponseData("failed-response-invalid-body-with-empty-array.json"))));

	}

	private String getRequestData(final String fileName) throws IOException {

		return getRequestDataFileContent(FILES_PATH, fileName);
	}

	private Object getResponseData(final String fileName) throws IOException {

		return getResponseDataFileContent(FILES_PATH, fileName);
	}

	private PriceEntityV2 mockPriceEntityV2() {

		final PriceEntityV2 priceEntityV2 = new PriceEntityV2();

		final PriceCompoundKeyV2 id = new PriceCompoundKeyV2();
		id.setVendorItemId("VENDOR_ITEM_1");
		id.setValidFrom(Instant.ofEpochMilli(1649214000000L));

		priceEntityV2.setId(id);
		priceEntityV2.setVendorItemId("VENDOR_ITEM_1");
		priceEntityV2.setSku("SKU_1");
		priceEntityV2.setTimezone("America/Sao_Paulo");

		priceEntityV2.setBasePrice(BigDecimal.valueOf(20.99));
		priceEntityV2.setMinimumPrice(BigDecimal.valueOf(5.99));
		priceEntityV2.setMeasureUnit("CS");
		priceEntityV2.setType(PriceMeasureUnitType.PER_UNIT);
		priceEntityV2.setDeposit(BigDecimal.valueOf(3.99));
		priceEntityV2.setConsignment(BigDecimal.valueOf(0.75));
		priceEntityV2.setQuantityPerPallet(10);
		priceEntityV2.setTimezone("America/Sao_Paulo");
		priceEntityV2.setValidFrom("2022-04-06");
		priceEntityV2.setTaxes(mockTaxes());
		priceEntityV2.setPromotionalPrice(mockPromotionalPrice());
		priceEntityV2.setCharges(mockCharges());

		return priceEntityV2;
	}

	private Map<String, ChargeV2> mockCharges() {

		final ChargeV2 charge = new ChargeV2();
		charge.setChargeId("CHARGE_01");
		charge.setType("$");
		charge.setValue(BigDecimal.valueOf(11.77));
		charge.setBase(BigDecimal.valueOf(1.11));

		return Map.of("CHARGE_01", charge);
	}

	private PromotionalPriceV2 mockPromotionalPrice() {

		final PromotionalPriceV2 promotionalPrice = new PromotionalPriceV2();
		promotionalPrice.setPrice(BigDecimal.valueOf(9.99));
		promotionalPrice.setExternalId("EXTERNAL_1");
		promotionalPrice.setValidUntil("2023-04-06");

		return promotionalPrice;
	}

	private Map<String, TaxV2> mockTaxes() {

		final TaxOrderSubTotalV2 taxOrderSubTotal = new TaxOrderSubTotalV2();
		taxOrderSubTotal.setMinimumValue(BigDecimal.valueOf(2000.99));

		final TaxConditionV2 taxCondition = new TaxConditionV2();
		taxCondition.setOrderSubTotal(taxOrderSubTotal);

		final TaxV2 tax = new TaxV2();
		tax.setTaxId("TAX_1");
		tax.setType("%");
		tax.setValue(BigDecimal.valueOf(10.99));
		tax.setTaxBaseInclusionIds(Collections.singletonList("TAX_2"));
		tax.setHidden(false);
		tax.setBase(BigDecimal.valueOf(2.99));
		tax.setConditions(taxCondition);

		return Map.of("TAX_1", tax);
	}
}