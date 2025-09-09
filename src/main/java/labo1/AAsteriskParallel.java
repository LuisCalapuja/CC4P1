package labo1;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class AAsteriskParallel {

    /* ===== Tipos básicos ===== */
    static class Pair {
        final int r, c;
        Pair(int r, int c){ this.r = r; this.c = c; }
        @Override public boolean equals(Object o){ return o instanceof Pair p && p.r==r && p.c==c; }
        @Override public int hashCode(){ return Objects.hash(r, c); }
        @Override public String toString(){ return "(" + r + "," + c + ")"; }
    }

    static class Details {
        final double f;  // prioridad
        final int r, c;
        Details(double f, int r, int c){ this.f = f; this.r = r; this.c = c; }
    }

    static class Cell {
        Pair parent;
        double f, g, h;
        Cell(){ parent = new Pair(-1,-1); f = g = h = Double.POSITIVE_INFINITY; }
    }

    /* ===== Utilidades ===== */
    static boolean isValid(int[][] grid, Pair p){
        return p.r>=0 && p.c>=0 && p.r<grid.length && p.c<grid[0].length;
    }
    static boolean isUnblocked(int[][] grid, Pair p){
        return isValid(grid, p) && grid[p.r][p.c]==1;
    }
    static boolean isDest(Pair a, Pair b){ return a.equals(b); }

    static void tracePath(Cell[][] cells, Pair dest){
        if (Double.isInfinite(cells[dest.r][dest.c].f)) {
            System.out.println("Failed to find the Destination Cell");
            return;
        }
        System.out.println("Path AStarParalell :");
        Deque<Pair> st = new ArrayDeque<>();
        int r = dest.r, c = dest.c;
        while (true){
            st.push(new Pair(r,c));
            Pair par = cells[r][c].parent;
            if (par.r==r && par.c==c) break;
            r = par.r; c = par.c;
        }
        while (!st.isEmpty()){
            System.out.println("-> " + st.pop());
        }
    }

    /* ===== Worker: EXTENDS THREAD ===== */
    static class WorkerThread extends Thread {
        final int[][] grid;
        final Pair dest;
        final PriorityBlockingQueue<Details> open;
        final boolean[][] closed;
        final Cell[][] cell;
        final ReentrantLock[][] locks;
        final AtomicBoolean done;

        WorkerThread(int[][] grid, Pair dest,
                     PriorityBlockingQueue<Details> open,
                     boolean[][] closed, Cell[][] cell,
                     ReentrantLock[][] locks, AtomicBoolean done,
                     String name){
            super(name);
            this.grid=grid; this.dest=dest; this.open=open;
            this.closed=closed; this.cell=cell; this.locks=locks; this.done=done;
        }

        @Override public void run() {
            int[] dx = {-1,-1,-1, 0,0, 1,1,1};
            int[] dy = {-1, 0, 1,-1,1,-1,0,1};

            // System.out.println("Iniciando worker: " + getName());

            while(!done.get()){
                Details cur = null;
                try {
                    // Espera corta para revisar 'done' con frecuencia
                    cur = open.poll(50, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ignored) {}
                if (cur == null) continue;

                int i = cur.r, j = cur.c;

                // Marcar cerrado de forma segura
                locks[i][j].lock();
                try {
                    if (closed[i][j]) continue;
                    closed[i][j] = true;
                } finally {
                    locks[i][j].unlock();
                }

                // ¿Destino?
                if (isDest(new Pair(i,j), dest)) {
                    done.set(true);
                    return;
                }

                // Expandir 8 vecinos
                for (int k=0;k<8;k++){
                    int ni = i + dx[k], nj = j + dy[k];
                    Pair nb = new Pair(ni,nj);
                    if (!isUnblocked(grid, nb)) continue;

                    boolean diag = (dx[k]!=0 && dy[k]!=0);
                    double step = diag ? Math.sqrt(2.0) : 1.0;
                    double tentativeG = cell[i][j].g + step;

                    double ddx = dest.r - ni, ddy = dest.c - nj;
                    double h = Math.sqrt(ddx*ddx + ddy*ddy);
                    double f = tentativeG + h;

                    locks[ni][nj].lock();
                    try {
                        if (f < cell[ni][nj].f) {
                            cell[ni][nj].g = tentativeG;
                            cell[ni][nj].h = h;
                            cell[ni][nj].f = f;
                            cell[ni][nj].parent = new Pair(i,j);
                            open.add(new Details(f, ni, nj));
                        }
                    } finally {
                        locks[ni][nj].unlock();
                    }
                }
            }
        }
    }

    /* ===== A* paralelo con Thread ===== */
    static void aStarSearchParallelThread(int[][] grid, Pair src, Pair dest, int numThreads){
        if (!isUnblocked(grid, src) || !isUnblocked(grid, dest)) {
            System.out.println("Source or destination is blocked/invalid.");
            return;
        }
        if (isDest(src, dest)) {
            System.out.println("We're already (t)here...");
            return;
        }

        int R = grid.length, C = grid[0].length;
        boolean[][] closed = new boolean[R][C];
        Cell[][] cells = new Cell[R][C];
        ReentrantLock[][] locks = new ReentrantLock[R][C];
        for (int i=0;i<R;i++){
            for (int j=0;j<C;j++){
                cells[i][j] = new Cell();
                locks[i][j] = new ReentrantLock();
            }
        }
        // init origen
        cells[src.r][src.c].f = 0;
        cells[src.r][src.c].g = 0;
        cells[src.r][src.c].h = 0;
        cells[src.r][src.c].parent = new Pair(src.r, src.c);

        PriorityBlockingQueue<Details> open =
                new PriorityBlockingQueue<>(64, Comparator.comparingDouble(d -> d.f));
        open.add(new Details(0.0, src.r, src.c));

        AtomicBoolean done = new AtomicBoolean(false);

        // Crear y arrancar hilos (Workers)
        Thread[] ts = new Thread[numThreads];
        for (int t=0;t<numThreads;t++){
            ts[t] = new WorkerThread(grid, dest, open, closed, cells, locks, done, "A*-W"+t);
            ts[t].start();
        }
        /*
        // Mostrar hilos creados (principal + workers)
        System.out.println("Hilos creados:");
        System.out.println(" - " + Thread.currentThread().getName() + " (principal)");
        for (Thread th : ts){
            System.out.println(" - " + th.getName() + " (worker)");
        }*/

        // Esperar fin
        for (Thread th : ts){
            try { th.join(); } catch (InterruptedException ignored) {}
        }

        if (done.get()){
            System.out.println("The destination cell is found");
        }
        tracePath(cells, dest);
    }
}


