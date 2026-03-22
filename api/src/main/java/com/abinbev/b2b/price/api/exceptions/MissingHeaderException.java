package com.abinbev.b2b.price.api.exceptions;

import java.util.Arrays;

public class MissingHeaderException extends GlobalException {

	private static final long serialVersionUID = 1L;

	public MissingHeaderException(final String headerName, final String headerValue) {

		super(new Issue(IssueEnum.BAD_REQUEST,
				Arrays.asList(IssueEnum.REQUIRED_HEADER_MISSING.getFormattedMessage(headerName, headerValue))));
	}
}
