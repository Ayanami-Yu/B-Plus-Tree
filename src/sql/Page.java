package sql;

import net.sf.jsqlparser.statement.insert.Insert;

import java.util.*;

import static sql.parser.Parser.schemata;

public class Page {                 // 相当于row
    public List<String> attrs;      // 仅含值而不含column名称

    public Page() {
        attrs = new ArrayList<>();
    }

    public Page(Insert insert) {    // 创建Page对象时会校验是否合法
        attrs = new ArrayList<>();
        initPage(insert);
    }

    void initPage(Insert insert) {
        int size = insert.getColumns().size();

        // 要求指定的column和value一一对应
        if (size != insert.getValues().getExpressions().size()) {
            throw new RuntimeException("The columns and values should have one-to-one correspondence");
        }

        // 用户应手动指定主键并命名为id
        if (!Objects.equals(insert.getColumns().get(0).toString(), "id")) {
            throw new RuntimeException("The first column must be named 'id'");
        }

        // insert语句应指定了所有column
        String schemaName = insert.getTable().getSchemaName(),
                tableName = insert.getTable().getName();
        Table table = schemata.get(schemaName).tables.get(tableName);
        if (!verifyPage(table, insert)) {
            throw new RuntimeException("All the columns need to be specified");
        }

        for (int i = 0; i < size; i++) {
            attrs.add(insert.getValues().getExpressions().get(i).toString());
        }
    }

    // 检查是否所有column都指定了值
    boolean verifyPage(Table table, Insert insert) {
        boolean verified = true;

        if (insert.getColumns().size() != table.cols.size()) {
            verified = false;
        } else {
            for (int i = 0; i < table.cols.size(); i++) {
                String col = insert.getColumns().get(i).toString();
                if (!table.cols.containsKey(col)) { // Table的元数据中是否含有该column
                    verified = false;
                    break;
                }
            }
        }

        return verified;
    }

    public Integer getID() {
        return Integer.parseInt(attrs.get(0));
    }
}
