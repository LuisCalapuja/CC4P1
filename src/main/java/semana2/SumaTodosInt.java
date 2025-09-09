package semana2;

public class SumaTodosInt {
    public int sum[] = new int[40];
    public static void main(String args[]) {
        new SumaTodosInt().inicio();
    }
    void inicio() {
        int N = 10000;
        int H = 6;
        int d = (int) ((N) / H);
        Thread todos[] = new Thread[40];
        for (int i = 0; i < (H - 1); i++) {
            todos[i] = new tarea0101((i * d + 1), (i * d + d), i);
            todos[i].start();
        }
        //Thread Hilo;
        todos[H - 1] = new tarea0101(((d * (H - 1)) + 1), N, H - 1);
        todos[H - 1].start();

        for (int i = 0; i < H; i++) {
            try {
                todos[i].join();
            } catch (InterruptedException ex) {
                System.out.println("error" + ex);
            }
        }
        int sumatotal = 0;
        for (int i = 0; i < H; i++) {
            sumatotal = sumatotal + sum[i];
        }
        System.out.println("suma total:" + sumatotal);
    }

    public class tarea0101 extends Thread {
        public int max, min, id;
            tarea0101(int min_, int max_, int id_) {
            max = max_;
            min = min_;
            id = id_;
            System.out.println("id" + id + " min: " + min_ + " max " + max_);
        }
        public void run() {
            int suma = 0;
            for (int i = min; i <= max; i++) {
                suma = suma + i;
                try {
                    sleep(1);
                } catch (InterruptedException ex) {
                    System.out.println("error " + ex);
                }
            }
            sum[id] = suma;
            System.out.println("id" + id + " suma:" + suma);
        }
    }
    
}
