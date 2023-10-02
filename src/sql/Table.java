package sql;

import concurrent.Tree;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Table { // 一张表就是一棵以主键id为key的树
    static final int FACTOR = 5;
    public String name;
    Map<String, Integer> cols; // (name, index)
    public Tree<Integer, Page> tree;

    public Table(String name, Map<String, Integer> cols) {
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
            e.printStackTrace();
        }
    }

    public static Table getTable(Schema schema, String tableName) throws SQLException {
        Table table = schema.tables.get(tableName);
        if (table == null) {
            throw new SQLException("Table doesn't exist");
        }
        return table;
    }

    public List<List<String>> selectFromTree(Select select) {
        List<List<String>> res = new ArrayList<>();
        List<String> colNames = new ArrayList<>();

        select.getPlainSelect().getSelectItems().forEach(colName -> {
            colNames.add(colName.toString());
        });
        List<Integer> colIndices = new ArrayList<>(); // 各个column在page的属性中对应的下标
        colNames.forEach(colName -> {
            colIndices.add(cols.get(colName));
        });

        // 没有WHERE限制时获取的是所有叶子
        List<Page> pages = tree.getRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
        for (Page page : pages) {
            List<String> items = new ArrayList<>();
            for (Integer idx : colIndices) {
                items.add(page.attrs.get(idx));
            }
            res.add(items);
        }

        return res;
    }

    public List<List<String>> selectAllFromTree() {
        List<List<String>> res = new ArrayList<>();
        List<Page> pages = tree.getRange(Integer.MIN_VALUE, Integer.MAX_VALUE);

        for (Page page : pages) {
            List<String> items = new ArrayList<>(page.attrs);
            res.add(items);
        }

        return res;
    }
}
