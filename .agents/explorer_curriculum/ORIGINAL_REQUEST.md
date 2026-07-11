# Explorer Task - Curriculum Management Backend API Analysis

## Objective
Analyze the Spring Boot backend codebase and database schema to extract requirements and contracts for the Curriculum Management module frontend workflows.

## Questions to Answer
1. What are the API endpoints for `/api/v1/curricula`? Check controllers under `src/main/java`.
2. What is the schema of the Curriculum entity? (fields, types, validation rules, relationships).
3. How is the Curriculum Builder API structured? E.g., how are courses grouped by year level and semester? Is there an endpoint to fetch curriculum details with grouped courses, or does the frontend group them?
4. What are the endpoints to assign a course to a curriculum?
5. How are pre-requisites and co-requisites represented and managed via API?
6. Are there specific validation annotations (e.g. `@NotNull`, `@Size`, etc.) on the request DTOs?

## Outputs
Write your analysis to `c:\Users\PC\Projects\cis\.agents\explorer_curriculum\analysis.md`.
Include:
- API endpoint matrix (Method, Path, Request payload, Response schema)
- TypeScript interfaces matching the API
- DB table structure/relationships
- Recommendations for the implementation

## 2026-07-11T10:29:38Z
Please analyze the backend API and database schemas for Curriculum Management. Read the task requirements in c:\Users\PC\Projects\cis\.agents\explorer_curriculum\ORIGINAL_REQUEST.md. Save your findings in c:\Users\PC\Projects\cis\.agents\explorer_curriculum\analysis.md and reply when finished.
