package concurrent;

import java.util.List;

public enum Status {SUCCESS, FAILURE, RETRY, NEW_ROOT, NOT_EXIST, SINGLE_ROOT, EMPTY}

class Info<V> {
    Status st;
    V val;
    List<V> vals;
    public Info(Status st, V val) {
        this.st = st;
        this.val = val;
    }

    public Info(Status st, List<V> vals) {
        this.st = st;
        this.vals = vals;
    }
}
