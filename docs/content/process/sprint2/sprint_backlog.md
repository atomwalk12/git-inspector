---
title: "Sprint 2 Backlog: Design Patterns and Indexing"
author: "Razvan"
date: 2025-03-31
description: "Detailed breakdown of tasks and user stories for Sprint 2's design patterns and indexing phase"
aliases: ["sprint2-backlog"]
ShowToc: true
tags: ["sprint2", "design patterns", "code-indexing", "requirements"]
weight: 299
ShowBreadCrumbs: true
TocOpen: true
summary: "List of deliverables for Sprint 2 (March 31-April 6, 2025), focusing on design patterns and code indexing."
---


<!-- trunk-ignore(markdownlint/MD036) -->
**Sprint 1 Backlog**

**Sprint Goal:** Build and implement the foundation for a code search system that accepts Git repositories, processes their content, and creates searchable indexes. This sprint addresses business requirements BR1, BR2 and functional requirements FR1.1, FR2.1, FR2.2, FR2.3. Also, implementation requirements (IR1, IR2, IR4) and non-functional requirements (NFR1, NFR3), are also addressed.

**Key Deliverables:**
1. Repository URL input interface with validation
2. Git repository fetching and processing service
3. Code indexing system with vector DB integration
4. Basic layered architectural foundation (Scala)

**Definition of Done:**
*   Repository loading completes in <30 seconds for repos up to 100MB (*NFR1*)
*   Initial indexing operations demonstrate successful embedding storage (*FR2.2, FR2.3*)
*   90% of unit test cases for validation and processing pass (*FR1.1, FR2.1, FR2.2*)
*   Code adheres to basic layered structure principles (*IR4*)

---

## Task Board

Link to the main product backlog: {{< abslink url="/static/product-backlog#task-board" text="Product Backlog">}}

{{< responsive-table>}}

| SBI ID                           | Task Description                                              | User Story       | PBI ID                                           | Est. Points | Status |
| :------------------------------- | :------------------------------------------------------------ | :--------------- | :----------------------------------------------- | :---------- | :----- |
| **ARCHITECTURE & SETUP**         |                                                               |                  |                                                  |             |        |
| S1.A1                            | Set up basic layered project structure (Scala)                | *(Foundation)*   | [F1](../../static/product-backlog.md#task-board) | 10          | DONE   |
| S1.A2                            | Define core interfaces between initial layers                 | *(Foundation)*   | [F1](../../static/product-backlog.md#task-board) | 5           | DONE   |
| S1.A3                            | Implement design patterns discussed during the course         | *(Foundation)*   | [F1](../../static/product-backlog.md#task-board) | 10          | DONE   |
| **REPOSITORY INPUT (15 Points)** |                                                               |                  |                                                  |             |        |
| S1.1.1                           | Create UI component for repository URL input                  | Repository Input | [F2](../../static/product-backlog.md#task-board) | 5           | DONE   |
| S1.1.2                           | Implement URL validation with clear feedback                  | Repository Input | [F2](../../static/product-backlog.md#task-board) | 3           | To Do  |
| S1.1.3                           | Create Git wrapper for repository fetching                    | Repository Input | [F2](../../static/product-backlog.md#task-board) | 7           | To Do  |
| **CODE PROCESSING (20 Points)**  |                                                               |                  |                                                  |             |        |
| S1.2.1                           | Implement file traversal & content extraction                 | Code Processing  | [F2](../../static/product-backlog.md#task-board) | 8           | To Do  |
| S1.2.2                           | Add language detection & basic file filtering                 | Code Processing  | [F2](../../static/product-backlog.md#task-board) | 4           | To Do  |
| S1.2.3                           | Create code chunking strategy for indexing                    | Code Processing  | [F3](../../static/product-backlog.md#task-board) | 8           | To Do  |
| **SEARCH INDEXING (15 Points)**  |                                                               |                  |                                                  |             |        |
| S1.3.1                           | Implement code embedding generation (Langchain4J)             | Search Indexing  | [F3](../../static/product-backlog.md#task-board) | 8           | To Do  |
| S1.3.2                           | Create vector database integration (Qdrant) using Langchain4j | Search Indexing  | [F3](../../static/product-backlog.md#task-board) | 7           | DONE   |
|                                  |                                                               |                  |                                                  |             |        |

{{</ responsive-table>}}
