# GROUP: comp2021-3d


NAME | NR | GRADE | CONTRIBUTION
---| --- | --- | ---
Daniel Garcia Lima Sarmento da Silva | 201806524 | XX | 33%
João Diogo Martins Romão | 201806779 | XX | 33%
Rafael Valente Cristino | 201806680 | XX | 33%

 ---

## GLOBAL Grade of the project: <0 to 20>


---
## Summary

This project's aim was to build a compiler that generates valid Java Virtual Machine instructions in *jasmin* format, to be translated to Java Bytecodes by the *jasmin* assembler.

The compiler takes **.jmm** files as input, and performs syntatical and semantic analysis, which are followed by the generation of OLLIR (OO-based Low-Level Intermediate Representation) code. This OLLIR Code is then optimized, and the *jasmin* correspondent code is generated.

## Syntatical Errors

The compiler has error recovery implemented for syntatical *while* loop errors. These errors are stored in a list of reports, shown at the end of parsing.

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


**CODE GENERATION: (describe how the code generation of your tool works and identify the possible problems your tool has regarding code generation.)

## Code Generation

The code is generated to the folder where the program is run. It also generates a folder for each class inside the *compiled* folder. This folder includes the *.j* file, the *.ollir* file, the *.json* file, which contains the AST (Abstract Syntax Tree) and the *.log* file, which contains the compiler execution output, including all the compiling stages.

This stages are:



## Task Distribution

- Daniel Garcia Silva - Parser, Semantic Analysis, Code Generation
- João Romão - Parser, Semantic Analysis, Code Generation
- Rafael Cristino - Parser, Semantic Analysis, Code Generation

**PROS: (Identify the most positive aspects of your tool)

## Pros
 
- Allowing method overloading in semantic analysis.
- Detecting use of uninitialized variables.
- Use of most efficient instructions such as iinc, (...)
- Implemented code optimizations:
  - Constant propagation
  - Use of while templates to elimnate unnecessary got instructions
  - Removing initialized and unused variables.
  - Minimization of the number of registers used. 

**CONS: (Identify the most negative aspects of your tool)

## Cons

- Due to time constraints we could not implement constant folding, that we proposed to do as an extra optimization.