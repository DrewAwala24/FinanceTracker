package gui;

import Database.DatabaseConnection;
import listeners.BalanceListener;
import models.*;
import utils.InsightsGenerator;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;

public class MainFrame extends JFrame implements BalanceListener {
    private User currentUser;
    private JLabel balanceLabel;
    private JTable transactionsTable;
    private DefaultTableModel tableModel;
    private JPanel contentPanel;
    private JPanel sidebar;
    private List<BalanceListener> balanceListeners = new ArrayList<>();
    private JLabel lastUpdatedLabel;
    private Timer refreshTimer;

    public MainFrame(User user) {
        this.currentUser = user;
        addBalanceListener(this);

        setTitle("Finance Tracker - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // Main layout
        setLayout(new BorderLayout());

        // Create sidebar
        sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // Content panel
        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(Color.WHITE);

        // Add different views
        contentPanel.add(createDashboardPanel(), "DASHBOARD");
        contentPanel.add(createTransactionsPanel(), "TRANSACTIONS");
        contentPanel.add(createAddTransactionPanel(), "ADD_TRANSACTION");
        contentPanel.add(createReportPanel(), "REPORT");
        contentPanel.add(createBudgetPanel(), "BUDGET");

        add(contentPanel, BorderLayout.CENTER);

        // Start real-time updates
        startRealTimeUpdates();

        // Show dashboard by default
        showDashboard();
    }

    public void addBalanceListener(BalanceListener listener) {
        balanceListeners.add(listener);
    }

    @Override
    public void onBalanceChanged(User user) {
        SwingUtilities.invokeLater(() -> {
            updateBalance();
            refreshRecentTransactions();
            if (lastUpdatedLabel != null) {
                lastUpdatedLabel.setText("Last updated: " +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            }
        });
    }

    private void notifyBalanceChanged() {
        for (BalanceListener listener : balanceListeners) {
            listener.onBalanceChanged(currentUser);
        }
    }

    private void startRealTimeUpdates() {
        // Refresh every 30 seconds
        refreshTimer = new Timer(30000, e -> {
            refreshUserBalance();
        });
        refreshTimer.start();
    }

    private void refreshUserBalance() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT current_balance FROM users WHERE user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, currentUser.getUserId());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double newBalance = rs.getDouble("current_balance");
                if (newBalance != currentUser.getCurrentBalance()) {
                    currentUser.setCurrentBalance(newBalance);
                    notifyBalanceChanged();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setBackground(new Color(33, 33, 33));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        // User info
        JLabel userLabel = new JLabel(currentUser.getUsername());
        userLabel.setFont(new Font("Arial", Font.BOLD, 18));
        userLabel.setForeground(Color.WHITE);
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(userLabel);

        JLabel phoneLabel = new JLabel(currentUser.getPhoneNumber());
        phoneLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        phoneLabel.setForeground(new Color(200, 200, 200));
        phoneLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(phoneLabel);

        sidebar.add(Box.createRigidArea(new Dimension(0, 30)));

        // Menu buttons with Insights added
        String[] menuItems = {"Dashboard", "Transactions", "Add Transaction", "Budgets", "Weekly Report", "Insights", "Logout"};
        Color[] colors = {
                new Color(25, 118, 210),   // Dashboard
                new Color(76, 175, 80),     // Transactions
                new Color(255, 152, 0),      // Add Transaction
                new Color(156, 39, 176),      // Budgets
                new Color(255, 87, 34),       // Report
                new Color(255, 193, 7),       // Insights (gold)
                new Color(244, 67, 54)        // Logout
        };

        for (int i = 0; i < menuItems.length; i++) {
            JButton menuButton = createSidebarButton(menuItems[i], colors[i]);
            final int index = i;
            menuButton.addActionListener(e -> {
                switch (index) {
                    case 0: showDashboard(); break;
                    case 1: showTransactions(); break;
                    case 2: showAddTransaction(); break;
                    case 3: showBudget(); break;
                    case 4: showReport(); break;
                    case 5: showInsights(); break;
                    case 6: logout(); break;
                }
            });
            sidebar.add(menuButton);
            sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        // Last updated label
        lastUpdatedLabel = new JLabel("Last updated: --:--:--");
        lastUpdatedLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        lastUpdatedLabel.setForeground(new Color(150, 150, 150));
        lastUpdatedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(lastUpdatedLabel);

        return sidebar;
    }

    private JButton createSidebarButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        button.setPreferredSize(new Dimension(200, 45));
        button.setMaximumSize(new Dimension(200, 45));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setContentAreaFilled(false);
        return button;
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top section with balance
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setBackground(Color.WHITE);

        JPanel balanceCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(25, 118, 210));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
            }
        };
        balanceCard.setLayout(new BorderLayout());
        balanceCard.setPreferredSize(new Dimension(300, 150));

        JLabel balanceTitle = new JLabel("Current Balance", SwingConstants.CENTER);
        balanceTitle.setFont(new Font("Arial", Font.PLAIN, 16));
        balanceTitle.setForeground(Color.WHITE);
        balanceCard.add(balanceTitle, BorderLayout.NORTH);

        balanceLabel = new JLabel(String.format("$%.2f", currentUser.getCurrentBalance()), SwingConstants.CENTER);
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 32));
        balanceLabel.setForeground(Color.WHITE);
        balanceCard.add(balanceLabel, BorderLayout.CENTER);

        topPanel.add(balanceCard);
        panel.add(topPanel, BorderLayout.NORTH);

        // Quick stats panel
        JPanel statsPanel = createQuickStatsPanel();
        panel.add(statsPanel, BorderLayout.CENTER);

        // Recent transactions
        JPanel recentPanel = new JPanel(new BorderLayout());
        recentPanel.setBackground(Color.WHITE);
        recentPanel.setBorder(BorderFactory.createTitledBorder("Recent Transactions"));
        recentPanel.setPreferredSize(new Dimension(600, 250));

        String[] columns = {"Date", "Type", "Amount", "Category"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        transactionsTable = new JTable(tableModel);
        transactionsTable.setRowHeight(30);
        transactionsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        transactionsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(transactionsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        recentPanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(recentPanel, BorderLayout.SOUTH);

        loadRecentTransactions();

        return panel;
    }

    private JPanel createQuickStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 20, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get today's spending
            String todayQuery = "SELECT COALESCE(SUM(amount), 0) as total FROM transactions " +
                    "WHERE user_id = ? AND type = 'WITHDRAWAL' AND DATE(transaction_date) = CURDATE()";
            PreparedStatement todayStmt = conn.prepareStatement(todayQuery);
            todayStmt.setInt(1, currentUser.getUserId());
            ResultSet todayRs = todayStmt.executeQuery();
            double todaySpent = todayRs.next() ? todayRs.getDouble("total") : 0;

            // Get this month's spending
            String monthQuery = "SELECT COALESCE(SUM(amount), 0) as total FROM transactions " +
                    "WHERE user_id = ? AND type = 'WITHDRAWAL' AND MONTH(transaction_date) = MONTH(NOW())";
            PreparedStatement monthStmt = conn.prepareStatement(monthQuery);
            monthStmt.setInt(1, currentUser.getUserId());
            ResultSet monthRs = monthStmt.executeQuery();
            double monthSpent = monthRs.next() ? monthRs.getDouble("total") : 0;

            // Get this month's income
            String incomeQuery = "SELECT COALESCE(SUM(amount), 0) as total FROM transactions " +
                    "WHERE user_id = ? AND type = 'DEPOSIT' AND MONTH(transaction_date) = MONTH(NOW())";
            PreparedStatement incomeStmt = conn.prepareStatement(incomeQuery);
            incomeStmt.setInt(1, currentUser.getUserId());
            ResultSet incomeRs = incomeStmt.executeQuery();
            double monthIncome = incomeRs.next() ? incomeRs.getDouble("total") : 0;

            panel.add(createStatCard("Today's Spending", String.format("$%.2f", todaySpent), new Color(255, 152, 0)));
            panel.add(createStatCard("Month's Spending", String.format("$%.2f", monthSpent), new Color(244, 67, 54)));
            panel.add(createStatCard("Month's Income", String.format("$%.2f", monthIncome), new Color(76, 175, 80)));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return panel;
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
            }
        };
        card.setPreferredSize(new Dimension(150, 80));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        titleLabel.setForeground(Color.WHITE);
        card.add(titleLabel, BorderLayout.NORTH);

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 18));
        valueLabel.setForeground(Color.WHITE);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createBudgetPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Budget Tracking", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(new Color(25, 118, 210));
        panel.add(title, BorderLayout.NORTH);

        JPanel budgetsPanel = new JPanel();
        budgetsPanel.setLayout(new BoxLayout(budgetsPanel, BoxLayout.Y_AXIS));
        budgetsPanel.setBackground(Color.WHITE);

        // Load budgets from database instead of sample data
        loadBudgets(budgetsPanel);

        JScrollPane scrollPane = new JScrollPane(budgetsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);

        // Add budget button
        JButton addBudgetBtn = Components.createRoundedButton("Set New Budget", new Color(76, 175, 80), Color.WHITE);
        addBudgetBtn.addActionListener(e -> showAddBudgetDialog());

        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(addBudgetBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void addBudgetBar(JPanel panel, String category, double limit, double spent) {
        JPanel barPanel = new JPanel(new BorderLayout());
        barPanel.setBackground(Color.WHITE);
        barPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        double percentage = (spent / limit) * 100;

        JLabel label = new JLabel(String.format("%s: $%.2f / $%.2f (%.1f%%)",
                category, spent, limit, percentage));
        label.setFont(new Font("Arial", Font.BOLD, 14));
        barPanel.add(label, BorderLayout.NORTH);

        JProgressBar progressBar = new JProgressBar(0, (int)limit);
        progressBar.setValue((int)spent);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Arial", Font.PLAIN, 12));
        progressBar.setString(String.format("$%.0f / $%.0f", spent, limit));

        if (percentage > 100) {
            progressBar.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this,
                    "⚠️ Alert: You've exceeded your " + category + " budget!",
                    "Budget Alert",
                    JOptionPane.WARNING_MESSAGE);
        } else if (percentage > 90) {
            progressBar.setForeground(Color.ORANGE);
        } else if (percentage > 75) {
            progressBar.setForeground(Color.YELLOW);
        } else {
            progressBar.setForeground(new Color(76, 175, 80));
        }

        barPanel.add(progressBar, BorderLayout.CENTER);
        panel.add(barPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    private void showAddBudgetDialog() {
        JDialog dialog = new JDialog(this, "Set Monthly Budget", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);

        JLabel titleLabel = new JLabel("Set Monthly Budget");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(25, 118, 210));
        panel.add(titleLabel, gbc);

        panel.add(Box.createRigidArea(new Dimension(0, 10)), gbc);

        panel.add(new JLabel("Category:"), gbc);
        JComboBox<String> categoryCombo = new JComboBox<>();

        // Load categories from database
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT category_name FROM categories WHERE category_type = 'EXPENSE'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                categoryCombo.addItem(rs.getString("category_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Fallback to hardcoded categories
            categoryCombo.addItem("Food & Dining");
            categoryCombo.addItem("Shopping");
            categoryCombo.addItem("Transportation");
            categoryCombo.addItem("Entertainment");
            categoryCombo.addItem("Bills & Utilities");
            categoryCombo.addItem("Healthcare");
        }

        panel.add(categoryCombo, gbc);

        panel.add(new JLabel("Monthly Limit ($):"), gbc);
        JTextField limitField = Components.createRoundedTextField(15);
        panel.add(limitField, gbc);

        panel.add(new JLabel(" "), gbc);

        JButton saveBtn = Components.createRoundedButton("Save Budget", new Color(76, 175, 80), Color.WHITE);
        saveBtn.addActionListener(e -> {
            String category = (String) categoryCombo.getSelectedItem();
            String limitStr = limitField.getText();

            try {
                double limit = Double.parseDouble(limitStr);

                try (Connection conn = DatabaseConnection.getConnection()) {
                    // Get category_id
                    String catQuery = "SELECT category_id FROM categories WHERE category_name = ?";
                    PreparedStatement catStmt = conn.prepareStatement(catQuery);
                    catStmt.setString(1, category);
                    ResultSet catRs = catStmt.executeQuery();

                    if (catRs.next()) {
                        int categoryId = catRs.getInt("category_id");

                        // Check if budget exists
                        String checkQuery = "SELECT budget_id FROM budgets WHERE user_id = ? AND category_id = ?";
                        PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                        checkStmt.setInt(1, currentUser.getUserId());
                        checkStmt.setInt(2, categoryId);
                        ResultSet checkRs = checkStmt.executeQuery();

                        if (checkRs.next()) {
                            // Update existing budget
                            String updateQuery = "UPDATE budgets SET monthly_limit = ? WHERE user_id = ? AND category_id = ?";
                            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                            updateStmt.setDouble(1, limit);
                            updateStmt.setInt(2, currentUser.getUserId());
                            updateStmt.setInt(3, categoryId);
                            updateStmt.executeUpdate();
                        } else {
                            // Insert new budget
                            String insertQuery = "INSERT INTO budgets (user_id, category_id, monthly_limit) VALUES (?, ?, ?)";
                            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                            insertStmt.setInt(1, currentUser.getUserId());
                            insertStmt.setInt(2, categoryId);
                            insertStmt.setDouble(3, limit);
                            insertStmt.executeUpdate();
                        }

                        JOptionPane.showMessageDialog(dialog, "Budget saved successfully!");
                        dialog.dispose();
                        showBudget(); // Refresh budget view
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid number");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Database error: " + ex.getMessage());
            }
        });

        panel.add(saveBtn, gbc);

        JButton cancelBtn = Components.createRoundedButton("Cancel", new Color(158, 158, 158), Color.WHITE);
        cancelBtn.addActionListener(e -> dialog.dispose());
        panel.add(cancelBtn, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    // ===== BUDGET METHODS =====

    private void loadBudgets(JPanel budgetsPanel) {
        budgetsPanel.removeAll();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT b.*, c.category_name, " +
                    "COALESCE((SELECT SUM(amount) FROM transactions t " +
                    "WHERE t.user_id = b.user_id AND t.category_id = b.category_id " +
                    "AND t.type = 'WITHDRAWAL' " +
                    "AND MONTH(t.transaction_date) = MONTH(NOW()) " +
                    "AND YEAR(t.transaction_date) = YEAR(NOW())), 0) as spent " +
                    "FROM budgets b " +
                    "JOIN categories c ON b.category_id = c.category_id " +
                    "WHERE b.user_id = ?";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, currentUser.getUserId());
            ResultSet rs = pstmt.executeQuery();

            boolean hasBudgets = false;
            while (rs.next()) {
                hasBudgets = true;
                double limit = rs.getDouble("monthly_limit");
                double spent = rs.getDouble("spent");
                String category = rs.getString("category_name");
                int budgetId = rs.getInt("budget_id");

                // Update spent_so_far in budgets table
                String updateQuery = "UPDATE budgets SET spent_so_far = ? WHERE budget_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setDouble(1, spent);
                updateStmt.setInt(2, budgetId);
                updateStmt.executeUpdate();

                addBudgetBar(budgetsPanel, category, limit, spent);
            }

            if (!hasBudgets) {
                JLabel noBudgetsLabel = new JLabel("No budgets set. Click 'Set New Budget' to create one.");
                noBudgetsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                noBudgetsLabel.setForeground(Color.GRAY);
                noBudgetsLabel.setHorizontalAlignment(SwingConstants.CENTER);
                budgetsPanel.add(noBudgetsLabel);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading budgets: " + e.getMessage());
        }

        budgetsPanel.revalidate();
        budgetsPanel.repaint();
    }

    private void checkBudgetsAfterTransaction(int categoryId, double amount) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT b.*, c.category_name FROM budgets b " +
                    "JOIN categories c ON b.category_id = c.category_id " +
                    "WHERE b.user_id = ? AND b.category_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, currentUser.getUserId());
            pstmt.setInt(2, categoryId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double limit = rs.getDouble("monthly_limit");
                double spent = rs.getDouble("spent_so_far") + amount;
                String categoryName = rs.getString("category_name");
                double percentage = (spent / limit) * 100;

                // Update spent_so_far
                String updateQuery = "UPDATE budgets SET spent_so_far = ? WHERE budget_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setDouble(1, spent);
                updateStmt.setInt(2, rs.getInt("budget_id"));
                updateStmt.executeUpdate();

                if (percentage >= 100) {
                    JOptionPane.showMessageDialog(this,
                            "⚠️ You've exceeded your " + categoryName + " budget of $" + String.format("%.2f", limit) + "!",
                            "Budget Alert",
                            JOptionPane.WARNING_MESSAGE);
                } else if (percentage >= 90) {
                    JOptionPane.showMessageDialog(this,
                            "⚠️ You've used " + String.format("%.1f", percentage) +
                                    "% of your " + categoryName + " budget ($" + String.format("%.2f", spent) +
                                    " / $" + String.format("%.2f", limit) + ")",
                            "Budget Warning",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private JPanel createTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add search panel
        panel.add(createSearchPanel(), BorderLayout.NORTH);

        String[] columns = {"Date", "Type", "Amount", "Category", "Description", "Balance After"};
        DefaultTableModel transModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(transModel);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Export button
        JButton exportBtn = Components.createRoundedButton("Export to CSV", new Color(33, 150, 243), Color.WHITE);
        exportBtn.addActionListener(e -> exportToCSV());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(exportBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        // Load all transactions
        loadAllTransactions(transModel);

        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JTextField searchField = Components.createRoundedTextField(20);
        JComboBox<String> filterCombo = new JComboBox<>(
                new String[]{"All", "Deposits", "Withdrawals", "This Month", "Last Month"}
        );
        JButton searchBtn = Components.createRoundedButton("Search", new Color(25, 118, 210), Color.WHITE);

        panel.add(new JLabel("Search:"));
        panel.add(searchField);
        panel.add(new JLabel("Filter:"));
        panel.add(filterCombo);
        panel.add(searchBtn);

        return panel;
    }

    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File("transactions_export.csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(fileChooser.getSelectedFile());
                 Connection conn = DatabaseConnection.getConnection()) {

                writer.println("Date,Type,Amount,Category,Description,Balance After");

                String query = "SELECT t.*, c.category_name FROM transactions t " +
                        "JOIN categories c ON t.category_id = c.category_id " +
                        "WHERE t.user_id = ? ORDER BY t.transaction_date DESC";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, currentUser.getUserId());
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    writer.println(
                            rs.getTimestamp("transaction_date") + "," +
                                    rs.getString("type") + "," +
                                    rs.getDouble("amount") + "," +
                                    rs.getString("category_name") + "," +
                                    (rs.getString("description") != null ? rs.getString("description") : "") + "," +
                                    rs.getDouble("balance_after")
                    );
                }

                JOptionPane.showMessageDialog(this, "Export successful!");

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage());
            }
        }
    }

    private JPanel createAddTransactionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 50, 10, 50);

        JLabel title = new JLabel("Add New Transaction");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(new Color(25, 118, 210));
        panel.add(title, gbc);

        // Transaction type
        JLabel typeLabel = new JLabel("Transaction Type");
        typeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(typeLabel, gbc);

        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"DEPOSIT", "WITHDRAWAL"});
        typeCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        typeCombo.setPreferredSize(new Dimension(300, 40));
        panel.add(typeCombo, gbc);

        // Category
        JLabel catLabel = new JLabel("Category");
        catLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(catLabel, gbc);

        JComboBox<String> categoryCombo = new JComboBox<>();
        categoryCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        categoryCombo.setPreferredSize(new Dimension(300, 40));
        loadCategories(categoryCombo);
        panel.add(categoryCombo, gbc);

        // Amount
        JLabel amountLabel = new JLabel("Amount");
        amountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(amountLabel, gbc);

        JTextField amountField = Components.createRoundedTextField(20);
        panel.add(amountField, gbc);

        // Description
        JLabel descLabel = new JLabel("Description");
        descLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(descLabel, gbc);

        JTextField descField = Components.createRoundedTextField(20);
        panel.add(descField, gbc);

        // Add button
        JButton addButton = Components.createRoundedButton("Add Transaction", new Color(76, 175, 80), Color.WHITE);
        addButton.addActionListener(e -> {
            String type = (String) typeCombo.getSelectedItem();
            String category = (String) categoryCombo.getSelectedItem();
            String amountStr = amountField.getText();
            String description = descField.getText();

            try {
                double amount = Double.parseDouble(amountStr);
                addTransaction(type, category, amount, description);

                // Clear fields
                amountField.setText("");
                descField.setText("");

                // Refresh views
                refreshData();

                // Show success message
                JOptionPane.showMessageDialog(this, "Transaction added successfully!");

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid amount");
            }
        });
        panel.add(addButton, gbc);

        // Add recurring checkbox
        JCheckBox recurringCheck = new JCheckBox("Make this recurring");
        recurringCheck.setBackground(Color.WHITE);
        panel.add(recurringCheck, gbc);

        return panel;
    }

    private JPanel createReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel title = new JLabel("Weekly Spending Report", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(new Color(25, 118, 210));
        panel.add(title, BorderLayout.NORTH);

        // Report content
        JTextArea reportArea = new JTextArea();
        reportArea.setFont(new Font("Arial", Font.PLAIN, 14));
        reportArea.setEditable(false);
        reportArea.setMargin(new Insets(20, 20, 20, 20));

        generateWeeklyReport(reportArea);

        JScrollPane scrollPane = new JScrollPane(reportArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Refresh button
        JButton refreshBtn = Components.createRoundedButton("Refresh Report", new Color(25, 118, 210), Color.WHITE);
        refreshBtn.addActionListener(e -> generateWeeklyReport(reportArea));

        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void showDashboard() {
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, "DASHBOARD");
        refreshData();
    }

    private void showTransactions() {
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        contentPanel.remove(1);
        contentPanel.add(createTransactionsPanel(), "TRANSACTIONS", 1);
        cl.show(contentPanel, "TRANSACTIONS");
    }

    private void showAddTransaction() {
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, "ADD_TRANSACTION");
    }

    private void showBudget() {
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        contentPanel.remove(3);
        contentPanel.add(createBudgetPanel(), "BUDGET", 3);
        cl.show(contentPanel, "BUDGET");
    }

    private void showReport() {
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        contentPanel.remove(4);
        contentPanel.add(createReportPanel(), "REPORT", 4);
        cl.show(contentPanel, "REPORT");
    }

    private void showInsights() {
        JDialog insightsDialog = new JDialog(this, "Financial Insights", true);
        insightsDialog.setSize(600, 500);
        insightsDialog.setLocationRelativeTo(this);

        JTextArea insightsArea = new JTextArea();
        insightsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        insightsArea.setEditable(false);
        insightsArea.setMargin(new Insets(10, 10, 10, 10));

        InsightsGenerator insightsGen = new InsightsGenerator(currentUser);
        insightsArea.setText(insightsGen.generateAllInsights());

        JScrollPane scrollPane = new JScrollPane(insightsArea);
        insightsDialog.add(scrollPane);

        JButton closeBtn = Components.createRoundedButton("Close", new Color(100, 100, 100), Color.WHITE);
        closeBtn.addActionListener(e -> insightsDialog.dispose());

        JPanel btnPanel = new JPanel();
        btnPanel.add(closeBtn);
        insightsDialog.add(btnPanel, BorderLayout.SOUTH);

        insightsDialog.setVisible(true);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?",
                "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (refreshTimer != null) {
                refreshTimer.stop();
            }
            new LoginFrame().setVisible(true);
            dispose();
        }
    }

    private void loadCategories(JComboBox<String> combo) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT category_name FROM categories WHERE category_type IN ('INCOME', 'EXPENSE')";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                combo.addItem(rs.getString("category_name"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void addTransaction(String type, String category, double amount, String description) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            double newBalance;
            if (type.equals("DEPOSIT")) {
                newBalance = currentUser.getCurrentBalance() + amount;
            } else {
                if (amount > currentUser.getCurrentBalance()) {
                    JOptionPane.showMessageDialog(this, "Insufficient balance!");
                    return;
                }
                newBalance = currentUser.getCurrentBalance() - amount;
            }

            // Get category_id
            String catQuery = "SELECT category_id FROM categories WHERE category_name = ?";
            PreparedStatement catStmt = conn.prepareStatement(catQuery);
            catStmt.setString(1, category);
            ResultSet catRs = catStmt.executeQuery();
            int categoryId = catRs.next() ? catRs.getInt("category_id") : 1;

            // Insert transaction
            String transQuery = "INSERT INTO transactions (user_id, category_id, amount, type, description, balance_after, transaction_date) VALUES (?, ?, ?, ?, ?, ?, NOW())";
            PreparedStatement transStmt = conn.prepareStatement(transQuery);
            transStmt.setInt(1, currentUser.getUserId());
            transStmt.setInt(2, categoryId);
            transStmt.setDouble(3, amount);
            transStmt.setString(4, type);
            transStmt.setString(5, description);
            transStmt.setDouble(6, newBalance);
            transStmt.executeUpdate();

            // Update user balance
            String updateQuery = "UPDATE users SET current_balance = ? WHERE user_id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setDouble(1, newBalance);
            updateStmt.setInt(2, currentUser.getUserId());
            updateStmt.executeUpdate();

            conn.commit();

            currentUser.setCurrentBalance(newBalance);
            notifyBalanceChanged();

            // Check budgets after transaction (for withdrawals only)
            if (type.equals("WITHDRAWAL")) {
                checkBudgetsAfterTransaction(categoryId, amount);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding transaction: " + ex.getMessage());
        }
    }

    private void loadRecentTransactions() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT t.*, c.category_name FROM transactions t " +
                    "JOIN categories c ON t.category_id = c.category_id " +
                    "WHERE t.user_id = ? ORDER BY t.transaction_date DESC LIMIT 10";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, currentUser.getUserId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Timestamp date = rs.getTimestamp("transaction_date");
                String type = rs.getString("type");
                double amount = rs.getDouble("amount");
                String category = rs.getString("category_name");

                tableModel.addRow(new Object[]{
                        date.toString().substring(0, 16),
                        type,
                        String.format("$%.2f", amount),
                        category
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadAllTransactions(DefaultTableModel model) {
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT t.*, c.category_name FROM transactions t " +
                    "JOIN categories c ON t.category_id = c.category_id " +
                    "WHERE t.user_id = ? ORDER BY t.transaction_date DESC";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, currentUser.getUserId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Timestamp date = rs.getTimestamp("transaction_date");
                String type = rs.getString("type");
                double amount = rs.getDouble("amount");
                String category = rs.getString("category_name");
                String description = rs.getString("description");
                double balanceAfter = rs.getDouble("balance_after");

                model.addRow(new Object[]{
                        date.toString().substring(0, 16),
                        type,
                        String.format("$%.2f", amount),
                        category,
                        description != null ? description : "",
                        String.format("$%.2f", balanceAfter)
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void refreshRecentTransactions() {
        loadRecentTransactions();
    }

    private void refreshData() {
        updateBalance();
        loadRecentTransactions();
    }

    private void updateBalance() {
        if (balanceLabel != null) {
            balanceLabel.setText(String.format("$%.2f", currentUser.getCurrentBalance()));
        }
    }

    private void generateWeeklyReport(JTextArea reportArea) {
        StringBuilder report = new StringBuilder();
        report.append("WEEKLY SPENDING REPORT\n");
        report.append("======================\n\n");

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get weekly summary
            String query = "SELECT c.category_name, SUM(t.amount) as total " +
                    "FROM transactions t " +
                    "JOIN categories c ON t.category_id = c.category_id " +
                    "WHERE t.user_id = ? AND t.type = 'WITHDRAWAL' " +
                    "AND DATE(t.transaction_date) BETWEEN ? AND ? " +
                    "GROUP BY c.category_name " +
                    "ORDER BY total DESC";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, currentUser.getUserId());
            pstmt.setString(2, startDate.toString());
            pstmt.setString(3, endDate.toString());

            ResultSet rs = pstmt.executeQuery();

            double totalSpent = 0;
            report.append("Period: ").append(startDate).append(" to ").append(endDate).append("\n\n");
            report.append("Spending by Category:\n");
            report.append("--------------------\n");

            while (rs.next()) {
                String category = rs.getString("category_name");
                double amount = rs.getDouble("total");
                totalSpent += amount;
                report.append(String.format("%-20s $%.2f\n", category, amount));
            }

            report.append("\n");
            report.append("====================\n");
            report.append(String.format("TOTAL SPENT: $%.2f\n", totalSpent));

            // Get total income
            String incomeQuery = "SELECT SUM(amount) as total FROM transactions " +
                    "WHERE user_id = ? AND type = 'DEPOSIT' " +
                    "AND DATE(transaction_date) BETWEEN ? AND ?";
            PreparedStatement incStmt = conn.prepareStatement(incomeQuery);
            incStmt.setInt(1, currentUser.getUserId());
            incStmt.setString(2, startDate.toString());
            incStmt.setString(3, endDate.toString());

            ResultSet incRs = incStmt.executeQuery();
            if (incRs.next()) {
                double totalIncome = incRs.getDouble("total");
                report.append(String.format("TOTAL INCOME: $%.2f\n", totalIncome));
                report.append(String.format("NET CHANGE: $%.2f\n", totalIncome - totalSpent));
            }

            // Add insights using the new InsightsGenerator
            report.append("\n");
            InsightsGenerator insightsGen = new InsightsGenerator(currentUser);
            report.append(insightsGen.generateAllInsights());

        } catch (SQLException ex) {
            ex.printStackTrace();
            report.append("Error generating report");
        }

        reportArea.setText(report.toString());
    }
}