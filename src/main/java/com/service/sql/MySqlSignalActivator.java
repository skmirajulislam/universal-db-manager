package com.service.sql;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;

public class MySqlSignalActivator {
    public static void MySQlActivator() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String url = "";
        String user = "";
        String password = "";

        System.out.println("=== MySQL Connection Setup ===");
        System.out.println("1. Use Default Local Database (localhost:3306/testdb)");
        System.out.println("2. Enter Custom Database Credentials");
        System.out.print("Choose an option (1 or 2): ");

        try {
            String setupChoice = reader.readLine();

            if ("2".equals(setupChoice.trim())) {
                System.out.println("\n--- Custom Connection Setup ---");
                System.out.print("Enter JDBC URL (e.g., jdbc:mysql://localhost:3306/mydb): ");
                url = reader.readLine();
                System.out.print("Enter Database Username: ");
                user = reader.readLine();
                System.out.print("Enter Database Password: ");
                password = reader.readLine();
            } else {
                System.out.println("\nUsing default local MySQL credentials...");
                // Standard local MySQL setup. Make sure you have a database named 'testdb'
                // created!
                url = "jdbc:mysql://localhost:3306/testdb";
                user = "root";
                password = "password"; // Change this to your actual local root password
            }

            System.out.println("Attempting to connect to the database...");

        } catch (Exception e) {
            System.err.println("Error reading input during setup: " + e.getMessage());
            System.exit(1);
        }

        // --- CONNECTION ATTEMPT ---
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connected to the MySQL database successfully!\n");

            TableManager tableManager = new TableManager(connection);
            DataManager dataManager = new DataManager(connection);

