package com.abinbev.b2b.price.api.arch;

import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.ALL_CLASSES_PACKAGE;
import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.CONVERTER_PACKAGE;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.lang.ArchRule;

class ConverterArchRulesTest {

	private static JavaClasses allClasses;

	@BeforeAll
	static void init() {

		allClasses = new ClassFileImporter().withImportOption(new DoNotIncludeTests()).importPackages(ALL_CLASSES_PACKAGE);
	}

	@Test
	void shouldBeAnnotatedAsComponentWhenClassesConverterClassesInPackage() {

		final ArchRule rule = classes().that().resideInAPackage(CONVERTER_PACKAGE).and().areNotInnerClasses().should()
				.beAnnotatedWith(Component.class);
		rule.check(allClasses);
	}

	@Test
	void shouldNotBeFieldsPublicWhenFieldsConverterClassesInPackage() {

		final ArchRule rule = fields().that().areDeclaredInClassesThat().resideInAPackage(CONVERTER_PACKAGE).should().notBePublic();
		rule.check(allClasses);
	}

	@Test
	void shouldNotHaveStaticMethodsWhenMethodsConverterClassesInPackage() {

		final ArchRule rule = methods().that().areDeclaredInClassesThat().resideInAPackage(CONVERTER_PACKAGE).should().notBeStatic().
				orShould().haveModifier(JavaModifier.SYNTHETIC);
		rule.check(allClasses);
	}
}
