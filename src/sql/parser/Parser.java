package sql.parser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import sql.Schema;
import sql.UI;

import java.util.HashMap;
import java.util.Map;

import static sql.parser.Creation.createSchema;
import static sql.parser.Creation.createTable;
import static sql.parser.Insertion.insertInto;
import static sql.parser.Selection.selectFrom;

public class Parser {
    public final static String path = "./DB/";
    public static Map<String, Schema> schemata = new HashMap<>();

    public static void parse(String sql) { // todo drop, select
        try {

            // 关键字都会被转换为大写
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
                case "SELECT" -> selectFrom(sql);

                default -> UI.printError();
            }

        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
    }
}
