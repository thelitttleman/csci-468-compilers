# CSCI 468: Compilers Syllabus

_4 Credits. (3 Lec, 1 Lab) S_

COREQUISITE: CSCI 338 and CSCI 305. Senior capstone course. Compiler design and construction. Scanning, parsing, symbol 
tables, semantic analysis, intermediate representations, run-time memory management, target code generation, and 
optimization. Implementation of a small compiler.

CSCI 366 is recommended.

## Overview

CS468 is designed to give you a complete understanding of how to build a compiler. We will be
learning:

*  How to create a tokenizer by hand that produces a list of tokens to be consumed by a parser, and
how this maps to both regular expressions and finite state machines
* How to understand EBNF grammars, and how to create a recursive descent parser by hand
based on them
* The difference between Statements and Expressions, and the techniques for parsing them both
* Writing a Tree-Walk interpreter for our parse tree
* Semantic analysis and Type Systems
* Error Handling
* How JVM Byte-code works and how to generate it from our parse tree
* 
This class is very code heavy, and each section of the project builds on the last one. We will not, for
example, give you a tokenizer, it is up to you to produce one that works well enough for later parts of
the project.

So, I implore you, keep up with the class. This is not a class you can wait until the last month of and
knock out with a few over-nighters!

We will be working in Java, so you should be very comfortable with it. We will do a brief review as
well as an overview of the debugger at the start, but not much beyond that.

## Logistics

### Lectures

Lectures are at 3:10PM, MWF in Romney 8.  Lab is Friday, 5:10-6PM in 254 Barnard.  It is not required and is simply
an opportunity to work with the TA.

Rather than a midterm or final, the class will have quizzes every other Friday, beginning the third
week. The quizzes will be available online via D2L, and we will not have in-person classes on those days.

All lectures will be live-streamed via YouTube.

There is an active Discord for the server, a link to join can be found on the content tab in D2L.

### Office Hours

Office hours are MWF, 4:10-5PM in my office, 364 Barnard, or in the 347 Barnard conference room. 

### Course Grading

Course grading will be broken down as follows:

* Project - 60%
* Quizzes - 40%

The project is a major focus of the class, and will be done individually and graded mainly via
automated tests:

* 95% - The automated test suite
* 5% - Code quality, determined by manual inspection

### Book

The book for this course is “Crafting Interpreters”, and it is available in hard copy here:
https://craftinginterpreters.com/ or online, for free, here: https://craftinginterpreters.com/contents.html

We will be focusing on Sections 1 and 2 of the book.

For the JVM bytecode section, we will rely on notes and tools to learn the necessary material.

## 468 Spring Class Schedule

TODO: link to schedule
