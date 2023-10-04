package sql;

import net.sf.jsqlparser.expression.Expression;
import sql.parser.Parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import static java.lang.System.out;
import static sql.parser.Parser.path;
import static sql.parser.Parser.schemata;

public class Finalizer {

    static void saveDataOnDisk() {
        schemata.forEach((schemaName, schema) -> saveDataInSchema(schema));
        out.println("Data successfully saved on disk");
    }

    static void saveDataInSchema(Schema schema) {
        schema.tables.forEach((tableName, table) -> {
            File file = new File(path + schema.name + "/" + tableName);

            // 注意是重写磁盘上存储数据的文件
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                List<Page> pages = table.tree.getRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
                for (Page page : pages) {
                    for (String attr : page.attrs) {
                        bw.write(attr);
                        bw.newLine();
                    }
                    bw.write(";");
                    bw.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
