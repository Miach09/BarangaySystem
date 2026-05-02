package barangay;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class GuestDashboardPanel extends JPanel {

    private final AppController ctrl;

    public GuestDashboardPanel(AppController ctrl) {
        this.ctrl = ctrl;
        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
    }

    /** Draws a clean bell icon as an ImageIcon */
    private static ImageIcon drawBellIcon(int size, Color color) {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
            size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(color);
        int cx = size / 2;
        // Bell body (dome shape)
        g.fillArc(3, 3, size - 6, size - 6, 0, 180);
        // Bell skirt (rectangle bottom)
        g.fillRect(3, size / 2, size - 6, size / 2 - 5);
        // Bell rim bar
        g.fillRoundRect(1, size - 7, size - 2, 3, 2, 2);
        // Clapper dot
        g.fillOval(cx - 2, size - 4, 4, 4);
        // Erase top handle notch
        g.setComposite(java.awt.AlphaComposite.Clear);
        g.fillOval(cx - 2, 0, 4, 5);
        g.dispose();
        return new ImageIcon(img);
    }

    private JPanel buildHeader() {
        JPanel bar = UITheme.headerBar("DASHBOARD");
        bar.setPreferredSize(new Dimension(0, 60));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        right.setOpaque(false);

        // Notification bell with red badge number
        User user = Database.getCurrentUser();
        int unread = user != null ? Database.countUnreadNotifications(user.email) : 0;

        /* Icon-only bell button — yellow default, darker yellow on hover
           Falls back to drawn icon if file not found. */
        int btnSize = 42;
        int iconSize = 22;

        // Try loading image
        ImageIcon bellNormal = null;
        ImageIcon bellHover  = null;
        try {
            java.net.URL bellUrl = GuestDashboardPanel.class.getResource("/barangay/bell.png");
            if (bellUrl != null) {
                Image raw = new ImageIcon(bellUrl).getImage()
                    .getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
                bellNormal = new ImageIcon(raw);
                bellHover  = bellNormal; 
            }
        } catch (Exception ignored) {}

        // Fallback to drawn icon
        final ImageIcon finalNormal = bellNormal != null ? bellNormal : drawBellIcon(iconSize, new Color(80, 55, 0));
        final ImageIcon finalHover  = bellHover  != null ? bellHover  : drawBellIcon(iconSize, new Color(50, 30, 0));

        JPanel notifWrapper = new JPanel(null);
        notifWrapper.setOpaque(false);
        notifWrapper.setPreferredSize(new Dimension(btnSize, btnSize));

        JButton notifBtn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()
                    ? new Color(220, 170, 0)   
                    : new Color(255, 228, 114));  
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                // Draw icon centered
                ImageIcon icon = getModel().isRollover() ? finalHover : finalNormal;
                int ix = (getWidth()  - icon.getIconWidth())  / 2;
                int iy = (getHeight() - icon.getIconHeight()) / 2;
                icon.paintIcon(this, g, ix, iy);
            }
        };
        notifBtn.setOpaque(false);
        notifBtn.setContentAreaFilled(false);
        notifBtn.setBorderPainted(false);
        notifBtn.setFocusPainted(false);
        notifBtn.setToolTipText("Notifications");
        notifBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        notifBtn.addActionListener(e -> ctrl.showNotifications());
        notifBtn.setBounds(0, 0, btnSize, btnSize);
        notifBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { notifBtn.repaint(); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { notifBtn.repaint(); }
        });
        notifWrapper.add(notifBtn);

        // Red badge circle with unread count
        if (unread > 0) {
            JLabel badge = new JLabel(String.valueOf(unread), SwingConstants.CENTER) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(210, 30, 30));
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
            badge.setForeground(Color.WHITE);
            badge.setOpaque(false);
            int bw = unread > 9 ? 20 : 16;
            badge.setBounds(btnSize - bw, 0, bw, bw);
            notifWrapper.add(badge);
            notifWrapper.setComponentZOrder(badge, 0);
        }

        JButton settings = UITheme.settingsButton();
        JButton logout   = UITheme.logoutButton();
        settings.addActionListener(e -> ctrl.showGuestSettings());
        logout.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(this),
                    "Are you sure you want to logout?", 
                    "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) ctrl.logout();
        });

        right.add(notifWrapper);
        right.add(settings);
        right.add(logout);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(UITheme.BG_MAIN);
        body.setBorder(new EmptyBorder(24, 30, 24, 30));
        body.setMinimumSize(new Dimension(600, 480));
        body.setPreferredSize(new Dimension(820, 580));

        User user = Database.getCurrentUser();
        List<CertRequest> myReqs = Database.getRequestsForUser(user.email);

        long pending  = myReqs.stream().filter(r -> r.status == CertRequest.RequestStatus.PENDING).count();
        long accepted = myReqs.stream().filter(r -> r.status == CertRequest.RequestStatus.ACCEPTED).count();

        // Welcome label
        JLabel welcome = new JLabel("Welcome back, " + user.fullName + "!");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcome.setForeground(UITheme.TEXT_DARK);
        welcome.setAlignmentX(LEFT_ALIGNMENT);

        // Stat cards
        JPanel stats = new JPanel(new GridLayout(1, 3, 16, 0));
        stats.setOpaque(false);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        stats.setAlignmentX(LEFT_ALIGNMENT);
        stats.add(bigStatCard("Total Request",    String.valueOf(myReqs.size()), UITheme.PRIMARY));
        stats.add(bigStatCard("Pending Request",   String.valueOf(pending),       UITheme.WARNING));
        stats.add(bigStatCard("Accepted Request",  String.valueOf(accepted),      UITheme.SUCCESS));

        // Recent requests card
        JPanel recentCard = UITheme.card();
        recentCard.setLayout(new BorderLayout(0, 10));
        recentCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        recentCard.setAlignmentX(LEFT_ALIGNMENT);

        JLabel recentTitle = new JLabel("Recent Request");
        recentTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        recentCard.add(recentTitle, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        if (myReqs.isEmpty()) {
            JLabel empty = new JLabel("No requests yet.");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            empty.setForeground(UITheme.TEXT_MUTED);
            listPanel.add(empty);
        } else {
            for (CertRequest req : myReqs) {
                listPanel.add(requestRow(req));
                listPanel.add(Box.createVerticalStrut(8));
            }
        }

        JScrollPane recentScroll = new JScrollPane(listPanel);
        recentScroll.setBorder(null);
        recentScroll.setOpaque(false);
        recentScroll.getViewport().setOpaque(false);
        recentScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        recentScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        recentScroll.getVerticalScrollBar().setUnitIncrement(12);
        recentScroll.setPreferredSize(new Dimension(0, 240));
        recentCard.add(recentScroll, BorderLayout.CENTER);

        // Action buttons
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 16, 0));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        btnRow.setAlignmentX(LEFT_ALIGNMENT);

        JButton newReqBtn = UITheme.primaryButton("+ New Request");
        JButton histBtn   = UITheme.secondaryButton("Request History");
        newReqBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        histBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        newReqBtn.addActionListener(e -> ctrl.showNewRequest());
        histBtn.addActionListener(e -> ctrl.showRequestHistory());

        btnRow.add(newReqBtn);
        btnRow.add(histBtn);

        body.add(welcome);
        body.add(Box.createVerticalStrut(16));
        body.add(stats);
        body.add(Box.createVerticalStrut(20));
        body.add(recentCard);
        body.add(Box.createVerticalStrut(20));
        body.add(btnRow);

        return body;
    }

    // Bigger stat card than the default UITheme one
    private JPanel bigStatCard(String label, String value, Color iconColor) {
        JPanel card = new JPanel(new BorderLayout(10, 6));
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(18, 20, 18, 20)));

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valLbl.setForeground(UITheme.TEXT_DARK);

        JLabel nameLbl = new JLabel(label);
        nameLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        nameLbl.setForeground(UITheme.TEXT_MUTED);

        JLabel icon = new JLabel("●");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        icon.setForeground(iconColor);
        icon.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        textPanel.setOpaque(false);
        textPanel.add(valLbl);
        textPanel.add(nameLbl);

        card.add(textPanel, BorderLayout.CENTER);
        card.add(icon, BorderLayout.EAST);
        return card;
    }

    private JPanel requestRow(CertRequest req) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(new Color(240, 250, 240));
        row.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(12, 16, 12, 16)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        JLabel typeLbl = new JLabel(req.requestType);
        typeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        row.add(typeLbl, BorderLayout.CENTER);
        row.add(UITheme.statusBadge(req.status), BorderLayout.EAST);
        return row;
    }
}