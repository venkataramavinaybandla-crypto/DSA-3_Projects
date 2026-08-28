<div align="center">

# 📚 Citation Analysis System

### A Data Structures–Driven Approach to Tracking Academic Citations

*Modeling the scholarly world as a graph — one directed edge at a time.*

[![Java](https://img.shields.io/badge/Java-JDK%2017-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Build](https://img.shields.io/badge/Build-Passing-brightgreen?style=for-the-badge)](.)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)
[![DSA](https://img.shields.io/badge/Powered%20By-Pure%20DSA-purple?style=for-the-badge)](.)

</div>

---

## 🧠 Overview

Research literature grows through an ever-expanding web of citations — yet tracking **who cites whom**, spotting **influential work**, and navigating this network is still largely manual and unsystematic.

**Citation Analysis System** fixes that by modeling scholarly literature as a **directed citation graph**:

- 🔵 Every **research paper** → a **vertex**
- ➡️ Every **citation** → a **directed edge**

Once the graph exists, the system unleashes core graph algorithms to explore it — traversal, ranking, flow analysis, fast search — turning a tangled mess of academic cross-referencing into a structured, queryable network.

> No frameworks doing the heavy lifting. No `java.util` shortcuts. Every graph, hash table, and search algorithm here is **hand-built from scratch** — because that's the whole point.

---

## ❗ The Problem

Researchers currently lack a systematic, data-structure-driven way to:

- Track citation relationships across papers
- Identify influential / high-impact work
- Analyze citation flow between authors or research clusters

Most of this happens **manually or across siloed databases** — which slows down research discovery, causes redundant work, and lets genuinely seminal papers quietly go unnoticed.

---

## ⚙️ How It Works

| Capability | Algorithm(s) Used |
|---|---|
| 🔗 Add papers & record citation edges | Custom **adjacency-list graph** |
| 🔍 Traverse citation relationships | **BFS** & **DFS** |
| ⚡ Search papers by title/author | Custom **hash tables** (open addressing) — O(1) lookup |
| ✏️ Fuzzy / typo-tolerant search | **Wagner–Fischer edit distance** |
| 🧵 Exact string/pattern search | **KMP** & **Rabin–Karp** |
| 🌊 Citation-flow analysis between authors/clusters | **Dinic's / Edmonds–Karp max-flow** |
| 📊 Rank papers by citation count | Custom **sorting routines** |
| 📈 Generate trend & influence reports | Graph + hash-table aggregation |

The core insight driving the flow-analysis piece: a citation network is fundamentally a **flow of scholarly influence** — from source papers outward to the works that build on them. Strongly cited chains are treated as **high-capacity paths**, and max-flow techniques surface how influence actually moves through the network.

---

## ✨ Features

- ➕ **Add papers** and record directed citation relationships
- 🔎 **Search** via hashing, with edit-distance–based fuzzy matching for typos
- 📉 **Sort & rank** papers by citation count
- 🕸️ **Traverse** the citation graph (BFS/DFS) to reveal direct & indirect relationships
- 🌊 **Analyze citation flow** between authors or research clusters via max-flow
- 📑 **Generate reports** — citation trends, top authors, most-cited papers

---

## 🛠️ Tech Stack

<div align="center">

| Category | Tools |
|---|---|
| **Language** | Java (JDK 17) — hand-built graph, hashing, string & flow algorithms |
| **Data Structures** | Custom adjacency-list graph · Custom hash tables |
| **Algorithms** | BFS/DFS · KMP · Rabin–Karp · Wagner–Fischer · Dinic's / Edmonds–Karp |
| **Data Storage** | CSV / local file-based persistence |
| **Dev Environment** | IntelliJ IDEA / VS Code |
| **Version Control** | Git & GitHub |
| **Testing** | JUnit |
| **UI (optional)** | Console / JavaFX report views |

</div>

> ⚠️ **No `java.util` collection classes** are used for core logic — every graph, hash table, and algorithm is implemented from scratch, consistent with the course's built-from-first-principles constraint.

---

## 🚀 Getting Started

### Prerequisites
- JDK 17+
- IntelliJ IDEA or VS Code (with Java extensions)
- Git

### Installation

```bash
# Clone the repository
git clone https://github.com/<your-username>/citation-analysis-system.git
cd citation-analysis-system

# Build the project
javac -d out src/**/*.java

# Run
java -cp out Main
```

### Quick Usage

```
1. Add a paper       →  Register title, author(s), year
2. Add a citation     →  Link Paper A → Paper B (directed edge)
3. Search a paper      →  By exact title/author, or fuzzy match
4. Traverse           →  Run BFS/DFS from any paper
5. Analyze flow        →  Run max-flow between author clusters
6. Generate report      →  View top authors, popular papers, trends
```

---

## 🎯 Expected Outcome

- ✅ A functioning citation graph supporting full **BFS/DFS traversal**
- ✅ **Ranked citation counts** and automatic identification of top authors
- ✅ **Citation-trend reports** generated straight from graph + hash-table data
- ✅ A demonstrable **end-to-end search-and-analysis flow**, start to finish

This project proves that core DSA concepts — **graphs, hashing, and flow** — aren't just theory. They solve a real, practical academic research problem.

---

## 👥 Team

<div align="center">

| Name | Roll Number |
|---|---|
| **Bandla Vinay** | 2520030437 |
| **Sai Sashank** | 2520030454 |
| **Ganesh** | 2520030252 |

**Section 07 · Team 20 · DSA-3 (25CS2103E)**
**Guide:** Dr. S. Madhavi

</div>

---

## 📄 License

This project is built for academic purposes under **DSA-3 (25CS2103E)**. Licensed under [MIT](LICENSE) unless your course says otherwise — check with your guide before going full open-source rebel.

---

<div align="center">

**Built with directed edges, hand-rolled hash tables, and zero `java.util` shortcuts.**

</div>
