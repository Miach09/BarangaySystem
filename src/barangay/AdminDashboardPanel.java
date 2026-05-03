package barangay;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.print.*;
import java.util.List;

public class AdminDashboardPanel extends JPanel {

    private final AppController ctrl;
    private String currentSort   = "date_desc";
    private String currentFilter = "PENDING";

    private JLabel totalLbl, pendingLbl, acceptedLbl, rejectedLbl;
    private JPanel listPanel;
    private JComboBox<String> sortBox;
    private JComboBox<String> filterBox;
    private javax.swing.Timer autoRefreshTimer;

    public AdminDashboardPanel(AppController ctrl) {
        this.ctrl = ctrl;
        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
        startAutoRefresh();
    }

    private void startAutoRefresh() {
        autoRefreshTimer = new javax.swing.Timer(5000, e -> {
            // Only refresh if this panel is still showing
            if (isShowing()) refreshList();
        });
        autoRefreshTimer.setRepeats(true);
        autoRefreshTimer.start();
    }

    public void stopAutoRefresh() {
        if (autoRefreshTimer != null) autoRefreshTimer.stop();
    }

    private JPanel buildHeader() {
        JPanel bar = UITheme.headerBar("ADMIN DASHBOARD");
        bar.setPreferredSize(new Dimension(0, 60));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        right.setOpaque(false);
        JButton settings = UITheme.settingsButton();
        JButton logout   = UITheme.logoutButton();
        settings.addActionListener(e -> ctrl.showAdminSettings());
        logout.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(this), 
            "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
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
        body.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Top section holds fixed-height elements (stats + controls)
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);

        // Stat cards
        JPanel stats = new JPanel(new GridLayout(1, 4, 12, 0));
        stats.setOpaque(false);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        stats.setAlignmentX(LEFT_ALIGNMENT);

        totalLbl    = new JLabel("0");
        pendingLbl  = new JLabel("0");
        acceptedLbl = new JLabel("0");
        rejectedLbl = new JLabel("0");

        stats.add(bigStatCard("Total",    totalLbl,    UITheme.PRIMARY));
        stats.add(bigStatCard("Pending",  pendingLbl,  UITheme.WARNING));
        stats.add(bigStatCard("Accepted", acceptedLbl, UITheme.SUCCESS));
        stats.add(bigStatCard("Rejected", rejectedLbl, new Color(200, 50, 50)));

        // Controls row: Filter + Sort (left)  |  Request History (right)
        JPanel sortRow = new JPanel(new BorderLayout(0, 0));
        sortRow.setOpaque(false);
        sortRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        sortRow.setAlignmentX(LEFT_ALIGNMENT);

        // Left side: filter + sort combos
        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftControls.setOpaque(false);

        JLabel filterLbl = new JLabel("Filter:");
        filterLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        String[] filterOptions = {"All", "Pending", "Accepted", "Rejected"};
        filterBox = new JComboBox<>(filterOptions);
        filterBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        filterBox.setSelectedIndex(1);
        filterBox.addActionListener(e -> {
            switch (filterBox.getSelectedIndex()) {
                case 0: currentFilter = "ALL";      break;
                case 1: currentFilter = "PENDING";  break;
                case 2: currentFilter = "ACCEPTED"; break;
                case 3: currentFilter = "REJECTED"; break;
            }
            refreshList();
        });

        JLabel sortLbl = new JLabel("Sort by:");
        sortLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        String[] sortOptions = {
            "Date (Newest First)", "Date (Oldest First)",
            "Name (A-Z)", "Name (Z-A)"
        };
        sortBox = new JComboBox<>(sortOptions);
        sortBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sortBox.addActionListener(e -> {
            switch (sortBox.getSelectedIndex()) {
                case 0: currentSort = "date_desc"; break;
                case 1: currentSort = "date_asc";  break;
                case 2: currentSort = "name_asc";  break;
                case 3: currentSort = "name_desc"; break;
            }
            refreshList();
        });

        leftControls.add(filterLbl);
        leftControls.add(filterBox);
        leftControls.add(Box.createHorizontalStrut(8));
        leftControls.add(sortLbl);
        leftControls.add(sortBox);

        // Right side: Request History button
        final Color HIST_BASE  = new Color(30, 100, 210);
        final Color HIST_HOVER = new Color(20, 80, 180);
        JButton historyBtn = new JButton("Request History") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed() ? HIST_HOVER.darker()
                         : getModel().isRollover() ? HIST_HOVER : HIST_BASE;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight() / 2, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        historyBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        historyBtn.setForeground(Color.WHITE);
        historyBtn.setOpaque(false);
        historyBtn.setContentAreaFilled(false);
        historyBtn.setBorderPainted(false);
        historyBtn.setFocusPainted(false);
        historyBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        historyBtn.setPreferredSize(new Dimension(172, 36));
        historyBtn.addActionListener(e -> ctrl.showAdminRequestHistory());

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightControls.setOpaque(false);
        rightControls.add(historyBtn);

        sortRow.add(leftControls,  BorderLayout.WEST);
        sortRow.add(rightControls, BorderLayout.EAST);

