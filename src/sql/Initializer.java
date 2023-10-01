package sql;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import sql.parser.Creation;
import sql.parser.Parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static java.lang.System.out;
import static sql.parser.Parser.path;

public class Initializer {
    static void openSchemata() {
        File db = new File(Parser.path);
        File[] schemata = db.listFiles(); // 打开DB文件夹下的所有子文件夹
        if (schemata != null) {
            for (File schema : schemata) {
                if (schema.isDirectory())
                    openSchema(schema.getName());
            }
        }
    }

    static void openSchema(String schemaName) {
        File dir = new File(Parser.path + schemaName);
        if (dir.exists() && dir.isDirectory()) {
            Parser.schemata.put(schemaName, new Schema(schemaName)); // 生成对象便于程序管理
            out.println("Successfully opened " + schemaName);
            openTables(dir);
        } else {
            out.println("Failed to open " + schemaName);
        }
    }

    static void openTables(File dir) {
        File[] tables = dir.listFiles();
        if (tables != null) {
            for (File table : tables) {
                if (table.getName().contains(".dict")) {
                    try (BufferedReader br = new BufferedReader(new FileReader(table))) {
                        int c;
                        StringBuilder sql = new StringBuilder();
                        while ((c = br.read()) != -1) {
                            sql.append((char) c);
                        }
                        loadTable(String.valueOf(sql)); // 创建Table对象并加入tables中
                    } catch (IOException e) {
                        out.println("Failed to load " + table.getName());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    static void loadTable(String sql) {
        try {
            CreateTable stmt = (CreateTable) CCJSqlParserUtil.parse(sql);
            // 元数据在dict文件中的存储方式是完整的SQL语句
            Table table = Creation.generateTable(stmt);

            // 将Table对象纳入schemata对象的管理中
            String schemaName = stmt.getTable().getSchemaName();
            Creation.addTableToSchema(table, schemaName);

            // 将磁盘上的记录加载到树中
            File data = new File(path + schemaName + "/" + table.name);
            table.loadDataOnTree(data);

            out.println("Table " + stmt.getTable().getName() + " loaded");
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
    }
}
