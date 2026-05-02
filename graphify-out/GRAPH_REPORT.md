# Graph Report - /Users/zibby/Workspace/shipmonk-interview  (2026-05-02)

## Corpus Check
- 26 files · ~25,051 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 218 nodes · 304 edges · 36 communities detected
- Extraction: 69% EXTRACTED · 31% INFERRED · 0% AMBIGUOUS · INFERRED: 95 edges (avg confidence: 0.8)
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
- [[_COMMUNITY_Community 9|Community 9]]
- [[_COMMUNITY_Community 10|Community 10]]
- [[_COMMUNITY_Community 11|Community 11]]
- [[_COMMUNITY_Community 12|Community 12]]
- [[_COMMUNITY_Community 13|Community 13]]
- [[_COMMUNITY_Community 14|Community 14]]
- [[_COMMUNITY_Community 15|Community 15]]
- [[_COMMUNITY_Community 16|Community 16]]
- [[_COMMUNITY_Community 17|Community 17]]
- [[_COMMUNITY_Community 18|Community 18]]
- [[_COMMUNITY_Community 19|Community 19]]
- [[_COMMUNITY_Community 20|Community 20]]
- [[_COMMUNITY_Community 21|Community 21]]
- [[_COMMUNITY_Community 22|Community 22]]
- [[_COMMUNITY_Community 23|Community 23]]
- [[_COMMUNITY_Community 24|Community 24]]
- [[_COMMUNITY_Community 25|Community 25]]
- [[_COMMUNITY_Community 26|Community 26]]
- [[_COMMUNITY_Community 27|Community 27]]
- [[_COMMUNITY_Community 28|Community 28]]
- [[_COMMUNITY_Community 29|Community 29]]
- [[_COMMUNITY_Community 30|Community 30]]
- [[_COMMUNITY_Community 31|Community 31]]
- [[_COMMUNITY_Community 32|Community 32]]
- [[_COMMUNITY_Community 33|Community 33]]
- [[_COMMUNITY_Community 34|Community 34]]
- [[_COMMUNITY_Community 35|Community 35]]

## God Nodes (most connected - your core abstractions)
1. `Implementation Plan Phase 1` - 18 edges
2. `ExchangeRatesIT` - 15 edges
3. `Graph Report Summary (153 nodes, 198 edges, 20 communities)` - 12 edges
4. `FixerExchangeRateProviderTest` - 10 edges
5. `ExchangeRatesService (Cache-Aside Logic)` - 10 edges
6. `RatesMapConverterTest` - 8 edges
7. `ExchangeRateSnapshot` - 8 edges
8. `ExchangeRatesControllerTest` - 7 edges
9. `ExchangeRatesServiceTest` - 6 edges
10. `GlobalExceptionHandler` - 6 edges

## Surprising Connections (you probably didn't know these)
- `Implementation Plan Phase 1` --conceptually_related_to--> `Exchange Rates Caching Service (ShipMonk Interview)`  [INFERRED]
  docs/plans/implementation-plan-phase-1.md → graphify-out/obsidian/Exchange Rates Caching Service (ShipMonk Interview).md
- `Package Structure (config/controller/service/repository/entity/client/dto/converter/exception)` --conceptually_related_to--> `God Node: Expected Architecture Layers (Controller/Service/Repository/Client)`  [INFERRED]
  docs/plans/implementation-plan-phase-1.md → graphify-out/GRAPH_REPORT.md
- `ExchangeRateSnapshot JPA Entity` --conceptually_related_to--> `God Node: ExchangeRateSnapshot (8 edges)`  [INFERRED]
  docs/plans/implementation-plan-phase-1.md → graphify-out/GRAPH_REPORT.md
- `ExchangeRatesService (Cache-Aside Logic)` --conceptually_related_to--> `God Node: Service Layer (Cache Check + Fixer.io Fallback)`  [INFERRED]
  docs/plans/implementation-plan-phase-1.md → graphify-out/GRAPH_REPORT.md
- `GlobalExceptionHandler (@ControllerAdvice)` --implements--> `Error Handling Constraint (Downstream Simplification)`  [INFERRED]
  docs/plans/implementation-plan-phase-1.md → graphify-out/obsidian/Error Handling Constraint (Downstream Simplification).md

## Hyperedges (group relationships)
- **Assignment Non-Functional Requirements: Architecture, Error Handling, No Auth** — assignment_architecture_priority, assignment_error_handling_requirement, assignment_no_auth_required [EXTRACTED 0.92]
- **CLAUDE.md Layered Architecture: Controller -> Service -> Repository + Fixer Client** — claude_architecture_layers, claude_service_layer, claude_repository_layer, claude_fixer_client [EXTRACTED 0.95]
- **CLAUDE.md Non-Functional Constraints: Architecture Quality, Error Handling, No Auth** — claude_rationale_architecture_over_features, claude_error_handling_constraint, claude_rationale_no_auth [EXTRACTED 0.92]

