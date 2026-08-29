package algo;

import core.DynamicArray;
import core.Graph;
import core.Paper;

/**
 * Test suite for Graph core and GraphTraversal algorithms (Phase 2).
 */
public class GraphTest {

    private static int totalTests = 0;
    private static int passedTests = 0;

    public static void main(String[] args) {
        System.out.println("Running Phase 2 Graph & GraphTraversal Test Suite...\n");

        testAddVertex();
        testAddEdge();
        testAddCitation();
        testBFS();
        testDFS();
        testEdgeCases();

        System.out.println("\n==========================================");
        System.out.println("GRAPH TEST RESULTS: " + passedTests + " / " + totalTests + " PASSED");
        System.out.println("==========================================");

        if (passedTests != totalTests) {
            System.exit(1);
        }
    }

    private static void assertTrue(String name, boolean cond) {
        totalTests++;
        if (cond) {
            passedTests++;
            System.out.println("[PASS] " + name);
        } else {
            System.err.println("[FAIL] " + name);
            throw new AssertionError("FAILED: " + name);
        }
    }

    private static void assertEquals(String name, Object expected, Object actual) {
        totalTests++;
        boolean eq = (expected == null) ? (actual == null) : expected.equals(actual);
        if (eq) {
            passedTests++;
            System.out.println("[PASS] " + name);
        } else {
            System.err.println("[FAIL] " + name + " | Expected=" + expected + " Actual=" + actual);
            throw new AssertionError("FAILED: " + name);
        }
    }

    // ------------------------------------------------------------------ addVertex
    private static void testAddVertex() {
        System.out.println("--- Graph.addVertex Tests ---");
        Graph graph = new Graph();

        Paper p0 = new Paper("P0", "Title 0", "Author 0", 2020);
        Paper p1 = new Paper("P1", "Title 1", "Author 1", 2021);
        Paper p2 = new Paper("P2", "Title 2", "Author 2", 2022);

        int idx0 = graph.addVertex(p0);
        int idx1 = graph.addVertex(p1);
        int idx2 = graph.addVertex(p2);

        assertEquals("idx0 is 0", 0, idx0);
        assertEquals("idx1 is 1", 1, idx1);
        assertEquals("idx2 is 2", 2, idx2);
        assertEquals("vertexCount is 3", 3, graph.vertexCount());

        // Duplicate id returns EXISTING index and vertexCount does not increase
        Paper p0Dup = new Paper("P0", "Duplicate Title", "Author X", 2025);
        int dupIdx = graph.addVertex(p0Dup);
        assertEquals("Duplicate addVertex returns existing idx 0", idx0, dupIdx);
        assertEquals("vertexCount stays 3 after duplicate insert", 3, graph.vertexCount());
    }

    // ------------------------------------------------------------------ addEdge
    private static void testAddEdge() {
        System.out.println("\n--- Graph.addEdge Tests ---");
        Graph graph = new Graph();
        graph.addVertex(new Paper("P0", "T0", "A0", 2020));
        graph.addVertex(new Paper("P1", "T1", "A1", 2021));

        graph.addEdge(0, 1);
        assertEquals("edgeCount is 1", 1, graph.edgeCount());

        // Duplicate edge is ignored
        graph.addEdge(0, 1);
        assertEquals("edgeCount stays 1 after adding duplicate edge", 1, graph.edgeCount());
        assertEquals("neighbors size of 0 is 1", 1, graph.getNeighbors(0).size());

        // Out-of-bounds index rejects with IndexOutOfBoundsException
        boolean threwFrom = false;
        try {
            graph.addEdge(-1, 1);
        } catch (IndexOutOfBoundsException e) {
            threwFrom = true;
        }
        assertTrue("addEdge negative fromIndex throws IndexOutOfBoundsException", threwFrom);

        boolean threwTo = false;
        try {
            graph.addEdge(0, 99);
        } catch (IndexOutOfBoundsException e) {
            threwTo = true;
        }
        assertTrue("addEdge out of bounds toIndex throws IndexOutOfBoundsException", threwTo);
    }

    // ------------------------------------------------------------------ addCitation
    private static void testAddCitation() {
        System.out.println("\n--- Graph.addCitation Tests ---");
        Graph graph = new Graph();
        graph.addVertex(new Paper("P0", "T0", "A0", 2020));
        graph.addVertex(new Paper("P1", "T1", "A1", 2021));

        graph.addCitation("P0", "P1");
        assertEquals("edgeCount is 1", 1, graph.edgeCount());
        assertTrue("neighbors of 0 contains 1", graph.getNeighbors(0).contains(1));

        // Unknown id throws IllegalArgumentException
        boolean threwCiting = false;
        try {
            graph.addCitation("UNKNOWN", "P1");
        } catch (IllegalArgumentException e) {
            threwCiting = true;
        }
        assertTrue("addCitation unknown citing paper throws IllegalArgumentException", threwCiting);

        boolean threwCited = false;
        try {
            graph.addCitation("P0", "UNKNOWN");
        } catch (IllegalArgumentException e) {
            threwCited = true;
        }
        assertTrue("addCitation unknown cited paper throws IllegalArgumentException", threwCited);
    }

