## SQL Usage
Before using, here are a few points that you should know:
1. All SQL statements are not case-sensitive, meaning both "create schema" and "CREATE SCHEMA" work.
2. You should create a folder named "DB" in the root folder of this project, which is where the data are stored.

Currently supported SQL statements are as listed below:

### CREATE
```
CREATE SCHEMA schema_name;
```

Remember: Switching "SCHEMA" to "DATABASE" won't work. This also applies elsewhere like DROP statements.

```
CREATE TABLE schema_name.table_name (
    id int,
    col1_name char,
    ...
    coln_name char
);
```

Notice that you have to manually name the first column as "id", which will be assigned as the primary key.

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

### INSERT
```
INSERT INTO schema_name.table_name (column1, column2, column3,...columnN)
VALUES (value1, value2, value3,...valueN);
```

Here are a few points to be noticed:
1. You should specify all the columns and values, and in correct sequence.
2. The `schema_name` part is necessary, for it specifies which schema the table belongs to.
3. If a value belongs to type "char", then you should enclose it with single quotes.
