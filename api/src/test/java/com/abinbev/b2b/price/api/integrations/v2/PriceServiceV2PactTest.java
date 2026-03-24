package com.abinbev.b2b.price.api.integrations.v2;

import static java.nio.charset.Charset.defaultCharset;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.StreamUtils;

import com.abinbev.b2b.price.api.PriceApiApplication;
import com.abinbev.b2b.price.api.domain.v2.PriceResultListV2;
import com.abinbev.b2b.price.api.repository.PriceEntityV2Repository;
import com.fasterxml.jackson.databind.ObjectMapper;

import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.loader.PactBroker;
import au.com.dius.pact.provider.junit.loader.PactBrokerAuth;
import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;

@PactBroker(scheme = "https", host = "ab-inbev.pactflow.io", authentication = @PactBrokerAuth(token = "${PACT_TOKEN}"))
@Provider("PriceServiceV2")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = PriceApiApplication.class)
@EnabledIfSystemProperty(named = "execute-contract-tests", matches = "true")
class PriceServiceV2PactTest {

	ObjectMapper objectMapper = new ObjectMapper();

	@LocalServerPort
	private int randomServerPort;

	@MockBean
	private PriceEntityV2Repository priceRepositoryV2;

	@BeforeEach
	public void setupTestTarget(final PactVerificationContext context) {

		context.setTarget(new HttpTestTarget("localhost", randomServerPort));
	}

	@TestTemplate
	@ExtendWith(PactVerificationInvocationContextProvider.class)
	void pactVerificationTestTemplate(final PactVerificationContext context) {

		context.verifyInteraction();
	}

	@State("Vendor account is not found")
	public void priceNotFoundResponse() {

		when(priceRepositoryV2.findPriceByIdFilteringVendorItemId(any(), any(), any(), any(), any(), any(), isNull()))
				.thenReturn(new PriceResultListV2());
	}

	@State("Vendor Account exists and has a price with taxes")
	public void priceWithTaxesFoundResponse() throws IOException {

		final PriceResultListV2 priceResultList = prepareMock("pact-response-data/v2/MockedPriceResponseWithTaxes.json");
		when(priceRepositoryV2.findPriceByIdFilteringVendorItemId(any(), any(), any(), any(), any(), any(), isNull()))
				.thenReturn(priceResultList);
	}

	@State("Vendor Account exists and has a price with Charges")
	public void priceWithChargesFoundResponse() throws IOException {

		final PriceResultListV2 priceResultList = prepareMock("pact-response-data/v2/MockedPriceResponseWithCharges.json");
		when(priceRepositoryV2.findPriceByIdFilteringVendorItemId(any(), any(), any(), any(), any(), any(), isNull()))
				.thenReturn(priceResultList);
	}

	@State("Vendor Account exists and has a price with Promotional Price")
	public void priceWithPromotionalPriceFoundResponse() throws IOException {

		final PriceResultListV2 priceResultList = prepareMock("pact-response-data/v2/MockedPriceResponseWithPromotionalPrice.json");
		when(priceRepositoryV2.findPriceByIdFilteringVendorItemId(any(), any(), any(), any(), any(), any(), isNull()))
				.thenReturn(priceResultList);
	}

	@State("Vendor Account exists and has two full prices filtered by vendor item id")
	public void fullPricesFoundFilteredByVendorItemIdResponse() throws IOException {

		final PriceResultListV2 priceResultList = prepareMock("pact-response-data/v2/MockedPriceResponseWithTwoFullPrices.json");
		when(priceRepositoryV2.findPriceByIdFilteringVendorItemId(any(), any(), any(), any(), any(), any(), isNull()))
				.thenReturn(priceResultList);
	}

	private PriceResultListV2 prepareMock(final String fileName) throws IOException {

		final String resource = StreamUtils.copyToString(new ClassPathResource(fileName).getInputStream(), defaultCharset());
		return objectMapper.readValue(resource, PriceResultListV2.class);
	}
}
