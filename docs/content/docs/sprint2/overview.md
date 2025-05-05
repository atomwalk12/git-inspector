---
title: "Sprint 2 Overview: Design Patterns and Indexing"
author: "Razvan"
date: 2025-03-31
description: "Second sprint focusing on design patterns and code indexing"
aliases: ["sprint2", "sprint2-overview", "design-patterns-sprint"]
tags: ["sprint2", "design-patterns", "code-indexing"]
ShowToc: true
TocOpen: true
weight: 298
ShowBreadCrumbs: true
summary: "Second sprint (March 31-April 6, 2025) overview, focused on design patterns and core functionality."
---


Duration: 1 week (Start date: 2025-03-31, End date: 2025-04-06)

The following sections build on the [Requirements Specifications](../../static/requirement-specifications.md) document.

## Sprint Goal

1. **Repository URL Input Interface** *(FR1.1, NFR2, NFR3)*

2. **Git Repository Fetching Service** *(FR2.1, IR1, NFR1, NFR2, IR3)*

3. **Code Processing Pipeline** *(FR2.1, FR2.2, FR1.4)*

4. **Search Indexing System** *(FR2.2, FR2.3, IR2, NFR1)*

5. **Architectural Foundation** *(IR1, IR4)*


## Sprint Backlog

For detailed sprint backlog items, see [Sprint Backlog](./sprint_backlog.md).



## Sprint Retrospective

- **What went well?**
  - **Good progress.** Most of the requirements that were planned were completed.
  - **Layered Architecture.** It becomes clear how useful the layered architecture is. The different parts communicate using interfaces, and the dependencies are well defined. By allowing different modules to depend on other modules strictly below them, the code becomes more flexible and easier to maintain.
  - **Example.** The application layer is not allowed to depend on the infrastructure layer, but all communicate is done through the domain layer, which is an intermediary designated to satisfy business requirements.

- **What could be improved?**
  - **NFRs not addressed.** For instance, while the repository loading functionality is complete, it is still necessary to assess qualities such as the robustness and performance of the code.
  - **Testing.** From a testing perspective, both integration and mocked tests pass, with the former requiring local external services to be running. The later are executable using CI.
  - **Requirements Documentation.** It would be ideal to have the requirements documented using tests. However, since the implementation details are likely to change, I deemed more valuable to to document this later  when the code is more stable.
  - **Code Quality.** The code is not as clean as it could be, and more refactoring is going to be necessary in future sprints.

- **What did I learn?**
  - **Requirements.** Since the requirements were well defined, the implementation was relatively straightforward.
  - **Documenting progress.** It is fairly useful to document progress, and to think about the process by documenting it. This can certainly influence the direction of future sprints.
