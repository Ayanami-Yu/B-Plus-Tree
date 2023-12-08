package phonebook;

import sql.parser.Parser;

import java.util.Scanner;

import static java.lang.System.out;
import static phonebook.PhoneBook.pk;

public class Utils {
    static Scanner sc = new Scanner(System.in);
    private static final String schemaName = "phone_book", tableName = "dir";

    static void find() {
        out.println("Enter the name in one line:");
        String name = sc.nextLine();

        String sql = String.format("""
                SELECT name, number, addr
                FROM phone_book.dir
                WHERE name = '%s';
                """, name);
        Parser.parse(sql);
    }

    static void add() {
        out.println("Enter the name in one line:");
        String name = sc.nextLine();
        out.println("Enter the phone number:");
        String number = sc.nextLine();
        out.println("Enter the address:");
        String addr = sc.nextLine();

        delRecord(name);    // 若记录已存在则先删除再插入

        String sql = String.format("""
                insert into phone_book.dir (id, name, number, addr)
                values (%d, '%s', '%s', '%s');
                """, pk, name, number, addr);
        Parser.parse(sql);
    }

    static void delRecord(String colVal) {
        if (Parser.exists(schemaName, tableName, "name", colVal)) {
            Parser.remove(schemaName, tableName, "name", colVal);
        }
    }

    static void remove() {
        out.println("Enter the name to be deleted:");
        String name = sc.nextLine();

        String sql = String.format("""
                DELETE FROM phone_book.dir WHERE name = '%s';
                """, name);
        Parser.parse(sql);
    }
}
