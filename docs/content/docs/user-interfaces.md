---
title: "User Interfaces"
author: "Razvan"
date: 2025-03-26
description: "Defining the user interfaces for the Git Inspector project"
aliases: ["user-interfaces"]
tocopen: true
tags: ["user-interfaces"]
summary: "This document contains images with the UIs. They allow inspection of the UI without installing the app."
bibFile: /assets/bibliography.json
---


<!-- markdownlint-disable MD051 -->


## User Interfaces

Components in the Scala frontend package:

**Components:**
- `LinkViewer`: Component for viewing and indexing content from GitHub repositories
- `ChatInterface`: Component for chatting with the LLM about indexed repositories, handling message display and submission.
- `IndexSelector`: Component for choosing which repository index to query, with options to refresh or remove indices.
- `StatusBar`: Simple component displaying status messages to users.
- `TabContainer`: Component for switching between the Chat and Link Viewer tabs.

**Services:**
- `ContentService`: Service that communicates with the backend API to fetch content, generate indices, and handle chat interactions.
- `HttpClient`: Low-level service handling HTTP requests and Server-Sent Events for streaming chat responses.

**Models:**
- `Models.scala`: Contains data models used throughout the application like `ChatMessage`, `IndexOption`, and various request/response models.

**Utilities:**
- `IDGenerator`: Utility for generating unique IDs for chat messages and other elements.
- `Main.scala`: Entry point that initializes the application, sets up event listeners, and creates the UI components.

**Python Interface:**
- `main.py`: Implements an alternative frontend using Gradio with functionality for chatting with repositories, viewing content, and managing indices.
- `style.css`: Provides styling for the Gradio interface.


### Gradio Interface


{{< numbered-figure id="fig:sequence-diagram-chat" align="center" src="../../static/figures/gradio-chat.png" caption="Gradio interface for the chatbot" >}}

{{< numbered-figure id="fig:sequence-diagram-second" align="center" src="../../static/figures/gradio-index.png" caption="Gradio interface for the code indexer" >}}


### Laminar Interface

{{< numbered-figure id="fig:sequence-diagram-chat" align="center" src="../../static/figures/laminar-chat.png" caption="Laminar interface for the chatbot" >}}

{{< numbered-figure id="fig:sequence-diagram-second" align="center" src="../../static/figures/laminar-index.png" caption="Laminar interface for the code indexer" >}}

