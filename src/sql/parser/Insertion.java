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

import static java.lang.System.out;
import static sql.parser.Parser.path;
import static sql.parser.Parser.schemata;

public class Insertion {
    static void insertInto(String sql) {
        try {
            Insert insert = (Insert) CCJSqlParserUtil.parse(sql);
            Schema schema = schemata.get(insert.getTable().getSchemaName());
            if (schema == null) {
                out.println("Schema doesn't exist");
                return;
            }
            Table table = schema.tables.get(insert.getTable().getName());
            if (table == null) {
                out.println("Table doesn't exist");
                return;
            }
            Page page = new Page(insert);

            table.tree.insert(page.getID(), page);
            insertIntoDisk(insert);
            out.println("Record successfully inserted");

        } catch (JSQLParserException | RuntimeException e) {
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
