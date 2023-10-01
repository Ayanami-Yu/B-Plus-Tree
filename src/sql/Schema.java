package sql;

import concurrent.Tree;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Schema {
    public String name;
    public Map<String, Table> tables;

    public Schema(String name) {
        this.name = name;
        tables = new LinkedHashMap<>(); // tables会在Initializer中加入
    }
}
