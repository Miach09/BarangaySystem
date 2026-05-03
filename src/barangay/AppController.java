package barangay;

import javax.swing.*;
import java.awt.*;

public class AppController {

    private static final int APP_W = 1000;
    private static final int APP_H = 720;

    private final JFrame frame;

    public AppController() {
        frame = new JFrame("Barangay Bagong Sikat – Certificate Request System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(700, 500));
        frame.setLocationRelativeTo(null);
    }

    // Screen navigation

    public void showLogin() {
        frame.setExtendedState(JFrame.NORMAL);
        setScreen(new LoginPanel(this));
        frame.setTitle("Login – Barangay Bagong Sikat");
        frame.setSize(640, 650);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void showRegister() {
        frame.setExtendedState(JFrame.NORMAL);
        setScreen(new RegisterPanel(this));
        frame.setTitle("Create Account – Barangay Bagong Sikat");
        frame.setSize(640, 760);
        frame.setLocationRelativeTo(null);
    }

    public void showGuestDashboard() {
        setScreen(new GuestDashboardPanel(this));
        frame.setTitle("Dashboard – Barangay Bagong Sikat");
        setSizeIfNotMaximized(APP_W, APP_H);
        // Show floating toast for accepted/rejected requests
        User u = Database.getCurrentUser();
        if (u != null && !u.isAdmin) {
            SwingUtilities.invokeLater(() ->
                ToastNotification.show(frame, u.email));
        }
    }

    public void showNewRequest() {
        setScreen(new NewRequestPanel(this));
        frame.setTitle("New Request – Barangay Bagong Sikat");
        setSizeIfNotMaximized(APP_W, APP_H);
    }

    public void showRequestHistory() {
        setScreen(new RequestHistoryPanel(this));
        frame.setTitle("Request History – Barangay Bagong Sikat");
        setSizeIfNotMaximized(APP_W, APP_H);
    }

    public void showGuestSettings() {
        setScreen(new SettingsPanel(this, false));
        frame.setTitle("Settings – Barangay Bagong Sikat");
        setSizeIfNotMaximized(APP_W, APP_H);
    }

    public void showAdminDashboard() {
        setScreen(new AdminDashboardPanel(this));
        frame.setTitle("Admin Dashboard – Barangay Bagong Sikat");
        setSizeIfNotMaximized(APP_W, APP_H);
    }

    public void showAdminSettings() {
        setScreen(new SettingsPanel(this, true));
        frame.setTitle("Admin Settings – Barangay Bagong Sikat");
        setSizeIfNotMaximized(APP_W, APP_H);
    }

    public void showNotifications() {
        setScreen(new NotificationPanel(this));
        frame.setTitle("Notifications – Barangay Bagong Sikat");
        setSizeIfNotMaximized(APP_W, APP_H);
    }

    public void showAdminRequestHistory() {
        setScreen(new AdminRequestHistoryPanel(this));
        frame.setTitle("Request History – Barangay Bagong Sikat");
        setSizeIfNotMaximized(APP_W, APP_H);
    }

    public void logout() {
        Database.logout();
        showLogin();
    }

    // Helpers
    /** Only resize if the window is not currently maximized */
    private void setSizeIfNotMaximized(int w, int h) {
        if (frame.getExtendedState() != JFrame.MAXIMIZED_BOTH) {
            frame.setSize(w, h);
            frame.setLocationRelativeTo(null);
        }
    }

    private void stopCurrentTimer() {
        java.awt.Component current = frame.getContentPane();
        // Unwrap from scroll pane if needed
        if (current instanceof JScrollPane) {
            current = ((JScrollPane) current).getViewport().getView();
        }
        if (current instanceof AdminDashboardPanel)
            ((AdminDashboardPanel) current).stopAutoRefresh();
        if (current instanceof GuestDashboardPanel)
            ((GuestDashboardPanel) current).stopAutoRefresh();
    }

    private void setScreen(JPanel panel) {
        stopCurrentTimer();
        if (panel instanceof LoginPanel || panel instanceof RegisterPanel) {
            frame.setContentPane(panel);
        } else {
            JScrollPane scroll = new JScrollPane(panel);
            scroll.setBorder(null);
            scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            frame.setContentPane(scroll);
        }
        frame.revalidate();
        frame.repaint();
    }

    public JFrame getFrame() { return frame; }
}