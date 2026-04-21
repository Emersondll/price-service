package context;

import static java.lang.ThreadLocal.withInitial;

import java.util.LinkedHashMap;

public class TestContextService {

	private static final TestContextService INSTANCE = new TestContextService();
	private final ThreadLocal<TestContext> testContexts = withInitial(TestContext::new);

	private TestContextService() {

	}

	public static TestContextService getInstance() {

		return INSTANCE;
	}

	public TestContext getTestContext() {

		return testContexts.get();
	}

	public void reset() {

		final TestContext testContext = testContexts.get();
		testContext.setRequestHeaders(new LinkedHashMap<>());
		testContext.setRequestParams(new LinkedHashMap<>());
		testContext.setValidUntilDaysAhead(null);
		testContext.setValidFrom(null);
	}
}