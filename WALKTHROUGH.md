# Walkthrough — Optimal Citation Path (Held-Karp Bitmask DP)

A complete, end-to-end walkthrough of the **Optimal Citation Path** feature added to the
Citation Analysis System: what it does, how the algorithm works, how to run it, and how it was
verified. Every code reference, transcript, and number in this document was checked against the
current working tree — nothing here is illustrative-only.

---

## 1. What this feature does

> Given a subset of paper IDs, find the **minimum-cost directed citation path that visits all of
> them**, respecting the existing directed citation edges in the graph.

Concretely:

- The user enters a comma-separated list of paper IDs (e.g. `P102,P101,P104`).
- The system returns the cheapest **ordered route** through those papers — the route may pass
  through papers that were *not* in the list (intermediate papers), because citations are only
  traversable along real edges.
- The **cost** is the total number of citation hops (edges) travelled along the whole route.
- If no directed route can connect the whole subset, the user gets
  `No valid path exists for this subset` — no exception, no crash.
- More than **20** papers in the subset is rejected up front with a clear error.

This is a **path-TSP over a directed graph with shortest-path edge weights** (a.k.a. the open
Hamiltonian-path problem over a subset), which is what the Held-Karp bitmask DP solves.

---

## 2. Where everything lives

| Piece | File | Location |
|---|---|---|
| Public cap constant | `src/algo/GraphTraversal.java` | line 438 |
| Canonical no-path message | `src/algo/GraphTraversal.java` | line 441 |
| `NO_ROUTE` sentinel | `src/algo/GraphTraversal.java` | line 444 |
| Result type `OptimalPathResult` | `src/algo/GraphTraversal.java` | line 450 |
| `optimalCitationPath(List<String>)` (required signature) | `src/algo/GraphTraversal.java` | line 522 |
| `optimalCitationPath(DynamicArray<String>)` overload | `src/algo/GraphTraversal.java` | line 538 |
| `optimalCitationPath(Graph, List<String>)` core routine | `src/algo/GraphTraversal.java` | line 557 |
| Pairwise shortest-hop sweep | `src/algo/GraphTraversal.java` | line 692 |
| Route-tail stitcher | `src/algo/GraphTraversal.java` | line 721 |
| `noValidPath()` factory | `src/algo/GraphTraversal.java` | line 734 |
| Menu entry 12 | `src/main/Main.java` | line 175 |
| Switch dispatch | `src/main/Main.java` | line 142 |
| Console handler | `src/main/Main.java` | line 953 |
| Valid-path test | `src/algo/GraphTest.java` | line 396 |
| No-path test | `src/algo/GraphTest.java` | line 434 |
| Design doc section | `BUILD_PLAN.md` | line 196 |

Files that were **not** touched: `src/algo/EdmondsKarp.java` (max-flow engine), and every
pre-existing method in `GraphTraversal.java` (`bfs`, `dfs`, `narrateChain`, `getChainLength`,
`findAllPaths`, `isHamiltonianPath`). The GraphTraversal diff is **+305 lines, 0 deletions**.

---

## 3. Quick start

```bash
# from C:/CAS
find src -name '*.java' > /tmp/srcfiles.txt && javac -d out @/tmp/srcfiles.txt
java -cp out main.Main
```

Then choose `12`, type the IDs, and press Enter.

### Verified transcript (real run against `citation_data.csv`, 75 papers / 91 citations)

Menu after this change:

```
----------------------------- MAIN MENU --------------------------------
  1. Add a paper
  2. Add a citation
  3. Search a paper (Exact / Fuzzy)
  4. Explore citation network reachability (Level-wise / Deep Lineage)
  5. Analyze citation flow (Edmonds-Karp Max-Flow)
  6. View reports (Top papers, Top authors, Trends)
  7. Save current data to CSV
  8. Load data from CSV
  9. View paper content
 10. Narrate citation chain (Shortest Path)
 11. Display All Paths (Hamiltonian Check)
 12. Optimal Citation Path (Bitmask DP)
 13. Exit
------------------------------------------------------------------------
```

Valid request — `P102,P101,P104`:

```
--- Optimal Citation Path (Bitmask DP) ---
Enter comma-separated paper IDs (e.g. P101,P102,P104):
=================== OPTIMAL CITATION PATH ===================
Papers requested : 3
Path             : P102 -> P101 -> P104 (total cost: 2)
Total cost       : 2
=============================================================
```

