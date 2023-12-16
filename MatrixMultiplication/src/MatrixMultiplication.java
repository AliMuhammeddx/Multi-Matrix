import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.Scanner;


public class MatrixMultiplication extends RecursiveTask<int[][]> {
    private final int[][] A;
    private final int[][] B;
    private final int rowStart;
    private final int rowEnd;
    private final int colStart;
    private final int colEnd;

    public MatrixMultiplication(int[][] A, int[][] B, int rowStart, int rowEnd, int colStart, int colEnd) {
        this.A = A;
        this.B = B;
        this.rowStart = rowStart;
        this.rowEnd = rowEnd;
        this.colStart = colStart;
        this.colEnd = colEnd;
    }

    @Override
    protected int[][] compute() {
        if (rowEnd - rowStart <= 64) {
            return multiplySequentially();
        } else {
            int midRow = (rowStart + rowEnd) / 2;
            int midCol = (colStart + colEnd) / 2;

            MatrixMultiplication topLeft = new MatrixMultiplication(A, B, rowStart, midRow, colStart, midCol);
            MatrixMultiplication topRight = new MatrixMultiplication(A, B, rowStart, midRow, midCol, colEnd);
            MatrixMultiplication bottomLeft = new MatrixMultiplication(A, B, midRow, rowEnd, colStart, midCol);
            MatrixMultiplication bottomRight = new MatrixMultiplication(A, B, midRow, rowEnd, midCol, colEnd);

            topLeft.fork();
            topRight.fork();
            bottomLeft.fork();
            bottomRight.fork();

            int[][] C = new int[A.length][B[0].length];
            int[][] C1 = topLeft.join();
            int[][] C2 = topRight.join();
            int[][] C3 = bottomLeft.join();
            int[][] C4 = bottomRight.join();

            for (int i = rowStart; i < rowEnd; i++) {
                for (int j = colStart; j < colEnd; j++) {
                    if (i < midRow && j < midCol) {
                        C[i][j] = C1[i][j] + C2[i][j];
                    } else if (i < midRow && j >= midCol) {
                        C[i][j] = C1[i][j - midCol] + C2[i][j - midCol];
                    } else if (i >= midRow && j < midCol) {
                        C[i][j] = C3[i - midRow][j] + C4[i - midRow][j];
                    } else {
                        C[i][j] = C3[i - midRow][j - midCol] + C4[i - midRow][j - midCol];
                    }
                }
            }


            return C;
        }
    }

    private int[][] multiplySequentially() {
        int[][] C = new int[rowEnd - rowStart][colEnd - colStart];

        for (int i = rowStart; i < rowEnd; i++) {
            for (int j = colStart; j < colEnd; j++) {
                for (int k = 0; k < B.length; k++) {
                    C[i - rowStart][j - colStart] += A[i][k] * B[k][j];
                }
            }
        }

        return C;
    }


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int[][] A = new int[3][3];
        int[][] B = new int[3][3];

        System.out.println("Please enter the values for the first matrix (A):");
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                A[i][j] = scanner.nextInt();
            }
        }

        System.out.println("Please enter the values for the second matrix (B):");
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                B[i][j] = scanner.nextInt();
            }
        }

        MatrixMultiplication task = new MatrixMultiplication(A, B, 0, A.length, 0, B[0].length);
        ForkJoinPool pool = new ForkJoinPool();
        int[][] C = pool.invoke(task);

        for (int[] row : C) {
            for (int cell : row) {
                System.out.print(cell + " ");
            }
            System.out.println();
        }
    }




}
