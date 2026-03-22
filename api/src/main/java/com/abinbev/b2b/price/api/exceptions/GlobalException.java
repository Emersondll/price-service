package com.abinbev.b2b.price.api.exceptions;

public class GlobalException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final Issue issue;

	protected GlobalException(final Issue issue) {
		this.issue = issue;
	}

	protected GlobalException(final Issue issue, final Throwable cause) {
		super(issue.getMessage(), cause);
		this.issue = issue;
	}

	public Issue getIssue() {
		return issue;
	}
}