            // --- MAIN APPLICATION LOOP ---
            while (true) {
                System.out.println("\n=== MYSQL DB MANAGER ===");
                System.out.println("--- Table Operations (DDL) ---");
                System.out.println("1.  Create Table");
                System.out.println("2.  Drop Table");
                System.out.println("3.  List All Tables");
                System.out.println("4.  List Columns in a Table");
                System.out.println("5.  Rename Table");
                System.out.println("6.  Truncate Table");
                System.out.println("--- Basic Data Operations (DML) ---");
                System.out.println("7.  Insert Record");
                System.out.println("8.  Fetch All Records");
                System.out.println("9.  Update Record (by ID)");
                System.out.println("10. Delete Record (by ID)");
                System.out.println("11. Count Records in Table");
                System.out.println("12. Find Record by ID");
                System.out.println("--- Advanced Data Operations ---");
                System.out.println("13. Find Records by Exact Match");
                System.out.println("14. Find Records by Pattern (LIKE)");
                System.out.println("15. Find Records Greater Than (Numeric)");
                System.out.println("16. Update Records by Exact Match");
                System.out.println("17. Delete Records by Exact Match");
                System.out.println("18. Delete All Records (Clear Data)");
                System.out.println("--- Column Operations ---");
                System.out.println("19. Add Column to Table");
                System.out.println("20. Drop Column from Table");
                System.out.println("21. Rename Column");
                System.out.println("0.  Exit");
                System.out.print("Enter your choice: ");

                try {
                    String input = reader.readLine();
                    if (input == null || input.trim().isEmpty())
                        continue;

                    int choice = Integer.parseInt(input.trim());
                    String tableName, columnName, searchValue, updateColumn, updateValue;

                    switch (choice) {
                        case 1:
                            System.out.print("Enter table name to create: ");
                            tableManager.createTable(reader.readLine());
                            break;
                        case 2:
                            System.out.print("Enter table name to drop: ");
                            tableManager.dropTable(reader.readLine());
                            break;
                        case 3:
                            tableManager.listTables();
                            break;
                        case 4:
                            System.out.print("Enter table name to describe: ");
                            tableManager.listColumns(reader.readLine());
                            break;
                        case 5:
                            System.out.print("Enter current table name: ");
                            tableName = reader.readLine();
                            System.out.print("Enter new table name: ");
                            String newTableName = reader.readLine();
                            tableManager.renameTable(tableName, newTableName);
                            break;
                        case 6:
                            System.out.print("Enter table name to truncate (empty): ");
                            tableManager.truncateTable(reader.readLine());
                            break;
                        case 7:
                            System.out.print("Enter table name to insert data into: ");
                            tableName = reader.readLine();
                            LinkedHashMap<String, String> colVals = new LinkedHashMap<>();
                            try {
                                DatabaseMetaData insertMeta = connection.getMetaData();
                                try (ResultSet colRs = insertMeta.getColumns(null, null, tableName, null)) {
                                    while (colRs.next()) {
                                        String colName = colRs.getString("COLUMN_NAME");
                                        if (colName.equalsIgnoreCase("id")) continue;
                                        System.out.print("Enter value for '" + colName + "': ");
                                        colVals.put(colName, reader.readLine());
                                    }
                                }
                            } catch (Exception colEx) {
                                System.out.println("Error reading table schema: " + colEx.getMessage());
                                break;
                            }
                            if (colVals.isEmpty()) {
                                System.out.println("No columns found (or only 'id'). Cannot insert.");
                            } else {
                                dataManager.insertRecord(tableName, colVals);
                            }
                            break;
                        case 8:
                            System.out.print("Enter table name to fetch records from: ");
                            dataManager.fetchAllRecords(reader.readLine());
                            break;
                        case 9:
                            System.out.print("Enter table name: ");
                            tableName = reader.readLine();
                            System.out.print("Enter ID of record to update: ");
                            int updateId = Integer.parseInt(reader.readLine());
                            System.out.print("Enter column name to update (e.g., name, address): ");
                            String updateColName = reader.readLine();
                            System.out.print("Enter new value: ");
                            String updateVal = reader.readLine();
                            dataManager.updateRecord(tableName, updateId, updateColName, updateVal);
                            break;
                        case 10:
                            System.out.print("Enter table name: ");
                            tableName = reader.readLine();
                            System.out.print("Enter ID of record to delete: ");
                            int deleteId = Integer.parseInt(reader.readLine());
                            dataManager.deleteRecord(tableName, deleteId);
                            break;
                        case 11:
                            System.out.print("Enter table name: ");
                            dataManager.countRecords(reader.readLine());
                            break;
                        case 12:
                            System.out.print("Enter table name: ");
                            tableName = reader.readLine();
                            System.out.print("Enter ID to search for: ");
                            int searchId = Integer.parseInt(reader.readLine());
                            dataManager.findRecordById(tableName, searchId);
                            break;
                        case 13:
                            System.out.print("Enter table name: ");
                            tableName = reader.readLine();
                            System.out.print("Enter column name to search in (e.g., name): ");
                            columnName = reader.readLine();
                            System.out.print("Enter exact value to match: ");
                            searchValue = reader.readLine();
                            dataManager.findRecordsByAnyData(tableName, columnName, searchValue);
                            break;
                        case 14:
                            System.out.print("Enter table name: ");
                            tableName = reader.readLine();
                            System.out.print("Enter column name to search in (e.g., name): ");
                            columnName = reader.readLine();
                            System.out.print("Enter pattern to match (use % as wildcard, e.g., A%): ");
                            String pattern = reader.readLine();
                            dataManager.findRecordsByAnyDataLike(tableName, columnName, pattern);
                            break;
                        case 15:
                            System.out.print("Enter table name: ");
                            tableName = reader.readLine();
                            System.out.print("Enter numeric column name (e.g., id): ");
                            columnName = reader.readLine();
                            System.out.print("Enter integer value to find records greater than: ");
                            int greaterValue = Integer.parseInt(reader.readLine());
                            dataManager.findRecordsByAnyDataGreaterThan(tableName, columnName, greaterValue);
                            break;
                        case 16:
                            System.out.print("Enter table name: ");
                            tableName = reader.readLine();
                            System.out.print("Enter column name to search by (e.g., name): ");
                            String searchCol = reader.readLine();
                            System.out.print("Enter value to search for: ");
                            String searchVal = reader.readLine();
                            System.out.print("Enter column name to update (e.g., name): ");
                            updateColumn = reader.readLine();
                            System.out.print("Enter the new value: ");
                            updateValue = reader.readLine();
                            dataManager.updateRecordsByAnyData(tableName, searchCol, searchVal, updateColumn,
                                    updateValue);
                            break;
                        case 17:
                            System.out.print("Enter table name: ");
                            tableName = reader.readLine();
                            System.out.print("Enter column name to match for deletion: ");
                            columnName = reader.readLine();
                            System.out.print("Enter exact value to delete: ");
                            searchValue = reader.readLine();
                            dataManager.deleteRecordsByAnyData(tableName, columnName, searchValue);
                            break;
                        case 18:
                            System.out.print("Enter table name to delete ALL records from: ");
                            tableName = reader.readLine();
                            System.out.print("Are you sure? Type 'yes' to confirm: ");
                            if (reader.readLine().equalsIgnoreCase("yes")) {
                                dataManager.deleteAllRecords(tableName);
                            } else {
                                System.out.println("Operation cancelled.");
                            }
                            break;
                        case 19:
                            System.out.print("Enter table name: ");
                            tableName = reader.readLine();
                            System.out.print("Enter column name to add: ");
                            columnName = reader.readLine();
                            System.out.print("Enter data type (e.g., VARCHAR(100), INT, TEXT): ");
                            String addColType = reader.readLine();
                            tableManager.addColumn(tableName, columnName, addColType);
                            break;
                        case 20:
                            System.out.print("Enter table name: ");
                            tableName = reader.readLine();
                            System.out.print("Enter column name to drop: ");
                            columnName = reader.readLine();
                            tableManager.dropColumn(tableName, columnName);
                            break;
                        case 21:
                            System.out.print("Enter table name: ");
                            tableName = reader.readLine();
                            System.out.print("Enter current column name: ");
                            String oldColName = reader.readLine();
                            System.out.print("Enter new column name: ");
                            String newColName = reader.readLine();
                            tableManager.renameColumn(tableName, oldColName, newColName);
                            break;
                        case 0:
                            System.out.println("Closing database connection...");
                            tableManager.closeConnection();
                            System.out.println("Goodbye!");
                            return;
                        default:
                            System.out.println("Invalid choice. Please enter a number from the menu.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid numeric input. Please ensure IDs and choices are numbers.");
                } catch (Exception e) {
                    System.out.println("Error reading input: " + e.getMessage());
                }
            }

        } catch (SQLException e) {
            System.err.println("\n[CRITICAL ERROR] Connection to the database failed!");
            System.err.println("Reason: " + e.getMessage());
            System.exit(1);
        }
    }
}
