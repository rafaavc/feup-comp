# GROUP: comp2021-3d


NAME | NR | GRADE | CONTRIBUTION
---| --- | --- | ---
Daniel Garcia Lima Sarmento da Silva | 201806524 | 19 | 33%
João Diogo Martins Romão | 201806779 | 19 | 33%
Rafael Valente Cristino | 201806680 | 19 | 33%

---

## Global Grade of the project

19

---
## Summary

This project's aim was to build a compiler that generates valid Java Virtual Machine instructions in *jasmin* format, to be translated to Java Bytecodes by the *jasmin* assembler.

The compiler takes **.jmm** files as input, and performs syntactical and semantic analysis, which are followed by the generation of OLLIR (OO-based Low-Level Intermediate Representation) code. This OLLIR Code is then optimized, and the *jasmin* correspondent code is generated.

### Command line arguments
---

```
./comp2021-3d Main <jmmfilepath> [-e=<n1>] [-r=<n2>] [-o] [-d]
```

Where:
  - -e shows n1 reports (n1 >= 0 and default = 15)
  - -r assigns the variables to n2 registers (n2 > 0)
  - -o activates constant propagation and whiles goto optimizations
  - -d saves the output files to the `./compiled/<ClassName>/` folder instead of the directory where the compiler is executed

or

```
./comp2021-3d Main run <jasminfilepath> [stdin]
```

Where:
  - jasminfilepath is the path of the jasmin code file to run
  - stdin is the input to be given to the program

## Syntactical Errors

The compiler has error recovery implemented for syntactical *while* loop errors. These errors are stored in a list of reports, shown at the end of parsing.

This way, a more helpful report is provided, since the compilation does not stop at the first *while* loop error. This happens by ignoring everything until the first Open braces (*{*) token. 


## Semantic Analysis

The compiler enforces the following semantic rules:

- operands are of the same type;
- no operations between arrays;
- array access is only performed on actual arrays;
- array access index is actual integer;
- assignee type is the same as assigned's;
- boolean operations only have boolean operands;
- while and if conditions result in boolean type;
- upon method call, object exists and contains method (for imported class methods, it assumes the method exists and the return type is the expected);
- upon method call, if the method is not declared, the compiler assumes it belongs to the super class, if the class is extended;
- upon method call, number and type of parameters is the same as the number and type of parameters declared

## Code Generation

The code is generated to the folder where the program is run. It also generates a folder for each class inside the *compiled* folder. This folder includes the *.j* file, the *.ollir* file, the *.json* file, which contains the AST (Abstract Syntax Tree) and the *.log* file, which contains the compiler execution output, including all the compiling stages.

These stages are:

1. Jmm code parsing and AST generation
2. Semantic Analysis and Symbol Table creation
3. Ollir code generation by traversing the abstract syntax tree
4. Optimization
    - Liveness Analysis and Registry Allocation (-r=k)
    - Constant Propagation (-o)
    - While loops *goto* instructions are minimized (-o)
    - Initialized but unused variables have their declaration and assignment instructions removed
5. Jasmin code generation (with selection of the most efficient instructions)

## Task Distribution

- Daniel Garcia Silva - Parser, Semantic Analysis, Code Generation
- João Romão - Parser, Semantic Analysis, Code Generation
- Rafael Cristino - Parser, Semantic Analysis, Code Generation


## Pros
 
- Allowing method overloading in semantic analysis.
- Detecting use of uninitialized variables.
- Use of most efficient instructions such as iinc, sipush, etc.
- Implemented code optimizations:
  - Constant propagation
  - Use of while templates to eliminate unnecessary goto instructions
  - Removing initialized and unused variables
  - Registry Allocation optimized with Liveness Analysis and Graph Coloring algorithms

## Cons

- Due to time constraints we could not implement constant folding, that we had proposed to do as an extra optimization.