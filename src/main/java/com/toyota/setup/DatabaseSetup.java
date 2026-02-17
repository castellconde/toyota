package com.toyota.setup;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseSetup {

    private static final String CONFIG_FILE = "db.properties";

    public static void main(String[] args) {
        Properties props = loadProperties();
        String url = props.getProperty("db.url.server"); // e.g., jdbc:postgresql://localhost:5432/
        String dbName = props.getProperty("db.name");
        String user = props.getProperty("db.user");
        String pass = props.getProperty("db.password");

        try {
            // Add SSL properties for Supabase
            Properties connProps = new Properties();
            connProps.setProperty("user", user);
            connProps.setProperty("password", pass);
            connProps.setProperty("sslmode", "require");

            // 1. If dbName is not 'postgres', try to create it. Otherwise, skip creation.
            if (!"postgres".equalsIgnoreCase(dbName)) {
                System.out.println("Connecting to server to check database...");
                try (Connection conn = DriverManager.getConnection(url + "postgres", connProps);
                        Statement stmt = conn.createStatement()) {

                    // Check if DB exists
                    boolean dbExists = false;
                    try (var rs = conn.getMetaData().getCatalogs()) {
                        while (rs.next()) {
                            if (rs.getString(1).equalsIgnoreCase(dbName)) {
                                dbExists = true;
                                break;
                            }
                        }
                    }

                    if (!dbExists) {
                        System.out.println("Creating database: " + dbName);
                        stmt.executeUpdate("CREATE DATABASE " + dbName);
                    } else {
                        System.out.println("Database " + dbName + " already exists.");
                    }
                }
            } else {
                System.out.println("Using default 'postgres' database. Skipping creation step.");
            }

            // 2. Connect to the target DB to create table and insert data
            System.out.println("Connecting to database: " + dbName);
            try (Connection conn = DriverManager.getConnection(url + dbName, connProps);
                    Statement stmt = conn.createStatement()) {

                System.out.println("Creating table ADDENDA_CONFIG...");
                String createTableSQL = "CREATE TABLE IF NOT EXISTS ADDENDA_CONFIG (" +
                        "CONFIG_KEY VARCHAR(255) PRIMARY KEY, " +
                        "CONFIG_VALUE VARCHAR(255))";
                stmt.executeUpdate(createTableSQL);

                System.out.println("Inserting default values...");
                String insertSQL = "INSERT INTO ADDENDA_CONFIG (CONFIG_KEY, CONFIG_VALUE) VALUES (?, ?) " +
                        "ON CONFLICT (CONFIG_KEY) DO NOTHING";

                try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                    insertConfig(pstmt, "CFDI_ADDENDA", "cfdi:Addenda");
                    insertConfig(pstmt, "ADDENDA_TOYOTA", "AddendaToyota");
                    insertConfig(pstmt, "NAMESPACE_TOYOTA",
                            "http://www.pegasotecnologia.com/secfd/schemas/AddendaReceptorToyota");
                    insertConfig(pstmt, "TMMGT", "TMMGT");
                    insertConfig(pstmt, "SHIPMENT_ORDER", "shipmentOrder");
                    insertConfig(pstmt, "SHIPMENT_ID_ATTR", "shipmentId");
                    insertConfig(pstmt, "AMOUNT_ATTR", "Amount");
                    insertConfig(pstmt, "CFDI_COMPROBANTE", "cfdi:Comprobante");
                    insertConfig(pstmt, "TOTAL_ATTR", "Total");
                    insertConfig(pstmt, "CFDI_CONCEPTO", "cfdi:Concepto");
                    insertConfig(pstmt, "NO_IDENTIFICACION_ATTR", "NoIdentificacion");
                    pstmt.executeBatch();
                }

                System.out.println("Database setup completed successfully.");
            }

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void insertConfig(PreparedStatement pstmt, String key, String value) throws SQLException {
        pstmt.setString(1, key);
        pstmt.setString(2, value);
        pstmt.addBatch();
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = DatabaseSetup.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                System.err.println("Sorry, unable to find " + CONFIG_FILE);
                System.exit(1);
            }
            props.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        return props;
    }
}
