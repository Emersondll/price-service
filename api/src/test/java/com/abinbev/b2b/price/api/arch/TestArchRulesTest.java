package com.abinbev.b2b.price.api.arch;

import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.ALL_CLASSES_PACKAGE;
import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.ARCH_UNIT_PACKAGE_PATH;
import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.CONFIG_TEST_NAME;
import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.CONTROLLER_PACKAGE_PATH;
import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.INTEGRATION_TEST_NAME;
import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.PACT_TEST_SUFFIX;
import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.TEST_SUFFIX;
import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.abinbev.b2b.price.api.testhelpers.ArchUnitHelper;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;

class TestArchRulesTest {

	private static JavaClasses allClasses;

	@BeforeAll
	static void init() {

		allClasses = new ClassFileImporter().withImportOption(ArchUnitHelper.ignoreHelpers)
				.withImportOption(new ImportOption.OnlyIncludeTests()).importPackages(ALL_CLASSES_PACKAGE);
	}

	@Test
	void shouldBeSuffixedAsTestWhenTestClassesInPackage() {

		final ArchRule rule = classes().that().areNotAnonymousClasses().should().haveSimpleNameEndingWith(TEST_SUFFIX);

		rule.check(allClasses);
	}

	@Test
	void shouldNotBeStaticWhenTestMethods() {

		final ArchRule rule = methods().that().areAnnotatedWith(Test.class).should().notBeStatic();

		rule.check(allClasses);
	}

	@Test
	void shouldBeAnnotatedAsTestOrBeforeEachOrBeforeAllWhenTestMethods() {

		final ArchRule rule = methods().that().arePublic().and().areDeclaredInClassesThat().areNotAnonymousClasses().should()
				.beAnnotatedWith(Test.class).orShould().beAnnotatedWith(BeforeEach.class).orShould().beAnnotatedWith(BeforeAll.class);

		rule.check(allClasses);
	}

	@Test
	void shouldBeImportHamcrestWhenTestClassesInPackage() {

		final ArchRule rule = classes()
				.that(are(not(JavaClass.Predicates.resideInAnyPackage(ARCH_UNIT_PACKAGE_PATH, CONTROLLER_PACKAGE_PATH)))).should()
				.dependOnClassesThat().doNotHaveFullyQualifiedName(org.junit.jupiter.api.Assertions.class.getName()).andShould()
				.dependOnClassesThat().haveFullyQualifiedName(org.hamcrest.MatcherAssert.class.getName()).orShould().dependOnClassesThat()
				.haveFullyQualifiedName(org.mockito.Mockito.class.getName()).orShould()
				.callMethod(org.junit.jupiter.api.Assertions.class, "assertThrows")
				.because("we consistently want to use Hamcrest in our tests");

		rule.check(allClasses);
	}

	@Test
	void shouldFollowStandardNomenclatureWhenTestMethodsAnnotatedWithTest() {

		final String nameRegex = "^should([a-zA-Z0-9])+When([a-zA-Z0-9])+";
		final ArchRule rule = methods().that().areAnnotatedWith(Test.class).should().haveNameMatching(nameRegex);

		rule.check(allClasses);
	}

	@Test
	void shouldContainInTheNameIntegrationWhenTestBeAnnotatedAsSpringBootTestAndAutoConfigureMockMvc() {

		final ArchRule rule = classes().that().haveSimpleNameContaining(INTEGRATION_TEST_NAME).and().areNotInnerClasses().should()
				.beAnnotatedWith(SpringBootTest.class).andShould().beAnnotatedWith(AutoConfigureMockMvc.class).orShould()
				.beAnnotatedWith(DataMongoTest.class);

		rule.check(allClasses);
	}

	@Test
	void shouldContainInTheNameContractOrIntegrationOrComponentWhenTestAllClassesAnnotatedWithSpringBootTest() {

		final ArchRule rule = classes().that().areAnnotatedWith(SpringBootTest.class).should()
				.haveSimpleNameContaining(INTEGRATION_TEST_NAME).orShould().haveSimpleNameEndingWith(PACT_TEST_SUFFIX).orShould()
				.haveSimpleNameContaining(CONFIG_TEST_NAME);

		rule.check(allClasses);
	}
}
