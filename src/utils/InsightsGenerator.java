package utils;

import Database.DatabaseConnection;
import models.User;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class InsightsGenerator {

    private User currentUser;
    private StringBuilder insights;
    private Connection conn;
    private static final String CURRENCY = "KSH";
    private static final String CURRENCY_SYMBOL = "KSH";

    public InsightsGenerator(User user) {
        this.currentUser = user;
        this.insights = new StringBuilder();
    }

    private String formatKSH(double amount) {
        return String.format(CURRENCY_SYMBOL + " %,.2f", amount);
    }

    public String generateAllInsights() {
        insights = new StringBuilder();
        insights.append("╔════════════════════════════════════════════════════════════╗\n");
        insights.append("║           FINANCIAL INSIGHTS & RECOMMENDATIONS            ║\n");
        insights.append("╚════════════════════════════════════════════════════════════╝\n\n");

        try (Connection conn = DatabaseConnection.getConnection()) {
            this.conn = conn;

            generateSpendingAnalysis();
            generateSavingsAnalysis();
            generateMonthlyComparison();
            generateSmartRecommendations();

        } catch (SQLException e) {
            e.printStackTrace();
            return "Error generating insights: " + e.getMessage();
        }

        insights.append("\n══════════════════════════════════════════════════════════════\n");
        insights.append("📌 Keep tracking your finances daily!\n");
        insights.append("Generated on: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));

        return insights.toString();
    }

    public String generateHTMLInsights() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html><head>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
        html.append("<title>Financial Insights Report</title>\n");
        html.append("<style>\n");
        html.append("@import url('https://fonts.googleapis.com/css2?family=Montserrat+Alternates:wght@400;500;600;700&display=swap');\n");
        html.append("* { margin: 0; padding: 0; box-sizing: border-box; }\n");
        html.append("body { \n");
        html.append("  font-family: 'Montserrat Alternates', sans-serif; \n");
        html.append("  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n");
        html.append("  min-height: 100vh;\n");
        html.append("  padding: 40px 20px;\n");
        html.append("}\n");
        html.append(".container { \n");
        html.append("  max-width: 1000px; \n");
        html.append("  margin: 0 auto; \n");
        html.append("  background: white; \n");
        html.append("  padding: 40px; \n");
        html.append("  border-radius: 30px; \n");
        html.append("  box-shadow: 0 20px 60px rgba(0,0,0,0.3);\n");
        html.append("  animation: slideIn 0.5s ease-out;\n");
        html.append("}\n");
        html.append("@keyframes slideIn {\n");
        html.append("  from { transform: translateY(30px); opacity: 0; }\n");
        html.append("  to { transform: translateY(0); opacity: 1; }\n");
        html.append("}\n");
        html.append("h1 { \n");
        html.append("  color: #1976d2; \n");
        html.append("  text-align: center; \n");
        html.append("  font-size: 2.5em;\n");
        html.append("  font-weight: 700;\n");
        html.append("  margin-bottom: 20px;\n");
        html.append("  padding-bottom: 20px;\n");
        html.append("  border-bottom: 4px solid #1976d2;\n");
        html.append("  position: relative;\n");
        html.append("}\n");
        html.append("h1::after {\n");
        html.append("  content: '📊';\n");
        html.append("  position: absolute;\n");
        html.append("  right: 20px;\n");
        html.append("  top: 50%;\n");
        html.append("  transform: translateY(-50%);\n");
        html.append("  font-size: 1.2em;\n");
        html.append("}\n");
        html.append("h2 { \n");
        html.append("  color: #2e7d32; \n");
        html.append("  font-size: 1.8em;\n");
        html.append("  font-weight: 600;\n");
        html.append("  margin: 30px 0 20px;\n");
        html.append("  padding-left: 15px;\n");
        html.append("  border-left: 5px solid #1976d2;\n");
        html.append("}\n");
        html.append("h3 {\n");
        html.append("  color: #1565c0;\n");
        html.append("  font-size: 1.4em;\n");
        html.append("  margin: 20px 0 10px;\n");
        html.append("}\n");
        html.append(".section { \n");
        html.append("  margin: 30px 0; \n");
        html.append("  padding: 25px; \n");
        html.append("  background: #f8f9fa; \n");
        html.append("  border-radius: 20px; \n");
        html.append("  transition: transform 0.3s;\n");
        html.append("}\n");
        html.append(".section:hover {\n");
        html.append("  transform: translateX(10px);\n");
        html.append("}\n");
        html.append(".amount { \n");
        html.append("  font-weight: 700; \n");
        html.append("  color: #1976d2; \n");
        html.append("  font-size: 1.2em;\n");
        html.append("  background: #e3f2fd;\n");
        html.append("  padding: 5px 15px;\n");
        html.append("  border-radius: 30px;\n");
        html.append("  display: inline-block;\n");
        html.append("}\n");
        html.append(".positive { color: #2e7d32; }\n");
        html.append(".negative { color: #c62828; }\n");
        html.append(".warning { \n");
        html.append("  color: #ff6f00; \n");
        html.append("  font-weight: 600;\n");
        html.append("  background: #fff3e0;\n");
        html.append("  padding: 10px;\n");
        html.append("  border-radius: 10px;\n");
        html.append("  border-left: 4px solid #ff6f00;\n");
        html.append("}\n");
        html.append(".stat-card {\n");
        html.append("  display: inline-block;\n");
        html.append("  background: white;\n");
        html.append("  padding: 20px;\n");
        html.append("  border-radius: 15px;\n");
        html.append("  box-shadow: 0 4px 6px rgba(0,0,0,0.1);\n");
        html.append("  margin: 10px;\n");
        html.append("  min-width: 200px;\n");
        html.append("}\n");
        html.append("ul { \n");
        html.append("  list-style-type: none; \n");
        html.append("  padding-left: 0; \n");
        html.append("}\n");
        html.append("li { \n");
        html.append("  margin: 15px 0; \n");
        html.append("  padding: 15px 20px; \n");
        html.append("  background: white; \n");
        html.append("  border-radius: 15px; \n");
        html.append("  box-shadow: 0 2px 4px rgba(0,0,0,0.05);\n");
        html.append("  display: flex;\n");
        html.append("  justify-content: space-between;\n");
        html.append("  align-items: center;\n");
        html.append("  transition: all 0.3s;\n");
        html.append("}\n");
        html.append("li:hover {\n");
        html.append("  box-shadow: 0 4px 8px rgba(0,0,0,0.1);\n");
        html.append("  transform: scale(1.02);\n");
        html.append("}\n");
        html.append(".badge {\n");
        html.append("  background: #1976d2;\n");
        html.append("  color: white;\n");
        html.append("  padding: 5px 15px;\n");
        html.append("  border-radius: 30px;\n");
        html.append("  font-size: 0.9em;\n");
        html.append("  font-weight: 600;\n");
        html.append("}\n");
        html.append(".grid {\n");
        html.append("  display: grid;\n");
        html.append("  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));\n");
        html.append("  gap: 20px;\n");
        html.append("  margin: 20px 0;\n");
        html.append("}\n");
        html.append(".footer { \n");
        html.append("  text-align: center; \n");
        html.append("  margin-top: 40px; \n");
        html.append("  padding-top: 20px;\n");
        html.append("  color: #666; \n");
        html.append("  font-size: 0.95em;\n");
        html.append("  border-top: 2px dashed #1976d2;\n");
        html.append("}\n");
        html.append(".signature {\n");
        html.append("  font-family: 'Montserrat Alternates', cursive;\n");
        html.append("  color: #1976d2;\n");
        html.append("  font-size: 1.1em;\n");
        html.append("  margin-top: 10px;\n");
        html.append("}\n");
        html.append("</style>\n");
        html.append("</head><body>\n");
        html.append("<div class='container'>\n");

        html.append("<h1>📊 Financial Insights & Recommendations</h1>");

        try (Connection conn = DatabaseConnection.getConnection()) {
            this.conn = conn;
            html.append(generateHTMLSpendingAnalysis());
            html.append(generateHTMLSavingsAnalysis());
            html.append(generateHTMLMonthlyComparison());
            html.append(generateHTMLSmartRecommendations());
        } catch (SQLException e) {
            e.printStackTrace();
            html.append("<p style='color: red;'>Error generating insights</p>");
        }

        html.append("<div class='footer'>");
        html.append("<div>📌 Keep tracking your finances daily!</div>");
        html.append("<div class='signature'>");
        html.append("Generated on: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        html.append("</div>");
        html.append("</div>\n");

        html.append("</div></body></html>");
        return html.toString();
    }

    public String generateSpendingAnalysis() {
        StringBuilder spendingInsights = new StringBuilder();
        spendingInsights.append("\n💰 SPENDING ANALYSIS\n");
        spendingInsights.append("══════════════════════════════════════════════════════════════\n");

        try {
            // Average daily spending
            String avgQuery = "SELECT AVG(daily_total) as avg_daily FROM (" +
                    "SELECT DATE(transaction_date) as day, SUM(amount) as daily_total " +
                    "FROM transactions WHERE user_id = ? AND type = 'WITHDRAWAL' " +
                    "GROUP BY DATE(transaction_date)) as daily";
            PreparedStatement avgStmt = conn.prepareStatement(avgQuery);
            avgStmt.setInt(1, currentUser.getUserId());
            ResultSet avgRs = avgStmt.executeQuery();

            if (avgRs.next()) {
                double avgDaily = avgRs.getDouble("avg_daily");
                spendingInsights.append("• Average daily spending: ").append(formatKSH(avgDaily)).append("\n");

                // Recommendation based on average daily spending
                if (avgDaily > 2000) {
                    spendingInsights.append("  ⚠️ Your daily spending is high. Consider setting a daily limit of ").append(formatKSH(1500)).append("\n");
                } else if (avgDaily > 1000) {
                    spendingInsights.append("  ✅ Your daily spending is moderate. You're doing well!\n");
                } else if (avgDaily > 0) {
                    spendingInsights.append("  🌟 Excellent! Your daily spending is very controlled.\n");
                }
            }

            // Most expensive categories
            String catQuery = "SELECT c.category_name, SUM(t.amount) as total " +
                    "FROM transactions t JOIN categories c ON t.category_id = c.category_id " +
                    "WHERE t.user_id = ? AND t.type = 'WITHDRAWAL' " +
                    "GROUP BY c.category_name ORDER BY total DESC LIMIT 5";
            PreparedStatement catStmt = conn.prepareStatement(catQuery);
            catStmt.setInt(1, currentUser.getUserId());
            ResultSet catRs = catStmt.executeQuery();

            spendingInsights.append("\n📈 TOP SPENDING CATEGORIES\n");
            spendingInsights.append("──────────────────────────────────────────────────────\n");
            int rank = 1;
            while (catRs.next()) {
                String category = catRs.getString("category_name");
                double amount = catRs.getDouble("total");
                spendingInsights.append(String.format("  %d. %-20s %s\n", rank, category, formatKSH(amount)));
                rank++;
            }

            // Category-specific recommendations
            spendingInsights.append(categorySpecificTips());

        } catch (SQLException e) {
            e.printStackTrace();
            return "Error in spending analysis";
        }

        insights.append(spendingInsights);
        return spendingInsights.toString();
    }

    private String generateHTMLSpendingAnalysis() throws SQLException {
        StringBuilder html = new StringBuilder();
        html.append("<div class='section'>");
        html.append("<h2>💰 Spending Analysis</h2>");

        // Average daily spending
        String avgQuery = "SELECT AVG(daily_total) as avg_daily FROM (" +
                "SELECT DATE(transaction_date) as day, SUM(amount) as daily_total " +
                "FROM transactions WHERE user_id = ? AND type = 'WITHDRAWAL' " +
                "GROUP BY DATE(transaction_date)) as daily";
        PreparedStatement avgStmt = conn.prepareStatement(avgQuery);
        avgStmt.setInt(1, currentUser.getUserId());
        ResultSet avgRs = avgStmt.executeQuery();

        if (avgRs.next()) {
            double avgDaily = avgRs.getDouble("avg_daily");
            if (avgDaily > 0) {
                html.append("<div class='grid'>");
                html.append("<div class='stat-card'>");
                html.append("<div style='font-size: 0.9em; color: #666;'>Average Daily Spending</div>");
                html.append("<div class='amount'>").append(formatKSH(avgDaily)).append("</div>");
                html.append("</div>");

                if (avgDaily > 2000) {
                    html.append("<div class='warning'>⚠️ Your daily spending is high. Consider setting a daily limit of ").append(formatKSH(1500)).append("</div>");
                } else if (avgDaily > 1000) {
                    html.append("<div class='positive'>✅ Your daily spending is moderate. You're doing well!</div>");
                } else {
                    html.append("<div class='positive'>🌟 Excellent! Your daily spending is very controlled.</div>");
                }
                html.append("</div>");
            }
        }

        // Most expensive categories
        String catQuery = "SELECT c.category_name, SUM(t.amount) as total " +
                "FROM transactions t JOIN categories c ON t.category_id = c.category_id " +
                "WHERE t.user_id = ? AND t.type = 'WITHDRAWAL' " +
                "GROUP BY c.category_name ORDER BY total DESC LIMIT 5";
        PreparedStatement catStmt = conn.prepareStatement(catQuery);
        catStmt.setInt(1, currentUser.getUserId());
        ResultSet catRs = catStmt.executeQuery();

        html.append("<h3>📈 Top Spending Categories</h3><ul>");
        int rank = 1;
        while (catRs.next()) {
            String category = catRs.getString("category_name");
            double amount = catRs.getDouble("total");
            html.append("<li>");
            html.append("<span><span class='badge'>#").append(rank).append("</span> ").append(category).append("</span>");
            html.append("<span class='amount'>").append(formatKSH(amount)).append("</span>");
            html.append("</li>");
            rank++;
        }
        html.append("</ul>");

        // Category-specific tips in HTML
        html.append(generateHTMLCategoryTips());

        html.append("</div>");
        return html.toString();
    }

    private String generateHTMLCategoryTips() throws SQLException {
        StringBuilder tips = new StringBuilder();
        tips.append("<div class='tips' style='margin-top: 20px;'>");

        String catQuery = "SELECT c.category_name, SUM(t.amount) as total " +
                "FROM transactions t JOIN categories c ON t.category_id = c.category_id " +
                "WHERE t.user_id = ? AND t.type = 'WITHDRAWAL' " +
                "GROUP BY c.category_name";
        PreparedStatement catStmt = conn.prepareStatement(catQuery);
        catStmt.setInt(1, currentUser.getUserId());
        ResultSet catRs = catStmt.executeQuery();

        while (catRs.next()) {
            String category = catRs.getString("category_name");
            double amount = catRs.getDouble("total");

            if (category.equalsIgnoreCase("Food & Dining") && amount > 5000) {
                tips.append("<div class='warning' style='margin: 10px 0;'>");
                tips.append("🍽️ <strong>Food & Dining:</strong> You spent ").append(formatKSH(amount));
                tips.append("<br>• Try meal prepping to save up to 30%");
                tips.append("<br>• Use cashback apps like Zina or PesaPal");
                tips.append("</div>");
            }
            else if (category.equalsIgnoreCase("Shopping") && amount > 3000) {
                tips.append("<div class='warning' style='margin: 10px 0;'>");
                tips.append("🛍️ <strong>Shopping:</strong> You spent ").append(formatKSH(amount));
                tips.append("<br>• Wait 24 hours before making non-essential purchases");
                tips.append("</div>");
            }
            else if (category.equalsIgnoreCase("Transportation") && amount > 2000) {
                tips.append("<div class='warning' style='margin: 10px 0;'>");
                tips.append("🚗 <strong>Transportation:</strong> You spent ").append(formatKSH(amount));
                tips.append("<br>• Consider using public transport like matatus/SGR");
                tips.append("<br>• Use ride-sharing apps during off-peak hours");
                tips.append("</div>");
            }
            else if (category.equalsIgnoreCase("Entertainment") && amount > 1500) {
                tips.append("<div class='warning' style='margin: 10px 0;'>");
                tips.append("🎬 <strong>Entertainment:</strong> You spent ").append(formatKSH(amount));
                tips.append("<br>• Look for free community events in Nairobi");
                tips.append("<br>• Share streaming service subscriptions with family");
                tips.append("</div>");
            }
            else if (category.equalsIgnoreCase("Bills & Utilities") && amount > 4000) {
                tips.append("<div class='warning' style='margin: 10px 0;'>");
                tips.append("💡 <strong>Utilities:</strong> You spent ").append(formatKSH(amount));
                tips.append("<br>• Consider energy-efficient appliances");
                tips.append("<br>• Turn off lights and electronics when not in use");
                tips.append("</div>");
            }
        }

        tips.append("</div>");
        return tips.toString();
    }

    private String categorySpecificTips() throws SQLException {
        StringBuilder tips = new StringBuilder();

        String catQuery = "SELECT c.category_name, SUM(t.amount) as total " +
                "FROM transactions t JOIN categories c ON t.category_id = c.category_id " +
                "WHERE t.user_id = ? AND t.type = 'WITHDRAWAL' " +
                "GROUP BY c.category_name";
        PreparedStatement catStmt = conn.prepareStatement(catQuery);
        catStmt.setInt(1, currentUser.getUserId());
        ResultSet catRs = catStmt.executeQuery();

        while (catRs.next()) {
            String category = catRs.getString("category_name");
            double amount = catRs.getDouble("total");

            if (category.equalsIgnoreCase("Food & Dining") && amount > 5000) {
                tips.append("\n🍽️ FOOD & DINING TIPS:\n");
                tips.append("  • You spent ").append(formatKSH(amount)).append(" on food this month.\n");
                tips.append("  • Try meal prepping to save up to 30%\n");
                tips.append("  • Use cashback apps like Zina or PesaPal\n");
            }
            else if (category.equalsIgnoreCase("Shopping") && amount > 3000) {
                tips.append("\n🛍️ SHOPPING TIPS:\n");
                tips.append("  • You spent ").append(formatKSH(amount)).append(" on shopping.\n");
                tips.append("  • Wait 24 hours before making non-essential purchases\n");
            }
            else if (category.equalsIgnoreCase("Transportation") && amount > 2000) {
                tips.append("\n🚗 TRANSPORTATION TIPS:\n");
                tips.append("  • You spent ").append(formatKSH(amount)).append(" on transport.\n");
                tips.append("  • Consider using public transport like matatus/SGR\n");
                tips.append("  • Use ride-sharing apps during off-peak hours\n");
            }
            else if (category.equalsIgnoreCase("Entertainment") && amount > 1500) {
                tips.append("\n🎬 ENTERTAINMENT TIPS:\n");
                tips.append("  • You spent ").append(formatKSH(amount)).append(" on entertainment.\n");
                tips.append("  • Look for free community events in Nairobi\n");
                tips.append("  • Share streaming service subscriptions with family\n");
            }
            else if (category.equalsIgnoreCase("Bills & Utilities") && amount > 4000) {
                tips.append("\n💡 UTILITIES TIPS:\n");
                tips.append("  • You spent ").append(formatKSH(amount)).append(" on utilities.\n");
                tips.append("  • Consider energy-efficient appliances\n");
                tips.append("  • Turn off lights and electronics when not in use\n");
            }
            else if (category.equalsIgnoreCase("Healthcare") && amount > 3000) {
                tips.append("\n🏥 HEALTHCARE TIPS:\n");
                tips.append("  • You spent ").append(formatKSH(amount)).append(" on healthcare.\n");
                tips.append("  • Check if you qualify for NHIF benefits\n");
                tips.append("  • Compare medicine prices at different pharmacies\n");
            }
        }

        return tips.toString();
    }

    public String generateSavingsAnalysis() {
        StringBuilder savingsInsights = new StringBuilder();
        savingsInsights.append("\n\n💰 SAVINGS ANALYSIS\n");
        savingsInsights.append("══════════════════════════════════════════════════════════════\n");

        try {
            String savingsQuery = "SELECT " +
                    "(SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE user_id = ? AND type = 'DEPOSIT') as total_income, " +
                    "(SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE user_id = ? AND type = 'WITHDRAWAL') as total_expenses, " +
                    "(SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE user_id = ? AND type = 'DEPOSIT' AND MONTH(transaction_date) = MONTH(NOW())) as month_income, " +
                    "(SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE user_id = ? AND type = 'WITHDRAWAL' AND MONTH(transaction_date) = MONTH(NOW())) as month_expenses";

            PreparedStatement savingsStmt = conn.prepareStatement(savingsQuery);
            savingsStmt.setInt(1, currentUser.getUserId());
            savingsStmt.setInt(2, currentUser.getUserId());
            savingsStmt.setInt(3, currentUser.getUserId());
            savingsStmt.setInt(4, currentUser.getUserId());
            ResultSet savingsRs = savingsStmt.executeQuery();

            if (savingsRs.next()) {
                double totalIncome = savingsRs.getDouble("total_income");
                double totalExpenses = savingsRs.getDouble("total_expenses");
                double monthIncome = savingsRs.getDouble("month_income");
                double monthExpenses = savingsRs.getDouble("month_expenses");
                double totalSavings = totalIncome - totalExpenses;
                double monthSavings = monthIncome - monthExpenses;

                savingsInsights.append("• Total Lifetime Income: ").append(formatKSH(totalIncome)).append("\n");
                savingsInsights.append("• Total Lifetime Expenses: ").append(formatKSH(totalExpenses)).append("\n");
                savingsInsights.append("• Total Lifetime Savings: ").append(formatKSH(totalSavings)).append("\n");

                String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM"));
                savingsInsights.append("\n📅 THIS MONTH (").append(currentMonth).append(")\n");
                savingsInsights.append("  • Income: ").append(formatKSH(monthIncome)).append("\n");
                savingsInsights.append("  • Expenses: ").append(formatKSH(monthExpenses)).append("\n");
                savingsInsights.append("  • Savings: ").append(formatKSH(monthSavings)).append("\n");

                // Savings rate calculation and recommendations
                if (monthIncome > 0) {
                    double savingsRate = (monthSavings / monthIncome) * 100;
                    savingsInsights.append("\n📊 SAVINGS RATE: ").append(String.format("%.1f%%", savingsRate)).append("\n");

                    if (savingsRate >= 50) {
                        savingsInsights.append("  🌟 EXCELLENT! You're saving more than 50% of your income!\n");
                        savingsInsights.append("  • Consider investing your extra savings in Sacco or Money Market Funds\n");
                        savingsInsights.append("  • Look into M-Shwari or KCB M-PESA savings accounts\n");
                    } else if (savingsRate >= 30) {
                        savingsInsights.append("  👍 GREAT! You're saving 30-50% of your income.\n");
                        savingsInsights.append("  • You're on track for financial independence!\n");
                        savingsInsights.append("  • Consider increasing your emergency fund\n");
                    } else if (savingsRate >= 20) {
                        savingsInsights.append("  ✅ GOOD! You're saving 20-30% of your income.\n");
                        savingsInsights.append("  • This is the recommended savings rate\n");
                        savingsInsights.append("  • Keep up the good work!\n");
                    } else if (savingsRate >= 10) {
                        savingsInsights.append("  ⚠️ You're saving 10-20% of your income.\n");
                        savingsInsights.append("  • Try to cut back on discretionary spending\n");
                        savingsInsights.append("  • Aim for 20% savings rate\n");
                    } else if (savingsRate >= 0) {
                        savingsInsights.append("  🔴 URGENT: Your savings rate is below 10%.\n");
                        savingsInsights.append("  • Review your expenses and cut unnecessary costs\n");
                        savingsInsights.append("  • Try the 50/30/20 budgeting rule:\n");
                        savingsInsights.append("    - 50% Needs (rent, food, bills)\n");
                        savingsInsights.append("    - 30% Wants (entertainment, shopping)\n");
                        savingsInsights.append("    - 20% Savings & Investments\n");
                    } else {
                        savingsInsights.append("  🔴 NEGATIVE: You're spending more than you earn!\n");
                        savingsInsights.append("  • Immediate action needed to reduce expenses\n");
                        savingsInsights.append("  • Look for ways to increase income (side hustle, freelance)\n");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        insights.append(savingsInsights);
        return savingsInsights.toString();
    }

    private String generateHTMLSavingsAnalysis() throws SQLException {
        StringBuilder html = new StringBuilder();
        html.append("<div class='section'>");
        html.append("<h2>💰 Savings Analysis</h2>");

        String savingsQuery = "SELECT " +
                "(SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE user_id = ? AND type = 'DEPOSIT') as total_income, " +
                "(SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE user_id = ? AND type = 'WITHDRAWAL') as total_expenses, " +
                "(SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE user_id = ? AND type = 'DEPOSIT' AND MONTH(transaction_date) = MONTH(NOW())) as month_income, " +
                "(SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE user_id = ? AND type = 'WITHDRAWAL' AND MONTH(transaction_date) = MONTH(NOW())) as month_expenses";

        PreparedStatement savingsStmt = conn.prepareStatement(savingsQuery);
        savingsStmt.setInt(1, currentUser.getUserId());
        savingsStmt.setInt(2, currentUser.getUserId());
        savingsStmt.setInt(3, currentUser.getUserId());
        savingsStmt.setInt(4, currentUser.getUserId());
        ResultSet savingsRs = savingsStmt.executeQuery();

        if (savingsRs.next()) {
            double totalIncome = savingsRs.getDouble("total_income");
            double totalExpenses = savingsRs.getDouble("total_expenses");
            double monthIncome = savingsRs.getDouble("month_income");
            double monthExpenses = savingsRs.getDouble("month_expenses");
            double totalSavings = totalIncome - totalExpenses;
            double monthSavings = monthIncome - monthExpenses;

            html.append("<div class='grid'>");
            html.append("<div class='stat-card'><div>Lifetime Income</div><div class='amount positive'>").append(formatKSH(totalIncome)).append("</div></div>");
            html.append("<div class='stat-card'><div>Lifetime Expenses</div><div class='amount negative'>").append(formatKSH(totalExpenses)).append("</div></div>");
            html.append("<div class='stat-card'><div>Lifetime Savings</div><div class='amount'>").append(formatKSH(totalSavings)).append("</div></div>");
            html.append("</div>");

            String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM"));
            html.append("<h3>📅 This Month (").append(currentMonth).append(")</h3>");
            html.append("<div class='grid'>");
            html.append("<div class='stat-card'><div>Income</div><div class='amount positive'>").append(formatKSH(monthIncome)).append("</div></div>");
            html.append("<div class='stat-card'><div>Expenses</div><div class='amount negative'>").append(formatKSH(monthExpenses)).append("</div></div>");
            html.append("<div class='stat-card'><div>Savings</div><div class='amount'>").append(formatKSH(monthSavings)).append("</div></div>");
            html.append("</div>");

            // Savings rate
            if (monthIncome > 0) {
                double savingsRate = (monthSavings / monthIncome) * 100;
                html.append("<div class='stat-card' style='margin-top: 20px;'>");
                html.append("<div>Savings Rate</div>");
                html.append("<div class='amount'>").append(String.format("%.1f%%", savingsRate)).append("</div>");

                if (savingsRate >= 50) {
                    html.append("<div class='positive'>🌟 EXCELLENT! You're saving more than 50% of your income!</div>");
                } else if (savingsRate >= 30) {
                    html.append("<div class='positive'>👍 GREAT! You're saving 30-50% of your income.</div>");
                } else if (savingsRate >= 20) {
                    html.append("<div class='positive'>✅ GOOD! You're saving 20-30% of your income.</div>");
                } else if (savingsRate >= 10) {
                    html.append("<div class='warning'>⚠️ You're saving 10-20% of your income.</div>");
                } else {
                    html.append("<div class='negative'>🔴 URGENT: Your savings rate is below 10%.</div>");
                }
                html.append("</div>");
            }
        }

        html.append("</div>");
        return html.toString();
    }

    public String generateMonthlyComparison() {
        StringBuilder comparisonInsights = new StringBuilder();
        comparisonInsights.append("\n\n📊 MONTH-OVER-MONTH COMPARISON\n");
        comparisonInsights.append("══════════════════════════════════════════════════════════════\n");

        try {
            String compareQuery = "SELECT " +
                    "MONTH(transaction_date) as month, " +
                    "SUM(CASE WHEN type = 'DEPOSIT' THEN amount ELSE 0 END) as income, " +
                    "SUM(CASE WHEN type = 'WITHDRAWAL' THEN amount ELSE 0 END) as expenses " +
                    "FROM transactions WHERE user_id = ? AND transaction_date >= DATE_SUB(NOW(), INTERVAL 3 MONTH) " +
                    "GROUP BY MONTH(transaction_date) ORDER BY month DESC LIMIT 2";

            PreparedStatement compareStmt = conn.prepareStatement(compareQuery);
            compareStmt.setInt(1, currentUser.getUserId());
            ResultSet compareRs = compareStmt.executeQuery();

            double[] monthlyExpenses = new double[2];
            double[] monthlyIncome = new double[2];
            String[] months = new String[2];
            int monthIndex = 0;

            while (compareRs.next() && monthIndex < 2) {
                months[monthIndex] = getMonthName(compareRs.getInt("month"));
                monthlyIncome[monthIndex] = compareRs.getDouble("income");
                monthlyExpenses[monthIndex] = compareRs.getDouble("expenses");
                monthIndex++;
            }

            if (monthIndex == 2) {
                double expenseChange = ((monthlyExpenses[0] - monthlyExpenses[1]) / monthlyExpenses[1]) * 100;
                double incomeChange = ((monthlyIncome[0] - monthlyIncome[1]) / monthlyIncome[1]) * 100;

                comparisonInsights.append("• ").append(months[0]).append(" vs ").append(months[1]).append(":\n");

                if (expenseChange > 0) {
                    comparisonInsights.append(String.format("  📈 Spending increased by %.1f%%\n", expenseChange));
                    comparisonInsights.append("  • Review what caused this increase\n");
                    comparisonInsights.append("  • Try to identify one area to cut back\n");
                } else if (expenseChange < 0) {
                    comparisonInsights.append(String.format("  📉 Great job! Spending decreased by %.1f%%\n", Math.abs(expenseChange)));
                }

                if (incomeChange > 0) {
                    comparisonInsights.append(String.format("  📈 Income increased by %.1f%%\n", incomeChange));
                } else if (incomeChange < 0) {
                    comparisonInsights.append(String.format("  📉 Income decreased by %.1f%%\n", Math.abs(incomeChange)));
                }
            } else {
                comparisonInsights.append("• Not enough data for month-over-month comparison yet.\n");
                comparisonInsights.append("• Add more transactions to see trends!\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        insights.append(comparisonInsights);
        return comparisonInsights.toString();
    }

    private String generateHTMLMonthlyComparison() throws SQLException {
        StringBuilder html = new StringBuilder();
        html.append("<div class='section'>");
        html.append("<h2>📊 Month-over-Month Comparison</h2>");

        String compareQuery = "SELECT " +
                "MONTH(transaction_date) as month, " +
                "SUM(CASE WHEN type = 'DEPOSIT' THEN amount ELSE 0 END) as income, " +
                "SUM(CASE WHEN type = 'WITHDRAWAL' THEN amount ELSE 0 END) as expenses " +
                "FROM transactions WHERE user_id = ? AND transaction_date >= DATE_SUB(NOW(), INTERVAL 3 MONTH) " +
                "GROUP BY MONTH(transaction_date) ORDER BY month DESC LIMIT 2";

        PreparedStatement compareStmt = conn.prepareStatement(compareQuery);
        compareStmt.setInt(1, currentUser.getUserId());
        ResultSet compareRs = compareStmt.executeQuery();

        double[] monthlyExpenses = new double[2];
        double[] monthlyIncome = new double[2];
        String[] months = new String[2];
        int monthIndex = 0;

        while (compareRs.next() && monthIndex < 2) {
            months[monthIndex] = getMonthName(compareRs.getInt("month"));
            monthlyIncome[monthIndex] = compareRs.getDouble("income");
            monthlyExpenses[monthIndex] = compareRs.getDouble("expenses");
            monthIndex++;
        }

        if (monthIndex == 2) {
            double expenseChange = ((monthlyExpenses[0] - monthlyExpenses[1]) / monthlyExpenses[1]) * 100;
            double incomeChange = ((monthlyIncome[0] - monthlyIncome[1]) / monthlyIncome[1]) * 100;

            html.append("<h3>").append(months[0]).append(" vs ").append(months[1]).append("</h3>");
            html.append("<div class='grid'>");

            if (expenseChange > 0) {
                html.append("<div class='stat-card warning'>");
                html.append("<div>Spending Change</div>");
                html.append("<div class='negative'>+").append(String.format("%.1f%%", expenseChange)).append("</div>");
                html.append("<small>Review what caused this increase</small>");
                html.append("</div>");
            } else if (expenseChange < 0) {
                html.append("<div class='stat-card positive'>");
                html.append("<div>Spending Change</div>");
                html.append("<div>").append(String.format("%.1f%%", Math.abs(expenseChange))).append(" decrease</div>");
                html.append("<small>Great job!</small>");
                html.append("</div>");
            }

            if (incomeChange != 0) {
                html.append("<div class='stat-card'>");
                html.append("<div>Income Change</div>");
                html.append("<div class='").append(incomeChange > 0 ? "positive" : "negative").append("'>");
                html.append(incomeChange > 0 ? "+" : "").append(String.format("%.1f%%", incomeChange));
                html.append("</div>");
                html.append("</div>");
            }

            html.append("</div>");
        } else {
            html.append("<p>Not enough data for month-over-month comparison yet. Add more transactions to see trends!</p>");
        }

        html.append("</div>");
        return html.toString();
    }

    public String generateSmartRecommendations() {
        StringBuilder recommendations = new StringBuilder();
        recommendations.append("\n\n💡 SMART RECOMMENDATIONS\n");
        recommendations.append("══════════════════════════════════════════════════════════════\n");

        try {
            // Calculate average monthly expenses
            double monthlyExpenses = getAverageMonthlyExpenses();
            double monthlyIncome = getAverageMonthlyIncome();
            double currentBalance = currentUser.getCurrentBalance();

            // Emergency fund recommendation
            if (monthlyExpenses > 0) {
                double emergencyFund = monthlyExpenses * 6; // 6 months of expenses

                recommendations.append("\n💰 EMERGENCY FUND\n");
                recommendations.append("──────────────────────────────────────────────────────\n");
                recommendations.append("• Goal: ").append(formatKSH(emergencyFund))
                        .append(" (6 months of expenses)\n");

                if (currentBalance < emergencyFund) {
                    double needed = emergencyFund - currentBalance;
                    double monthsToSave = 6;
                    double monthlySavingsNeeded = needed / monthsToSave;

                    recommendations.append("  ⚠️ You need ").append(formatKSH(needed))
                            .append(" more to reach your emergency fund goal.\n");
                    recommendations.append("  • Try to save ").append(formatKSH(monthlySavingsNeeded))
                            .append(" per month for the next ").append((int)monthsToSave).append(" months\n");

                    if (monthlyIncome > 0 && monthlySavingsNeeded > monthlyIncome * 0.3) {
                        recommendations.append("  • This is more than 30% of your income - consider:\n");
                        recommendations.append("    - Reducing non-essential spending\n");
                        recommendations.append("    - Finding additional income sources\n");
                        recommendations.append("    - Extending your savings timeline\n");
                    }
                } else {
                    recommendations.append("  ✅ Congratulations! You have a fully-funded emergency fund!\n");
                    recommendations.append("  • Consider investing excess funds for growth\n");
                }
            }

            // Investment recommendations
            if (currentBalance > 50000) { // 50,000 KSH threshold
                recommendations.append("\n📈 INVESTMENT OPPORTUNITIES\n");
                recommendations.append("──────────────────────────────────────────────────────\n");

                double investAmount = currentBalance * 0.3;
                recommendations.append("• Consider investing ").append(formatKSH(investAmount))
                        .append(" (30% of your savings)\n");

                recommendations.append("• investment options:\n");
                recommendations.append("  - 📊 Money Market Funds: 8-10% returns (Cytonn, Britam)\n");
                recommendations.append("  - 🏦 Sacco Accounts: 10-12% dividends\n");
                recommendations.append("  - 📈 Treasury Bills/Bonds: Government-backed\n");
                recommendations.append("  - 🏠 Chama Investments: Group saving power\n");
                recommendations.append("  - 📱 M-Shwari/KCB M-PESA: 4-6% interest\n");
            }

            // Debt management recommendations
            if (monthlyExpenses > monthlyIncome * 0.5) {
                recommendations.append("\n💳 DEBT MANAGEMENT\n");
                recommendations.append("──────────────────────────────────────────────────────\n");
                recommendations.append("• Your expenses are >50% of income\n");
                recommendations.append("• Debt reduction strategies:\n");
                recommendations.append("  - 🎯 Debt Snowball: Pay smallest debts first\n");
                recommendations.append("  - 📉 Debt Avalanche: Pay highest interest first\n");
                recommendations.append("  - 🤝 Consolidation: Single loan with lower rate\n");
            }

            // Budget rule recommendation
            recommendations.append("\n📋 BUDGETING RULE (50/30/20)\n");
            recommendations.append("──────────────────────────────────────────────────────\n");
            if (monthlyIncome > 0) {
                recommendations.append("• Based on your income of ").append(formatKSH(monthlyIncome)).append("/month:\n");
                recommendations.append("  - 🏠 Needs (50%): ").append(formatKSH(monthlyIncome * 0.5)).append("\n");
                recommendations.append("  - 🎉 Wants (30%): ").append(formatKSH(monthlyIncome * 0.3)).append("\n");
                recommendations.append("  - 💰 Savings (20%): ").append(formatKSH(monthlyIncome * 0.2)).append("\n");
            }

            // Weekly money-saving tips for Kenya
            recommendations.append("\n📅 WEEKLY MONEY-SAVING TIPS\n");
            recommendations.append("──────────────────────────────────────────────────────\n");
            recommendations.append("  • 🚫 Try a 'no-spend weekend' once a month\n");
            recommendations.append("  • 🍱 Pack lunch instead of buying (save ~").append(formatKSH(500)).append("/week)\n");
            recommendations.append("  • ☕ Make coffee at home (save ~").append(formatKSH(200)).append("/day)\n");
            recommendations.append("  • 🛒 Shop at Quickmart for cheaper groceries\n");
            recommendations.append("  • 🚌 Use matatus instead of taxis for short distances\n");
            recommendations.append("  • 📱 Use Zuku or Faiba for cheaper internet bundles\n");


        } catch (SQLException e) {
            e.printStackTrace();
        }

        insights.append(recommendations);
        return recommendations.toString();
    }

    private String generateHTMLSmartRecommendations() throws SQLException {
        StringBuilder html = new StringBuilder();
        html.append("<div class='section'>");
        html.append("<h2>Smart Recommendations</h2>");

        double monthlyExpenses = getAverageMonthlyExpenses();
        double monthlyIncome = getAverageMonthlyIncome();
        double currentBalance = currentUser.getCurrentBalance();

        // Emergency fund
        if (monthlyExpenses > 0) {
            double emergencyFund = monthlyExpenses * 6;

            html.append("<h3> Emergency Fund</h3>");
            html.append("<div class='stat-card'>");
            html.append("<div>Target (6 months)</div>");
            html.append("<div class='amount'>").append(formatKSH(emergencyFund)).append("</div>");

            if (currentBalance < emergencyFund) {
                double needed = emergencyFund - currentBalance;
                double monthlySavingsNeeded = needed / 6;

                html.append("<div class='warning'>⚠️ You need ").append(formatKSH(needed)).append(" more</div>");
                html.append("<div>Save ").append(formatKSH(monthlySavingsNeeded)).append(" per month for 6 months</div>");
            } else {
                html.append("<div class='positive'>✅ You have a fully-funded emergency fund!</div>");
            }
            html.append("</div>");
        }

        // Investment
        if (currentBalance > 50000) {
            double investAmount = currentBalance * 0.3;
            html.append("<h3>📈 Investment Opportunities</h3>");
            html.append("<div class='grid'>");
            html.append("<div class='stat-card'>");
            html.append("<div>Consider investing</div>");
            html.append("<div class='amount positive'>").append(formatKSH(investAmount)).append("</div>");
            html.append("<small>30% of your savings</small>");
            html.append("</div>");

            html.append("<div class='stat-card'>");
            html.append("<div>Options in Kenya</div>");
            html.append("<ul style='font-size: 0.9em; margin-top: 10px;'>");
            html.append("<li>📊 Money Market Funds (8-10%)</li>");
            html.append("<li>🏦 Sacco Accounts (10-12%)</li>");
            html.append("<li>📈 Treasury Bonds</li>");
            html.append("</ul>");
            html.append("</div>");
            html.append("</div>");
        }

        // Budget rule
        if (monthlyIncome > 0) {
            html.append("<h3> 50/30/20 Budget Rule</h3>");
            html.append("<div class='grid'>");
            html.append("<div class='stat-card'><div>Needs (50%)</div><div class='amount'>").append(formatKSH(monthlyIncome * 0.5)).append("</div></div>");
            html.append("<div class='stat-card'><div>Wants (30%)</div><div class='amount'>").append(formatKSH(monthlyIncome * 0.3)).append("</div></div>");
            html.append("<div class='stat-card'><div>Savings (20%)</div><div class='amount'>").append(formatKSH(monthlyIncome * 0.2)).append("</div></div>");
            html.append("</div>");
        }

        html.append("</div>");
        return html.toString();
    }

    // Helper methods
    private double getAverageMonthlyExpenses() throws SQLException {
        String query = "SELECT AVG(monthly) as avg_monthly FROM (" +
                "SELECT MONTH(transaction_date) as month, SUM(amount) as monthly " +
                "FROM transactions WHERE user_id = ? AND type = 'WITHDRAWAL' " +
                "GROUP BY MONTH(transaction_date) LIMIT 3) as monthly_avg";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, currentUser.getUserId());
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return rs.getDouble("avg_monthly");
        }
        return 0;
    }

    private double getAverageMonthlyIncome() throws SQLException {
        String query = "SELECT AVG(monthly) as avg_monthly FROM (" +
                "SELECT MONTH(transaction_date) as month, SUM(amount) as monthly " +
                "FROM transactions WHERE user_id = ? AND type = 'DEPOSIT' " +
                "GROUP BY MONTH(transaction_date) LIMIT 3) as monthly_avg";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, currentUser.getUserId());
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return rs.getDouble("avg_monthly");
        }
        return 0;
    }

    private String getMonthName(int month) {
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        return months[month - 1];
    }

    // Individual insight methods
    public String getSpendingInsights() {
        return generateSpendingAnalysis();
    }

    public String getSavingsInsights() {
        return generateSavingsAnalysis();
    }

    public String getComparisonInsights() {
        return generateMonthlyComparison();
    }

    public String getRecommendations() {
        return generateSmartRecommendations();
    }

    public String getQuickSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(" QUICK FINANCIAL SUMMARY\n");
        summary.append("══════════════════════════════════════════════════════════════\n\n");

        try (Connection conn = DatabaseConnection.getConnection()) {
            this.conn = conn;

            // Current balance
            summary.append("💰 Current Balance: ").append(formatKSH(currentUser.getCurrentBalance())).append("\n\n");

            // This month
            String monthQuery = "SELECT " +
                    "COALESCE(SUM(CASE WHEN type = 'DEPOSIT' THEN amount ELSE 0 END), 0) as income, " +
                    "COALESCE(SUM(CASE WHEN type = 'WITHDRAWAL' THEN amount ELSE 0 END), 0) as expenses " +
                    "FROM transactions WHERE user_id = ? AND MONTH(transaction_date) = MONTH(NOW())";
            PreparedStatement stmt = conn.prepareStatement(monthQuery);
            stmt.setInt(1, currentUser.getUserId());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                double income = rs.getDouble("income");
                double expenses = rs.getDouble("expenses");
                summary.append("📅 This Month:\n");
                summary.append("  • Income: ").append(formatKSH(income)).append("\n");
                summary.append("  • Expenses: ").append(formatKSH(expenses)).append("\n");
                summary.append("  • Net: ").append(formatKSH(income - expenses)).append("\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return summary.toString();
    }

    public String getHTMLQuickSummary() {
        StringBuilder html = new StringBuilder();
        html.append("<div class='section'>");
        html.append("<h2>Quick Financial Summary</h2>");

        html.append("<div class='grid'>");
        html.append("<div class='stat-card'>");
        html.append("<div>Current Balance</div>");
        html.append("<div class='amount'>").append(formatKSH(currentUser.getCurrentBalance())).append("</div>");
        html.append("</div>");

        try (Connection conn = DatabaseConnection.getConnection()) {
            this.conn = conn;
            String monthQuery = "SELECT " +
                    "COALESCE(SUM(CASE WHEN type = 'DEPOSIT' THEN amount ELSE 0 END), 0) as income, " +
                    "COALESCE(SUM(CASE WHEN type = 'WITHDRAWAL' THEN amount ELSE 0 END), 0) as expenses " +
                    "FROM transactions WHERE user_id = ? AND MONTH(transaction_date) = MONTH(NOW())";
            PreparedStatement stmt = conn.prepareStatement(monthQuery);
            stmt.setInt(1, currentUser.getUserId());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                double income = rs.getDouble("income");
                double expenses = rs.getDouble("expenses");
                html.append("<div class='stat-card'><div>This Month Income</div><div class='amount positive'>").append(formatKSH(income)).append("</div></div>");
                html.append("<div class='stat-card'><div>This Month Expenses</div><div class='amount negative'>").append(formatKSH(expenses)).append("</div></div>");
                html.append("<div class='stat-card'><div>Net Change</div><div class='amount'>").append(formatKSH(income - expenses)).append("</div></div>");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        html.append("</div></div>");
        return html.toString();
    }
}