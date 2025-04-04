<!-- trunk-ignore-all(markdownlint/MD041) -->
# Agile Board: Project Tasks

<!-- markdownlint-disable MD041 MD033 MD056 -->

## Task Board

*Based on the [Requirements Specifications](../static/requirement-specifications.md) document.*

Link to the other product backlogs:
- {{< abslink url="/process/sprint1/sprint_backlog#task-board" text="Sprint 1">}}
- {{< abslink url="/process/sprint2/sprint_backlog#task-board" text="Sprint 2">}}


{{< responsive-table>}}

| ID              | Task                                                                                                                                                                                                    | Priority | Related Requirements                  | Status | Tests | Sprint |
| --------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------- | ------------------------------------- | ------ | ----- | ------ |
| **FOUNDATION**  |
| F1              | **Layered Architecture Implementation** <br>- Implement modules from architecture diagram<br>- Apply design patterns from lectures (dependency injection, layered architecture, strategy, factory, etc) | HIGHEST  | IR4, IR1                              | To Do  | TODO  | 2      |
| F2              | **Repository Input and Processing** <br>- Accept/validate Git repository URLs<br>- Fetch repository contents<br>- Support file type filtering                                                           | HIGHEST  | FR1.1, FR2.1, NFR3, BR1, NFR1         | To Do  |       |        |
| F3              | **Code Indexing System** <br>- Process code into searchable representations<br>- Generate/store code embeddings<br>- Integrate with Langchain4J for vector storage                                      | HIGHEST  | FR2.2, FR2.3, IR2, IR3, BR2, NFR1     | To Do  |       |        |
| **CORE VALUE**  |
| C1              | **Natural Language Code Search** <br>- Enable semantic search across codebase<br>- Support language/extension filtering<br>- Display relevant results with context                                      | HIGH     | FR1.2, FR1.4, FR1.3, FR1.5, BR1, NFR2 | To Do  |       |        |
| C2              | **Code Understanding Chat Interface** <br>- Provide chat interface for code questions<br>- Retrieve relevant code context for queries<br>- Generate context-aware responses                             | HIGH     | FR1.6, FR1.5, FR2.4, BR2, NFR2        | To Do  |       |        |
| **ENHANCEMENT** |
| E1              | **User Interface Implementation** <br>- Create intuitive frontend for all functionality<br>- Support Scala.js and Python (Gradio) interfaces<br>- Ensure responsive and accessible design               | MEDIUM   | NFR2, IR1, NFR3                       | To Do  |       |        |
| E2              | **Performance Optimization** <br>- Optimize repository lookup speed<br>- Implement caching/reuse of embeddings<br>- Ensure responsive search/chat experience                                            | MEDIUM   | NFR1, FR2.2, FR2.3, NFR2              | To Do  |       |        |
| E3              | **Security Implementation** <br>- Sanitize user inputs<br>- Secure Restful API<br>- Secure data storage                                                                                                 | MEDIUM   | NFR3, FR2.3                           | To Do  |       |        |
| E4              | **Testing and Quality Assurance** <br>- Implement comprehensive testing strategy<br>- Set up CI/CD pipeline<br>- Define quality metrics and thresholds                                                  | MEDIUM   | IR1, NFR1, NFR2                       | To Do  |       |        |
| E5              | **Visualization and Analysis** <br>- Visualize code embeddings for quality analysis<br>- Provide metrics on search effectiveness<br>- Generate insights for documentation                               | LOW      | IR5, FR1.2, BR2                       | To Do  |       |        |
| E6              | **Documentation and Reporting** <br>- Create user documentation<br>- Generate project report<br>- Document architecture and design decisions                                                            | LOW      | NFR2, IR4                             | To Do  |       |        |

{{</ responsive-table>}}

## Legend
- **Task Categories**:
  - **F** = Foundation tasks
  - **C** = Core Value tasks
  - **E** = Enhancement tasks

## Requirements Reference

- **Business Requirements (BR):**
    - BR1: Boost Productivity
    - BR2: Improve Understanding

- **Functional Requirements (FR):**
    - **User Requirements (FR1.x):**
        - FR1.1: Input Repository URL
        - FR1.2: Search Code (NL/Keyword)
        - FR1.3: Display Search Results
        - FR1.4: Filter by Language
        - FR1.5: Show Code Context
        - FR1.6: Implement Code Chat
    - **System Requirements (FR2.x):**
        - FR2.1: Clone/Manage Repositories
        - FR2.2: Index Code (Embeddings)
        - FR2.3: Implement Vector DB (Qdrant)
        - FR2.4: Integrate LLM (Ollama/Langchain4J)

- **Non-Functional Requirements (NFR):**
    - NFR1: Ensure Performance
    - NFR2: Ensure Usability
    - NFR3: Ensure Security (Input)

- **Implementation Requirements (IR):**
    - IR1: Use Functional Programming (Backend)
    - IR2: Use Qdrant
    - IR3: Use Ollama
    - IR4: Use Layered Architecture
    - IR5: Visualize Embeddings
