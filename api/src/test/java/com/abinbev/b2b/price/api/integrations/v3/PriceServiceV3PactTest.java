package com.abinbev.b2b.price.api.integrations.v3;

import static java.nio.charset.Charset.defaultCharset;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import com.abinbev.b2b.price.api.repository.PriceEntityV2Repository;
import com.abinbev.b2b.price.domain.model.v2.PriceEntityV2;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.loader.PactBroker;
import au.com.dius.pact.provider.junit.loader.PactBrokerAuth;
import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;

@PactBroker(scheme = "https", host = "ab-inbev.pactflow.io", authentication = @PactBrokerAuth(token = "${PACT_TOKEN}"))
@Provider("GetPricesV3Service")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = PriceApiApplication.class)
@EnabledIfSystemProperty(named = "execute-contract-tests", matches = "true")
class PriceServiceV3PactTest {

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

    @State("Item id exists and has prices for deliveryCenterId and contractId")
    public void pricesWithDeliveryCenterIdAndContractId() throws IOException {

        final List<PriceEntityV2> priceResultList = prepareMock("pact-response-data/v3/MockedPriceResponseFromDeliveryCenterIdAndContractId.json");
        when(priceRepositoryV2.findPricesByAccountPriceForContractAndContractlessList(any(List.class), anyString(), any(), anyBoolean()))
                .thenReturn(priceResultList);
    }

    @State("Invalid header")
    public void invalidHeader() {
        final List<PriceEntityV2> priceResultList = new ArrayList<>();
        when(priceRepositoryV2.findPricesByAccountPriceForContractAndContractlessList(any(List.class), anyString(), any(), anyBoolean()))
                .thenReturn(priceResultList);
    }

    @State("None price was found")
    public void nonePriceWasFound() {
        final List<PriceEntityV2> priceResultList = new ArrayList<>();
        when(priceRepositoryV2.findPricesByAccountPriceForContractAndContractlessList(any(List.class), anyString(), any(), anyBoolean()))
                .thenReturn(priceResultList);
    }

    private List<PriceEntityV2> prepareMock(final String fileName) throws IOException {

        final String resource = StreamUtils.copyToString(new ClassPathResource(fileName).getInputStream(), defaultCharset());
        return objectMapper.readValue(resource, new TypeReference<>() {
        });
    }
}
