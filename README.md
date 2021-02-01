# parallel_gaussian_elimination
Gaussian Elimination using MPJ Express (MPI library for Java)




---------------------------------------------------------------------------------------------------------------------------
To run the code:
javac project1.java
java project1 <int matrixSize> <NumOfThreads>

---------------------------------------------------------------------------------------------------------------------------

The data matrix (A) and vector (b) in (Ax=b) is generated randomly.
The output times are recorded in a text file named "results.txt"


The commented code present in the main method was used to test the data on:
Matrix size ---> a sequential and a parallel program
16 --> single + 2, single + 4, ... single + 128 Threads
64 --> ...
...
2048
4096

< Report + Diagrams + Plots + Results >are all present in the report.pdf file
