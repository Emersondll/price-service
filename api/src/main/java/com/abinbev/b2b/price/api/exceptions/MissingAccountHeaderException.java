package com.abinbev.b2b.price.api.exceptions;

import java.util.Collections;

public class MissingAccountHeaderException extends GlobalException {

	private static final long serialVersionUID = 1L;

	public MissingAccountHeaderException() {

		super(new Issue(IssueEnum.BAD_REQUEST,
				Collections.singletonList(IssueEnum.MISSING_ACCOUNT_ID_OR_VENDOR_ACCOUNT_ID_HEADER.getFormattedMessage())));
	}

}