## Communities

### Community 0 - "Community 0"
Cohesion: 0.1
Nodes (7): ExchangeRatesController, ExchangeRatesControllerTest, ExchangeRateSnapshot, ExchangeRatesService, ExchangeRatesServiceTest, isHistorical(), isSuccess()

### Community 1 - "Community 1"
Cohesion: 0.14
Nodes (23): Community: Service Architecture, Community: Architecture Design Principles (cohesion 0.40), Community: Assignment Constraints (cohesion 0.50), Community: Fixer.io API Client (cohesion 0.50), Community: Persistence Infrastructure (cohesion 0.67), Community: Service Layer & Caching (cohesion 0.40), God Node: Expected Architecture Layers (Controller/Service/Repository/Client), God Node: ExchangeRateSnapshot (8 edges) (+15 more)

### Community 2 - "Community 2"
Cohesion: 0.17
Nodes (21): God Node: Fixer.io HTTP Client, God Node: FixerRatesResponse (15 edges), God Node: RatesResponse (8 edges), AppConfig (RestTemplate Bean, @Value Bindings), ExchangeRatesController Update (wire service, parse day), ErrorResponse DTO, Exception Hierarchy (ExchangeRateException / RatesNotFoundException / FixerApiException / InvalidDateException), ExchangeRateSnapshot JPA Entity (+13 more)

### Community 3 - "Community 3"
Cohesion: 0.15
Nodes (3): isSuccess(), FixerExchangeRateProvider, FixerExchangeRateProviderTest

### Community 4 - "Community 4"
Cohesion: 0.19
Nodes (2): ExchangeRatesIT, ExchangeRateSnapshotRepository

### Community 5 - "Community 5"
Cohesion: 0.23
Nodes (2): RatesMapConverter, RatesMapConverterTest

### Community 6 - "Community 6"
Cohesion: 0.24
Nodes (11): Base Currency USD, Fixer.io Access Key in application.properties, Expected Architecture Layers (Controller/Service/Repository/Client), Error Handling Constraint (Downstream Simplification), Fixer.io HTTP Client, PostgreSQL 14 via Docker Compose, Exchange Rates Caching Service (ShipMonk Interview), Rationale: Architecture Quality Prioritised Over Feature Coverage (+3 more)

### Community 7 - "Community 7"
Cohesion: 0.29
Nodes (8): Base Currency USD, Expected Architecture Layers (Controller/Service/Repository/Client), Fixer.io Access Key in application.properties, Fixer.io HTTP Client, God Node: Expected Architecture Layers (Controller/Service/Repository/Client), God Node: Service Layer (Cache Check + Fixer.io Fallback), Hyperedge: Cache-aside Pattern (Controller -> Service -> Repository/Fixer.io), Service Layer (Cache Check + Fixer.io Fallback)

### Community 8 - "Community 8"
Cohesion: 0.29
Nodes (1): GlobalExceptionHandler

### Community 9 - "Community 9"
Cohesion: 0.6
Nodes (5): Community: Assignment Constraints & Rationale, Community: Data Source & Caching Layer, Community: Graph Documentation Meta, fixer.io as Exchange Rates Source, Graph Report Summary (35 nodes, 35 edges, 7 communities)

### Community 10 - "Community 10"
Cohesion: 0.5
Nodes (4): Architecture Quality over Feature Coverage, Solid Error Handling for Downstream Services, Exchange Rates Task Assignment, No API Authentication Needed (Internal Service)

### Community 11 - "Community 11"
Cohesion: 0.5
Nodes (4): Caching Layer over Third-Party Service, fixer.io as Exchange Rates Source, Hibernate/JPA Requirement, PostgreSQL for Cached Exchange Rates

### Community 12 - "Community 12"
Cohesion: 0.5
Nodes (4): Exchange Rates Task Assignment, Hyperedge: PostgreSQL + Hibernate + Docker Compose Persistence Stack, No API Authentication Needed (Internal Service), Solid Error Handling for Downstream Services

### Community 13 - "Community 13"
Cohesion: 0.67
Nodes (1): ExchangeRateProvider

### Community 14 - "Community 14"
Cohesion: 0.67
Nodes (1): InvalidDateException

### Community 15 - "Community 15"
Cohesion: 0.67
Nodes (1): ExchangeRateException

### Community 16 - "Community 16"
Cohesion: 0.67
Nodes (1): RatesNotFoundException

### Community 17 - "Community 17"
Cohesion: 0.67
Nodes (1): ProviderException

### Community 18 - "Community 18"
Cohesion: 0.67
Nodes (1): TransientProviderException

