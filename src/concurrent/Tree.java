package concurrent;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


// 所有CRUD前是否空树的判断都应在外部调用isEmpty完成
public class Tree<K extends Comparable<? super K>, V> {
    final int factor;
    AtomicReference<Node<K, V>> root;

    public Tree(int factor) {
        this.factor = factor;
        root = new AtomicReference<>(new Leaf<>(factor, this));
    }

    public boolean isEmpty() {
        return root.get().keys.isEmpty();
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


    // 此时若有重复键则获取第一个值
    public Info<V> delete(K key) {
        Info<V> info;
        do {
            info = root.get().optimisticDelete(key, null, 0);
        } while (info.st == Status.RETRY);

        if (info.st == Status.FAILURE) {
            do {
                info = root.get().delete(key, null, 0, 0);
            } while (info.st == Status.RETRY);
        }
        return info;
    }

    public Info<V> delete(K key, V val) {
        Info<V> info;
        do {
            info = root.get().optimisticDelete(key, val, 0);
        } while (info.st == Status.RETRY);

        if (info.st == Status.FAILURE) {
            do {
                info = root.get().delete(key, val, 0, 0);
            } while (info.st == Status.RETRY);
        }
        return info;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (root.get().keys.isEmpty()) return null;
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
