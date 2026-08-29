package algo;

import core.ArrayQueue;
import core.ArrayStack;
import core.DynamicArray;
import core.Graph;

/**
 * Graph traversal algorithms including Breadth-First Search (BFS) and Depth-First Search (DFS).
 * Uses ArrayQueue and ArrayStack without standard collections.
 */
public class GraphTraversal {

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
}
