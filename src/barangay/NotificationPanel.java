package barangay;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class NotificationPanel extends JPanel {

    private final AppController ctrl;

    public NotificationPanel(AppController ctrl) {
        this.ctrl = ctrl;
        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);

        // Mark all as read when opened
        User user = Database.getCurrentUser();
        if (user != null) Database.markAllNotificationsRead(user.email);
    }

    private JPanel buildHeader() {
        JPanel bar = UITheme.headerBar("Notifications");
        bar.setPreferredSize(new Dimension(0, 60));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        right.setOpaque(false);
        JButton settings = UITheme.settingsButton();
        JButton logout   = UITheme.logoutButton();
        settings.addActionListener(e -> ctrl.showGuestSettings());
        logout.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(this), "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) ctrl.logout();
        });
        right.add(settings);
        right.add(logout);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(UITheme.BG_MAIN);
        body.setBorder(new EmptyBorder(20, 24, 20, 24));
        body.setMinimumSize(new Dimension(600, 480));

        // Back button
        JButton backBtn = UITheme.backButton("Back to Dashboard");
        backBtn.setAlignmentX(LEFT_ALIGNMENT);
        backBtn.addActionListener(e -> ctrl.showGuestDashboard());
        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        backRow.setOpaque(false);
        backRow.setAlignmentX(LEFT_ALIGNMENT);
        backRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        backRow.add(backBtn);

        // Notification card
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JLabel cardTitle = new JLabel("All Notifications");
        cardTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        card.add(cardTitle, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        User user = Database.getCurrentUser();
        List<Notification> notifs = Database.getNotificationsForUser(user.email);

        if (notifs.isEmpty()) {
            JPanel emptyPanel = new JPanel(new BorderLayout());
            emptyPanel.setOpaque(false);
            emptyPanel.setBorder(new EmptyBorder(30, 0, 30, 0));
            JLabel emptyLbl = new JLabel("No notifications yet.", SwingConstants.CENTER);
            emptyLbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            emptyLbl.setForeground(UITheme.TEXT_MUTED);
            emptyPanel.add(emptyLbl, BorderLayout.CENTER);
            listPanel.add(emptyPanel);
        } else {
            for (Notification n : notifs) {
                listPanel.add(buildNotifRow(n));
                listPanel.add(Box.createVerticalStrut(8));
            }
        }

        JPanel listWrapper = new JPanel(new BorderLayout());
        listWrapper.setOpaque(false);
        listWrapper.add(listPanel, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(listWrapper);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JButton backToTopBtn = new JButton("↑  Top") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover() ? new Color(30, 100, 210) : new Color(60, 60, 60, 200);
                g2.setColor(bg); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose(); super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        backToTopBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        backToTopBtn.setForeground(Color.WHITE);
        backToTopBtn.setOpaque(false); backToTopBtn.setContentAreaFilled(false);
        backToTopBtn.setBorderPainted(false); backToTopBtn.setFocusPainted(false);
        backToTopBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backToTopBtn.setVisible(false);
        backToTopBtn.addActionListener(e -> scroll.getVerticalScrollBar().setValue(0));
        scroll.getVerticalScrollBar().addAdjustmentListener(e2 ->
            backToTopBtn.setVisible(e2.getValue() > 80));

        JLayeredPane layered = new JLayeredPane();
        layered.setLayout(null);
        layered.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e2) {
                scroll.setBounds(0, 0, layered.getWidth(), layered.getHeight());
                int bw = 80, bh = 30;
                backToTopBtn.setBounds(
                    layered.getWidth() - bw - scroll.getVerticalScrollBar().getWidth() - 8,
                    layered.getHeight() - bh - 10, bw, bh);
            }
        });
        layered.add(scroll,       JLayeredPane.DEFAULT_LAYER);
        layered.add(backToTopBtn, JLayeredPane.POPUP_LAYER);
        card.add(layered, BorderLayout.CENTER);

        // Switch body to BorderLayout so card fills all remaining height
        body.setLayout(new BorderLayout(0, 12));
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);
        topSection.add(backRow);
        body.add(topSection, BorderLayout.NORTH);
        body.add(card,       BorderLayout.CENTER);

        return body;
    }

    private JPanel buildNotifRow(Notification n) {
        boolean isResolved = "ACCEPTED".equals(n.type);

        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(n.isRead ? Color.WHITE : new Color(235, 245, 255));
        row.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(isResolved ? new Color(180, 230, 180) : new Color(255, 180, 180), 1, true),
            new EmptyBorder(14, 16, 14, 16)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        row.setAlignmentX(LEFT_ALIGNMENT);

        // Icon label on the left
        ImageIcon notifIcon = null;
        try {
            String imgName = isResolved ? "/barangay/accept.png" : "/barangay/rejected.png";
            java.net.URL iconUrl = NotificationPanel.class.getResource(imgName);
            if (iconUrl != null) {
                Image iconImg = new ImageIcon(iconUrl).getImage().getScaledInstance(28, 28, Image.SCALE_SMOOTH);
                notifIcon = new ImageIcon(iconImg);
            }
        } catch (Exception ex) { /* fallback below */ }
        JLabel iconLbl = notifIcon != null ? new JLabel(notifIcon) : new JLabel(isResolved ? "\u2713" : "\u2717");
        if (notifIcon == null) {
            iconLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
            iconLbl.setForeground(isResolved ? new Color(34, 160, 90) : new Color(210, 50, 50));
        }
        iconLbl.setVerticalAlignment(SwingConstants.CENTER);

        // Text block
        JPanel textPanel = new JPanel(new GridLayout(0, 1, 0, 4));
        textPanel.setOpaque(false);

        String mainMsg = n.message;
        String subMsg  = null;
        if (!isResolved) {
            int reasonIdx = n.message.indexOf("Reason:");
            if (reasonIdx != -1) {
                mainMsg = n.message.substring(0, reasonIdx).trim();
                subMsg  = n.message.substring(reasonIdx).trim();
            }
        } else {
            subMsg = "Please go to the barangay hall to claim your certificate.";
        }

        JLabel msgLbl = new JLabel("<html><body style='width:460px'>" + mainMsg + "</body></html>");
        msgLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        msgLbl.setForeground(isResolved ? new Color(0, 100, 40) : new Color(150, 0, 0));

        JLabel timeLbl = new JLabel(n.createdAt);
        timeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLbl.setForeground(UITheme.TEXT_MUTED);

        textPanel.add(msgLbl);
        if (subMsg != null) {
            JLabel subLbl = new JLabel("<html><body style='width:460px'>" + subMsg + "</body></html>");
            subLbl.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            subLbl.setForeground(isResolved ? new Color(30, 130, 60) : new Color(180, 30, 30));
            textPanel.add(subLbl);
        }
        textPanel.add(timeLbl);

        row.add(iconLbl,   BorderLayout.WEST);
        row.add(textPanel, BorderLayout.CENTER);

        // Unread dot
        if (!n.isRead) {
            JLabel dot = new JLabel("●");
            dot.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            dot.setForeground(new Color(30, 100, 210));
            row.add(dot, BorderLayout.EAST);
        }

        return row;
    }
}