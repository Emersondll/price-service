package com.abinbev.b2b.price.api.common;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;

import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class IntegrationTestsResourceHelper {

	private static <T> T getDataFileContent(final IntegrationPathEnum integrationPath, final String resourcePath, final String contextPath,
			final String fileName, final TypeReference<T> typeReference) throws IOException {

		final String filePath = String.format("classpath:%s/%s/%s/%s", integrationPath.getPath(), resourcePath, contextPath, fileName);
		final String dataFile = new String(Files.readAllBytes(ResourceUtils.getFile(filePath).toPath()));

		return generateDataFromString(typeReference, dataFile);
	}

	public static <T> T generateDataFromString(final TypeReference<T> typeReference, final String data)
			throws com.fasterxml.jackson.core.JsonProcessingException {

		final ObjectMapper mapper = new ObjectMapper();
		final SimpleModule moduleInstantDeserializer = new SimpleModule("InstantDeserializer", new Version(1, 0, 0, null, null, null));
		moduleInstantDeserializer.addDeserializer(Instant.class, new InstantDeserializer());
		mapper.registerModule(moduleInstantDeserializer);
		return mapper.readValue(data, typeReference);
	}

	public static <T> T getJavaMockFileContent(final IntegrationPathEnum integrationPath, final String resourcePath, final String fileName,
			final TypeReference<T> typeReference) throws IOException {

		return getDataFileContent(integrationPath, resourcePath, "javaMock", fileName, typeReference);
	}

	public static <T> T getMongoMockDataFileContent(final IntegrationPathEnum integrationPath, final String resourcePath,
			final String fileName, final TypeReference<T> typeReference) throws IOException {

		return getDataFileContent(integrationPath, resourcePath, "mongoMock", fileName, typeReference);
	}
}
