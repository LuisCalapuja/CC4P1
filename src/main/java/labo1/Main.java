package labo1;

public class Main { 
    public static void main(String[] args) {
        /*
        int[][] grid = {
               { 1, 1, 0, 0, 1, 0, 0, 0 },
               { 1, 0, 0, 1, 1, 0, 1, 0 },
               { 1, 1, 0, 1, 0, 0, 1, 0 },
               { 1, 1, 0, 1, 1, 1, 1, 1 },
               { 1, 1, 0, 0, 0, 1, 1, 1 },
               { 0, 1, 1, 1, 0, 1, 1, 0 },
               { 1, 1, 0, 1, 1, 1, 1, 0 },
               { 0, 1, 1, 1, 1, 1, 1, 1 }

       };*/
        int[][] grid = GridFactory.makeGridN();
    
        AAsteriskParallel.Pair src0 = new AAsteriskParallel.Pair(0,0);
        AAsteriskParallel.Pair dest0 = new AAsteriskParallel.Pair(9,9);
        
        long startTime0 = System.nanoTime();
        AAsteriskParallel.aStarSearchParallelThread(grid, src0, dest0, /*numThreads=*/4);
        long timeInNanos0 = System.nanoTime() - startTime0;
        System.out.printf("Tiempo de ejecucion en  paralelo %8.3f (ms): ",timeInNanos0/1e6);
        
        AAsterisk.Pair src1 = new AAsterisk.Pair(0,0);
        AAsterisk.Pair dest1 = new AAsterisk.Pair(9,9);
        
        AAsterisk app = new AAsterisk();
        
        long startTime1 = System.nanoTime();
        app.aStarSearch(grid, grid.length , grid[0].length, src1, dest1);
        long timeInNanos1 = System.nanoTime() - startTime1;
        System.out.printf("Tiempo de ejecucion en secuencial %8.3f (ms): ",timeInNanos1/1e6);
    }
    
}
