# Graph Report - .  (2026-05-01)

## Corpus Check
- Corpus is ~17,726 words - fits in a single context window. You may not need a graph.

## Summary
- 220 nodes · 297 edges · 32 communities detected
- Extraction: 71% EXTRACTED · 29% INFERRED · 0% AMBIGUOUS · INFERRED: 86 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Service & Repository Core|Service & Repository Core]]
- [[_COMMUNITY_Graph Documentation Meta|Graph Documentation Meta]]
- [[_COMMUNITY_Exception Handling & Client Tests|Exception Handling & Client Tests]]
- [[_COMMUNITY_Implementation Plan & Config|Implementation Plan & Config]]
- [[_COMMUNITY_Controller & Integration Tests|Controller & Integration Tests]]
- [[_COMMUNITY_Assignment Constraints & Design|Assignment Constraints & Design]]
- [[_COMMUNITY_Rates Response DTO|Rates Response DTO]]
- [[_COMMUNITY_Error Response DTOs|Error Response DTOs]]
- [[_COMMUNITY_Exchange Rate Entity|Exchange Rate Entity]]
- [[_COMMUNITY_Architecture Design Principles|Architecture Design Principles]]
- [[_COMMUNITY_Fixer Error Detail DTO|Fixer Error Detail DTO]]
- [[_COMMUNITY_App Bootstrap & Tests|App Bootstrap & Tests]]
- [[_COMMUNITY_Non-Functional Requirements|Non-Functional Requirements]]
- [[_COMMUNITY_Task Assignment Meta|Task Assignment Meta]]
- [[_COMMUNITY_Rates Map Converter|Rates Map Converter]]
- [[_COMMUNITY_Architecture Quality Principles|Architecture Quality Principles]]
- [[_COMMUNITY_Caching Layer Design|Caching Layer Design]]
- [[_COMMUNITY_Spring Config Bean|Spring Config Bean]]
- [[_COMMUNITY_Date Validation Exception|Date Validation Exception]]
- [[_COMMUNITY_Base Exchange Rate Exception|Base Exchange Rate Exception]]
- [[_COMMUNITY_Rates Not Found Exception|Rates Not Found Exception]]
- [[_COMMUNITY_Fixer API Client|Fixer API Client]]
- [[_COMMUNITY_API Endpoint|API Endpoint]]
- [[_COMMUNITY_Persistence Stack|Persistence Stack]]
- [[_COMMUNITY_Application Entry|Application Entry]]
- [[_COMMUNITY_Rates Endpoint|Rates Endpoint]]
- [[_COMMUNITY_Fixer Error Response|Fixer Error Response]]
- [[_COMMUNITY_Cache Data Layer Meta|Cache Data Layer Meta]]
- [[_COMMUNITY_Exchange Rates Controller|Exchange Rates Controller]]
- [[_COMMUNITY_PostgreSQL Database|PostgreSQL Database]]
- [[_COMMUNITY_Non-Functional Hyperedge|Non-Functional Hyperedge]]
- [[_COMMUNITY_No Auth Rationale|No Auth Rationale]]

## God Nodes (most connected - your core abstractions)
1. `Implementation Plan Phase 1` - 18 edges
2. `FixerRatesResponse` - 15 edges
3. `Graph Report Summary (153 nodes, 198 edges, 20 communities)` - 12 edges
4. `ExchangeRatesService (Cache-Aside Logic)` - 10 edges
5. `RatesResponse` - 8 edges
6. `ExchangeRateSnapshot` - 8 edges
7. `ExchangeRatesServiceTest` - 7 edges
8. `FixerClientTest` - 7 edges
9. `ExchangeRatesControllerTest` - 7 edges
10. `ExchangeRatesService` - 6 edges

## Surprising Connections (you probably didn't know these)
- `Community: Spring Boot App Entry` --references--> `TestingdayExchangeRatesApplicationTests.contextLoads`  [EXTRACTED]
  graphify-out/obsidian/_COMMUNITY_Spring Boot App Entry.md → src/test/java/com/shipmonk/testingday/TestingdayExchangeRatesApplicationTests.java
- `Community: Spring Boot App Entry` --references--> `TestingdayExchangeRatesApplication.main`  [EXTRACTED]
  graphify-out/obsidian/_COMMUNITY_Spring Boot App Entry.md → src/main/java/com/shipmonk/testingday/TestingdayExchangeRatesApplication.java
- `Implementation Plan Phase 1` --conceptually_related_to--> `Exchange Rates Caching Service (ShipMonk Interview)`  [INFERRED]
  docs/plans/implementation-plan-phase-1.md → graphify-out/obsidian/Exchange Rates Caching Service (ShipMonk Interview).md
- `Fixer.io EUR-to-USD Rebasing Constraint` --conceptually_related_to--> `God Node: Fixer.io HTTP Client`  [INFERRED]
  docs/plans/implementation-plan-phase-1.md → graphify-out/GRAPH_REPORT.md
