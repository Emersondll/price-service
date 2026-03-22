package com.abinbev.b2b.price.api.exceptions;

import java.util.Collections;

public class InvalidHeaderException extends GlobalException {

	private static final long serialVersionUID = 1L;

	public InvalidHeaderException(final String headerName, final String headerValue) {

		super(new Issue(IssueEnum.BAD_REQUEST,
				Collections.singletonList(IssueEnum.REQUEST_HEADER_NOT_VALID.getFormattedMessage(headerName, headerValue))));
	}
}
