package concurrent;

import java.util.List;

public class Info<V> {
    public Status st;
    public V val;
    public List<V> vals;

    public Info(Status st) {
        this.st = st;
    }

    public Info(Status st, V val) {
        this.st = st;
        this.val = val;
    }

    public Info(Status st, List<V> vals) {
        this.st = st;
        this.vals = vals;
    }
}
