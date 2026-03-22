package com.abinbev.b2b.price.api.exceptions;

import java.util.Collections;

public class MismatchedAccountValuesException extends GlobalException {

	private static final long serialVersionUID = 1L;

	public MismatchedAccountValuesException() {

		super(new Issue(IssueEnum.BAD_REQUEST,
				Collections.singletonList(IssueEnum.MISMATCHED_ACCOUNT_ID_AND_VENDOR_ACCOUNT_ID_HEADER.getFormattedMessage())));
	}

}
