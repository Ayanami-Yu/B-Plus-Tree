package sql.parser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.delete.Delete;
import sql.Schema;
import sql.Table;

import java.sql.SQLException;

import static java.lang.System.out;

public class Deletion {

    // DELETE总入口
    public static void deleteFrom(String sql) {
        try {
            Delete delete = (Delete) CCJSqlParserUtil.parse(sql);

            // 注意两个get方法都已经检查了schema或table是否存在
            Schema schema = Schema.getSchema(delete.getTable().getSchemaName());
            Table table = Table.getTable(schema, delete.getTable().getName());

            if (delete.getWhere() != null) {
                table.deleteWhereFromTree(delete);
            } else {
                table.deleteAllFromTree();
            }
        } catch (JSQLParserException | SQLException e) {
            e.printStackTrace();
        }
    }
}
