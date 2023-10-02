package sql;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static sql.parser.Parser.schemata;

public class Schema {
    public String name;
    public Map<String, Table> tables;

    public Schema(String name) {
        this.name = name;
        tables = new HashMap<>(); // tables会在Initializer中加入
    }

    public static Schema getSchema(String schemaName) throws SQLException {
        Schema schema = schemata.get(schemaName);
        if (schema == null) {
            throw new SQLException("Schema doesn't exist");
        }
        return schema;
    }
}
