package concurrent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class Node<K extends Comparable<? super K>, V> {
    List<K> keys;
    List<V> vals;
    List<Node<K, V>> children;
    Node<K, V> next;
    Node<K, V> parent;
    int factor;
    Tree<K, V> tree;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock rLock = lock.readLock();
    private final Lock wLock = lock.writeLock();

    ReentrantReadWriteLock getLock() { return lock; }

    Lock getrLock() { return rLock; }

    Lock getwLock() { return wLock; }

    boolean safeForDelete() { return keys.size() > factor / 2; }

    boolean safeForInsert() { return keys.size() < factor - 1; }

    boolean isOverflow() { return keys.size() > factor - 1; }

    boolean isUnderflow() { return keys.size() < factor / 2; }

    abstract Status optimisticInsert(K key, V val, int dep);

    abstract Status insert(K key, V val, int loc, int dep); // 告知子结点其在本结点中的索引

    abstract Status split(int loc); // 传入子结点在键中的位置

    void unlockAncestors() {
        Node<K, V> ancestor = parent;
        while (ancestor != null && ancestor.getLock().isWriteLockedByCurrentThread()) {
            Node<K, V> node = ancestor;
            ancestor = ancestor.parent;
            node.getwLock().unlock();
        }
    }

    // neg为true时返回如果插入则该键在集合中对应的下标
    int getIdx(K key, boolean neg) {
        int i = Collections.binarySearch(keys, key);
        if (i >= 0) {
            K tmp = keys.get(i);
            while (i - 1 >= 0 && keys.get(i - 1).equals(tmp)) { // 有重复键时应取到第一个key
                i--;
            }
        }
        if (neg) return i;
        else return i >= 0 ? i : -i - 1;
    }

    @Override
    public String toString() { return " " + keys.toString(); }

    void generateNewRoot() {
        Node<K, V> newRoot = new Internal<>(factor, tree);
        newRoot.children.add(this);
        parent = newRoot;
    }

    abstract Node<K, V> splitNode(int mid);

    abstract Info<V> get(K key, int dep);

    abstract Info<V> getRange(K start, K end, int dep);

    abstract Info<V> delete(K key, V val, int loc, int dep);    // 有重复键时需指定val的值

    abstract Info<V> optimisticDelete(K key, V val, int dep);

    Status redistribute(int loc) {
        getwLock().lock();
        try {
            Node<K, V> leNode = null, riNode = null;
            Status st;
            if (loc > 0) leNode = parent.children.get(loc - 1); // 若左边有兄弟结点
            if (loc < parent.children.size() - 1)               // 若右边有兄弟结点
                riNode = parent.children.get(loc + 1);

            if (leNode != null && leNode.safeForDelete()) {     // 若左兄弟非空且不会下溢
                st = borrow(leNode, loc, true);
            } else if (riNode != null && riNode.safeForDelete()) {
                st = borrow(riNode, loc, false);
            } else if (leNode != null) {
                st = merge(leNode, loc, true);
            } else if (riNode != null) {
                st = merge(riNode, loc, false);
            } else st = Status.SINGLE_ROOT;
            return st;
        } finally {
            getwLock().unlock();
        }
    }

    abstract Status borrow(Node<K, V> sib, int loc, boolean isLeft);

    abstract Status merge(Node<K, V> sib, int loc, boolean isLeft);

    void log(String message, K key, Node<K, V> node, int dep) {
        System.out.println(Thread.currentThread().getName() + message
                + " k=" + key + " dep=" + dep + node);
    }
}
