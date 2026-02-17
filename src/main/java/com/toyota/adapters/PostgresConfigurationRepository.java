package com.toyota.adapters;

import com.toyota.ports.ConfigurationRepository;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PostgresConfigurationRepository implements ConfigurationRepository {

    private static final String CONFIG_FILE = "db.properties";
    private final Map<String, String> cache = new HashMap<>();

    public PostgresConfigurationRepository() {
        loadFromDatabase();
    }

    private void loadFromDatabase() {
        Properties props = loadProperties();
        String url = props.getProperty("db.url.server") + props.getProperty("db.name");
        String user = props.getProperty("db.user");
        String pass = props.getProperty("db.password");

        String query = "SELECT CONFIG_KEY, CONFIG_VALUE FROM ADDENDA_CONFIG";

        Properties connProps = new Properties();
        connProps.setProperty("user", user);
        connProps.setProperty("password", pass);
        // Ensure SSL is used for Supabase or remote connections
        connProps.setProperty("sslmode", "require");

        try (Connection conn = DriverManager.getConnection(url, connProps);
                PreparedStatement pstmt = conn.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                cache.put(rs.getString("CONFIG_KEY"), rs.getString("CONFIG_VALUE"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load configuration from database", e);
        }
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("Sorry, unable to find " + CONFIG_FILE);
            }
            props.load(input);
        } catch (IOException ex) {
            throw new RuntimeException("Error loading db.properties", ex);
        }
        return props;
    }

    @Override
    public String getString(String key) {
        if (!cache.containsKey(key)) {
            throw new IllegalArgumentException("Configuration key not found: " + key);
        }
        return cache.get(key);
    }
}
