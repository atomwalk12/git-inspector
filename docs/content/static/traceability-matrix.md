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
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>FR1.1</td>
      <td>inspectGitRepository</td>
      <td>inspectGitRepository.scala</td>
    </tr>
    <tr>
      <td>FR1.2</td>
      <td>searchCode</td>
      <td>searchCode.scala</td>
    </tr>
    <tr>
      <td>FR2.1</td>
      <td>buildVectorDatabase</td>
      <td>buildVectorDatabase.scala</td>
    </tr>
    <tr>
      <td>FR2.2</td>
      <td>optimizeSearch</td>
      <td>optimizeSearch.scala</td>
    </tr>
  </tbody>
</table>
