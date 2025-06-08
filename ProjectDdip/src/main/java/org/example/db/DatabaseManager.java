package org.example.db;

import org.example.config.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() throws SQLException {
        reconnect();
    }

    private void reconnect() throws SQLException {
        String url  = Config.get("db.url");
        String user = Config.get("db.username");
        String pass = Config.get("db.password");

        if (url == null || user == null) {
            throw new RuntimeException("Не найдены ключи db.url или db.username в config.properties");
        }
        this.connection = DriverManager.getConnection(url, user, pass);
    }

    public static synchronized DatabaseManager getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseManager();
        } else {
            try {
                if (instance.connection == null || instance.connection.isClosed()) {
                    instance.reconnect();
                }
            } catch (SQLException e) {
                instance.reconnect();
            }
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            reconnect();
        }
        return connection;
    }
}
