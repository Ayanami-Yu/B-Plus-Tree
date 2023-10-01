package bplustree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class LeafNode<K extends Comparable<? super K>, V> extends TreeNode<K, V> {
    LeafNode(int order) {
        this.order = order;
        keys = new ArrayList<>();
        vals = new ArrayList<>();
    }

    LeafNode(int order, List<K> keys, List<V> vals) {
        this.order = order;
        this.keys = new ArrayList<>(keys);
        this.vals = new ArrayList<>(vals);
    }

    @Override
    TreeNode<K, V> insertVal(K key, V val) {
        int i = Collections.binarySearch(keys, key);
        keys.add(-i - 1, key);
        vals.add(-i - 1, val);
        return this; // 叶子的分裂由其父结点调用
    }

    @Override
    void split(TreeNode<K, V> parent) {
        int mid = order / 2 + 1;
        LeafNode<K, V> newLeaf = new LeafNode<>(order, keys.subList(mid, order + 1)
                ,vals.subList(mid, order + 1));
        keys.subList(mid, order + 1).clear();
        vals.subList(mid, order + 1).clear();

        K splitKey = keys.get(order / 2);
        int i = Collections.binarySearch(parent.keys, splitKey);
        if (parent.keys.size() == 0) { // 此时新根与该结点无任何关联
            parent.keys.add(splitKey);
            parent.keys.add(newLeaf.keys.get(newLeaf.keys.size() - 1));
            parent.children.add(this);
            parent.children.add(newLeaf);
        } else {
            parent.keys.add(-i - 1, splitKey);
            parent.children.add(-i, newLeaf);
            newLeaf.next = next;
        }
        next = newLeaf;
    }

    @Override
    V getVal(K key) {
        int i = Collections.binarySearch(keys, key);
        return i >= 0 ? vals.get(i) : null;
    }

    @Override
    List<V> getRange(K start, K end) {
        List<V> res = new LinkedList<>();
        int i = Collections.binarySearch(keys, start);
        i = i >= 0 ? i : -i - 1;

        LeafNode<K, V> leaf = this;
        while (leaf!= null && leaf.keys.get(i).compareTo(end) < 0) {
            res.add(leaf.vals.get(i++));
            if (i >= leaf.keys.size()) {
                i = 0;
                leaf = (LeafNode<K, V>) leaf.next;
            }
        }
        return res;
    }

    @Override
    TreeNode<K, V> delVal(K key) {
        int i = Collections.binarySearch(keys, key);
        if (i >= 0) {
            keys.remove(i);
            vals.remove(i);
        } else return null;
        return this;
    }

    @Override
    void borrow(TreeNode<K, V> sibling, TreeNode<K, V> parent) {
        if (sibling.keys.get(0).compareTo(keys.get(0)) > 0) {
            keys.add(sibling.keys.get(0));
            vals.add(sibling.vals.get(0));
            sibling.keys.remove(0);
            sibling.vals.remove(0);

            int i = Collections.binarySearch(parent.keys, keys.get(keys.size() - 1));
            parent.keys.set(-i - 2, keys.get(keys.size() - 1));
        } else {
            int idx = sibling.keys.size() - 1;
            keys.add(0, sibling.keys.get(idx));
            vals.add(0, sibling.vals.get(idx));
            sibling.keys.remove(idx);
            sibling.vals.remove(idx);

            int i = Collections.binarySearch(parent.keys, sibling.keys.get(idx - 1));
            parent.keys.set(-i - 1, sibling.keys.get(idx - 1));
        }
    }

    @Override
    void merge(TreeNode<K, V> sibling, TreeNode<K, V> parent) {
        K maxKey = keys.get(keys.size() - 1);
        int i = Collections.binarySearch(parent.keys, maxKey);

        if (sibling.keys.get(0).compareTo(keys.get(0)) > 0) {
            sibling.keys.addAll(0, keys);
            sibling.vals.addAll(0, vals);
        } else {
            sibling.keys.addAll(keys);
            sibling.vals.addAll(vals);
            parent.keys.set(i - 1, maxKey);
        }
        parent.keys.remove(i);
        parent.children.remove(i);
    }
}
