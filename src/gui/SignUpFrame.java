package gui;

import Database.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class SignUpFrame extends JFrame {
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    public SignUpFrame() {
        setTitle("Finance Tracker - Sign Up");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(450, 600);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(240, 248, 255));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);

        // Title
        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(25, 118, 210));
        mainPanel.add(titleLabel, gbc);

        gbc.insets = new Insets(10, 0, 5, 0);

        // Name
        mainPanel.add(new JLabel("Full Name"), gbc);
        nameField = Components.createRoundedTextField(20);
        mainPanel.add(nameField, gbc);

        // Email
        mainPanel.add(new JLabel("Email"), gbc);
        emailField = Components.createRoundedTextField(20);
        mainPanel.add(emailField, gbc);

        // Phone
        mainPanel.add(new JLabel("Phone Number"), gbc);
        phoneField = Components.createRoundedTextField(20);
        mainPanel.add(phoneField, gbc);

        // Password
        mainPanel.add(new JLabel("Password"), gbc);
        passwordField = Components.createRoundedPasswordField(20);
        mainPanel.add(passwordField, gbc);

        // Confirm Password
        mainPanel.add(new JLabel("Confirm Password"), gbc);
        confirmPasswordField = Components.createRoundedPasswordField(20);
        mainPanel.add(confirmPasswordField, gbc);

        gbc.insets = new Insets(20, 0, 10, 0);

        // Sign Up button
        JButton signUpButton = Components.createRoundedButton("Sign Up", new Color(76, 175, 80), Color.WHITE);
        signUpButton.addActionListener(e -> signUp());
        mainPanel.add(signUpButton, gbc);

        // Back to login
        JButton backButton = Components.createRoundedButton("Back to Login", new Color(158, 158, 158), Color.WHITE);
        backButton.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        mainPanel.add(backButton, gbc);

        add(mainPanel);
    }

    private void signUp() {
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields");
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if phone already exists
            String checkQuery = "SELECT * FROM users WHERE phone_number = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, phone);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Phone number already registered");
                return;
            }

            // Insert new user
            String insertQuery = "INSERT INTO users (username, password, email, phone_number, current_balance) VALUES (?, ?, ?, ?, 0.00)";
            PreparedStatement pstmt = conn.prepareStatement(insertQuery);
            pstmt.setString(1, name);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.setString(4, phone);

            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Account created successfully! Please login.");
            new LoginFrame().setVisible(true);
            dispose();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }
}