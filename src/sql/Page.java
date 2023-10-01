package sql;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.insert.Insert;

import java.util.ArrayList;
import java.util.List;

public class Page {
    List<Expression> attrs;

    public Page(Insert insert) {
        attrs = new ArrayList<>();
        attrs.addAll(insert.getValues().getExpressions());
    }

    int getPK() {
        return Integer.parseInt(attrs.get(0).toString());
    }
}
