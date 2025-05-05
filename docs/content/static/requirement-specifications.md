```md
Code Search Productivity (BR1)

- Choice: Enable developers to efficiently search and understand code within Git repositories.
  - Rationale: Developers spend significant time searching through codebases, and improving this process directly impacts productivity.
  - Validation Criteria:
    - At least 85\% of test users report improved workflow efficiency in post-usage surveys (SUS).
    - Average query-to-result time under 10 seconds for the predetermined repositories.
  - Implementation Considerations:
    - Ensure integration with common development workflows. This can be done using Gradio interfaces.
    - Focus on search result quality and relevance (i.e. analyze the effectiveness of the generated embeddings).
  - Related Requirements:
    - FR1.2 (Search)
    - FR1.5 (Context)
    - FR1.6 (Chat)
```


```md
Improving Code Understanding (BR2)

- Choice: Improve developer productivity by facilitating code search/understanding workflows.
  - Rationale: Understanding existing code is often more time-consuming than writing new code.
  - Validation Criteria:
    - Code explanations rated as "accurate and helpful" by at least 70\% of test users (SUS).
  - Implementation Considerations:
    - Implement contextual code explanations (i.e. use separate models that understand code and natural language).
    - Provide relationship visualization between embedding by using a 2D visualization tool.
    - Prioritize speed and accuracy in responses by allowing the users to select any open source model.
  - Related Requirements:
    - FR1.6 (Chat)
    - FR2.4 (LLM integration)
    - NFR2 (Usability)
    - NFR4 (Visualization)
```


```md
Repository URL Input Interface (FR1.1)

- Choice: As a user, I can specify a Git repository URL to inspect its code, so that I can access and analyze specific codebases I'm interested in.
  - Rationale: The system needs a secure and user-friendly way to fetch Git repository URLs.
  - Validation Criteria:
    - The acceptance tests parse valid GitHub URLs successfully.
    - Error feedback displayed within 2 seconds of validation failure.
    - URL validation completes within half a second for all inputs.
  - Implementation Considerations:
    - Implement URL validation and display clear feedback in case of parsing errors.
  - Related Requirements:
    - FR2.1 (Repository Cloning)
    - NFR3 (Security)
```


```md
Code Search Using Markdown (FR1.2)

- Choice: As a user, I can search for code using keywords or natural language, so that I can quickly find relevant code sections without manually browsing through files.
  - Rationale: This allows users to search for code using natural language, making it easier to find relevant code sections.
  - Validation Criteria:
    - Search results return in under 2 seconds for the predetermined repositories.
    - Language filtering correctly categorizes at least 95\% of code files.
  - Implementation Considerations:
    - Use embedding model to convert natural language queries to vectors.
    - Support filtering by language, content type and extension.
  - Related Requirements:
    - FR2.2 (Code Indexing)
    - FR2.3 (Vector Database)
    - NFR1 (Performance)
```

```md
Search Results Display System (FR1.3)

- Choice: As a user, I can view the search results with code snippets and links to the original files in the repository, so that I can efficiently evaluate search results and navigate to the full context when needed.
  - Validation Criteria:
    - 95\% of users can correctly identify file locations from the display assessed via the SUS survey.
    - Code snippets maintain proper indentation and formatting (Python, Scala frontend).
  - Implementation Considerations:
    - Display code snippets with syntax highlighting (Python).
    - Show file path and location information.
  - Related Requirements:
    - FR1.2 (Search)
    - FR1.5 (Context)
    - NFR2 (Usability)
```

```md
Code Search using Code Embeddings (FR1.4)

- Choice: As a user, I can filter search results by programming language, so that I can focus on code written in languages relevant to my current task.
  - Rationale: Developers often need to restrict searches to specific languages or file types.
  - Validation Criteria:
    - Language detection accuracy >95\% across all common programming languages (see partser impl.).
    - Multiple simultaneous filters function correctly in 100\% as assessed by the acceptance tests.
  - Implementation Considerations:
    - Detect and classify programming languages during indexing.
    - Create efficient language metadata for fast filtering.
    - Support multiple simultaneous language filters.
    - Include language identification in UI.
  - Related Requirements:
    - FR1.2 (Search)
    - FR1.3 (Search Results)
    - FR2.2 (Code Indexing)
```


