# Git Inspector

## SBT Commands

**SBT Commands & Tools:**

- **Scalafix**: Lints & refactors code (unused imports). Command: `sbt scalafixAll`.
- **Scalafmt**: Formats Scala code. Command: `sbt scalafmtAll`.
- **Wartremover**: Checks for code issues (final case classes). Command: `sbt wartremover`.
- **Scoverage**: Checks code coverage (report). Commands: `sbt clean coverage test`, `sbt coverageReport`.
- **sbt-assembly**: Creates project jar. Command: `sbt assembly`.

**Other SBT Commands:**

- `sbt test`: Runs tests.
- `sbt doc`: Generates documentation.

**Code Style Tool:**

- **Trunk**: Checks code style (trailing spaces).