Why cost 2: `P102 → P101` and `P101 → P104` are both direct edges. The reverse order
(`P101 → P104` then trying to reach `P102`) is impossible, so the DP picks `P102 → P101 → P104`.

Impossible request — `P501,P104`:

```
--- Optimal Citation Path (Bitmask DP) ---
Papers requested : 2
Result           : No valid path exists for this subset
```

`P501` is a source paper (nothing cites its way *into* it) and its only outgoing route
(`P501 → P107`) can never reach `P104`, while `P104` cannot reach `P501` either.

Over the cap — 21 IDs:

```
Enter comma-separated paper IDs (e.g. P101,P102,P104): [Error] Too many papers for optimalCitationPath: 21 requested, hard cap is 20
```

---

## 4. The algorithm, step by step

Entry point (line 522) simply delegates to the bound graph and the core routine at line 557,
which executes these six stages.

### Stage 1 — Validate and enforce the hard cap

```java
if (paperIds == null) throw new IllegalArgumentException("Paper ID list cannot be null");
if (paperIds.size() > OPTIMAL_PATH_MAX_PAPERS)   // 20
    throw new IllegalArgumentException("Too many papers for optimalCitationPath: "
        + paperIds.size() + " requested, hard cap is " + OPTIMAL_PATH_MAX_PAPERS);
```

The cap is checked first, so an oversized request is rejected before any work happens.

### Stage 2 — Resolve IDs to distinct vertex indices

Each requested ID is resolved through `graph.findIndexById(...)` (the existing O(1) hash lookup).
- If **any** ID is unknown → immediately return the no-path result.
- Duplicates collapse to a single visit (first-seen order preserved).
- `n` is the number of **distinct** requested papers.
- `n == 0` → trivially satisfied (empty route, cost 0).
- `n == 1` → trivially satisfied (one-paper route, cost 0).

### Stage 3 — Pairwise shortest-hop distances (the "edge weights")

`levelOrderShortestPaths` (line 692) runs one level-order sweep **per requested paper** over the
full graph, producing, for each source, the shortest hop count and a parent pointer for every
vertex. This is what makes intermediate papers legal: the "distance" from subset paper `i` to
subset paper `j` is the length of the shortest *real* directed route, even if it detours through
papers outside the subset. Unreachable pairs get the sentinel `NO_ROUTE`.

### Stage 4 — Held-Karp bitmask DP

The DP table is `dp[mask][lastNode]`, flattened into `dp[mask * n + lastNode]`:

- **Meaning:** cheapest way to visit exactly the set of subset papers in `mask`, ending at
  subset paper `lastNode`.
- **Base:** `dp[1 << i][i] = 0` for every paper `i` (start anywhere, cost 0).
- **Transition:** from `(mask, last)`, for each `next` not in `mask` with a finite
  `shortestDist[last][nodes.get(next)]`:
  ```
  candidate = dp[mask][last] + shortestDist[last][next]
  if candidate < dp[mask | (1 << next)][next]:
      dp[mask | (1 << next)][next] = candidate
      predecessor[mask | (1 << next)][next] = last
  ```
- **Answer:** the minimum `dp[fullMask][last]` over all `last`.
- If the minimum is still `NO_ROUTE`, no ordering connects the subset → return the no-path result.

### Stage 5 — Reconstruct the visiting order

Walk `predecessor` backwards from `(fullMask, bestLast)`, clearing one bit per step, to recover
the order in which the subset papers are visited.

### Stage 6 — Stitch concrete sub-routes into one explicit route

For each consecutive pair in the visiting order, `appendRouteTail` (line 721) walks the stored
parent pointers and appends the real intermediate papers, so the returned path is an actual
walk over citation edges (e.g. `A → X → B`), not just the requested subset in order. The costs of
the legs are summed into `totalCost`.

---

## 5. Fully hand-traced example (the test fixture)

This is the exact fixture used by `testOptimalCitationPathValid`.

**Graph** (`X` is an intermediate paper, *not* in the subset):

```
A → X → B → C → D        subset requested = [A, B, C, D]   → indices A=0 B=1 C=2 D=3
```

**Stage 3 — pairwise shortest hops** (`∞` = `NO_ROUTE`):

