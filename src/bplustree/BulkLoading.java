package bplustree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BulkLoading {
    static class Entry implements Comparable<Entry>{
        Integer key;
        String val;
        Entry(Integer key, String val) {
            this.key = key;
            this.val = val;
        }

        @Override
        public int compareTo(Entry o) {
            return key - o.key;
        }
    }

    public void bulkLoad(String filePath, BPlusTree<Integer, String> bpt) {
        List<Entry> entries = readData(filePath);
        int i = 0;
        LeafNode<Integer, String> oldLeaf = new LeafNode<>(bpt.order);
        LeafNode<Integer, String> newLeaf = new LeafNode<>(bpt.order);
        while (i < bpt.order) {
            oldLeaf.keys.add(entries.get(i).key);
            oldLeaf.vals.add(entries.get(i).val);
            i++;
        }
        bpt.root = oldLeaf; //root不断指向每层的第一个结点

        while (i < entries.size()) { //构建叶子层
            oldLeaf.next = newLeaf;
            for (int j = 0; j < bpt.order; j++) {
                newLeaf.keys.add(entries.get(i).key);
                newLeaf.vals.add(entries.get(i).val);
                i++;
            }
            newLeaf = new LeafNode<>(bpt.order);
            oldLeaf = (LeafNode<Integer, String>) oldLeaf.next;
        }
        bpt.root = buildTree(bpt.root, bpt.order);
    }

    List<Entry> readData(String filePath) {
        File file = new File(filePath);
        List<Entry> entries = new ArrayList<>();
        try (FileReader fr = new FileReader(file, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(fr)
        ) {
            String st;
            while ((st = br.readLine()) != null) {
                String[] s = st.split(" ");
                Entry entry = new Entry(Integer.parseInt(s[0]), s[1]);
                entries.add(entry);
            }
            Collections.sort(entries); //有序后再bulk loading
        } catch (IOException e) {
            e.printStackTrace();
        }
        return entries;
    }

    TreeNode<Integer, String> buildTree(TreeNode<Integer, String> node, int order) {
        InternalNode<Integer, String> oldNode = new InternalNode<>(order);
        InternalNode<Integer, String> newNode = new InternalNode<>(order);
        for (int i = 0; node != null && i < order; i++) {
            oldNode.keys.add(node.keys.get(node.keys.size() - 1)); //先对第一个父结点初始化
            oldNode.children.add(node);
            node = node.next;
        }
        TreeNode<Integer, String> firstNode = oldNode;
        if (node == null) return firstNode; //已达到根

        for (int i = 1; node != null; i++) {
            oldNode.next = newNode;
            newNode.keys.add(node.keys.get(node.keys.size() - 1));
            newNode.children.add(node);
            node = node.next;
            if (i == order) {
                newNode = new InternalNode<>(order);
                oldNode = (InternalNode<Integer, String>) oldNode.next;
                i = 0;
            }
        }
        return buildTree(firstNode, order);
    }
}
