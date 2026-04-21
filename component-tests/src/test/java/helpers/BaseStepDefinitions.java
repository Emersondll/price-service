package helpers;

import static helpers.JsonLoader.getRequestDataFileContent;
import static io.restassured.RestAssured.given;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import context.TestContext;
import context.TestContextService;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class BaseStepDefinitions {

	protected static String getValueAsString(final Object value) {

		final ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
		mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
		try {
			return mapper.writeValueAsString(value);
		} catch (final JsonProcessingException e) {
			throw new RuntimeException("Fail to write value as String: " + e.getMessage());
		}
	}

	protected TestContext getTestContext() {

		return TestContextService.getInstance().getTestContext();
	}

	protected Response doRequest(final TestContext testContext) throws IOException {

		final String payloadFileName = testContext.getPayloadFileName();
		final Map<String, Object> headers = testContext.getRequestHeaders();
		final Map<String, Object> params = testContext.getRequestParams();

		final RequestSpecification request = given();
		if (isNotEmpty(payloadFileName)) {
			request.body(getRequestDataFileContent(testContext, payloadFileName));
		}
		if (isNotEmpty(headers)) {
			request.headers(headers);
		}
		if (isNotEmpty(params)) {
			request.params(params);
		}
		return request.when().request(testContext.getRequestMethod(), testContext.getRequestEndpoint());
	}

	protected void publishMessageIntoExchange(final String exchangeName, final String routingKey) {

		RabbitmqOperations
				.publishMessageIntoExchange(exchangeName, routingKey, getTestContext().getQueueMessagePayload(),
						getTestContext().getQueueMessageHeaders());
	}

	protected long extractNumberOfMessagesFromQueue(final String queueName) {

		return RabbitmqOperations.extractNumberOfMessagesFromQueue(queueName);
	}

	protected String extractMessagePayloadFromQueue(final String queueName) {

		return RabbitmqOperations.extractMessagePayloadFromQueue(queueName);
	}
}
