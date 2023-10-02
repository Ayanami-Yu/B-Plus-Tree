package sql.parser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.insert.Insert;
import sql.Page;
import sql.Schema;
import sql.Table;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;

import static java.lang.System.out;
import static sql.parser.Parser.path;
import static sql.parser.Parser.schemata;

public class Insertion {
    static void insertInto(String sql) {
        try {
            Insert insert = (Insert) CCJSqlParserUtil.parse(sql);
            Schema schema = Schema.getSchema(insert.getTable().getSchemaName());
            Table table = Table.getTable(schema, insert.getTable().getName());

            Page page = new Page(insert);
            table.tree.insert(page.getID(), page);
            insertIntoDisk(insert);

            out.println("Record successfully inserted");
        } catch (JSQLParserException | SQLException e) {
            e.printStackTrace();
        }
    }



    static void insertIntoDisk(Insert insert) {
        String schemaName = insert.getTable().getSchemaName(),
                tableName = insert.getTable().getName();
        File data = new File(path + schemaName + "/" + tableName);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(data, true))) {

            // 每行为一个记录的一条属性，记录之间以分号分隔
            for (Expression val : insert.getValues().getExpressions()) {
                bw.write(val.toString());
                bw.newLine();
            }
            bw.write(";");
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
