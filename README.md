[![Code CI](https://github.com/DiamondLightSource/pytac/actions/workflows/code.yml/badge.svg)](https://github.com/DiamondLightSource/pytac/actions/workflows/code.yml)
![Docs CI](https://github.com/atomwalk12/PPS-22-git-insp/actions/workflows/gh-pages.yml/badge.svg?branch=main)
[![codecov](https://codecov.io/gh/atomwalk12/pps-22-git-insp/branch/main/graph/badge.svg)](https://codecov.io/gh/atomwalk12/pps-22-git-insp)
[![release](https://img.shields.io/github/v/release/atomwalk12/pps-22-git-insp.svg?label=release)](https://github.com/atomwalk12/pps-22-git-insp/releases)

# Git Inspector

LLM powered agent for answering questions about Github Repositories.

Checkout out the [report](docs/report/report.pdf), [poster](docs/report/poster.pdf), [website](https://atomwalk12.github.io/PPS-22-git-insp/) and the documentation of the [development process](https://atomwalk12.github.io/PPS-22-git-insp/process).

![poster](https://github.com/user-attachments/assets/50a20106-5bf8-40d3-9725-a7dc16ea6aa1)

## Features

- **RAG Pipeline for Code:** Implements a Retrieval Augmented Generation (RAG) pipeline specifically designed to query and understand code within Github repositories.
- **Local LLM Support via Ollama:** Integrates directly with Ollama, enabling the use of various open-source Large Language Models (LLMs) hosted locally.
- **Advanced Code Parsing:** Utilizes a Scala implementation of a recursive character text splitter (inspired by Python Langchain) to break down code into semantically meaningful chunks for effective indexing.
- **Dual Indexing Strategy:** Maintains separate vector indices for code and textual documentation (like Markdown), allowing for more targeted retrieval and potentially differentiated handling of modalities.
- **Web Interfaces:** Provides user interaction through web interfaces built with both Gradio (Python) and Scala.js.

## **Prerequisites:**

- **Ollama:** Required for running LLMs locally. Install Ollama following its [official documentation](https://github.com/ollama/ollama/tree/main) and download the desired models. The configuration file can be used to modify Ollama chat or embedding models (under the ollama entry).
- **Qdrant:** Required as the vector database for indexing and retrieval. The Qdrant client can be installed by using the [docker-compose.yaml](compose.yaml) file. Docker Compose can be installed by following the [official documentation](https://docs.docker.com/compose/install/).
- **Scala/Java:** The core backend is implemented in Scala. A compatible JDK and a Scala build tool (sbt) is necessary.
- **Python (Optional):** If using the Gradio interface, a Python environment with necessary packages (like Gradio) is required.
- **Node.js/npm (Optional):** If using or developing the Scala.js interface, [Node.js](https://nodejs.org/en/download) is necessary.

### Model Installation

After installing the dependencies, you can install the default models:

- **Chat model:** ```ollama pull qwen2.5-coder:32b``` then run ```ollama cp qwen2.5-coder:32b qwen2.5-coder-32b``` to ensure the model name has no `:`
- **Text embedding model:** ```ollama pull avr/sfr-embedding-mistral```
- **Code embedding model:** ```ollama pull nclemusclez/jina-embeddings-v2-base-code```
- **Reranker:** run the following in a terminal.

```bash
# Make sure git-lfs is installed (https://egit-lfs.com)
git lfs install

# From the root directory
git clone https://huggingface.co/jinaai/jina-reranker-v2-base-multilingual reranker/jina-reranker-v2-base-multilingual
```

## Running the App

```bash
# Run the backend (from the root directory)
sbt backend/run

# Running the frontend (from the root directory)
npm install
npm run dev

# Running all unit tests (without external services)
sbt "testOnly -- -l gitinsp.tests.tags.ExternalService"

# Running all integration tests that require external dependencies
sbt "testOnly -- -n gitinsp.tests.tags.ExternalService"

# Running all acceptance tests
sbt "backend/testOnly gitinsp.tests.requirements.*"

# Run only the ArchUnit tests
sbt "backend/testOnly gitinsp.tests.HexagonalTest gitinsp.tests.DesignPatternsTest gitinsp.tests.FunctionalProgrammingTest"
```


## Directory structure
To visualize the directory structure of the repository see [this link](https://uithub.com/atomwalk12/PPS-22-git-insp?accept=text%2Fhtml&maxTokens=50000&ext=scala).

Here is a brief summary of the code:

```plaintext
├── backend
    └── src
    │   ├── main
    │       └── scala
    │       │   └── gitinsp
    │       │       ├── application                      // High-level module
    │       │           ├── LangchainCoordinator.scala   // Coordinator for RAG pipeline and LLM interactions
    │       │           └── Main.scala                   // Entry point for the backend server
    │       │       ├── domain                           // Business logic
    │       │           ├── ChatService.scala            // Chat functionality
    │       │           ├── IngestorService.scala        // Used for generating indices from code and text
    │       │           ├── PipelineService.scala        // Key entry point that communicates with the app. layer
    │       │           ├── interfaces
    │       │           │   ├── application              // Interfaces for application layer services
    │       │           │   │   ├── ...
    │       │           │   └── infrastructure           // Interfaces for infrastructure layer components
    │       │           │   │   ├── ...
    │       │           └── models                       // Entities that define the domain model
    │       │           │   ├── ...
    │       │       └── infrastructure                   // External services
    │       │           ├── CacheService.scala           // Handles caching of expensive operations
    │       │           ├── ContentService.scala         // For formatting the content
    │       │           ├── FetchingService.scala        // HTTP wrapper used for fetching data
    │       │           ├── GithubWrapperService.scala   // Uses the FetchingService to fetch data from Github
    │       │           ├── QueryFilterService.scala
    │       │           ├── factories                    // Factory implementations for creating various objects
    │       │               ├── ...
    │       │           ├── parser                       // Utilities for parsing text and code
    │       │               ├── ...
    │       │           └── strategies                   // Implementations of various strategies
    │       │               ├── ...
    │   └── test
    │       └── scala
    │           ├── ArchUnit.scala                       // Tests enforcing architectural design
    │           ├── domain
    │               ├── ...
    │           ├── external                             // Tests relying on external services (Ollama)
    │               ├── ...
    │           ├── infrastructure                       // Tests for infrastructure (API wrapper)
    │               ├── ...
    │           └── requirements                         // Acceptance tests verifying project requirements
    │               ├── ...
└── frontend
    └── src
        └── main
            └── scala
                └── gitinsp
                    ├── Main.scala                       // Entry point for the Scala.js frontend application
                    ├── api                              // Handles API communication with the backend services
                        └── HttpClient.scala
                    ├── components                       // Reusable UI elements for building the web interface
                        ├── ...
                    ├── models
                        └── Models.scala                 // Frontend-specific data models
                    ├── services
                        └── ContentService.scala         // Service that coordinates communication with the backend
                    └── util
                        └── IDGenerator.scala
```
