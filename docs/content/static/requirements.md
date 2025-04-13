| Req ID | Description                                                                                                                                                                     |
| :----- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| BR1    | (Functionality) Enable users to efficiently search and understand code within Git repositories.                                                                                 |
| BR2    | (Productivity) Improve developer productivity by streamlining code search/understanding workflows.                                                                              |
| FR1.1  | (Repository Input) As a user, I can specify a Git repository URL to inspect its code.                                                                                           |
| FR1.2  | (Search) As a user, I can search for code using keywords or natural language queries.                                                                                           |
| FR1.3  | (Search Results) As a user, I can view the search results with code snippets and links to the original files in the repository.                                                 |
| FR1.4  | (Filter) As a user, I can filter search results by programming language.                                                                                                        |
| FR1.5  | (Context) As a user, I can view the context around a code snippet in the search results.                                                                                        |
| FR1.6  | (Chat) As a user, I can use chat functionality to ask questions about the code, where the chat history is remembered.                                                           |
| FR2.1  | (Repository Cloning) The system should fetch and clone Git repositories from provided URLs.                                                                                     |
| FR2.2  | (Code Indexing) The system should index the code of the fetched repositories.                                                                           |
| FR2.3  | (Vector Database) The system should use a vector database to store code embeddings for semantic search.                                                                         |
| FR2.4  | (LLM Integration) The system should integrate with an LLM to process natural language queries and generate responses.                                                           |
| NFR1   | (Performance) The system should complete initial code extraction and indexing for repositories up to 500MB within 2 minutes on the defined target hardware specification.       |
| NFR2   | (Usability) The system interface should achieve a System Usability Scale (SUS) score of 70 or higher, based on usability testing with at least 5 representative target users.   |
| NFR3   | (Security) The system should sanitize user search query inputs to prevent Cross-Site Scripting (XSS) and OS Command Injection vulnerabilities.                                  |
| NFR4   | (Visualization) The system should include a visualization tool to display code embeddings in a 2D space for analyzing and improving code indexing and search quality.           |
| IR1    | (Implementation) The system's backend components should be implemented using Scala, adhering to the functional programming guidelines specified in .gemini/styleguide.md        |
| IR2    | (Architecture) The system should use Qdrant as the vector database for code embeddings.                                                                                         |
| IR3    | (Architecture) The system should integrate with Ollama for LLM functionalities.                                                                                                 |
| IR4    | (Architecture) The system should follow a layered architecture approach as described in the architectural overview diagram. This ensures separation of concerns and modularity. |
