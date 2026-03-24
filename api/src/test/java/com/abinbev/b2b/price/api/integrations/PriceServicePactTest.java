package com.abinbev.b2b.price.api.integrations;

import static java.nio.charset.Charset.defaultCharset;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import com.abinbev.b2b.price.api.domain.PriceResultList;
import com.abinbev.b2b.price.api.repository.PriceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.loader.PactBroker;
import au.com.dius.pact.provider.junit.loader.PactBrokerAuth;
import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;

@Disabled
@PactBroker(scheme = "https", host = "ab-inbev.pactflow.io", authentication = @PactBrokerAuth(token = "${PACT_TOKEN}"))
@Provider("PriceService")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = PriceApiApplication.class)
@EnabledIfSystemProperty(named = "execute-contract-tests", matches = "true")
class PriceServicePactTest {

	ObjectMapper objectMapper = new ObjectMapper();

	@LocalServerPort
	private int randomServerPort;

	@MockBean
	private PriceRepository priceDao;

	@BeforeEach
	public void setupTestTarget(final PactVerificationContext context) {

		context.setTarget(new HttpTestTarget("localhost", randomServerPort));
	}

	@TestTemplate
	@ExtendWith(PactVerificationInvocationContextProvider.class)
	void pactVerificationTestTemplate(final PactVerificationContext context) {

		context.verifyInteraction();
	}

	@State("Account exists and has one price")
	public void successfulResponse() throws IOException {

		final PriceResultList priceResultList = prepareMock("pact-response-data/v1/MockedPriceResponseWithOnePrice.json");
		when(priceDao.findPriceByIdFilteringSkus(any(), any(), any(), any(), anyBoolean(), any())).thenReturn(priceResultList);
	}

	@State("Account exists and has prices filtered by skus")
	public void successfulResponseFilteringSku() throws IOException {

		final PriceResultList priceResultList = prepareMock("pact-response-data/v1/MockedPriceResponseWithTwoPrices.json");
		when(priceDao.findPriceByIdFilteringSkus(any(), any(), any(), any(), anyBoolean(), any())).thenReturn(priceResultList);
	}

	@State("Account is not found")
	public void callApiWithNotFoundAccountId() {

		when(priceDao.findPriceByIdFilteringSkus(any(), any(), any(), any(), anyBoolean(), any())).thenReturn(null);
	}

	@State("Account exists and has one price with valid until")
	public void successfulResponseWithValidUntil() throws IOException {

		final PriceResultList priceResultList = prepareMock("pact-response-data/v1/MockedPriceResponseWithOnePriceAndValidUntil.json");
		when(priceDao.findPriceByIdFilteringSkus(any(), any(), any(), any(), anyBoolean(), any())).thenReturn(priceResultList);
	}

	private PriceResultList prepareMock(final String fileName) throws IOException {

		final String resource = StreamUtils.copyToString(new ClassPathResource(fileName).getInputStream(), defaultCharset());
		return objectMapper.readValue(resource, PriceResultList.class);
	}
}
