package com.service.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.StringJoiner;

class DataManager {
    private Connection connection;

    public DataManager(Connection connection) {
        this.connection = connection;
    }

    private boolean tableExists(String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getTables(null, null, tableName, new String[] { "TABLE" })) {
            return resultSet.next();
        }
    }

    private void printRow(ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int colCount = rsmd.getColumnCount();
        StringBuilder row = new StringBuilder();
        for (int i = 1; i <= colCount; i++) {
            if (i > 1)
                row.append(" | ");
            row.append(rsmd.getColumnName(i)).append(": ").append(rs.getString(i));
        }
        System.out.println(row.toString());
    }

    private PreparedStatement prepare(String sql) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setQueryTimeout(30);
        return ps;
    }

    // --- INSERT (Create) ---
    public void insertRecord(String tableName, LinkedHashMap<String, String> columnValues) {
        try {
            if (!tableExists(tableName)) {
                System.out.println("Error: Cannot insert record. Table '" + tableName + "' does not exist.");
                return;
            }
            if (columnValues.isEmpty()) {
                System.out.println("Error: No column values provided.");
                return;
            }

            StringJoiner cols = new StringJoiner(", ");
            StringJoiner placeholders = new StringJoiner(", ");
            for (String col : columnValues.keySet()) {
                cols.add(col);
                placeholders.add("?");
            }
            String sql = "INSERT INTO " + tableName + " (" + cols + ") VALUES (" + placeholders + ")";
            try (PreparedStatement pstmt = prepare(sql)) {
                int idx = 1;
                for (String val : columnValues.values()) {
                    pstmt.setString(idx++, val);
                }
                int rowsAffected = pstmt.executeUpdate();
                System.out.println("Success: Inserted " + rowsAffected + " row(s) into " + tableName + ".");
            }
        } catch (SQLException e) {
            System.out.println("Error inserting record: " + e.getMessage());
        }
    }

    // --- SELECT (Read) ---
    public void fetchAllRecords(String tableName) {
        try {
            if (!tableExists(tableName)) {
                System.out.println("Error: Cannot fetch records. Table '" + tableName + "' does not exist.");
                return;
            }

            String sql = "SELECT * FROM " + tableName;
            try (PreparedStatement pstmt = prepare(sql);
                    ResultSet rs = pstmt.executeQuery()) {

                System.out.println("\n--- Current Records in " + tableName + " ---");
                boolean found = false;
                while (rs.next()) {
                    printRow(rs);
                    found = true;
                }
                if (!found) {
                    System.out.println("(Table is empty)");
                }
                System.out.println("-------------------------------------\n");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching records: " + e.getMessage());
        }
    }

    // --- UPDATE (Update) ---
    public void updateRecord(String tableName, int id, String columnName, String newValue) {
        try {
            if (!tableExists(tableName)) {
                System.out.println("Error: Cannot update. Table '" + tableName + "' does not exist.");
                return;
            }

            String sql = "UPDATE " + tableName + " SET " + columnName + " = ? WHERE id = ?";
            try (PreparedStatement pstmt = prepare(sql)) {
                pstmt.setString(1, newValue);
                pstmt.setInt(2, id);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Success: Updated ID " + id + " set " + columnName + " = '" + newValue + "'.");
                } else {
                    System.out.println("Notice: No record found with ID " + id + " to update.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error updating record: " + e.getMessage());
        }
    }

    // --- DELETE (Delete) ---
    public void deleteRecord(String tableName, int id) {
        try {
            if (!tableExists(tableName)) {
                System.out.println("Error: Cannot delete. Table '" + tableName + "' does not exist.");
                return;
            }

            String sql = "DELETE FROM " + tableName + " WHERE id = ?";
            try (PreparedStatement pstmt = prepare(sql)) {
                pstmt.setInt(1, id);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Success: Deleted record with ID " + id + ".");
                } else {
                    System.out.println("Notice: No record found with ID " + id + " to delete.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error deleting record: " + e.getMessage());
        }
    }

    public void deleteAllRecords(String tableName) {
        try {
            if (!tableExists(tableName)) {
                System.out.println("Error: Cannot delete records. Table '" + tableName + "' does not exist.");
                return;
            }

            String sql = "DELETE FROM " + tableName;
            try (PreparedStatement pstmt = prepare(sql)) {
                int rowsAffected = pstmt.executeUpdate();
                System.out.println(
                        "Success: Deleted all records from " + tableName + ". Total rows deleted: " + rowsAffected);
            }
        } catch (SQLException e) {
            System.out.println("Error deleting all records: " + e.getMessage());
        }
    }

    public void countRecords(String tableName) {
        try {
            if (!tableExists(tableName)) {
                System.out.println("Error: Cannot count records. Table '" + tableName + "' does not exist.");
                return;
            }

            String sql = "SELECT COUNT(*) AS total FROM " + tableName;
            try (PreparedStatement pstmt = prepare(sql);
                    ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    int totalRecords = rs.getInt("total");
                    System.out.println("Total records in " + tableName + ": " + totalRecords);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error counting records: " + e.getMessage());
        }
    }

    public void findRecordById(String tableName, int id) {
        try {
            if (!tableExists(tableName)) {
                System.out.println("Error: Cannot search. Table '" + tableName + "' does not exist.");
                return;
            }

            String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
            try (PreparedStatement pstmt = prepare(sql)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Record found:");
                        printRow(rs);
                    } else {
                        System.out.println("No record found with ID " + id + ".");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding record by ID: " + e.getMessage());
        }
    }

    public void deleteRecordsByAnyData(String tableName, String columnName, String value) {
        try {
            if (!tableExists(tableName)) {
                System.out.println("Error: Cannot delete. Table '" + tableName + "' does not exist.");
                return;
            }

            String sql = "DELETE FROM " + tableName + " WHERE CAST(" + columnName + " AS CHAR) = ?";
            try (PreparedStatement pstmt = prepare(sql)) {
                pstmt.setString(1, value);
                int rowsAffected = pstmt.executeUpdate();
                System.out.println("Success: Deleted " + rowsAffected + " record(s) from " + tableName + " where "
                        + columnName + " = '" + value + "'.");
            }
        } catch (SQLException e) {
            System.out.println("Error deleting records by any data: " + e.getMessage());
        }
    }

    public void updateRecordsByAnyData(String tableName, String searchColumn, String searchValue, String updateColumn,
            String updateValue) {
        try {
            if (!tableExists(tableName)) {
                System.out.println("Error: Cannot update. Table '" + tableName + "' does not exist.");
                return;
            }

            String sql = "UPDATE " + tableName + " SET " + updateColumn + " = ? WHERE CAST(" + searchColumn
                    + " AS CHAR) = ?";
            try (PreparedStatement pstmt = prepare(sql)) {
                pstmt.setString(1, updateValue);
                pstmt.setString(2, searchValue);
                int rowsAffected = pstmt.executeUpdate();
                System.out.println(
                        "Success: Updated " + rowsAffected + " record(s) in " + tableName + " where " + searchColumn
                                + " = '" + searchValue + "' to set " + updateColumn + " = '" + updateValue + "'.");
            }
        } catch (SQLException e) {
            System.out.println("Error updating records by any data: " + e.getMessage());
        }
    }

    public void findRecordsByAnyData(String tableName, String columnName, String value) {
        try {
            if (!tableExists(tableName)) {
                System.out.println("Error: Cannot search. Table '" + tableName + "' does not exist.");
                return;
            }

            String sql = "SELECT * FROM " + tableName + " WHERE CAST(" + columnName + " AS CHAR) = ?";
            try (PreparedStatement pstmt = prepare(sql)) {
                pstmt.setString(1, value);
                try (ResultSet rs = pstmt.executeQuery()) {
                    System.out.println(
                            "\n--- Records in " + tableName + " where " + columnName + " = '" + value + "' ---");
                    boolean found = false;
                    while (rs.next()) {
                        printRow(rs);
                        found = true;
                    }
                    if (!found) {
                        System.out.println("No records found where " + columnName + " = '" + value + "'.");
                    }
                    System.out.println("-------------------------------------\n");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding records by any data: " + e.getMessage());
        }
    }

    public void findRecordsByAnyDataLike(String tableName, String columnName, String pattern) {
        try {
            if (!tableExists(tableName)) {
                System.out.println("Error: Cannot search. Table '" + tableName + "' does not exist.");
                return;
            }

            String sql = "SELECT * FROM " + tableName + " WHERE CAST(" + columnName + " AS CHAR) LIKE ?";
            try (PreparedStatement pstmt = prepare(sql)) {
                pstmt.setString(1, pattern);
                try (ResultSet rs = pstmt.executeQuery()) {
                    System.out.println(
                            "\n--- Records in " + tableName + " where " + columnName + " LIKE '" + pattern + "' ---");
                    boolean found = false;
                    while (rs.next()) {
                        printRow(rs);
                        found = true;
                    }
                    if (!found) {
                        System.out.println("No records found where " + columnName + " LIKE '" + pattern + "'.");
                    }
                    System.out.println("-------------------------------------\n");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding records by any data like: " + e.getMessage());
        }
    }

    public void findRecordsByAnyDataGreaterThan(String tableName, String columnName, int value) {
        try {
            if (!tableExists(tableName)) {
                System.out.println("Error: Cannot search. Table '" + tableName + "' does not exist.");
                return;
            }

            String sql = "SELECT * FROM " + tableName + " WHERE " + columnName + " > ?";
            try (PreparedStatement pstmt = prepare(sql)) {
                pstmt.setInt(1, value);
                try (ResultSet rs = pstmt.executeQuery()) {
                    System.out
                            .println("\n--- Records in " + tableName + " where " + columnName + " > " + value + " ---");
                    boolean found = false;
                    while (rs.next()) {
                        printRow(rs);
                        found = true;
                    }
                    if (!found) {
                        System.out.println("No records found where " + columnName + " > " + value + ".");
                    }
                    System.out.println("-------------------------------------\n");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding records by any data greater than: " + e.getMessage());
        }
    }
}