```md
Code Context Visualization (FR1.5)

- Choice: As a user, I can view the context around a code snippet in the search results, so that I can better understand how the code fits into the broader implementation.
  - Rationale: This allows users to view the context around a code snippet in the search results, making it easier to understand how the code fits into the broader implementation.
  - Validation Criteria:
    - Fetching a repository loads within 5 seconds for the predetermined repositories.
    - 90\% of users report sufficient context for understanding code purpose (SUS)
  - Implementation Considerations:
    - Display the entire code being indexed in the search results.
    - When answering questions, display relevant snippets from the codebase.
    - Allow the user to switch between the full text and retrieved snippets.
  - Related Requirements:
    - FR1.3 (Search Results)
    - NFR2 (Usability)
```

```md
Model with Past Chat History (FR1.6)

- Choice: As a user, I can ask code-related questions via chat, and the chat history is preserved, so that I can have a continuous conversation with the system.
  - Rationale: This allows users to have a continuous conversation with the system, making it easier to understand how the code fits into the broader implementation.
  - Validation Criteria:
    - Context-aware responses remain relevant for at least 2 consecutive related questions.
    - Chat history can be cleared by regenerating the index.
  - Implementation Considerations:
    - Maintain chat history within session scope.
    - Structure LLM prompts to include chat history and retrieved code.
  - Related Requirements:
    - FR2.4 (LLM Integration)
    - FR2.3 (Vector Database)
    - NFR2 (Usability)
```

```md
Repository Cloning and Management (FR2.1)

- Choice: As a developer, I need the system to fetch and clone Git repositories from provided URLs, so that I can work with up-to-date code without performing these operations manually.
  - Rationale: This allows developers to work with up-to-date code without performing these operations manually.
  - Validation Criteria:
    - Predetermined repositories clone successfully within 30 seconds.
    - UI remains responsive (no blocking) during 100\% of cloning operations.
    - Invalid repository URLs are handled gracefully.
  - Implementation Considerations:
    - Use Uithub for extracting the repository code.
    - Implement caching mechanism for previously cloned repositories.
    - Add repository verification to ensure valid Git URLs.
  - Related Requirements:
    - FR2.2 (Code Indexing)
    - NFR1 (Performance)
    - NFR3 (Security)
```

```md
Vector Database Generation for RAG (FR2.2)

- Choice: As a developer, I need the system to index the code of the fetched repositories, so that I can perform fast and accurate searches across the entire codebase.
  - Validation Criteria:
    - The clusters generated by the embeddings are well defined, suggesting successful embedding generation.
    - Metadata correctly captures language and file type.
  - Implementation Considerations:
    - Use Qdrant for caching code embeddings.
    - Utilize metadata to enhance search results relevance.
  - Related Requirements:
    - FR2.2 (Code Indexing)
    - NFR1 (Performance)
```

```md
Semantic Search (FR2.3)

- Choice: As a developer, I need the system to use a vector database to store code embeddings, so that I can perform semantic searches that understand code context beyond simple keyword matching.
  - Validation Criteria:
    - Queries complete in less than 300ms for the predetermined repositories.
    - Vector similarity scores correctly correlate with semantic relevance as determined by the cluster analysis.
  - Implementation Considerations:
    - Configure Qdrant collection schema for code embeddings.
    - Use metadata for filtering specific file types.
  - Related Requirements:
    - FR1.2 (Search)
    - FR2.2 (Code Indexing)
    - NFR1 (Performance)
    - IR2 (Qdrant)
```

```md
LLM Integration for Natural Language Queries (FR2.4)

- Choice: As a developer, I need the system to integrate with an LLM to process natural language queries, so that I can interact with the codebase using plain English rather than specialized query syntax.
  - Validation Criteria:
    - Ollama integration successfully handles queries within tests without errors.
  - Implementation Considerations:
    - Implement prompt engineering techniques to guide responses (i.e. conditional RAG, search by file type).
    - Design context management for large repositories (limit the amount of tokens being processed).
  - Related Requirements:
    - FR1.6 (Chat Functionality)
    - NFR1 (Performance)
    - NFR3 (Security)
```

