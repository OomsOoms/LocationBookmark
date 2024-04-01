package io.github.oomsooms.locationbookmark;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;
import java.sql.Statement;

public class DatabaseManager {

    private static DatabaseManager instance;
    private Connection connection;

    // Private constructor to prevent instantiation from outside
    private DatabaseManager(String folderDir, String dbName) throws SQLException {
        // Construct the URL for the SQLite database
        String url = "jdbc:sqlite:" + folderDir + "/" + dbName;
        File dbFile = new File(folderDir, dbName);

        // If the database file does not exist, create it
        if (!dbFile.exists()) {
            try {
                dbFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Establish a connection to the database
        this.connection = DriverManager.getConnection(url);
    }

    // Method to get the Singleton instance
    public static DatabaseManager getInstance(String folderDir, String dbName) throws SQLException {
        if (instance == null) {
            instance = new DatabaseManager(folderDir, dbName);
        }
        return instance;
    }

    // Method to get the connection
    public Connection getConnection() {
        return connection;
    }

    public ResultSet executeQuery(String query) {
        ResultSet resultSet = null;
        try {
            Statement statement = connection.createStatement();
    
            if (query.trim().toUpperCase().startsWith("SELECT")) {
                // Execute SELECT query
                resultSet = statement.executeQuery(query);
            } else {
                // Execute other types of queries (INSERT, UPDATE, DELETE, etc.)
                statement.executeUpdate(query);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultSet;
    }


    // Close the connection
    public void closeConnection() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}
