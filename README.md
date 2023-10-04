## SQL Usage
Before using, here are a few points that you should know:
1. All SQL statements are not case-sensitive, meaning both `create schema` and `CREATE SCHEMA` work.
2. You should create a folder named `DB` in the root folder of this project, which is where the data are stored.
3. In this document, {datatype} represents a variable with type of "datatype".

Currently supported SQL statements are as listed below:

### CREATE
#### SCHEMA
```
CREATE SCHEMA schema_name;
```

Remember: Switching `SCHEMA` to `DATABASE` won't work. This also applies elsewhere like DROP statements.

#### TABLE
```
CREATE TABLE schema_name.table_name (
    id int,
    col1_name char,
    ...
    coln_name char
);
```

Notice that you have to manually name the first column as `id`, which will be assigned as the primary key.

More complicated create table statements are not currently supported, for example:
```
CREATE TABLE CUSTOMERS(
   ID          INT NOT NULL,
   NAME        VARCHAR (20) NOT NULL,
   AGE         INT NOT NULL,
   ADDRESS     CHAR (25),
   SALARY      DECIMAL (18, 2),
   PRIMARY KEY (ID)
);
```

#### INDEX
```
CREATE INDEX index_name
ON schema_name.table_name (column_name);
```

A few things to be noticed:
1. Combination of different columns are not currently supported.
2. Though after creating index retrieving data will be faster, yet updating will take longer.
3. The `index_name` should be identical to `column_name`, otherwise you won't get the boost in query efficiency since at least for now the execution plan in my implementation is not complicated.
4. `index_name` must not be empty.

### SELECT
```
SELECT column1, column2, columnN
FROM schema_name.table_name;
```

1. You can only specify one table in standard select-from statement. Otherwise, only the first table will be recognized.
2. Currently, you should specify the schema containing this table, because I haven't implemented USE SCHEMA statement.

```
SELECT * FROM schma_name.table_name;
```

```
SELECT column1, column2, ...
FROM schema_name.table_name
WHERE condition;
```

Currently supported conditions are as follows:
1. id = {number}
    - The number of spaces wrapping `=` doesn't matter.

Notice that having multiple conditions in `WHERE` clause is not currently supported.

### INSERT
```
INSERT INTO schema_name.table_name (column1, column2, column3,...columnN)
VALUES (value1, value2, value3,...valueN);
```

Here are a few points to be noticed:
1. You should specify all the columns and values, and in correct sequence.
2. The `schema_name` part is necessary, for it specifies which schema the table belongs to.
3. If a value belongs to type "char", then you should enclose it with single quotes.

### DELETE
```
DELETE FROM schema_name.table_name WHERE condition;
```

Note that the supported `WHERE` clauses in `DELETE` statements are in accordance with those in `SELECT`.
