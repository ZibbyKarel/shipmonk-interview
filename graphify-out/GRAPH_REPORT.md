# Graph Report - .  (2026-05-01)

## Corpus Check
- Corpus is ~4,671 words - fits in a single context window. You may not need a graph.

## Summary
- 37 nodes · 34 edges · 9 communities detected
- Extraction: 82% EXTRACTED · 18% INFERRED · 0% AMBIGUOUS · INFERRED: 6 edges (avg confidence: 0.83)
- Token cost: 3,200 input · 1,050 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Architecture Design Principles|Architecture Design Principles]]
- [[_COMMUNITY_Service Layer & Caching|Service Layer & Caching]]
- [[_COMMUNITY_Graph Documentation Meta|Graph Documentation Meta]]
- [[_COMMUNITY_Spring Boot App Entry|Spring Boot App Entry]]
- [[_COMMUNITY_Assignment Constraints|Assignment Constraints]]
- [[_COMMUNITY_Caching & Data Layer|Caching & Data Layer]]
- [[_COMMUNITY_Fixer.io API Client|Fixer.io API Client]]
- [[_COMMUNITY_API Endpoint Controller|API Endpoint Controller]]
- [[_COMMUNITY_Persistence Infrastructure|Persistence Infrastructure]]

## God Nodes (most connected - your core abstractions)
1. `Expected Architecture Layers (Controller/Service/Repository/Client)` - 6 edges
2. `Service Layer (Cache Check + Fixer.io Fallback)` - 6 edges
3. `Fixer.io HTTP Client` - 5 edges
4. `Repository Layer (Hibernate/JPA Entity)` - 4 edges
5. `Graph Report Summary (35 nodes, 35 edges, 7 communities)` - 4 edges
6. `Exchange Rates Task Assignment` - 3 edges
7. `Error Handling Constraint (Downstream Simplification)` - 3 edges
8. `TestingdayExchangeRatesApplicationTests` - 2 edges
9. `ExchangeRatesController.getRates` - 2 edges
10. `TestingdayExchangeRatesApplication` - 2 edges

## Surprising Connections (you probably didn't know these)
- `ExchangeRatesController.getRates` --implements--> `GET /api/v1/rates/{day} Endpoint`  [INFERRED]
  src/main/java/com/shipmonk/testingday/ExchangeRatesController.java → ASSIGNMENT.md
- `Base Currency USD` --references--> `Fixer.io HTTP Client`  [INFERRED]
  ASSIGNMENT.md → CLAUDE.md
- `God Node: Expected Architecture Layers (Controller/Service/Repository/Client)` --references--> `Expected Architecture Layers (Controller/Service/Repository/Client)`  [EXTRACTED]
  graphify-out/GRAPH_REPORT.md → CLAUDE.md
- `God Node: Service Layer (Cache Check + Fixer.io Fallback)` --references--> `Service Layer (Cache Check + Fixer.io Fallback)`  [EXTRACTED]
  graphify-out/GRAPH_REPORT.md → CLAUDE.md
- `Hyperedge: Cache-aside Pattern (Controller -> Service -> Repository/Fixer.io)` --references--> `Service Layer (Cache Check + Fixer.io Fallback)`  [EXTRACTED]
  graphify-out/GRAPH_REPORT.md → CLAUDE.md

## Hyperedges (group relationships)
- **Assignment Non-Functional Requirements: Architecture, Error Handling, No Auth** — assignment_architecture_priority, assignment_error_handling_requirement, assignment_no_auth_required [EXTRACTED 0.92]
- **CLAUDE.md Layered Architecture: Controller -> Service -> Repository + Fixer Client** — claude_architecture_layers, claude_service_layer, claude_repository_layer, claude_fixer_client [EXTRACTED 0.95]
- **CLAUDE.md Non-Functional Constraints: Architecture Quality, Error Handling, No Auth** — claude_rationale_architecture_over_features, claude_error_handling_constraint, claude_rationale_no_auth [EXTRACTED 0.92]
- **Graph Report God Nodes: Core Architectural Abstractions** — graphreport_god_node_arch_layers, graphreport_god_node_service_layer, graphreport_god_node_fixer_client [EXTRACTED 0.90]

