# fm2se-java-smt Demo Project

This is a demo of how to use java-smt based on the template and tutorial from https://github.com/sosy-lab/java-smt/blob/master/doc/Getting-started.md. We use a pure Java-based SMT solver. For more SMT solvers, e.g., Z3 binaries, please refer to the above tutorial.

We demonstrate the use of JavaSMT by solving two tasks from the lecture and exercises of the Formal Methods for Software Engineering module.

First, we show how to encode and solve a simple emoji math puzzle in class [EmojiSolver](src/main/java/de/buw/fm4se/java_smt/EmojiSolver.java). This example only uses integer arithmetic.

Second, we show how to encode a PC configuration problem in class [PCConfigSolver](src/main/java/de/buw/fm4se/java_smt/PCConfigSolver.java). This example combines propositional logic and simple integer arithmetic.

A video is available from .
