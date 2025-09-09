package labo1;

// En tu package labo1 si quieres
import java.util.Random;

public class GridFactory {
    /**
     * Genera una grilla NxN con 1=celdas libres y 0=bloqueadas.
     * @param N tamaño (por ej. 1000)
     * @param obstacleProb probabilidad de bloqueo [0..1) (p.ej. 0.30)
     * @param src fila/col de inicio
     * @param dest fila/col de destino
     * @param ensurePath si true, abre un camino Manhattan simple src->dest
     * @param seed semilla fija para reproducibilidad (o null para aleatoria)
     */
    public static int[][] makeGrid(int N, double obstacleProb,
                                   int[] src, int[] dest,
                                   boolean ensurePath, Long seed) {
        Random rnd = (seed == null ? new Random() : new Random(seed));
        int[][] g = new int[N][N];
        // Relleno aleatorio
        for (int r=0; r<N; r++) {
            for (int c=0; c<N; c++) {
                g[r][c] = (rnd.nextDouble() < obstacleProb) ? 0 : 1;
            }
        }
        // Asegurar que src y dest estén libres
        g[src[0]][src[1]] = 1;
        g[dest[0]][dest[1]] = 1;

        // Camino garantizado (opcional): recta en L (Manhattan)
        if (ensurePath) {
            int r = src[0], c = src[1];
            // mover filas
            int stepR = (dest[0] >= r) ? 1 : -1;
            while (r != dest[0]) { g[r][c] = 1; r += stepR; }
            // mover columnas
            int stepC = (dest[1] >= c) ? 1 : -1;
            while (c != dest[1]) { g[r][c] = 1; c += stepC; }
            g[dest[0]][dest[1]] = 1;
        }
        return g;
    }

    // Atajo específico 1000x1000
    public static int[][] makeGridN() {
        int N = 10;
        int[] src  = {0, 0};
        int[] dest = {N-1, N-1};
        double obstacleProb = 0.30; // 30% bloqueado (ajusta a gusto)
        boolean ensurePath = true;  // garantiza solución
        long seed = 12345L;         // fija la aleatoriedad (reproducible)
        return makeGrid(N, obstacleProb, src, dest, ensurePath, seed);
    }
}

