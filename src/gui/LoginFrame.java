package gui;

import Database.DatabaseConnection;
import models.User;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginFrame extends JFrame {
    private JTextField phoneField;
    private JPasswordField passwordField;

    public LoginFrame() {
        setTitle("Finance Tracker - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(null);

        // Main panel with background color
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(240, 248, 255));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);

        // Title
        JLabel titleLabel = new JLabel("Finance Tracker");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(25, 118, 210));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, gbc);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Track your expenses easily");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(subtitleLabel, gbc);

        gbc.insets = new Insets(20, 0, 10, 0);

        // Phone field
        JLabel phoneLabel = new JLabel("Phone Number");
        phoneLabel.setFont(new Font("Arial", Font.BOLD, 14));
        mainPanel.add(phoneLabel, gbc);

        phoneField = Components.createRoundedTextField(20);
        mainPanel.add(phoneField, gbc);

        // Password field
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Arial", Font.BOLD, 14));
        mainPanel.add(passLabel, gbc);

        passwordField = Components.createRoundedPasswordField(20);
        mainPanel.add(passwordField, gbc);

        gbc.insets = new Insets(20, 0, 10, 0);

        // Login button
        JButton loginButton = Components.createRoundedButton("Login", new Color(25, 118, 210), Color.WHITE);
        loginButton.addActionListener(e -> login());
        mainPanel.add(loginButton, gbc);

        // Sign up link
        JButton signUpButton = Components.createRoundedButton("Create Account", new Color(76, 175, 80), Color.WHITE);
        signUpButton.addActionListener(e -> {
            new SignUpFrame().setVisible(true);
            dispose();
        });
        mainPanel.add(signUpButton, gbc);

        add(mainPanel);
    }

    private void login() {
        String phone = phoneField.getText();
        String password = new String(passwordField.getPassword());

        if (phone.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // Get a new connection
            conn = DatabaseConnection.getConnection();

            // Query user
            String query = "SELECT * FROM users WHERE phone_number = ? AND password = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, phone);
            pstmt.setString(2, password);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                // Login successful
                User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("phone_number"),
                        rs.getDouble("current_balance")
                );

                // Update last login - use the SAME connection
                String update = "UPDATE users SET last_login = NOW() WHERE user_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(update)) {
                    updateStmt.setInt(1, user.getUserId());
                    updateStmt.executeUpdate();
                }

                // Close resources before opening new frame
                try { if (rs != null) rs.close(); } catch (SQLException e) {}
                try { if (pstmt != null) pstmt.close(); } catch (SQLException e) {}
                try { if (conn != null) conn.close(); } catch (SQLException e) {}

                // Open main frame
                new MainFrame(user).setVisible(true);
                dispose();

            } else {
                JOptionPane.showMessageDialog(this, "Invalid phone number or password");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        } finally {
            // Always close resources
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
    }
}