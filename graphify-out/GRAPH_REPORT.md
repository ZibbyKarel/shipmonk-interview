# Graph Report - /Users/zibby/Workspace/shipmonk-interview  (2026-05-01)

## Corpus Check
- 3 files · ~7,511 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 37 nodes · 34 edges · 9 communities detected
- Extraction: 82% EXTRACTED · 18% INFERRED · 0% AMBIGUOUS · INFERRED: 6 edges (avg confidence: 0.83)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Community 0|Community 0]]
- [[_COMMUNITY_Community 1|Community 1]]
- [[_COMMUNITY_Community 2|Community 2]]
- [[_COMMUNITY_Community 3|Community 3]]
- [[_COMMUNITY_Community 4|Community 4]]
- [[_COMMUNITY_Community 5|Community 5]]
- [[_COMMUNITY_Community 6|Community 6]]
- [[_COMMUNITY_Community 7|Community 7]]
- [[_COMMUNITY_Community 8|Community 8]]

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
- `Expected Architecture Layers (Controller/Service/Repository/Client)` --references--> `God Node: Expected Architecture Layers (Controller/Service/Repository/Client)`  [EXTRACTED]
  CLAUDE.md → graphify-out/GRAPH_REPORT.md
- `Service Layer (Cache Check + Fixer.io Fallback)` --references--> `God Node: Service Layer (Cache Check + Fixer.io Fallback)`  [EXTRACTED]
  CLAUDE.md → graphify-out/GRAPH_REPORT.md
- `Service Layer (Cache Check + Fixer.io Fallback)` --references--> `Hyperedge: Cache-aside Pattern (Controller -> Service -> Repository/Fixer.io)`  [EXTRACTED]
  CLAUDE.md → graphify-out/GRAPH_REPORT.md

## Hyperedges (group relationships)
- **Assignment Non-Functional Requirements: Architecture, Error Handling, No Auth** — assignment_architecture_priority, assignment_error_handling_requirement, assignment_no_auth_required [EXTRACTED 0.92]
- **CLAUDE.md Layered Architecture: Controller -> Service -> Repository + Fixer Client** — claude_architecture_layers, claude_service_layer, claude_repository_layer, claude_fixer_client [EXTRACTED 0.95]
- **CLAUDE.md Non-Functional Constraints: Architecture Quality, Error Handling, No Auth** — claude_rationale_architecture_over_features, claude_error_handling_constraint, claude_rationale_no_auth [EXTRACTED 0.92]
- **Graph Report God Nodes: Core Architectural Abstractions** — graphreport_god_node_arch_layers, graphreport_god_node_service_layer, graphreport_god_node_fixer_client [EXTRACTED 0.90]

## Communities

### Community 0 - "Community 0"
Cohesion: 0.4
Nodes (5): Expected Architecture Layers (Controller/Service/Repository/Client), Exchange Rates Caching Service (ShipMonk Interview), Rationale: Architecture Quality Prioritised Over Feature Coverage, Rationale: No Auth Because Internal Service, God Node: Expected Architecture Layers (Controller/Service/Repository/Client)

### Community 1 - "Community 1"
Cohesion: 0.4
Nodes (5): Error Handling Constraint (Downstream Simplification), Service Layer (Cache Check + Fixer.io Fallback), God Node: Service Layer (Cache Check + Fixer.io Fallback), Hyperedge: Cache-aside Pattern (Controller -> Service -> Repository/Fixer.io), Hyperedge: Assignment Non-Functional Requirements (Architecture, Error Handling, No Auth)

### Community 2 - "Community 2"
Cohesion: 0.4
Nodes (5): Community: App Bootstrap & Tests, Community: Assignment Constraints & Rationale, Community: Data Source & Caching Layer, Community: Service Architecture, Graph Report Summary (35 nodes, 35 edges, 7 communities)

### Community 3 - "Community 3"
Cohesion: 0.5
Nodes (4): TestingdayExchangeRatesApplication, TestingdayExchangeRatesApplication.main, TestingdayExchangeRatesApplicationTests, TestingdayExchangeRatesApplicationTests.contextLoads

### Community 4 - "Community 4"
Cohesion: 0.5
Nodes (4): Architecture Quality over Feature Coverage, Solid Error Handling for Downstream Services, Exchange Rates Task Assignment, No API Authentication Needed (Internal Service)

### Community 5 - "Community 5"
Cohesion: 0.5
Nodes (4): Caching Layer over Third-Party Service, fixer.io as Exchange Rates Source, Hibernate/JPA Requirement, PostgreSQL for Cached Exchange Rates

### Community 6 - "Community 6"
Cohesion: 0.5
Nodes (4): Base Currency USD, Fixer.io Access Key in application.properties, Fixer.io HTTP Client, God Node: Fixer.io HTTP Client

### Community 7 - "Community 7"
Cohesion: 0.67
Nodes (3): GET /api/v1/rates/{day} Endpoint, ExchangeRatesController, ExchangeRatesController.getRates

### Community 8 - "Community 8"
Cohesion: 0.67
Nodes (3): PostgreSQL 14 via Docker Compose, Repository Layer (Hibernate/JPA Entity), Hyperedge: PostgreSQL + Hibernate + Docker Compose Persistence Stack

## Knowledge Gaps
- **23 isolated node(s):** `TestingdayExchangeRatesApplicationTests.contextLoads`, `ExchangeRatesController`, `TestingdayExchangeRatesApplication.main`, `fixer.io as Exchange Rates Source`, `Architecture Quality over Feature Coverage` (+18 more)
  These have ≤1 connection - possible missing edges or undocumented components.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Expected Architecture Layers (Controller/Service/Repository/Client)` connect `Community 0` to `Community 8`, `Community 1`, `Community 6`?**
  _High betweenness centrality (0.088) - this node is a cross-community bridge._
- **Why does `Service Layer (Cache Check + Fixer.io Fallback)` connect `Community 1` to `Community 0`, `Community 8`, `Community 6`?**
  _High betweenness centrality (0.084) - this node is a cross-community bridge._
- **Why does `Fixer.io HTTP Client` connect `Community 6` to `Community 0`, `Community 1`?**
  _High betweenness centrality (0.067) - this node is a cross-community bridge._
- **What connects `TestingdayExchangeRatesApplicationTests.contextLoads`, `ExchangeRatesController`, `TestingdayExchangeRatesApplication.main` to the rest of the system?**
  _23 weakly-connected nodes found - possible documentation gaps or missing edges._