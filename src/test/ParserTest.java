package test;

import org.junit.jupiter.api.Test;
import sql.UI;

public class ParserTest {
    @Test
    void testParser() {
        String s1 = """
                   CREATE TABLE test.Persons (
                       PersonID int,
                       LastName varchar(255),
                       FirstName varchar(255),
                       Address varchar(255),
                       City varchar(255)
                   );""";
        String s2 = """
                CREATE TABLE test.singers (
                	id int,
                	name char,
                	genre char,
                	primary key (id)
                );""";
        String s3 = """
                create table test.albums (
                    id int,
                    title char,
                    artist char,
                    date char,
                    primary key (id)
                );""";

        UI.main(null);
    }
}
