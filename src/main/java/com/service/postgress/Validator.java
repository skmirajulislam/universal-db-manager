package com.service.postgress;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

class Validator {
    private Connection connection;

    public Validator(Connection connection) {
        this.connection = connection;
    }

    /**
     * Helper method to check if a table exists in the database.
     */
    public boolean tableValidation(String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getTables(null, null, tableName.toLowerCase(), new String[] { "TABLE" })) {
            return resultSet.next();
        }
    }
}
