<!-- trunk-ignore-all(markdownlint/MD041) -->
[TODO] To trace the requirements to the design elements, I will use a traceability matrix. This matrix will be
populated by running unit tests, which will populate a table with the following format:

<!-- markdownlint-disable MD033 -->
<table style="display: table;">
  <thead>
    <tr>
      <th>Requirement</th>
      <th>Design element</th>
      <th>Implementation Evidence</th>
      <th>Done</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>BR1: Search Productivity</td>
      <td>Project-wide</td>
      <td>BusinessRequirementsSuite.scala<br>SUS Questionnaire<br>Embedding Diagrams</td>
      <td>Still need SUS Questionnaire and Embedding Diagrams</td>
    </tr>
    <tr>
      <td>BR2: Improve Code Understanding</td>
      <td>Project-wide</td>
      <td>createTextEmbeddingModel<br>createCodeEmbeddingModel<br>Python/Scala Frontend<br>SUS Questionnaire<br>Embedding Diagrams</td>
      <td>Still need SUS Questionnaire and Embedding Diagrams</td>
    </tr>
    <tr>
      <td>FR1.1: Repository URL Input Interface</td>
      <td>GithubWrapperService.scala</td>
      <td>UserFunctionalRequirementsSuite.scala</td>
      <td>✓</td>
    </tr>
    <tr>
      <td>FR1.2: Code Search Using Markdown</td>
      <td>QdrantEmbeddingStore.scala<br>GithubWrapperService.scala</td>
      <td>UserFunctionalRequirementsSuite.scala</td>
      <td>✓</td>
    </tr>
    <tr>
      <td>FR1.3: Search Results Display System</td>
      <td>Scala frontend<br>Python frontend</td>
      <td>SUS Questionnaire</td>
      <td>Still need SUS Questionnaire</td>
    </tr>
    <tr>
      <td>FR1.4: Code Search using Code</td>
      <td>QdrantEmbeddingStore.scala<br>GithubWrapperService.scala</td>
      <td>UserFunctionalRequirementsSuite.scala</td>
      <td>✓ (see related FR1.2)</td>
    </tr>
    <tr>
      <td>FR1.5: Code Context Visualization</td>
      <td>Scala frontend<br>Python frontend<br>RepositoryWithLanguages<br>GithubWrapperService</td>
      <td>UserFunctionalRequirementsSuite.scala<br>SUS Questionnaire</td>
      <td>Still need SUS Questionnaire</td>
    </tr>
    <tr>
      <td>FR1.6: Model with Past Chat History</td>
      <td>Pipeline.scala<br>RAGComponentFactory.scala</td>
      <td>UserFunctionalRequirementsSuite.scala</td>
      <td>✓</td>
    </tr>
    <tr>
      <td>FR2.1: Repository Cloning and Management</td>
      <td>GithubWrapperService.scala<br>FetchingService.scala</td>
      <td>SystemFunctionalRequirementsSuite.scala</td>
      <td>✓</td>
    </tr>
    <tr>
      <td>FR2.2: Vector Database Generation for RAG</td>
      <td>IngestorService.scala<br>CacheService.scala<br>QdrantEmbeddingStore.scala</td>
      <td>SystemFunctionalRequirementsSuite.scala</td>
      <td>✓</td>
    </tr>
    <tr>
      <td>FR2.3: Vector Database Implementation</td>
      <td>QdrantEmbeddingStore.scala<br>GithubWrapperService.scala</td>
      <td>UserFunctionalRequirementsSuite.scala</td>
      <td>✓</td>
    </tr>
    <tr>
      <td>FR2.4: LLM Integration for Code Understanding</td>
      <td>QueryRoutingStrategy.scala<br>QueryFilterService.scala<br>ChatService.scala</td>
      <td>SystemFunctionalRequirementsSuite.scala</td>
      <td>✓</td>
    </tr>
    <tr>
      <td>NFR1: System Performance Optimization</td>
      <td>ChatService.scala<br>CacheService.scala<br>IngestorService.scala<br>GithubWrapperService.scala</td>
      <td>NonFunctionalRequirementsSuite.scala</td>
      <td>✓</td>
    </tr>
    <tr>
      <td>NFR2: System Usability Optimization</td>
      <td>GithubWrapperService.scala<br>Scala frontend<br>Python frontend</td>
      <td>NonFunctionalRequirementsSuite.scala</td>
      <td>Need to add SUS Questionnaire</td>
    </tr>
    <tr>
      <td>NFR3: User Interface Security</td>
      <td>Scala frontend<br>Python frontend</td>
      <td>NonFunctionalRequirementsSuite.scala</td>
      <td>✓</td>
    </tr>
    <tr>
      <td>NFR4: Embedding Visualization</td>
      <td>IngestorService.scala</td>
      <td>Final Report</td>
      <td>Need to add visualization to the report</td>
    </tr>
    <tr>
      <td>IR1: Scala Implementation</td>
      <td>Project-wide</td>
      <td></td>
      <td>✓</td>
    </tr>
    <tr>
      <td>IR2: Qdrant Vector Database</td>
      <td>IngestorService.scala<br>ComponentFactory.scala</td>
      <td>application.conf</td>
      <td>✓</td>
    </tr>
    <tr>
      <td>IR3: Ollama Integration</td>
      <td>QueryRoutingStrategy.scala<br>QueryFilterService.scala<br>ChatService.scala</td>
      <td>application.conf</td>
      <td>✓</td>
    </tr>
    <tr>
      <td>IR4: Layered Architecture</td>
      <td>Project-wide</td>
      <td>ArchUnit tests</td>
      <td>Need to implement the ArchUnit tests</td>
    </tr>
  </tbody>
</table>
