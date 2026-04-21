package helpers;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class InstantHelper {

	public static Instant convertStringFormattedAsIsoLocalDateToInstant(final String value) {

		final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
		final LocalDateTime d = LocalDate.parse(value, formatter).atTime(0, 0);
		final ZoneId zone = ZoneId.of("UTC");

		return ZonedDateTime.of(d, zone).toInstant();
	}

	public static Instant convertStringFormattedAsIsoOffsetDateTimeToInstant(final String value) {

		final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
		return ZonedDateTime.parse(value, formatter).withZoneSameInstant(ZoneId.of("UTC")).toInstant();
	}
}
