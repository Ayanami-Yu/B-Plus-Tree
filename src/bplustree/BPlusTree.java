package bplustree;

import java.util.List;

public class BPlusTree<K extends Comparable<? super K>, V> {
    int order;
    TreeNode<K, V> root;

    public BPlusTree(int order) {
        this.order = order;
    }

    public void insert(K key, V val) {
        if (root == null) {
            root = new LeafNode<>(order);
            root.keys.add(key);
            root.vals.add(val);
        } else {
            root.insertVal(key, val);
            if (root.keys.size() > order) {
                InternalNode<K, V> newRoot = new InternalNode<>(order);
                root.split(newRoot);
                root = newRoot;
            }
        }
    }

    public V search(K key) {
        return root.getVal(key);
    }

    public List<V> searchRange(K start, K end) {
        return root.getRange(start, end);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (root.keys.size() == 0) return null;
        printTree(sb, root);
        return sb.toString();
    }

    void printTree(StringBuilder sb, TreeNode<K, V> node){
        sb.append('{');
        sb.append(node.keys.get(0));
        for (int i = 1; i < node.keys.size(); i++){
            sb.append(',');
            sb.append(node.keys.get(i));
        }
        if (node.children != null){
            for (TreeNode<K, V> child : node.children){
                printTree(sb, child);
            }
        }
        sb.append('}');
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean delete(K key) {
        TreeNode<K, V> node = root.delVal(key);
        if (node == null) return false;
        if (node.keys.size() == 1 && node.children != null)
            root = node.children.get(0);
        return true;
    }
}
