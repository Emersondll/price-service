package com.abinbev.b2b.price.api.exceptions;

public class BadRequestException extends GlobalException {

	private static final long serialVersionUID = 1L;

	private BadRequestException(final Issue issue) {

		super(issue);
	}

	public static BadRequestException invalidPagination(final IssueEnum issueEnum) {

		return new BadRequestException(new Issue(issueEnum));
	}

	public static BadRequestException requestDecodingError(final String field) {

		return new BadRequestException(new Issue(IssueEnum.REQUEST_DECODING_ERROR, field));
	}

	public static BadRequestException customBadRequest(final IssueEnum issueEnum, final Object... args) {

		return new BadRequestException(new Issue(issueEnum, args));
	}
}
