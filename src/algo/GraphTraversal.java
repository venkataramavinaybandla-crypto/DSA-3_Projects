package algo;

import core.ArrayQueue;
import core.ArrayStack;
import core.DynamicArray;
import core.Graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Graph traversal algorithms including Breadth-First Search (BFS) and Depth-First Search (DFS).
 * Uses ArrayQueue and ArrayStack without standard collections.
 */
public class GraphTraversal {

    /** Citation graph instance (optional, for instance-based traversal calls). */
    private Graph graph;

    /** Default constructor. */
    public GraphTraversal() {}

    /**
     * Constructs a GraphTraversal bound to a specific citation graph.
     *
     * @param graph the citation graph
     */
    public GraphTraversal(Graph graph) {
        this.graph = graph;
    }

    /**
     * Sets the citation graph for this traverser.
     *
     * @param graph the citation graph
     */
    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    /**
     * Gets the citation graph associated with this traverser.
     *
     * @return the citation graph, or null if unset
     */
    public Graph getGraph() {
        return this.graph;
    }

    /** Last computed chain length (hop count), -1 if unreachable. */
    private int lastChainLength = -1;

    /**
     * Traverses the graph in Breadth-First Search (BFS) order starting from startIndex.
     * Only visits the reachable component from startIndex.
     *
     * @param graph      the graph to traverse
     * @param startIndex the starting vertex index
     * @return dynamic array of visited vertex indices in BFS order
     */
    public static DynamicArray<Integer> bfs(Graph graph, int startIndex) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        if (startIndex < 0 || startIndex >= graph.vertexCount()) {
            throw new IndexOutOfBoundsException("Start index out of bounds: " + startIndex + ", Vertex count: " + graph.vertexCount());
        }

        DynamicArray<Integer> visitOrder = new DynamicArray<>();
        boolean[] visited = new boolean[graph.vertexCount()];
        ArrayQueue<Integer> queue = new ArrayQueue<>();

        visited[startIndex] = true;
        queue.enqueue(startIndex);

        while (!queue.isEmpty()) {
            int current = queue.dequeue();
            visitOrder.add(current);

            DynamicArray<Integer> neighbors = graph.getNeighbors(current);
            for (int i = 0; i < neighbors.size(); i++) {
                int neighbor = neighbors.get(i);
                if (!visited[neighbor]) {
                    visited[neighbor] = true;
                    queue.enqueue(neighbor);
                }
            }
        }

