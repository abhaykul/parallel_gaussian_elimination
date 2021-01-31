package project1;
/**
 * Abhay Vivek Kulkarni
 * ak6277@rit.edu
 * <p>
 * CSCI - 654
 * Parallel Computing - Project 1
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class project1 {
    static PrintWriter writer;
    double[][] matrixA;
    double[] vectorB;
    double[] vectorX;
    int size;

    static {
        try {
            // Prints results in a file
            writer = new PrintWriter(new File("results.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws FileNotFoundException {
        int matrixSize = Integer.parseInt(args[0]);
        int threads = Integer.parseInt(args[1]);
        // ***********************************************************************************************
        /*
        int[] s = new int[]{16, 64, 256, 1024, 2048, 4096, 8192};
        int[] p = new int[]{2, 4, 8, 12, 16, 24, 32, 40, 64, 128};
        for (int i = 0; i < s.length; i++) {
            for (int j = 0; j < p.length; j++) {
                System.out.println(s[i] + ":" + p[j]);
                new project1().run(s[i], p[j]);
                writer.flush();
            }
        }
        */
        // ***********************************************************************************************
        new project1().run(matrixSize, threads);
        writer.close();
    }

    void run(int sizeOfMatrix, int numOfThreads) {
        Random r = new Random();
        matrixA = new double[sizeOfMatrix][sizeOfMatrix];
        vectorB = new double[sizeOfMatrix];
        vectorX = new double[sizeOfMatrix];
        size = sizeOfMatrix;
        // ***********************************************************************************************
        // Randomly generate matrix
        for (int i = 0; i < sizeOfMatrix; i++) {
            for (int j = 0; j < sizeOfMatrix; j++) {
                matrixA[i][j] = r.nextDouble();
            }
            vectorB[i] = r.nextDouble();
        }
        // ***********************************************************************************************
        // Writes the times to a file
        writer.print(size + "\t");
        writer.print("\t(1)");
        gaussianElimination(1);
        writer.print("\t(" + numOfThreads + ")");
        gaussianElimination(numOfThreads);
        writer.println();
        // ***********************************************************************************************
    }

    public void gaussianElimination(int threads) {
        long start = System.currentTimeMillis();
        // ***********************************************************************************************
        ExecutorService es = Executors.newFixedThreadPool(threads);
        Future[] futures = new Future[size];
        int cores = Math.min(threads, Runtime.getRuntime().availableProcessors());
        writer.print("[" + cores + "]");
        // For each row
        // ***********************************************************************************************
        for (int row = 0; row < size; row++) {
            // Gets a diagonal element
            double value = matrixA[row][row];
            //Max val swap
            int pointer = row;
            if (row != (size - 1)) {
                for (int i = row + 1; i < size; i++) {
                    if (Math.abs(matrixA[i][row]) > value) {
                        pointer = i;
                        value = matrixA[i][row];
                    }
                }
            }
            // Swapping the max. element row with pivot
            if (row != pointer) {
                for (int k = 0; k < matrixA.length; k++) {
                    double temp = matrixA[row][k];
                    matrixA[row][k] = matrixA[pointer][k];
                    matrixA[pointer][k] = temp;
                }
            }
            // All elements in this row are divided by diagonal
            for (int col = row + 1; col < size; col++) {
                matrixA[row][col] /= value;
            }
            vectorB[row] /= value;
            matrixA[row][row] = 1.0;
            int step = (size - row + cores - 1) / cores;
            // ***********************************************************************************************
            // Parallel Method managed by executor service
            for (int innerRow = row + 1; innerRow < size; innerRow += step) {
                int rowD = row;
                int begin = innerRow, end = Math.min(size, innerRow + step);

                futures[innerRow] = es.submit(() -> {
                    for (int innerRow_ = begin; innerRow_ < end; innerRow_++) {
                        double innerValue = matrixA[innerRow_][rowD];
                        for (int innerCol = rowD + 1; innerCol < size; innerCol++) {
                            matrixA[innerRow_][innerCol] -= innerValue * matrixA[rowD][innerCol];
                        }
                        vectorB[innerRow_] -= matrixA[innerRow_][rowD] * vectorB[rowD];
                        matrixA[innerRow_][rowD] = 0.0;
                    }
                });
            }
            // ***********************************************************************************************
            for (int innerRow = row + 1; innerRow < size; innerRow += step) {
                try {
                    futures[innerRow].get(); // Waits until computation is complete
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        }
        es.shutdown();
        // ***********************************************************************************************
        // Back-substitution
        for (int back = size - 1; back >= 0; back--) {
            vectorX[back] = vectorB[back];
            for (int i = back - 1; i >= 0; i--) {
                vectorB[i] -= vectorX[back] * matrixA[i][back];
            }
        }
        // ***********************************************************************************************
        writer.print("\t\t" + (System.currentTimeMillis() - start));
    }
}