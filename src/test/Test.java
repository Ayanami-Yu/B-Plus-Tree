package test;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.insert.Insert;

public class Test {
    public static void main(String[] args) throws JSQLParserException {
        sqlInsert();
    }

    static void sqlCreate() throws JSQLParserException {
        String s1 = """
                CREATE TABLE test.singers (
                	id int,
                	name char,
                	genre char,
                	primary key (id)
                );""";
        CreateTable table = (CreateTable) CCJSqlParserUtil.parse(s1);
        for (ColumnDefinition col : table.getColumnDefinitions()) {
            System.out.println(col.getColumnName());
            System.out.println(col.getColDataType());
        }
        System.out.println();
        for (Index index : table.getIndexes()) {
            System.out.println(index.getName());
            System.out.println(index.getType());
            System.out.println(index.getColumnsNames());
            System.out.println(index.getColumnsNames().get(0));
        }
    }

    static void sqlInsert() throws JSQLParserException {
        String s2 = """
                insert into test.singers (id, name, genre)
                values (1, 'David Bowie', 'Glam');
                """;
        Insert insert = (Insert) CCJSqlParserUtil.parse(s2);
        System.out.println(insert.getColumns());
        System.out.println(insert.getValues());
        System.out.println(insert.getValues().getExpressions().get(1).getClass());
        System.out.println(insert.getValues().getExpressions().get(1));

        Expression expr = insert.getValues().getExpressions().get(0);
        System.out.println(expr instanceof LongValue);
        int id = Integer.parseInt(expr.toString());
        System.out.println(id);
    }
}
