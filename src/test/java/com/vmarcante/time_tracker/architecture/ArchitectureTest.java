package com.vmarcante.time_tracker.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
        packages = "com.vmarcante.time_tracker",
        importOptions = { ImportOption.DoNotIncludeTests.class, ImportOption.DoNotIncludeJars.class })
class ArchitectureTest {

    @ArchTest
    static final ArchRule baseMustNotDependOnCore = noClasses()
            .that().resideInAPackage("com.vmarcante.time_tracker.base..")
            .should().dependOnClassesThat()
            .resideInAPackage("com.vmarcante.time_tracker.core..");

    @ArchTest
    static final ArchRule domainAndApplicationMustNotDependOnOuterLayers = noClasses()
            .that().resideInAnyPackage(
                    "com.vmarcante.time_tracker.core.domain..",
                    "com.vmarcante.time_tracker.core.application..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                    "com.vmarcante.time_tracker.core.infrastructure..",
                    "com.vmarcante.time_tracker.core.interfaces..");

    @ArchTest
    static final ArchRule interfacesMustNotDependOnInfrastructure = noClasses()
            .that().resideInAPackage("com.vmarcante.time_tracker.core.interfaces..")
            .should().dependOnClassesThat()
            .resideInAPackage("com.vmarcante.time_tracker.core.infrastructure..");
}
