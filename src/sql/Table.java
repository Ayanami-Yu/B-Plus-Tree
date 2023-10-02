package sql;

import concurrent.Tree;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Table { // 一张表就是一棵以主键id为key的树
    static final int FACTOR = 5;
    public String name;
    Map<String, String> cols; // (name, datatype)
    public Tree<Integer, Page> tree;

    public Table(String name, Map<String, String> cols) { // LinkedHashMap
        this.name = name;
        this.cols = cols;
        tree = new Tree<>(FACTOR);
    }

    public void loadDataOnTree(File data) {
        try (BufferedReader br = new BufferedReader(new FileReader(data))) {
            String ln;
            do {
                Page page = new Page();
                while ((ln = br.readLine()) != null && !ln.equals(";")) {
                    page.attrs.add(ln);
                }
                if (ln != null) {
                    tree.insert(page.getID(), page);
                }
            } while (ln != null);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
