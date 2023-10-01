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
import java.util.LinkedHashMap;
import java.util.Map;

import static sql.parser.Parser.schemata;
import static sql.parser.Parser.path;
import static java.lang.System.out;

public class Creation {
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
            schemata.put(schema.name, schema);
            out.println("Schema " + schema.name + " created");
        } else {
            out.println("Failed to create " + schema.name);
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

    // 根据create table语句创建一个树为空的Table对象
    public static Table generateTable(CreateTable table) {
        Map<String, String> cols = new LinkedHashMap<>();
        String name = table.getTable().getName();

        for (ColumnDefinition col : table.getColumnDefinitions()) {
            cols.put(col.getColumnName(), col.getColDataType().toString());
        }
        return new Table(name, cols);
    }

    static void createTableOnDisk(Table table, String sql, String schemaName) {
        File dict = new File(path + schemaName + "/" + table.name + ".dict"); // 存放table的元数据
        File data = new File(path + schemaName + "/" + table.name);
        try {
            if (dict.createNewFile() && data.createNewFile()) {
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(dict))) {
                    bw.write(sql); // 便于再次读入时使用JSQLParser
                    bw.newLine();
                }
                out.println("Table " + table.name + " created");
                addTableToSchema(table, schemaName);
            } else {
                out.println("Table " + table.name + " already exists");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addTableToSchema(Table table, String schemaName) {
        schemata.get(schemaName).tables.put(table.name, table);
    }
}
