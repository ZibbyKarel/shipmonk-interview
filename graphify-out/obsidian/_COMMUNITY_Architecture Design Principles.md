---
type: community
cohesion: 0.40
members: 5
---

# Architecture Design Principles

**Cohesion:** 0.40 - moderately connected
**Members:** 5 nodes

## Members
- [[Exchange Rates Caching Service (ShipMonk Interview)]] - document - CLAUDE.md
- [[Expected Architecture Layers (ControllerServiceRepositoryClient)]] - document - CLAUDE.md
- [[God Node Expected Architecture Layers (ControllerServiceRepositoryClient)]] - document - graphify-out/GRAPH_REPORT.md
- [[Rationale Architecture Quality Prioritised Over Feature Coverage]] - document - CLAUDE.md
- [[Rationale No Auth Because Internal Service]] - document - CLAUDE.md

## Live Query (requires Dataview plugin)

```dataview
TABLE source_file, type FROM #community/Architecture_Design_Principles
SORT file.name ASC
```

## Connections to other communities
- 2 edges to [[_COMMUNITY_Service Layer & Caching]]
- 1 edge to [[_COMMUNITY_Persistence Infrastructure]]
- 1 edge to [[_COMMUNITY_Fixer.io API Client]]

## Top bridge nodes
- [[Expected Architecture Layers (ControllerServiceRepositoryClient)]] - degree 6, connects to 3 communities
- [[Rationale Architecture Quality Prioritised Over Feature Coverage]] - degree 2, connects to 1 community