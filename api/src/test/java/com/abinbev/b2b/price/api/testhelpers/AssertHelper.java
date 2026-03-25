package com.abinbev.b2b.price.api.testhelpers;

import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;

import com.abinbev.b2b.price.domain.model.Tax;
import com.abinbev.b2b.price.domain.model.TaxCondition;
import com.abinbev.b2b.price.domain.model.TaxOrderSubTotal;
import com.abinbev.b2b.price.domain.model.v2.TaxConditionV2;
import com.abinbev.b2b.price.domain.model.v2.TaxOrderSubTotalV2;
import com.abinbev.b2b.price.domain.model.v2.TaxV2;

public class AssertHelper {

	private AssertHelper() {

	}

	public static void assertTaxes(final String id, final Map<String, Tax> taxes, final Boolean hidden, final String type,
			final BigDecimal base, final BigDecimal value) {

		taxes.values().forEach(tax -> assertAll("should return all the correct values", () -> Assertions.assertEquals(id, tax.getTaxId()),
				() -> Assertions.assertEquals(type, tax.getType()), () -> Assertions.assertEquals(base, tax.getBase()),
				() -> Assertions.assertEquals(value, tax.getValue()), () -> Assertions.assertEquals(hidden, tax.isHidden())));
	}

	public static void assertTaxesV2(final String id, final Map<String, TaxV2> taxesV2, final Boolean hidden, final String type,
			final BigDecimal base, final BigDecimal value) {

		taxesV2.values().forEach(tax -> assertAll("should return all the correct values", () -> Assertions.assertEquals(id, tax.getTaxId()),
				() -> Assertions.assertEquals(type, tax.getType()), () -> Assertions.assertEquals(base, tax.getBase()),
				() -> Assertions.assertEquals(value, tax.getValue()), () -> Assertions.assertEquals(hidden, tax.isHidden())));
	}
	
	public static Map<String, Tax> mockTaxes(final boolean hidden, final BigDecimal base, final BigDecimal tax, final String taxId,
			final String type, final BigDecimal minimumOrderValue, final String... taxBaseInclusionIds) {

		final Map<String, Tax> taxes = new HashMap<>();
		taxes.put(taxId,
				new Tax(taxId, type, tax, Arrays.asList(taxBaseInclusionIds), hidden, base, createTaxCondition(minimumOrderValue)));
		return taxes;
	}
	
	public static Map<String, TaxV2> mockTaxesV2(final boolean hidden, final BigDecimal base, final BigDecimal tax, final String taxId,
			final String type, final BigDecimal minimumOrderValue, final String... taxBaseInclusionIds) {

		final Map<String, TaxV2> taxes = new HashMap<>();
		taxes.put(taxId,
				new TaxV2(taxId, type, tax, Arrays.asList(taxBaseInclusionIds), hidden, base, createTaxConditionV2(minimumOrderValue)));
		return taxes;
	}

	private static TaxConditionV2 createTaxConditionV2(BigDecimal minimumOrderValue) {
		
		final TaxConditionV2 taxConditionV2 = new TaxConditionV2();
		if (minimumOrderValue != null) {
			taxConditionV2.setOrderSubTotal(new TaxOrderSubTotalV2(minimumOrderValue));
		}

		return taxConditionV2;
	}

	private static TaxCondition createTaxCondition(final BigDecimal minimumOrderValue) {

		final TaxCondition taxCondition = new TaxCondition();
		if (minimumOrderValue != null) {
			taxCondition.setOrderSubTotal(new TaxOrderSubTotal(minimumOrderValue));
		}

		return taxCondition;
	}
}
