package sql;

import concurrent.Tree;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;

import java.util.Map;

public class Table {
    final int factor = 5;
    public String name;
    Map<String, ColumnDefinition> cols;
    Tree<Integer, Page> tree;

    public Table(String name, Map<String, ColumnDefinition> cols) {
        this.name = name;
        this.cols = cols;
        tree = new Tree<>(factor);
    }

    public void insert(Page page) {
        int key = page.getPK();
    }
}
