package test;

import concurrent.Status;
import concurrent.Tree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

public class ConcurrentTest {
    @Test
    void test() throws InterruptedException {
        Tree<Integer, Integer> t = new Tree<>(5);
        Thread t0 = new Thread(() -> testInsert1(t));
        Thread t1 = new Thread(() -> testInsert2(t));
        Thread t2 = new Thread(() -> testInsert3(t));
        Thread t3 = new Thread(() -> testInsert4(t));
        t0.start();
        t1.start();
        t2.start();
        t3.start();
        t0.join();
        t1.join();
        t2.join();
        t3.join();
        Thread t4 = new Thread(() -> testDelete1(t));
        Thread t5 = new Thread(() -> testDelete2(t));
        Thread t6 = new Thread(() -> testDelete3(t));
        t4.start();
        t5.start();
        t6.start();
        t4.join();
        t5.join();
        t6.join();
        System.out.println(t);
    }

    @Test
    void test1() {
        Tree<Integer, Integer> t = new Tree<>(5);
        for (int i = 0; i < 20; i++) t.insert(i, i);
        for (int i = 1; i < 19; i++) t.delete(i);
        System.out.println(t);
    }

    void testInsert1(Tree<Integer, Integer> t) {
        for (int i = 1; i < 200; i += 2) {
            t.insert(i, i);
        }
    }

    void testInsert2(Tree<Integer, Integer> t) {
        for (int i = -1; i > -200; i -= 2) {
            t.insert(i, i);
        }
    }

    void testInsert3(Tree<Integer, Integer> t) {
        for (int i = 2; i < 201; i += 2) {
            t.insert(i, i);
        }
    }

    void testInsert4(Tree<Integer, Integer> t) {
        for (int i = 0; i > -201; i -= 2) {
            t.insert(i, i);
        }
    }

    void testInsert5(Tree<Integer, Integer> t) {
        for (int i = 201; i < 400; i += 2) {
            t.insert(i, i);
        }
    }

    void testGet(Tree<Integer, Integer> t) {
        for (int i = -150; i <= 150; i++) {
            Assertions.assertEquals(i, t.get(i));
        }
    }

    void testGetRange(Tree<Integer, Integer> t) {
        for (int i = -150; i < 150; i += 10) {
            List<Integer> lst = new LinkedList<>();
            for (int j = i; j < i + 10; j++) lst.add(j);
            Assertions.assertEquals(lst, t.getRange(i, i + 10));
        }
    }

    void testDelete1(Tree<Integer, Integer> t) {
        for (int i = -200; i < -100; i++) {
            Assertions.assertEquals(Status.SUCCESS, t.delete(i));
        }
    }

    void testDelete2(Tree<Integer, Integer> t) {
        for (int i = -100; i < 0; i++) {
            Assertions.assertEquals(Status.SUCCESS, t.delete(i));
        }
    }

    void testDelete3(Tree<Integer, Integer> t) {
        for (int i = 0; i < 201; i++) {
            Assertions.assertEquals(Status.SUCCESS, t.delete(i));
        }
    }
}
