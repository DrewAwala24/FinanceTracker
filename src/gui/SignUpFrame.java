package gui;

import Database.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.regex.Pattern;

public class SignUpFrame extends JFrame {
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JLabel strengthLabel;
    private JProgressBar strengthBar;

    public SignUpFrame() {
        setTitle("Finance Tracker - Sign Up");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 700);
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
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(25, 118, 210));
        mainPanel.add(titleLabel, gbc);

        // Subtitle with info
        JLabel infoLabel = new JLabel("You can login with any of these later");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        infoLabel.setForeground(new Color(100, 100, 100));
        mainPanel.add(infoLabel, gbc);

        gbc.insets = new Insets(15, 0, 5, 0);

        // Username field with hint
        JLabel nameLabel = new JLabel("Username *");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        mainPanel.add(nameLabel, gbc);

        nameField = Components.createRoundedTextField(20);
        nameField.setToolTipText("Choose a unique username (min 3 characters)");
        mainPanel.add(nameField, gbc);

        // Email field
        JLabel emailLabel = new JLabel("Email Address *");
        emailLabel.setFont(new Font("Arial", Font.BOLD, 14));
        mainPanel.add(emailLabel, gbc);

        emailField = Components.createRoundedTextField(20);
        emailField.setToolTipText("Enter a valid email address");
        mainPanel.add(emailField, gbc);

        // Phone field
        JLabel phoneLabel = new JLabel("Phone Number *");
        phoneLabel.setFont(new Font("Arial", Font.BOLD, 14));
        mainPanel.add(phoneLabel, gbc);

        phoneField = Components.createRoundedTextField(20);
        phoneField.setToolTipText("Enter at least 10 digits");
        mainPanel.add(phoneField, gbc);

        // Password field with strength meter
        JLabel passLabel = new JLabel("Password *");
        passLabel.setFont(new Font("Arial", Font.BOLD, 14));
        mainPanel.add(passLabel, gbc);

        passwordField = Components.createRoundedPasswordField(20);
        passwordField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { checkPasswordStrength(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { checkPasswordStrength(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { checkPasswordStrength(); }
        });
        mainPanel.add(passwordField, gbc);

        // Password strength indicator
        JPanel strengthPanel = new JPanel(new BorderLayout(5, 0));
        strengthPanel.setBackground(new Color(240, 248, 255));

        strengthLabel = new JLabel("Password strength: ");
        strengthLabel.setFont(new Font("Arial", Font.PLAIN, 11));

        strengthBar = new JProgressBar(0, 100);
        strengthBar.setStringPainted(true);
        strengthBar.setString("");
        strengthBar.setPreferredSize(new Dimension(100, 15));

        strengthPanel.add(strengthLabel, BorderLayout.WEST);
        strengthPanel.add(strengthBar, BorderLayout.CENTER);
        mainPanel.add(strengthPanel, gbc);

        // Confirm Password
        JLabel confirmLabel = new JLabel("Confirm Password *");
        confirmLabel.setFont(new Font("Arial", Font.BOLD, 14));
        mainPanel.add(confirmLabel, gbc);

        confirmPasswordField = Components.createRoundedPasswordField(20);
        mainPanel.add(confirmPasswordField, gbc);

        // Password match indicator
        JLabel matchLabel = new JLabel(" ");
        matchLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        matchLabel.setName("matchLabel");

        // Add listener to check password match
        Runnable checkMatch = () -> {
            String pass = new String(passwordField.getPassword());
            String confirm = new String(confirmPasswordField.getPassword());
            if (!pass.isEmpty() && !confirm.isEmpty()) {
                if (pass.equals(confirm)) {
                    matchLabel.setText("✓ Passwords match");
                    matchLabel.setForeground(new Color(76, 175, 80));
                } else {
                    matchLabel.setText("✗ Passwords do not match");
                    matchLabel.setForeground(Color.RED);
                }
            } else {
                matchLabel.setText(" ");
            }
        };

        passwordField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { checkMatch.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { checkMatch.run(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { checkMatch.run(); }
        });

        confirmPasswordField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { checkMatch.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { checkMatch.run(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { checkMatch.run(); }
        });

        mainPanel.add(matchLabel, gbc);

        // Terms and conditions checkbox
        JCheckBox termsCheck = new JCheckBox("I agree to the Terms and Conditions");
        termsCheck.setBackground(new Color(240, 248, 255));
        termsCheck.setFont(new Font("Arial", Font.PLAIN, 12));
        mainPanel.add(termsCheck, gbc);

        gbc.insets = new Insets(20, 0, 10, 0);

        // Sign Up button
        JButton signUpButton = Components.createRoundedButton("Create Account", new Color(76, 175, 80), Color.WHITE);
        signUpButton.addActionListener(e -> signUp(termsCheck.isSelected()));
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

    private void checkPasswordStrength() {
        String password = new String(passwordField.getPassword());
        int strength = calculatePasswordStrength(password);
        strengthBar.setValue(strength);

        if (strength < 30) {
            strengthBar.setForeground(Color.RED);
            strengthBar.setString("Weak");
        } else if (strength < 60) {
            strengthBar.setForeground(Color.ORANGE);
            strengthBar.setString("Medium");
        } else if (strength < 80) {
            strengthBar.setForeground(new Color(255, 193, 7));
            strengthBar.setString("Good");
        } else {
            strengthBar.setForeground(new Color(76, 175, 80));
            strengthBar.setString("Strong");
        }
    }

    private int calculatePasswordStrength(String password) {
        if (password.isEmpty()) return 0;

        int strength = 0;

        // Length check
        if (password.length() >= 8) strength += 25;
        else if (password.length() >= 6) strength += 15;
        else if (password.length() >= 4) strength += 5;

        // Contains numbers
        if (password.matches(".*\\d.*")) strength += 20;

        // Contains lowercase
        if (password.matches(".*[a-z].*")) strength += 15;

        // Contains uppercase
        if (password.matches(".*[A-Z].*")) strength += 20;

        // Contains special characters
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) strength += 20;

        return Math.min(strength, 100);
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        // Remove any non-digit characters
        String digits = phone.replaceAll("\\D", "");
        return digits.length() >= 10;
    }

    private boolean isValidUsername(String username) {
        // Username: 3-20 characters, letters, numbers, underscore
        return username.matches("^[a-zA-Z0-9_]{3,20}$");
    }

    private void signUp(boolean termsAccepted) {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim().toLowerCase();
        String phone = phoneField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Validation
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields");
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match");
            return;
        }

        if (!isValidUsername(name)) {
            JOptionPane.showMessageDialog(this,
                    "Invalid username!\n\nUsername must:\n" +
                            "• Be 3-20 characters long\n" +
                            "• Contain only letters, numbers, and underscores\n" +
                            "• No spaces or special characters");
            return;
        }

        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address");
            return;
        }

        if (!isValidPhone(phone)) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid phone number with at least 10 digits");
            return;
        }

        if (password.length() < 4) {
            JOptionPane.showMessageDialog(this, "Password must be at least 4 characters long");
            return;
        }

        if (!termsAccepted) {
            JOptionPane.showMessageDialog(this,
                    "Please accept the Terms and Conditions to continue");
            return;
        }

        // Clean phone number (remove non-digits)
        String cleanPhone = phone.replaceAll("\\D", "");

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if username already exists
            String checkUserQuery = "SELECT * FROM users WHERE username = ?";
            PreparedStatement userStmt = conn.prepareStatement(checkUserQuery);
            userStmt.setString(1, name);
            ResultSet userRs = userStmt.executeQuery();

            if (userRs.next()) {
                JOptionPane.showMessageDialog(this, "Username already taken. Please choose another.");
                return;
            }

            // Check if email already exists
            String checkEmailQuery = "SELECT * FROM users WHERE email = ?";
            PreparedStatement emailStmt = conn.prepareStatement(checkEmailQuery);
            emailStmt.setString(1, email);
            ResultSet emailRs = emailStmt.executeQuery();

            if (emailRs.next()) {
                JOptionPane.showMessageDialog(this, "Email already registered. Please use another or login.");
                return;
            }

            // Check if phone already exists
            String checkPhoneQuery = "SELECT * FROM users WHERE phone_number = ?";
            PreparedStatement phoneStmt = conn.prepareStatement(checkPhoneQuery);
            phoneStmt.setString(1, cleanPhone);
            ResultSet phoneRs = phoneStmt.executeQuery();

            if (phoneRs.next()) {
                JOptionPane.showMessageDialog(this, "Phone number already registered. Please use another or login.");
                return;
            }

            // Insert new user
            String insertQuery = "INSERT INTO users (username, password, email, phone_number, current_balance) VALUES (?, ?, ?, ?, 0.00)";
            PreparedStatement pstmt = conn.prepareStatement(insertQuery);
            pstmt.setString(1, name);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.setString(4, cleanPhone);

            pstmt.executeUpdate();

            // Success message with login options
            String successMessage = String.format(
                    "✅ Account created successfully!\n\n" +
                            "You can now login with ANY of these credentials:\n\n" +
                            "📱 Phone: %s\n" +
                            "📧 Email: %s\n" +
                            "👤 Username: %s\n\n" +
                            "Password: %s\n\n" +
                            "Current Balance: $0.00",
                    cleanPhone, email, name,
                    password.replaceAll(".", "*") // Show asterisks for security
            );

            JOptionPane.showMessageDialog(this,
                    successMessage,
                    "Registration Successful",
                    JOptionPane.INFORMATION_MESSAGE);

            new LoginFrame().setVisible(true);
            dispose();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}