        return visitOrder;
    }

    /**
     * Traverses the graph in Depth-First Search (DFS) order starting from startIndex.
     * Iterative implementation using ArrayStack.
     * Only visits the reachable component from startIndex.
     *
     * @param graph      the graph to traverse
     * @param startIndex the starting vertex index
     * @return dynamic array of visited vertex indices in DFS order
     */
    public static DynamicArray<Integer> dfs(Graph graph, int startIndex) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        if (startIndex < 0 || startIndex >= graph.vertexCount()) {
            throw new IndexOutOfBoundsException("Start index out of bounds: " + startIndex + ", Vertex count: " + graph.vertexCount());
        }

        DynamicArray<Integer> visitOrder = new DynamicArray<>();
        boolean[] visited = new boolean[graph.vertexCount()];
        ArrayStack<Integer> stack = new ArrayStack<>();

        stack.push(startIndex);

        while (!stack.isEmpty()) {
            int current = stack.pop();

            if (!visited[current]) {
                visited[current] = true;
                visitOrder.add(current);

                DynamicArray<Integer> neighbors = graph.getNeighbors(current);
                // Push neighbors in reverse order so that neighbor at index 0 is popped and visited first
                for (int i = neighbors.size() - 1; i >= 0; i--) {
                    int neighbor = neighbors.get(i);
                    if (!visited[neighbor]) {
                        stack.push(neighbor);
                    }
                }
            }
        }

        return visitOrder;
    }

    /**
     * Narrates a citation chain between two papers. Reuses existing bfs() to
     * first verify reachability, then reconstructs one shortest path using a
     * parent-tracking BFS and formats the chain as:
     * "Paper &lt;A&gt; refers to Paper &lt;B&gt; &amp; Paper &lt;B&gt; refers to Paper &lt;C&gt;,
     * so Paper &lt;A&gt; refers to Paper &lt;C&gt;."
     *
     * @param graph   the citation graph
     * @param startId the ID of the starting paper
     * @param endId   the ID of the ending paper
     * @return the narrated chain string, or "No path found." if unreachable
     */
    public String narrateChain(Graph graph, String startId, String endId) {
        int startIdx = graph.findIndexById(startId);
        int endIdx = graph.findIndexById(endId);

        if (startIdx == -1 || endIdx == -1) {
            lastChainLength = -1;
            return "No path found.";
        }

        if (startIdx == endIdx) {
            lastChainLength = 0;
            return "Paper " + startId + " is the same as Paper " + endId + ".";
        }

        // Use existing bfs() to verify that endIdx is reachable from startIdx
        DynamicArray<Integer> bfsOrder = bfs(graph, startIdx);
        boolean reachable = false;
        for (int i = 0; i < bfsOrder.size(); i++) {
            if (bfsOrder.get(i) == endIdx) {
                reachable = true;
                break;
            }
        }

        if (!reachable) {
            lastChainLength = -1;
            return "No path found.";
        }

        // Reconstruct one shortest path via parent-tracking BFS
        int n = graph.vertexCount();
        int[] parent = new int[n];
        boolean[] visited = new boolean[n];
        for (int i = 0; i < n; i++) {
            parent[i] = -1;
        }

        ArrayQueue<Integer> queue = new ArrayQueue<>();
        visited[startIdx] = true;
        queue.enqueue(startIdx);

        while (!queue.isEmpty()) {
            int current = queue.dequeue();
            if (current == endIdx) {
                break;
            }
            DynamicArray<Integer> neighbors = graph.getNeighbors(current);
            for (int i = 0; i < neighbors.size(); i++) {
                int neighbor = neighbors.get(i);
                if (!visited[neighbor]) {
                    visited[neighbor] = true;
                    parent[neighbor] = current;
                    queue.enqueue(neighbor);
                }
            }
        }

        // Backtrack to build the path from start to end
        DynamicArray<String> pathIds = new DynamicArray<>();
        int cur = endIdx;
        while (cur != -1) {
            pathIds.add(graph.getPaper(cur).getId());
            cur = parent[cur];
        }

        // Reverse the path (it's currently end -> start)
        DynamicArray<String> reversedPath = new DynamicArray<>();
        for (int i = pathIds.size() - 1; i >= 0; i--) {
            reversedPath.add(pathIds.get(i));
        }

        lastChainLength = reversedPath.size() - 1;

        // Build the narrated chain
        // For each consecutive pair: "Paper <A> refers to Paper <B>"
        // Join pairs with " & "
        // Append ", so Paper <first> refers to Paper <last>."
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < reversedPath.size() - 1; i++) {
            if (i > 0) {
                sb.append(" & ");
            }
            sb.append("Paper ").append(reversedPath.get(i))
              .append(" refers to Paper ").append(reversedPath.get(i + 1));
        }

        if (reversedPath.size() > 2) {
            sb.append(", so Paper ").append(reversedPath.get(0))
              .append(" refers to Paper ").append(reversedPath.get(reversedPath.size() - 1));
        }
        sb.append(".");

        return sb.toString();
    }

    /**
     * Narrates a citation chain using the traverser's bound graph.
     *
     * @param startId the ID of the starting paper
     * @param endId   the ID of the ending paper
     * @return the narrated chain string
     */
    public String narrateChain(String startId, String endId) {
        if (this.graph == null) {
            throw new IllegalStateException("Graph has not been set for GraphTraversal");
        }
        return narrateChain(this.graph, startId, endId);
    }

    /**
     * Returns the hop count of the last chain computed by {@link #narrateChain},
     * or -1 if no path was found or narrateChain has not been called.
     *
     * @return the hop count, or -1 if unreachable
     */
    public int getChainLength() {
        return lastChainLength;
    }

    /** Hard cap on the number of paths enumerated by {@link #findAllPaths}. */
    private static final int MAX_PATHS = 10_000;

    /**
     * Finds all simple (loop-free) directed paths from {@code startId} to
     * {@code endId} using recursive DFS with backtracking.
     * <p>
     * Enumeration is hard-capped at {@value #MAX_PATHS} paths. If the cap is
     * reached a warning is printed to {@code System.err}.
     *
     * @param graph   the citation graph
     * @param startId the source paper ID
     * @param endId   the target paper ID
     * @return a DynamicArray of paths, where each path is a DynamicArray of paper IDs
     */
    public DynamicArray<DynamicArray<String>> findAllPaths(Graph graph, String startId, String endId) {
        DynamicArray<DynamicArray<String>> allPaths = new DynamicArray<>();

        int startIdx = graph.findIndexById(startId);
        int endIdx = graph.findIndexById(endId);

        if (startIdx == -1 || endIdx == -1) {
            return allPaths;
        }

        boolean[] visited = new boolean[graph.vertexCount()];
        DynamicArray<Integer> currentPath = new DynamicArray<>();

        dfsEnumerate(graph, startIdx, endIdx, visited, currentPath, allPaths);
        return allPaths;
    }

    /**
     * Finds all simple (loop-free) directed paths from {@code startId} to
     * {@code endId} using the bound citation graph, returning a standard {@link List} of paths.
     *
     * @param startId the source paper ID
     * @param endId   the target paper ID
     * @return a List of paths, where each path is a List of paper IDs
     */
    public List<List<String>> findAllPaths(String startId, String endId) {
        if (this.graph == null) {
            throw new IllegalStateException("Graph has not been set for GraphTraversal");
        }
        return findAllPathsList(this.graph, startId, endId);
    }

    /**
     * Finds all simple (loop-free) directed paths from {@code startId} to
     * {@code endId} returning a standard {@link List} of paths.
     *
     * @param graph   the citation graph
     * @param startId the source paper ID
     * @param endId   the target paper ID
     * @return a List of paths, where each path is a List of paper IDs
     */
    public List<List<String>> findAllPathsList(Graph graph, String startId, String endId) {
        DynamicArray<DynamicArray<String>> dArrayPaths = findAllPaths(graph, startId, endId);
        List<List<String>> listPaths = new ArrayList<>();
        for (int i = 0; i < dArrayPaths.size(); i++) {
            DynamicArray<String> dPath = dArrayPaths.get(i);
            List<String> lPath = new ArrayList<>();
            for (int j = 0; j < dPath.size(); j++) {
                lPath.add(dPath.get(j));
            }
            listPaths.add(lPath);
        }
        return listPaths;
    }

    /**
     * Recursive DFS helper that enumerates all simple paths.
     * Stops adding new paths once {@link #MAX_PATHS} is reached.
     */
    private void dfsEnumerate(Graph graph, int current, int endIdx,
                              boolean[] visited, DynamicArray<Integer> currentPath,
                              DynamicArray<DynamicArray<String>> allPaths) {

        if (allPaths.size() >= MAX_PATHS) {
            return;
        }

        visited[current] = true;
        currentPath.add(current);

        if (current == endIdx) {
            // Snapshot current path as paper IDs
            DynamicArray<String> pathCopy = new DynamicArray<>();
            for (int i = 0; i < currentPath.size(); i++) {
                pathCopy.add(graph.getPaper(currentPath.get(i)).getId());
            }
            allPaths.add(pathCopy);

            if (allPaths.size() >= MAX_PATHS) {
                System.out.println("[Warning] Path enumeration hard-cap of " + MAX_PATHS
                        + " reached. Results are truncated.");
                System.err.println("[Warning] Path enumeration hard-cap of " + MAX_PATHS
                        + " reached. Results are truncated.");
            }
        } else {
            DynamicArray<Integer> neighbors = graph.getNeighbors(current);
            for (int i = 0; i < neighbors.size(); i++) {
                int neighbor = neighbors.get(i);
                if (!visited[neighbor]) {
                    dfsEnumerate(graph, neighbor, endIdx, visited, currentPath, allPaths);
                    if (allPaths.size() >= MAX_PATHS) {
                        break;
                    }
                }
            }
        }

        // Backtrack
        visited[current] = false;
        currentPath.remove(currentPath.size() - 1);
    }

    /**
     * Checks whether a given path is a Hamiltonian path, i.e. it visits every
     * node in the graph exactly once.
     *
     * @param path           a single path represented as a DynamicArray of paper IDs
     * @param totalNodeCount the total number of nodes in the graph
     * @return {@code true} if the path visits exactly {@code totalNodeCount}
     *         distinct nodes (i.e. every node once)
     */
    public static boolean isHamiltonianPath(DynamicArray<String> path, int totalNodeCount) {
        if (path == null || totalNodeCount <= 0 || path.size() != totalNodeCount) {
            return false;
        }
        // Verify all elements are distinct
        for (int i = 0; i < path.size(); i++) {
            for (int j = i + 1; j < path.size(); j++) {
                if (path.get(i).equals(path.get(j))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks whether a given path is a Hamiltonian path, i.e. it visits every
     * node in the graph exactly once.
     *
     * @param path           a single path represented as a List of paper IDs
     * @param totalNodeCount the total number of nodes in the graph
     * @return {@code true} if the path visits exactly {@code totalNodeCount}
     *         distinct nodes (i.e. every node once)
     */
    public static boolean isHamiltonianPath(List<String> path, int totalNodeCount) {
        if (path == null || totalNodeCount <= 0 || path.size() != totalNodeCount) {
            return false;
        }
        // Verify all elements are distinct
        for (int i = 0; i < path.size(); i++) {
            for (int j = i + 1; j < path.size(); j++) {
                if (path.get(i).equals(path.get(j))) {
                    return false;
                }
            }
        }
        return true;
    }

    // =====================================================================
    // Optimal Citation Path (Held-Karp bitmask dynamic programming)
    // =====================================================================

    /** Hard cap on the number of papers accepted by {@link #optimalCitationPath}. */
    public static final int OPTIMAL_PATH_MAX_PAPERS = 20;

    /** Canonical message returned when no directed path can connect every requested paper. */
    public static final String NO_VALID_PATH_MESSAGE = "No valid path exists for this subset";

    /** Sentinel meaning "no directed route" in the pairwise distance table. */
    private static final int NO_ROUTE = Integer.MAX_VALUE / 4;

    /**
     * Outcome of {@link #optimalCitationPath}: either a minimum-cost directed route that
     * visits every requested paper, or a "no valid path" result.
     */
    public static class OptimalPathResult {
        private final boolean found;
        private final List<String> path;
        private final int cost;
        private final String message;

        private OptimalPathResult(boolean found, List<String> path, int cost, String message) {
            this.found = found;
            this.path = path;
            this.cost = cost;
            this.message = message;
        }

        /** @return true when a valid route connecting every requested paper exists */
        public boolean isFound() {
            return found;
        }

        /** @return the ordered paper IDs along the route (empty when not found) */
        public List<String> getPath() {
            return path;
        }

        /** @return the total number of citation hops (0 for a single paper) */
        public int getCost() {
            return cost;
        }

        /** @return a human-readable summary, or {@link #NO_VALID_PATH_MESSAGE} when not found */
        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            if (!found) {
                return message;
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < path.size(); i++) {
                if (i > 0) {
                    sb.append(" -> ");
                }
                sb.append(path.get(i));
            }
            sb.append(" (total cost: ").append(cost).append(")");
            return sb.toString();
        }
    }

    /**
     * Finds the minimum-cost directed citation path that visits every paper in the given
     * subset, using a Held-Karp style bitmask dynamic program over a {@code dp[mask][lastNode]}
     * table.
     *
     * <p>Directed citation edges are respected end-to-end: the pairwise cost between two
     * requested papers is the length of the shortest directed route (which may pass through
     * papers outside the subset), computed once per source with a level-order sweep. The DP
     * then picks the cheapest order that visits the whole subset, and the concrete sub-routes
     * are stitched back together into one explicit paper route.
     *
     * <p>At most {@value #OPTIMAL_PATH_MAX_PAPERS} papers are accepted; a larger subset throws
     * {@link IllegalArgumentException}. When no directed route can connect every requested
     * paper the method returns a result carrying {@link #NO_VALID_PATH_MESSAGE} instead of
     * throwing.
     *
     * @param paperIds the paper IDs to visit (duplicates are collapsed to one visit)
     * @return an {@link OptimalPathResult} with the route and its total cost, or a no-path result
     * @throws IllegalArgumentException if {@code paperIds} is null or holds more than
     *         {@value #OPTIMAL_PATH_MAX_PAPERS} papers
     * @throws IllegalStateException    if no graph has been bound to this traverser
     */
    public OptimalPathResult optimalCitationPath(List<String> paperIds) {
        if (this.graph == null) {
            throw new IllegalStateException("Graph has not been set for GraphTraversal");
        }
        return optimalCitationPath(this.graph, paperIds);
    }

    /**
     * Convenience overload of {@link #optimalCitationPath(List)} that accepts this project's
     * {@link DynamicArray} so callers can avoid standard collection types.
     *
     * @param paperIds the paper IDs to visit
     * @return the optimal route result
     * @throws IllegalArgumentException if {@code paperIds} is null or exceeds the hard cap
     * @throws IllegalStateException    if no graph has been bound to this traverser
     */
    public OptimalPathResult optimalCitationPath(DynamicArray<String> paperIds) {
        if (paperIds == null) {
            throw new IllegalArgumentException("Paper ID list cannot be null");
        }
        List<String> asList = new ArrayList<>();
        for (int i = 0; i < paperIds.size(); i++) {
            asList.add(paperIds.get(i));
        }
        return optimalCitationPath(asList);
    }

    /**
     * Core optimal-path routine against an explicit graph.
     *
     * @param graph    the citation graph
     * @param paperIds the paper IDs to visit
     * @return the optimal route result
     * @throws IllegalArgumentException if {@code paperIds} is null or exceeds the hard cap
     */
    public OptimalPathResult optimalCitationPath(Graph graph, List<String> paperIds) {
        if (paperIds == null) {
            throw new IllegalArgumentException("Paper ID list cannot be null");
        }
        if (paperIds.size() > OPTIMAL_PATH_MAX_PAPERS) {
            throw new IllegalArgumentException(
                    "Too many papers for optimalCitationPath: " + paperIds.size()
                            + " requested, hard cap is " + OPTIMAL_PATH_MAX_PAPERS);
        }

        // Resolve requested IDs to distinct vertex indices, preserving first-seen order.
        List<Integer> nodes = new ArrayList<>();
        for (int i = 0; i < paperIds.size(); i++) {
            String id = paperIds.get(i);
            if (id == null) {
                continue;
            }
            int idx = graph.findIndexById(id.trim());
            if (idx == -1) {
                return noValidPath();
            }
            if (!nodes.contains(idx)) {
                nodes.add(idx);
            }
        }

        int n = nodes.size();
        if (n == 0) {
            return new OptimalPathResult(true, new ArrayList<String>(), 0,
                    "No papers requested; empty route with total cost 0.");
        }
        if (n == 1) {
            List<String> single = new ArrayList<>();
            single.add(graph.getPaper(nodes.get(0)).getId());
            return new OptimalPathResult(true, single, 0,
                    "Single-paper route with total cost 0.");
        }

        int vertexCount = graph.vertexCount();
        int[][] shortestDist = new int[n][vertexCount];
        int[][] shortestParent = new int[n][vertexCount];
        for (int s = 0; s < n; s++) {
            levelOrderShortestPaths(graph, nodes.get(s), shortestDist[s], shortestParent[s]);
        }

        // Held-Karp bitmask DP: dp[mask][lastNode] = cheapest way to cover mask, ending at lastNode.
        int fullMask = (1 << n) - 1;
        int stateCount = 1 << n;
        int[] dp = new int[stateCount * n];
        byte[] predecessor = new byte[stateCount * n];
        for (int i = 0; i < dp.length; i++) {
            dp[i] = NO_ROUTE;
            predecessor[i] = (byte) -1;
        }
        for (int i = 0; i < n; i++) {
            dp[(1 << i) * n + i] = 0;
        }

        for (int mask = 1; mask < stateCount; mask++) {
            int base = mask * n;
            for (int last = 0; last < n; last++) {
                if ((mask & (1 << last)) == 0) {
                    continue;
                }
                int current = dp[base + last];
                if (current >= NO_ROUTE) {
                    continue;
                }
                for (int next = 0; next < n; next++) {
                    if ((mask & (1 << next)) != 0) {
                        continue;
                    }
                    int leg = shortestDist[last][nodes.get(next)];
                    if (leg >= NO_ROUTE) {
                        continue;
                    }
                    int nextMask = mask | (1 << next);
                    int slot = nextMask * n + next;
                    int candidate = current + leg;
                    if (candidate < dp[slot]) {
                        dp[slot] = candidate;
                        predecessor[slot] = (byte) last;
                    }
                }
            }
        }

        int bestLast = -1;
        int bestCost = NO_ROUTE;
        int fullBase = fullMask * n;
        for (int last = 0; last < n; last++) {
            if (dp[fullBase + last] < bestCost) {
                bestCost = dp[fullBase + last];
                bestLast = last;
            }
        }
        if (bestLast == -1) {
            return noValidPath();
        }

        // Reconstruct the visiting order of the subset nodes from the bitmask table.
        List<Integer> order = new ArrayList<>();
        int mask = fullMask;
        int last = bestLast;
        while (last != -1) {
            order.add(0, last);
            int prev = predecessor[mask * n + last];
            mask &= ~(1 << last);
            last = prev;
        }

        // Stitch the concrete sub-routes into one explicit paper route.
        List<String> route = new ArrayList<>();
        route.add(graph.getPaper(nodes.get(order.get(0))).getId());
        int totalCost = 0;
        for (int i = 0; i < order.size() - 1; i++) {
            int fromSlot = order.get(i);
            int toSlot = order.get(i + 1);
            int toNode = nodes.get(toSlot);
            int leg = shortestDist[fromSlot][toNode];
            if (leg >= NO_ROUTE) {
                return noValidPath();
            }
            totalCost += leg;
            appendRouteTail(graph, shortestParent[fromSlot], nodes.get(fromSlot), toNode, route);
        }

        return new OptimalPathResult(true, route, totalCost,
                "Optimal route with total cost " + totalCost + ".");
    }

    /**
     * Level-order sweep that fills shortest-hop distances (and the parent used to reconstruct
     * them) from {@code source} to every vertex in {@code graph}.
     */
    private void levelOrderShortestPaths(Graph graph, int source, int[] dist, int[] parent) {
        int vertexCount = graph.vertexCount();
        for (int i = 0; i < vertexCount; i++) {
            dist[i] = NO_ROUTE;
            parent[i] = -1;
        }

        ArrayQueue<Integer> frontier = new ArrayQueue<>();
        dist[source] = 0;
        frontier.enqueue(source);

        while (!frontier.isEmpty()) {
            int current = frontier.dequeue();
            DynamicArray<Integer> neighbors = graph.getNeighbors(current);
            for (int i = 0; i < neighbors.size(); i++) {
                int neighbor = neighbors.get(i);
                if (dist[neighbor] == NO_ROUTE) {
                    dist[neighbor] = dist[current] + 1;
                    parent[neighbor] = current;
                    frontier.enqueue(neighbor);
                }
            }
        }
    }

    /**
     * Appends the vertices strictly after {@code from} on the shortest route to {@code to}.
     * {@code from} is assumed to already be the last element of {@code route}.
     */
    private void appendRouteTail(Graph graph, int[] parent, int from, int to, List<String> route) {
        List<Integer> reversed = new ArrayList<>();
        int current = to;
        while (current != -1 && current != from) {
            reversed.add(current);
            current = parent[current];
        }
        for (int i = reversed.size() - 1; i >= 0; i--) {
            route.add(graph.getPaper(reversed.get(i)).getId());
        }
    }

    /** Builds the canonical "no valid path" result. */
    private static OptimalPathResult noValidPath() {
        return new OptimalPathResult(false, new ArrayList<String>(), -1, NO_VALID_PATH_MESSAGE);
    }
}

