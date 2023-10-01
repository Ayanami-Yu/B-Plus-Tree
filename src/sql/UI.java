package sql;

import sql.parser.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UI {
    public static void printPrompt() { System.out.print("> "); }

    public static void printError() { System.out.println("Syntax error"); }

    public static void main(String[] args) {
        Initializer.openSchemata();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                printPrompt();
                StringBuilder sql = new StringBuilder();
                char c;
                while ((c = (char) br.read()) != ';') {
                    sql.append(c);
                }
                if (sql.toString().equalsIgnoreCase("quit")) return;

                Parser.parse(sql.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
