package helpers;

import static io.restassured.RestAssured.given;
import static java.util.Optional.ofNullable;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;

public class RabbitmqOperations {

	private static final String RABBITMQ_URL_TO_GET_QUEUE_CONTENT = "http://localhost:15672/api/queues/%s/%s/get";
	private static final String RABBITMQ_URL_TO_GET_QUEUE_INFO = "http://localhost:15672/api/queues/%s/%s";
	private static final String RABBITMQ_DEFAULT_MESSAGE_FORMAT = "{\"properties\":{\"headers\":%s},\"routing_key\":\"%s\",\"payload\":\"%s\",\"payload_encoding\":\"string\"}";
	private static final String RABBITMQ_DEFAULT_VHOST = "local-vh-1";
	private static final String RABBITMQ_URL_TO_PUBLISH_INTO_EXCHANGE = "http://localhost:15672/api/exchanges/%s/%s/publish";
	private static final String RABBITMQ_USER = "price";
	private static final String RABBITMQ_PASS = "price";

	private static Map<String, Object> createRestTemplateHeadersForRabbitmqApi() {

		final String auth = RABBITMQ_USER + ":" + RABBITMQ_PASS;
		final byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.US_ASCII));
		final String authHeader = "Basic " + new String(encodedAuth);

		final Map<String, Object> headers = new HashMap<>();
		headers.put("Authorization", authHeader);
		return headers;
	}

	private static Response getQueueInfo(final String queueName) {

		final String url = String.format(RABBITMQ_URL_TO_GET_QUEUE_INFO, RABBITMQ_DEFAULT_VHOST, queueName);
		final Map<String, Object> headers = createRestTemplateHeadersForRabbitmqApi();
		return given().headers(headers).when().request(Method.GET, url);
	}

	private static Response getMessagesFromQueue(final String queueName, final int numberOfMessages) {

		final String url = String.format(RABBITMQ_URL_TO_GET_QUEUE_CONTENT, RABBITMQ_DEFAULT_VHOST, queueName);
		final String payload = String.format("{\"count\":%d,\"ackmode\":\"ack_requeue_false\",\"encoding\":\"auto\"}", numberOfMessages);
		final Map<String, Object> headers = createRestTemplateHeadersForRabbitmqApi();

		return given().body(payload).headers(headers).when().request(Method.POST, url);
	}

	public static void publishMessageIntoExchange(final String exchangeName, final String routingKey, final String messagePayload,
			final String messageHeaders) {

		final String formattedQueuePayload = messagePayload.replace("\"", "\\\"");
		final String url = String.format(RABBITMQ_URL_TO_PUBLISH_INTO_EXCHANGE, RABBITMQ_DEFAULT_VHOST, exchangeName);
		final String payload = String.format(RABBITMQ_DEFAULT_MESSAGE_FORMAT, messageHeaders, routingKey, formattedQueuePayload);
		final Map<String, Object> headers = createRestTemplateHeadersForRabbitmqApi();
		final Response response = given().body(payload).headers(headers).when().request(Method.POST, url);

		final boolean isNotRouted = !Boolean.parseBoolean(
				ofNullable(response.getBody()).map(ResponseBody::jsonPath).map(jsonMap -> jsonMap.get("routed")).map(Object::toString)
						.orElse("false"));

		if (isNotRouted) {
			throw new RuntimeException("Error on put message to exchange!");
		}
	}

	public static long extractNumberOfMessagesFromQueue(final String queueName) {

		final Response response = getQueueInfo(queueName);
		return ofNullable(response.getBody()).map(ResponseBody::jsonPath).map(info -> (Number) info.get("messages")).map(Number::longValue)
				.orElse(0L);
	}

	public static String extractMessagePayloadFromQueue(final String queueName) {

		final Response response = getMessagesFromQueue(queueName, 1);

		return ofNullable(response.getBody()).map(ResponseBody::jsonPath).map(jsonMap -> jsonMap.getList("payload").get(0))
				.map(Object::toString).orElse("");
	}
}
