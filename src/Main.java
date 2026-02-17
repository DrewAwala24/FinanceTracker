import Database.DatabaseConnection;
import gui.LoginFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Set Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Start application
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });


    }
}