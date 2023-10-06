package sql.parser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Select;
import sql.Schema;
import sql.Table;

import java.sql.SQLException;
import java.util.List;

import static java.lang.System.out;

public class Selection {

    // SELECT的主入口，各类分支在Table类中实现
    static void selectFrom(String sql) {
        try {
            Select select = (Select) CCJSqlParserUtil.parse(sql);
            String[] fromItem = select.getPlainSelect().getFromItem().toString().split("\\.");
            if (fromItem.length < 2) {
                throw new SQLException("Both schema and table should be specified at the same time");
            }
            String schemaName = fromItem[0], tableName = fromItem[1];

            Schema schema = Schema.getSchema(schemaName);
            Table table = Table.getTable(schema, tableName);
            if (table.tree.isEmpty()) {
                throw new SQLException("Table " + tableName + " is empty");
            }

            List<List<String>> res;

            if (select.getPlainSelect().getWhere() != null) { // 有WHERE限定
                res = table.selectWhereFromTree(select);
            } else {
                var items = select.getPlainSelect().getSelectItems();
                if (items.size() == 1 && items.get(0).toString().equals("*")) {
                    res = table.selectAllFromTree();
                } else {
                    res = table.selectFromTree(select);
                }
            }
            printSelection(res);
        } catch (JSQLParserException | SQLException e) {
            e.printStackTrace();
        }
    }

    static void printSelection(List<List<String>> res) {
        for (List<String> pageItems : res) {
            for (String item : pageItems) {
                out.print(item + " ");
            }
            out.println();
        }
    }
}
