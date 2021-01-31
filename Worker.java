package project1;

import java.util.concurrent.CountDownLatch;

/**
 * Abhay Vivek Kulkarni
 * ak6277@rit.edu
 * <p>
 * CSCI - 654
 * Parallel Computing Project 1
 * <p>
 * Worker for Gaussian Elimination
 */
public class Worker extends Thread {
    private final CountDownLatch latch;
    private double[][] A;
    private int startRow;
    private final int endRow;

    /**
     * Init
     *
     * @param A        :        Matrix
     * @param startRow : lower bound for range of this thread
     * @param endRow   :   upper bound for range of this thread
     * @param latch:   Latch
     */
    public Worker(double[][] A, int startRow, int endRow, CountDownLatch latch) {
        this.A = A;
        this.startRow = startRow;
        this.endRow = endRow;
        this.latch = latch;

    }

    /**
     * Processing
     */
    public void run() {
        int n = A.length;
        for (; startRow < endRow; startRow++) {
            int pointer = startRow;
            double value = A[pointer][startRow];
            // The pivots exits on diagonal elements <ie> A[i][i]
            // Pointer starting from that element to the last element in the matrix column
            // Get maximum value in the diagonal column
            for (int x = startRow + 1; x < endRow; x++) { //endrow--n
                if (Math.abs(A[x][startRow]) > value) {
                    value = A[x][startRow];
                    pointer = x;
                }
            }
            //latch.countDown();
            // Make sure the current row has the max val. at the pivot position.
            // Swap the origin row with MAX row

            if (startRow != pointer) {
                SolveLinearEquation.swapRows(A, startRow, pointer);
            }
            latch.countDown();
            // Iterate through the rows
            // f = A[row][c] / A[pivot][c]
            // Perform subtraction with multiple on the entire row
            // Populate the bottom triangular matrix with 0's
            for (int x = startRow + 1; x < endRow; x++) { //
                // Multiplier
                double f = A[x][startRow] / A[startRow][startRow];
                for (int y = startRow + 1; y <= n; y++)
                    A[x][y] -= A[startRow][y] * f;
                A[x][startRow] = 0;
            }
        }
    }
}



