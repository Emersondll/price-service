package com.abinbev.b2b.price.api.rest.vo;

import java.util.List;

public class JwtDataVo {

	private List<String> roles;
	private List<String> accounts;
	private List<String> extensionAccountIds;
	private String country;
	private String app;

	public static JwtDataBuilder builder() {

		return new JwtDataBuilder();
	}

	public List<String> getExtensionAccountIds() {

		return extensionAccountIds;
	}

	public void setExtensionAccountIds(final List<String> extensionAccountIds) {

		this.extensionAccountIds = extensionAccountIds;
	}

	public List<String> getRoles() {

		return roles;
	}

	public void setRoles(final List<String> roles) {

		this.roles = roles;
	}

	public List<String> getAccounts() {

		return accounts;
	}

	public void setAccounts(final List<String> accounts) {

		this.accounts = accounts;
	}

	public String getCountry() {

		return country;
	}

	public void setCountry(final String country) {

		this.country = country;
	}

	public String getApp() {

		return app;
	}

	public void setApp(final String app) {

		this.app = app;
	}

	public static class JwtDataBuilder {

		final JwtDataVo jwtDataVo = new JwtDataVo();

		public JwtDataBuilder withRoles(final List<String> roles) {

			this.jwtDataVo.roles = roles;
			return this;
		}

		public JwtDataBuilder withAccounts(final List<String> accounts) {

			this.jwtDataVo.accounts = accounts;
			return this;
		}

		public JwtDataBuilder withExtensionAccountIds(final List<String> extensionAccountIds) {

			this.jwtDataVo.setExtensionAccountIds(extensionAccountIds);
			return this;
		}

		public JwtDataBuilder withApp(final String app) {

			this.jwtDataVo.app = app;
			return this;
		}

		public JwtDataBuilder withCountry(final String country) {

			this.jwtDataVo.country = country;
			return this;
		}

		public JwtDataVo build() {

			return this.jwtDataVo;
		}
	}

}