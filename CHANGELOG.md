## [1.8.0](https://github.com/atomwalk12/PPS-22-git-insp/compare/v1.7.0...v1.8.0) (2025-05-05)

### Features

* **ci:** add GitHub Actions workflow for Codecov coverage reporting ([fe0cadc](https://github.com/atomwalk12/PPS-22-git-insp/commit/fe0cadc19f107cb371e3f985e33e3f9079a89329))
* **filter:** implement dynamic query filtering strategy with LLM classification ([3c9e268](https://github.com/atomwalk12/PPS-22-git-insp/commit/3c9e268d124806c11ca51bb9409a0b0441577b41))
* **models:** improve domain models and services with detailed documentation and new structures ([903ae05](https://github.com/atomwalk12/PPS-22-git-insp/commit/903ae05202583c4dea0b7940e24d9ff9fa82b7ef))
* **presentation:** add presentation website and update CI workflows ([ce79fd5](https://github.com/atomwalk12/PPS-22-git-insp/commit/ce79fd55cf102e8938e1b591058276c64bc34e59))

### Bug Fixes

* **llm-classifier:** use contents of the AI prompt instead of its parent class ([54c077c](https://github.com/atomwalk12/PPS-22-git-insp/commit/54c077ccb3260a8f2133b1e43ba3f216b96abf68))

## [1.7.0](https://github.com/atomwalk12/PPS-22-git-insp/compare/v1.6.0...v1.7.0) (2025-04-16)

### Features

* **backend:** add additional metadata information with chuink overlap and chunk size to aid debugging ([aff7b0a](https://github.com/atomwalk12/PPS-22-git-insp/commit/aff7b0a2fefd02a3e51f14cc921f3f42c076282a))
* **backend:** implement index removal functionality and enhance error handling ([155c969](https://github.com/atomwalk12/PPS-22-git-insp/commit/155c969425912d15973095724134bb93e973f4a0))
* **frontend:** add routes to the Vite configuration and update the Python ChatInterface for index removal ([c3e7c9a](https://github.com/atomwalk12/PPS-22-git-insp/commit/c3e7c9ac8b9ddcfe5a04a049a6cf23905d1fc56b))
* **frontend:** add styling and link viewer component ([69a2f9a](https://github.com/atomwalk12/PPS-22-git-insp/commit/69a2f9a1907775c459e9b6b1c4a38e7295cc7873))
* **frontend:** add the ability to fetch repository data, remove and generate indexes ([2563c18](https://github.com/atomwalk12/PPS-22-git-insp/commit/2563c1832a303dcc3129be153c20e216294d34ef))
* **frontend:** enhance chat interface with index selection and status bar ([a4e9234](https://github.com/atomwalk12/PPS-22-git-insp/commit/a4e9234dc0c59289e0a206a46df0795d61104989))
* **frontend:** implement chat interface and tab navigation ([a858840](https://github.com/atomwalk12/PPS-22-git-insp/commit/a8588406531c46b45a9aa50b0c1a2851baa5bd5b))
* **frontend:** improve styling and create basic layout for the link viewer section ([f61da46](https://github.com/atomwalk12/PPS-22-git-insp/commit/f61da4638dc5ef712abbcb929c3fd24feb859322))
* **frontend:** refactor chat interface and integrate HTTP client for streaming ([27250a2](https://github.com/atomwalk12/PPS-22-git-insp/commit/27250a219f32742a20b45aea5db5df0623b34898))

## [1.6.0](https://github.com/atomwalk12/PPS-22-git-insp/compare/v1.5.0...v1.6.0) (2025-04-13)

### Features

* **acceptance-tests:** add acceptance tests evaluating all requirements from the specification document ([e70b950](https://github.com/atomwalk12/PPS-22-git-insp/commit/e70b9500b6d8b62f1469d943a45fe0a8b4bf60e2))
* **cloud-services:** Enhance backend configuration to use a model factory that allows to switch model provider (i.e. ollama, gemini, clauide) ([16127e2](https://github.com/atomwalk12/PPS-22-git-insp/commit/16127e2a5f1db28da0ef503b767181f6cae9b420))
* **tests:** add acceptance tests for both business requirements as well as user functional requirements ([3c371d0](https://github.com/atomwalk12/PPS-22-git-insp/commit/3c371d02633bd1af2638f8676b2eedeb058a691a))

## [1.5.0](https://github.com/atomwalk12/PPS-22-git-insp/compare/v1.4.0...v1.5.0) (2025-04-09)

### Features

* **langchain-coordinator:** implement LangchainCoordinator for managing AI interactions ([eed65c4](https://github.com/atomwalk12/PPS-22-git-insp/commit/eed65c4669762e2b032d946535ab8433f02d08a6))
* **language-management:** introduce Category enum, separating coding languages from their categories ([d3632d6](https://github.com/atomwalk12/PPS-22-git-insp/commit/d3632d63eb1453cea065303c3743a0735c546cef))
* **refactor:** reorganize the architecture services to strictly follow the layered approach defined in the documentation ([3750435](https://github.com/atomwalk12/PPS-22-git-insp/commit/375043568bb3aef7280c7ec7a363d0c08ab16003))
* **tests:** add tests to assess the entire pipeline, including the API that interacts with the frontend. Includes integration tests. ([d35fd2d](https://github.com/atomwalk12/PPS-22-git-insp/commit/d35fd2d8edae7621be2bfbc38d53dabf46063076))

### Bug Fixes

* **github-integration:** improve GithubWrapperService and IngestorService for better repository fetching ([922a7d1](https://github.com/atomwalk12/PPS-22-git-insp/commit/922a7d199a0bf1abbb3a295b26ceed141d1f8c86))

## [1.4.0](https://github.com/atomwalk12/PPS-22-git-insp/compare/v1.3.1...v1.4.0) (2025-04-07)

### Features

* **github-integration:** add GithubWrapperService for repository handling ([908169f](https://github.com/atomwalk12/PPS-22-git-insp/commit/908169fadab61fc0d14e46a31748cd4cb42069c4))

## [1.3.1](https://github.com/atomwalk12/PPS-22-git-insp/compare/v1.3.0...v1.3.1) (2025-04-05)

### Bug Fixes

* **qdrant:** create separate collections for each file type ([e408855](https://github.com/atomwalk12/PPS-22-git-insp/commit/e408855e2acb66408512dfc954b6cb54d391f08b))

## [1.3.0](https://github.com/atomwalk12/PPS-22-git-insp/compare/v1.2.0...v1.3.0) (2025-04-04)

### Features

* **chat-interface:** add Gradio-based chat interface and link viewer ([35eaf58](https://github.com/atomwalk12/PPS-22-git-insp/commit/35eaf5812a2f1c65619a27b731de3a1103fd962a))
* **chat:** implement ChatService for AI interaction ([7cfddd9](https://github.com/atomwalk12/PPS-22-git-insp/commit/7cfddd98d8e786ba63934d21f6bea5941b0b6f0c))
* **configuration:** update application configuration and add URL sanitization utility ([379e9ee](https://github.com/atomwalk12/PPS-22-git-insp/commit/379e9ee3ae568303ac529e683caf6e120664884b))
* **ingestion-strategy:** introduce IngestionStrategy trait and default implementation ([c8a4b31](https://github.com/atomwalk12/PPS-22-git-insp/commit/c8a4b31697134c4aeb37d543b5ee1b1b8662b277))
* **ingestor:** implement IngestorService and integrate with Pipeline ([78702bb](https://github.com/atomwalk12/PPS-22-git-insp/commit/78702bbe72a454e7a8f02d5bb3346b8aaddd1649))
* **styles:** add CSS styles for content display area layout ([1a703b9](https://github.com/atomwalk12/PPS-22-git-insp/commit/1a703b97faa9fe4299dba83b086fec9a5ee5dee2))
* **tests:** add complete suites of tests for the ingestor module ([fa696be](https://github.com/atomwalk12/PPS-22-git-insp/commit/fa696be834bdb27a25b9d606f8f802b14c857711))
* **text-splitting:** introduce RecursiveCharacterTextSplitter and TextSplitter base class ([62010ea](https://github.com/atomwalk12/PPS-22-git-insp/commit/62010ea6dec5d6ca1c5b67fa55b5be2db37341e0))

### Bug Fixes

* **chat:** enhance error handling in chat function ([b7b21ff](https://github.com/atomwalk12/PPS-22-git-insp/commit/b7b21ffe5d6f837d22e95f9cf57e618175c327b0))

## [1.2.0](https://github.com/atomwalk12/PPS-22-git-insp/compare/v1.1.2...v1.2.0) (2025-04-03)

### Features

* **analysis:** enhance analysis strategies with streaming capabilities ([d56f1a0](https://github.com/atomwalk12/PPS-22-git-insp/commit/d56f1a0c0dd36a5a2fb6fa215f33d8b02a52a0d0))
* **analysis:** implement analysis strategies and context ([742149e](https://github.com/atomwalk12/PPS-22-git-insp/commit/742149e3037ee2fe8500aef40cde1d24decaff6a))
* **build:** update dependencies and configuration for reranker model ([9341f8f](https://github.com/atomwalk12/PPS-22-git-insp/commit/9341f8f01b88685589e3d81c4dc979697ffa2055))
* **cache:** introduce CacheService for AI service management ([dc45d4b](https://github.com/atomwalk12/PPS-22-git-insp/commit/dc45d4ba0958d08ab5cfb15cbcc4c18e575899f1))
* **embedding:** add language support and embedding model creation ([b5d2d3c](https://github.com/atomwalk12/PPS-22-git-insp/commit/b5d2d3ca5c0fd82d14bbd4e6fc0135031d60b12a))
* **formatter:** implement ContentFormatter for filtering text ([a586ebe](https://github.com/atomwalk12/PPS-22-git-insp/commit/a586ebe54d80d06f62520c0cf34b0f5bd54af283))
* **query-routing:** implement query routing strategies and factory ([cd9761e](https://github.com/atomwalk12/PPS-22-git-insp/commit/cd9761e404cf7a694869eb55fdbf8dee95596d30))
* **query-routing:** refine ConditionalQueryStrategy and enhance tests ([b81ac65](https://github.com/atomwalk12/PPS-22-git-insp/commit/b81ac65e40b987e51786ab2fa8d779906691e03c))
* **rag:** add retrieval augmentor creation to RAG component factory ([8ed4512](https://github.com/atomwalk12/PPS-22-git-insp/commit/8ed45121dfb89dabffb2cdfff4f124dd1d8ce0e2))
* **rag:** enhance RAG component factory with conditional routing and embedding models ([9b1a293](https://github.com/atomwalk12/PPS-22-git-insp/commit/9b1a293d188131dbb3e33b8b50687757d2794954))
* **rag:** enhance RAGComponentFactory with new methods for embedding models and Qdrant integration ([340a707](https://github.com/atomwalk12/PPS-22-git-insp/commit/340a70769d2005c277faae90080c6bf5f02c1b82))
* **reranking:** add reranker configuration and content aggregator to RAG pipeline ([838729a](https://github.com/atomwalk12/PPS-22-git-insp/commit/838729a39ef45a12376006646436b335f6106ef6))
* **tests:** add Mockito support and enhance AnalysisTest with new chat model interactions ([14c7e8e](https://github.com/atomwalk12/PPS-22-git-insp/commit/14c7e8efae9305fe31b488b62a691a513aac0a72))
* **tests:** add tests for markdown and code retriever creation in RAGComponentFactory ([a7e146b](https://github.com/atomwalk12/PPS-22-git-insp/commit/a7e146b61de797227b9db3c4bcaeb590a77ac95e))

## [1.1.2](https://github.com/atomwalk12/PPS-22-git-insp/compare/v1.1.1...v1.1.2) (2025-03-31)

### Bug Fixes

* **docs:** correct URL path in abslink shortcode for consistency ([e981a96](https://github.com/atomwalk12/PPS-22-git-insp/commit/e981a9697e80be07e11d6af13c324201dea997c6))

## [1.1.1](https://github.com/atomwalk12/PPS-22-git-insp/compare/v1.1.0...v1.1.1) (2025-03-31)

### Bug Fixes

* **docs:** correct file path for Layered Architecture diagram in product backlog ([d0953e4](https://github.com/atomwalk12/PPS-22-git-insp/commit/d0953e4912b948b5855b272bd21ce8a85dea6db6))
* **docs:** update file path for Layered Architecture diagram in product backlog ([93ea150](https://github.com/atomwalk12/PPS-22-git-insp/commit/93ea15004a6d559566ae7c97bb0ecb58594bd11d))
* **Github-Actions:** update GitHub Pages workflow to clean destination directory during build ([033d08c](https://github.com/atomwalk12/PPS-22-git-insp/commit/033d08c6105c8e05e86d5edcd514897b2b6dec55))

## [1.1.0](https://github.com/atomwalk12/PPS-22-git-insp/compare/v1.0.1...v1.1.0) (2025-03-26)

### Features

* **docs:** enhance documentation and CI/CD setup for Sprint 1 ([#20](https://github.com/atomwalk12/PPS-22-git-insp/issues/20)) ([52b3cee](https://github.com/atomwalk12/PPS-22-git-insp/commit/52b3cee5b15ce303262e1f1a1dfde3a5ccabf679))

## [1.0.1](https://github.com/atomwalk12/PPS-22-git-insp/compare/v1.0.0...v1.0.1) (2025-03-25)

### Bug Fixes

* **github-pages:** update concurrency settings in GitHub Actions workflow to prevent canceling in-progress deployments ([3948291](https://github.com/atomwalk12/PPS-22-git-insp/commit/3948291ea1a84af8a67fd6d6a8a0d182940dd94c))

## 1.0.0 (2025-03-24)

### Features

* add pre-commit hook for automated checks and staging ([17c057d](https://github.com/atomwalk12/PPS-22-git-insp/commit/17c057dc89b714658ca99e2b62419ef1aa0370f7))
* add product backlog and sprint documentation ([fa617a2](https://github.com/atomwalk12/PPS-22-git-insp/commit/fa617a25eac774117d5c323429d8a65c283725b7))
* add Scala formatting and style configuration ([90beb7a](https://github.com/atomwalk12/PPS-22-git-insp/commit/90beb7aa333c961c61f5ca81ccb7f18366766a9f))
* add setup script and main object for initial application structure ([f83cc9a](https://github.com/atomwalk12/PPS-22-git-insp/commit/f83cc9aaa8467526f4477dc336b20c23e17cb630))
* enable code coverage and update README with SBT commands ([7303b96](https://github.com/atomwalk12/PPS-22-git-insp/commit/7303b96098aae51b84e5e3537adc3dda3dd9f63a))
* implement git hook for enforcing semantic commit messages ([fcf2974](https://github.com/atomwalk12/PPS-22-git-insp/commit/fcf2974258dcedab4fec5f47bd8bdaf9855ab69c))
* initialise project ([3b36c69](https://github.com/atomwalk12/PPS-22-git-insp/commit/3b36c697ae90e0ca58aa9050cc9fa83968473767))
* integrate Scalafix for linting and code quality improvements ([a7d3a6f](https://github.com/atomwalk12/PPS-22-git-insp/commit/a7d3a6fe95cb90986de708f785a743048403e03d))
* integrate Trunk for enhanced linting and configuration management ([5399cc4](https://github.com/atomwalk12/PPS-22-git-insp/commit/5399cc40d19f7a6a5384cc6331ae926d1f89f23d))
* update README and add main object for Git Inspector ([606cab6](https://github.com/atomwalk12/PPS-22-git-insp/commit/606cab64585ec22dd4aea02acbfd675badd5e8b6))

### Bug Fixes

* adjust maxColumn settings in Scala formatting configuration ([602ab0b](https://github.com/atomwalk12/PPS-22-git-insp/commit/602ab0bd24c0a5c4f86535f70b2b0820737c85a3))
* **github-actions:** update permissions in release workflow to read for contents ([17efa6d](https://github.com/atomwalk12/PPS-22-git-insp/commit/17efa6d91bc5bc54c61af36a5587012b47185e1f))
* **github-pages:** correct syntax in GitHub Actions workflow for Hugo site deployment ([af47191](https://github.com/atomwalk12/PPS-22-git-insp/commit/af47191225d20522dce988d4c810df035f26abf8))
* **hook:** add package.json and package-lock.json for dependency management ([1242120](https://github.com/atomwalk12/PPS-22-git-insp/commit/1242120daffb3c0779792cf46391ab60be77bd8f))
* **hook:** enhance error message in pre-commit script ([c1011c5](https://github.com/atomwalk12/PPS-22-git-insp/commit/c1011c50051c1a4981cf4e5cab673b632d2a08de))
* **hook:** enhance pre-commit script error handling and logging ([6f10a82](https://github.com/atomwalk12/PPS-22-git-insp/commit/6f10a822b243606c5b69dba764e6a111d1ae5172))
* **hook:** improve pre-commit script output clarity ([5376f35](https://github.com/atomwalk12/PPS-22-git-insp/commit/5376f356e9fe1c6276982340235ada253ebb13ef))
* **package:** update JAR file path in package.json ([7a2790b](https://github.com/atomwalk12/PPS-22-git-insp/commit/7a2790b8742385e109db7ced723830d3ca370046))
* **pre-commit:** cherry pick [#c1011c50](https://github.com/atomwalk12/PPS-22-git-insp/issues/c1011c50) from main to hotfix/pre-commit. ([c4058a5](https://github.com/atomwalk12/PPS-22-git-insp/commit/c4058a520ed427d32ef52148059139410e4df08e))
* **website:** correct syntax error in ScalaDoc generation command in GitHub Actions workflow ([1737c0a](https://github.com/atomwalk12/PPS-22-git-insp/commit/1737c0ad4e64110ef94d411d6b8a97cf5b117e35))
