import scala.sys.process.* // Used to change git hooks path

val scala3Version = "3.6.4"

// ------ Git hooks ------
// Change git hooks path to a tracked folder
val hooks = taskKey[Unit](
  "change git hooks path to a tracked folder (run 'sbt hooks' to configure)",
)

hooks := {
  val currentHooksPath =
    "git config --get core.hooksPath".!!.trim
  if (currentHooksPath != "git-hooks") {
    val command =
      "git config core.hooksPath git-hooks"
    println(
      s"Setting git hooks path... Command exited with code: ${command.!}",
    )
  }
  else {
    println(
      "Git hooks path is already set.",
    )
  }
}

// ------ Scalafmt ------
scalafmtOnCompile := true

// ------ Scoverage ------
coverageEnabled := true

// ------ Wartremover ------
wartremoverWarnings ++= Warts.all
wartremoverWarnings --= Seq(
  Wart.ImplicitParameter,
  Wart.Nothing,
  Wart.Equals,
)

wartremoverErrors ++= Warts.unsafe
wartremoverErrors --= Seq(
  Wart.Any,
)

// ------ Scalafix ------
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

// ------ Tests ------
Test / fork := true // provides isolation between test runs and the build tool
Test / javaOptions ++= Seq(
  "-Xmx4G", // increase memory for tests
)

// ------ Assembly ------
enablePlugins(AssemblyPlugin)
Compile / mainClass := Some("gitinsp.Main")
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", _*) => MergeStrategy.discard // Metadata is discarded to avoid conflicts
  case _                        => MergeStrategy.first
}
assembly / assemblyJarName := "gitinsp.jar"

// ------ Dependencies ------
lazy val root = project
  .in(file("."))
  .settings(
    name         := "pps-22-git-insp",
    version      := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    scalacOptions ++= Seq(
      // Enable Scala 3 indentation syntax
      "-new-syntax",
      "-indent",
      "-source:3.3",
      // Options for Scalafix
      "-Wunused:imports",
    ),
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test,

    // Logging
    libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5", // Logging library
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.5.18", // Engine for logging
  )
