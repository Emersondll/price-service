package com.abinbev.b2b.price.api.exceptions;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "MessageResponse")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Issue implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonProperty
	@Schema(description = "Code of response", required = true)
	private final int code;

	@JsonProperty
	@Schema(description = "Message of response", required = true)
	private final String message;

	@JsonProperty
	@ArraySchema(arraySchema = @Schema(description = "Details of response"), schema = @Schema(description = "Detail"))
	private List<String> details;

	public Issue(final IssueEnum issue) {

		code = issue.getCode();
		message = issue.getFormattedMessage();
	}

	public Issue(final IssueEnum issue, final Object... args) {

		code = issue.getCode();
		message = issue.getFormattedMessage(args);
	}

	public Issue(final IssueEnum issue, final List<String> details) {

		this(issue);
		this.details = details;
	}

	public int getCode() {

		return code;
	}

	public String getMessage() {

		return message;
	}

	public List<String> getDetails() {

		return details;
	}

	public void setDetails(final List<String> details) {

		this.details = details;
	}
}
