package test;

import bplustree.BPlusTree;
import bplustree.BulkLoading;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BPlusTreeTest {
    @Test
    void test1() {
        BPlusTree<Integer, String> t = new BPlusTree<>(3);
        t.insert(2, "A");
        t.insert(4, "B");
        t.insert(6, "C");
        t.insert(8, "D");
        String s1 = "{4,8{2,4}{6,8}}";
        Assertions.assertEquals(s1, t.toString());

        t.insert(10, "E");
        t.insert(12, "F");
        t.insert(14, "G");
        t.insert(16, "H");
        t.insert(18, "I");
        t.insert(7, "L");
        String s2 = "{8,18{4,8{2,4}{6,7,8}}{12,18{10,12}{14,16,18}}}";
        Assertions.assertEquals(s2, t.toString());

        t.insert(5, "K");
        t.insert(-1, "O");
        t.insert(-4, "P");
        t.insert(13, "Z");
        t.insert(11, "M");
        t.insert(9, "N");
        String s3 = "{8,18{4,8{-1,4{-4,-1}{2,4}}{6,8{5,6}{7,8}}}{12,18{10,12{9,10}{11,12}}{14,18{13,14}{16,18}}}}";
        Assertions.assertEquals(s3, t.toString());

        t.delete(14);
        t.delete(18);
        t.delete(16);
        t.delete(-4);
        t.delete(8);
        t.delete(13);
        String s4 = "{7,12{4,7{-1,2,4}{5,6,7}}{10,12{9,10}{11,12}}}";
        Assertions.assertEquals(s4, t.toString());

        t.delete(4);
        t.delete(7);
        t.delete(-1);
        t.delete(6);
        t.delete(5);
        String s5 = "{10,12{2,9,10}{11,12}}";
        Assertions.assertEquals(s5, t.toString());
    }

    @Test
    void testBulkLoading() {
        BPlusTree<Integer, String> t = new BPlusTree<>(3);
        BulkLoading bl = new BulkLoading();
        String s1 = "/Users/yu/Downloads/test.txt";
        bl.bulkLoad(s1, t);
        String s2 = "{17,35{5,11,17{1,3,5}{7,9,11}{13,15,17}}{23,29,35{19,21,23}{25,27,29}{31,33,35}}}";
        Assertions.assertEquals(s2, t.toString());
    }

    @Test
    void test2() {
        BPlusTree<Integer, String> t = new BPlusTree<>(3);
        t.insert(1, "a");
        t.insert(3, "b");
        t.insert(27, "n");
        t.insert(29, "o");
        t.insert(11, "f");
        t.insert(13, "g");
        t.insert(21, "k");
        t.insert(5, "c");
        t.insert(7, "d");
        t.insert(9, "e");
        t.insert(23, "l");
        t.insert(33, "q");
        t.insert(35, "r");
        t.insert(15, "h");
        t.insert(25, "m");
        t.insert(31, "p");
        t.insert(17, "i");
        t.insert(19, "j");
        String s = "{7,23,35{3,7{1,3}{5,7}}{13,17,23{9,11,13}{15,17}{19,21,23}}{29,35{25,27,29}{31,33,35}}}";
        Assertions.assertEquals(s, t.toString());
    }
}
