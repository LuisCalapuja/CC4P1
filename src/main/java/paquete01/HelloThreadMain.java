package paquete01;

public class HelloThreadMain {
    public static void main(String args[]) {
        (new HelloThread02()).start();
    }
}
class HelloThread02 extends Thread {
    @Override
    public void run() {
        System.out.println("Hello from a thread04!");
    }
}
