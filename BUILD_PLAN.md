# Citation Analysis System — Build Plan & Research Dossier
**Target execution tool:** Google Antigravity (multi-agent, credit-metered)
**Objective:** Ship a correct, from-scratch (no `java.util`) DSA implementation while burning the minimum possible credits.

---

## Part 1 — Research: The 7 Hard Problems & Their Known Solutions

Every module in this project is a *solved* CS problem. The risk isn't "can it be done" — it's implementing it correctly **without the standard library safety net**, and without an AI agent looping/retrying its way through bugs (which is where credits actually get burned). Below: the problem, the canonical solution, the from-scratch gotchas, and the complexity target.

### 1. Graph Representation & Traversal (BFS/DFS)
- **Problem:** Represent a potentially large, sparse citation graph and traverse it to find direct/indirect relationships.
- **Existing solution:** Adjacency list over adjacency matrix — citation graphs are sparse (most papers don't cite most other papers), so adjacency list keeps it at `O(V+E)` space instead of `O(V²)`.
- **From-scratch gotcha:** No `ArrayList<Integer>` per node — needs a **custom dynamic array** or **custom linked list** as the edge-list backing structure. Build this ONE reusable dynamic array class first; everything else (hash buckets, adjacency lists, BFS queues) reuses it.
- **BFS needs a queue; DFS needs a stack (or recursion).** Build a custom array-backed queue/stack once, reuse everywhere.
- **Complexity target:** `O(V+E)` traversal, `O(V+E)` space.

### 2. Custom Hashing (Paper Lookup by Title/Author)
- **Problem:** O(1) average lookup without `HashMap`.
- **Existing solution:** Open addressing (linear/quadratic probing) or separate chaining with a custom bucket array. For a fixed academic dataset size, **open addressing with a good hash + resize-on-load-factor** is simpler to get correct than chaining and avoids needing linked lists inside buckets.
- **From-scratch gotcha:** String hashing needs a solid hash function (polynomial rolling hash, e.g. base 31 or 131, mod a large prime) — a weak hash function is the #1 source of silent bugs (clustering → degrades to O(n)). Also must handle **tombstones** for deletion under open addressing, and **resize + rehash** at ~0.7 load factor.
- **Complexity target:** `O(1)` average insert/search, `O(n)` worst case (acceptable, document it).

### 3. Exact String Matching (Title/Author Search) — KMP & Rabin-Karp
- **Problem:** Find exact substring matches for search queries.
- **Existing solution:** KMP for single-pattern deterministic `O(n+m)` matching; Rabin-Karp when checking a query against **many** paper titles at once (rolling hash amortizes well across multiple comparisons).
- **From-scratch gotcha:** KMP's failure function (LPS array) construction is the classic bug source — off-by-one errors are extremely common. This is the single highest-risk-of-wasted-iteration module; get the LPS array right with a hand-traced example *before* writing the matcher.
- **Complexity target:** KMP `O(n+m)`, Rabin-Karp `O(n+m)` average.

### 4. Fuzzy Matching — Edit Distance (Wagner-Fischer)
- **Problem:** Tolerate typos in search queries (e.g., "Dijkstar" → "Dijkstra").
- **Existing solution:** Classic Wagner-Fischer DP — `dp[i][j]` = edit distance between first `i` and `j` chars of two strings.
- **From-scratch gotcha:** Naive implementation is `O(m·n)` **time and space** — fine for title-length strings, but if applied paper-by-paper across a large corpus it becomes the system's bottleneck. Mitigate by **pre-filtering candidates via the hash table** (e.g., same first letter / similar length) before running edit distance, rather than running DP against every paper title.
- **Complexity target:** `O(m·n)` per comparison; use **space-optimized rolling 2-row DP** (not full matrix) to cut memory.

### 5. Max-Flow for Citation-Flow Analysis (Dinic's / Edmonds-Karp)
- **Problem:** Quantify "flow of influence" between author clusters through the citation graph.
- **Existing solution:** Edmonds-Karp (BFS-based Ford-Fulkerson, `O(VE²)`) is simpler to implement correctly; Dinic's (`O(V²E)`, faster in practice via blocking flows + level graphs) is more complex but much faster on denser graphs.
- **Recommendation for THIS project:** **Start with Edmonds-Karp.** It reuses the BFS you already built in Module 1 almost verbatim (BFS to find augmenting paths). Only upgrade to Dinic's if profiling shows it's needed — this is the single biggest opportunity to **save credits**, since Edmonds-Karp is ~70% less code and reuses existing components.
- **From-scratch gotcha:** Must model **residual graphs** (forward + backward edges with capacities) — citation edges need reverse edges with 0 initial capacity added at graph-construction time, not bolted on later.
- **Complexity target:** Edmonds-Karp `O(VE²)` — acceptable for an academic-scale demo dataset (hundreds–low thousands of papers).

### 6. Ranking / Sorting by Citation Count
- **Problem:** Rank papers by citation count for "top cited" reports.
- **Existing solution:** Merge sort or quicksort, hand-built, `O(n log n)`. Merge sort is safer to implement correctly from scratch (no pivot-selection edge cases) and is stable — useful if you ever want secondary sort keys (e.g., tie-break by year).
- **Complexity target:** `O(n log n)`.

### 7. Reporting & Persistence
- **Problem:** Turn graph/hash-table state into human-readable trend reports; persist between runs.
- **Existing solution:** CSV read/write via plain `java.io` (allowed — only *collections* are banned, not I/O). Reports are just formatted traversals of your own structures.
- **Gotcha:** Keep report generation **decoupled** from the algorithms — reports should only *read* graph/hash-table state, never mutate it. Cleaner testing, fewer regression bugs = fewer wasted agent iterations fixing report bugs that break the core engine.

---

## Part 2 — Antigravity Credit-Cost Strategy

Antigravity meters usage via a **5-hour-refresh compute quota + paid credit top-ups**, with cost varying heavily by **model tier** and **task complexity** (not just token count). Multi-agent parallelism and long back-and-forth debugging loops are the two biggest credit sinks. Strategy to minimize spend:

| Lever | Why it saves credits |
|---|---|
| **Model-tier matching** | Use the cheapest model (Flash-tier) for boilerplate: custom dynamic array, queue/stack, CSV I/O, getters/setters. Reserve the higher-reasoning model tier ONLY for the genuinely tricky logic: KMP's LPS array, Dinic's/Edmonds-Karp residual graph handling, hash table resize/rehash. |
| **One mission per module, fully specified upfront** | Every retry/clarification round is a new billed turn. Write complete mission briefs (inputs, outputs, method signatures, edge cases) *before* dispatching — this is what Part 3 below is for. |
| **Sequential, not parallel, for dependent modules** | Parallel agents are great for *independent* work, but this project is a dependency chain (dynamic array → graph → BFS → max-flow). Running dependent modules in parallel causes rework when interfaces don't match — burns credits fixing integration, not building. Only parallelize truly independent pieces (e.g., CSV I/O module vs. KMP module can run side-by-side). |
| **Reuse the plan as grounding context, not re-explaining each time** | Feed this document (or a condensed version) into each mission so the agent doesn't re-derive architecture decisions per session. |
| **Test-as-you-go, not one giant integration test at the end** | Catching a bug in a 50-line module costs one cheap-model fix. Catching the same bug after everything's wired together costs an expensive full-context debugging session. |
| **Use the Manager view's plan artifacts** | Antigravity generates a task plan before executing — review and correct that plan BEFORE it starts coding, since fixing a plan is cheap and fixing already-generated code is not. |

---

## Part 3 — Phased Build Plan (Mission Briefs for Antigravity)

Each phase = one Antigravity mission. Dependency order matters — do not skip ahead.

### Phase 0 — Project Scaffold *(Flash tier)*
- Java project structure, package layout (`core`, `algo`, `io`, `report`, `main`)
- `Paper` and `Citation` model classes (POJOs, no logic)
- Git init + `.gitignore`

### Phase 1 — Foundational Data Structures *(Flash tier)*
- Custom dynamic array (generic, resizable)
- Custom array-backed stack + queue
- Custom singly/doubly linked list (used inside hash buckets if chaining is chosen)
- **Unit tests for each before moving on** — these are load-bearing for every later module.

### Phase 2 — Citation Graph Core *(Flash tier, low complexity)*
- Adjacency-list graph class built on Phase 1's dynamic array
- Add paper (vertex) / add citation (directed edge)
- BFS + DFS traversal methods

### Phase 3 — Custom Hash Table *(Mid/high reasoning tier — hash function + collision handling is bug-prone)*
- Polynomial rolling hash function for strings
- Open addressing with linear probing, tombstone deletion, resize-on-load-factor
- Insert/search/delete by title and by author

### Phase 4 — String Matching *(High reasoning tier — LPS array is the highest-risk module)*
- KMP exact matcher (hand-trace the LPS array against a test string before accepting the implementation)
- Rabin-Karp for multi-title scanning
- Wagner-Fischer edit distance, **space-optimized (2-row DP)**, gated behind a cheap pre-filter (first-letter/length bucket from the hash table) so it's never run against the full corpus

### Phase 5 — Max-Flow Citation Analysis *(High reasoning tier)*
- Residual graph construction (reverse edges baked in at graph build time)
- Edmonds-Karp using the Phase 2 BFS (reuse, don't rewrite)
- *(Stretch, only if profiling demands it)* Dinic's level-graph + blocking flow upgrade

### Phase 6 — Sorting & Ranking *(Flash tier)*
- Merge sort by citation count (stable, so ties can secondary-sort by year later)

### Phase 7 — Persistence & Reporting *(Flash tier)*
- CSV read/write via `java.io`
- Report generator: top authors, most-cited papers, citation trend summary
- Strictly read-only against core structures

### Phase 8 — Integration + CLI/JavaFX Front End *(Mid tier)*
- Wire all modules behind a single `Main`
- Console menu (JavaFX only if time allows — treat as stretch goal)

### Phase 9 — Test Pass & Report Polish *(Flash tier)*
- JUnit coverage on graph, hash table, KMP, edit distance, max-flow
- Edge cases: empty graph, self-citation, duplicate titles, disconnected clusters

---

## Execution Order Summary

```
Phase 0 → Phase 1 → Phase 2 ─┬─→ Phase 5 (needs Phase 2's BFS)
                              ├─→ Phase 6
                              └─→ Phase 7
       Phase 3 → Phase 4 (needs Phase 3's hash table for pre-filtering)
                              ↓
                         Phase 8 (integration, needs everything)
                              ↓
                         Phase 9 (test pass)
```

Phases 1→2 and Phase 3 can run as **two parallel Antigravity agents** (they don't depend on each other). Phase 4 must wait for Phase 3. Phase 5 must wait for Phase 2. Everything funnels into Phase 8.
