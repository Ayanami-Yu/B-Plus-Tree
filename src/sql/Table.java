package sql;

import concurrent.Info;
import concurrent.Status;
import concurrent.Tree;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.Select;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.out;

public class Table {    // 一张表就是一棵以主键id为key的树
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

    public boolean isDuplicatePK(Integer id) {
        if (tree.isEmpty()) return false;
        return tree.get(id) != null;
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

        if (expr instanceof EqualsTo eq) {    // WHERE colName = colVal
            String colName = eq.getLeftExpression().toString();
            String colVal = eq.getRightExpression().toString();

            if (checkPK(colName)) {

                // 主键限定为Integer
                res.add(selEqualFromTree(select, Integer.parseInt(colVal)));
            } else {
                res = selEqualFromSecTrees(select, colName, colVal);
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
        if (!tree.isEmpty()) {
            List<Page> pages = tree.getRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
            pages.forEach(page -> secTree.insert(page.attrs.get(colIdx), page.getID()));
        }

        // 若树为空secTree也为空
        secTrees.put(idxName, secTree);
    }


    // 利用垃圾回收
    public void deleteAllFromTree() {
        tree = new Tree<>(FACTOR);
        secTrees.forEach((key, secTree) -> new Tree<>(FACTOR));
        out.println("All records in table " + name + " deleted");
    }

    public void deleteWhereFromTree(Delete delete) throws SQLException {
        Expression expr = delete.getWhere();
        if (expr instanceof EqualsTo eq) {
            if (tree.isEmpty()) {
                throw new SQLException("Table " + name + " is empty");
            }
            String colName = eq.getLeftExpression().toString();
            String colVal = eq.getRightExpression().toString();

            if (checkPK(colName)) {    // 若删除的是主键
                Info<Page> info = tree.delete(Integer.parseInt(colVal));    // colVal为id的值
                if (info.st == Status.NOT_EXIST) {
                    throw new SQLException("No records matched");
                }

                Page delPage = info.val;
                Map<String, String> pageCols = new HashMap<>();

                // 为page的每个非主键属性生成 (colName, colVal) 键值对
                cols.forEach((key, idx) -> pageCols.put(key, delPage.attrs.get(idx)));

                // 由page的属性更新所有副键树
                pageCols.forEach((key, val) -> {
                    if (secTrees.containsKey(key)) {
                        secTrees.get(key).delete(val, delPage.getID());    // todo Unit Test NOT_EXIST
                    }
                });
                out.println("Page " + delPage + "deleted");
            } else {
                if (secTrees.containsKey(colName)) {    // 若删除的是其他列则最高效的方式是先回表
                    List<Integer> ids = new ArrayList<>();
                    Info<Integer> info;
                    while (true) {
                        info = secTrees.get(colName).delete(colVal);
                        if (info.st == Status.SUCCESS) {
                            ids.add(info.val);    // 加入符合colVal的所有主键
                        } else break;
                    }
                    if (ids.isEmpty()) {
                        throw new SQLException("No records matched");
                    }

                    // 将删除的条目都加入到pages中
                    List<Page> delPages = new ArrayList<>();
                    ids.forEach(id -> delPages.add(tree.delete(id).val));

                    // 删除每个副键树中对应的条目
                    secTrees.forEach((key, secTree) -> {
                        Integer idx = cols.get(key);
                        delPages.forEach(page -> secTree.delete(page.attrs.get(idx), page.getID()));
                    });
                    delPages.forEach(delPage -> out.println("Page " + delPage + "deleted"));

                } else {    // 若没有为colName对应的列建立索引
                    List<Page> delPages = new ArrayList<>();
                    List<Page> pages = tree.getRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
                    Integer colIdx = getColIdx(colName);

                    pages.forEach(page -> {
                        if (page.attrs.get(colIdx).equals(colVal)) {
                            delPages.add(tree.delete(page.getID()).val);    // id不会重复
                        }
                    });
                    if (delPages.isEmpty()) {
                        throw new SQLException("No records matched");
                    }

                    secTrees.forEach((key, secTree) -> {
                        Integer idx = cols.get(key);
                        delPages.forEach(page -> secTree.delete(page.attrs.get(idx), page.getID()));
                    });
                    delPages.forEach(delPage -> out.println("Page " + delPage + "deleted"));
                }
            }
        } else {
            throw new SQLException("The type of WHERE clause is not currently supported");
        }
    }
}
