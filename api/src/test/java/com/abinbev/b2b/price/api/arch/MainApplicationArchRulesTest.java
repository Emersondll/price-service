package com.abinbev.b2b.price.api.arch;

import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.MAIN_PACKAGE;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;

class MainApplicationArchRulesTest {

	private static JavaClasses allClasses;

	@BeforeAll
	static void init() {

		allClasses = new ClassFileImporter().withImportOption(new ImportOption.DoNotIncludeTests())
				.importPackages(ArchUnitConstants.ALL_CLASSES_PACKAGE);
	}

	@Test
	void shouldBeAnnotatedWithFeignAndSpringBootApplicationAnnotationWhenHaveMainClass() {

		final ArchRule rule = classes().that().resideInAPackage(MAIN_PACKAGE).should().beAnnotatedWith(SpringBootApplication.class);

		rule.check(allClasses);
	}

}
