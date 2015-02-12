package jp.mufg.api;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataMartEngine {
    final static long started = System.nanoTime();
    final ExecutorService executor = Executors.newCachedThreadPool();
    volatile boolean running = true;

    public void add(@NotNull DataMartWrapper dataMartWrapper) {
        executor.submit(() -> {
            try {
                String name = "Engine-" + dataMartWrapper.getTarget();
                Thread.currentThread().setName(name);
                System.out.printf("%s: Started after %.3f%n",
                        name, (System.nanoTime() - started) / 1e9);
                dataMartWrapper.start();
                while (running) {
                    while (dataMartWrapper.runOnce()) {
            //            System.out.println("ran one");
                    }
                    if (dataMartWrapper.onIdle()) {
        //                System.out.println("onIdle");
                    } else {
//                        System.out.println("zzzz");
                        Thread.sleep(20);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                dataMartWrapper.stop();
            }
        });
    }

    public void shutdown() {
        running = false;
        executor.shutdown();
        System.out.printf("Engine ran for %.3f secs%n",
                (System.nanoTime() - started) / 1e9);
    }
}