| from \ to | A | B | C | D |
|---|---|---|---|---|
| **A** | 0 | 2 | 3 | 4 |
| **B** | ∞ | 0 | 1 | 2 |
| **C** | ∞ | ∞ | 0 | 1 |
| **D** | ∞ | ∞ | ∞ | 0 |

`A → B` costs 2 because the only route is `A → X → B`.

**Stage 4 — DP (only reachable extensions shown):**

| Mask | Ending at | Cost | How |
|---|---|---|---|
| `0001` | A | 0 | base |
| `0010` | B | 0 | base |
| `0100` | C | 0 | base |
| `1000` | D | 0 | base |
| `0011` | B | 2 | A→B |
| `0101` | C | 3 | A→C |
| `1001` | D | 4 | A→D |
| `0110` | C | 1 | B→C |
| `1010` | D | 2 | B→D |
| `1100` | D | 1 | C→D |
| `0111` | C | 3 | (A,B)→C |
| `1011` | D | 4 | (A,B)→D |
| `1101` | D | 4 | (A,C)→D |
| `1110` | D | 2 | (B,C)→D |
| **`1111`** | **D** | **4** | (A,B,C)→D |

`fullMask = 1111` has exactly one finisher: `D` at cost **4**. Every other "last" paper is
unreachable, which is why starting anywhere other than `A` can never cover the whole subset.

**Stage 5 — order:** back-tracking `predecessor` gives `[A, B, C, D]`.

**Stage 6 — stitched route:** `[A, X, B, C, D]`, total cost `2 + 1 + 1 = 4`.

**Assertions that pass** (`GraphTest.java` lines 396–433):
`isFound() == true`, `getCost() == 4`, route size `5`, and `route[0..4] == A, X, B, C, D`.

---

## 6. The "no valid path" path (`testOptimalCitationPathNoValidPath`)

Fixture: `A → B`, and `C` isolated. Subset `[A, B, C]`.

- From `A`: `B` reachable (1), `C` unreachable.
- From `C`: nothing reachable.
- No ordering can cover `{A, B, C}`, so the DP's best over `fullMask` stays `NO_ROUTE`.

Returned instead of crashing:

```java
new OptimalPathResult(false, new ArrayList<String>(), -1, NO_VALID_PATH_MESSAGE)
```

Assertions: `!isFound()`, `getMessage().equals("No valid path exists for this subset")`, and
`toString()` equals the same message — so callers can print either safely.

---

## 7. Result type and API

`OptimalPathResult` (line 450) is a small immutable value object:

| Member | Meaning |
|---|---|
| `isFound()` | `true` when a connecting route exists |
| `getPath()` | ordered paper IDs along the route (`"A","X","B","C","D"`); empty when not found |
| `getCost()` | total hops; `-1` when not found |
| `getMessage()` | human-readable summary, or the canonical no-path message |
| `toString()` | `"A -> X -> B -> C -> D (total cost: 4)"`, or the no-path message |

Three method forms are provided:

1. `optimalCitationPath(List<String>)` — the required signature; uses the bound graph.
2. `optimalCitationPath(DynamicArray<String>)` — lets `Main` call the feature **without adding
   `java.util` imports**, which `Main`'s own documentation explicitly forbids.
3. `optimalCitationPath(Graph, List<String>)` — explicit-graph form used internally and by tests.

---

## 8. Complexity

- **Time:** `O(2^n · n^2)` for the bitmask DP, where `n` = number of requested papers (`n ≤ 20`).
  Each of the `2^n` masks is combined with each of the `n` "last" states and `n` transitions.
- **Extra space:** `O(2^n · n)` for the `dp` table (plus a `byte` predecessor table of the same
  shape), plus `O(n · (V + E))` for the per-source shortest-hop distances.
- **Pre-processing:** `n` level-order sweeps, each `O(V + E)`.
- Worst case at the cap (`n = 20`) is `2^20 × 400 ≈ 4.2 × 10^8` inner steps with ~105 MB of DP
  state — deliberately capped at 20 for that reason. The cap is verified working: a 20-paper
  chain returns `found=true cost=19`, and 21 papers throws.

---

## 9. Tests

Two tests were added to `src/algo/GraphTest.java` (registered in `main()` at lines 28–29):

| Test | Fixture | Asserts |
|---|---|---|
| `testOptimalCitationPathValid` (line 396) | `A→X→B→C→D`, subset `{A,B,C,D}` | found, cost `4`, route `A,X,B,C,D` |
| `testOptimalCitationPathNoValidPath` (line 434) | `A→B`, subset `{A,B,C}` | not found, exact message, no crash |

