package gitinsp.tests

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.Location
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import org.scalatest.flatspec.AnyFlatSpec

object Packages:
  val PROJECT = "..gitinsp.."

  val DOMAIN         = "..domain.."
  val APPLICATION    = "..application.."
  val INFRASTRUCTURE = "..infrastructure.."

  val DOMAIN_INFRASTRUCTURE_INTERFACES = "..domain.interfaces.infrastructure.."

object ArchUnits:
  private val DO_NOT_INCLUDE_SCALA_COMPILED_FILE: Location => Boolean = !_.contains("$")
  // This excludes files in target/scala*/test-classes
  private val DO_NOT_INCLUDE_CLASSES_TEST: Location => Boolean = !_.contains("test-classes")
  private val DO_NOT_INCLUDE_TEMP: Location => Boolean         = !_.contains("temp")

  val IMPORT_ONLY_CLASSES_CREATED = new ClassFileImporter()
    .withImportOption(DO_NOT_INCLUDE_SCALA_COMPILED_FILE(_))
    .withImportOption(DO_NOT_INCLUDE_CLASSES_TEST(_))
    .withImportOption(DO_NOT_INCLUDE_TEMP(_))
    .importPackages(Packages.PROJECT)

class HexagonalTest extends AnyFlatSpec:

  "Domain implementations" should "only depend on interfaces, not on infrastructure implementations" in:
    // The domain is completely isolated
    // Interfaces that the domain needs from the outside world ("ports") are defined in the domain
    // Implementation of those interfaces ("adapters") are in infrastructure
    // Inbound adapter are the intrastructure classes, outbound ports are application classes
    val rule = ArchRuleDefinition.classes()
      .that().resideInAPackage(Packages.DOMAIN)
      .and().areNotInterfaces()
      .should().onlyDependOnClassesThat()
      .resideInAnyPackage(
        "..scala..",
        "..java..",
        "..akka..",
        "..typesafe..",
        "..concurrent..",
        "..dev.langchain4j..",
        "..util..",
        Packages.DOMAIN,
        Packages.DOMAIN_INFRASTRUCTURE_INTERFACES,
      )
      .because(
        "Domain implementations should only depend on interfaces and standard libraries, not on infrastructure implementations",
      )
      .allowEmptyShould(true)
    rule.check(ArchUnits.IMPORT_ONLY_CLASSES_CREATED)

  "Classes of the application package" should "be allowed to depend on domain packages but not on infrastructure" in:
    // Application can depend on domain but not on infrastructure
    val rule = ArchRuleDefinition.classes()
      .that().resideInAPackage(Packages.APPLICATION)
      .should().onlyDependOnClassesThat()
      .resideInAnyPackage(
        "..scala..",
        "..java..",
        "..akka..",
        "..typesafe..",
        "..concurrent..",
        "..dev.langchain4j..",
        "..util..",
        Packages.DOMAIN,
        Packages.APPLICATION,
      )
      .because(
        "Application layer should only depend on domain layer and itself, not on infrastructure",
      )
      .allowEmptyShould(true)
    rule.check(ArchUnits.IMPORT_ONLY_CLASSES_CREATED)

  "Classes of the infrastructure package" should "be allowed to depend on domain package" in:
    val rule = ArchRuleDefinition.classes()
      .that().resideInAPackage(Packages.INFRASTRUCTURE)
      .should().onlyDependOnClassesThat()
      .resideInAnyPackage(
        "..scala..",
        "..java..",
        "..akka..",
        "..typesafe..",
        "..concurrent..",
        "..dev.langchain4j..",
        "..util..",
        "..io..",
        "..org..",
        "..ai..",
        "..com..",
        Packages.DOMAIN,
        Packages.INFRASTRUCTURE,
      )
      .allowEmptyShould(true)
    rule.check(ArchUnits.IMPORT_ONLY_CLASSES_CREATED)

class DesignPatternsTest extends AnyFlatSpec:

  "Factory classes" should "implement factory pattern" in:
    val rule = ArchRuleDefinition
      .methods()
      .that()
      .areDeclaredInClassesThat()
      .haveSimpleNameContaining("Factory")
      .and()
      .haveNameNotMatching(".*\\$.*") // exclude compiler generated classes
      .and()
      .haveNameNotMatching(".*logger.*") // exclude inherited logger
      .should()
      .haveNameMatching(".*create.*") // Include the factory method
      .orShould()
      .haveNameMatching(".*apply.*") // Include the singleton apply method
      .allowEmptyShould(true)
    rule.check(ArchUnits.IMPORT_ONLY_CLASSES_CREATED)

class FunctionalProgrammingTest extends AnyFlatSpec:

  "Service implementations" should "be in appropriate packages" in:
    val rule = ArchRuleDefinition
      .classes
      .that()
      .haveSimpleNameEndingWith("Service")
      .should()
      .resideInAnyPackage(Packages.DOMAIN, Packages.INFRASTRUCTURE)
      .allowEmptyShould(true)
    rule.check(ArchUnits.IMPORT_ONLY_CLASSES_CREATED)
