package utils;

import Database.DatabaseConnection;
import models.Notification;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationManager {

    public static void createNotification(int userId, String title, String message) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO notifications (user_id, title, message) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, userId);
            pstmt.setString(2, title);
            pstmt.setString(3, message);
            pstmt.executeUpdate();

            // Show popup for important notifications
            if (title.contains("Budget") || title.contains("Alert")) {
                showNotificationPopup(title, message);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Notification> getUnreadNotifications(int userId) {
        List<Notification> notifications = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM notifications WHERE user_id = ? AND is_read = FALSE ORDER BY created_at DESC";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                notifications.add(new Notification(
                        rs.getInt("notification_id"),
                        rs.getInt("user_id"),
                        rs.getString("title"),
                        rs.getString("message"),
                        rs.getBoolean("is_read"),
                        rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifications;
    }

    public static void markAsRead(int notificationId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE notifications SET is_read = TRUE WHERE notification_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, notificationId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void showNotificationPopup(String title, String message) {
        if (SystemTray.isSupported()) {
            try {
                SystemTray tray = SystemTray.getSystemTray();
                Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
                TrayIcon trayIcon = new TrayIcon(image, "Finance Tracker");
                trayIcon.displayMessage(title, message, TrayIcon.MessageType.WARNING);
                tray.add(trayIcon);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE);
        }
    }
}