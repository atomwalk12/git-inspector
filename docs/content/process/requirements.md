---
title: "Requirements Engineering"
author: "Razvan"
date: 2025-03-27
description: "Defining the requirements for the Git Inspector project"
aliases: ["requirements"]
tocopen: true
tags: ["requirements"]
summary: "The requirements engineering step where we define all 5 requirement types: Business, Functional (user and system), Non-Functional, Implementation."
---

## Workflow

![PPS Workflow](../../static/figures/PPS-workflow.svg)

## Requirements Definition

For more details on the requirements engineering process, please refer to [day 4](sprint1/daily_updates/2025-03-27.md) of the first sprint.

{{< insert-text "requirements.md" >}}

Hardware specification:

- CPU: AMD Radeon 7950X3D
- RAM: 64GB
- Storage: SSD

## Design Representation

### Domain Model

The domain model is a diagram that shows the relationships between the different entities in the system.

![Domain Model](../../static/figures/PPS-domain-model.svg)

### Architectural Diagram

The architectural diagram is a diagram that shows the different components of the system and their interactions.

![Architectural Diagram](../../static/figures/PPS-architecture.svg)

### Design choices

- [TODO] Gradio picture for the chat interface, annotated to reference the different requirements listed above.

### Requirements Specification

Below, I refine the requirements presented in Section [Requirements Definition](#requirements-definition).

{{< insert-text "requirement-specifications.md" >}}

## Reference Requirements in Code

- The requirements should be referenced in the code comments.

```scala
// @Requirement(id = "FR1.1", description = "As a user, I can specify a Git repository URL to inspect its code.")
def inspectGitRepository(url: String): List[Requirement] = ???
```

## Traceability Matrix

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
