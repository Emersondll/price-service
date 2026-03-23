package com.abinbev.b2b.price.api.arch;

import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.ALL_CLASSES_PACKAGE;
import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.CONTROLLERS_PACKAGE;
import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.CONTROLLER_LAYER_NAME;
import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.CONTROLLER_SUFFIX;
import static com.abinbev.b2b.price.api.testhelpers.ArchUnitHelper.returnLayeredArchitecture;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.constructors;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.library.GeneralCodingRules.BE_ANNOTATED_WITH_AN_INJECTION_ANNOTATION;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

class ControllerArchRulesTest {

	private static JavaClasses allClasses;

	@BeforeAll
	static void init() {

		allClasses = new ClassFileImporter().withImportOption(new ImportOption.DoNotIncludeTests()).importPackages(ALL_CLASSES_PACKAGE);
	}

	@Test
	void shouldBeAnnotatedJustAsControllerWhenControllerClassesInPackage() {

		final ArchRule rule = classes().that().resideInAPackage(CONTROLLERS_PACKAGE).should().beAnnotatedWith(RestController.class)
				.andShould().notBeAnnotatedWith(Component.class).andShould().notBeAnnotatedWith(Service.class).andShould()
				.notBeAnnotatedWith(Configuration.class).andShould().notBeAnnotatedWith(Repository.class);
		rule.check(allClasses);
	}

	@Test
	void shouldBeSuffixedAsControllerWhenControllerClassesInPackage() {

		final ArchRule rule = classes().that().resideInAPackage(CONTROLLERS_PACKAGE).should().haveSimpleNameEndingWith(CONTROLLER_SUFFIX)
				.orShould().haveSimpleNameContaining(CONTROLLER_SUFFIX);
		rule.check(allClasses);
	}

	@Test
	void shouldNotBeTopLevelClassesWhenControllerClassesInPackage() {

		final ArchRule rule = classes().that().resideInAPackage(CONTROLLERS_PACKAGE).should().beTopLevelClasses();
		rule.check(allClasses);
	}

	@Test
	void shouldNotHavePublicFieldsWhenControllerFieldsClassesInPackage() {

		final ArchRule rule = fields().that().areDeclaredInClassesThat().resideInAPackage(CONTROLLERS_PACKAGE).should().notBePublic();
		rule.check(allClasses);
	}

	@Test
	void shouldNotHaveFieldInjectionWhenControllerFieldsClassesInPackage() {

		final ArchRule rule = noFields().that().areDeclaredInClassesThat().resideInAnyPackage(CONTROLLERS_PACKAGE)
				.should(BE_ANNOTATED_WITH_AN_INJECTION_ANNOTATION);

		rule.check(allClasses);
	}

	@Test
	void shouldHaveConstructorInjectionWhenControllerConstructorsClassesInPackage() {

		final ArchRule rule = constructors().that().areDeclaredInClassesThat().resideInAnyPackage(CONTROLLERS_PACKAGE).should()
				.beAnnotatedWith(Autowired.class);

		rule.check(allClasses);
	}

	@Test
	void shouldNotBeAccessedByAnyLayerWhenControllerLayerClassesInPackage() {

		final ArchRule rule = returnLayeredArchitecture().whereLayer(CONTROLLER_LAYER_NAME).mayNotBeAccessedByAnyLayer();
		rule.check(allClasses);

	}

	@Test
	void shouldNotInjectClassesAnnotatedAsRepositoryWhenControllerConstructorsClassesInPackage() {

		final ArchRule rule = constructors().that().areDeclaredInClassesThat().resideInAPackage(CONTROLLERS_PACKAGE).should()
				.haveRawParameterTypes(new DescribedPredicate<>("not annotated as repository") {
					@Override
					public boolean apply(final List<JavaClass> input) {

						return input.stream().noneMatch(p -> p.isAnnotatedWith(Repository.class));
					}
				});

		rule.check(allClasses);
	}

	@Test
	void shouldInjectClassAnnotatedAsServiceWhenConstructorsControllerClassesInPackage() {

		final ArchRule rule = constructors().that().areDeclaredInClassesThat().resideInAPackage(CONTROLLERS_PACKAGE).should()
				.haveRawParameterTypes(new DescribedPredicate<>("annotated as service") {
					@Override
					public boolean apply(final List<JavaClass> input) {

						return input.stream().anyMatch(p -> p.isAnnotatedWith(Service.class));
					}
				});

		rule.check(allClasses);
	}

	@Test
	void shouldBeAnnotatedWithSwaggerApiWhenClassesControllerClassesInPackage() {

		final ArchRule rule = classes().that().resideInAPackage(CONTROLLERS_PACKAGE).should().beAnnotatedWith(Tag.class);
		rule.check(allClasses);
	}

	@Test
	void shouldBeAnnotatedWithSwaggerWhenMethodsControllerClassesInPackage() {

		final ArchRule rule = methods().that().areDeclaredInClassesThat().resideInAPackage(CONTROLLERS_PACKAGE).and().arePublic().should()
				.beAnnotatedWith(Operation.class);
		rule.check(allClasses);
	}

}
