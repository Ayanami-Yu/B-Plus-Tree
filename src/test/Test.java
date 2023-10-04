package test;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.UseStatement;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.Arrays;
import java.util.Iterator;

import static java.lang.System.out;

public class Test {
    public static void main(String[] args) throws JSQLParserException {
        sqlDelete();
    }

    static void sqlCreate() throws JSQLParserException {
        String s1 = """
                CREATE TABLE music.singers (
                	id int,
                	name char,
                	genre char
                );""";
        CreateTable table = (CreateTable) CCJSqlParserUtil.parse(s1);
        for (ColumnDefinition col : table.getColumnDefinitions()) {
            out.println(col.getColumnName()); // id
            out.println(col.getColDataType()); // int
        }
        out.println();
        for (Index index : table.getIndexes()) {
            out.println(index.getName()); // null
            out.println(index.getType()); // primary key
            out.println(index.getColumnsNames()); // [id]
            out.println(index.getColumnsNames().get(0)); // id
        }
    }

    static void sqlInsert() throws JSQLParserException {
        String s2 = """
                insert into music.singers (id, name, genre)
                values (1, 'David Bowie', 'Glam');
                """;
        Insert insert = (Insert) CCJSqlParserUtil.parse(s2);
        out.println(insert.getColumns().get(0)); // id
        out.println(insert.getColumns().size()); // 3
        out.println(insert.getValues().getExpressions().size()); // 3
        out.println(insert.getColumns()); // id, name, genre
        out.println(insert.getValues()); // VALUES (1, 'David Bowie', 'Glam')
        out.println(insert.getValues().getExpressions().get(1)); // 'David Bowie'
        out.println(insert.getValues().getExpressions()); // (1, 'David Bowie', 'Glam')

        Expression expr = insert.getValues().getExpressions().get(0);
        // expr super class -> Expression
        out.println(expr instanceof LongValue); // true
        int id = Integer.parseInt(expr.toString());
        out.println(id); // 1
    }

    static void sqlDrop() throws JSQLParserException {
        String s = """
                drop table music.singers;
                """;
        Drop drop = (Drop) CCJSqlParserUtil.parse(s);
        out.println(drop.getType()); // table
        out.println(drop.getParameters()); // null
        out.println(drop.getName().getSchemaName()); // music // drop schema -> null
        out.println(drop.getName().getName()); // singers // schema -> music
        out.println(drop.getName()); // music.singers // schema -> music
    }

    static void sqlSelect() throws JSQLParserException {
        String s = """
                SELECT name, genre
                FROM music.singers
                WHERE id = 3;
                """;
        String s1 = """
                SELECT * FROM music.singers;
                """;
        Select select = (Select) CCJSqlParserUtil.parse(s);
        out.println(select.getPlainSelect().getDistinct()); // null
        out.println(select.getPlainSelect().getFirst()); // null
        out.println(select.getPlainSelect().getFromItem()); // music.singers
        out.println(select.getPlainSelect().getSelectItems()); // [name, genre] // [*]
        out.println(select.getPlainSelect().getSelectItems().get(0)); // name // *
        //out.println(select.getPlainSelect().getSelectItems().get(1)); // genre
        out.println(select.getPlainSelect().getWhere().getClass()); // null // id = 3
        Expression expr = select.getPlainSelect().getWhere();
        if (expr instanceof EqualsTo eq) {
            out.println(eq.getRightExpression());
        }
    }

    static void sqlUse() throws JSQLParserException {
        String s1 = """
                use schema music;
                """;
        UseStatement use = (UseStatement) CCJSqlParserUtil.parse(s1);
        out.println(use.getName()); // music
    }

    static void sqlIndex() throws JSQLParserException {
        String s = """
                CREATE INDEX index_name
                ON schema_name.table_name (column_name1, column_name2);
                """;
        CreateIndex createIndex = (CreateIndex) CCJSqlParserUtil.parse(s);
        out.println(createIndex.getTable().getName()); // table_name
        out.println(createIndex.getIndex().getName()); // index_name
        out.println(createIndex.getIndex().getType()); // null
        out.println(createIndex.getIndex().getIndexSpec()); // null
        out.println(createIndex.getIndex().getColumnsNames()); // [column_name1, column_name2]
        out.println(createIndex.getIndex().getColumns()); // [column_name1, column_name2]
        out.println(createIndex.getIndex().getNameParts()); // [index_name]
        out.println(createIndex.getIndex().getColumnsNames().get(0)); // column_name1
        out.println(createIndex.getTable().getSchemaName()); // schema_name
    }

    static void sqlDelete() throws JSQLParserException {
        String s = """
                DELETE FROM schema_name.table_name;
                """;
        Delete delete = (Delete) CCJSqlParserUtil.parse(s);
        out.println(delete.getTable().getSchemaName()); // schema_name
        out.println(delete.getTable().getName()); // table_name
        out.println(delete.getWhere()); // id = 2
        out.println(delete.getJoins()); // null
        Expression expr = delete.getWhere();
        if (expr instanceof EqualsTo eq) {
            out.println(eq.getLeftExpression()); // id
        }
    }
}
