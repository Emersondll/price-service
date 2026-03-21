package com.abinbev.b2b.price.api.domain;

import java.util.List;

import com.abinbev.b2b.price.api.helpers.offers.enums.VersionFlag;

public class OffersAliasParameters {

	private VersionFlag requestVersion;
	private String requestAccountId;
	private List<String> requestItems;

	public VersionFlag getRequestVersion() {

		return requestVersion;
	}

	public void setRequestVersion(final VersionFlag requestVersion) {

		this.requestVersion = requestVersion;
	}

	public String getRequestAccountId() {

		return requestAccountId;
	}

	public void setRequestAccountId(final String requestAccountId) {

		this.requestAccountId = requestAccountId;
	}

	public List<String> getRequestItems() {

		return requestItems;
	}

	public void setRequestItems(final List<String> requestItems) {

		this.requestItems = requestItems;
	}
}
