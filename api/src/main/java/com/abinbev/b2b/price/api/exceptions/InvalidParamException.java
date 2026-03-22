package com.abinbev.b2b.price.api.exceptions;

import java.util.Collections;

public class InvalidParamException extends GlobalException {

	private static final long serialVersionUID = 1L;

	public InvalidParamException(final String paramName, final String paramValue) {

		super(new Issue(IssueEnum.BAD_REQUEST,
				Collections.singletonList(IssueEnum.REQUEST_PARAM_NOT_VALID.getFormattedMessage(paramName, paramValue))));
	}
}
