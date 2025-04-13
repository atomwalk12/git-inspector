---
title: "Requirements Engineering"
author: "Razvan"
date: 2025-03-27
description: "Defining the requirements for the Git Inspector project"
aliases: ["requirements"]
tocopen: true
tags: ["requirements"]
summary: "I define all 5 requirement types: Business, Functional (user and system), Non-Functional, Implementation."
bibFile: /assets/bibliography.json
---

<!-- markdownlint-disable MD051 -->

## Workflow

![PPS Workflow](../../static/figures/PPS-workflow.svg)

## Requirements Engineering

### Requirements Definition

For more details on the requirements engineering process, please refer to [day 4](sprint1/daily_updates/2025-03-27.md) of the first sprint.

{{< insert-text "requirements.md" >}}

### Domain Model

#### Class Diagram

The domain model is a diagram that shows the relationships between the different domain entities.

![Class Diagram](../../static/figures/PPS-domain-model-class-diagram.svg)

#### Sequence Diagram

Figure [fig:sequence-diagram-indexing](#fig:sequence-diagram-indexing) shows the how the indexing process works.

{{< numbered-figure id="fig:sequence-diagram-indexing" align="center" src="../../static/figures/PPS-domain-model-sequence-diagram-indexing.svg" caption="Sequence Diagram that illustrates the repository indexing process" >}}


Figure [fig:sequence-diagram-chat](#fig:sequence-diagram-chat) shows the sequence diagram for the code search process.

{{< numbered-figure id="fig:sequence-diagram-chat" align="center" src="../../static/figures/PPS-domain-model-sequence-diagram-chat.svg" caption="Sequence Diagram that illustrates the code search process" >}}

### Design choices

- [TODO] Gradio picture for the chat interface, annotated to reference the different requirements listed above.

### Requirements Specification

Below, I refine the requirements presented in Section [Requirements Definition](#requirements-definition).

{{< insert-text "requirement-specifications.md" >}}

### Code References

- The requirements should be referenced in the code comments.

```scala
// @Requirement(id = "FR1.1", description = "As a user, I can specify a Git repository URL to inspect its code.")
def inspectGitRepository(url: String): List[Requirement] = ???
```

### Traceability Matrix

{{< insert-text "traceability-matrix.md" >}}

## Design

### Architectural Diagram

The architectural diagram is a diagram that shows the different components of the system and their interactions. See sprint 1, [day 3](sprint1/daily_updates/2025-03-26.md) for more details.

![Architectural Diagram](../../static/figures/PPS-architecture.svg)

### Design Principles

Domain:
  - Core business models
  - Core business interfaces
  - Core business logic

Application:
  - Use cases that use domain interfaces
  - Management of domain objects
  - Application-specific interfaces (for DTOs, content type, status codes, etc.)

Infrastructure:
  - Implements domain interfaces
  - Technical concerns (Qdrant, Ollama, etc.)

### Detailed Design

Detailed design includes more implementation specifics like private methods, framework-specific elements, etc.
