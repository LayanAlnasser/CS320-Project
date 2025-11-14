# **Enhanced Lexical and Syntax Analyzer — CS320 Project**

This repository contains the implementation for the **CS320 Programming Languages** project: an enhanced **lexical analyzer** and **recursive-descent syntax parser**, inspired by Robert W. Sebesta’s compiler front-end examples.

The project is divided into two major components:

1. **Lexical Analysis (Scanner)**
2. **Syntax Analysis (Parser)**

---

## Project Description

### Part 1 — Lexical Analysis

The lexical analyzer processes source code and identifies **tokens**, such as:

- Identifiers  
- Integer literals  
- Assignment operators (`=`)  
- Arithmetic operators (`+`, `-`, `*`, `/`)  
- Keywords (`if`, `then`, `else`)  

#### Features

- Detects illegal identifiers  
  - Examples: `2sum`, `total#4`
- Detects invalid integer literals  
  - Example: `23b2`
- Builds a symbol table with token types and line numbers
- Provides descriptive lexical error messages
- **Optional Enhancements**
  - Floating-point literals  
  - Logical operators (`&&`, `||`)  
  - Output token stream to a `.lexout` file  

---

### Part 2 — Syntax Analysis

Implements a **recursive-descent parser** using the following EBNF grammar:



#### Features

- Implements `expr()`, `term()`, and `factor()` functions
- Detects:
  - Missing operators  
  - Missing or mismatched parentheses  
- Integrates with the lexical analyzer  
- Produces clear syntax error messages
- **Optional Enhancements**
  - Panic-mode error recovery  
  - Parse tree visualization  

---

## 🧪 Example Input




---

## 📸 Deliverables

- Source code files  
- Input and output examples  
- Screenshots of successful execution  
- 4–5 page project report  

### For Distinction / Bonus

- Semantic analysis (types, declarations)
- Extended grammar (conditionals)
- Mini-interpreter to evaluate expressions
- Parse tree visualization (Graphviz or ASCII)
- JSON-based logging

---

## Authors

**CS320 Group Project — Fall 2025**  
Layan Alnasser