- `Package Structure (config/controller/service/repository/entity/client/dto/converter/exception)` --conceptually_related_to--> `God Node: Expected Architecture Layers (Controller/Service/Repository/Client)`  [INFERRED]
  docs/plans/implementation-plan-phase-1.md → graphify-out/GRAPH_REPORT.md

## Hyperedges (group relationships)
- **Assignment Non-Functional Requirements: Architecture, Error Handling, No Auth** — assignment_architecture_priority, assignment_error_handling_requirement, assignment_no_auth_required [EXTRACTED 0.92]
- **CLAUDE.md Layered Architecture: Controller -> Service -> Repository + Fixer Client** — claude_architecture_layers, claude_service_layer, claude_repository_layer, claude_fixer_client [EXTRACTED 0.95]
- **CLAUDE.md Non-Functional Constraints: Architecture Quality, Error Handling, No Auth** — claude_rationale_architecture_over_features, claude_error_handling_constraint, claude_rationale_no_auth [EXTRACTED 0.92]

## Communities

### Community 0 - "Service & Repository Core"
Cohesion: 0.13
Nodes (4): ExchangeRateSnapshotRepository, ExchangeRatesService, ExchangeRatesServiceTest, FixerRatesResponse

### Community 1 - "Graph Documentation Meta"
Cohesion: 0.12
Nodes (26): Community: Service Architecture, Community: Architecture Design Principles (cohesion 0.40), Community: Assignment Constraints (cohesion 0.50), Community: Fixer.io API Client (cohesion 0.50), Community: Persistence Infrastructure (cohesion 0.67), Community: Service Layer & Caching (cohesion 0.40), God Node: Expected Architecture Layers (Controller/Service/Repository/Client), God Node: ExchangeRateSnapshot (8 edges) (+18 more)

### Community 2 - "Exception Handling & Client Tests"
Cohesion: 0.11
Nodes (3): FixerApiException, FixerClientTest, GlobalExceptionHandler

### Community 3 - "Implementation Plan & Config"
Cohesion: 0.21
Nodes (18): God Node: FixerRatesResponse (15 edges), AppConfig (RestTemplate Bean, @Value Bindings), ExchangeRatesController Update (wire service, parse day), ErrorResponse DTO, Exception Hierarchy (ExchangeRateException / RatesNotFoundException / FixerApiException / InvalidDateException), ExchangeRateSnapshot JPA Entity, ExchangeRateSnapshotRepository (JpaRepository), ExchangeRatesService (Cache-Aside Logic) (+10 more)

### Community 4 - "Controller & Integration Tests"
Cohesion: 0.19
Nodes (2): ExchangeRatesController, ExchangeRatesControllerTest

### Community 5 - "Assignment Constraints & Design"
Cohesion: 0.24
Nodes (11): Base Currency USD, Fixer.io Access Key in application.properties, Expected Architecture Layers (Controller/Service/Repository/Client), Error Handling Constraint (Downstream Simplification), Fixer.io HTTP Client, PostgreSQL 14 via Docker Compose, Exchange Rates Caching Service (ShipMonk Interview), Rationale: Architecture Quality Prioritised Over Feature Coverage (+3 more)

### Community 6 - "Rates Response DTO"
Cohesion: 0.22
Nodes (1): RatesResponse

### Community 7 - "Error Response DTOs"
Cohesion: 0.22
Nodes (2): ErrorDetail, ErrorResponse

### Community 8 - "Exchange Rate Entity"
Cohesion: 0.22
Nodes (1): ExchangeRateSnapshot

### Community 9 - "Architecture Design Principles"
Cohesion: 0.29
Nodes (8): Base Currency USD, Expected Architecture Layers (Controller/Service/Repository/Client), Fixer.io Access Key in application.properties, Fixer.io HTTP Client, God Node: Expected Architecture Layers (Controller/Service/Repository/Client), God Node: Service Layer (Cache Check + Fixer.io Fallback), Hyperedge: Cache-aside Pattern (Controller -> Service -> Repository/Fixer.io), Service Layer (Cache Check + Fixer.io Fallback)

### Community 10 - "Fixer Error Detail DTO"
Cohesion: 0.33
Nodes (1): FixerErrorDetail

### Community 11 - "App Bootstrap & Tests"
Cohesion: 0.47
Nodes (6): Community: Spring Boot App Entry, TestingdayExchangeRatesApplication, TestingdayExchangeRatesApplication.main, TestingdayExchangeRatesApplicationTests, TestingdayExchangeRatesApplicationTests, TestingdayExchangeRatesApplicationTests.contextLoads

### Community 12 - "Non-Functional Requirements"
Cohesion: 0.4
Nodes (5): Exchange Rates Task Assignment, Hyperedge: PostgreSQL + Hibernate + Docker Compose Persistence Stack, No API Authentication Needed (Internal Service), Solid Error Handling for Downstream Services, TestingdayExchangeRatesApplication.main

### Community 13 - "Task Assignment Meta"
Cohesion: 0.6
Nodes (5): Community: Assignment Constraints & Rationale, Community: Data Source & Caching Layer, Community: Graph Documentation Meta, fixer.io as Exchange Rates Source, Graph Report Summary (35 nodes, 35 edges, 7 communities)

