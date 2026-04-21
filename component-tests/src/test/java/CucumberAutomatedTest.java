import java.io.File;
import java.time.Duration;
import java.time.temporal.TemporalUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(features = { "src/test/resources/features" },
		snippets = CucumberOptions.SnippetType.CAMELCASE,
		glue = "steps",
		//tags =  "@enableTest",
		plugin = { "pretty",
				"html:target/site/cucumber-pretty",
				"json:target/cucumber.json",
				"html:target/cucumber" })
public class CucumberAutomatedTest {

	private static final Logger log = LoggerFactory.getLogger(CucumberAutomatedTest.class);
	private static final String DOCKER_COMPOSE_YML = "./docker-compose.yml";
	private static final String SERVICE_NAME_CONSUMER = "consumer";
	private static final String SERVICE_NAME_API = "api";
	private static final String SERVICE_NAME_RELAY = "relay";

	@Rule
	private static final DockerComposeContainer<?> DOCKER_COMPOSE_CONTAINER = new DockerComposeContainer<>(new File(DOCKER_COMPOSE_YML));

	static {
		DOCKER_COMPOSE_CONTAINER
				.withLocalCompose(true)
				.withExposedService(SERVICE_NAME_API, 8082, Wait.forHealthcheck().withStartupTimeout(Duration.ofSeconds(200)))
				.withExposedService(SERVICE_NAME_RELAY, 8080, Wait.forHealthcheck().withStartupTimeout(Duration.ofSeconds(200)))
				.withExposedService(SERVICE_NAME_CONSUMER, 8081, Wait.forHealthcheck().withStartupTimeout(Duration.ofSeconds(200)))
				.withStartupTimeout(Duration.ofSeconds(200));
	}

	@BeforeClass
	public static void setUp() {

		log.info("=== Starting Automated Tests ===");
		DOCKER_COMPOSE_CONTAINER.start();
	}

	@AfterClass
	public static void tearDown() {

		log.info("=== Automated Tests Finished ===");
		DOCKER_COMPOSE_CONTAINER.stop();
	}
}
