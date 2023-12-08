package phonebook;

import sql.Finalizer;
import sql.Initializer;
import sql.parser.Parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import static java.lang.System.out;
import static phonebook.Utils.*;

public class PhoneBook {
    // 存储最大主键值以实现主键自增
    private static final File path = new File("./DB/phone_book/dir.pk");
    static Integer pk;

    public static void prompt() {
        out.println("""
                \nChoose an operation:
                1) find       2) add
                3) remove     4) quit
                """);
    }

    static void init() {    // 先创建好name的副键索引
        String sql = """
                CREATE INDEX name
                ON phone_book.dir (name);
                """;
        Parser.parse(sql);

        try {
            if (path.createNewFile()) {    // 初次使用电话本
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
                    bw.write("0");     // 无记录则最大主键值为0
                }
                pk = 0;
            } else {
                try (Scanner sc = new Scanner(path)) {
                    pk = sc.nextInt();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void updatePK() {    // 更新最大主键值
        if (pk != 0) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
                bw.write(pk.toString());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Initializer.openSchemata();
        init();

        Scanner sc = new Scanner(System.in);

        boolean b = true;
        while (b) {
            prompt();
            switch (sc.nextInt()) {
                case 1 -> find();
                case 2 -> {
                    add();
                    pk++;              // 主键自增的逻辑由PhoneBook维护
                }
                case 3 -> remove();    // todo Adjust existing PKs
                case 4 -> b = false;

                default -> out.println("Please enter a number on the menu");
            }
        }

        Finalizer.saveDataOnDisk();
        updatePK();
    }
}