## Communities

### Community 0 - "Architecture Design Principles"
Cohesion: 0.4
Nodes (5): Expected Architecture Layers (Controller/Service/Repository/Client), Exchange Rates Caching Service (ShipMonk Interview), Rationale: Architecture Quality Prioritised Over Feature Coverage, Rationale: No Auth Because Internal Service, God Node: Expected Architecture Layers (Controller/Service/Repository/Client)

### Community 1 - "Service Layer & Caching"
Cohesion: 0.4
Nodes (5): Error Handling Constraint (Downstream Simplification), Service Layer (Cache Check + Fixer.io Fallback), God Node: Service Layer (Cache Check + Fixer.io Fallback), Hyperedge: Cache-aside Pattern (Controller -> Service -> Repository/Fixer.io), Hyperedge: Assignment Non-Functional Requirements (Architecture, Error Handling, No Auth)

### Community 2 - "Graph Documentation Meta"
Cohesion: 0.4
Nodes (5): Community: App Bootstrap & Tests, Community: Assignment Constraints & Rationale, Community: Data Source & Caching Layer, Community: Service Architecture, Graph Report Summary (35 nodes, 35 edges, 7 communities)

### Community 3 - "Spring Boot App Entry"
Cohesion: 0.5
Nodes (4): TestingdayExchangeRatesApplication, TestingdayExchangeRatesApplication.main, TestingdayExchangeRatesApplicationTests, TestingdayExchangeRatesApplicationTests.contextLoads

### Community 4 - "Assignment Constraints"
Cohesion: 0.5
Nodes (4): Architecture Quality over Feature Coverage, Solid Error Handling for Downstream Services, Exchange Rates Task Assignment, No API Authentication Needed (Internal Service)

### Community 5 - "Caching & Data Layer"
Cohesion: 0.5
Nodes (4): Caching Layer over Third-Party Service, fixer.io as Exchange Rates Source, Hibernate/JPA Requirement, PostgreSQL for Cached Exchange Rates

### Community 6 - "Fixer.io API Client"
Cohesion: 0.5
Nodes (4): Base Currency USD, Fixer.io Access Key in application.properties, Fixer.io HTTP Client, God Node: Fixer.io HTTP Client

### Community 7 - "API Endpoint Controller"
Cohesion: 0.67
Nodes (3): GET /api/v1/rates/{day} Endpoint, ExchangeRatesController, ExchangeRatesController.getRates

### Community 8 - "Persistence Infrastructure"
Cohesion: 0.67
Nodes (3): PostgreSQL 14 via Docker Compose, Repository Layer (Hibernate/JPA Entity), Hyperedge: PostgreSQL + Hibernate + Docker Compose Persistence Stack

## Knowledge Gaps
- **23 isolated node(s):** `TestingdayExchangeRatesApplicationTests.contextLoads`, `ExchangeRatesController`, `TestingdayExchangeRatesApplication.main`, `fixer.io as Exchange Rates Source`, `Architecture Quality over Feature Coverage` (+18 more)
  These have ≤1 connection - possible missing edges or undocumented components.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Expected Architecture Layers (Controller/Service/Repository/Client)` connect `Architecture Design Principles` to `Persistence Infrastructure`, `Service Layer & Caching`, `Fixer.io API Client`?**
  _High betweenness centrality (0.088) - this node is a cross-community bridge._
- **Why does `Service Layer (Cache Check + Fixer.io Fallback)` connect `Service Layer & Caching` to `Architecture Design Principles`, `Persistence Infrastructure`, `Fixer.io API Client`?**
  _High betweenness centrality (0.084) - this node is a cross-community bridge._
- **Why does `Fixer.io HTTP Client` connect `Fixer.io API Client` to `Architecture Design Principles`, `Service Layer & Caching`?**
  _High betweenness centrality (0.067) - this node is a cross-community bridge._
- **What connects `TestingdayExchangeRatesApplicationTests.contextLoads`, `ExchangeRatesController`, `TestingdayExchangeRatesApplication.main` to the rest of the system?**
  _23 weakly-connected nodes found - possible documentation gaps or missing edges._