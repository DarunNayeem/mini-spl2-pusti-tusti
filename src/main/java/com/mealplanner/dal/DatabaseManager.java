package com.mealplanner.dal;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton class to manage SQLite database connections
 */
public class DatabaseManager {
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static final String DEFAULT_DATABASE_PATH = "src/main/resources/recipes.db";
    private static final String DATABASE_URL = "jdbc:sqlite:" + resolveDefaultDatabasePath();
    private static volatile Connection connection;
    private static volatile String activeDatabaseUrl;

    private DatabaseManager() {
        // Private constructor to prevent instantiation
    }

    /**
     * Gets the database connection, creating it if necessary
     * 
     * @return Connection instance
     * @throws SQLException if connection cannot be established
     */
    public static synchronized Connection getConnection() throws SQLException {
        String databaseUrl = resolveDatabaseUrl();

        if (connection != null && !connection.isClosed() && databaseUrl.equals(activeDatabaseUrl)) {
            return connection;
        }

        if (connection != null) {
            closeConnection();
        }

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(databaseUrl);
            activeDatabaseUrl = databaseUrl;

            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }

            createUnifiedView();
            createProfilesTable();

            LOGGER.info("Database connection established successfully");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found", e);
        }
        return connection;
    }

    /**
     * Creates a unified view combining all recipe tables
     */
    private static void createUnifiedView() throws SQLException {
        String createViewSQL = """
                CREATE VIEW IF NOT EXISTS recipe_all AS
                SELECT 'BREAKFAST' as meal_type_derived, category, * FROM breakfast
                UNION ALL
                SELECT 'LUNCH' as meal_type_derived, category, * FROM lunch
                UNION ALL
                SELECT 'DINNER' as meal_type_derived, category, * FROM dinner
                UNION ALL
                SELECT 'SNACKS' as meal_type_derived, category, * FROM snacks
                """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createViewSQL);
        }
    }

    /**
     * Creates the user_profiles table if it does not already exist.
     */
    private static void createProfilesTable() throws SQLException {
        String createTableSql = """
                CREATE TABLE IF NOT EXISTS user_profiles (
                    user_id TEXT PRIMARY KEY,
                    name TEXT NOT NULL COLLATE NOCASE UNIQUE,
                    age INTEGER NOT NULL,
                    height_cm REAL NOT NULL,
                    weight_kg REAL NOT NULL,
                    gender TEXT,
                    primary_health_goal TEXT,
                    allergies TEXT,
                    meal_preferences TEXT,
                    medical_conditions TEXT,
                    activity_level INTEGER NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

        String createIndexSql = "CREATE INDEX IF NOT EXISTS idx_user_profiles_name ON user_profiles(name)";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSql);
            stmt.execute(createIndexSql);
        }
    }

    private static String resolveDefaultDatabasePath() {
        Path defaultPath = Paths.get(DEFAULT_DATABASE_PATH);
        try {
            Path parent = defaultPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Could not ensure database directory exists", e);
        }
        return defaultPath.toAbsolutePath().toString();
    }

    private static String resolveDatabaseUrl() {
        return System.getProperty("mealplanner.db.url", DATABASE_URL);
    }

    /**
     * Tests the database connection
     * 
     * @return true if connection is successful
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Closes the database connection gracefully
     */
    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    LOGGER.info("Database connection closed successfully");
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing database connection", e);
            } finally {
                connection = null;
                activeDatabaseUrl = null;
            }
        }
    }

    /**
     * Executes a query to check if the database is accessible
     * 
     * @return true if database is accessible
     */
    public static boolean isDatabaseAccessible() {
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.executeQuery("SELECT 1");
            return true;
        } catch (SQLException e) {
            System.err.println("Database accessibility check failed: " + e.getMessage());
            return false;
        }
    }
}
