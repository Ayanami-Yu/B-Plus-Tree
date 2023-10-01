package sql;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import sql.parser.Create;
import sql.parser.Parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Initializer {
    static void openSchemata() {
        File db = new File(Parser.path);
        File[] schemata = db.listFiles();
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
            Parser.Schemata.put(schemaName, new Schema(schemaName));
            System.out.println("Successfully opened " + schemaName);
            openTables(dir);
        } else {
            System.out.println("Failed to open " + schemaName);
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
                        loadTableOnSchema(String.valueOf(sql));
                    } catch (IOException e) {
                        System.out.println("Failed to load " + table.getName());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    static void loadTableOnSchema(String sql) {
        try {
            CreateTable stmt = (CreateTable) CCJSqlParserUtil.parse(sql);
            Table table = Create.generateTable(stmt);
            String schemaName = stmt.getTable().getSchemaName();

            Create.addTableToSchema(table, schemaName);
            System.out.println("Table " + stmt.getTable().getName() + " loaded");
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
    }
}
