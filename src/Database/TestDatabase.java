package Database;

import Database.DatabaseConnection;
import java.sql.*;

public class TestDatabase {
    public static void main(String[] args) {
        System.out.println("Testing database connection...");

        try {
            // Test 1: Get connection
            Connection conn = DatabaseConnection.getConnection();
            System.out.println("✅ Connection successful!");

            // Test 2: Check if database exists
            DatabaseMetaData metaData = conn.getMetaData();
            System.out.println("✅ Database: " + conn.getCatalog());
            System.out.println("✅ MySQL Version: " + metaData.getDatabaseProductVersion());

            // Test 3: Check if tables exist
            String[] tables = {"users", "categories", "transactions"};
            for (String table : tables) {
                try {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table);
                    if (rs.next()) {
                        System.out.println("✅ Table '" + table + "' exists");
                    }
                } catch (SQLException e) {
                    System.out.println("❌ Table '" + table + "' does not exist: " + e.getMessage());
                }
            }

            // Test 4: Count records in users table
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM users");
            if (rs.next()) {
                System.out.println("✅ Users in database: " + rs.getInt("count"));
            }

            // Close connection
            //DatabaseConnection.closeConnection();
            System.out.println("\n✅ All tests completed!");

        } catch (SQLException e) {
            System.out.println("❌ Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}