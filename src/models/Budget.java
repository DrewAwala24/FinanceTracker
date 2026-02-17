package models;

public class Budget {
    private int budgetId;
    private int userId;
    private int categoryId;
    private String categoryName;
    private double monthlyLimit;
    private double spentSoFar;

    public Budget(int budgetId, int userId, int categoryId, String categoryName,
                  double monthlyLimit, double spentSoFar) {
        this.budgetId = budgetId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.monthlyLimit = monthlyLimit;
        this.spentSoFar = spentSoFar;
    }

    // Getters and Setters
    public int getBudgetId() { return budgetId; }
    public int getUserId() { return userId; }
    public int getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public double getMonthlyLimit() { return monthlyLimit; }
    public double getSpentSoFar() { return spentSoFar; }
    public void setSpentSoFar(double spentSoFar) { this.spentSoFar = spentSoFar; }

    public double getPercentage() {
        if (monthlyLimit == 0) return 0;
        return (spentSoFar / monthlyLimit) * 100;
    }

    public double getRemaining() {
        return monthlyLimit - spentSoFar;
    }
}