        // All Requests card
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JLabel cardTitle = new JLabel("All Requests");
        cardTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        card.add(cardTitle, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        // Wrap listPanel so scroll measures content height correctly
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

        //Back-to-top floating button
        JButton backToTopBtn = new JButton("↑  Top") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover()
                    ? new Color(30, 100, 210)
                    : new Color(60, 60, 60, 200);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        backToTopBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        backToTopBtn.setForeground(Color.WHITE);
        backToTopBtn.setOpaque(false);
        backToTopBtn.setContentAreaFilled(false);
        backToTopBtn.setBorderPainted(false);
        backToTopBtn.setFocusPainted(false);
        backToTopBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backToTopBtn.setVisible(false);
        backToTopBtn.addActionListener(e ->
            scroll.getVerticalScrollBar().setValue(0));

        // Show/hide back-to-top based on scroll position
        scroll.getVerticalScrollBar().addAdjustmentListener(e2 -> {
            backToTopBtn.setVisible(e2.getValue() > 80);
        });

        // Overlay the button on top of the scroll pane using a layered panel
        JLayeredPane layered = new JLayeredPane();
        layered.setLayout(null);
        scroll.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e2) {
                scroll.setBounds(0, 0, layered.getWidth(), layered.getHeight());
                int bw = 80, bh = 30;
                backToTopBtn.setBounds(
                    layered.getWidth() - bw - scroll.getVerticalScrollBar().getWidth() - 8,
                    layered.getHeight() - bh - 10,
                    bw, bh);
            }
        });
        layered.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e2) {
                scroll.setBounds(0, 0, layered.getWidth(), layered.getHeight());
                int bw = 80, bh = 30;
                backToTopBtn.setBounds(
                    layered.getWidth() - bw - scroll.getVerticalScrollBar().getWidth() - 8,
                    layered.getHeight() - bh - 10,
                    bw, bh);
            }
        });
        layered.add(scroll,        JLayeredPane.DEFAULT_LAYER);
        layered.add(backToTopBtn,  JLayeredPane.POPUP_LAYER);

        card.add(layered, BorderLayout.CENTER);

        topSection.add(stats);
        topSection.add(Box.createVerticalStrut(12));
        topSection.add(sortRow);

        body.add(topSection, BorderLayout.NORTH);
        body.add(card,       BorderLayout.CENTER);

        refreshList();
        return body;
    }

    private void refreshList() {
        List<CertRequest> all = Database.getAllRequestsSorted(currentSort);
        long pending  = all.stream().filter(r -> r.status == CertRequest.RequestStatus.PENDING).count();
        long accepted = all.stream().filter(r -> r.status == CertRequest.RequestStatus.ACCEPTED).count();
        long rejected = all.stream().filter(r -> r.status == CertRequest.RequestStatus.REJECTED).count();

        totalLbl.setText(String.valueOf(all.size()));
        pendingLbl.setText(String.valueOf(pending));
        acceptedLbl.setText(String.valueOf(accepted));
        rejectedLbl.setText(String.valueOf(rejected));

        // Apply filter
        List<CertRequest> filtered = new java.util.ArrayList<>(all);
        if (!currentFilter.equals("ALL")) {
            filtered.removeIf(r -> !r.status.name().equals(currentFilter));
        }

        listPanel.removeAll();
        if (filtered.isEmpty()) {
            JLabel empty = new JLabel("No requests match the selected filter.");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            empty.setForeground(UITheme.TEXT_MUTED);
            listPanel.add(empty);
        } else {
            for (CertRequest req : filtered) {
                listPanel.add(buildAdminRow(req));
                listPanel.add(Box.createVerticalStrut(6));
            }
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel buildAdminRow(CertRequest req) {
        Color rowBg, rowBorder, nameColor, subColor;
        switch (req.status) {
            case PENDING:
                rowBg = new Color(255, 250, 225); rowBorder = new Color(220, 170, 40);
                nameColor = new Color(100, 60, 0); subColor = new Color(140, 100, 20); break;
            case ACCEPTED:
                rowBg = new Color(230, 248, 235); rowBorder = new Color(60, 180, 100);
                nameColor = new Color(0, 90, 30);  subColor = new Color(30, 130, 60);  break;
            case REJECTED:
                rowBg = new Color(255, 235, 235); rowBorder = new Color(210, 60, 60);
                nameColor = new Color(140, 20, 20); subColor = new Color(180, 50, 50); break;
            default:
                rowBg = Color.WHITE; rowBorder = UITheme.BORDER;
                nameColor = UITheme.TEXT_DARK; subColor = UITheme.TEXT_MUTED;
        }

        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(rowBg);
        row.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, rowBorder),
            new EmptyBorder(12, 10, 12, 10)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        row.setPreferredSize(new Dimension(0, 80));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JPanel textPanel = new JPanel(new GridLayout(3, 1, 0, 2));
        textPanel.setOpaque(false);

        JLabel nameLbl = new JLabel(req.requesterName);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLbl.setForeground(nameColor);

        JLabel typeLbl = new JLabel(req.requestType);
        typeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        typeLbl.setForeground(subColor);

        JLabel timeLbl = new JLabel("Submitted: " + req.dateSubmitted);
        timeLbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        timeLbl.setForeground(subColor);

        textPanel.add(nameLbl);
        textPanel.add(typeLbl);
        textPanel.add(timeLbl);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(statusBadge(req.status));

        // Accept/Reject moved into the detail dialog — row shows status only

        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        row.setToolTipText("Click to view full request details");
        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { showRequestDetailDialog(req); }
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { row.setBackground(rowBg.darker()); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { row.setBackground(rowBg); }
        });

        row.add(textPanel,  BorderLayout.CENTER);
        row.add(rightPanel, BorderLayout.EAST);
        return row;
    }

    private void showRequestDetailDialog(CertRequest req) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this),
            "Request Details", true);
        dialog.setSize(760, 560);
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        dialog.setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel titleLbl = new JLabel("Request Details");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(UITheme.TEXT_DARK);
        titleLbl.setAlignmentX(LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(UITheme.BORDER);
        sep.setAlignmentX(LEFT_ALIGNMENT);

        content.add(titleLbl);
        content.add(Box.createVerticalStrut(8));
        content.add(sep);
        content.add(Box.createVerticalStrut(16));

        content.add(detailRow("Request ID",   "#" + req.id));
        content.add(Box.createVerticalStrut(10));
        content.add(detailRow("Requester",    req.requesterName));
        content.add(Box.createVerticalStrut(10));
        content.add(detailRow("Contact",      req.contactNumber));
        content.add(Box.createVerticalStrut(10));
        content.add(detailRow("Email",        req.email));
        content.add(Box.createVerticalStrut(10));
        content.add(detailRow("Civil Status", req.civilStatus));
        content.add(Box.createVerticalStrut(10));
        content.add(detailRow("Sex",          req.sex));
        content.add(Box.createVerticalStrut(10));
        content.add(detailRow("Birthdate",    req.birthdate));
        content.add(Box.createVerticalStrut(10));
        content.add(detailRow("Birthplace",   req.birthplace));
        content.add(Box.createVerticalStrut(10));
        content.add(detailRow("Request Type", req.requestType));
        content.add(Box.createVerticalStrut(10));
        content.add(detailRow("Submitted",    req.dateSubmitted));
        content.add(Box.createVerticalStrut(10));

        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusRow.setOpaque(false);
        statusRow.setAlignmentX(LEFT_ALIGNMENT);
        JLabel statusKey = new JLabel("Status");
        statusKey.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statusKey.setForeground(UITheme.TEXT_MUTED);
        statusKey.setPreferredSize(new Dimension(110, 20));
        statusRow.add(statusKey);
        statusRow.add(statusBadge(req.status));
        content.add(statusRow);
        content.add(Box.createVerticalStrut(14));

        JLabel descKey = new JLabel("Description / Reason for Request");
        descKey.setFont(new Font("Segoe UI", Font.BOLD, 13));
        descKey.setForeground(UITheme.TEXT_MUTED);
        descKey.setAlignmentX(LEFT_ALIGNMENT);

        JTextArea descArea = new JTextArea(req.description);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setBackground(new Color(247, 249, 252));
        descArea.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(null);
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        descScroll.setAlignmentX(LEFT_ALIGNMENT);

        content.add(descKey);
        content.add(Box.createVerticalStrut(6));
        content.add(descScroll);

        if (req.status == CertRequest.RequestStatus.REJECTED
                && req.rejectReason != null && !req.rejectReason.isEmpty()) {
            content.add(Box.createVerticalStrut(14));
            JLabel rejKey = new JLabel("Rejection Reason");
            rejKey.setFont(new Font("Segoe UI", Font.BOLD, 13));
            rejKey.setForeground(new Color(160, 20, 20));
            rejKey.setAlignmentX(LEFT_ALIGNMENT);
            JTextArea rejArea = new JTextArea(req.rejectReason);
            rejArea.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            rejArea.setForeground(new Color(160, 20, 20));
            rejArea.setBackground(new Color(255, 240, 240));
            rejArea.setLineWrap(true);
            rejArea.setWrapStyleWord(true);
            rejArea.setEditable(false);
            rejArea.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 100, 100), 1, true),
                new EmptyBorder(8, 10, 8, 10)));
            rejArea.setAlignmentX(LEFT_ALIGNMENT);
            rejArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            content.add(rejKey);
            content.add(Box.createVerticalStrut(6));
            content.add(rejArea);
        }

        // Action section for PENDING requests
        if (req.status == CertRequest.RequestStatus.PENDING) {
            content.add(Box.createVerticalStrut(14));
            JSeparator actionSep = new JSeparator();
            actionSep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            actionSep.setForeground(UITheme.BORDER);
            actionSep.setAlignmentX(LEFT_ALIGNMENT);
            content.add(actionSep);
            content.add(Box.createVerticalStrut(14));

            JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            actionRow.setOpaque(false);
            actionRow.setAlignmentX(LEFT_ALIGNMENT);

            // Accept button
            final Color ACC_BASE  = new Color(34, 160, 90);
            final Color ACC_HOVER = new Color(24, 130, 70);
            JButton acceptBtn = new JButton("Accept Request") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg = getModel().isPressed() ? ACC_HOVER.darker()
                             : getModel().isRollover() ? ACC_HOVER : ACC_BASE;
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.setColor(new Color(255, 255, 255, 40));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight() / 2, 12, 12);
                    g2.dispose();
                    super.paintComponent(g);
                }
                @Override protected void paintBorder(Graphics g) {}
            };
            acceptBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            acceptBtn.setForeground(Color.WHITE);
            acceptBtn.setOpaque(false);
            acceptBtn.setContentAreaFilled(false);
            acceptBtn.setBorderPainted(false);
            acceptBtn.setFocusPainted(false);
            acceptBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            acceptBtn.setPreferredSize(new Dimension(170, 40));
            acceptBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Accept this request from " + req.requesterName + "?",
                    "Confirm Accept", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION) return;
                Database.resolveRequest(req.id);
                dialog.dispose();
                refreshList();
            });

            // Reject button
            final Color REJ_BASE  = new Color(210, 50, 50);
            final Color REJ_HOVER = new Color(180, 30, 30);
            JButton rejectBtn = new JButton("Reject Request") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg = getModel().isPressed() ? REJ_HOVER.darker()
                             : getModel().isRollover() ? REJ_HOVER : REJ_BASE;
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.setColor(new Color(255, 255, 255, 40));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight() / 2, 12, 12);
                    g2.dispose();
                    super.paintComponent(g);
                }
                @Override protected void paintBorder(Graphics g) {}
            };
            rejectBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            rejectBtn.setForeground(Color.WHITE);
            rejectBtn.setOpaque(false);
            rejectBtn.setContentAreaFilled(false);
            rejectBtn.setBorderPainted(false);
            rejectBtn.setFocusPainted(false);
            rejectBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            rejectBtn.setPreferredSize(new Dimension(170, 40));
            rejectBtn.addActionListener(e -> {
                dialog.dispose();
                showRejectDialog(req);
            });

            actionRow.add(acceptBtn);
            actionRow.add(rejectBtn);
            content.add(actionRow);
        }

        // Button row: Print (if accepted) + Close
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);

        if (req.status == CertRequest.RequestStatus.ACCEPTED) {
            final Color PRINT_BASE  = new Color(30, 100, 210);
            final Color PRINT_HOVER = new Color(20, 80, 180);
            JButton printBtn = new JButton("Print Certificate") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg = getModel().isPressed() ? PRINT_HOVER.darker()
                             : getModel().isRollover() ? PRINT_HOVER : PRINT_BASE;
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.setColor(new Color(255, 255, 255, 40));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight() / 2, 12, 12);
                    g2.dispose();
                    super.paintComponent(g);
                }
                @Override protected void paintBorder(Graphics g) {}
            };
            printBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            printBtn.setForeground(Color.WHITE);
            printBtn.setOpaque(false);
            printBtn.setContentAreaFilled(false);
            printBtn.setBorderPainted(false);
            printBtn.setFocusPainted(false);
            printBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            printBtn.setPreferredSize(new Dimension(190, 42));
            printBtn.addActionListener(e -> printCertificate(req));
            btnRow.add(printBtn);
        }

        JButton closeBtn = UITheme.secondaryButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeBtn.setPreferredSize(new Dimension(140, 42));
        closeBtn.addActionListener(e -> dialog.dispose());
        btnRow.add(closeBtn);

        content.add(Box.createVerticalStrut(20));
        content.add(btnRow);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(content, BorderLayout.NORTH);

        JScrollPane mainScroll = new JScrollPane(wrapper);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(10);
        dialog.add(mainScroll, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private JPanel detailRow(String key, String value) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JLabel keyLbl = new JLabel(key);
        keyLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        keyLbl.setForeground(UITheme.TEXT_MUTED);
        keyLbl.setPreferredSize(new Dimension(110, 20));
        keyLbl.setVerticalAlignment(SwingConstants.TOP);
        JLabel valLbl = new JLabel("<html><body style='width:440px'>"
            + (value != null ? value : "\u2014") + "</body></html>");
        valLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        valLbl.setForeground(UITheme.TEXT_DARK);
        row.add(keyLbl, BorderLayout.WEST);
        row.add(valLbl, BorderLayout.CENTER);
        return row;
    }

    private void showRejectDialog(CertRequest req) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this),
            "Reject Request", true);
        dialog.setSize(440, 280);
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        dialog.setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 24, 20, 24));
        content.setBackground(Color.WHITE);

        JLabel titleLbl = new JLabel("Reason for Rejection");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLbl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel infoLbl = new JLabel("Requester: " + req.requesterName + "  |  " + req.requestType);
        infoLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoLbl.setForeground(UITheme.TEXT_MUTED);
        infoLbl.setAlignmentX(LEFT_ALIGNMENT);

        JTextArea reasonArea = new JTextArea(4, 30);
        reasonArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UITheme.BORDER, 1),
            new EmptyBorder(6, 8, 6, 8)));
        JScrollPane reasonScroll = new JScrollPane(reasonArea);
        reasonScroll.setAlignmentX(LEFT_ALIGNMENT);
        reasonScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);

        JButton cancelBtn  = UITheme.secondaryButton("Cancel");
        JButton confirmBtn = UITheme.dangerButton("Confirm Reject");
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelBtn.setBorder(new EmptyBorder(8, 16, 8, 16));
        confirmBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        confirmBtn.setBorder(new EmptyBorder(8, 16, 8, 16));

        cancelBtn.addActionListener(e -> dialog.dispose());
        confirmBtn.addActionListener(e -> {
            String reason = reasonArea.getText().trim();
            if (reason.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "Please enter a reason for rejection.",
                    "Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Database.rejectRequest(req.id, reason);
            dialog.dispose();
            refreshList();
        });

        btnRow.add(cancelBtn);
        btnRow.add(confirmBtn);

        content.add(titleLbl);
        content.add(Box.createVerticalStrut(4));
        content.add(infoLbl);
        content.add(Box.createVerticalStrut(12));
        content.add(reasonScroll);
        content.add(Box.createVerticalStrut(14));
        content.add(btnRow);

        dialog.add(content, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void printCertificate(CertRequest req) {
        // Pre-print details dialog
        JDialog form = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this),
            "Certificate Details", true);
        form.setSize(520, 620);
        form.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        form.setLayout(new BorderLayout());

        JPanel formContent = new JPanel();
        formContent.setLayout(new BoxLayout(formContent, BoxLayout.Y_AXIS));
        formContent.setBorder(new EmptyBorder(20, 24, 20, 24));
        formContent.setBackground(Color.WHITE);

        JLabel formTitle = new JLabel("Fill in Certificate Details");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formTitle.setAlignmentX(LEFT_ALIGNMENT);
        formContent.add(formTitle);
        formContent.add(Box.createVerticalStrut(4));
        JLabel formSub = new JLabel("Pre-filled from account. Edit as needed before printing.");
        formSub.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        formSub.setForeground(UITheme.TEXT_MUTED);
        formSub.setAlignmentX(LEFT_ALIGNMENT);
        formContent.add(formSub);
        formContent.add(Box.createVerticalStrut(14));
        JSeparator fs = new JSeparator();
        fs.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        fs.setAlignmentX(LEFT_ALIGNMENT);
        formContent.add(fs);
        formContent.add(Box.createVerticalStrut(14));

        // Helper to make a labeled field row
        java.util.function.BiFunction<String, String, JTextField> addField = (label, def) -> {
            JLabel lbl = new JLabel(label);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setAlignmentX(LEFT_ALIGNMENT);
            JTextField fld = new JTextField(def != null ? def : "");
            fld.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            fld.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            fld.setAlignmentX(LEFT_ALIGNMENT);
            fld.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(4, 8, 4, 8)));
            formContent.add(lbl);
            formContent.add(Box.createVerticalStrut(3));
            formContent.add(fld);
            formContent.add(Box.createVerticalStrut(10));
            return fld;
        };

        // Section: Personal Details
        JLabel secPersonal = new JLabel("PERSONAL DETAILS");
        secPersonal.setFont(new Font("Segoe UI", Font.BOLD, 11));
        secPersonal.setForeground(new Color(30, 100, 210));
        secPersonal.setAlignmentX(LEFT_ALIGNMENT);
        formContent.add(secPersonal);
        formContent.add(Box.createVerticalStrut(8));

        // Address — auto-filled from account
        JLabel addrLbl = new JLabel("Address *");
        addrLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addrLbl.setAlignmentX(LEFT_ALIGNMENT);
        JTextArea fldAddressArea = new JTextArea(Database.getUserAddress(req.email));
        fldAddressArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        fldAddressArea.setLineWrap(true);
        fldAddressArea.setWrapStyleWord(true);
        fldAddressArea.setRows(2);
        fldAddressArea.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(4, 8, 4, 8)));
        JScrollPane addrScroll = new JScrollPane(fldAddressArea);
        addrScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        addrScroll.setAlignmentX(LEFT_ALIGNMENT);
        addrScroll.setBorder(null);
        formContent.add(addrLbl);
        formContent.add(Box.createVerticalStrut(3));
        formContent.add(addrScroll);
        formContent.add(Box.createVerticalStrut(10));

        JTextField fldCivilStat  = addField.apply("Civil Status *",        req.civilStatus);
        JTextField fldSex        = addField.apply("Sex *",                  req.sex);
        JTextField fldDOB        = addField.apply("Date of Birth *",        req.birthdate);
        JTextField fldPOB        = addField.apply("Place of Birth *",       req.birthplace);
        JTextField fldCitizen    = addField.apply("Citizenship *",          "Filipino");
        JTextField fldPurpose    = addField.apply("Purpose / Where to Use Certificate *", req.purpose);

        formContent.add(Box.createVerticalStrut(4));
        JSeparator fs2 = new JSeparator();
        fs2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        fs2.setAlignmentX(LEFT_ALIGNMENT);
        formContent.add(fs2);
        formContent.add(Box.createVerticalStrut(10));

        JLabel secOfficial = new JLabel("BARANGAY OFFICIALS");
        secOfficial.setFont(new Font("Segoe UI", Font.BOLD, 11));
        secOfficial.setForeground(new Color(30, 100, 210));
        secOfficial.setAlignmentX(LEFT_ALIGNMENT);
        formContent.add(secOfficial);
        formContent.add(Box.createVerticalStrut(8));

        // Pre-fill officials from saved admin fields if available
        String[] officials = req.barangayOfficials != null && !req.barangayOfficials.isEmpty()
            ? req.barangayOfficials.split("\n", 2) : new String[]{"", ""};
        JTextField fldCaptain    = addField.apply("Barangay Captain (Punong Barangay)",
            officials.length > 0 ? officials[0] : "");
        JTextField fldSecretary  = addField.apply("Prepared by (Secretary)",
            officials.length > 1 ? officials[1] : "");

        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setBackground(Color.WHITE);
        formWrapper.add(formContent, BorderLayout.NORTH);
        JScrollPane formScroll = new JScrollPane(formWrapper);
        formScroll.setBorder(null);
        formScroll.getVerticalScrollBar().setUnitIncrement(12);
        form.add(formScroll, BorderLayout.CENTER);

        // Buttons
        JPanel fBtnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        fBtnRow.setBackground(new Color(245, 247, 250));
        fBtnRow.setBorder(new MatteBorder(1, 0, 0, 0, UITheme.BORDER));

        final Color PB = new Color(30, 100, 210), PH = new Color(20, 80, 180);
        JButton printNowBtn = new JButton("Print Certificate") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed() ? PH.darker() : getModel().isRollover() ? PH : PB;
                g2.setColor(bg); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(255,255,255,40)); g2.fillRoundRect(0,0,getWidth(),getHeight()/2,12,12);
                g2.dispose(); super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        printNowBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        printNowBtn.setForeground(Color.WHITE);
        printNowBtn.setOpaque(false); printNowBtn.setContentAreaFilled(false);
        printNowBtn.setBorderPainted(false); printNowBtn.setFocusPainted(false);
        printNowBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        printNowBtn.setPreferredSize(new Dimension(190, 38));

        JButton cancelBtn = UITheme.secondaryButton("Cancel");
        cancelBtn.setPreferredSize(new Dimension(100, 38));
        cancelBtn.addActionListener(e -> form.dispose());

        // Helper to collect and validate fields
        java.util.function.Supplier<String[]> collectFields = () -> {
            String address2   = fldAddressArea.getText().trim();
            String civil2     = fldCivilStat.getText().trim();
            String sex2       = fldSex.getText().trim();
            String dob2       = fldDOB.getText().trim();
            String pob2       = fldPOB.getText().trim();
            String citizen2   = fldCitizen.getText().trim();
            String purpose2   = fldPurpose.getText().trim();
            String captain2   = fldCaptain.getText().trim();
            String secretary2 = fldSecretary.getText().trim();
            java.util.List<String> missing2 = new java.util.ArrayList<>();
            if (address2.isEmpty())   missing2.add("Address");
            if (civil2.isEmpty())     missing2.add("Civil Status");
            if (sex2.isEmpty())       missing2.add("Sex");
            if (dob2.isEmpty())       missing2.add("Date of Birth");
            if (pob2.isEmpty())       missing2.add("Place of Birth");
            if (citizen2.isEmpty())   missing2.add("Citizenship");
            if (purpose2.isEmpty())   missing2.add("Purpose / Where to Use");
            if (captain2.isEmpty())   missing2.add("Barangay Captain");
            if (secretary2.isEmpty()) missing2.add("Secretary");
            if (!missing2.isEmpty()) {
                JOptionPane.showMessageDialog(form,
                    "Please fill in the following required fields:\n• "
                    + String.join("\n• ", missing2),
                    "Required Fields", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            return new String[]{address2, civil2, sex2, dob2, pob2, citizen2, purpose2, captain2, secretary2};
        };

        // Preview button
        JButton previewBtn = new JButton("Preview");
        previewBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        previewBtn.setPreferredSize(new Dimension(120, 38));
        previewBtn.addActionListener(e -> {
            String[] vals = collectFields.get();
            if (vals == null) return;
            showPreview(form, req, vals[0], vals[1], vals[2], vals[3], vals[4], vals[5], vals[6], vals[7], vals[8]);
        });
        fBtnRow.add(previewBtn);

        printNowBtn.addActionListener(e -> {
            String[] vals = collectFields.get();
            if (vals == null) return;
            form.dispose();
            doPrint(req, vals[0], vals[1], vals[2], vals[3], vals[4], vals[5], vals[6], vals[7], vals[8]);
        });

        fBtnRow.add(printNowBtn);
        fBtnRow.add(cancelBtn);
        form.add(fBtnRow, BorderLayout.SOUTH);
        form.setVisible(true);
    }

    private String getMonthName(String mm) {
        String[] months = {"January","February","March","April","May","June",
                           "July","August","September","October","November","December"};
        try { int m = Integer.parseInt(mm.trim()); if (m >= 1 && m <= 12) return months[m-1]; }
        catch (Exception e) {}
        return mm;
    }

    /** Draws wrapped text and returns the new Y position after the text block */
    private int drawWrapped2(Graphics2D g, String text, int x, int y, int maxWidth) {
        FontMetrics fm = g.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int lineY = y + fm.getAscent();
        for (String word : words) {
            String test = line.length() > 0 ? line + " " + word : word;
            if (fm.stringWidth(test) > maxWidth && line.length() > 0) {
                g.drawString(line.toString(), x, lineY);
                lineY += fm.getHeight() + 1;
                line = new StringBuilder(word);
            } else {
                if (line.length() > 0) line.append(" ");
                line.append(word);
            }
        }
        if (line.length() > 0) g.drawString(line.toString(), x, lineY);
        return lineY + fm.getDescent();
    }


    private void showPreview(JDialog parent, CertRequest req,
            String address, String civil, String sex, String dob,
            String pob, String citizen, String purpose, String captain, String secretary) {

        JDialog preview = new JDialog(parent, "Certificate Preview", true);
        preview.setSize(700, 780);
        preview.setLocationRelativeTo(parent);
        preview.setLayout(new BorderLayout());

        // Scale factor — 1.0 = fit to window width (A4 ratio)
        final float[] scale = {1.0f};

        // The certificate canvas
        final int CERT_W = 640;  // logical certificate width in pixels
        final int CERT_H = 820;  // logical certificate height

        JPanel canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                int sw = Math.round(CERT_W * scale[0]);
                int sh = Math.round(CERT_H * scale[0]);
                int ox = Math.max(0, (getWidth()  - sw) / 2);
                int oy = 20;

                // Drop shadow
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRect(ox + 4, oy + 4, sw, sh);

                // Paper background
                g2.setColor(Color.WHITE);
                g2.fillRect(ox, oy, sw, sh);

                // Scale everything to the certificate coordinate space
                g2.translate(ox, oy);
                g2.scale(scale[0], scale[0]);

                drawCertificate(g2, req, address, civil, sex, dob, pob, citizen, purpose, captain, secretary, CERT_W, CERT_H);
                g2.dispose();
            }

            @Override public Dimension getPreferredSize() {
                return new Dimension(
                    Math.round(CERT_W * scale[0]) + 40,
                    Math.round(CERT_H * scale[0]) + 60);
            }
        };
        canvas.setBackground(new Color(180, 185, 195));

        JScrollPane scroll = new JScrollPane(canvas);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(new Color(180, 185, 195));
        preview.add(scroll, BorderLayout.CENTER);

        // ── Toolbar: zoom + buttons ──
        JPanel toolbar = new JPanel(new BorderLayout(0, 0));
        toolbar.setBackground(new Color(50, 50, 55));
        toolbar.setBorder(new EmptyBorder(8, 16, 8, 16));

        // Zoom controls (left)
        JPanel zoomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        zoomPanel.setOpaque(false);

        JLabel zoomLbl = new JLabel("Zoom:");
        zoomLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        zoomLbl.setForeground(Color.WHITE);

        JButton zoomOut = new JButton("−");
        JButton zoomIn  = new JButton("+");
        JLabel  zoomPct = new JLabel("100%");
        zoomPct.setFont(new Font("Segoe UI", Font.BOLD, 12));
        zoomPct.setForeground(Color.WHITE);
        zoomPct.setPreferredSize(new Dimension(44, 24));
        zoomPct.setHorizontalAlignment(SwingConstants.CENTER);

        for (JButton zb : new JButton[]{zoomOut, zoomIn}) {
            zb.setFont(new Font("Segoe UI", Font.BOLD, 14));
            zb.setPreferredSize(new Dimension(32, 28));
            zb.setFocusPainted(false);
            zb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        zoomOut.addActionListener(e2 -> {
            if (scale[0] > 0.5f) {
                scale[0] = Math.round((scale[0] - 0.1f) * 10) / 10.0f;
                zoomPct.setText(Math.round(scale[0] * 100) + "%");
                canvas.revalidate(); canvas.repaint();
            }
        });
        zoomIn.addActionListener(e2 -> {
            if (scale[0] < 2.0f) {
                scale[0] = Math.round((scale[0] + 0.1f) * 10) / 10.0f;
                zoomPct.setText(Math.round(scale[0] * 100) + "%");
                canvas.revalidate(); canvas.repaint();
            }
        });

        zoomPanel.add(zoomLbl);
        zoomPanel.add(zoomOut);
        zoomPanel.add(zoomPct);
        zoomPanel.add(zoomIn);
        toolbar.add(zoomPanel, BorderLayout.WEST);

        // Action buttons (right)
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        // Print button
        final Color PB = new Color(30, 100, 210), PH = new Color(20, 80, 180);
        JButton printFromPreview = new JButton("Print") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed() ? PH.darker() : getModel().isRollover() ? PH : PB;
                g2.setColor(bg); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose(); super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        printFromPreview.setFont(new Font("Segoe UI", Font.BOLD, 13));
        printFromPreview.setForeground(Color.WHITE);
        printFromPreview.setOpaque(false); printFromPreview.setContentAreaFilled(false);
        printFromPreview.setBorderPainted(false); printFromPreview.setFocusPainted(false);
        printFromPreview.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        printFromPreview.setPreferredSize(new Dimension(110, 34));

        printFromPreview.addActionListener(e2 -> {
            preview.dispose();
            doPrint(req, address, civil, sex, dob, pob, citizen, purpose, captain, secretary);
        });

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        closeBtn.setForeground(new Color(220, 220, 220));
        closeBtn.setOpaque(false); closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false); closeBtn.setFocusPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.setPreferredSize(new Dimension(80, 34));
        closeBtn.addActionListener(e2 -> preview.dispose());

        btnPanel.add(printFromPreview);
        btnPanel.add(closeBtn);
        toolbar.add(btnPanel, BorderLayout.EAST);

        preview.add(toolbar, BorderLayout.SOUTH);
        preview.setVisible(true);
    }

    /** Draws the full certificate onto any Graphics2D in a 0,0 -> certW,certH coordinate space */
    private void drawCertificate(Graphics2D g2, CertRequest req,
            String address, String civil, String sex, String dob,
            String pob, String citizen, String purpose, String captain, String secretary,
            int certW, int certH) {

        Color DARK_BLUE  = new Color(0,  50, 130);
        Color MED_BLUE   = new Color(10, 80, 170);
        Color LIGHT_BLUE = new Color(220, 230, 248);
        Color GOLD       = new Color(180, 140, 30);
        int   mx         = 40;    // left/right margin
        int   labelW     = 92;    // fixed label column — values stay aligned
        int   lineH      = 18;    // row height for personal details
        int   y;

        // Decorative outer frame
        int corner = 16;
        g2.setColor(DARK_BLUE);
        int[][] corners = {{4,4},{certW-4,4},{4,certH-4},{certW-4,certH-4}};
        for (int[] c : corners) g2.fillRect(c[0]-corner/2, c[1]-corner/2, corner, corner);

        g2.setStroke(new BasicStroke(3.5f));
        g2.drawRect(4, 4, certW-8, certH-8);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRect(12, 12, certW-24, certH-24);

        g2.setColor(GOLD);
        g2.setStroke(new BasicStroke(0.8f));
        g2.drawRect(17, 17, certW-34, certH-34);
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(DARK_BLUE);

        // Watermark logo centered in body 
        try {
            java.net.URL logoUrl = getClass().getResource("/barangay/brgyLogo.png");
            if (logoUrl != null) {
                java.awt.image.BufferedImage logo = javax.imageio.ImageIO.read(logoUrl);
                int logoSize = 500;
                int logoX = (certW - logoSize) / 2;
                int logoY = (certH - logoSize) / 2 + 40; // slightly below center
                // Draw at low opacity using AlphaComposite
                java.awt.Composite orig = g2.getComposite();
                g2.setComposite(java.awt.AlphaComposite.getInstance(
                    java.awt.AlphaComposite.SRC_OVER, 0.08f));
                g2.drawImage(logo, logoX, logoY, logoSize, logoSize, null);
                g2.setComposite(orig);
            }
        } catch (Exception ex) { /* skip if logo unavailable */ }

        // Header band
        g2.setColor(LIGHT_BLUE);
        g2.fillRect(17, 17, certW-34, 96);
        g2.setColor(DARK_BLUE);
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(17, 113, certW-17, 113);
        g2.setStroke(new BasicStroke(1f));

        y = 42;
        g2.setColor(new Color(60, 60, 80));
        g2.setFont(new Font("Serif", Font.PLAIN, 8));
        drawCentered(g2, "Republic of the Philippines", certW, y); y += 12;
        drawCentered(g2, "Municipality of Cabanatuan City", certW, y); y += 15;

        g2.setFont(new Font("Serif", Font.BOLD, 22));
        g2.setColor(DARK_BLUE);
        drawCentered(g2, "Barangay Bagong Sikat", certW, y); y += 22;

        g2.setFont(new Font("Serif", Font.ITALIC, 8));
        g2.setColor(new Color(80, 80, 100));
        drawCentered(g2, "OFFICE OF THE PUNONG BARANGAY", certW, y);

        // Gold rule
        y = 120;
        g2.setColor(GOLD);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(mx, y, certW - mx, y);
        g2.setStroke(new BasicStroke(1f));
        y += 26;

        // Certificate title
        g2.setFont(new Font("Serif", Font.BOLD, 18));
        g2.setColor(DARK_BLUE);
        drawCentered(g2, req.requestType.toUpperCase(), certW, y);
        FontMetrics fmT = g2.getFontMetrics();
        int titleW = fmT.stringWidth(req.requestType.toUpperCase());
        int titleX = (certW - titleW) / 2;
        y += 5;
        g2.setColor(GOLD);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(titleX, y, titleX + titleW, y);
        g2.setStroke(new BasicStroke(1f));
        y += 14;

        // Motto
        g2.setFont(new Font("Serif", Font.BOLD + Font.ITALIC, 9));
        g2.setColor(MED_BLUE);
        drawCentered(g2, "\u201cBAGONG SIKAT, BAGONG PAG-ASA\u201d", certW, y); y += 10;

        // Date right-aligned
        g2.setFont(new Font("Serif", Font.PLAIN, 8));
        g2.setColor(new Color(80, 80, 80));
        String dateLine = req.dateSubmitted != null ? req.dateSubmitted : "";
        FontMetrics fmD = g2.getFontMetrics();
        g2.drawString(dateLine, certW - mx - fmD.stringWidth(dateLine), y); y += 22;

        // Body text
        g2.setFont(new Font("Serif", Font.BOLD, 10));
        g2.setColor(Color.BLACK);
        drawCentered(g2, "TO WHOM IT MAY CONCERN:", certW, y); y += 18;

        g2.setFont(new Font("Serif", Font.PLAIN, 10));
        String body1 = "        THIS IS TO CERTIFY that " + req.requesterName.toUpperCase()
            + " is a resident of BARANGAY BAGONG SIKAT, Cabanatuan City, Nueva Ecija, for years, "
            + "with no criminal or any derogatory record of whatever nature as per the records "
            + "of this Barangay is concern.";
        y = drawWrapped2(g2, body1, mx, y, certW - mx * 2) + 14;

        String body2 = "        This certification is issued upon the request of the above-named person for "
            + (purpose.isEmpty() ? "whatever legal purpose that it may served" : purpose) + ".";
        y = drawWrapped2(g2, body2, mx, y, certW - mx * 2) + 14;

        String[] dp = req.dateSubmitted != null ? req.dateSubmitted.split("-") : new String[]{"","",""};
        String issuedDate = dp.length >= 3
            ? "Issued this " + dp[2].split(" ")[0] + " day of " + getMonthName(dp[1]) + " " + dp[0]
            : "Issued this day";
        y = drawWrapped2(g2, "        " + issuedDate
            + ", at the Office of the Punong Barangay of Barangay Bagong Sikat,"
            + " Cabanatuan City, Nueva Ecija, Republic of the Philippines.",
            mx, y, certW - mx * 2) + 22;

        // Personal Details section
        g2.setColor(LIGHT_BLUE);
        g2.fillRect(mx, y - 2, certW - mx * 2, 20);
        g2.setColor(DARK_BLUE);
        g2.setStroke(new BasicStroke(0.8f));
        g2.drawRect(mx, y - 2, certW - mx * 2, 20);
        g2.setStroke(new BasicStroke(1f));
        g2.setFont(new Font("Serif", Font.BOLD, 10));
        drawCentered(g2, "PERSONAL DETAILS", certW, y + 13);
        y += 28;

        int valX  = mx + labelW;
        int col2x = certW / 2 + 10;
        int val2X = col2x + labelW;

        // Name — full width
        g2.setFont(new Font("Serif", Font.BOLD, 9));
        g2.setColor(new Color(40, 40, 40));
        g2.drawString("Name:", mx, y);
        g2.setFont(new Font("Serif", Font.PLAIN, 9));
        g2.setColor(Color.BLACK);
        g2.drawString(req.requesterName, valX, y); y += lineH;

        // Address — full width wrapped
        g2.setFont(new Font("Serif", Font.BOLD, 9));
        g2.setColor(new Color(40, 40, 40));
        g2.drawString("Address:", mx, y);
        g2.setFont(new Font("Serif", Font.PLAIN, 9));
        g2.setColor(Color.BLACK);
        int addrBottom = drawWrapped2(g2, address, valX, y - g2.getFontMetrics().getAscent(), certW - valX - mx);
        y = Math.max(y + lineH, addrBottom + 4);

        // Thin separator
        g2.setColor(new Color(200, 210, 230));
        g2.setStroke(new BasicStroke(0.5f));
        g2.drawLine(mx, y - 2, certW - mx, y - 2);
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(Color.BLACK);

        // Two-column rows
        String[][] rows = {
            {"Civil Status:", civil,    "Place of Birth:",  pob},
            {"Sex:",          sex,      "Citizenship:",     citizen.isEmpty() ? "Filipino" : citizen},
            {"Date of Birth:", dob,     "Purpose:",         purpose},
        };
        for (String[] row : rows) {
            g2.setFont(new Font("Serif", Font.BOLD, 9));
            g2.setColor(new Color(40, 40, 40));
            g2.drawString(row[0], mx, y);
            g2.setFont(new Font("Serif", Font.PLAIN, 9));
            g2.setColor(Color.BLACK);
            g2.drawString(row[1], valX, y);
            g2.setFont(new Font("Serif", Font.BOLD, 9));
            g2.setColor(new Color(40, 40, 40));
            g2.drawString(row[2], col2x, y);
            g2.setFont(new Font("Serif", Font.PLAIN, 9));
            g2.setColor(Color.BLACK);
            if (row[2].equals("Purpose:")) {
                drawWrapped2(g2, row[3], val2X, y - g2.getFontMetrics().getAscent(), certW - val2X - mx);
            } else {
                g2.drawString(row[3], val2X, y);
            }
            y += lineH;
        }
        y += 20;

        // Signature block
        g2.setColor(GOLD);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(mx, y, certW - mx, y); y += 6;
        g2.setColor(LIGHT_BLUE);
        g2.fillRect(mx, y, certW - mx * 2, 12);
        g2.setColor(DARK_BLUE);
        g2.setStroke(new BasicStroke(0.8f));
        g2.drawLine(mx, y + 12, certW - mx, y + 12);
        g2.setStroke(new BasicStroke(1f));
        y += 28;

        int sigLeft  = mx + 20;
        int sigRight = certW / 2 + 40;

        g2.setFont(new Font("Serif", Font.PLAIN, 9));
        g2.setColor(new Color(80, 80, 80));
        g2.drawString("Prepared by:", sigLeft, y);
        g2.drawString("Approved by:", sigRight, y); y += 44;

        g2.setColor(DARK_BLUE);
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(sigLeft, y, sigLeft + 160, y);
        g2.drawLine(sigRight, y, sigRight + 160, y); y += 13;

        g2.setFont(new Font("Serif", Font.BOLD, 10));
        g2.setColor(DARK_BLUE);
        g2.drawString(secretary.isEmpty() ? "___________________" : secretary.toUpperCase(), sigLeft, y);
        g2.drawString(captain.isEmpty()   ? "___________________" : captain.toUpperCase(), sigRight, y); y += 13;

        g2.setFont(new Font("Serif", Font.PLAIN, 9));
        g2.setColor(new Color(60, 60, 60));
        g2.drawString("Barangay Secretary", sigLeft, y);
        g2.drawString("Punong Barangay", sigRight, y);

        // Footer
        g2.setColor(LIGHT_BLUE);
        g2.fillRect(17, certH - 38, certW - 34, 20);
        g2.setColor(DARK_BLUE);
        g2.setStroke(new BasicStroke(0.8f));
        g2.drawRect(17, certH - 38, certW - 34, 20);
        g2.setStroke(new BasicStroke(1f));

        g2.setFont(new Font("Serif", Font.ITALIC, 7));
        g2.setColor(new Color(60, 60, 100));
        drawCentered(g2, "Note: valid only for 6 months from the date issued. Not valid without dry seal.", certW, certH - 23);
        g2.setFont(new Font("Serif", Font.PLAIN, 7));
        g2.setColor(new Color(100, 100, 120));
    }


    /** Executes the actual print job */
    private void doPrint(CertRequest req,
            String address, String civil, String sex, String dob,
            String pob, String citizen, String purpose, String captain, String secretary) {

        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Certificate - " + req.requestType);
        PageFormat pf = job.defaultPage();
        pf.setOrientation(PageFormat.PORTRAIT);

        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) graphics;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            int pw = (int) pageFormat.getImageableWidth();
            int ph = (int) pageFormat.getImageableHeight();

            // Scale certificate to fit the printable area
            double sx = pw / 640.0, sy = ph / 820.0;
            double s  = Math.min(sx, sy);
            g2.scale(s, s);

            drawCertificate(g2, req, address, civil, sex, dob, pob, citizen, purpose,
                            captain, secretary, 640, 820);
            return Printable.PAGE_EXISTS;
        }, pf);

        if (job.printDialog()) {
            try { job.print(); }
            catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Print failed: " + ex.getMessage(),
                    "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void drawCentered(Graphics2D g, String text, int width, int y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, (width - fm.stringWidth(text)) / 2, y);
    }

    private void drawWrapped(Graphics2D g, String text, int x, int y, int maxWidth) {
        FontMetrics fm = g.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int lineY = y;
        for (String word : words) {
            String test = line + (line.length() > 0 ? " " : "") + word;
            if (fm.stringWidth(test) > maxWidth && line.length() > 0) {
                g.drawString(line.toString(), x, lineY);
                lineY += fm.getHeight() + 2;
                line = new StringBuilder(word);
            } else {
                if (line.length() > 0) line.append(" ");
                line.append(word);
            }
        }
        if (line.length() > 0) g.drawString(line.toString(), x, lineY);
    }

    private JLabel statusBadge(CertRequest.RequestStatus status) {
        String text; Color bg, fg;
        switch (status) {
            case ACCEPTED: text = "Accepted"; bg = new Color(180,240,200); fg = new Color(0,100,40);   break;
            case REJECTED: text = "Rejected"; bg = new Color(255,200,200); fg = new Color(150,0,0);    break;
            default:       text = "Pending";  bg = new Color(255,235,170); fg = new Color(120,70,0);   break;
        }
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setOpaque(true);
        lbl.setBackground(bg);
        lbl.setForeground(fg);
        lbl.setBorder(new EmptyBorder(3, 10, 3, 10));
        return lbl;
    }

    private JPanel bigStatCard(String label, JLabel valueLbl, Color color) {
        JPanel card = new JPanel(new BorderLayout(8, 4));
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(14, 16, 14, 16)));
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 30));
        valueLbl.setForeground(UITheme.TEXT_DARK);
        JLabel nameLbl = new JLabel(label);
        nameLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nameLbl.setForeground(UITheme.TEXT_MUTED);
        JLabel dot = new JLabel("\u25CF");
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        dot.setForeground(color);
        dot.setHorizontalAlignment(SwingConstants.RIGHT);
        JPanel txt = new JPanel(new GridLayout(2, 1, 0, 2));
        txt.setOpaque(false);
        txt.add(valueLbl); txt.add(nameLbl);
        card.add(txt, BorderLayout.CENTER);
        card.add(dot, BorderLayout.EAST);
        return card;
    }
}