    // ------------------------------------------------------------------ BFS
    private static void testBFS() {
        System.out.println("\n--- BFS Traversal Tests ---");
        // Graph structure:
        // 0 -> 1, 0 -> 2
        // 1 -> 3, 1 -> 4
        // 2 -> 5
        // 6 is disconnected
        Graph graph = new Graph();
        for (int i = 0; i <= 6; i++) {
            graph.addVertex(new Paper("P" + i, "T" + i, "A" + i, 2020 + i));
        }

        graph.addEdge(0, 1);
        graph.addEdge(0, 2);
        graph.addEdge(1, 3);
        graph.addEdge(1, 4);
        graph.addEdge(2, 5);

        DynamicArray<Integer> bfsResult = GraphTraversal.bfs(graph, 0);
        assertEquals("BFS visit count (component of 6 reachable vertices)", 6, bfsResult.size());
        assertEquals("BFS order[0] == 0", 0, (int) bfsResult.get(0));
        assertEquals("BFS order[1] == 1", 1, (int) bfsResult.get(1));
        assertEquals("BFS order[2] == 2", 2, (int) bfsResult.get(2));
        assertEquals("BFS order[3] == 3", 3, (int) bfsResult.get(3));
        assertEquals("BFS order[4] == 4", 4, (int) bfsResult.get(4));
        assertEquals("BFS order[5] == 5", 5, (int) bfsResult.get(5));
        assertTrue("BFS excludes disconnected vertex 6", !bfsResult.contains(6));
    }

    // ------------------------------------------------------------------ DFS
    private static void testDFS() {
        System.out.println("\n--- DFS Traversal Tests ---");
        // Graph structure:
        // 0 -> 1, 0 -> 2
        // 1 -> 3, 1 -> 4
        // 2 -> 5
        // 6 is disconnected
        Graph graph = new Graph();
        for (int i = 0; i <= 6; i++) {
            graph.addVertex(new Paper("P" + i, "T" + i, "A" + i, 2020 + i));
        }

        graph.addEdge(0, 1);
        graph.addEdge(0, 2);
        graph.addEdge(1, 3);
        graph.addEdge(1, 4);
        graph.addEdge(2, 5);

        DynamicArray<Integer> dfsResult = GraphTraversal.dfs(graph, 0);
        assertEquals("DFS visit count (component of 6 reachable vertices)", 6, dfsResult.size());
        assertEquals("DFS order[0] == 0", 0, (int) dfsResult.get(0));
        assertEquals("DFS order[1] == 1 (left path first)", 1, (int) dfsResult.get(1));
        assertEquals("DFS order[2] == 3 (deepest left neighbor of 1)", 3, (int) dfsResult.get(2));
        assertEquals("DFS order[3] == 4 (next neighbor of 1)", 4, (int) dfsResult.get(3));
        assertEquals("DFS order[4] == 2 (backtrack to 0, visit right branch)", 2, (int) dfsResult.get(4));
        assertEquals("DFS order[5] == 5 (deepest neighbor of 2)", 5, (int) dfsResult.get(5));
        assertTrue("DFS excludes disconnected vertex 6", !dfsResult.contains(6));
    }

    // ------------------------------------------------------------------ Edge Cases
    private static void testEdgeCases() {
        System.out.println("\n--- Edge Cases (Single Vertex & Cyclic Graph) ---");

        // Single vertex graph
        Graph single = new Graph();
        single.addVertex(new Paper("P0", "T0", "A0", 2020));

        DynamicArray<Integer> singleBfs = GraphTraversal.bfs(single, 0);
        assertEquals("Single vertex BFS size is 1", 1, singleBfs.size());
        assertEquals("Single vertex BFS element is 0", 0, (int) singleBfs.get(0));

        DynamicArray<Integer> singleDfs = GraphTraversal.dfs(single, 0);
        assertEquals("Single vertex DFS size is 1", 1, singleDfs.size());
        assertEquals("Single vertex DFS element is 0", 0, (int) singleDfs.get(0));

        // Cyclic graph: 0 -> 1 -> 2 -> 0
        Graph cyclic = new Graph();
        cyclic.addVertex(new Paper("C0", "T0", "A0", 2020));
        cyclic.addVertex(new Paper("C1", "T1", "A1", 2021));
        cyclic.addVertex(new Paper("C2", "T2", "A2", 2022));

        cyclic.addEdge(0, 1);
        cyclic.addEdge(1, 2);
        cyclic.addEdge(2, 0);

        DynamicArray<Integer> cyclicBfs = GraphTraversal.bfs(cyclic, 0);
        assertEquals("Cyclic BFS terminates with size 3", 3, cyclicBfs.size());
        assertEquals("Cyclic BFS [0]", 0, (int) cyclicBfs.get(0));
        assertEquals("Cyclic BFS [1]", 1, (int) cyclicBfs.get(1));
        assertEquals("Cyclic BFS [2]", 2, (int) cyclicBfs.get(2));

        DynamicArray<Integer> cyclicDfs = GraphTraversal.dfs(cyclic, 0);
        assertEquals("Cyclic DFS terminates with size 3", 3, cyclicDfs.size());
        assertEquals("Cyclic DFS [0]", 0, (int) cyclicDfs.get(0));
        assertEquals("Cyclic DFS [1]", 1, (int) cyclicDfs.get(1));
        assertEquals("Cyclic DFS [2]", 2, (int) cyclicDfs.get(2));
    }
}
