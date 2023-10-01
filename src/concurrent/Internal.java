package concurrent;

import java.util.ArrayList;

public class Internal<K extends Comparable<? super K>, V> extends Node<K, V> {
    Internal(int factor, Tree<K, V> tree) {
        this.factor = factor;
        this.tree = tree;
        keys = new ArrayList<>();
        children = new ArrayList<>();
    }

    @Override
    Status optimisticInsert(K key, V val, int dep) {
        getrLock().lock();
        if (dep == 0 && this != tree.root.get()) {
            getrLock().unlock();
            return Status.RETRY;
        }
        if (dep != 0) parent.getrLock().unlock();

        int idx = getIdx(key);
        return children.get(idx).optimisticInsert(key, val, dep + 1);
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
                st = children.get(idx).insert(key, val, idx, dep + 1);
                if (isOverflow()) st = split(loc);
            }
            return st;
        } finally {
            if (getLock().isWriteLockedByCurrentThread())
                getwLock().unlock();
        }
    }

    @Override
    Status split(int loc) {
        getwLock().lock();
        Status st = Status.SUCCESS;
        try {
            int mid = factor / 2;
            Node<K, V> newNode = splitNode(mid);

            if (parent == null) {
                generateNewRoot();
                st = Status.NEW_ROOT;
            }
            for (Node<K, V> node : newNode.children) // 更新分裂出去的子结点的父指针
                node.parent = newNode;

            parent.keys.add(loc, keys.get(mid - 1));
            parent.children.add(loc + 1, newNode);
            newNode.parent = parent;

            if (st == Status.NEW_ROOT) tree.root.set(parent);
            return st;
        } finally {
            getwLock().unlock();
        }
    }

    @Override
    Node<K, V> splitNode(int mid) {
        Node<K, V> newNode = new Internal<>(factor, tree);
        newNode.keys.addAll(keys.subList(mid, keys.size())); // 分裂新结点
        newNode.children.addAll(children.subList(mid, children.size()));
        keys.subList(mid, keys.size()).clear();
        children.subList(mid, children.size()).clear();
        return newNode;
    }

    @Override
    Info<V> get(K key, int dep) {
        getrLock().lock();
        if (dep == 0 && this != tree.root.get()) {
            getrLock().unlock();
            return new Info<>(Status.RETRY, null);
        } else {
            if (dep != 0) parent.getrLock().unlock();

            int idx = getIdx(key);
            return children.get(idx).get(key, dep + 1);
        }
    }

    @Override
    Info<V> getRange(K start, K end, int dep) {
        getrLock().lock();
        if (dep == 0 && this != tree.root.get()) {
            getrLock().unlock();
            return new Info<>(Status.RETRY, null);
        } else {
            if (dep != 0) parent.getrLock().unlock();

            int idx = getIdx(start);
            return children.get(idx).getRange(start, end, dep + 1);
        }
    }

    @Override
    Status delete(K key, int loc, int dep) {
        getwLock().lock();
        try {
            Status st;
            if (dep == 0 && this != tree.root.get()) {
                getwLock().unlock();
                st = Status.RETRY;
            } else {
                if (safeForDelete()) unlockAncestors();

                int idx = getIdx(key);
                st = children.get(idx).delete(key, idx, dep + 1);
                if (dep == 0 && keys.size() == 0) { // 空的根结点
                    children.get(0).getwLock().lock();
                    try {
                        children.get(0).parent = null;
                        tree.root.set(children.get(0));
                    } finally {
                        children.get(0).getwLock().unlock();
                    }
                } else if (dep != 0 && isUnderflow()) st = redistribute(loc);
            }
            return st;
        } finally {
            if (getLock().isWriteLockedByCurrentThread())
                getwLock().unlock();
        }
    }

    @Override
    Status optimisticDelete(K key, int dep) {
        getrLock().lock();
        if (dep == 0 && this != tree.root.get()) {
            getrLock().unlock();
            return Status.RETRY;
        }
        if (dep != 0) parent.getrLock().unlock();

        int idx = getIdx(key);
        return children.get(idx).optimisticDelete(key, dep + 1);
    }

    @Override
    Status borrow(Node<K, V> sib, int loc, boolean isLeft) {
        if (isLeft) {
            int idx = sib.keys.size() - 1;
            keys.add(0, sib.keys.get(idx));
            children.add(0, sib.children.get(idx));
            sib.keys.remove(idx);
            sib.children.remove(idx);
            children.get(0).parent = this;

            parent.keys.set(loc - 1, sib.keys.get(sib.keys.size() - 1));
        } else {
            keys.add(sib.keys.get(0));
            children.add(sib.children.get(0));
            sib.keys.remove(0);
            sib.children.remove(0);
            children.get(children.size() - 1).parent = this;

            parent.keys.set(loc, keys.get(keys.size() - 1));
        }
        return Status.SUCCESS;
    }

    @Override
    Status merge(Node<K, V> sib, int loc, boolean isLeft) {
        if (isLeft) {
            for (Node<K, V> child : children)
                child.parent = sib;

            sib.keys.addAll(keys);
            sib.children.addAll(children);

            parent.keys.remove(loc - 1);
            parent.children.remove(loc);
        } else {
            for (Node<K, V> child : sib.children)
                child.parent = this;

            keys.addAll(sib.keys);
            children.addAll(sib.children);

            parent.keys.remove(loc);
            parent.children.remove(loc + 1);
        }
        return Status.SUCCESS;
    }
}
