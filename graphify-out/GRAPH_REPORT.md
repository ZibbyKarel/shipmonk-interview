# Graph Report - .  (2026-05-01)

## Corpus Check
- Corpus is ~633 words - fits in a single context window. You may not need a graph.

## Summary
- 35 nodes · 35 edges · 7 communities detected
- Extraction: 69% EXTRACTED · 31% INFERRED · 0% AMBIGUOUS · INFERRED: 11 edges (avg confidence: 0.9)
- Token cost: 1,850 input · 1,200 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Assignment Constraints & Rationale|Assignment Constraints & Rationale]]
- [[_COMMUNITY_Data Source & Caching Layer|Data Source & Caching Layer]]
- [[_COMMUNITY_Service Architecture|Service Architecture]]
- [[_COMMUNITY_App Bootstrap & Tests|App Bootstrap & Tests]]
- [[_COMMUNITY_Test Suite (AST)|Test Suite (AST)]]
- [[_COMMUNITY_Controller Implementation (AST)|Controller Implementation (AST)]]
- [[_COMMUNITY_Spring Boot Entry Point (AST)|Spring Boot Entry Point (AST)]]

## God Nodes (most connected - your core abstractions)
1. `Expected Architecture Layers (Controller/Service/Repository/Client)` - 5 edges
2. `Service Layer (Cache Check + Fixer.io Fallback)` - 5 edges
3. `Fixer.io HTTP Client` - 5 edges
4. `Exchange Rates Task Assignment` - 4 edges
5. `Repository Layer (Hibernate/JPA Entity)` - 4 edges
6. `ExchangeRatesController` - 3 edges
7. `Caching Layer over Third-Party Service` - 3 edges
8. `TestingdayExchangeRatesApplicationTests` - 2 edges
9. `ExchangeRatesController` - 2 edges
10. `TestingdayExchangeRatesApplication` - 2 edges

## Surprising Connections (you probably didn't know these)
- `ExchangeRatesController` --references--> `Service Layer (Cache Check + Fixer.io Fallback)`  [INFERRED]
  src/main/java/com/shipmonk/testingday/ExchangeRatesController.java → CLAUDE.md
- `ExchangeRatesController.getRates` --implements--> `GET /api/v1/rates/{day} Endpoint`  [INFERRED]
  src/main/java/com/shipmonk/testingday/ExchangeRatesController.java → ASSIGNMENT.md
- `Rationale: Architecture Quality Prioritised Over Feature Coverage` --semantically_similar_to--> `Architecture Quality over Feature Coverage`  [INFERRED] [semantically similar]
  CLAUDE.md → ASSIGNMENT.md
- `Error Handling Constraint (Downstream Simplification)` --semantically_similar_to--> `Solid Error Handling for Downstream Services`  [INFERRED] [semantically similar]
  CLAUDE.md → ASSIGNMENT.md
- `Rationale: No Auth Because Internal Service` --semantically_similar_to--> `No API Authentication Needed (Internal Service)`  [INFERRED] [semantically similar]
  CLAUDE.md → ASSIGNMENT.md

## Hyperedges (group relationships)
- **Cache-aside Pattern: Controller -> Service -> Repository/Fixer.io** — exchangeratescontroller_class, claude_service_layer, claude_repository_layer, claude_fixer_client [INFERRED 0.88]
- **Assignment Non-Functional Requirements: Architecture, Error Handling, No Auth** — assignment_architecture_priority, assignment_error_handling_requirement, assignment_no_auth_required [EXTRACTED 0.92]
- **PostgreSQL + Hibernate + Docker Compose Persistence Stack** — assignment_postgresql_cache, assignment_hibernate_requirement, claude_postgres_docker, claude_repository_layer [INFERRED 0.85]

## Communities

### Community 0 - "Assignment Constraints & Rationale"
Cohesion: 0.25
Nodes (8): Architecture Quality over Feature Coverage, Solid Error Handling for Downstream Services, Exchange Rates Task Assignment, No API Authentication Needed (Internal Service), Error Handling Constraint (Downstream Simplification), Exchange Rates Caching Service (ShipMonk Interview), Rationale: Architecture Quality Prioritised Over Feature Coverage, Rationale: No Auth Because Internal Service

### Community 1 - "Data Source & Caching Layer"
Cohesion: 0.29
Nodes (7): Caching Layer over Third-Party Service, fixer.io as Exchange Rates Source, Hibernate/JPA Requirement, PostgreSQL for Cached Exchange Rates, Base Currency USD, Fixer.io Access Key in application.properties, Fixer.io HTTP Client

### Community 2 - "Service Architecture"
Cohesion: 0.38
Nodes (7): GET /api/v1/rates/{day} Endpoint, Expected Architecture Layers (Controller/Service/Repository/Client), PostgreSQL 14 via Docker Compose, Repository Layer (Hibernate/JPA Entity), Service Layer (Cache Check + Fixer.io Fallback), ExchangeRatesController, ExchangeRatesController.getRates

### Community 3 - "App Bootstrap & Tests"
Cohesion: 0.5
Nodes (4): TestingdayExchangeRatesApplication, TestingdayExchangeRatesApplication.main, TestingdayExchangeRatesApplicationTests, TestingdayExchangeRatesApplicationTests.contextLoads

### Community 4 - "Test Suite (AST)"
Cohesion: 0.67
Nodes (1): TestingdayExchangeRatesApplicationTests

### Community 5 - "Controller Implementation (AST)"
Cohesion: 0.67
Nodes (1): ExchangeRatesController

### Community 6 - "Spring Boot Entry Point (AST)"
Cohesion: 0.67
Nodes (1): TestingdayExchangeRatesApplication

## Knowledge Gaps
- **9 isolated node(s):** `TestingdayExchangeRatesApplicationTests.contextLoads`, `TestingdayExchangeRatesApplication.main`, `Base Currency USD`, `GET /api/v1/rates/{day} Endpoint`, `Exchange Rates Caching Service (ShipMonk Interview)` (+4 more)
  These have ≤1 connection - possible missing edges or undocumented components.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Expected Architecture Layers (Controller/Service/Repository/Client)` connect `Service Architecture` to `Assignment Constraints & Rationale`, `Data Source & Caching Layer`?**
  _High betweenness centrality (0.212) - this node is a cross-community bridge._
- **Why does `Rationale: Architecture Quality Prioritised Over Feature Coverage` connect `Assignment Constraints & Rationale` to `Service Architecture`?**
  _High betweenness centrality (0.175) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `Service Layer (Cache Check + Fixer.io Fallback)` (e.g. with `Caching Layer over Third-Party Service` and `ExchangeRatesController`) actually correct?**
  _`Service Layer (Cache Check + Fixer.io Fallback)` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Are the 2 inferred relationships involving `Repository Layer (Hibernate/JPA Entity)` (e.g. with `Hibernate/JPA Requirement` and `PostgreSQL 14 via Docker Compose`) actually correct?**
  _`Repository Layer (Hibernate/JPA Entity)` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `TestingdayExchangeRatesApplicationTests.contextLoads`, `TestingdayExchangeRatesApplication.main`, `Base Currency USD` to the rest of the system?**
  _9 weakly-connected nodes found - possible documentation gaps or missing edges._