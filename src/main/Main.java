package main;

import algo.EdmondsKarp;
import algo.FuzzyMatcher;
import algo.GraphTraversal;
import algo.KMPMatcher;
import core.DynamicArray;
import core.Graph;
import core.Paper;
import io.CsvHandler;
import report.ReportGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Main interactive console application for the Citation Analysis System.
 *
 * <p>Delivered strictly as a terminal/CLI application using {@code System.in} and {@code System.out}.
 * Absolutely zero {@code java.util} imports, zero external dependencies, Java 17 standard library only.
 */
public class Main {

    private static final String DEFAULT_CSV_FILE = "citation_data.csv";

    private Graph graph;
    private final BufferedReader reader;
    private boolean unsavedChanges;

    public Main() {
        this.graph = new Graph();
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.unsavedChanges = false;
        loadSampleDataIfEmpty();
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.run();
    }

    /**
     * Seeds initial academic papers to make the system instantly interactive on launch.
     */
    private void loadSampleDataIfEmpty() {
        Paper p1 = new Paper("P101", "Attention Is All You Need", "Vaswani et al.", 2017);
        Paper p2 = new Paper("P102", "BERT: Pre-training of Deep Bidirectional Transformers", "Devlin et al.", 2018);
        Paper p3 = new Paper("P103", "Language Models are Few-Shot Learners (GPT-3)", "Brown et al.", 2020);
        Paper p4 = new Paper("P104", "Deep Residual Learning for Image Recognition", "He et al.", 2016);
        Paper p5 = new Paper("P105", "Mastering the Game of Go with Deep Neural Networks", "Silver et al.", 2016);

        graph.addVertex(p1);
        graph.addVertex(p2);
        graph.addVertex(p3);
        graph.addVertex(p4);
        graph.addVertex(p5);

        graph.addCitation("P102", "P101"); // BERT cites Attention
        graph.addCitation("P103", "P101"); // GPT-3 cites Attention
        graph.addCitation("P103", "P102"); // GPT-3 cites BERT
        graph.addCitation("P101", "P104"); // Attention cites ResNet
        graph.addCitation("P102", "P104"); // BERT cites ResNet
        graph.addCitation("P105", "P104"); // AlphaGo cites ResNet

        CsvHandler.syncCitationCounts(graph);
    }

