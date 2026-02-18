package gui;

import Database.DatabaseConnection;
import listeners.BalanceListener;
import models.*;
import utils.InsightsGenerator;
import utils.ReportExporter;
import utils.FontManager;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
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
    private static final String CURRENCY = "KSH";

    public MainFrame(User user) {
        this.currentUser = user;
        addBalanceListener(this);

        setTitle("Finance Tracker - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
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

    private String formatKSH(double amount) {
        return String.format(CURRENCY + " %,.2f", amount);
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
        sidebar.setPreferredSize(new Dimension(280, getHeight()));
        sidebar.setBackground(new Color(33, 33, 33));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(30, 15, 30, 15));

        // User info with Montserrat font
        JLabel userLabel = new JLabel(currentUser.getUsername());
        userLabel.setFont(FontManager.getBoldFont(20));
        userLabel.setForeground(Color.WHITE);
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(userLabel);

        JLabel phoneLabel = new JLabel(currentUser.getPhoneNumber());
        phoneLabel.setFont(FontManager.getRegularFont(12));
        phoneLabel.setForeground(new Color(200, 200, 200));
        phoneLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(phoneLabel);

        JLabel balanceLabel = new JLabel(formatKSH(currentUser.getCurrentBalance()));
        balanceLabel.setFont(FontManager.getBoldFont(16));
        balanceLabel.setForeground(new Color(76, 175, 80));
        balanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        balanceLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        sidebar.add(balanceLabel);

        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));

        // Menu buttons
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
        lastUpdatedLabel.setFont(FontManager.getItalicFont(10));
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
        button.setPreferredSize(new Dimension(220, 45));
        button.setMaximumSize(new Dimension(220, 45));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(FontManager.getBoldFont(14));
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setContentAreaFilled(false);
        return button;
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

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
        balanceCard.setPreferredSize(new Dimension(400, 180));

        JLabel balanceTitle = new JLabel("Current Balance", SwingConstants.CENTER);
        balanceTitle.setFont(FontManager.getRegularFont(18));
        balanceTitle.setForeground(Color.WHITE);
        balanceCard.add(balanceTitle, BorderLayout.NORTH);

        balanceLabel = new JLabel(formatKSH(currentUser.getCurrentBalance()), SwingConstants.CENTER);
        balanceLabel.setFont(FontManager.getBoldFont(36));
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
        recentPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                "Recent Transactions",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                FontManager.getBoldFont(14),
                new Color(25, 118, 210)
        ));
        recentPanel.setPreferredSize(new Dimension(800, 300));

        String[] columns = {"Date", "Type", "Amount", "Category"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        transactionsTable = new JTable(tableModel);
        transactionsTable.setRowHeight(35);
        transactionsTable.setFont(FontManager.getRegularFont(13));
        transactionsTable.getTableHeader().setFont(FontManager.getBoldFont(13));
        transactionsTable.getTableHeader().setBackground(new Color(240, 240, 240));

        // Center align amount column
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        transactionsTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

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
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 30, 20));

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

            panel.add(createStatCard("Today's Spending", formatKSH(todaySpent), new Color(255, 152, 0)));
            panel.add(createStatCard("Month's Spending", formatKSH(monthSpent), new Color(244, 67, 54)));
            panel.add(createStatCard("Month's Income", formatKSH(monthIncome), new Color(76, 175, 80)));

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
        card.setPreferredSize(new Dimension(200, 100));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(FontManager.getRegularFont(14));
        titleLabel.setForeground(Color.WHITE);
        card.add(titleLabel, BorderLayout.NORTH);

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(FontManager.getBoldFont(20));
        valueLabel.setForeground(Color.WHITE);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createBudgetPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Budget Tracking", SwingConstants.CENTER);
        title.setFont(FontManager.getBoldFont(28));
        title.setForeground(new Color(25, 118, 210));
        panel.add(title, BorderLayout.NORTH);

        JPanel budgetsPanel = new JPanel();
        budgetsPanel.setLayout(new BoxLayout(budgetsPanel, BoxLayout.Y_AXIS));
        budgetsPanel.setBackground(Color.WHITE);

        loadBudgets(budgetsPanel);

        JScrollPane scrollPane = new JScrollPane(budgetsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton addBudgetBtn = Components.createRoundedButton("Set New Budget", new Color(76, 175, 80), Color.WHITE);
        addBudgetBtn.setFont(FontManager.getBoldFont(14));
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

        JLabel label = new JLabel(String.format("%s: %s / %s (%.1f%%)",
                category, formatKSH(spent), formatKSH(limit), percentage));
        label.setFont(FontManager.getBoldFont(14));
        barPanel.add(label, BorderLayout.NORTH);

        JProgressBar progressBar = new JProgressBar(0, (int)limit);
        progressBar.setValue((int)spent);
        progressBar.setStringPainted(true);
        progressBar.setFont(FontManager.getRegularFont(12));
        progressBar.setString(String.format("%s / %s", formatKSH(spent), formatKSH(limit)));

        if (percentage > 100) {
            progressBar.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this,
                    "⚠️ Alert: You've exceeded your " + category + " budget!\n" +
                            "Budget: " + formatKSH(limit) + "\n" +
                            "Spent: " + formatKSH(spent),
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
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);

        JLabel titleLabel = new JLabel("Set Monthly Budget");
        titleLabel.setFont(FontManager.getBoldFont(20));
        titleLabel.setForeground(new Color(25, 118, 210));
        panel.add(titleLabel, gbc);

        panel.add(Box.createRigidArea(new Dimension(0, 10)), gbc);

        panel.add(new JLabel("Category:"), gbc);
        JComboBox<String> categoryCombo = new JComboBox<>();
        categoryCombo.setFont(FontManager.getRegularFont(14));

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT category_name FROM categories WHERE category_type = 'EXPENSE'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                categoryCombo.addItem(rs.getString("category_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            String[] fallback = {"Food & Dining", "Shopping", "Transportation", "Entertainment", "Bills & Utilities", "Healthcare"};
            for (String cat : fallback) {
                categoryCombo.addItem(cat);
            }
        }

        panel.add(categoryCombo, gbc);

        panel.add(new JLabel("Monthly Limit (" + CURRENCY + "):"), gbc);
        JTextField limitField = Components.createRoundedTextField(15);
        limitField.setFont(FontManager.getRegularFont(14));
        panel.add(limitField, gbc);

        panel.add(new JLabel(" "), gbc);

        JButton saveBtn = Components.createRoundedButton("Save Budget", new Color(76, 175, 80), Color.WHITE);
        saveBtn.setFont(FontManager.getBoldFont(14));
        saveBtn.addActionListener(e -> {
            String category = (String) categoryCombo.getSelectedItem();
            String limitStr = limitField.getText();

            try {
                double limit = Double.parseDouble(limitStr);

                try (Connection conn = DatabaseConnection.getConnection()) {
                    String catQuery = "SELECT category_id FROM categories WHERE category_name = ?";
                    PreparedStatement catStmt = conn.prepareStatement(catQuery);
                    catStmt.setString(1, category);
                    ResultSet catRs = catStmt.executeQuery();

                    if (catRs.next()) {
                        int categoryId = catRs.getInt("category_id");

                        String checkQuery = "SELECT budget_id FROM budgets WHERE user_id = ? AND category_id = ?";
                        PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                        checkStmt.setInt(1, currentUser.getUserId());
                        checkStmt.setInt(2, categoryId);
                        ResultSet checkRs = checkStmt.executeQuery();

                        if (checkRs.next()) {
                            String updateQuery = "UPDATE budgets SET monthly_limit = ? WHERE user_id = ? AND category_id = ?";
                            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                            updateStmt.setDouble(1, limit);
                            updateStmt.setInt(2, currentUser.getUserId());
                            updateStmt.setInt(3, categoryId);
                            updateStmt.executeUpdate();
                        } else {
                            String insertQuery = "INSERT INTO budgets (user_id, category_id, monthly_limit) VALUES (?, ?, ?)";
                            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                            insertStmt.setInt(1, currentUser.getUserId());
                            insertStmt.setInt(2, categoryId);
                            insertStmt.setDouble(3, limit);
                            insertStmt.executeUpdate();
                        }

                        JOptionPane.showMessageDialog(dialog, "Budget saved successfully!");
                        dialog.dispose();
                        showBudget();
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
        cancelBtn.setFont(FontManager.getBoldFont(14));
        cancelBtn.addActionListener(e -> dialog.dispose());
        panel.add(cancelBtn, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void loadBudgets(JPanel budgetsPanel) {
        budgetsPanel.removeAll();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT b.*, c.category_name, " +
                    "COALESCE((SELECT SUM(amount) FROM transactions t " +
                    "WHERE t.user_id = b.user_id AND t.category_id = b.category_id " +
                    "AND t.type = 'WITHDRAWAL' " +
                    "AND MONTH(t.transaction_date) = MONTH(NOW())), 0) as spent " +
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

                String updateQuery = "UPDATE budgets SET spent_so_far = ? WHERE budget_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setDouble(1, spent);
                updateStmt.setInt(2, budgetId);
                updateStmt.executeUpdate();

                addBudgetBar(budgetsPanel, category, limit, spent);
            }

            if (!hasBudgets) {
                JLabel noBudgetsLabel = new JLabel("No budgets set. Click 'Set New Budget' to create one.");
                noBudgetsLabel.setFont(FontManager.getRegularFont(14));
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

                String updateQuery = "UPDATE budgets SET spent_so_far = ? WHERE budget_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setDouble(1, spent);
                updateStmt.setInt(2, rs.getInt("budget_id"));
                updateStmt.executeUpdate();

                if (percentage >= 100) {
                    JOptionPane.showMessageDialog(this,
                            "⚠️ You've exceeded your " + categoryName + " budget!\n" +
                                    "Budget: " + formatKSH(limit) + "\n" +
                                    "Spent: " + formatKSH(spent) + "\n" +
                                    "Overspent: " + formatKSH(spent - limit),
                            "Budget Alert",
                            JOptionPane.WARNING_MESSAGE);
                } else if (percentage >= 90) {
                    JOptionPane.showMessageDialog(this,
                            "⚠️ You've used " + String.format("%.1f", percentage) +
                                    "% of your " + categoryName + " budget\n" +
                                    "Spent: " + formatKSH(spent) + " / " + formatKSH(limit),
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
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        panel.add(createSearchPanel(), BorderLayout.NORTH);

        String[] columns = {"Date", "Type", "Amount", "Category", "Description", "Balance After"};
        DefaultTableModel transModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(transModel);
        table.setRowHeight(35);
        table.setFont(FontManager.getRegularFont(13));
        table.getTableHeader().setFont(FontManager.getBoldFont(13));
        table.getTableHeader().setBackground(new Color(240, 240, 240));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);

        JButton exportBtn = Components.createRoundedButton("Export to CSV", new Color(33, 150, 243), Color.WHITE);
        exportBtn.setFont(FontManager.getBoldFont(14));
        exportBtn.addActionListener(e -> exportToCSV());
        btnPanel.add(exportBtn);

        JButton exportReportBtn = Components.createRoundedButton("Export Report", new Color(156, 39, 176), Color.WHITE);
        exportReportBtn.setFont(FontManager.getBoldFont(14));
        exportReportBtn.addActionListener(e -> {
            InsightsGenerator insightsGen = new InsightsGenerator(currentUser);
            String report = insightsGen.generateAllInsights();
            ReportExporter.exportToText(report, this);
        });
        btnPanel.add(exportReportBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);

        loadAllTransactions(transModel);

        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(FontManager.getBoldFont(14));
        panel.add(searchLabel);

        JTextField searchField = Components.createRoundedTextField(20);
        searchField.setFont(FontManager.getRegularFont(14));
        panel.add(searchField);

        JLabel filterLabel = new JLabel("Filter:");
        filterLabel.setFont(FontManager.getBoldFont(14));
        panel.add(filterLabel);

        JComboBox<String> filterCombo = new JComboBox<>(
                new String[]{"All", "Deposits", "Withdrawals", "This Month", "Last Month"}
        );
        filterCombo.setFont(FontManager.getRegularFont(14));
        panel.add(filterCombo);

        JButton searchBtn = Components.createRoundedButton("Search", new Color(25, 118, 210), Color.WHITE);
        searchBtn.setFont(FontManager.getBoldFont(14));
        panel.add(searchBtn);

        return panel;
    }

    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        fileChooser.setSelectedFile(new java.io.File("transactions_" + timestamp + ".csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(fileChooser.getSelectedFile());
                 Connection conn = DatabaseConnection.getConnection()) {

                writer.println("Date,Type,Amount (KSH),Category,Description,Balance After (KSH)");

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

                JOptionPane.showMessageDialog(this,
                        "✅ Export successful!\nSaved to: " + fileChooser.getSelectedFile().getName(),
                        "Export Complete",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "❌ Export failed: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createAddTransactionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 50, 10, 50);

        JLabel title = new JLabel("Add New Transaction");
        title.setFont(FontManager.getBoldFont(28));
        title.setForeground(new Color(25, 118, 210));
        panel.add(title, gbc);

        JLabel typeLabel = new JLabel("Transaction Type");
        typeLabel.setFont(FontManager.getBoldFont(14));
        panel.add(typeLabel, gbc);

        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"DEPOSIT", "WITHDRAWAL"});
        typeCombo.setFont(FontManager.getRegularFont(14));
        typeCombo.setPreferredSize(new Dimension(350, 40));
        panel.add(typeCombo, gbc);

        JLabel catLabel = new JLabel("Category");
        catLabel.setFont(FontManager.getBoldFont(14));
        panel.add(catLabel, gbc);

        JComboBox<String> categoryCombo = new JComboBox<>();
        categoryCombo.setFont(FontManager.getRegularFont(14));
        categoryCombo.setPreferredSize(new Dimension(350, 40));
        loadCategories(categoryCombo);
        panel.add(categoryCombo, gbc);

        JLabel amountLabel = new JLabel("Amount (" + CURRENCY + ")");
        amountLabel.setFont(FontManager.getBoldFont(14));
        panel.add(amountLabel, gbc);

        JTextField amountField = Components.createRoundedTextField(20);
        amountField.setFont(FontManager.getRegularFont(14));
        panel.add(amountField, gbc);

        JLabel descLabel = new JLabel("Description");
        descLabel.setFont(FontManager.getBoldFont(14));
        panel.add(descLabel, gbc);

        JTextField descField = Components.createRoundedTextField(20);
        descField.setFont(FontManager.getRegularFont(14));
        panel.add(descField, gbc);

        JButton addButton = Components.createRoundedButton("Add Transaction", new Color(76, 175, 80), Color.WHITE);
        addButton.setFont(FontManager.getBoldFont(14));
        addButton.addActionListener(e -> {
            String type = (String) typeCombo.getSelectedItem();
            String category = (String) categoryCombo.getSelectedItem();
            String amountStr = amountField.getText();
            String description = descField.getText();

            try {
                double amount = Double.parseDouble(amountStr);
                addTransaction(type, category, amount, description);

                amountField.setText("");
                descField.setText("");

                refreshData();

                JOptionPane.showMessageDialog(this,
                        "✅ Transaction added successfully!\n" +
                                "Amount: " + formatKSH(amount) + "\n" +
                                "New Balance: " + formatKSH(currentUser.getCurrentBalance()),
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "❌ Please enter a valid amount",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(addButton, gbc);

        JCheckBox recurringCheck = new JCheckBox("Make this recurring");
        recurringCheck.setBackground(Color.WHITE);
        recurringCheck.setFont(FontManager.getRegularFont(12));
        panel.add(recurringCheck, gbc);

        return panel;
    }

    private JPanel createReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Weekly Spending Report", SwingConstants.CENTER);
        title.setFont(FontManager.getBoldFont(28));
        title.setForeground(new Color(25, 118, 210));
        panel.add(title, BorderLayout.NORTH);

        JTextArea reportArea = new JTextArea();
        reportArea.setFont(FontManager.getRegularFont(13));
        reportArea.setEditable(false);
        reportArea.setMargin(new Insets(20, 20, 20, 20));

        generateWeeklyReport(reportArea);

        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(Color.WHITE);

        JButton refreshBtn = Components.createRoundedButton("Refresh Report", new Color(25, 118, 210), Color.WHITE);
        refreshBtn.setFont(FontManager.getBoldFont(14));
        refreshBtn.addActionListener(e -> generateWeeklyReport(reportArea));
        btnPanel.add(refreshBtn);

        JButton exportTextBtn = Components.createRoundedButton("Export as Text", new Color(76, 175, 80), Color.WHITE);
        exportTextBtn.setFont(FontManager.getBoldFont(14));
        exportTextBtn.addActionListener(e -> {
            ReportExporter.exportToText(reportArea.getText(), this);
        });
        btnPanel.add(exportTextBtn);

        JButton exportHTMLBtn = Components.createRoundedButton("Export as HTML", new Color(255, 152, 0), Color.WHITE);
        exportHTMLBtn.setFont(FontManager.getBoldFont(14));
        exportHTMLBtn.addActionListener(e -> {
            InsightsGenerator insightsGen = new InsightsGenerator(currentUser);
            String htmlReport = generateHTMLWeeklyReport();
            ReportExporter.exportToHTML(htmlReport, this);
        });
        btnPanel.add(exportHTMLBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private String generateHTMLWeeklyReport() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html><head>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<title>Weekly Spending Report</title>\n");
        html.append("<style>\n");
        html.append("@import url('https://fonts.googleapis.com/css2?family=Montserrat+Alternates:wght@400;700&display=swap');\n");
        html.append("body { font-family: 'Montserrat Alternates', sans-serif; padding: 30px; background: #f5f5f5; }\n");
        html.append(".container { max-width: 1000px; margin: 0 auto; background: white; padding: 30px; border-radius: 15px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }\n");
        html.append("h1 { color: #1976d2; text-align: center; border-bottom: 3px solid #1976d2; padding-bottom: 15px; }\n");
        html.append("h2 { color: #2e7d32; margin-top: 25px; }\n");
        html.append(".stats { background: #e3f2fd; padding: 20px; border-radius: 10px; margin: 20px 0; }\n");
        html.append(".amount { font-size: 1.2em; font-weight: bold; color: #1976d2; }\n");
        html.append(".positive { color: #2e7d32; }\n");
        html.append(".negative { color: #c62828; }\n");
        html.append("table { width: 100%; border-collapse: collapse; margin: 20px 0; }\n");
        html.append("th { background: #1976d2; color: white; padding: 12px; text-align: left; }\n");
        html.append("td { padding: 10px; border-bottom: 1px solid #ddd; }\n");
        html.append("tr:hover { background: #f5f5f5; }\n");
        html.append(".footer { text-align: center; margin-top: 30px; color: #666; font-size: 0.9em; }\n");
        html.append("</style>\n");
        html.append("</head><body>\n");
        html.append("<div class='container'>\n");

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);

        try (Connection conn = DatabaseConnection.getConnection()) {
            html.append("<h1>📊 Weekly Spending Report</h1>");
            html.append("<p style='text-align: center;'>Period: ").append(startDate).append(" to ").append(endDate).append("</p>");

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
            html.append("<h2>📈 Spending by Category</h2>");
            html.append("<table>");
            html.append("<tr><th>Category</th><th>Amount (").append(CURRENCY).append(")</th></tr>");

            while (rs.next()) {
                String category = rs.getString("category_name");
                double amount = rs.getDouble("total");
                totalSpent += amount;
                html.append("<tr>");
                html.append("<td>").append(category).append("</td>");
                html.append("<td><span class='amount'>").append(formatKSH(amount)).append("</span></td>");
                html.append("</tr>");
            }

            html.append("</table>");

            html.append("<div class='stats'>");
            html.append("<p><strong>Total Spent:</strong> <span class='amount negative'>")
                    .append(formatKSH(totalSpent)).append("</span></p>");

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
                html.append("<p><strong>Total Income:</strong> <span class='amount positive'>")
                        .append(formatKSH(totalIncome)).append("</span></p>");
                html.append("<p><strong>Net Change:</strong> <span class='amount'>")
                        .append(formatKSH(totalIncome - totalSpent)).append("</span></p>");
            }

            html.append("</div>");

            InsightsGenerator insightsGen = new InsightsGenerator(currentUser);
            html.append(insightsGen.generateHTMLInsights());

        } catch (SQLException ex) {
            ex.printStackTrace();
            html.append("<p style='color: red;'>Error generating report</p>");
        }

        html.append("<div class='footer'>");
        html.append("Generated on: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm:ss")));
        html.append("<br>📌 Keep tracking your finances daily!");
        html.append("</div>");

        html.append("</div></body></html>");
        return html.toString();
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
        insightsDialog.setSize(700, 600);
        insightsDialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        JTextArea insightsArea = new JTextArea();
        insightsArea.setFont(FontManager.getRegularFont(13));
        insightsArea.setEditable(false);
        insightsArea.setMargin(new Insets(20, 20, 20, 20));

        InsightsGenerator insightsGen = new InsightsGenerator(currentUser);
        insightsArea.setText(insightsGen.generateAllInsights());

        JScrollPane scrollPane = new JScrollPane(insightsArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(Color.WHITE);

        JButton refreshBtn = Components.createRoundedButton("Refresh", new Color(25, 118, 210), Color.WHITE);
        refreshBtn.setFont(FontManager.getBoldFont(14));
        refreshBtn.addActionListener(e -> {
            InsightsGenerator gen = new InsightsGenerator(currentUser);
            insightsArea.setText(gen.generateAllInsights());
        });
        btnPanel.add(refreshBtn);

        JButton exportBtn = Components.createRoundedButton("Export as Text", new Color(76, 175, 80), Color.WHITE);
        exportBtn.setFont(FontManager.getBoldFont(14));
        exportBtn.addActionListener(e -> {
            ReportExporter.exportToText(insightsArea.getText(), this);
        });
        btnPanel.add(exportBtn);

        JButton exportHTMLBtn = Components.createRoundedButton("Export as HTML", new Color(255, 152, 0), Color.WHITE);
        exportHTMLBtn.setFont(FontManager.getBoldFont(14));
        exportHTMLBtn.addActionListener(e -> {
            InsightsGenerator gen = new InsightsGenerator(currentUser);
            String htmlReport = gen.generateHTMLInsights();
            ReportExporter.exportToHTML(htmlReport, this);
        });
        btnPanel.add(exportHTMLBtn);

        JButton closeBtn = Components.createRoundedButton("Close", new Color(100, 100, 100), Color.WHITE);
        closeBtn.setFont(FontManager.getBoldFont(14));
        closeBtn.addActionListener(e -> insightsDialog.dispose());
        btnPanel.add(closeBtn);

        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        insightsDialog.add(mainPanel);
        insightsDialog.setVisible(true);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (refreshTimer != null) {
                refreshTimer.stop();
            }
            JOptionPane.showMessageDialog(this,
                    "Thank you for using Finance Tracker!\nSee you next time.",
                    "Goodbye",
                    JOptionPane.INFORMATION_MESSAGE);
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
                    JOptionPane.showMessageDialog(this,
                            "❌ Insufficient balance!\n" +
                                    "Current: " + formatKSH(currentUser.getCurrentBalance()) + "\n" +
                                    "Attempted: " + formatKSH(amount),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                newBalance = currentUser.getCurrentBalance() - amount;
            }

            String catQuery = "SELECT category_id FROM categories WHERE category_name = ?";
            PreparedStatement catStmt = conn.prepareStatement(catQuery);
            catStmt.setString(1, category);
            ResultSet catRs = catStmt.executeQuery();
            int categoryId = catRs.next() ? catRs.getInt("category_id") : 1;

            String transQuery = "INSERT INTO transactions (user_id, category_id, amount, type, description, balance_after, transaction_date) VALUES (?, ?, ?, ?, ?, ?, NOW())";
            PreparedStatement transStmt = conn.prepareStatement(transQuery);
            transStmt.setInt(1, currentUser.getUserId());
            transStmt.setInt(2, categoryId);
            transStmt.setDouble(3, amount);
            transStmt.setString(4, type);
            transStmt.setString(5, description);
            transStmt.setDouble(6, newBalance);
            transStmt.executeUpdate();

            String updateQuery = "UPDATE users SET current_balance = ? WHERE user_id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setDouble(1, newBalance);
            updateStmt.setInt(2, currentUser.getUserId());
            updateStmt.executeUpdate();

            conn.commit();

            currentUser.setCurrentBalance(newBalance);
            notifyBalanceChanged();

            if (type.equals("WITHDRAWAL")) {
                checkBudgetsAfterTransaction(categoryId, amount);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "❌ Error adding transaction: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
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
                        formatKSH(amount),
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
                        formatKSH(amount),
                        category,
                        description != null ? description : "",
                        formatKSH(balanceAfter)
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
            balanceLabel.setText(formatKSH(currentUser.getCurrentBalance()));
        }
    }

    private void generateWeeklyReport(JTextArea reportArea) {
        StringBuilder report = new StringBuilder();
        report.append("                    WEEKLY SPENDING REPORT                 \n");

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);

        try (Connection conn = DatabaseConnection.getConnection()) {
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
            report.append("SPENDING BY CATEGORY\n");
            report.append("──────────────────────────────────────────────────────\n");

            while (rs.next()) {
                String category = rs.getString("category_name");
                double amount = rs.getDouble("total");
                totalSpent += amount;
                report.append(String.format("  %-20s %s\n", category, formatKSH(amount)));
            }

            report.append("\n");
            report.append("──────────────────────────────────────────────────────\n");
            report.append(String.format("  %-20s %s\n", "TOTAL SPENT:", formatKSH(totalSpent)));

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
                report.append(String.format("  %-20s %s\n", "TOTAL INCOME:", formatKSH(totalIncome)));
                report.append(String.format("  %-20s %s\n", "NET CHANGE:", formatKSH(totalIncome - totalSpent)));
            }

            report.append("\n");
            InsightsGenerator insightsGen = new InsightsGenerator(currentUser);
            report.append(insightsGen.generateAllInsights());

        } catch (SQLException ex) {
            ex.printStackTrace();
            report.append("Error generating report: " + ex.getMessage());
        }

        reportArea.setText(report.toString());
    }
}