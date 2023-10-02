package sql.parser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Select;

public class Selection {
    static void selectFrom(String sql) {
        try {
            Select select = (Select) CCJSqlParserUtil.parse(sql);


        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
    }
}