Run them:

```bash
java -cp out algo.GraphTest
```

Latest full run (all 10 suites, recompiled from source):

```
SUITE                            EXIT   RESULT LINE                                              [FAIL]
algo.GraphTest                   0      GRAPH TEST RESULTS: 73 / 73 PASSED                        0
algo.MaxFlowTest                 0      MAX-FLOW TEST RESULTS: 14 / 14 PASSED                     0
algo.MergeSortTest               0      MERGESORT TEST RESULTS: 40 / 40 PASSED                    0
algo.StringMatchingTest          0      STRING MATCHING TEST RESULTS: 33 / 33 PASSED              0
core.DataStructuresTest          0      DATA STRUCTURES TEST RESULTS: 85 / 85 PASSED              0
core.HashTableTest               0      HASHTABLE TEST RESULTS: 158 / 158 PASSED                  0
io.CsvTest                       0      CSV TEST RESULTS: 28 / 28 PASSED                          0
main.IntegrationEdgeCasesTest    0      EDGE CASES TEST RESULTS: 32 / 32 PASSED                   0
report.GraphRendererTest         0      GRAPHRENDERER TEST RESULTS: 30 / 30 PASSED                0
report.ReportTest                0      REPORT TEST RESULTS: 36 / 36 PASSED                       0
```

`GraphTest` went from 62 → 73 assertions (`+11` from the two new tests); zero `[FAIL]` anywhere.

---

## 10. Guarantees and deliberate constraints

- **Existing behaviour unchanged.** `bfs`, `dfs`, `narrateChain`, `getChainLength`,
  `findAllPaths`, `isHamiltonianPath`, and the Edmonds-Karp engine are byte-for-byte untouched;
  the `GraphTraversal` diff contains no deleted lines.
- **No crash on impossible input.** Missing IDs and un-connectable subsets both return the
  canonical no-path message.
- **No new `println` leaks algorithm jargon.** `grep -rnE "println\(.*\b(BFS|DFS)\b" src/`
  returns only two **pre-existing** matches in `GraphTest.java` (lines 152, 182); this feature
  introduced zero. The new console text says "Bitmask DP" / "Optimal Citation Path".
- **`Main` stays free of `java.util` imports** — the feature is reached through the
  `DynamicArray` overload.
- **Documentation** in `BUILD_PLAN.md` (line 196, `## CO-3: Dynamic Programming`) records the
  signature, menu number, `O(2^n · n^2)` complexity, cap, and no-path behaviour.

---

## 11. Reproduce the verification yourself

```bash
# 1. Compile
find src -name '*.java' > /tmp/srcfiles.txt && javac -d out @/tmp/srcfiles.txt

# 2. All suites
for t in algo.GraphTest algo.MaxFlowTest algo.MergeSortTest algo.StringMatchingTest \
         core.DataStructuresTest core.HashTableTest io.CsvTest \
         main.IntegrationEdgeCasesTest report.GraphRendererTest report.ReportTest; do
  echo "== $t"; java -cp out "$t" | grep "TEST RESULTS"
done

# 3. Valid route through the console
printf '12\nP102,P101,P104\n13\n' | java -cp out main.Main

# 4. Impossible subset
printf '12\nP501,P104\n13\n' | java -cp out main.Main

# 5. Over-cap rejection (21 IDs)
IDS=$(for i in $(seq 1 21); do printf 'P%d,' "$i"; done | sed 's/,$//')
printf '12\n%s\n13\n' "$IDS" | java -cp out main.Main
```

---

## 12. Known limitations (by design, not bugs)

1. **Unweighted costs.** Every citation edge counts as 1 hop; there is no weighting by, e.g.,
   citation strength or recency — the graph model has no edge weights.
2. **Exponential by nature.** Beyond ~20 papers the problem is intractable at interactive speed,
   hence the hard cap rather than a silent slowdown.
3. **Ties are broken arbitrarily.** When several orders share the minimum cost, whichever the
   DP encounters first wins; the *cost* is always optimal, but the specific route among equal-cost
   routes is not guaranteed to be unique/deterministic across future edits.
4. **No partial success.** If the subset cannot be fully connected, the method returns no route
   at all rather than the best-effort path over the largest connectable subset.
