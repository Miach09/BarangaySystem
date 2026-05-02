package barangay;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class RequestHistoryPanel extends JPanel {

    private final AppController ctrl;

    public RequestHistoryPanel(AppController ctrl) {
        this.ctrl = ctrl;
        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel bar = UITheme.headerBar("Request History");
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
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setBackground(UITheme.BG_MAIN);
        body.setBorder(new EmptyBorder(16, 24, 20, 24));
        body.setMinimumSize(new Dimension(600, 480));

        JButton backBtn = UITheme.backButton("Back to Dashboard");
        backBtn.addActionListener(e -> ctrl.showGuestDashboard());

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        topRow.setOpaque(false);
        topRow.add(backBtn);

        // Request list card
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));

        JLabel title = new JLabel("All Requests");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        card.add(title, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        User user = Database.getCurrentUser();
        List<CertRequest> reqs = Database.getRequestsForUser(user.email);

        if (reqs.isEmpty()) {
            JLabel empty = new JLabel("You have no requests yet.");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            empty.setForeground(UITheme.TEXT_MUTED);
            listPanel.add(empty);
        } else {
            for (CertRequest req : reqs) {
                listPanel.add(buildRow(req));
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

        body.add(topRow, BorderLayout.NORTH);
        body.add(card, BorderLayout.CENTER);

        return body;
    }

    private JPanel buildRow(CertRequest req) {
        JPanel row = new JPanel(new BorderLayout(10, 4));
        row.setBackground(new Color(240, 250, 240));
        row.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(12, 16, 12, 16)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        row.setPreferredSize(new Dimension(0, 80));

        // Left: type + timestamp + reject reason
        JPanel textPanel = new JPanel(new GridLayout(0, 1, 0, 3));
        textPanel.setOpaque(false);

        JLabel typeLbl = new JLabel(req.requestType);
        typeLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel dateLbl = new JLabel("Submitted: " + req.dateSubmitted);
        dateLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLbl.setForeground(UITheme.TEXT_MUTED);

        textPanel.add(typeLbl);
        textPanel.add(dateLbl);

        // Show reject reason if rejected
        if (req.status == CertRequest.RequestStatus.REJECTED
                && req.rejectReason != null && !req.rejectReason.isEmpty()) {
            JLabel reasonLbl = new JLabel("Reason: " + req.rejectReason);
            reasonLbl.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            reasonLbl.setForeground(new Color(180, 30, 30));
            textPanel.add(reasonLbl);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
            row.setPreferredSize(new Dimension(0, 100));
        }

        row.add(textPanel, BorderLayout.CENTER);
        row.add(statusBadgeExtended(req.status), BorderLayout.EAST);
        return row;
    }

    private JLabel statusBadgeExtended(CertRequest.RequestStatus status) {
        String text;
        Color bg, fg;
        switch (status) {
            case ACCEPTED:
                text = "Accepted"; bg = new Color(180, 240, 200); fg = new Color(0, 100, 40); break;
            case REJECTED:
                text = "Rejected"; bg = new Color(255, 200, 200); fg = new Color(150, 0, 0);  break;
            default:
                text = "Pending";  bg = new Color(255, 235, 170); fg = new Color(120, 70, 0);  break;
        }
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setOpaque(true);
        lbl.setBackground(bg);
        lbl.setForeground(fg);
        lbl.setBorder(new EmptyBorder(4, 10, 4, 10));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        return lbl;
    }
}