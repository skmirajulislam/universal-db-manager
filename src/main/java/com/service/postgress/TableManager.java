package com.service.postgress;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

class TableManager {
    private Connection connection;
    private Validator validator;

    public TableManager(Connection connection) {
        this.connection = connection;
        this.validator = new Validator(connection);
    }

    /**
     * Helper method to check if a table exists in the database.
     */
    private boolean tableExists(String tableName) throws SQLException {
        return validator.tableValidation(tableName);
    }

    public void createTable(String tableName) {
        try {
            if (tableExists(tableName)) {
                System.out.println("Notice: Table '" + tableName + "' already exists. Skipping creation.");
                return;
            }

            String sql = "CREATE TABLE " + tableName + " (id SERIAL PRIMARY KEY, name VARCHAR(100))";

            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
                System.out.println("Success: Table '" + tableName + "' created successfully.");
            }

        } catch (SQLException e) {
            System.out.println("Error handling table creation: " + e.getMessage());
        }
    }

    public void dropTable(String tableName) {
        try {
            if (!tableExists(tableName)) {
                System.out.println("Error: Cannot drop. Table '" + tableName + "' does not exist.");
                return;
            }

            // Removed "IF EXISTS" since we now guarantee it exists
            String sql = "DROP TABLE " + tableName;

            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
                System.out.println("Success: Table '" + tableName + "' dropped successfully.");
            }
        } catch (SQLException e) {
            System.out.println("Error dropping table: " + e.getMessage());
        }
    }

    public void listTables() {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet resultSet = metaData.getTables(null, null, "%", new String[] { "TABLE" })) {
                System.out.println("\n--- List of Tables in the Database ---");
                boolean found = false;
                while (resultSet.next()) {
                    String tableName = resultSet.getString("TABLE_NAME");
                    System.out.println(tableName);
                    found = true;
                }
                if (!found) {
                    System.out.println("(No tables found in the database)");
                }
                System.out.println("-------------------------------------\n");
            }
        } catch (SQLException e) {
            System.out.println("Error listing tables: " + e.getMessage());
        }
    }

    public void renameTable(String oldName, String newName) {
        try {
            if (!tableExists(oldName)) {
                System.out.println("Error: Cannot rename. Table '" + oldName + "' does not exist.");
                return;
            }
            if (tableExists(newName)) {
                System.out.println("Error: Cannot rename. A table named '" + newName + "' already exists.");
                return;
            }

            String sql = "ALTER TABLE " + oldName + " RENAME TO " + newName;

            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
                System.out.println("Success: Table '" + oldName + "' renamed to '" + newName + "' successfully.");
            }
        } catch (SQLException e) {
            System.out.println("Error renaming table: " + e.getMessage());
        }
    }

    public void truncateTable(String tableName) {
        try {
            if (!tableExists(tableName)) {
                System.out.println("Error: Cannot truncate. Table '" + tableName + "' does not exist.");
                return;
            }

            String sql = "TRUNCATE TABLE " + tableName;

            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
                System.out.println("Success: Table '" + tableName + "' truncated successfully.");
            }
        } catch (SQLException e) {
            System.out.println("Error truncating table: " + e.getMessage());
        }
    }

    public void addColumn(String tableName, String columnName, String dataType) {
        try {
            if (!tableExists(tableName)) {
                System.out.println("Error: Cannot add column. Table '" + tableName + "' does not exist.");
                return;
            }

            String sql = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + dataType;

            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
                System.out.println("Success: Column '" + columnName + "' added to table '" + tableName + "'.");
            }
        } catch (SQLException e) {
            System.out.println("Error adding column: " + e.getMessage());
        }
    }

    public void dropColumn(String tableName, String columnName) {
        try {
            if (!tableExists(tableName)) {
                System.out.println("Error: Cannot drop column. Table '" + tableName + "' does not exist.");
                return;
            }

            String sql = "ALTER TABLE " + tableName + " DROP COLUMN " + columnName;

            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
                System.out.println("Success: Column '" + columnName + "' dropped from table '" + tableName + "'.");
            }
        } catch (SQLException e) {
            System.out.println("Error dropping column: " + e.getMessage());
        }
    }

    public void renameColumn(String tableName, String oldColumnName, String newColumnName) {
        try {
            if (!tableExists(tableName)) {
                System.out.println("Error: Cannot rename column. Table '" + tableName + "' does not exist.");
                return;
            }

            String sql = "ALTER TABLE " + tableName + " RENAME COLUMN " + oldColumnName + " TO " + newColumnName;

            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
                System.out.println("Success: Column '" + oldColumnName + "' renamed to '" + newColumnName + "'.");
            }
        } catch (SQLException e) {
            System.out.println("Error renaming column: " + e.getMessage());
        }
    }

    public void listColumns(String tableName) {
        try {
            if (!tableExists(tableName)) {
                System.out.println("Error: Cannot list columns. Table '" + tableName + "' does not exist.");
                return;
            }

            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet resultSet = metaData.getColumns(null, null, tableName.toLowerCase(), "%")) {
                System.out.println("\n--- Columns in Table '" + tableName + "' ---");
                while (resultSet.next()) {
                    String columnName = resultSet.getString("COLUMN_NAME");
                    String dataType = resultSet.getString("TYPE_NAME");
                    System.out.println("- " + columnName + " (" + dataType + ")");
                }
                System.out.println("-----------------------------------------\n");
            }
        } catch (SQLException e) {
            System.out.println("Error listing columns: " + e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed successfully.");
            }
        } catch (SQLException e) {
            System.out.println("Error closing database connection: " + e.getMessage());
        }
    }
}