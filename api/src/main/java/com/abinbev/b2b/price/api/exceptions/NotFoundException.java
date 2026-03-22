package com.abinbev.b2b.price.api.exceptions;

public class NotFoundException extends GlobalException {

	private static final long serialVersionUID = 1L;

	private NotFoundException(final Issue issue) {

		super(issue);
	}

	public static NotFoundException customerPricesNotFound(final String accountId, final String country) {

		return new NotFoundException(new Issue(IssueEnum.PRICES_NOT_FOUND, accountId, country));
	}

	public static NotFoundException customerPricesNotFoundV2(final String vendorId, final String vendorAccountId, final String country) {

		return new NotFoundException(new Issue(IssueEnum.PRICES_NOT_FOUND_V2, vendorId, vendorAccountId, country));
	}

	public static NotFoundException customerPricesWithPriceListIdNotFoundV2(final String vendorId, final String vendorAccountId, final String country, final String priceListId) {

		return new NotFoundException(new Issue(IssueEnum.PRICES_WITH_PRICE_LIST_ID_NOT_FOUND_V2, vendorId, vendorAccountId, country, priceListId));
	}

}
