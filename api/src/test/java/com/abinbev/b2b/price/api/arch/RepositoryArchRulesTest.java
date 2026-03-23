package com.abinbev.b2b.price.api.arch;

import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.ALL_CLASSES_PACKAGE;
import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.REPOSITORY_LAYER_NAME;
import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.REPOSITORY_PACKAGE;
import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.REPOSITORY_SUFFIX;
import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.SERVICE_LAYER_NAME;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.constructors;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.library.GeneralCodingRules.BE_ANNOTATED_WITH_AN_INJECTION_ANNOTATION;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.abinbev.b2b.price.api.testhelpers.ArchUnitHelper;
import com.newrelic.api.agent.Trace;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;

class RepositoryArchRulesTest {

	private static JavaClasses allClasses;

	@BeforeAll
	static void init() {

		allClasses = new ClassFileImporter().withImportOption(new ImportOption.DoNotIncludeTests()).importPackages(ALL_CLASSES_PACKAGE);
	}

	@Test
	void shouldBeAnnotatedJustAsRepositoryWhenRepositoryClassesInPackage() {

		final ArchRule rule = classes().that().resideInAPackage(REPOSITORY_PACKAGE).should().beAnnotatedWith(Repository.class).andShould()
				.notBeAnnotatedWith(Component.class).andShould().notBeAnnotatedWith(Service.class).andShould()
				.notBeAnnotatedWith(Configuration.class);
		rule.check(allClasses);
	}

	@Test
	void shouldBeAsuffixedAsRepositoryWhenRepositoryClassesInPackage() {

		final ArchRule rule = classes().that().resideInAPackage(REPOSITORY_PACKAGE).should().haveSimpleNameEndingWith(REPOSITORY_SUFFIX);
		rule.check(allClasses);
	}

	@Test
	void shouldResideInRepositoryWhenRepositoryClassesAnnotatedAsRepository() {

		final ArchRule rule = classes().that().areAnnotatedWith(Repository.class).should().resideInAnyPackage(REPOSITORY_PACKAGE);
		rule.check(allClasses);
	}

	@Test
	void shouldBeAnnotatedWithTraceWhenMethodsPublicDeclaredInClassesInPackage() {

		final ArchRule rule = methods().that().arePublic().and().areDeclaredInClassesThat().resideInAPackage(REPOSITORY_PACKAGE).should()
				.beAnnotatedWith(Trace.class);
		rule.check(allClasses);
	}

	@Test
	void shouldBePrivateWhenFieldsDeclaredInRepositoryClassesInPackage() {

		final ArchRule rule = fields().that().areDeclaredInClassesThat().resideInAnyPackage(REPOSITORY_PACKAGE).should().notBePublic();
		rule.check(allClasses);
	}

	@Test
	void shouldNotBeTopLevelClassesWhenRepositoryClassesInPackage() {

		final ArchRule rule = classes().that().resideInAPackage(REPOSITORY_PACKAGE).should().beTopLevelClasses();
		rule.check(allClasses);
	}

	@Test
	void shouldOnlyBeAccessedByServicesWhenRepositoryClassesInPackage() {

		final ArchRule rule = ArchUnitHelper.returnLayeredArchitecture().whereLayer(REPOSITORY_LAYER_NAME)
				.mayOnlyBeAccessedByLayers(SERVICE_LAYER_NAME);

		rule.check(allClasses);

	}

	@Test
	void shouldNotHaveFieldInjectionWhenRepositoryClassesInPackage() {

		final ArchRule rule = noFields().that().areDeclaredInClassesThat().resideInAnyPackage(REPOSITORY_PACKAGE)
				.should(BE_ANNOTATED_WITH_AN_INJECTION_ANNOTATION);

		rule.check(allClasses);
	}

	@Test
	void shouldHaveConstructorInjectionWhenRepositoryClassesInPackage() {

		final ArchRule rule = constructors().that().areDeclaredInClassesThat().resideInAnyPackage(REPOSITORY_PACKAGE).should()
				.beAnnotatedWith(Autowired.class);

		rule.check(allClasses);
	}

}
