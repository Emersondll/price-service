package com.abinbev.b2b.price.api.arch;

import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.ALL_CLASSES_PACKAGE;
import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.CONFIG_PACKAGE_PATH;
import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.CONFIG_SUFFIX;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.lang.ArchRule;

class ConfigurationArchRulesTest {

	private static JavaClasses allClasses;

	@BeforeAll
	static void init() {

		allClasses = new ClassFileImporter().withImportOption(new DoNotIncludeTests()).importPackages(ALL_CLASSES_PACKAGE);
	}

	@Test
	void shouldBeSuffixedAsConfigurationWhenConfigClassesInPackage() {

		final ArchRule rule = classes().that().resideInAPackage(CONFIG_PACKAGE_PATH).and().areNotMemberClasses().and()
				.areNotAnonymousClasses().should().haveSimpleNameEndingWith(CONFIG_SUFFIX);
		rule.check(allClasses);
	}

	@Test
	void shouldBeAnnotatedAsConfigurationWhenConfigClassesInPackage() {

		final ArchRule rule = classes().that().resideInAPackage(CONFIG_PACKAGE_PATH).and().areNotMemberClasses().and()
				.areNotAnonymousClasses().should().beAnnotatedWith(Configuration.class);

		rule.check(allClasses);
	}
}