    public void run() {
        printBanner();

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = readLine("Enter your choice (1-9): ");
            if (choice == null) {
                // End of input stream (e.g. piped input or EOF)
                System.out.println("\nInput stream closed. Exiting.");
                break;
            }

            choice = choice.trim();
            if (choice.isEmpty()) {
                System.out.println("Please enter a selection from 1 to 9.");
                continue;
            }

            switch (choice) {
                case "1":
                    handleAddPaper();
                    break;
                case "2":
                    handleAddCitation();
                    break;
                case "3":
                    handleSearchPaper();
                    break;
                case "4":
                    handleTraverseGraph();
                    break;
                case "5":
                    handleAnalyzeFlow();
                    break;
                case "6":
                    handleReportsSubmenu();
                    break;
                case "7":
                    handleSaveCsv();
                    break;
                case "8":
                    handleLoadCsv();
                    break;
                case "9":
                    running = handleExit();
                    break;
                default:
                    System.out.println("[Error] Invalid choice: '" + choice + "'. Please enter a number between 1 and 9.");
            }
        }
    }

    private void printBanner() {
        System.out.println("========================================================================");
        System.out.println("                          CERBERUS SYSTEM (CS)                       ");
        System.out.println("                 Pure Java Data Structures & Algorithms                 ");
        System.out.println("========================================================================");
        System.out.println("Graph loaded with " + graph.vertexCount() + " sample papers and " + graph.edgeCount() + " citation edges.");
    }

    private void printMainMenu() {
        System.out.println("\n----------------------------- MAIN MENU --------------------------------");
        System.out.println("  1. Add a paper");
        System.out.println("  2. Add a citation");
        System.out.println("  3. Search a paper (Exact / Fuzzy)");
        System.out.println("  4. Explore citation network reachability (Level-wise / Deep Lineage)");
        System.out.println("  5. Analyze citation flow (Edmonds-Karp Max-Flow)");
        System.out.println("  6. View reports (Top papers, Top authors, Trends)");
        System.out.println("  7. Save current data to CSV");
        System.out.println("  8. Load data from CSV");
        System.out.println("  9. Exit");
        System.out.println("------------------------------------------------------------------------");
    }

    // -------------------------------------------------------------------------
    // Option 1: Add a Paper
    // -------------------------------------------------------------------------
    private void handleAddPaper() {
        System.out.println("\n--- Add a New Paper ---");
        String id;
        while (true) {
            id = readLine("Enter paper ID (e.g. P106): ");
            if (id == null) return;
            id = id.trim();
            if (id.isEmpty()) {
                System.out.println("[Error] Paper ID cannot be empty. Please try again.");
                continue;
            }
            if (graph.findIndexById(id) != -1) {
                System.out.println("[Error] A paper with ID '" + id + "' already exists. Please choose a unique ID.");
                continue;
            }
            break;
        }

        String title;
        while (true) {
            title = readLine("Enter paper title: ");
            if (title == null) return;
            title = title.trim();
            if (title.isEmpty()) {
                System.out.println("[Error] Paper title cannot be empty. Please try again.");
                continue;
            }
            break;
        }

        String author;
        while (true) {
            author = readLine("Enter author name: ");
            if (author == null) return;
            author = author.trim();
            if (author.isEmpty()) {
                System.out.println("[Error] Author name cannot be empty. Please try again.");
                continue;
            }
            break;
        }

        int year;
        while (true) {
            String yearStr = readLine("Enter publication year (e.g. 2023): ");
            if (yearStr == null) return;
            yearStr = yearStr.trim();
            try {
                year = Integer.parseInt(yearStr);
                if (year < 1500 || year > 2100) {
                    System.out.println("[Error] Please enter a valid publication year between 1500 and 2100.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("[Error] Malformed year '" + yearStr + "'. Please enter a numeric year.");
            }
        }

        Paper paper = new Paper(id, title, author, year);
        int idx = graph.addVertex(paper);
        unsavedChanges = true;
        System.out.println("[Success] Paper '" + title + "' added successfully at vertex index " + idx + ".");
        System.out.println("Total papers in graph: " + graph.vertexCount());
    }

    // -------------------------------------------------------------------------
    // Option 2: Add a Citation
    // -------------------------------------------------------------------------
    private void handleAddCitation() {
        System.out.println("\n--- Add a Citation (Directed Edge: Citing -> Cited) ---");
        if (graph.vertexCount() < 2) {
            System.out.println("[Notice] At least 2 papers must exist to create a citation edge.");
            return;
        }

        String citingId;
        int fromIdx;
        while (true) {
            citingId = readLine("Enter CITING paper ID (source of citation): ");
            if (citingId == null) return;
            citingId = citingId.trim();
            fromIdx = graph.findIndexById(citingId);
            if (fromIdx == -1) {
                System.out.println("[Error] Paper ID '" + citingId + "' not found. Available papers:");
                printAvailablePaperIds();
                continue;
            }
            break;
        }

        String citedId;
        int toIdx;
        while (true) {
            citedId = readLine("Enter CITED paper ID (target of citation): ");
            if (citedId == null) return;
            citedId = citedId.trim();
            if (citedId.equals(citingId)) {
                System.out.println("[Warning] Self-citations are permitted but generally unusual. Continuing...");
            }
            toIdx = graph.findIndexById(citedId);
            if (toIdx == -1) {
                System.out.println("[Error] Paper ID '" + citedId + "' not found. Available papers:");
                printAvailablePaperIds();
                continue;
            }
            break;
        }

        // Check if edge already exists
        if (graph.getNeighbors(fromIdx).contains(toIdx)) {
            System.out.println("[Notice] Citation from " + citingId + " to " + citedId + " already exists.");
            return;
        }

        graph.addCitation(citingId, citedId);
        CsvHandler.syncCitationCounts(graph);
        unsavedChanges = true;
        System.out.println("[Success] Citation recorded: [" + citingId + "] cites [" + citedId + "].");
        System.out.println("Total citations in graph: " + graph.edgeCount());
    }

    private void printAvailablePaperIds() {
        StringBuilder sb = new StringBuilder("  ");
        int count = graph.vertexCount();
        for (int i = 0; i < count; i++) {
            sb.append(graph.getPaper(i).getId());
            if (i < count - 1) {
                sb.append(", ");
            }
        }
        System.out.println(sb.toString());
    }

    // -------------------------------------------------------------------------
    // Option 3: Search a Paper
    // -------------------------------------------------------------------------
    private void handleSearchPaper() {
        System.out.println("\n--- Search Papers ---");
        if (graph.vertexCount() == 0) {
            System.out.println("[Notice] Graph is empty. No papers to search.");
            return;
        }

        String query = readLine("Enter search query: ");
        if (query == null) return;
        query = query.trim();
        if (query.isEmpty()) {
            System.out.println("[Error] Search query cannot be empty.");
            return;
        }

        String mode;
        while (true) {
            mode = readLine("Search mode - [E]xact substring (KMP) or [F]uzzy typo-tolerant (Wagner-Fischer)? [E/F]: ");
            if (mode == null) return;
            mode = mode.trim().toUpperCase();
            if (mode.equals("E") || mode.equals("EXACT") || mode.equals("F") || mode.equals("FUZZY")) {
                break;
            }
            System.out.println("[Error] Invalid choice. Please enter 'E' for exact or 'F' for fuzzy.");
        }

        boolean isFuzzy = mode.startsWith("F");
        DynamicArray<Integer> matchedIndices = new DynamicArray<>();

        if (!isFuzzy) {
            // Exact substring search via KMPMatcher across title, author, and ID
            String lowerQuery = query.toLowerCase();
            for (int i = 0; i < graph.vertexCount(); i++) {
                Paper p = graph.getPaper(i);
                boolean matchTitle = KMPMatcher.contains(p.getTitle().toLowerCase(), lowerQuery);
                boolean matchAuthor = KMPMatcher.contains(p.getAuthor().toLowerCase(), lowerQuery);
                boolean matchId = KMPMatcher.contains(p.getId().toLowerCase(), lowerQuery);

                if (matchTitle || matchAuthor || matchId) {
                    matchedIndices.add(i);
                }
            }
        } else {
            // Fuzzy search via Wagner-Fischer edit distance against paper titles and title words
            int maxDist = 2;
            if (query.length() >= 8) {
                maxDist = 3;
            }

            String lowerQuery = query.toLowerCase();
            for (int i = 0; i < graph.vertexCount(); i++) {
                Paper p = graph.getPaper(i);
                String title = p.getTitle();
                boolean matched = false;

                if (FuzzyMatcher.editDistance(lowerQuery, title.toLowerCase()) <= maxDist) {
                    matched = true;
                } else {
                    // Check against words within title
                    String[] words = title.split("[\\s,.:;!?()\\[\\]\\-]+");
                    for (String w : words) {
                        if (!w.isEmpty() && Math.abs(w.length() - lowerQuery.length()) <= maxDist) {
                            if (FuzzyMatcher.editDistance(lowerQuery, w.toLowerCase()) <= maxDist) {
                                matched = true;
                                break;
                            }
                        }
                    }
                }

                if (matched) {
                    matchedIndices.add(i);
                }
            }
        }

        if (matchedIndices.isEmpty()) {
            System.out.println("[Result] No papers matched your search query '" + query + "'.");
        } else {
            System.out.println("\n[Result] Found " + matchedIndices.size() + " matching paper(s):");
            System.out.printf("%-10s | %-35s | %-22s | %-6s | %-10s%n", "ID", "Title", "Author", "Year", "Citations");
            System.out.println("---------------------------------------------------------------------------------------------");
            for (int k = 0; k < matchedIndices.size(); k++) {
                Paper p = graph.getPaper(matchedIndices.get(k));
                String title = p.getTitle();
                if (title.length() > 33) title = title.substring(0, 30) + "...";
                String author = p.getAuthor();
                if (author.length() > 20) author = author.substring(0, 17) + "...";

                System.out.printf("%-10s | %-35s | %-22s | %-6d | %-10d%n",
                        p.getId(), title, author, p.getYear(), p.getCitationCount());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Option 4: Traverse Citation Graph
    // -------------------------------------------------------------------------
    private void handleTraverseGraph() {
        System.out.println("\n--- Traverse Citation Graph ---");
        if (graph.vertexCount() == 0) {
            System.out.println("[Notice] Graph is empty.");
            return;
        }

        String startId;
        int startIdx;
        while (true) {
            startId = readLine("Enter start paper ID for traversal: ");
            if (startId == null) return;
            startId = startId.trim();
            startIdx = graph.findIndexById(startId);
            if (startIdx == -1) {
                System.out.println("[Error] Paper ID '" + startId + "' not found. Available IDs:");
                printAvailablePaperIds();
                continue;
            }
            break;
        }

        String traversalType;
        while (true) {
            traversalType = readLine("Choose exploration mode - [L]evel-wise Spread or [D]eep Lineage Tracing? [L/D]: ");
            if (traversalType == null) return;
            traversalType = traversalType.trim().toUpperCase();
            if (traversalType.equals("L") || traversalType.equals("LEVEL") || traversalType.equals("B") || traversalType.equals("BFS")
                    || traversalType.equals("D") || traversalType.equals("DFS") || traversalType.equals("DEEP")) {
                break;
            }
            System.out.println("[Error] Invalid choice. Please enter 'L' for Level-wise spread or 'D' for Deep lineage.");
        }

        boolean isLevelWise = traversalType.startsWith("L") || traversalType.startsWith("B");
        DynamicArray<Integer> visitOrder = isLevelWise
                ? GraphTraversal.bfs(graph, startIdx)
                : GraphTraversal.dfs(graph, startIdx);

        System.out.println("\n" + (isLevelWise ? "Level-Wise Reachability Spread" : "Deep Lineage Tracing")
                + " Order starting from [" + startId + "]:");
        System.out.println("Total reachable papers in component: " + visitOrder.size());
        System.out.println("------------------------------------------------------------------------");

        for (int i = 0; i < visitOrder.size(); i++) {
            Paper p = graph.getPaper(visitOrder.get(i));
            System.out.printf("%2d. [%s] \"%s\" by %s (%d)%n",
                    (i + 1), p.getId(), p.getTitle(), p.getAuthor(), p.getYear());
        }
    }

    // -------------------------------------------------------------------------
    // Option 5: Analyze Citation Flow (Edmonds-Karp Max-Flow)
    // -------------------------------------------------------------------------
    private void handleAnalyzeFlow() {
        System.out.println("\n--- Analyze Citation Flow (Edmonds-Karp Max-Flow) ---");
        if (graph.vertexCount() < 2) {
            System.out.println("[Notice] Need at least 2 papers in the graph to analyze flow.");
            return;
        }

        String sourceId;
        int sourceIdx;
        while (true) {
            sourceId = readLine("Enter SOURCE paper ID: ");
            if (sourceId == null) return;
            sourceId = sourceId.trim();
            sourceIdx = graph.findIndexById(sourceId);
            if (sourceIdx == -1) {
                System.out.println("[Error] Paper ID '" + sourceId + "' not found. Available IDs:");
                printAvailablePaperIds();
                continue;
            }
            break;
        }

        String sinkId;
        int sinkIdx;
        while (true) {
            sinkId = readLine("Enter SINK (target) paper ID: ");
            if (sinkId == null) return;
            sinkId = sinkId.trim();
            sinkIdx = graph.findIndexById(sinkId);
            if (sinkIdx == -1) {
                System.out.println("[Error] Paper ID '" + sinkId + "' not found. Available IDs:");
                printAvailablePaperIds();
                continue;
            }
            break;
        }

        if (sourceIdx == sinkIdx) {
            System.out.println("[Notice] Source and sink are the same paper (" + sourceId + "). Max-flow is 0.");
            return;
        }

        int maxFlow = EdmondsKarp.maxFlow(graph, sourceIdx, sinkIdx);
        String explanation = EdmondsKarp.describeFlow(maxFlow);

        Paper sourcePaper = graph.getPaper(sourceIdx);
        Paper sinkPaper = graph.getPaper(sinkIdx);

        System.out.println("\n====================== FLOW ANALYSIS RESULT ======================");
        System.out.println("Source : [" + sourcePaper.getId() + "] " + sourcePaper.getTitle());
        System.out.println("Sink   : [" + sinkPaper.getId() + "] " + sinkPaper.getTitle());
        System.out.println("Max-Flow Value : " + maxFlow);
        System.out.println("Interpretation : " + explanation);
        System.out.println("==================================================================");
    }

    // -------------------------------------------------------------------------
    // Option 6: View Reports Submenu
    // -------------------------------------------------------------------------
    private void handleReportsSubmenu() {
        boolean inSubmenu = true;
        while (inSubmenu) {
            System.out.println("\n------------------------- REPORTS SUBMENU ------------------------------");
            System.out.println("  1. Top N Most-Cited Papers (Stable Ranked)");
            System.out.println("  2. Top Authors by Total Citations");
            System.out.println("  3. Yearly Citation Trends");
            System.out.println("  4. Return to Main Menu");
            System.out.println("------------------------------------------------------------------------");

            String choice = readLine("Select report (1-4): ");
            if (choice == null) return;
            choice = choice.trim();

            switch (choice) {
                case "1":
                    int nPapers = readInt("Enter number of papers to rank (default 5): ", 5);
                    System.out.println("\n=== TOP " + nPapers + " MOST-CITED PAPERS ===");
                    System.out.println(ReportGenerator.formatTopPapers(graph, nPapers));
                    break;
                case "2":
                    int nAuthors = readInt("Enter number of top authors to rank (default 5): ", 5);
                    System.out.println("\n=== TOP " + nAuthors + " AUTHORS BY CITATIONS ===");
                    System.out.println(ReportGenerator.formatTopAuthors(graph, nAuthors));
                    break;
                case "3":
                    System.out.println("\n=== YEARLY CITATION TRENDS ===");
                    System.out.println(ReportGenerator.formatCitationTrends(graph));
                    break;
                case "4":
                    inSubmenu = false;
                    break;
                default:
                    System.out.println("[Error] Invalid choice. Please enter a number between 1 and 4.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Option 7: Save Current Data to CSV
    // -------------------------------------------------------------------------
    private void handleSaveCsv() {
        System.out.println("\n--- Save Citation Data to CSV ---");
        String filename = readLine("Enter filename to save [default: " + DEFAULT_CSV_FILE + "]: ");
        if (filename == null) return;
        filename = filename.trim();
        if (filename.isEmpty()) {
            filename = DEFAULT_CSV_FILE;
        }

        try {
            CsvHandler.save(graph, filename);
            unsavedChanges = false;
            System.out.println("[Success] Graph state saved successfully to '" + filename + "'.");
            System.out.println("Saved " + graph.vertexCount() + " papers and " + graph.edgeCount() + " citations.");
        } catch (IOException e) {
            System.out.println("[Error] Failed to save CSV file: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Option 8: Load Data from CSV
    // -------------------------------------------------------------------------
    private void handleLoadCsv() {
        System.out.println("\n--- Load Citation Data from CSV ---");
        String filename = readLine("Enter filename to load [default: " + DEFAULT_CSV_FILE + "]: ");
        if (filename == null) return;
        filename = filename.trim();
        if (filename.isEmpty()) {
            filename = DEFAULT_CSV_FILE;
        }

        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("[Error] File '" + filename + "' does not exist.");
            return;
        }

        String mode = readLine("Do you want to [M]erge with current data or [R]eplace it? [M/R, default M]: ");
        if (mode == null) return;
        mode = mode.trim().toUpperCase();

        try {
            if (mode.startsWith("R")) {
                // To replace: create a new graph and load into it
                Graph newGraph = CsvHandler.load(filename);
                // Clear and rebuild
                System.out.println("[Info] Replacing existing data with file contents.");
                // Reload by creating new instance or copying into current
                // Since Graph doesn't have clear(), we load into a fresh graph and point to it
                // To keep internal graph reference clean:
                this.graph = newGraph;
            } else {
                // Merge into current graph
                CsvHandler.loadInto(graph, filename);
            }
            unsavedChanges = false;
            System.out.println("[Success] Loaded data from '" + filename + "'.");
            System.out.println("Graph now contains " + graph.vertexCount() + " papers and " + graph.edgeCount() + " citations.");
        } catch (IOException e) {
            System.out.println("[Error] Failed to load CSV file: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Option 9: Exit
    // -------------------------------------------------------------------------
    private boolean handleExit() {
        if (unsavedChanges) {
            String ans = readLine("You have unsaved changes. Do you want to save before exiting? [y/N]: ");
            if (ans != null && (ans.trim().equalsIgnoreCase("y") || ans.trim().equalsIgnoreCase("yes"))) {
                handleSaveCsv();
            }
        }
        System.out.println("\nThank you for using the Citation Analysis System. Goodbye!");
        return false; // Stop main loop
    }

    // -------------------------------------------------------------------------
    // Input Helpers
    // -------------------------------------------------------------------------
    private String readLine(String prompt) {
        System.out.print(prompt);
        try {
            return reader.readLine();
        } catch (IOException e) {
            System.out.println("[Error reading input: " + e.getMessage() + "]");
            return null;
        }
    }

    private int readInt(String prompt, int defaultValue) {
        String s = readLine(prompt);
        if (s == null) return defaultValue;
        s = s.trim();
        if (s.isEmpty()) return defaultValue;
        try {
            int val = Integer.parseInt(s);
            return val > 0 ? val : defaultValue;
        } catch (NumberFormatException e) {
            System.out.println("[Notice] Invalid number. Using default value: " + defaultValue);
            return defaultValue;
        }
    }
}
