package models;

import java.sql.Timestamp;

public class Notification {
    private int notificationId;
    private int userId;
    private String title;
    private String message;
    private boolean isRead;
    private Timestamp createdAt;

    public Notification(int notificationId, int userId, String title,
                        String message, boolean isRead, Timestamp createdAt) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    // Getters
    public int getNotificationId() { return notificationId; }
    public int getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public boolean isRead() { return isRead; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setRead(boolean read) { isRead = read; }
}