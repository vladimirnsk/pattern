package org.example;

import org.example.pool.ThreadPool;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        var pool = new ThreadPool(2);
        for (int i = 0; i < 10; i++) {
            pool.execute(
                    Main::jobForExecution
            );
        }
    }
    private static void jobForExecution() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println("thread interrupted");
        }
        System.out.println("job done");
    }
}