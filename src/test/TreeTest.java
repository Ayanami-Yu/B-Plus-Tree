package test;

import concurrent.Tree;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TreeTest {
    Integer FACTOR = 2000;
    long THREADS = 10, NUM = 20000;

    @Test
    void concurrentInsert() throws InterruptedException {
        Tree<Byte, Byte> t1 = new Tree<>(FACTOR);
        ExecutorService es = Executors.newCachedThreadPool();

        long start = System.nanoTime();
        for (long i = 0; i < THREADS; i++) {
            es.submit(() -> {
                for (long j = 0; j < NUM; j++) {
                    t1.insert((byte) j, (byte) j);
                }
            });
        }
        es.shutdown();
        boolean b = es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        long end = System.nanoTime();
        System.out.println(end - start);
        if (!b) es.shutdownNow();
    }

    @Test
    void serialInsert() throws InterruptedException {
        Tree<Byte, Byte> t2 = new Tree<>(FACTOR);
        ExecutorService es = Executors.newSingleThreadExecutor();

        long start = System.nanoTime();
        es.submit(() -> {
            for (long i = 0; i < THREADS * NUM; i++) {
                t2.insert((byte) i, (byte) i);
            }
        });
        es.shutdown();
        boolean b = es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        long end = System.nanoTime();
        System.out.println(end - start);
        if (!b) es.shutdownNow();
    }
}
