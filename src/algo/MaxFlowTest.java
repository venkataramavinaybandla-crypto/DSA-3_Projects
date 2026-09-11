package algo;

import core.Graph;
import core.Paper;

/**
 * Test suite for Edmonds-Karp maximum flow algorithm (Phase 5).
 */
public class MaxFlowTest {

    private static int totalTests = 0;
    private static int passedTests = 0;

    public static void main(String[] args) {
        System.out.println("Running Phase 5 Edmonds-Karp Max-Flow Test Suite...\n");

        testSinglePathFlow();
        testDiamondDisjointPaths();
        testThreeDisjointChains();
        testBottleneckNetwork();
        testDisconnectedVertices();
        testSelfLoopAndSameSourceSink();
        testCyclicNetwork();
        testIdBasedApiAndExceptions();

        System.out.println("\n==========================================");
        System.out.println("MAX-FLOW TEST RESULTS: " + passedTests + " / " + totalTests + " PASSED");
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

    private static void testSinglePathFlow() {
        System.out.println("--- Single Path Flow ---");
        // S -> A -> B -> T
        Graph g = new Graph();
        g.addVertex(new Paper("S", "Source Paper", "Author S", 2020));
        g.addVertex(new Paper("A", "Paper A", "Author A", 2021));
        g.addVertex(new Paper("B", "Paper B", "Author B", 2022));
        g.addVertex(new Paper("T", "Sink Paper", "Author T", 2023));

        g.addCitation("S", "A");
        g.addCitation("A", "B");
        g.addCitation("B", "T");

        int flow = EdmondsKarp.maxFlow(g, 0, 3);
        assertEquals("Single linear path has max-flow 1", 1, flow);
        assertTrue("Flow description mentions 1 chain", EdmondsKarp.describeFlow(flow).contains("1 edge-disjoint citation chain"));
    }

    private static void testDiamondDisjointPaths() {
        System.out.println("\n--- Diamond Network (2 Disjoint Paths) ---");
        // S -> A -> T
        // S -> B -> T
        Graph g = new Graph();
        g.addVertex(new Paper("S", "Source", "Author S", 2020));
        g.addVertex(new Paper("A", "Upper", "Author A", 2021));
        g.addVertex(new Paper("B", "Lower", "Author B", 2021));
        g.addVertex(new Paper("T", "Sink", "Author T", 2022));

        g.addCitation("S", "A");
        g.addCitation("A", "T");
        g.addCitation("S", "B");
        g.addCitation("B", "T");

        int flow = EdmondsKarp.maxFlow(g, 0, 3);
        assertEquals("Diamond network has max-flow 2", 2, flow);
    }

    private static void testThreeDisjointChains() {
        System.out.println("\n--- Three Disjoint Chains ---");
        // S -> A1 -> A2 -> T
        // S -> B1 -> B2 -> T
        // S -> C1 -> C2 -> T
        Graph g = new Graph();
        g.addVertex(new Paper("S", "Source", "Author S", 2020));
        g.addVertex(new Paper("A1", "A1", "Author A", 2021));
        g.addVertex(new Paper("A2", "A2", "Author A", 2022));
        g.addVertex(new Paper("B1", "B1", "Author B", 2021));
        g.addVertex(new Paper("B2", "B2", "Author B", 2022));
        g.addVertex(new Paper("C1", "C1", "Author C", 2021));
        g.addVertex(new Paper("C2", "C2", "Author C", 2022));
        g.addVertex(new Paper("T", "Sink", "Author T", 2023));

        g.addCitation("S", "A1");
        g.addCitation("A1", "A2");
        g.addCitation("A2", "T");

        g.addCitation("S", "B1");
        g.addCitation("B1", "B2");
        g.addCitation("B2", "T");

        g.addCitation("S", "C1");
        g.addCitation("C1", "C2");
        g.addCitation("C2", "T");

        int flow = EdmondsKarp.maxFlow(g, "S", "T");
        assertEquals("Three disjoint chains has max-flow 3", 3, flow);
        assertTrue("Flow description mentions 3 chains", EdmondsKarp.describeFlow(flow).contains("3 edge-disjoint citation chains"));
    }

    private static void testBottleneckNetwork() {
        System.out.println("\n--- Bottleneck Bridge Network ---");
        // S -> A1, S -> A2
        // A1 -> Bridge, A2 -> Bridge
        // Bridge -> B1, Bridge -> B2
        // B1 -> T, B2 -> T
        Graph g = new Graph();
        g.addVertex(new Paper("S", "Source", "A", 2020));
        g.addVertex(new Paper("A1", "A1", "A", 2021));
        g.addVertex(new Paper("A2", "A2", "A", 2021));
        g.addVertex(new Paper("Bridge", "Bridge", "B", 2022));
        g.addVertex(new Paper("B1", "B1", "C", 2023));
        g.addVertex(new Paper("B2", "B2", "C", 2023));
        g.addVertex(new Paper("T", "Target", "D", 2024));

        g.addCitation("S", "A1");
        g.addCitation("S", "A2");
        g.addCitation("A1", "Bridge");
        g.addCitation("A2", "Bridge");
        g.addCitation("Bridge", "B1");
        g.addCitation("Bridge", "B2");
        g.addCitation("B1", "T");
        g.addCitation("B2", "T");

        int flow = EdmondsKarp.maxFlow(g, "S", "T");
        // Because Bridge is a single vertex with in-degree 2 and out-degree 2, but each outgoing edge from bridge has capacity 1:
        // Wait, Bridge -> B1 has cap 1, Bridge -> B2 has cap 1, so flow through Bridge can be 2!
        // Let's check: S->A1->Bridge->B1->T, and S->A2->Bridge->B2->T are 2 edge-disjoint paths!
        assertEquals("Edge-disjoint flow through shared vertex is 2", 2, flow);

        // Now test a true edge bottleneck: Single edge X -> Y between two cliques
        Graph g2 = new Graph();
        g2.addVertex(new Paper("S", "S", "A", 2020));
        g2.addVertex(new Paper("X", "X", "A", 2021));
        g2.addVertex(new Paper("Y", "Y", "B", 2022));
        g2.addVertex(new Paper("T", "T", "B", 2023));

        g2.addCitation("S", "X");
        g2.addCitation("X", "Y"); // Bottleneck edge of capacity 1
        g2.addCitation("Y", "T");

        int flow2 = EdmondsKarp.maxFlow(g2, "S", "T");
        assertEquals("Single edge bottleneck yields max-flow 1", 1, flow2);
    }

    private static void testDisconnectedVertices() {
        System.out.println("\n--- Disconnected Vertices ---");
        Graph g = new Graph();
        g.addVertex(new Paper("S", "S", "A", 2020));
        g.addVertex(new Paper("A", "A", "A", 2021));
        g.addVertex(new Paper("T", "T", "B", 2022));

        g.addCitation("S", "A"); // No path to T

        int flow = EdmondsKarp.maxFlow(g, "S", "T");
        assertEquals("Disconnected source and sink yields max-flow 0", 0, flow);
        assertTrue("Flow description mentions 0 chains", EdmondsKarp.describeFlow(flow).contains("0 edge-disjoint citation chains"));
    }

    private static void testSelfLoopAndSameSourceSink() {
        System.out.println("\n--- Source Equals Sink & Boundary Conditions ---");
        Graph g = new Graph();
        g.addVertex(new Paper("S", "S", "A", 2020));

        int flow = EdmondsKarp.maxFlow(g, 0, 0);
        assertEquals("Source == sink yields 0 flow", 0, flow);
    }

    private static void testCyclicNetwork() {
        System.out.println("\n--- Cyclic Network ---");
        // S -> A -> B -> T, with cycle B -> A
        Graph g = new Graph();
        g.addVertex(new Paper("S", "S", "A", 2020));
        g.addVertex(new Paper("A", "A", "A", 2021));
        g.addVertex(new Paper("B", "B", "A", 2022));
        g.addVertex(new Paper("T", "T", "A", 2023));

        g.addCitation("S", "A");
        g.addCitation("A", "B");
        g.addCitation("B", "A"); // cycle
        g.addCitation("B", "T");

        int flow = EdmondsKarp.maxFlow(g, "S", "T");
        assertEquals("Max-flow with cycle terminates correctly with flow 1", 1, flow);
    }

    private static void testIdBasedApiAndExceptions() {
        System.out.println("\n--- ID-based API & Exceptions ---");
        Graph g = new Graph();
        g.addVertex(new Paper("P1", "P1", "A", 2020));
        g.addVertex(new Paper("P2", "P2", "A", 2021));

        boolean threwUnknownSource = false;
        try {
            EdmondsKarp.maxFlow(g, "NON_EXISTENT", "P2");
        } catch (IllegalArgumentException e) {
            threwUnknownSource = true;
        }
        assertTrue("Unknown source throws IllegalArgumentException", threwUnknownSource);

        boolean threwUnknownSink = false;
        try {
            EdmondsKarp.maxFlow(g, "P1", "NON_EXISTENT");
        } catch (IllegalArgumentException e) {
            threwUnknownSink = true;
        }
        assertTrue("Unknown sink throws IllegalArgumentException", threwUnknownSink);

        boolean threwIndexOutOfBounds = false;
        try {
            EdmondsKarp.maxFlow(g, -1, 1);
        } catch (IndexOutOfBoundsException e) {
            threwIndexOutOfBounds = true;
        }
        assertTrue("Negative index throws IndexOutOfBoundsException", threwIndexOutOfBounds);
    }
}
