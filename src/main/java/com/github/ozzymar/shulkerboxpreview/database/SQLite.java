package com.github.ozzymar.shulkerboxpreview.database;

import com.github.ozzymar.shulkerboxpreview.ShulkerboxPreview;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

public class SQLite {
    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC"); // Specify Driver Needed
            Path path = Paths.get(ShulkerboxPreview.getPlugin(ShulkerboxPreview.class).getDataFolder().getAbsolutePath() + "/data/");
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + path + "/sbp.db"); // Create .db File in a specified directory

        } catch (SQLException | ClassNotFoundException | IOException ex) {
            ex.printStackTrace();
        }
        return connection;
    }

    public static void initializeDB(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS EnabledSBP (UUID TEXT, isEnabled TEXT);");
            statement.close();
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static boolean isPlayerInDatabase(Player player) {
        boolean isFound = false;
        Connection connection = SQLite.getConnection();
        try {
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT * FROM EnabledSBP;");
            while (results.next()) {
                if (results.getString("UUID").equalsIgnoreCase(player.getUniqueId().toString())) {
                    isFound = true;
                    break;
                }
            }
            results.close();
            statement.close();
            connection.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return isFound;
    }

    public static String doesPlayerHavePreviewEnabled(Player player) {
        Connection connection = SQLite.getConnection();
        PreparedStatement updateStatement;
        String enabled = null;
        try {
            updateStatement = connection.prepareStatement("SELECT isEnabled FROM EnabledSBP WHERE uuid = ?;");
            updateStatement.setString(1, player.getUniqueId().toString());
            updateStatement.execute();
            ResultSet results = updateStatement.executeQuery();
            while (results.next()) {
                enabled = results.getString("isEnabled");
            }
            results.close();
            updateStatement.close();
            connection.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return enabled;
    }
}
