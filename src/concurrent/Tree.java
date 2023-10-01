package concurrent;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Tree<K extends Comparable<? super K>, V> {
    final int factor;
    AtomicReference<Node<K, V>> root;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock rLock = lock.readLock();
    private final Lock wLock = lock.writeLock();
    public Tree(int factor) {
        this.factor = factor;
        root = new AtomicReference<>(new Leaf<>(factor, this));
    }

    public void insert(K key, V val) {
        Status st;
        do {
            st = root.get().optimisticInsert(key, val, 0);
        } while (st == Status.RETRY);

        if (st == Status.FAILURE) {
            do {
                st = root.get().insert(key, val, 0, 0);
            } while (st == Status.RETRY);
        }
    }

    public V get(K key) {
        Info<V> info;
        do {
            info = root.get().get(key, 0);
        } while (info.st == Status.RETRY);
        return info.val;
    }

    public List<V> getRange(K start, K end) {
        Info<V> info;
        do {
            info = root.get().getRange(start, end, 0);
        } while (info.st == Status.RETRY);
        return info.vals;
    }

    public Status delete(K key) {
        Status st;
        do {
            st = root.get().optimisticDelete(key, 0);
        } while (st == Status.RETRY);

        if (st == Status.FAILURE) {
            do {
                st = root.get().delete(key, 0, 0);
            } while (st == Status.RETRY);
        }
        return st;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (root.get().keys.size() == 0) return null;
        printTree(sb, root.get());
        return sb.toString();
    }

    void printTree(StringBuffer sb, Node<K, V> node){
        sb.append('{');
        sb.append(node.keys.get(0));
        for (int i = 1; i < node.keys.size(); i++){
            sb.append(',');
            sb.append(node.keys.get(i));
        }
        if (node.children != null){
            for (Node<K, V> child : node.children){
                printTree(sb, child);
            }
        }
        sb.append('}');
    }
}
