package gui;

import Database.DatabaseConnection;
import models.User;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginFrame extends JFrame {
    private JTextField loginField;
    private JPasswordField passwordField;
    private JComboBox<String> loginMethodCombo;
    private JCheckBox rememberCheck;
    private java.util.prefs.Preferences prefs;

    public LoginFrame() {
        // Initialize preferences
        prefs = java.util.prefs.Preferences.userRoot().node("financeTracker");

        setTitle("Finance Tracker - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 600);
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

        gbc.insets = new Insets(20, 0, 5, 0);

        // Login method selector
        JLabel methodLabel = new JLabel("Login with:");
        methodLabel.setFont(new Font("Arial", Font.BOLD, 14));
        mainPanel.add(methodLabel, gbc);

        String[] loginMethods = {"Phone Number", "Email", "Username"};
        loginMethodCombo = new JComboBox<>(loginMethods);
        loginMethodCombo.setFont(new Font("Montserrat Alternates", Font.PLAIN, 14));
        loginMethodCombo.setPreferredSize(new Dimension(300, 35));
        loginMethodCombo.addActionListener(e -> updateLoginFieldLabel());
        mainPanel.add(loginMethodCombo, gbc);

        gbc.insets = new Insets(15, 0, 5, 0);

        // Login field label (dynamic)
        JLabel loginLabel = new JLabel("Phone Number:");
        loginLabel.setFont(new Font("Montserrat Alternates", Font.BOLD, 14));
        loginLabel.setName("loginLabel");
        mainPanel.add(loginLabel, gbc);

        // Login field
        loginField = Components.createRoundedTextField(20);
        mainPanel.add(loginField, gbc);

        // Password field
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Montserrat Alternates", Font.BOLD, 14));
        mainPanel.add(passLabel, gbc);

        passwordField = Components.createRoundedPasswordField(20);
        mainPanel.add(passwordField, gbc);

        // Remember me checkbox
        rememberCheck = new JCheckBox("Remember me");
        rememberCheck.setBackground(new Color(240, 248, 255));
        rememberCheck.setFont(new Font("Arial", Font.PLAIN, 12));
        mainPanel.add(rememberCheck, gbc);

        // Forgot password link
        JLabel forgotLabel = new JLabel("Forgot password?");
        forgotLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        forgotLabel.setForeground(new Color(25, 118, 210));
        forgotLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        forgotLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                JOptionPane.showMessageDialog(LoginFrame.this,
                        "Please contact support to reset your password.\nOr sign up for a new account.",
                        "Forgot Password",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        mainPanel.add(forgotLabel, gbc);

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

        // Load saved preferences
        loadSavedPreferences();
    }

    private void updateLoginFieldLabel() {
        // Find the login label by name and update its text
        for (Component c : ((JPanel)getContentPane().getComponent(0)).getComponents()) {
            if (c instanceof JLabel && "loginLabel".equals(c.getName())) {
                String selected = (String) loginMethodCombo.getSelectedItem();
                if ("Phone Number".equals(selected)) {
                    ((JLabel) c).setText("Phone Number:");
                } else if ("Email".equals(selected)) {
                    ((JLabel) c).setText("Email Address:");
                } else if ("Username".equals(selected)) {
                    ((JLabel) c).setText("Username:");
                }
                break;
            }
        }
    }

    private void loadSavedPreferences() {
        // Load last used login method
        String lastMethod = prefs.get("lastLoginMethod", "Phone Number");
        loginMethodCombo.setSelectedItem(lastMethod);
        updateLoginFieldLabel();

        // Load saved login value if remember me was checked
        if (prefs.getBoolean("rememberMe", false)) {
            loginField.setText(prefs.get("savedLogin", ""));
            rememberCheck.setSelected(true);
        }
    }

    private void savePreferences(String loginMethod, String loginValue, boolean remember) {
        prefs.put("lastLoginMethod", loginMethod);
        prefs.putBoolean("rememberMe", remember);
        if (remember) {
            prefs.put("savedLogin", loginValue);
        } else {
            prefs.remove("savedLogin");
        }
    }

    private void login() {
        String loginValue = loginField.getText().trim();
        String password = new String(passwordField.getPassword());
        String loginMethod = (String) loginMethodCombo.getSelectedItem();
        boolean remember = rememberCheck.isSelected();

        if (loginValue.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            // Dynamic query based on login method
            String query;
            if ("Phone Number".equals(loginMethod)) {
                query = "SELECT * FROM users WHERE phone_number = ? AND password = ?";
            } else if ("Email".equals(loginMethod)) {
                query = "SELECT * FROM users WHERE email = ? AND password = ?";
            } else { // Username
                query = "SELECT * FROM users WHERE username = ? AND password = ?";
            }

            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, loginValue);
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

                // Update last login
                String update = "UPDATE users SET last_login = NOW() WHERE user_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(update)) {
                    updateStmt.setInt(1, user.getUserId());
                    updateStmt.executeUpdate();
                }

                // Save preferences
                savePreferences(loginMethod, loginValue, remember);

                // Close resources
                try { if (rs != null) rs.close(); } catch (SQLException e) {}
                try { if (pstmt != null) pstmt.close(); } catch (SQLException e) {}
                try { if (conn != null) conn.close(); } catch (SQLException e) {}

                // Show welcome message
                String welcomeMessage = String.format(
                        "Welcome back, %s!\n\nLogged in with: %s\n" +
                        user.getUsername(),
                        loginMethod,
                        user.getCurrentBalance()
                );

                JOptionPane.showMessageDialog(this,
                        welcomeMessage,
                        "Login Successful",
                        JOptionPane.INFORMATION_MESSAGE);

                // Open main frame
                new MainFrame(user).setVisible(true);
                dispose();

            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid " + loginMethod.toLowerCase() + " or password",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            // Always close resources
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
    }
}