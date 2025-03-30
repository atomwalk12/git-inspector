---
title: "Sprint 1 Overview: Project Infrastructure Setup"
author: "Razvan"
date: 2025-03-24
description: "First sprint focusing on establishing project foundations, development environment, and CI/CD pipelines"
aliases: ["sprint1", "sprint1-overview", "infrastructure-sprint"]
tags: ["sprint1", "infrastructure", "project-setup", "ci-cd"]
ShowToc: true
TocOpen: true
weight: 298
ShowBreadCrumbs: true
summary: "First sprint (March 24-30, 2025) dedicated to setting up the project's foundational infrastructure, including Scala 3 environment, development tools, automated checks, testing framework, and CI/CD pipeline."
---


Duration: 1 week (Start date: 2025-03-24, End date: 2025-03-30)

## Sprint Goal

Sprint 1 focused on setting up the project infrastructure and development environment. This includes:

1. Setting up the basic Scala project structure
2. Implementing essential development tools and configurations
3. Establishing code quality standards and automated checks
4. Creating a reliable CI/CD pipeline

Deliverables:

- Basic project structure with Scala 3.6.4
- Automated code formatting and linting setup
- Git hooks for code quality checks
- Test infrastructure with ScalaTest
- Code coverage reporting with Scoverage
- Semantic release configuration
- Logging infrastructure

## Sprint Backlog

[TODO: include summary of sprint backlog]

For detailed sprint backlog items, see [Sprint Backlog](./sprint_backlog.md)

## Sprint Progress

### Daily Updates

Checkout the [daily progress updates](./daily_updates/) folder for detailed day-by-day developments.

## Key Takeaways

- Established Scala 3 project with modern build tools
- Implemented multiple layers of code quality checks
- Set up automated release management
- Created robust testing infrastructure

### Technical Debt

- Need to fine-tune Wartremover rules
- Consider adding more specific Scalafmt configurations
- May need to optimize test memory settings

### Lessons Learned

- Early investment in development infrastructure pays off
- Automated checks help maintain code quality
- Semantic versioning helps track changes effectively


## Sprint Retrospective

The following tools will help maintain code quality and automate tasks, freeing up time for more
complex tasks.

Main completed tasks:
- Successfully set up the project infrastructure using Scala 3
- Added tools to ensure code quality: Scalafmt, Wartremover, Scalafix and Trunk
- Set up complete CI/CD pipeline with Github Actions and artifact publishing
- Configured automatic semantic versioning and release management
- Implemented git hooks with pre-commit checks
- Added Gemini bot for automated pull request reviews

*What went well?*
- This approach to setting up the requirements is likely to save time in future sprints, as it makes it easier to track requirements and choose tasks to focus on.
- The documentation process was fairly smooth. During the forthcoming sprints, it will become much more easier to maintain the documentation.
- Learnt about semantic versioning and how it can be used to manage the release of new versions of the project.

*What could be improved?*
- Some planned tasks remain incomplete, including Dependabot configuration. This will be removed from the product backlog. I prefer not to use it, to avoid build stability issues.
- Wartremover rules need to be fine-tuned, as they will likely lead to accumulating technical debt if I rely too much on excluded rules.
- Repository loading functionality is not yet implemented (see [Sprint Backlog](./sprint_backlog.md)). This will be remedied in the next sprint.

*What did I learn?*
- I need to pay special attention to the Wartremover rules, as if I rely too much on excluded rules, this will likely end up in accumulating technical debt. Used `Wart.Any`, `Wart.Throw` and `Wart.Var`.
- Early investment in development infrastructure may pay off in the long run. This will have to be proven in the forthcoming sprints.
- Setting up automated checks from the beginning helps maintain consistent code quality standards.
- Breaking down infrastructure tasks into smaller, more manageable pieces makes the development process more straightforward.
