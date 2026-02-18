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

    public InsightsGenerator(User user) {
        this.currentUser = user;
        this.insights = new StringBuilder();
    }

    public String generateAllInsights() {
        insights = new StringBuilder();
        insights.append(" FINANCIAL INSIGHTS & RECOMMENDATIONS\n");
        insights.append("=====================================\n\n");

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

        insights.append("\n=====================================\n");
        insights.append(" Keep tracking your finances daily!\n");

        return insights.toString();
    }

    public String generateSpendingAnalysis() {
        StringBuilder spendingInsights = new StringBuilder();
        spendingInsights.append("SPENDING ANALYSIS\n");
        spendingInsights.append("--------------------\n");

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
                spendingInsights.append("• Average daily spending: $")
                        .append(String.format("%.2f", avgDaily)).append("\n");

                // Recommendation based on average daily spending
                if (avgDaily > 200) {
                    spendingInsights.append("  ⚠️ Your daily spending is high. Consider setting a daily limit of $150.\n");
                } else if (avgDaily > 100) {
                    spendingInsights.append("  ✅ Your daily spending is moderate. You're doing well!\n");
                } else if (avgDaily > 0) {
                    spendingInsights.append("  🌟 Excellent! Your daily spending is very controlled.\n");
                }
            }

            // Most expensive categories
            String catQuery = "SELECT c.category_name, SUM(t.amount) as total " +
                    "FROM transactions t JOIN categories c ON t.category_id = c.category_id " +
                    "WHERE t.user_id = ? AND t.type = 'WITHDRAWAL' " +
                    "GROUP BY c.category_name ORDER BY total DESC LIMIT 3";
            PreparedStatement catStmt = conn.prepareStatement(catQuery);
            catStmt.setInt(1, currentUser.getUserId());
            ResultSet catRs = catStmt.executeQuery();

            spendingInsights.append("\n📈 TOP SPENDING CATEGORIES\n");
            int rank = 1;
            while (catRs.next()) {
                String category = catRs.getString("category_name");
                double amount = catRs.getDouble("total");
                spendingInsights.append("  ").append(rank).append(". ").append(category)
                        .append(": $").append(String.format("%.2f", amount)).append("\n");
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

            if (category.equalsIgnoreCase("Food & Dining") && amount > 500) {
                tips.append("\n🍽️ FOOD & DINING TIPS:\n");
                tips.append("  • You spent $").append(String.format("%.2f", amount))
                        .append(" on food this month.\n");
                tips.append("  • Try meal prepping to save up to 30%\n");
                tips.append("  • Use cashback apps like Rakuten or Ibotta\n");
            }
            else if (category.equalsIgnoreCase("Shopping") && amount > 300) {
                tips.append("\n🛍️ SHOPPING TIPS:\n");
                tips.append("  • Wait 24 hours before making non-essential purchases\n");
                tips.append("  • Unsubscribe from promotional emails to reduce impulse buying\n");
                tips.append("  • Look for discount codes before checking out\n");
            }
            else if (category.equalsIgnoreCase("Transportation") && amount > 200) {
                tips.append("\n🚗 TRANSPORTATION TIPS:\n");
                tips.append("  • Consider carpooling or public transport\n");
                tips.append("  • Check if you qualify for student/senior discounts\n");
                tips.append("  • Compare gas prices using apps like GasBuddy\n");
            }
            else if (category.equalsIgnoreCase("Entertainment") && amount > 150) {
                tips.append("\n🎬 ENTERTAINMENT TIPS:\n");
                tips.append("  • Look for free community events\n");
                tips.append("  • Share streaming service subscriptions with family\n");
                tips.append("  • Check for student/military discounts\n");
            }
            else if (category.equalsIgnoreCase("Bills & Utilities") && amount > 400) {
                tips.append("\n💡 UTILITIES TIPS:\n");
                tips.append("  • Consider energy-efficient appliances\n");
                tips.append("  • Turn off lights and electronics when not in use\n");
                tips.append("  • Compare providers for better rates\n");
            }
            else if (category.equalsIgnoreCase("Healthcare") && amount > 300) {
                tips.append("\n🏥 HEALTHCARE TIPS:\n");
                tips.append("  • Check if you qualify for FSA/HSA accounts\n");
                tips.append("  • Compare prescription prices at different pharmacies\n");
                tips.append("  • Consider generic brands when available\n");
            }
        }

        return tips.toString();
    }

    public String generateSavingsAnalysis() {
        StringBuilder savingsInsights = new StringBuilder();
        savingsInsights.append("\n\n💰 SAVINGS ANALYSIS\n");
        savingsInsights.append("-------------------\n");

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

                savingsInsights.append("• Total Lifetime Income: $").append(String.format("%.2f", totalIncome)).append("\n");
                savingsInsights.append("• Total Lifetime Expenses: $").append(String.format("%.2f", totalExpenses)).append("\n");
                savingsInsights.append("• Total Lifetime Savings: $").append(String.format("%.2f", totalSavings)).append("\n");

                String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM"));
                savingsInsights.append("\n📅 THIS MONTH (").append(currentMonth).append(")\n");
                savingsInsights.append("  • Income: $").append(String.format("%.2f", monthIncome)).append("\n");
                savingsInsights.append("  • Expenses: $").append(String.format("%.2f", monthExpenses)).append("\n");
                savingsInsights.append("  • Savings: $").append(String.format("%.2f", monthSavings)).append("\n");

                // Savings rate calculation and recommendations
                if (monthIncome > 0) {
                    double savingsRate = (monthSavings / monthIncome) * 100;
                    savingsInsights.append("\n📊 SAVINGS RATE: ").append(String.format("%.1f%%", savingsRate)).append("\n");

                    if (savingsRate >= 50) {
                        savingsInsights.append("  🌟 EXCELLENT! You're saving more than 50% of your income!\n");
                        savingsInsights.append("  • Consider investing your extra savings\n");
                        savingsInsights.append("  • Look into high-yield savings accounts\n");
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
                        savingsInsights.append("  • Look for ways to increase income\n");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        insights.append(savingsInsights);
        return savingsInsights.toString();
    }

    public String generateMonthlyComparison() {
        StringBuilder comparisonInsights = new StringBuilder();
        comparisonInsights.append("\n\n📊 MONTH-OVER-MONTH COMPARISON\n");
        comparisonInsights.append("------------------------------\n");

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
                    comparisonInsights.append("  📈 Spending increased by ")
                            .append(String.format("%.1f%%", expenseChange)).append("\n");
                    comparisonInsights.append("  • Review what caused this increase\n");
                    comparisonInsights.append("  • Try to identify one area to cut back\n");
                } else if (expenseChange < 0) {
                    comparisonInsights.append("  📉 Great job! Spending decreased by ")
                            .append(String.format("%.1f%%", Math.abs(expenseChange)))
                            .append("!\n");
                }

                if (incomeChange > 0) {
                    comparisonInsights.append("  📈 Income increased by ")
                            .append(String.format("%.1f%%", incomeChange)).append("\n");
                } else if (incomeChange < 0) {
                    comparisonInsights.append("  📉 Income decreased by ")
                            .append(String.format("%.1f%%", Math.abs(incomeChange))).append("\n");
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

    public String generateSmartRecommendations() {
        StringBuilder recommendations = new StringBuilder();
        recommendations.append("\n\n💡 SMART RECOMMENDATIONS\n");
        recommendations.append("------------------------\n");

        try {
            // Calculate average monthly expenses
            double monthlyExpenses = getAverageMonthlyExpenses();
            double monthlyIncome = getAverageMonthlyIncome();

            // Emergency fund recommendation
            if (monthlyExpenses > 0) {
                double emergencyFund = monthlyExpenses * 6; // 6 months of expenses

                recommendations.append(" EMERGENCY FUND\n");
                recommendations.append("• Goal: $").append(String.format("%.2f", emergencyFund))
                        .append(" (6 months of expenses)\n");

                if (currentUser.getCurrentBalance() < emergencyFund) {
                    double needed = emergencyFund - currentUser.getCurrentBalance();
                    double monthsToSave = 6;
                    double monthlySavingsNeeded = needed / monthsToSave;

                    recommendations.append("  ⚠️ You need $").append(String.format("%.2f", needed))
                            .append(" more to reach your emergency fund goal.\n");
                    recommendations.append("  • Try to save $").append(String.format("%.2f", monthlySavingsNeeded))
                            .append(" per month for the next ").append((int)monthsToSave).append(" months\n");

                    // Specific advice based on income
                    if (monthlyIncome > 0 && monthlySavingsNeeded > monthlyIncome * 0.3) {
                        recommendations.append("  • This is more than 30% of your income - consider:\n");
                        recommendations.append("    - Reducing non-essential spending\n");
                        recommendations.append("    - Finding additional income sources\n");
                        recommendations.append("    - Extending your savings timeline\n");
                    }
                } else {
                    recommendations.append("  Congratulations! You have a fully-funded emergency fund!\n");
                    recommendations.append("  • Consider investing excess funds for growth\n");
                }
            }

            // Investment recommendations
            if (currentUser.getCurrentBalance() > 5000) {
                recommendations.append("\n📈 INVESTMENT OPPORTUNITIES\n");

                double investAmount = currentUser.getCurrentBalance() * 0.3;
                recommendations.append("• Consider investing $")
                        .append(String.format("%.2f", investAmount))
                        .append(" (30% of your savings)\n");

                recommendations.append("• Investment options to consider:\n");
                recommendations.append("  -  Index Funds (S&P 500): Average 10% annual return\n");
                recommendations.append("  -  High-Yield Savings: 4-5% APY, low risk\n");
                recommendations.append("  -  Roth IRA: Tax-free growth for retirement\n");
                recommendations.append("  -  401(k): Employer match = free money!\n");
            }

            // Debt management recommendations
            if (monthlyExpenses > monthlyIncome * 0.5) {
                recommendations.append("\n DEBT MANAGEMENT\n");
                recommendations.append("• Your expenses are >50% of income\n");
                recommendations.append("• Debt reduction strategies:\n");
                recommendations.append("  -  Debt Snowball: Pay smallest debts first\n");
                recommendations.append("  -  Debt Avalanche: Pay highest interest first\n");
                recommendations.append("  -  Balance Transfer: 0% APR cards\n");
                recommendations.append("  -  Consolidation: Single lower payment\n");
            }

            // Budget rule recommendation
            recommendations.append("\n📋 BUDGETING RULE (50/30/20)\n");
            if (monthlyIncome > 0) {
                recommendations.append("• Based on your income of $")
                        .append(String.format("%.2f", monthlyIncome)).append("/month:\n");
                recommendations.append("  - 🏠 Needs (50%): $")
                        .append(String.format("%.2f", monthlyIncome * 0.5)).append("\n");
                recommendations.append("  - 🎉 Wants (30%): $")
                        .append(String.format("%.2f", monthlyIncome * 0.3)).append("\n");
                recommendations.append("  - 💰 Savings (20%): $")
                        .append(String.format("%.2f", monthlyIncome * 0.2)).append("\n");
            }

            // Weekly money-saving tips
            recommendations.append("\n📅 WEEKLY MONEY-SAVING TIPS\n");
            recommendations.append("  • 🚫 Try a 'no-spend weekend' once a month\n");
            recommendations.append("  • 💵 Use cash instead of cards for discretionary spending\n");
            recommendations.append("  • 📱 Review subscriptions - cancel unused ones\n");
            recommendations.append("  • 🍱 Pack lunch 2-3 times per week\n");
            recommendations.append("  • ☕ Make coffee at home instead of buying\n");
            recommendations.append("  • 🛒 Use grocery lists to avoid impulse buys\n");
            recommendations.append("  • 📊 Track every expense for one week\n");

            // Goal setting
            recommendations.append("\n🎯 FINANCIAL GOALS SUGGESTIONS\n");
            recommendations.append("  • Short-term (3-6 months): Save $1,000 emergency fund\n");
            recommendations.append("  • Medium-term (1-2 years): Pay off credit card debt\n");
            recommendations.append("  • Long-term (5+ years): Save for down payment\n");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        insights.append(recommendations);
        return recommendations.toString();
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

    // Individual insight methods (can be called separately)
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
        summary.append("📊 QUICK FINANCIAL SUMMARY\n");
        summary.append("==========================\n\n");

        try (Connection conn = DatabaseConnection.getConnection()) {
            this.conn = conn;

            // Current balance
            summary.append("💰 Current Balance: $")
                    .append(String.format("%.2f", currentUser.getCurrentBalance())).append("\n\n");

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
                summary.append("  • Income: $").append(String.format("%.2f", income)).append("\n");
                summary.append("  • Expenses: $").append(String.format("%.2f", expenses)).append("\n");
                summary.append("  • Net: $").append(String.format("%.2f", income - expenses)).append("\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return summary.toString();
    }
}