package sql.parser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import sql.Schema;
import sql.Table;
import sql.UI;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static sql.parser.Creation.*;
import static sql.parser.Deletion.deleteFrom;
import static sql.parser.Insertion.insertInto;
import static sql.parser.Selection.selectFrom;

public class Parser {
    public final static String path = "./DB/";
    public static Map<String, Schema> schemata = new HashMap<>();

    public static void parse(String sql) {    // todo DROP
        try {
            // 关键字都会被转换为大写
            Statement stmt = CCJSqlParserUtil.parse(sql);
            String[] stmtType = stmt.toString().split(" ", 3);
            switch (stmtType[0]) {
                case "CREATE" -> {
                    switch (stmtType[1]) {
                        case "SCHEMA" -> createSchema(sql);
                        case "TABLE" -> createTable(sql);
                        case "INDEX" -> createIndex(sql);
                    }
                }
                case "INSERT" -> insertInto(sql);
                case "SELECT" -> selectFrom(sql);
                case "DELETE" -> deleteFrom(sql);

                default -> UI.printError();
            }
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查记录是否已经存在
     * @param colName 列名
     * @param colVal  指定的值
     */
    public static boolean exists(String schemaName, String tableName,
                                 String colName, String colVal) {
        Schema schema = schemata.get(schemaName);
        Table table = schema.tables.get(tableName);

        boolean ret = false;
        try {
            if (table.secTrees.containsKey(colName)) {
                if (table.secTrees.get(colName).get(colVal) != null)
                    ret = true;
            } else {
                // 该方法暂时只被PhoneBook使用
                throw new SQLException("You need to create index on this column first");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    // 确定记录存在时调用该方法
    public static void remove(String schemaName, String tableName,
                              String colName, String colVal) {
        Schema schema = schemata.get(schemaName);
        Table table = schema.tables.get(tableName);

        // 只被PhoneBook调用，此时无其他副键树
        Integer id = table.secTrees.get(colName).delete(colVal).val;
        table.tree.delete(id);
    }
}
