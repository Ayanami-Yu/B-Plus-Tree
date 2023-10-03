package concurrent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Leaf<K extends Comparable<? super K>, V> extends Node<K, V> {
    Leaf(int factor, Tree<K, V> tree) {
        this.factor = factor;
        this.tree = tree;
        keys = new ArrayList<>();
        vals = new ArrayList<>();
    }

    @Override
    Status optimisticInsert(K key, V val, int dep) {
        getwLock().lock();
        Status st;
        try {
            if (dep == 0 && this != tree.root.get()) {
                st = Status.RETRY;
            } else {
                if (dep != 0) parent.getrLock().unlock();

                if (safeForInsert()) {
                    int idx = getIdx(key);
                    keys.add(idx, key);
                    vals.add(idx, val);
                    st = Status.SUCCESS;
                } else st = Status.FAILURE;
            }
            return st;
        } finally {
            getwLock().unlock();
        }
    }

    @Override
    Status insert(K key, V val, int loc, int dep) {
        getwLock().lock();
        Status st;
        try {
            if (dep == 0 && this != tree.root.get()) {
                st = Status.RETRY;
            } else {
                if (safeForInsert()) unlockAncestors();

                int idx = getIdx(key);
                keys.add(idx, key);
                vals.add(idx, val);
                if (isOverflow()) st = split(loc);
                else st = Status.SUCCESS;
            }
            return st;
        } finally {
            getwLock().unlock();
        }
    }

    @Override
    Status split(int loc) {
        getwLock().lock();
        Status st = Status.SUCCESS;
        try {
            int mid = factor / 2;
            Node<K, V> newLeaf = splitNode(mid);

            if (parent == null) {
                generateNewRoot();
                st = Status.NEW_ROOT;
            }
            newLeaf.next = next;
            next = newLeaf;
            parent.keys.add(loc, keys.get(mid - 1));
            parent.children.add(loc + 1, newLeaf);
            newLeaf.parent = parent;

            if (st == Status.NEW_ROOT) tree.root.set(parent);
            return st;
        } finally {
            getwLock().unlock();
        }
    }

    @Override
    Node<K, V> splitNode(int mid) {
        Node<K, V> newLeaf = new Leaf<>(factor, tree);
        newLeaf.keys.addAll(keys.subList(mid, keys.size()));
        newLeaf.vals.addAll(vals.subList(mid, vals.size()));
        keys.subList(mid, keys.size()).clear();
        vals.subList(mid, vals.size()).clear();
        return newLeaf;
    }

    @Override
    Info<V> get(K key, int dep) {
        getrLock().lock();
        try {
            if (dep == 0 && this != tree.root.get()) {
                return new Info<>(Status.RETRY, null);
            } else {
                if (dep != 0) parent.getrLock().unlock();

                int idx = Collections.binarySearch(keys, key);
                if (idx >= 0) {
                    return new Info<>(Status.SUCCESS, vals.get(idx));
                } else return new Info<>(Status.NOT_EXIST, null);
            }
        }finally {
            getrLock().unlock();
        }
    }

    @Override
    Info<V> getRange(K start, K end, int dep) {
        getrLock().lock();
        if (dep == 0 && this != tree.root.get()) {
            getrLock().unlock();
            return new Info<>(Status.RETRY, null);
        } else {
            List<V> res = new LinkedList<>();
            if (end.compareTo(keys.get(0)) >= 0) {      // 当end比最小键还小时应返回空表
                getList(start, end, this, res, 0);
            }
            return new Info<>(Status.SUCCESS, res);
        }
    }

    @Override
    Status delete(K key, int loc, int dep) {
        getwLock().lock();
        Status st;
        try {
            if (dep == 0 && this != tree.root.get()) {
                st = Status.RETRY;
            } else if (dep == 0 && keys.isEmpty()) {
                st = Status.EMPTY;
            } else {
                if (safeForDelete()) unlockAncestors();

                int idx = Collections.binarySearch(keys, key);
                if (idx < 0) st = Status.NOT_EXIST;
                else {
                    keys.remove(idx);
                    vals.remove(idx);
                    if (dep != 0 && isUnderflow()) st = redistribute(loc);
                    else st = Status.SUCCESS;
                }
            }
            return st;
        } finally {
            getwLock().unlock();
        }
    }

    @Override
    Status optimisticDelete(K key, int dep) {
        getwLock().lock();
        Status st;
        try {
            if (dep == 0 && this != tree.root.get()) {
                st = Status.RETRY;
            } else {
                if (dep != 0) parent.getrLock().unlock();

                if (safeForDelete()) {
                    int idx = Collections.binarySearch(keys, key);
                    if (idx < 0) st = Status.NOT_EXIST;
                    else {
                        keys.remove(idx);
                        vals.remove(idx);
                        st = Status.SUCCESS;
                    }
                } else st = Status.FAILURE;
            }
            return st;
        } finally {
            getwLock().unlock();
        }
    }

    @Override
    Status borrow(Node<K, V> sib, int loc, boolean isLeft) {
        if (isLeft) {
            int idx = sib.keys.size() - 1;
            keys.add(0, sib.keys.get(idx));
            vals.add(0, sib.vals.get(idx));
            sib.keys.remove(idx);
            sib.vals.remove(idx);

            parent.keys.set(loc - 1, sib.keys.get(sib.keys.size() - 1));
        } else {
            keys.add(sib.keys.get(0));
            vals.add(sib.vals.get(0));
            sib.keys.remove(0);
            sib.vals.remove(0);

            parent.keys.set(loc, keys.get(keys.size() - 1));
        }
        return Status.SUCCESS;
    }

    @Override
    Status merge(Node<K, V> sib, int loc, boolean isLeft) {
        if (isLeft) {
            sib.keys.addAll(keys);
            sib.vals.addAll(vals);
            sib.next = next;

            parent.keys.remove(loc - 1);
            parent.children.remove(loc);
        } else {
            keys.addAll(sib.keys);
            vals.addAll(sib.vals);
            next = sib.next;

            parent.keys.remove(loc);
            parent.children.remove(loc + 1);
        }
        return Status.SUCCESS;
    }


    // 闭区间，同时取到start和end对应的值
    void getList(K start, K end, Leaf<K, V> preLeaf, List<V> res, int seq) {
        int from, to;
        if (seq == 0) {
            from = getIdx(start);   // 在start为重复键时应取到对应的第一个val
        } else {
            getrLock().lock();
            preLeaf.getrLock().unlock();
            from = 0;
        }
        to = from;
        while (to < keys.size() && keys.get(to).compareTo(end) <= 0) {  // 在end为重复键时应取到对应的最后一个val
            res.add(vals.get(to++));
        }

        if (to >= vals.size() && next != null) {
            ((Leaf<K, V>) next).getList(start, end, this, res, seq + 1);
        } else {
            getrLock().unlock();
        }
    }
}
