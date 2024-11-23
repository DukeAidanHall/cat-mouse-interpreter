# cat-mouse-interpreter
An interpreter for a theoretical programming language called CATGETMOUSE for simulating cat/mouse games.

## This project went through four stages:
Stage 1: Developing a scanner to identify different tokens within the program. Significant tokens would be entered into the symbol table for later use. <br /><br />
Stage 2: Implementating an SLR(1)-parsing method across multiple stacks managed by shift and reduce operations from a deterministic finite automaton (DFA) created in JFLAP.<br /><br />
Stage 3: Designing a method to recursively produce a singular tree in parallel to the parser using complex polymorphism for a range of node classes implemented in NodeFile.java. <br /><br />
Stage 4: Creating a graphical interpretation of the user's input. Catching null reference, out-of-bounds, and collision exceptions before runtime and displaying them to the user.<br />

## Grid Symbols Key
C --> Current location of the cat<br />
c --> Path of the cat<br />
M --> Current location of the mouse<br />
m --> Path of the mouse<br />
H --> Location of a hole<br />

## Included Tests
ptest1.txt --> Demonstrates the ability for the interpreter to hide a mouse within a hole and display a cat over a hole simultaneously (along with other basic movement).<br /><br />
p2test2.txt --> Demonstrates a "winning state" of the game, where a mouse is eaten by a cat.<br /><br />
ptesterrors.txt --> Provides all the possible different types of errors that could occur during the interpretation of a program.<br /><br />

### For more information, refer to comments within Interpreter.java.
