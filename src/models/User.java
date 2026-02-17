package models;

public class User {
    private int userId;
    private String username;
    private String password;
    private String email;
    private String phoneNumber;
    private double currentBalance;

    public User(int userId, String username, String email, String phoneNumber, double currentBalance) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.currentBalance = currentBalance;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public double getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(double currentBalance) { this.currentBalance = currentBalance; }
}