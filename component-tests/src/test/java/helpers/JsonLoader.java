package helpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

import org.springframework.util.ResourceUtils;

import com.jayway.jsonpath.JsonPath;

import context.TestContext;

public class JsonLoader {

	private JsonLoader() {

	}

	private static String buildResourceLocation(final String folder, final String subPath, final String fileName) {

		return String.format("src/test/resources/payloads/%s/%s/%s.json", folder, subPath, fileName);
	}

	public static Object getRequestDataFileContent(final TestContext testContext, final String fileName) throws IOException {

		String fileContent = Files
				.readString(ResourceUtils.getFile(buildResourceLocation(testContext.getFolder(), "inputs", fileName)).toPath());

		if (testContext.getValidUntilDaysAhead() != null) {
			fileContent = fileContent.replace("VALID_UNTIL_PLACE_HOLDER",
					getDatePlusDays(testContext.getValidUntilDaysAhead(), testContext.getRequestHeaders().get("timezone").toString()));
		}

		if (Objects.nonNull(testContext.getValidFrom())) {
			fileContent = fileContent.replace("VALID_FROM_PLACE_HOLDER", testContext.getValidFrom());
		}

		if (testContext.getPayloadAttributeValidFrom() != null) {
			fileContent = fileContent.replace("## WILL BE REPLACED BY CURRENT DATE ##", testContext.getPayloadAttributeValidFrom());
		}

		return JsonPath.compile("$").read(fileContent);
	}

	public static io.restassured.path.json.JsonPath getJsonPathFromFile(final String folder, final String fileName) {

		return io.restassured.path.json.JsonPath.from(new File(buildResourceLocation(folder, "outputs", fileName)));
	}

	public static String getDatePlusDays(final int daysToAdd, final String timezone) {

		return ZonedDateTime.now(ZoneId.of(timezone)).plusDays(daysToAdd).toLocalDate().toString();
	}

	public static String getResponseDataFileContentAsString(final String folder, final String fileName) throws IOException {

		return Files
				.readString(new File("src/test/resources/payloads/" + folder + "/outputs/" + fileName + ".json").toPath());
	}

}
