# Git Inspector

## SBT Commands

Here are some useful SBT commands you can run:

- **Run Scalafix**: `sbt scalafixAll`
  - This command applies all Scalafix rules to the codebase.

- **Format Code**: `sbt scalafmtAll`
  - This command formats the Scala code according to the Scalafmt configuration.

- **Run Wartremover**: `sbt wartremover`
  - This command checks the code for potential issues and anti-patterns as defined by the Wartremover rules.

- **Run Tests**: `sbt test`
  - This command runs all the tests in the project.

- **Generate Documentation**: `sbt doc`
  - This command generates documentation for the project based on the comments in the code.

- **Clean and Run Coverage Tests**: `sbt clean coverage test`
  - This command cleans the project, runs tests, and collects code coverage information.

- **Generate Coverage Report**: `sbt coverageReport`
  - This command generates a report of the code coverage from the tests.

- **Generate Assembly Jar**: `sbt assembly`
  - This command generates a jar file of the project.
