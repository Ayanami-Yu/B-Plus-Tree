package sql.parser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import sql.Page;
import sql.Schema;
import sql.Table;
import sql.UI;

import java.util.HashMap;
import java.util.Map;

import static sql.parser.Create.createSchema;
import static sql.parser.Create.createTable;

public class Parser {
    public final static String path = "./DB/";
    public static Map<String, Schema> Schemata = new HashMap<>();

    public static void parse(String sql) {
        try {
            Statement stmt = CCJSqlParserUtil.parse(sql);
            String[] stmtType = stmt.toString().split(" ", 3);
            switch (stmtType[0]) {
                case "CREATE" -> {
                    switch (stmtType[1]) {
                        case "SCHEMA" -> createSchema(sql);
                        case "TABLE" -> createTable(sql);
                    }
                }
                case "INSERT" -> insertInto(sql);
                default -> UI.printError();
            }
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
    }

    static void insertInto(String sql) {
        try {
            Insert insert = (Insert) CCJSqlParserUtil.parse(sql);
            Schema schema = Schemata.get(insert.getTable().getSchemaName());
            if (schema == null) {
                System.out.println("Schema doesn't exist");
                return;
            }
            Table table = schema.tables.get(insert.getTable().getName());
            if (table == null) {
                System.out.println("Table doesn't exist");
                return;
            }
            Page page = new Page(insert);
            table.insert(page);

        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
    }
}
