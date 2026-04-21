package helpers;

import static java.util.Optional.ofNullable;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class DataAdaptationHelper {

	public static final Function<Integer, Long> INTEGER_TO_LONG_FUNCTION = generateIntegerToLongFunction();
	public static final Function<String, Date> STRING_FORMATTED_AS_ISO_LOCAL_DATE_TO_DATE_FUNCTION = generateStringFormattedAsIsoLocalDateToInstantFunction();
	public static final Function<String, Date> STRING_FORMATTED_AS_ISO_OFFSET_DATE_TIME_TO_DATE_FUNCTION = generateStringFormattedAsIsoOffsetDateTimeToInstant();

	/**
	 * Read the Map that represents a json structure and change the values of some attributes with the respective converting functions.
	 *
	 * @param mapJsonList         The map that represents the json structure
	 * @param convertingFunctions The map of converting functions. The key represents the json attribute name and the value represents the conversion function.
	 *                            If the value of a json attribute is a Map, so the value of a convertingFunctions attribute need to be a Map too. So we can navigate into entire json structure.
	 */
	public static void adaptTypesIntoMapJsonListWithFunctions(final List<Map<String, Object>> mapJsonList,
			final Map<String, Object> convertingFunctions) {

		mapJsonList.forEach(map -> adaptTypesIntoMapJsonWithFunctions(map, convertingFunctions));
	}

	private static void adaptTypesIntoMapJsonWithFunctions(final Map<String, Object> mapJson,
			final Map<String, Object> convertingFunctions) {

		final Set<Map.Entry<String, Object>> mapJsonEntrySet = mapJson.entrySet();
		mapJsonEntrySet.forEach(entry -> {
			final String key = entry.getKey();
			final Object value = entry.getValue();

			final Object adaptType = ofNullable(convertingFunctions).map(map -> map.get(key)).orElse(null);
			if (value instanceof Map) {

				if (adaptType != null) {
					if (!(adaptType instanceof Map)) {
						throw new RuntimeException(
								String.format("The adaptType must be a Map when the json data is a Map: attribute '%s'", key));
					}
				}

				adaptTypesIntoMapJsonWithFunctions((Map) value, (Map) adaptType);
			} else {
				if (adaptType != null) {
					if (!(adaptType instanceof Function)) {
						throw new RuntimeException(
								String.format("The adaptType must be a Class when the json data is a Object: attribute '%s'", key));
					}
					entry.setValue(((Function) adaptType).apply(value));
				}
			}
		});
	}

	private static Function<Integer, Long> generateIntegerToLongFunction() {

		return (Integer value) -> ofNullable(value).map(nonNullValue -> Long.valueOf(nonNullValue.toString())).orElse(null);
	}

	private static Function<String, Date> generateStringFormattedAsIsoLocalDateToInstantFunction() {

		return (String value) -> ofNullable(value)
				.map(nonNullValue -> Date.from(InstantHelper.convertStringFormattedAsIsoLocalDateToInstant(nonNullValue))).orElse(null);
	}

	private static Function<String, Date> generateStringFormattedAsIsoOffsetDateTimeToInstant() {

		return (String value) -> ofNullable(value)
				.map(nonNullValue -> Date.from(InstantHelper.convertStringFormattedAsIsoOffsetDateTimeToInstant(nonNullValue)))
				.orElse(null);
	}
}
