package utils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReportExporter {

    public static void exportToText(String content, JFrame parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("finance_report_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt"));

        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt");
        fileChooser.setFileFilter(filter);

        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.print(content);
                JOptionPane.showMessageDialog(parent,
                        "Report exported successfully!\nSaved to: " + file.getAbsolutePath(),
                        "Export Complete",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(parent,
                        "Error exporting report: " + e.getMessage(),
                        "Export Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void exportToHTML(String htmlContent, JFrame parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("finance_report_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".html"));

        FileNameExtensionFilter filter = new FileNameExtensionFilter("HTML Files", "html");
        fileChooser.setFileFilter(filter);

        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.print(htmlContent);
                JOptionPane.showMessageDialog(parent,
                        "Report exported successfully!\nSaved to: " + file.getAbsolutePath(),
                        "Export Complete",
                        JOptionPane.INFORMATION_MESSAGE);

                // Ask if user wants to open the file
                int open = JOptionPane.showConfirmDialog(parent,
                        "Would you like to open the report now?",
                        "Open Report",
                        JOptionPane.YES_NO_OPTION);

                if (open == JOptionPane.YES_OPTION) {
                    Desktop.getDesktop().browse(file.toURI());
                }

            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(parent,
                        "Error exporting report: " + e.getMessage(),
                        "Export Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void exportToPDF(JTextArea content, JFrame parent) {
        // Note: For PDF export, you would need a library like iText
        // This is a placeholder for future implementation
        JOptionPane.showMessageDialog(parent,
                "PDF export feature coming soon!\nFor now, please use Text or HTML format.",
                "Feature Coming Soon",
                JOptionPane.INFORMATION_MESSAGE);
    }
}