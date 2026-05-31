package me.unprankable.bendinghub;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * StorageManager - a small SQLite-backed storage helper for Bendinghub.
 *
 * Responsibilities:
 * - Initialize an on-disk SQLite database in plugins/Bendinghub/data.db
 * - Provide methods to store/retrieve player channel and chat color
 * - Provide a simple key/value table for other plugin data
 *
 * Notes:
 * - This class uses the sqlite-jdbc driver (org.sqlite.JDBC). Add the dependency
 *   to your plugin POM/shade it into the final jar if it's not provided by the
 *   runtime. If the driver is missing the class will log a warning and attempt
 *   to connect; the call will fail with SQLException if the driver is absent.
 */
public class StorageManager {
    private static final String DEFAULT_DB_PATH = "plugins/Bendinghub/data.db";
    private final String dbPath;
    private final Logger logger;
    private Connection connection;

    public StorageManager(Logger logger) {
        this(logger, DEFAULT_DB_PATH);
    }

    public StorageManager(Logger logger, String dbPath) {
        this.logger = logger != null ? logger : Logger.getLogger("Bendinghub");
        this.dbPath = dbPath;
    }

    /**
     * Initialize the SQLite database and create required tables if missing.
     */
    public void init() throws SQLException {
        try {
            // Attempt to load the SQLite driver - optional if the driver auto-registers
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            logger.log(Level.WARNING, "SQLite JDBC driver not found on classpath. Add sqlite-jdbc to your dependencies/shade it into the plugin.", e);
        }

        String url = "jdbc:sqlite:" + dbPath;
        connection = DriverManager.getConnection(url);
        connection.setAutoCommit(true);

        try (Statement st = connection.createStatement()) {
            // Enable foreign keys as a good practice
            st.executeUpdate("PRAGMA foreign_keys = ON;");

            // Table for player channel selection
            st.executeUpdate("CREATE TABLE IF NOT EXISTS player_channels (uuid TEXT PRIMARY KEY, channel TEXT);");

            // Table for player chat color selection
            st.executeUpdate("CREATE TABLE IF NOT EXISTS player_chatcolors (uuid TEXT PRIMARY KEY, chatcolor TEXT);");

            // Generic key/value table for other plugin data
            st.executeUpdate("CREATE TABLE IF NOT EXISTS metadata (key TEXT PRIMARY KEY, value TEXT);");
        }

        logger.info("StorageManager initialized using SQLite DB: " + dbPath);
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("StorageManager: connection closed");
            } catch (SQLException e) {
                logger.log(Level.WARNING, "StorageManager: failed to close connection", e);
            }
        }
    }

    // --- Player channels ---
    public void setPlayerChannel(UUID player, String channel) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO player_channels (uuid, channel) VALUES (?, ?) ON CONFLICT(uuid) DO UPDATE SET channel=excluded.channel;")) {
            ps.setString(1, player.toString());
            ps.setString(2, channel);
            ps.executeUpdate();
        }
    }

    public Optional<String> getPlayerChannel(UUID player) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT channel FROM player_channels WHERE uuid = ?;")) {
            ps.setString(1, player.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.ofNullable(rs.getString("channel"));
                return Optional.empty();
            }
        }
    }

    public void removePlayerChannel(UUID player) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM player_channels WHERE uuid = ?;")) {
            ps.setString(1, player.toString());
            ps.executeUpdate();
        }
    }

    public Map<UUID, String> loadAllPlayerChannels() throws SQLException {
        Map<UUID, String> map = new HashMap<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT uuid, channel FROM player_channels;")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    try {
                        UUID id = UUID.fromString(rs.getString("uuid"));
                        String channel = rs.getString("channel");
                        map.put(id, channel);
                    } catch (IllegalArgumentException ignored) {
                        // Skip invalid UUIDs
                    }
                }
            }
        }
        return map;
    }

    // --- Player chat colors ---
    public void setPlayerChatColor(UUID player, String chatColor) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO player_chatcolors (uuid, chatcolor) VALUES (?, ?) ON CONFLICT(uuid) DO UPDATE SET chatcolor=excluded.chatcolor;")) {
            ps.setString(1, player.toString());
            ps.setString(2, chatColor);
            ps.executeUpdate();
        }
    }

    public Optional<String> getPlayerChatColor(UUID player) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT chatcolor FROM player_chatcolors WHERE uuid = ?;")) {
            ps.setString(1, player.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.ofNullable(rs.getString("chatcolor"));
                return Optional.empty();
            }
        }
    }

    public void removePlayerChatColor(UUID player) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM player_chatcolors WHERE uuid = ?;")) {
            ps.setString(1, player.toString());
            ps.executeUpdate();
        }
    }

    public Map<UUID, String> loadAllPlayerChatColors() throws SQLException {
        Map<UUID, String> map = new HashMap<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT uuid, chatcolor FROM player_chatcolors;")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    try {
                        UUID id = UUID.fromString(rs.getString("uuid"));
                        String color = rs.getString("chatcolor");
                        map.put(id, color);
                    } catch (IllegalArgumentException ignored) {
                        // Skip invalid UUIDs
                    }
                }
            }
        }
        return map;
    }

    // --- Generic metadata ---
    public void putMetadata(String key, String value) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO metadata (key, value) VALUES (?, ?) ON CONFLICT(key) DO UPDATE SET value=excluded.value;")) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        }
    }

    public Optional<String> getMetadata(String key) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT value FROM metadata WHERE key = ?;")) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.ofNullable(rs.getString("value"));
                return Optional.empty();
            }
        }
    }

    public void removeMetadata(String key) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM metadata WHERE key = ?;")) {
            ps.setString(1, key);
            ps.executeUpdate();
        }
    }
}

