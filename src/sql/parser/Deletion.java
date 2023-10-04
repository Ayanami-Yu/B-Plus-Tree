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

            Expression expr = delete.getWhere();
            if (expr != null) {
                deleteWhere(delete);
            } else {
                // todo DELETE without WHERE
                throw new SQLException("Deletion without WHERE clause is not currently supported");
            }
        } catch (JSQLParserException | SQLException e) {
            e.printStackTrace();
        }
    }

    static void deleteWhere(Delete delete) {
        try {
            // 注意两个get方法都已经检查了schema或table是否存在
            Schema schema = Schema.getSchema(delete.getTable().getSchemaName());
            Table table = Table.getTable(schema, delete.getTable().getName());

            Expression expr = delete.getWhere();
            if (expr instanceof EqualsTo eq) {
               table.delEqual(eq);
               deleteDisk(); // todo

            } else {
                throw new SQLException("The type of WHERE clause is not currently supported");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void deleteDisk() { // todo

    }
}
