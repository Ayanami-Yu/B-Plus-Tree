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

public class Insertion {

    // 将记录插入树中而暂不插入磁盘
    static void insertInto(String sql) {
        try {
            Insert insert = (Insert) CCJSqlParserUtil.parse(sql);
            Schema schema = Schema.getSchema(insert.getTable().getSchemaName());
            Table table = Table.getTable(schema, insert.getTable().getName());

            Page page = new Page(insert);
            table.tree.insert(page.getID(), page);

            //insertDisk(insert);

            // 更新副键索引的树
            if (!table.secTrees.isEmpty()) {
                table.secTrees.forEach((colName, secTree) -> {
                    Integer idx = table.cols.get(colName);              // 在Page构造方法中已经校验
                    secTree.insert(page.attrs.get(idx), page.getID());  // 因而无需调用getColIdx检查
                });
            }
            out.println("Record successfully inserted");
        } catch (JSQLParserException | SQLException e) {
            e.printStackTrace();
        }
    }
}
