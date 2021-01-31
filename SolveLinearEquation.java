package project1;

/**
 * Abhay Vivek Kulkarni
 * ak6277@rit.edu
 * <p>
 * CSCI - 654
 * Parallel Computing Project 1
 * <p>
 * Solve Linear Equations
 */

import java.io.File;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

public class SolveLinearEquation {

    /**
     * Swaps rows
     * Accessed by the worker threads
     *
     * @param A : Coefficient Matrix
     * @param i : first row to  be swapped
     * @param j : second row to  be swapped
     */
    public static void swapRows(double[][] A, int i, int j) {
        for (int k = 0; k <= A.length; k++) {
            double temp = A[i][k];
            A[i][k] = A[j][k];
            A[j][k] = temp;
        }
    }

    public static void main(String[] args) {
        String inputFile = "C:\\Users\\abhay\\IdeaProjects\\654\\src\\com\\company\\project1\\4.txt";
        try {
            String temp = String.valueOf(Runtime.getRuntime().availableProcessors());
            if (args.length > 0)
                temp = args[0];
            int numOfThreadsToUse = Integer.parseInt(temp); //Get this as user input
            if (numOfThreadsToUse < 1) {
                numOfThreadsToUse = 1;
                System.out.println("The number of threads was adjusted to be 1");
                System.out.println("can't perform any operations on <1 thread");
            }
            double[][] A = readFile(inputFile);
            if (A.length <= 1) {
                System.err.println("The matrix size was <= [1x1]");
                System.exit(1);
            }
            double[] result = doParallelGaussianElimination(A, numOfThreadsToUse);
            // Rounding up the answers
            Arrays.stream(result)
                    .forEach(value -> System.out.printf("%.2f%n", value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static double[] doParallelGaussianElimination(double[][] A, int numOfThreadsToUse) {
        //printMatrix(A);
        final double begin = System.nanoTime();
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        System.out.println("Total processors: " + availableProcessors);
        if (numOfThreadsToUse < availableProcessors) {
            availableProcessors = numOfThreadsToUse;
        }
        if (availableProcessors > A.length) {
            availableProcessors = A.length / 2;
        }
        // availableProcessors = 1;
        System.out.println("Threads spawned for processing: " + availableProcessors);
        int div = A.length / availableProcessors;
        int remainder = A.length % availableProcessors;
        Worker[] threads = new Worker[availableProcessors];
        CountDownLatch latch = new CountDownLatch(availableProcessors);
        for (int p = 0; p < availableProcessors; p++) {
            int lower = p * div;
            int higher = (p + 1) * div;
            if (p == (availableProcessors - 1))
                threads[p] = new Worker(A, lower, higher + remainder, latch);
            else
                threads[p] = new Worker(A, lower, higher, latch);
            threads[p].start();
        }
        try {
            for (Thread t : threads) {
                t.join();
            }
        } catch (InterruptedException e) {
            System.out.println("Issue with Thread join! ");
            e.printStackTrace();
        }

        printMatrix(A);
        final double duration = (System.nanoTime() - begin) / 1000000;
        System.out.println("\n" + duration + "ms");
        return backSubstitution(A);
    }

    public static void printMatrix(double[][] answer) {
        for (double[] doubles : answer) {
            for (int j = 0; j < answer[0].length; j++) {
                if (j == answer[0].length - 1)
                    System.out.println(" || " + doubles[j]);
                else
                    System.out.print(doubles[j] + " ");
            }
            //System.out.println();
        }
        //System.out.println();
    }

    private static double[] backSubstitution(double[][] A) {

        int n = A.length;
        double[] currentArray = new double[n];
        for (int x = n - 1; x >= 0; x--) {
            currentArray[x] = A[x][n];
            for (int y = x + 1; y < n; y++) {
                currentArray[x] = currentArray[x] - A[x][y] * currentArray[y];
            }
            currentArray[x] = currentArray[x] / A[x][x];
        }
        return currentArray;
    }

    private static double[][] readFile(String text) {
        try {
            File file = new File(text);
            Scanner sc = new Scanner(file);
            int size = Integer.parseInt(sc.nextLine());
            // Augmented matrix [A|b] --> N*(N+1)
            double[][] matrix = new double[size][size + 1];
            int counter = 0;
            while (sc.hasNextLine()) {
                String tempStr = sc.nextLine().trim();
                double[] v = Stream.of(tempStr.split(" "))
                        .mapToDouble(Double::parseDouble)
                        .toArray();
                matrix[counter] = v;
                counter++;
            }
            sc.close();
            return matrix;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("File read complete");
        }

        return new double[1][1];
    }
}
