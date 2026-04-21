package context;

import java.util.LinkedHashMap;

import io.restassured.http.Method;
import io.restassured.response.Response;

public class TestContext {

	private String folder;
	private String payloadFileName;
	private LinkedHashMap<String, Object> requestHeaders;
	private LinkedHashMap<String, Object> requestParams;
	private Response response;
	private Method requestMethod;
	private String requestEndpoint;
	private String queueMessagePayload;
	private String queueMessageHeaders;
	private Integer validUntilDaysAhead;
	private String validFrom;
	private String payloadAttributeValidFrom;

	public String getFolder() {

		return folder;
	}

	public void setFolder(final String folder) {

		this.folder = folder;
	}

	public String getPayloadFileName() {

		return payloadFileName;
	}

	public void setPayloadFileName(final String payloadFileName) {

		this.payloadFileName = payloadFileName;
	}

	public LinkedHashMap<String, Object> getRequestHeaders() {

		return requestHeaders;
	}

	public void setRequestHeaders(final LinkedHashMap<String, Object> requestHeaders) {

		this.requestHeaders = requestHeaders;
	}

	public LinkedHashMap<String, Object> getRequestParams() {

		return requestParams;
	}

	public void setRequestParams(final LinkedHashMap<String, Object> requestParams) {

		this.requestParams = requestParams;
	}

	public Response getResponse() {

		return response;
	}

	public void setResponse(final Response response) {

		this.response = response;
	}

	public Method getRequestMethod() {

		return requestMethod;
	}

	public void setRequestMethod(final Method requestMethod) {

		this.requestMethod = requestMethod;
	}

	public String getRequestEndpoint() {

		return requestEndpoint;
	}

	public void setRequestEndpoint(final String requestEndpoint) {

		this.requestEndpoint = requestEndpoint;
	}

	public String getQueueMessagePayload() {

		return queueMessagePayload;
	}

	public void setQueueMessagePayload(final String queueMessagePayload) {

		this.queueMessagePayload = queueMessagePayload;
	}

	public String getQueueMessageHeaders() {

		return queueMessageHeaders;
	}

	public void setQueueMessageHeaders(final String queueMessageHeaders) {

		this.queueMessageHeaders = queueMessageHeaders;
	}

	public Integer getValidUntilDaysAhead() {

		return validUntilDaysAhead;
	}

	public void setValidUntilDaysAhead(final Integer validUntilDaysAhead) {

		this.validUntilDaysAhead = validUntilDaysAhead;
	}

	public String getPayloadAttributeValidFrom() {

		return payloadAttributeValidFrom;
	}

	public void setPayloadAttributeValidFrom(final String payloadAttributeValidFrom) {

		this.payloadAttributeValidFrom = payloadAttributeValidFrom;
	}

	public String getValidFrom() {

		return validFrom;
	}

	public void setValidFrom(final String validFrom) {

		this.validFrom = validFrom;
	}
}
