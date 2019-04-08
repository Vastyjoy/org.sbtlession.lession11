package ThreadPool;

import ThreadPool.FixedThreadPool.FixedThreadPool;
import ThreadPool.ScalableThreadPool.ScalableThreadPool;

class Printer implements Runnable {

    private int count;

    private String name;

    Printer(String name, int count) {
        this.name = name;
        this.count = count;
    }

    @Override
    public void run() {
        for (int i = 0; i < count; i++) {
            System.out.println(Thread.currentThread().getName() + " work :" + name + " print:" + i + " in thread");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}

public class Main {


    public static void main(String[] args) throws InterruptedException {
        ThreadPool threadPool = ScalableThreadPool.getInstance(2, 2, 11000);
        //ThreadPool threadPool= FixedThreadPool.getInstance(5);
        threadPool.execute(new Printer("Printer:-1",5));
        threadPool.start();
        System.out.println("thread pool started");

        for (int i = 0; i < 3; i++) {
            threadPool.execute(new Printer("Printer:" + i, 10));
        }
        Thread.sleep(10000);
        System.out.println("Sleep 10000");
        for (int i = 3; i < 7; i++) {
            threadPool.execute(new Printer("Printer:" + i, 10));
        }
//        threadPool.terminate();

    }

}