### Community 19 - "Community 19"
Cohesion: 0.67
Nodes (1): AppConfig

### Community 20 - "Community 20"
Cohesion: 0.67
Nodes (3): Caching Layer over Third-Party Service, Hibernate/JPA Requirement, PostgreSQL for Cached Exchange Rates

### Community 21 - "Community 21"
Cohesion: 1.0
Nodes (2): TestingdayExchangeRatesApplication, TestingdayExchangeRatesApplication.main

### Community 22 - "Community 22"
Cohesion: 1.0
Nodes (2): Community: App Bootstrap & Tests, TestingdayExchangeRatesApplication

### Community 23 - "Community 23"
Cohesion: 1.0
Nodes (2): Community: API Endpoint Controller, GET /api/v1/rates/{day} Endpoint

### Community 24 - "Community 24"
Cohesion: 1.0
Nodes (1): TestingdayExchangeRatesApplicationTests

### Community 25 - "Community 25"
Cohesion: 1.0
Nodes (1): TestingdayExchangeRatesApplicationTests.contextLoads

### Community 26 - "Community 26"
Cohesion: 1.0
Nodes (0): 

### Community 27 - "Community 27"
Cohesion: 1.0
Nodes (0): 

### Community 28 - "Community 28"
Cohesion: 1.0
Nodes (0): 

### Community 29 - "Community 29"
Cohesion: 1.0
Nodes (1): GET /api/v1/rates/{day} Endpoint

### Community 30 - "Community 30"
Cohesion: 1.0
Nodes (1): FixerErrorResponse DTO

### Community 31 - "Community 31"
Cohesion: 1.0
Nodes (1): Community: Caching & Data Layer

### Community 32 - "Community 32"
Cohesion: 1.0
Nodes (1): Community: Spring Boot App Entry

### Community 33 - "Community 33"
Cohesion: 1.0
Nodes (1): PostgreSQL 14 via Docker Compose

### Community 34 - "Community 34"
Cohesion: 1.0
Nodes (1): Hyperedge: Assignment Non-Functional Requirements (Architecture, Error Handling, No Auth)

### Community 35 - "Community 35"
Cohesion: 1.0
Nodes (1): Rationale: No Auth Because Internal Service

## Knowledge Gaps
- **38 isolated node(s):** `TestingdayExchangeRatesApplicationTests`, `TestingdayExchangeRatesApplicationTests.contextLoads`, `TestingdayExchangeRatesApplication`, `TestingdayExchangeRatesApplication.main`, `fixer.io as Exchange Rates Source` (+33 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `Community 21`** (2 nodes): `TestingdayExchangeRatesApplication`, `TestingdayExchangeRatesApplication.main`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 22`** (2 nodes): `Community: App Bootstrap & Tests`, `TestingdayExchangeRatesApplication`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 23`** (2 nodes): `Community: API Endpoint Controller`, `GET /api/v1/rates/{day} Endpoint`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 24`** (1 nodes): `TestingdayExchangeRatesApplicationTests`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 25`** (1 nodes): `TestingdayExchangeRatesApplicationTests.contextLoads`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 26`** (1 nodes): `ExchangeRates.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 27`** (1 nodes): `FixerErrorDetail.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 28`** (1 nodes): `FixerRatesResponse.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 29`** (1 nodes): `GET /api/v1/rates/{day} Endpoint`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 30`** (1 nodes): `FixerErrorResponse DTO`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 31`** (1 nodes): `Community: Caching & Data Layer`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 32`** (1 nodes): `Community: Spring Boot App Entry`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 33`** (1 nodes): `PostgreSQL 14 via Docker Compose`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 34`** (1 nodes): `Hyperedge: Assignment Non-Functional Requirements (Architecture, Error Handling, No Auth)`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 35`** (1 nodes): `Rationale: No Auth Because Internal Service`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Implementation Plan Phase 1` connect `Community 2` to `Community 1`?**
  _High betweenness centrality (0.014) - this node is a cross-community bridge._
- **Are the 3 inferred relationships involving `ExchangeRatesService (Cache-Aside Logic)` (e.g. with `Rationale: Fixer Free Plan Locks Base to EUR, Rebase Required in Service` and `ExchangeRatesControllerTest Integration Test`) actually correct?**
  _`ExchangeRatesService (Cache-Aside Logic)` has 3 INFERRED edges - model-reasoned connections that need verification._
- **What connects `TestingdayExchangeRatesApplicationTests`, `TestingdayExchangeRatesApplicationTests.contextLoads`, `TestingdayExchangeRatesApplication` to the rest of the system?**
  _38 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.1 - nodes in this community are weakly interconnected._
- **Should `Community 1` be split into smaller, more focused modules?**
  _Cohesion score 0.14 - nodes in this community are weakly interconnected._