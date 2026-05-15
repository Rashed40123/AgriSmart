package com.agrisoft.utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.agrisoft.models.User;
import com.agrisoft.models.Land;

public class DatabaseManager {
    private static final String DB_NAME = "agrisoft.db";
    private static Connection connection;

    public static void initDatabase() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found", e);
        }

        connection = DriverManager.getConnection("jdbc:sqlite:" + DB_NAME);
        createTables();
        System.out.println("Database initialized successfully");
    }

    private static void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    nid TEXT NOT NULL,
                    name TEXT NOT NULL,
                    phone TEXT NOT NULL,
                    profile_pic TEXT
                )
            """);

            // Lands table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS lands (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    location TEXT NOT NULL,
                    area REAL NOT NULL,
                    soil_type TEXT NOT NULL,
                    gps_coords TEXT,
                    FOREIGN KEY (user_id) REFERENCES users (id)
                )
            """);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_NAME);
        }
        return connection;
    }

    // User operations
    public static User authenticateUser(String username, String password) throws SQLException {
        String hashedPassword = PasswordUtils.hashPassword(password);
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setNid(rs.getString("nid"));
                user.setName(rs.getString("name"));
                user.setPhone(rs.getString("phone"));
                user.setProfilePic(rs.getString("profile_pic"));
                return user;
            }
        }
        return null;
    }

    public static boolean registerUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password, nid, name, phone) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, PasswordUtils.hashPassword(user.getPassword()));
            stmt.setString(3, user.getNid());
            stmt.setString(4, user.getName());
            stmt.setString(5, user.getPhone());
            return stmt.executeUpdate() > 0;
        }
    }

    public static boolean updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET username = ?, nid = ?, name = ?, phone = ?, profile_pic = ? WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getNid());
            stmt.setString(3, user.getName());
            stmt.setString(4, user.getPhone());
            stmt.setString(5, user.getProfilePic());
            stmt.setInt(6, user.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    // Land operations
    public static boolean addLand(Land land) throws SQLException {
        String sql = "INSERT INTO lands (user_id, location, area, soil_type, gps_coords) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, land.getUserId());
            stmt.setString(2, land.getLocation());
            stmt.setDouble(3, land.getArea());
            stmt.setString(4, land.getSoilType());
            stmt.setString(5, land.getGpsCoords());
            return stmt.executeUpdate() > 0;
        }
    }

    public static java.util.List<Land> getUserLands(int userId) throws SQLException {
        java.util.List<Land> lands = new java.util.ArrayList<>();
        String sql = "SELECT * FROM lands WHERE user_id = ? ORDER BY id";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Land land = new Land();
                land.setId(rs.getInt("id"));
                land.setUserId(rs.getInt("user_id"));
                land.setLocation(rs.getString("location"));
                land.setArea(rs.getDouble("area"));
                land.setSoilType(rs.getString("soil_type"));
                land.setGpsCoords(rs.getString("gps_coords"));
                lands.add(land);
            }
        }
        return lands;
    }

    public static boolean deleteLand(int landId, int userId) throws SQLException {
        String sql = "DELETE FROM lands WHERE id = ? AND user_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, landId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    public static User getUserById(int userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setNid(rs.getString("nid"));
                user.setName(rs.getString("name"));
                user.setPhone(rs.getString("phone"));
                user.setProfilePic(rs.getString("profile_pic"));
                return user;
            }
        }
        return null;
    }
}