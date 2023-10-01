package bplustree;

import java.util.List;

abstract class TreeNode<K extends Comparable<? super K>, V> {
    List<K> keys;
    List<V> vals;
    List<TreeNode<K, V>> children;
    TreeNode<K, V> next;
    int order;
    abstract TreeNode<K, V> insertVal(K key, V val);
    abstract void split(TreeNode<K, V> parent); // 默认无重复键
    abstract V getVal(K key);
    abstract List<V> getRange(K start, K end);
    abstract TreeNode<K, V> delVal(K key);
    abstract void borrow(TreeNode<K, V> sibling, TreeNode<K, V> parent);
    abstract void merge(TreeNode<K, V> sibling, TreeNode<K, V> parent);
}
