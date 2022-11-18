# Automated PC configuration using an SMT solver

For this assignment you are asked to build a PC configuration SMT problem and extract a solution from the model using JavaSMT.

- Components, their categories, and their prices can be read from files, i.e., they may change. 
- Constraints between components of kind `requires` and `excludes` (similar to those in feature models) can be read from another file.
- Every valid PC needs at least component from each of these categories: `CPU`, `motherboard`, `RAM`, and `storage`
- Users provide a budget on the console.
- The selected components of a configuration are listed by the program, if one exists.

A video is available from [https://youtu.be/9ptEo4apVcU](https://youtu.be/9ptEo4apVcU).
