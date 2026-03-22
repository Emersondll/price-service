package com.abinbev.b2b.price.api.exceptions;

public class JWTException extends GlobalException {

	private static final long serialVersionUID = 1L;

	protected JWTException(final Issue issue) {

		super(issue);
	}

	public static JWTException invalidToken() {

		return new JWTException(new Issue(IssueEnum.JWT_TOKEN_INVALID));
	}

	public static JWTException decodeException() {

		return new JWTException(new Issue(IssueEnum.JWT_DECODE_ERROR));
	}
}
