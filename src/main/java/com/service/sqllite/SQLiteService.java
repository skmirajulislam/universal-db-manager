package com.service.sqllite;

import com.service.DatabaseService;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SQLiteService implements DatabaseService {

    private final Connection connection;
    private final DataManager dataManager;
    private final TableManager tableManager;

    public SQLiteService(Connection connection) {
        this.connection = connection;
        this.dataManager = new DataManager(connection);
        this.tableManager = new TableManager(connection);
    }

    private void checkConnection() {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                throw new RuntimeException("Connection lost. Please logout and reconnect.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Connection check failed: " + e.getMessage());
        }
    }

    @Override public void createTable(String t)                    { checkConnection(); tableManager.createTable(t); }
    @Override public void dropTable(String t)                      { checkConnection(); tableManager.dropTable(t); }
    @Override public void renameTable(String o, String n)          { checkConnection(); tableManager.renameTable(o, n); }
    @Override public void listTables()                             { checkConnection(); tableManager.listTables(); }
    @Override public void describeTable(String t)                  { checkConnection(); tableManager.listColumns(t); }
    @Override public void truncateTable(String t)                  { checkConnection(); tableManager.truncateTable(t); }
    @Override public void addColumn(String t, String c, String dt) { checkConnection(); tableManager.addColumn(t, c, dt); }
    @Override public void dropColumn(String t, String c)           { checkConnection(); tableManager.dropColumn(t, c); }
    @Override public void renameColumn(String t, String o, String n){ checkConnection(); tableManager.renameColumn(t, o, n); }

    @Override public void insertRecord(String t, LinkedHashMap<String, String> v)               { checkConnection(); dataManager.insertRecord(t, v); }
    @Override public void updateRecord(String t, int id, String col, String val)               { checkConnection(); dataManager.updateRecord(t, id, col, val); }
    @Override public void deleteRecord(String t, int id)                                        { checkConnection(); dataManager.deleteRecord(t, id); }
    @Override public void deleteAllRecords(String t)                                            { checkConnection(); dataManager.deleteAllRecords(t); }
    @Override public void updateRecordsByAnyData(String t, String sc, String sv, String uc, String uv) { checkConnection(); dataManager.updateRecordsByAnyData(t, sc, sv, uc, uv); }
    @Override public void deleteRecordsByAnyData(String t, String c, String v)                  { checkConnection(); dataManager.deleteRecordsByAnyData(t, c, v); }

    @Override public void fetchAllRecords(String t)                            { checkConnection(); dataManager.fetchAllRecords(t); }
    @Override public void countRecords(String t)                               { checkConnection(); dataManager.countRecords(t); }
    @Override public void findRecordById(String t, int id)                     { checkConnection(); dataManager.findRecordById(t, id); }
    @Override public void findRecordsByAnyData(String t, String c, String v)   { checkConnection(); dataManager.findRecordsByAnyData(t, c, v); }
    @Override public void findRecordsByAnyDataLike(String t, String c, String p){ checkConnection(); dataManager.findRecordsByAnyDataLike(t, c, p); }
    @Override public void findRecordsByAnyDataGreaterThan(String t, String c, int v){ checkConnection(); dataManager.findRecordsByAnyDataGreaterThan(t, c, v); }

    @Override
    public List<String> getTableColumns(String tableName) throws SQLException {
        checkConnection();
        List<String> cols = new ArrayList<>();
        DatabaseMetaData meta = connection.getMetaData();
        try (ResultSet rs = meta.getColumns(null, null, tableName, null)) {
            while (rs.next()) {
                String col = rs.getString("COLUMN_NAME");
                if (!col.equalsIgnoreCase("id"))
                    cols.add(col);
            }
        }
        return cols;
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed())
            connection.close();
    }
}
