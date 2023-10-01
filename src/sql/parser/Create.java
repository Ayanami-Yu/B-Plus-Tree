package sql.parser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.schema.CreateSchema;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import sql.Schema;
import sql.Table;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static sql.parser.Parser.Schemata;
import static sql.parser.Parser.path;

public class Create {
    static void createSchema(String sql) {
        try {
            CreateSchema schema = (CreateSchema) CCJSqlParserUtil.parse(sql);
            String name = schema.getSchemaName();
            createSchemaOnDisk(new Schema(name));

        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
    }

    static void createSchemaOnDisk(Schema schema) {
        File dir = new File(path + schema.name);
        if (dir.mkdir()) {
            Schemata.put(schema.name, schema);
            System.out.println("Schema " + schema.name + " created");
        } else {
            System.out.println("Failed to create " + schema.name);
        }
    }

    static void createTable(String sql) {
        try {
            CreateTable stmt = (CreateTable) CCJSqlParserUtil.parse(sql);
            Table table = generateTable(stmt);
            String schemaName = stmt.getTable().getSchemaName();
            createTableOnDisk(table, sql, schemaName);

        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
    }

    public static Table generateTable(CreateTable table) {
        Map<String, ColumnDefinition> cols = new HashMap<>();
        String name = table.getTable().getName();

        for (ColumnDefinition col : table.getColumnDefinitions()) {
            cols.put(col.getColumnName(), col);
        }
        return new Table(name, cols);
    }

    static void createTableOnDisk(Table table, String sql, String schemaName) {
        File dict = new File(path + schemaName + "/" + table.name + ".dict");
        File data = new File(path + schemaName + "/" + table.name);
        try {
            if (dict.createNewFile() && data.createNewFile()) {
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(dict))) {
                    bw.write(sql);
                    bw.newLine();
                }
                System.out.println("Table " + table.name + " created");
                addTableToSchema(table, schemaName);
            } else {
                System.out.println("Table " + table.name + " already exists");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addTableToSchema(Table table, String schemaName) {
        Schemata.get(schemaName).tables.put(table.name, table);
    }
}
