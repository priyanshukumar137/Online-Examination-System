package com.group2.onlineexam;

import com.group2.onlineexam.ui.OnlineExamApp;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Use Swing's default look and feel if the system theme is unavailable.
            }
            new OnlineExamApp().show();
        });
    }
}
