package models;

import java.sql.Timestamp;

public class Transaction {
    private int transactionId;
    private String type;
    private double amount;
    private String category;
    private String description;
    private Timestamp date;
    private double balanceAfter;

    public Transaction(int transactionId, String type, double amount, String category,
                       String description, Timestamp date, double balanceAfter) {
        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.date = date;
        this.balanceAfter = balanceAfter;
    }

    // Getters
    public int getTransactionId() { return transactionId; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public Timestamp getDate() { return date; }
    public double getBalanceAfter() { return balanceAfter; }
}