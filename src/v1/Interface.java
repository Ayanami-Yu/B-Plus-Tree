package v1;

import sql.Schema;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Interface {
    static Schema db;

    public static void main(String[] args) {
        repl();
    }

    static void printPrompt() { System.out.print("> "); }

    static void printError() { System.out.println("Syntax error."); }

    static void repl() {
        while (true) {
            printPrompt();
            String[] s = readInput();
            if (s[0].equals("quit")) break;
            parseSQL(s);
        }
    }

    static String[] readInput() { // 提取所有输入行的非空格部分 // todo bugs to fix
        List<String> s = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            String ln;
            while ((ln = br.readLine()) != null) {
                List<Character> word = new ArrayList<>();
                ln = ln.toLowerCase();

                for (int i = 0; i < ln.length(); i++) {
                    if (ln.charAt(i) != ' ')
                        word.add(ln.charAt(i));
                    else if (word.size() != 0) {
                        char[] wd = new char[word.size()];
                        for (int j = 0; j < word.size(); j++) {
                            wd[j] = word.get(j);
                        }
                        s.add(String.valueOf(wd));
                        word.clear();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s.toArray(new String[0]);
    }

    static void parseSQL(String[] s) {
        switch (s[0]) {
            case "create" -> {
                switch (s[1]) {
                    case "database" -> createDB(s);
                    case "table" -> createTable(s);
                    default -> printError();
                }
            }
            case "drop" -> {
                switch (s[1]) {
                    case "database" -> dropDB(s);
                    case "table" -> dropTable(s);
                    default -> printError();
                }
            }
        }
    }

    static void createDB(String[] s) {
        if (s.length != 3) {
            printError();
            return;
        }

        File dir = new File(db.name + s[2]);
        if (dir.mkdir()) System.out.println("Database created.");
        else System.out.println(s[2] + " already exists.");
    }

    static void dropDB(String[] s) {
        if (s.length != 3) {
            printError();
            return;
        }

        File dir = new File(db.name + s[2]);
        try {
            if (rmDir(dir)) System.out.println(s[2] + " deleted.");
            else System.out.println("Failed to delete.");
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }

    static boolean rmDir(File dir) throws FileNotFoundException {
        if (!dir.exists()) throw new FileNotFoundException(dir.getAbsolutePath());
        boolean ret = true;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files){
                ret = ret && rmDir(f);
            }
        }
        return ret && dir.delete();
    }

    static void createTable(String[] s) {

    }

    static void dropTable(String[] s) {

    }
}
