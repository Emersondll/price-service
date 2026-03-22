package com.abinbev.b2b.price.api.exceptions;

import java.util.Collections;

public class MismatchedSkusAndVendorItemsValuesException extends GlobalException {

	private static final long serialVersionUID = 1L;

	public MismatchedSkusAndVendorItemsValuesException() {

		super(new Issue(IssueEnum.BAD_REQUEST,
				Collections.singletonList(IssueEnum.MISMATCHED_SKUS_AND_VENDOR_ITEM_IDS_HEADER.getFormattedMessage())));
	}

}
