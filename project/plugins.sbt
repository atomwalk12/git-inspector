addSbtPlugin("org.wartremover" % "sbt-wartremover" % "3.3.1")  // Check potential bugs (i.e. case classes must be final)
addSbtPlugin("org.scalameta"   % "sbt-scalafmt"    % "2.5.4")  // Format code
addSbtPlugin("ch.epfl.scala"   % "sbt-scalafix"    % "0.12.1") // Linter for Scala 3 (i.e. unused imports)
addSbtPlugin("org.scoverage"   % "sbt-scoverage"   % "2.3.1")  // Code coverage
addSbtPlugin("com.eed3si9n"    % "sbt-assembly"    % "2.3.1")  // Create unique jar file
