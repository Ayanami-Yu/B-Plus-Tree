package bplustree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InternalNode<K extends Comparable<? super K>, V> extends TreeNode<K, V> {
    InternalNode(int order) {
        this.order = order;
        keys = new ArrayList<>();
        children = new ArrayList<>();
    }

    InternalNode(int order, List<K> keys, List<TreeNode<K, V>> children) {
        this.order = order;
        this.keys = new ArrayList<>(keys);
        this.children = new ArrayList<>(children);
    }

    @Override
    TreeNode<K, V> insertVal(K key, V val) {
        TreeNode<K, V> child = getChild(key).insertVal(key, val);
        if (child.keys.size() > order) {
            child.split(this);
        }
        return this;
    }

    TreeNode<K, V> getChild(K key) {
        if (key.compareTo(keys.get(keys.size() - 1)) > 0) {
            keys.set(keys.size() - 1, key);
            TreeNode<K, V> maxNode = children.get(keys.size() - 1);
            while (maxNode.children != null) { // 修改每个内结点的最大键
                maxNode.keys.set(maxNode.keys.size() - 1, key);
                maxNode = maxNode.children.get(maxNode.keys.size() - 1);
            }
        }
        int i = Collections.binarySearch(keys, key);
        i = i >= 0 ? i : -i - 1;
        return children.get(i);
    }

    void split(TreeNode<K, V> parent) {
        int mid = order / 2 + 1;
        InternalNode<K, V> newNode = new InternalNode<>(order, keys.subList(mid, order + 1)
                ,children.subList(mid, order + 1));
        keys.subList(mid, order + 1).clear();
        children.subList(mid, order + 1).clear();

        K splitKey = keys.get(order / 2);
        if (parent.keys.size() == 0) { // 父结点为新根（由于无重复键）
            parent.keys.add(splitKey);
            parent.keys.add(newNode.keys.get(newNode.keys.size() - 1));
            parent.children.add(this);
            parent.children.add(newNode);
        } else {
            int i = Collections.binarySearch(parent.keys, splitKey);
            parent.keys.add(-i - 1, splitKey);
            parent.children.add(-i, newNode);
        }
    }

    @Override
    V getVal(K key) {
        if (key.compareTo(keys.get(keys.size() - 1)) > 0) return null;
        int i = Collections.binarySearch(keys, key);
        i = i >= 0 ? i : -i - 1;
        return children.get(i).getVal(key);
    }

    @Override
    List<V> getRange(K start, K end) {
        int i = Collections.binarySearch(keys, start);
        i = i >= 0 ? i : -i - 1;
        return children.get(i).getRange(start, end);
    }

    @Override
    TreeNode<K, V> delVal(K key) {
        int i = Collections.binarySearch(keys, key);
        i = i >= 0 ? i : -i - 1;
        if (i >= keys.size()) return null; // 比最大键还大的key不存在

        TreeNode<K, V> child = children.get(i).delVal(key);
        if (child == null) return null;
        K maxKey = child.keys.get(child.keys.size() - 1);
        if (maxKey != keys.get(i)) // 孩子的最大值被删除
            keys.set(i, maxKey);

        if (child.keys.size() <= order / 2) { // 删除后孩子键数过少
            TreeNode<K, V> sibling;
            if (i + 1 < keys.size()) {
                sibling = children.get(i + 1);
            } else {
                sibling = children.get(i - 1);
            }
            if (sibling.keys.size() > order / 2 + 1) // 兄弟有多余key
                child.borrow(sibling, this);
            else
                child.merge(sibling, this);
        }
        return this;
    }

    @Override
    void borrow(TreeNode<K, V> sibling, TreeNode<K, V> parent) {
        if (sibling.keys.get(0).compareTo(keys.get(0)) > 0) { // sibling在this右边
            keys.add(sibling.keys.get(0));
            children.add(sibling.children.get(0));
            sibling.keys.remove(0);
            sibling.children.remove(0);

            int i = Collections.binarySearch(parent.keys, keys.get(keys.size() - 1));
            parent.keys.set(-i - 2, keys.get(keys.size() - 1));
        } else {
            int idx = sibling.keys.size() - 1;
            keys.add(0, sibling.keys.get(idx)); // sibling在左边时会借出局部最大值
            children.add(0, sibling.children.get(idx));
            sibling.keys.remove(idx);
            sibling.children.remove(idx);

            int i = Collections.binarySearch(parent.keys, sibling.keys.get(idx - 1));
            parent.keys.set(-i - 1, sibling.keys.get(idx - 1)); // 默认借出的键不重复
        }
    }

    @Override
    void merge(TreeNode<K, V> sibling, TreeNode<K, V> parent) {
        K maxKey = keys.get(keys.size() - 1);
        int i = Collections.binarySearch(parent.keys, maxKey);

        if (sibling.keys.get(0).compareTo(keys.get(0)) > 0) {
            sibling.keys.addAll(0, keys);
            sibling.children.addAll(0, children);
        } else {
            sibling.keys.addAll(keys);
            sibling.children.addAll(children);
            parent.keys.set(i - 1, maxKey);
        }
        parent.keys.remove(i);
        parent.children.remove(i);
    }
}
