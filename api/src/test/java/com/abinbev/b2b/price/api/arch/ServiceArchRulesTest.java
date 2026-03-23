package com.abinbev.b2b.price.api.arch;

import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.ALL_CLASSES_PACKAGE;
import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.CONTROLLER_LAYER_NAME;
import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.SERVICE_LAYER_NAME;
import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.SERVICE_PACKAGE;
import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.SERVICE_SUFFIX;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.abinbev.b2b.price.api.testhelpers.ArchUnitHelper;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.GeneralCodingRules;

class ServiceArchRulesTest {

	private static JavaClasses allClasses;

	@BeforeAll
	static void init() {

		allClasses = new ClassFileImporter().withImportOption(new DoNotIncludeTests())
				.importPackages(ALL_CLASSES_PACKAGE);
	}

	@Test
	void shouldBeSuffixedAsServiceWhenServiceClassesInPackage() {

		final ArchRule rule = classes().that().resideInAPackage(SERVICE_PACKAGE).and().areNotMemberClasses().should()
				.haveSimpleNameEndingWith(SERVICE_SUFFIX).orShould().haveSimpleNameContaining(SERVICE_SUFFIX);
		rule.check(allClasses);
	}

	@Test
	void shouldBeInTheServicePackageWhenAllClassesWithServiceSuffix() {

		final ArchRule rule = classes().that().haveSimpleNameEndingWith(SERVICE_SUFFIX).should().resideInAPackage(SERVICE_PACKAGE);
		rule.check(allClasses);
	}

	@Test
	void shouldBeInTheServicePackageWhenServiceClassesInPackage() {

		final ArchRule rule = classes().that().resideInAPackage(SERVICE_PACKAGE).and().areNotInnerClasses().should()
				.beAnnotatedWith(Service.class).andShould().notBeAnnotatedWith(Repository.class).andShould()
				.notBeAnnotatedWith(Configuration.class);
		rule.check(allClasses);
	}

	@Test
	void shouldNotBeFieldsPublicWhenServiceClassesInPackage() {

		final ArchRule rule = fields().that().areDeclaredInClassesThat().resideInAPackage(SERVICE_PACKAGE).should().notBePublic();
		rule.check(allClasses);
	}

	@Test
	void shouldNotBeFieldsAnnotatedWithInjectionWhenServiceClassesInPackage() {

		final ArchRule rule = noFields().that().areDeclaredInClassesThat().resideInAPackage(SERVICE_PACKAGE)
				.should(GeneralCodingRules.BE_ANNOTATED_WITH_AN_INJECTION_ANNOTATION);

		rule.check(allClasses);
	}

	@Test
	void shouldBeAccessedByControllerWhenClassesInServicePackage() {

		final ArchRule rule = ArchUnitHelper.returnLayeredArchitecture().whereLayer(SERVICE_LAYER_NAME)
				.mayOnlyBeAccessedByLayers(CONTROLLER_LAYER_NAME);

		rule.check(allClasses);
	}
}
