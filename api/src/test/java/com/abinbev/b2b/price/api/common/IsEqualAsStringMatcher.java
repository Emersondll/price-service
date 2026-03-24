package com.abinbev.b2b.price.api.common;

import java.util.Objects;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class IsEqualAsStringMatcher<T> extends BaseMatcher<T> {

	private final String expectedValueAsString;

	public IsEqualAsStringMatcher(final T expectedValue) {

		final String valueAsString;
		if (expectedValue == null) {
			valueAsString = null;
		} else {
			valueAsString = getValueAsString(expectedValue);
			if (valueAsString == null) {
				throw new IllegalArgumentException("expected value could not be processed as String");
			}
		}
		expectedValueAsString = valueAsString;
	}

	private static String getValueAsString(final Object value) {

		final ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
		mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
		mapper.registerModule(new JavaTimeModule());
		try {
			return mapper.writeValueAsString(value);
		} catch (final JsonProcessingException e) {
			throw new RuntimeException("Fail to write value as String: " + e.getMessage());
		}
	}

	private static boolean assertThatObjectsAsStringAreTheSame(final Object actual, final String expectedValueAsString) {

		if (expectedValueAsString == null) {
			return actual == null;
		}

		final String actualAsString = getValueAsString(actual);
		return Objects.equals(actualAsString, expectedValueAsString);
	}

	/**
	 * Creates a matcher that matches when the examined object as string is logically equal to the specified operand as string.
	 * If the specified operand is null then the created matcher will only match if the examined object's equals method returns true when passed a null (which would be a violation of the equals contract), unless the examined object itself is null, in which case the matcher will return a positive match.
	 * The created matcher provides a special behaviour when examining Arrays, whereby it will match if both the operand and the examined object are arrays of the same length and contain items that are equal to each other (according to the above rules) in the same indexes.
	 */
	public static <T> Matcher<T> equalToAsString(final T operand) {

		return new IsEqualAsStringMatcher<T>(operand);
	}

	@Override
	public boolean matches(final Object actualValue) {

		return assertThatObjectsAsStringAreTheSame(actualValue, expectedValueAsString);
	}

	@Override
	public void describeTo(final Description description) {

		description.appendValue(expectedValueAsString);
	}

	@Override
	public void describeMismatch(final Object item, final Description description) {

		final String actualValueAsString = getValueAsString(item);
		description.appendText("was ").appendValue(actualValueAsString);
	}

}
