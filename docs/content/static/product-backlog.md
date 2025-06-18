
<style>
/* Override the global style for this page only */
.custom_table_style th:last-child {
  width: auto !important;
}
</style>

<!-- trunk-ignore-all(markdownlint/MD041) -->
# Project Tasks

<!-- markdownlint-disable MD041 MD033 MD056 -->

*Based on the [Requirements Specifications](../static/requirement-specifications.md) document.*

The project was divided into 5 separate sprints, each with its focus described below:

- {{< abslink url="/process/docs/sprint1/sprint_backlog#task-board" text="Sprint 1">}}: project setup
- {{< abslink url="/process/docs/sprint2/sprint_backlog#task-board" text="Sprint 2">}}: design patterns and indexing
- {{< abslink url="/process/docs/sprint3/sprint_backlog#task-board" text="Sprint 3">}}: search and chat interface
- {{< abslink url="/process/docs/sprint4/sprint_backlog#task-board" text="Sprint 4">}}: performance, visualization and documentation
- {{< abslink url="/process/docs/sprint5/sprint_backlog#task-board" text="Sprint 5">}}: final report and presentation


{{< responsive-table>}}

| ID             | Task                                                                                                                                                                                                           | Priority | Related Requirements                  | Status | Sprint (click)                                                                                                                                             |
| -------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------- | ------------------------------------- | ------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Github**     |                                                                                                                                                                                                                |          |                                       |        |                                                                                                                                                            |
| I1             | **Build Configuration** <br>- Initialize SBT project with Scala 3.6.4<br>- Configure assembly plugin for JAR creation<br>- Set up test environment with ScalaTest<br>- Configure code coverage & documentation | HIGHEST  | IR1, IR4, NFR1                        | ✓      | <a href="/git-inspector//process/docs/sprint1/sprint_backlog#task-board">S1</a>                                                                               |
| I2             | **Code Quality Tools** <br>- Set up Scalafmt with formatting rules<br>- Implement Wartremover for code analysis<br>- Configure Scalafix and semantic DB<br>- Set up Trunk and Gemini bot                       | HIGH     | IR1, NFR2                             | ✓      | <a href="/git-inspector//process/docs/sprint1/sprint_backlog#task-board">S1</a>                                                                               |
| I3             | **Git Workflow** <br>- Implement git hooks system<br>- Set up semantic release system                                                                                                                          | MEDIUM   | NFR1, NFR2                            | ✓      | <a href="/git-inspector//process/docs/sprint1/sprint_backlog#task-board">S1</a>                                                                               |
| I4             | **Project Infrastructure** <br>- Set up logging infrastructure<br>- Configure CI/CD pipeline<br>- Define high-level architecture                                                                               | HIGHEST  | IR4, NFR1, NFR2                       | ✓      | <a href="/git-inspector//process/docs/sprint1/sprint_backlog#task-board">S1</a>                                                                               |
| I5             | **Core Domain Model** <br>- Design repository data model<br>- Design initial API contracts<br>- Generate API documentation                                                                                     | HIGH     | IR1, IR4, FR2.1                       | ✓      | <a href="/git-inspector//process/docs/sprint1/sprint_backlog#task-board">S1</a>, <a href="/git-inspector//process/docs/sprint5/sprint_backlog#task-board">S5</a> |
| I6             | **Basic Git Operations** <br>- Implement repository loading<br>- Extract repository metadata<br>- Create error handling                                                                                        | HIGH     | FR1.1, FR2.1, NFR3                    | ✓      | <a href="/git-inspector//process/docs/sprint1/sprint_backlog#task-board">S1</a>                                                                               |
| **Foundation** |
| F1             | **Layered Architecture Implementation** <br>- Implement modules from architecture diagram<br>- Apply design patterns from lectures (dependency injection, layered architecture, strategy, factory, etc)        | HIGHEST  | IR4, IR1                              | ✓      | <a href="/git-inspector//process/docs/sprint2/sprint_backlog#task-board">S2</a>                                                                               |
| F2             | **Repository Input and Processing** <br>- Accept/validate Git repository URLs<br>- Fetch repository contents<br>- Support file type filtering                                                                  | HIGHEST  | FR1.1, FR2.1, NFR3, BR1, NFR1         | ✓      | <a href="/git-inspector//process/docs/sprint2/sprint_backlog#task-board">S2</a>                                                                               |
| F3             | **Code Indexing System** <br>- Process code into searchable representations<br>- Generate/store code embeddings<br>- Integrate with Langchain4J for vector storage                                             | HIGHEST  | FR2.2, FR2.3, IR2, IR3, BR2, NFR1     | ✓      | <a href="/git-inspector//process/docs/sprint2/sprint_backlog#task-board">S2</a>                                                                               |
| **Core Value** |
| C1             | **Natural Language Code Search** <br>- Enable semantic search across codebase<br>- Support language/extension filtering<br>- Display relevant results with context                                             | HIGH     | FR1.2, FR1.4, FR1.3, FR1.5, BR1, NFR2 | ✓      | <a href="/git-inspector//process/docs/sprint3/sprint_backlog#task-board">S3</a>                                                                               |
| C2             | **Code Understanding Chat Interface** <br>- Provide chat interface for code questions<br>- Retrieve relevant code context for queries<br>- Generate context-aware responses                                    | HIGH     | FR1.6, FR1.5, FR2.4, BR2, NFR2        | ✓      | <a href="/git-inspector//process/docs/sprint3/sprint_backlog#task-board">S3</a>                                                                               |
| **Additional** |
| E1             | **User Interface Implementation** <br>- Create intuitive frontend for all functionality<br>- Support Scala.js and Python (Gradio) interfaces<br>- Ensure responsive and accessible design                      | MEDIUM   | NFR2, IR1, NFR3                       | ✓      | <a href="/git-inspector//process/docs/sprint3/sprint_backlog#task-board">S3</a>                                                                               |
| E2             | **Performance Optimization** <br>- Optimize repository lookup speed<br>- Implement caching/reuse of embeddings<br>- Ensure responsive search/chat experience                                                   | MEDIUM   | NFR1, FR2.2, FR2.3, NFR2              | ✓      | <a href="/git-inspector//process/docs/sprint4/sprint_backlog#task-board">S4</a>                                                                               |
| E3             | **Security Implementation** <br>- Sanitize user inputs<br>- Secure Restful API<br>- Secure data storage                                                                                                        | MEDIUM   | NFR3, FR2.3                           | ✓      | <a href="/git-inspector//process/docs/sprint4/sprint_backlog#task-board">S4</a>                                                                               |
| E4             | **Testing and Quality Assurance** <br>- Implement comprehensive testing strategy<br>- Define quality metrics (see requirements document)                                                                       | MEDIUM   | IR1, NFR1, NFR2                       | ✓      | <a href="/git-inspector//process/docs/sprint4/sprint_backlog#task-board">S4</a>, <a href="/git-inspector//process/docs/sprint5/sprint_backlog#task-board">S5</a> |
| E5             | **Visualization and Analysis** <br>- Visualize code embeddings for quality analysis<br>- Provide metrics on search effectiveness<br>- Generate insights for documentation                                      | MEDIUM   | IR5, FR1.2, BR2                       | ✓      | <a href="/git-inspector//process/docs/sprint5/sprint_backlog#task-board">S5</a>                                                                               |
| E6             | **Documentation and Reporting** <br>- Create user documentation<br>- Generate project report<br>- Document architecture and design decisions                                                                   | MEDIUM   | NFR2, IR4                             | ✓      | <a href="/git-inspector//process/docs/sprint5/sprint_backlog#task-board">S5</a>                                                                               |

{{</ responsive-table>}}

# Traceability Matrix

The following table shows evidence for the requirements in the [Requirements Specifications](../static/requirement-specifications.md) document.

{{< insert-text "traceability-matrix.md" >}}

# Test Results

The acceptance tests are not executable using the traditional CI/CD pipeline, so below are the results ran locally.

{{< numbered-figure id="fig:sequence-diagram-chat" align="center" src="../../static/figures/acceptance-tests-results.png" caption="Sample run of the unit tests, including the acceptance tests" >}}