### Community 14 - "Rates Map Converter"
Cohesion: 0.5
Nodes (1): RatesMapConverter

### Community 15 - "Architecture Quality Principles"
Cohesion: 0.5
Nodes (4): Architecture Quality over Feature Coverage, Solid Error Handling for Downstream Services, Exchange Rates Task Assignment, No API Authentication Needed (Internal Service)

### Community 16 - "Caching Layer Design"
Cohesion: 0.5
Nodes (4): Caching Layer over Third-Party Service, fixer.io as Exchange Rates Source, Hibernate/JPA Requirement, PostgreSQL for Cached Exchange Rates

### Community 17 - "Spring Config Bean"
Cohesion: 0.67
Nodes (1): AppConfig

### Community 18 - "Date Validation Exception"
Cohesion: 0.67
Nodes (1): InvalidDateException

### Community 19 - "Base Exchange Rate Exception"
Cohesion: 0.67
Nodes (1): ExchangeRateException

### Community 20 - "Rates Not Found Exception"
Cohesion: 0.67
Nodes (1): RatesNotFoundException

### Community 21 - "Fixer API Client"
Cohesion: 0.67
Nodes (1): FixerClient

### Community 22 - "API Endpoint"
Cohesion: 1.0
Nodes (3): Community: API Endpoint Controller, ExchangeRatesController.getRates, GET /api/v1/rates/{day} Endpoint

### Community 23 - "Persistence Stack"
Cohesion: 0.67
Nodes (3): Caching Layer over Third-Party Service, Hibernate/JPA Requirement, PostgreSQL for Cached Exchange Rates

### Community 24 - "Application Entry"
Cohesion: 1.0
Nodes (2): Community: App Bootstrap & Tests, TestingdayExchangeRatesApplication

### Community 25 - "Rates Endpoint"
Cohesion: 1.0
Nodes (1): GET /api/v1/rates/{day} Endpoint

### Community 26 - "Fixer Error Response"
Cohesion: 1.0
Nodes (1): FixerErrorResponse DTO

### Community 27 - "Cache Data Layer Meta"
Cohesion: 1.0
Nodes (1): Community: Caching & Data Layer

### Community 28 - "Exchange Rates Controller"
Cohesion: 1.0
Nodes (1): ExchangeRatesController

### Community 29 - "PostgreSQL Database"
Cohesion: 1.0
Nodes (1): PostgreSQL 14 via Docker Compose

### Community 30 - "Non-Functional Hyperedge"
Cohesion: 1.0
Nodes (1): Hyperedge: Assignment Non-Functional Requirements (Architecture, Error Handling, No Auth)

### Community 31 - "No Auth Rationale"
Cohesion: 1.0
Nodes (1): Rationale: No Auth Because Internal Service

## Knowledge Gaps
- **33 isolated node(s):** `fixer.io as Exchange Rates Source`, `Architecture Quality over Feature Coverage`, `Solid Error Handling for Downstream Services`, `No API Authentication Needed (Internal Service)`, `Hibernate/JPA Requirement` (+28 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `Application Entry`** (2 nodes): `Community: App Bootstrap & Tests`, `TestingdayExchangeRatesApplication`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Rates Endpoint`** (1 nodes): `GET /api/v1/rates/{day} Endpoint`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Fixer Error Response`** (1 nodes): `FixerErrorResponse DTO`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Cache Data Layer Meta`** (1 nodes): `Community: Caching & Data Layer`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Exchange Rates Controller`** (1 nodes): `ExchangeRatesController`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `PostgreSQL Database`** (1 nodes): `PostgreSQL 14 via Docker Compose`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Non-Functional Hyperedge`** (1 nodes): `Hyperedge: Assignment Non-Functional Requirements (Architecture, Error Handling, No Auth)`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `No Auth Rationale`** (1 nodes): `Rationale: No Auth Because Internal Service`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Are the 3 inferred relationships involving `ExchangeRatesService (Cache-Aside Logic)` (e.g. with `Rationale: Fixer Free Plan Locks Base to EUR, Rebase Required in Service` and `ExchangeRatesControllerTest Integration Test`) actually correct?**
  _`ExchangeRatesService (Cache-Aside Logic)` has 3 INFERRED edges - model-reasoned connections that need verification._
- **What connects `fixer.io as Exchange Rates Source`, `Architecture Quality over Feature Coverage`, `Solid Error Handling for Downstream Services` to the rest of the system?**
  _33 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Service & Repository Core` be split into smaller, more focused modules?**
  _Cohesion score 0.13 - nodes in this community are weakly interconnected._
- **Should `Graph Documentation Meta` be split into smaller, more focused modules?**
  _Cohesion score 0.12 - nodes in this community are weakly interconnected._
- **Should `Exception Handling & Client Tests` be split into smaller, more focused modules?**
  _Cohesion score 0.11 - nodes in this community are weakly interconnected._