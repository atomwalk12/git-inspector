[![Code CI](https://github.com/DiamondLightSource/pytac/actions/workflows/code.yml/badge.svg)](https://github.com/DiamondLightSource/pytac/actions/workflows/code.yml)
[![Docs CI](https://github.com/atomwalk12/PPS-22-git-insp/actions/workflows/gh-pages.yml/badge.svg)](https://github.com/atomwalk12/PPS-22-git-insp/actions/workflows/gh-pages.yml)
[![codecov](https://codecov.io/gh/atomwalk12/pps-22-git-insp/branch/main/graph/badge.svg)](https://codecov.io/gh/atomwalk12/pps-22-git-insp)
[![release](https://img.shields.io/github/v/tag/atomwalk12/pps-22-git-insp.svg?label=release)](https://github.com/atomwalk12/pps-22-git-insp/releases)

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


# Download the reranker model

```bash
# Make sure git-lfs is installed (https://git-lfs.com)
git lfs install

# From the root directory
git clone https://huggingface.co/jinaai/jina-reranker-v2-base-multilingual reranker/jina-reranker-v2-base-multilingual
```
