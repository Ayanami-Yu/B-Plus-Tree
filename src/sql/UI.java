package sql;

import sql.parser.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.lang.System.out;

public class UI {

    public static void printPrompt() { out.print("> "); }

    public static void printError() { out.println("Syntax error"); }

    public static void main(String[] args) {
        Initializer.openSchemata();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                printPrompt();
                StringBuilder sql = new StringBuilder();
                char c;
                while ((c = (char) br.read()) != ';') {      // 分号标志语句结束
                    sql.append(c);
                }
                if (sql.toString().toLowerCase().contains("quit")) break;

                Parser.parse(sql.toString());                // 解析之后调用对应方法
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Finalizer.saveDataOnDisk();
        }
    }
}
