package com.abinbev.b2b.price.api.testhelpers;

import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.CONTROLLER_LAYER_NAME;
import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.CONTROLLER_PACKAGE_PATH;
import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.REPOSITORY_LAYER_NAME;
import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.REPOSITORY_PACKAGE_PATH;
import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.SERVICE_LAYER_NAME;
import static com.abinbev.b2b.price.api.testhelpers.ArchUnitConstants.SERVICE_LAYER_PATH;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.library.Architectures;

public class ArchUnitHelper {

	public static ImportOption ignoreHelpers = location -> !location.contains("/com/abinbev/b2b/price/api/testhelpers/") && !location
			.contains("/com/abinbev/b2b/price/api/common/") && !location.contains("/com/abinbev/b2b/price/api/integrations/");

	public static Architectures.LayeredArchitecture returnLayeredArchitecture() {

		return Architectures.layeredArchitecture().layer(CONTROLLER_LAYER_NAME).definedBy(CONTROLLER_PACKAGE_PATH).layer(SERVICE_LAYER_NAME)
				.definedBy(SERVICE_LAYER_PATH).layer(REPOSITORY_LAYER_NAME).definedBy(REPOSITORY_PACKAGE_PATH);
	}
}