```md
System Performance Optimization (NFR1)

- Choice: The system will index code for targeted repositories within 10 seconds on the specified hardware.
  - Success Criteria:
    - Search queries return results in under 40 seconds for the predetermined repositories.
    - Embedding generation completes in under 30 seconds for the predetermined repositories.
    - Chat responses for simple search (without code context) arrives within 20 seconds for all tests.
  - Implementation Considerations:
    - Embeddings are stored in the Qdrant vector database.
    - The embeddings for retrieving chunks are generated using Ollama.
  - Related Requirements:
    - FR2.1 (Repository Cloning)
    - FR2.2 (Code Indexing)
    - FR1.2 (Search)
    - FR1.6 (Chat)
```


```md
System Usability Testing (NFR2)

- Choice: The system should achieve a System Usability Scale (SUS) score of 70+ based on at least 5 target users.
  - Rationale: This ensures that the system is usable, as evaluated by a group of users.
  - Validation Criteria:
    - First-time users find the interface easy to use without assistance in >70\% of cases (SUS)
    - 80\% of users rate UI intuitiveness as "good" or "excellent" (SUS)
  - Implementation Considerations:
    - Implement clean, intuitive UI. Rely on established UX design patterns by using Gradio components.
  - Related Requirements:
    - FR1.3 (Search Results)
    - FR1.5 (Context)
    - FR1.6 (Chat)
```

```md
User Interface Security (NFR3)

- Choice: The system should sanitize user search query inputs to prevent Cross-Site Scripting (XSS) attacks.
  - Validation Criteria:
    - 100\% of malformed/malicious URLs rejected before processing
  - Implementation Considerations:
    - Validate repository URLs against known valid patterns.
    - Test against standard Github URLs.
  - Related Requirements:
    - FR1.1 (Repository Input)
    - FR1.2 (Search)
    - FR1.6 (Chat)
```


```md
Embedding Visualization Requirement (NFR4)

- Choice: A 2D visualization tool will display code embeddings to help analyze and improve indexing and search.
  - Validation Criteria:
    - Visualization correctly clusters similar code types.
    - Report analysis identifies strategies for improving search quality.
  - Implementation Considerations:
    - Implement dimension reduction techniques (t-SNE, UMAP) for 2D visualization.
    - Used to make informed decisions about the search quality.
  - Related Requirements:
    - FR2.2 (Code Indexing)
    - FR2.3 (Vector Database)
```


```md
Scala Implementation Requirement (IR1)

- Choice: The system should be implemented in Scala, following functional programming principles.
  - Success Criteria:
    - Scala tools are used to ensure consisent.
    - Functional programming patterns are used to ensure consistent code quality.
  - Implementation Considerations:
    - Use appropriate abstraction mechanisms (strategies, factories, memoization, etc.)
    - Implement error handling using functional approaches (Try, Option)
  - Related Requirements:
    - FR2.2 (Code Indexing)
    - FR2.3 (Vector Database)
    - FR2.4 (LLM Integration)
```

```md
Qdrant Vector Database Requirement (IR2)

- Choice: The system should use Qdrant as the vector database for code embeddings.
  - Rationale: Qdrant provides efficient vector search capabilities with filtering options needed for code search.
  - Validation Criteria:
    - Qdrant client wrapper handles all required vector operations.
    - Collection schemas designed for code embeddings and text embeddings.
  - Implementation Considerations:
    - Implement the AIServices wrapper around the Qdrant module. Configure distance metrics.
  - Related Requirements:
    - FR2.3 (Vector Database)
    - NFR1 (Performance)
```

```md
Ollama Integration Requirement (IR3)

- Choice: The system should integrate with Ollama for LLM functionalities.
  - Rationale: Ollama provides locally-hosted LLM models, yielding good privacy and reduced latency.
  - Success Criteria:
    - The application successfully communicates with Ollama.
  - Implementation Considerations:
    - Implement the AIServices wrapper around the Ollama module.
    - Use prompt templates optimized for code understanding.
  - Related Requirements:
    - FR1.6 (Chat)
    - FR2.4 (LLM Integration)
    - NFR1 (Performance)
```


```md
Layered Architecture (IR4)

- Choice: The system should follow a layered architecture approach, ensuring better modularity.
  - Success Criteria:
    - All components separated into Presentation, Application, Domain, and Infrastructure layers.
    - The separation is assessed via ArchUnit tests.
```
