package algo;

import core.ArrayQueue;
import core.DynamicArray;
import core.Graph;

/**
 * Edmonds-Karp algorithm for computing the maximum citation flow between
 * two papers in the citation graph.
 *
 * <p>Uses Breadth-First Search (BFS) via {@link core.ArrayQueue} to find the
 * shortest augmenting paths in terms of number of edges, achieving O(V * E^2)
 * time complexity without any standard library collections.
 *
 * <p>In a citation graph with unit-capacity edges, the max flow between
 * source and sink directly equals the maximum number of edge-disjoint
 * citation chains connecting the two papers.
 */
public final class EdmondsKarp {

    private EdmondsKarp() {
        // utility class, prevent instantiation
    }

    /**
     * Computes the maximum flow from source to sink in the given graph.
     *
     * @param graph  the citation graph
     * @param source vertex index of the source paper
     * @param sink   vertex index of the sink (target) paper
     * @return maximum flow value (number of edge-disjoint citation paths)
     */
    public static int maxFlow(Graph graph, int source, int sink) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        int vCount = graph.vertexCount();
        if (source < 0 || source >= vCount) {
            throw new IndexOutOfBoundsException("Source vertex out of bounds: " + source);
        }
        if (sink < 0 || sink >= vCount) {
            throw new IndexOutOfBoundsException("Sink vertex out of bounds: " + sink);
        }
        if (source == sink) {
            return 0; // Flow from a vertex to itself is 0
        }

        // Build capacity matrix based on graph edges
        // Citation edges have capacity 1 (unit flow network)
        int[][] capacity = new int[vCount][vCount];
        for (int u = 0; u < vCount; u++) {
            DynamicArray<Integer> neighbors = graph.getNeighbors(u);
            for (int i = 0; i < neighbors.size(); i++) {
                int v = neighbors.get(i);
                capacity[u][v] = 1;
            }
        }

        int[][] flow = new int[vCount][vCount];
        int totalFlow = 0;
        int[] parent = new int[vCount];

        // Edmonds-Karp loop: find augmenting paths using BFS
        while (bfsAugmentingPath(capacity, flow, vCount, source, sink, parent)) {
            // Find bottleneck capacity along the augmenting path
            int bottleneck = Integer.MAX_VALUE;
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                int residual = capacity[u][v] - flow[u][v];
                if (residual < bottleneck) {
                    bottleneck = residual;
                }
            }

            // Augment the flow along the path and update reverse edges
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                flow[u][v] += bottleneck;
                flow[v][u] -= bottleneck;
            }

            totalFlow += bottleneck;
        }

        return totalFlow;
    }

    /**
     * Convenience method to compute max flow between two papers by their string IDs.
     *
     * @param graph    the citation graph
     * @param sourceId the ID of the source paper
     * @param sinkId   the ID of the sink paper
     * @return maximum flow value
     */
    public static int maxFlow(Graph graph, String sourceId, String sinkId) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        if (sourceId == null || sinkId == null) {
            throw new IllegalArgumentException("Paper IDs cannot be null");
        }

        int sourceIdx = graph.findIndexById(sourceId);
        if (sourceIdx == -1) {
            throw new IllegalArgumentException("Source paper ID not found in graph: " + sourceId);
        }

        int sinkIdx = graph.findIndexById(sinkId);
        if (sinkIdx == -1) {
            throw new IllegalArgumentException("Sink paper ID not found in graph: " + sinkId);
        }

        return maxFlow(graph, sourceIdx, sinkIdx);
    }

    /**
     * Returns a human-readable interpretation of the computed max-flow value.
     *
     * @param flow the max-flow value
     * @return descriptive string explaining what the flow represents
     */
    public static String describeFlow(int flow) {
        if (flow == 0) {
            return "0 edge-disjoint citation chains (no direct or indirect citation path exists)";
        } else if (flow == 1) {
            return "1 edge-disjoint citation chain (influence passes through a single bottleneck chain)";
        } else {
            return flow + " edge-disjoint citation chains (multiple independent influence pathways exist)";
        }
    }

    /**
     * BFS to find the shortest augmenting path in the residual graph.
     */
    private static boolean bfsAugmentingPath(int[][] capacity, int[][] flow, int vCount, int source, int sink, int[] parent) {
        for (int i = 0; i < vCount; i++) {
            parent[i] = -1;
        }

        parent[source] = source;
        ArrayQueue<Integer> queue = new ArrayQueue<>();
        queue.enqueue(source);

        while (!queue.isEmpty()) {
            int u = queue.dequeue();

            for (int v = 0; v < vCount; v++) {
                // Check if v is unvisited and residual capacity > 0
                if (parent[v] == -1 && capacity[u][v] - flow[u][v] > 0) {
                    parent[v] = u;
                    if (v == sink) {
                        return true; // Reached sink via shortest augmenting path
                    }
                    queue.enqueue(v);
                }
            }
        }

        return parent[sink] != -1;
    }
}
