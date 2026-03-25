package com.abinbev.b2b.price.api.testhelpers;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.jayway.jsonpath.JsonPath;

public class IntegrationTestsResourceHelper {

	public static Object getResponseDataFileContent(final String resourcePath, final String fileName) throws IOException {

		return JsonPath.compile("$")
				.read(Files.readString(ResourceUtils.getFile("classpath:request-data/" + resourcePath + "/response/" + fileName).toPath()));
	}

	public static Object getResponseDataFileContent(final String resourcePath, final String fileName, final Object... args)
			throws IOException {

		return JsonPath.compile("$").read(String.format(Files
				.readString(ResourceUtils.getFile("classpath:request-data/" + resourcePath + "/response/" + fileName).toPath()), args));
	}

	public static String getRequestDataFileContent(final String resourcePath, final String fileName) throws IOException {

		return Files.readString(ResourceUtils.getFile("classpath:request-data/" + resourcePath + "/request/" + fileName).toPath());
	}

	public static <T> T getMockDataFileContent(final String resourcePath, final String fileName, final TypeReference<T> typeReference)
			throws IOException {

		final String dataFile = new String(
				Files.readAllBytes(ResourceUtils.getFile("classpath:request-data/" + resourcePath + "/mock/" + fileName).toPath()));
		return new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").registerTypeAdapter(Instant.class, new InstantDeSerializer())
				.create().fromJson(dataFile, typeReference.getType());
	}

	private static class InstantDeSerializer implements JsonDeserializer<Instant> {

		@Override
		public Instant deserialize(final JsonElement jelement, final Type type, final JsonDeserializationContext jdc)
				throws JsonParseException {

			return createZonedValidFromDateTime(jelement.getAsJsonPrimitive().getAsString());
		}

		private Instant createZonedValidFromDateTime(final String value) {

			final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			final LocalDateTime d = LocalDate.parse(value, formatter).atTime(0, 0);
			final ZoneId zone = ZoneId.of("America/Sao_Paulo");

			return ZonedDateTime.of(d, zone).withZoneSameInstant(ZoneId.of("UTC")).toInstant();
		}

	}
}
