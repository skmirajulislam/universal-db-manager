package com;

import com.service.postgress.PostSignalActivator;
import com.service.sql.MySqlSignalActivator;
import com.service.sqllite.LiteSignalActivator;
import com.ui.DatabaseManagerUI;

import java.util.Scanner;

public class Main {

    // Explicitly register all JDBC drivers at class-load time.
    // jpackage bundles a custom JRE (jlink) that runs in module context;
    // DriverManager's ServiceLoader won't scan unnamed-module classpath JARs
    // automatically, so Class.forName() is required to trigger each driver's
    // static DriverManager.registerDriver() call.
    static {
        try {
            Class.forName("org.postgresql.Driver");
            Class.forName("com.mysql.cj.jdbc.Driver");
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // Single shared Scanner for System.in — never closed because closing it
    // would permanently close System.in for the entire JVM session.
    private static final Scanner STDIN = new Scanner(System.in);

    public static void main(String[] args) {
        boolean cliMode = args.length > 0 && args[0].equalsIgnoreCase("--cli");
        if (cliMode) {
            launchCLI();
        } else {
            DatabaseManagerUI.launch();
        }
    }

    private static void launchCLI() {
        System.out.println("Welcome to the Database Manager (CLI Mode)!");
        System.out.println("Please select a database to connect to:");
        System.out.println("  postgres");
        System.out.println("  mysql");
        System.out.println("  sqlite");
        System.out.print("Enter your choice: ");

        String database = System.console() != null
                ? System.console().readLine().trim()
                : STDIN.nextLine().trim();

        switch (database) {
            case "postgres" -> PostSignalActivator.PostActivator();
            case "mysql"    -> MySqlSignalActivator.MySQlActivator();
            case "sqlite"   -> LiteSignalActivator.LiteActivator();
            default         -> System.out.println("Invalid choice. Valid options: postgres, mysql, sqlite");
        }
    }
}
