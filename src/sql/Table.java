package sql;

import concurrent.Tree;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.select.Select;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import static java.lang.System.out;

public class Table {             // 一张表就是一棵以主键id为key的树
    static final int FACTOR = 5;
    public String name;

    // (name, index)
    // key set顺序应与插入顺序相同
    public Map<String, Integer> cols;
    public Tree<Integer, Page> tree;
    public Map<String, Tree<String, Integer>> secTrees;

    public Table(String name, Map<String, Integer> cols) {
        this.name = name;
        this.cols = cols;
        tree = new Tree<>(FACTOR);
        secTrees = new HashMap<>();
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
        List<Integer> colIndices = getIndices(select);

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


    public List<List<String>> selectWhereFromTree(Select select) throws SQLException {
        List<List<String>> res = new ArrayList<>();
        Expression expr = select.getPlainSelect().getWhere();

        if (expr instanceof EqualsTo eq) {            // WHERE colName = val
            String colName = eq.getLeftExpression().toString();
            String val = eq.getRightExpression().toString();

            if (checkPK(colName)) {

                // 主键限定为Integer
                res.add(selEqualFromTree(select, Integer.parseInt(val)));
            } else {
                res = selEqualFromSecTrees(select, colName, val);
            }
        } else {
            throw new SQLException("The type of WHERE clause is not currently supported");
        }
        return res;
    }


    // 返回colName在Page的attrs中对应的下标
    public Integer getColIdx(String colName) throws SQLException {
        Integer idx = cols.get(colName);
        if (idx == null) {
            throw new SQLException("There is no such column in the table");
        }
        return idx;
    }

    boolean checkPK(String colName) {     // 便于增加PRIMARY KEY Constraint
        return colName.equals("id") && cols.get(colName).equals(0);
    }


    // 返回SELECT的各个column在page的属性中对应的下标
    List<Integer> getIndices(Select select) {
        List<Integer> colIndices = new ArrayList<>();

        var items = select.getPlainSelect().getSelectItems();
        if (items.size() == 1 && items.get(0).toString().equals("*")) {

            // 注意全选时应保持与插入顺序一致
            cols.forEach((colName, colIdx) -> colIndices.add(colIdx));
        } else {
            List<String> colNames = new ArrayList<>();
            items.forEach(colName -> colNames.add(colName.toString()));
            colNames.forEach(colName -> colIndices.add(cols.get(colName)));
        }
        return colIndices;
    }


    // 主键查询
    List<String> selEqualFromTree(Select select, Integer id) throws SQLException {
        List<String> res = new ArrayList<>();
        List<Integer> colIndices = getIndices(select);

        Page page = tree.get(id);
        if (page == null) {
            throw new SQLException("No records matched");
        }
        for (Integer idx : colIndices) {
            res.add(page.attrs.get(idx));
        }
        return res;
    }

    // 副键查询
    // colName即WHERE表达式左侧，colVal即右侧
    List<List<String>> selEqualFromSecTrees(Select select, String colName, String colVal)
            throws SQLException {
        List<List<String>> res = new ArrayList<>();
        List<Integer> colIndices = getIndices(select);

        // 副键的名称应与其被指定的column相同
        Tree<String, Integer> secTree = secTrees.get(colName);
        if (secTree != null) {
            List<Integer> ids = secTree.getRange(colVal, colVal);     // 可能有重复键
            for (Integer id : ids) {
                Page page = tree.get(id);
                List<String> items = new ArrayList<>();
                for (Integer idx : colIndices) {
                    items.add(page.attrs.get(idx));
                }
                res.add(items);
            }

            // 测试CREATE INDEX是否起作用
            out.println("Secondary index " + colName + " successfully used");
        } else {
            List<Page> pages = tree.getRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
            Integer colIdx = getColIdx(colName);
            for (Page page : pages) {
                if (page.attrs.get(colIdx).equals(colVal)) {
                    List<String> items = new ArrayList<>();
                    for (Integer idx : colIndices) {
                        items.add(page.attrs.get(idx));
                    }
                    res.add(items);
                }
            }
        }
        if (res.isEmpty()) {
            throw new SQLException("No records matched");
        }
        return res;
    }


    public void createSecTree(CreateIndex createIndex) throws SQLException {
        Index index = createIndex.getIndex();
        String idxName = index.getName();
        String colName = index.getColumnsNames().get(0);

        if (index.getColumnsNames().size() > 1) {
            throw new SQLException("More than one column being specified is not currently supported");
        }
        if (!idxName.equals(colName)) {
            throw new SQLException("The index name should match the column name");
        }
        if (secTrees.get(index.getName()) != null) {
            throw new SQLException("The index has already been created");
        }

        Integer colIdx = getColIdx(colName);

        Tree<String, Integer> secTree = new Tree<>(FACTOR);
        List<Page> pages = tree.getRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
        pages.forEach(page -> secTree.insert(page.attrs.get(colIdx), page.getID()));

        secTrees.put(idxName, secTree);
    }


    public void delEqual(EqualsTo eq) {
        try {
            delEqualFromTree(eq);
            delEqualFromSecTrees(eq);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

   void delEqualFromTree(EqualsTo eq) throws SQLException {
        String colName = eq.getLeftExpression().toString();
        String colVal = eq.getRightExpression().toString();
        Integer colIdx = getColIdx(colName);

    }

   void delEqualFromSecTrees(EqualsTo eq) { // todo

   }
}
