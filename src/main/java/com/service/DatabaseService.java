package com.service;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;

public interface DatabaseService {

    // --- DDL: Table-level ---
    void createTable(String tableName);

    void dropTable(String tableName);

    void renameTable(String oldName, String newName);

    void listTables();

    void describeTable(String tableName);

    void truncateTable(String tableName);

    // --- DDL: Column-level ---
    void addColumn(String tableName, String columnName, String dataType);

    void dropColumn(String tableName, String columnName);

    void renameColumn(String tableName, String oldColName, String newColName);

    // --- DML ---
    void insertRecord(String tableName, LinkedHashMap<String, String> columnValues);

    void updateRecord(String tableName, int id, String columnName, String newValue);

    void deleteRecord(String tableName, int id);

    void deleteAllRecords(String tableName);

    void updateRecordsByAnyData(String tableName, String searchCol, String searchVal,
            String updateCol, String updateVal);

    void deleteRecordsByAnyData(String tableName, String columnName, String value);

    // --- DQL ---
    void fetchAllRecords(String tableName);

    void countRecords(String tableName);

    void findRecordById(String tableName, int id);

    void findRecordsByAnyData(String tableName, String columnName, String value);

    void findRecordsByAnyDataLike(String tableName, String columnName, String pattern);

    void findRecordsByAnyDataGreaterThan(String tableName, String columnName, int value);

    // --- Utility ---
    List<String> getTableColumns(String tableName) throws SQLException;

    void close() throws SQLException;
}
