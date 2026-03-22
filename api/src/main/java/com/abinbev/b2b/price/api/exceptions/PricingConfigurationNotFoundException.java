package com.abinbev.b2b.price.api.exceptions;

public class PricingConfigurationNotFoundException extends GlobalException {

    private static final long serialVersionUID = 1L;

    private PricingConfigurationNotFoundException(final Issue issue) {

        super(issue);
    }

    public static PricingConfigurationNotFoundException configurationNotFoundException(final String country) {

        return new PricingConfigurationNotFoundException(new Issue(IssueEnum.PRICING_CONFIGURATION_NOT_FOUND, country));
    }

}
