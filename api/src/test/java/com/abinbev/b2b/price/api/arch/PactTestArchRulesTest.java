package com.abinbev.b2b.price.api.arch;

import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.ALL_CLASSES_PACKAGE;
import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.INTEGRATION_TEST_PACKAGE_PATH;
import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.PACT_TEST_SUFFIX;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;

import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.loader.PactBroker;

class PactTestArchRulesTest {

	private static JavaClasses allClasses;

	@BeforeAll
	static void init() {

		allClasses = new ClassFileImporter().withImportOption(new ImportOption.OnlyIncludeTests()).importPackages(ALL_CLASSES_PACKAGE);
	}

	@Test
	void shouldBeSuffixedAsPactTestWhenTestClassesInPackage() {

		final ArchRule rule = classes().that().resideInAPackage(INTEGRATION_TEST_PACKAGE_PATH).and().areNotAnonymousClasses().should()
				.haveSimpleNameEndingWith(PACT_TEST_SUFFIX);

		rule.check(allClasses);
	}

	@Test
	void shouldBeAnnotatedAsTestOrBeforeEachOrBeforeAllWhenTestMethods() {

		final ArchRule rule = methods().that().arePublic().and().areDeclaredInClassesThat().areNotAnonymousClasses().and()
				.areDeclaredInClassesThat().resideInAPackage(INTEGRATION_TEST_PACKAGE_PATH).should().beAnnotatedWith(State.class).orShould()
				.beAnnotatedWith(BeforeEach.class).orShould().beAnnotatedWith(BeforeAll.class);

		rule.check(allClasses);
	}

	@Test
	void shouldContainInTheNameIntegrationWhenTestBeAnnotatedAsSpringBootTestAndAutoConfigureMockMvc() {

		final ArchRule rule = classes().that().resideInAPackage(INTEGRATION_TEST_PACKAGE_PATH).and().areNotInnerClasses().should()
				.beAnnotatedWith(PactBroker.class).andShould().beAnnotatedWith(Provider.class);

		rule.check(allClasses);
	}
}
