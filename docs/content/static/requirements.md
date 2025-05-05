| Req ID | Description                                                                                                    |
| :----- | :------------------------------------------------------------------------------------------------------------- |
| BR1    | Allow users to efficiently search and understand code within Git repositories.                                 |
| BR2    | Improve developer productivity by facilitating code search/understanding workflows.                            |
| FR1.1  | As a user, I can specify a Git repository URL to inspect its code.                                             |
| FR1.2  | As a user, I can search for code using keywords or natural language queries.                                   |
| FR1.3  | As a user, I can view the search results with code snippets and links to the original files in the repository. |
| FR1.4  | As a user, I can filter search results by programming language.                                                |
| FR1.5  | As a user, I can view the context around a code snippet in the search results.                                 |
| FR1.6  | As a user, I can ask code-related questions via chat, and the chat history is preserved.                       |
| FR2.1  | As a developer, I need the system to fetch and clone Git repositories from provided URLs.                      |
| FR2.2  | As a developer, I need the system to index the code of the fetched repositories, to generate fast responses.   |
| FR2.3  | As a developer, I need the system to use a vector database to store code embeddings for semantic search.       |
| FR2.4  | As a developer, I need the system to integrate with an LLM to process natural language queries.                |
| NFR1   | The system will index code for targeted repositories within 10 seconds on the specified hardware.              |
| NFR2   | The system should achieve a System Usability Scale (SUS) score of 70+ based on at least 5 target users.        |
| NFR3   | The system should sanitize user search query inputs to prevent Cross-Site Scripting (XSS) attacks.             |
| NFR4   | A 2D visualization tool will display code embeddings to help analyze and improve indexing and search.          |
| IR1    | The system should be implemented in Scala, following functional programming principles.                        |
| IR2    | The system should use Qdrant as the vector database for code embeddings.                                       |
| IR3    | The system should integrate with Ollama for LLM functionalities.                                               |
| IR4    | The system should follow a layered architecture approach, ensuring better modularity.                          |
