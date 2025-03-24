# Git Inspector Product Backlog

## High Priority - Project Foundation

1. **Setup Best Practices**
   - Setup a linter and a formatter
   - Setup a build tool
   - Setup a test framework
   - Setup a code coverage tool (coveralls)

2. **Create GitHub Actions**
   - CI/CD pipeline
   - Code documentation generation
   - Code quality metrics
   - Github Pages for project documentation
   - Dependabot for dependency security updates
  
3. **GitHub Project Setup**
   - GitHub issues and project boards
   - Setup README.md, Contributing.md, etc.
   - Build sites: 1) for process folder and 2) for showcasing documentation
   - Automatic deployment and release by version number
   - Github Flow for branching

## Medium Priority - Core Functionality

4. **HTTP Server**
   - Define API contracts using OpenAPI/Swagger
   - Generate interactive API documentation using Swagger UI

5. **Repository Loading**
   - Parse remote Git repository structure
   - Extract basic repository metadata (base path, JSON and textual format, etc.)

6. **Code Analysis**
   - List all files with basic information
   - Filter files by the number of lines of code and extensions

## Low Priority - Advanced Features

7. **Visualization**
   - Graphical visualization of the repository in Python for rapid prototyping
   - Testing the API through visual interfaces

8. **Advanced Metrics**
   - Code quality metrics using gemini-code-assist
   - Requires a pull request to be created
