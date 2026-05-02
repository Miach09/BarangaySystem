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

    public AdminDashboardPanel(AppController ctrl) {
        this.ctrl = ctrl;
        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
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
            int r = JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(this), "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) ctrl.logout();
        });
        right.add(settings);
        right.add(logout);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildBody() {
        // BorderLayout so the card fills remaining height and scroll works
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
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Certificate - " + req.requestType);
        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) graphics;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            int w = (int) pageFormat.getImageableWidth();
            int y = 0;
            g2.setColor(new Color(30, 100, 210));
            g2.setStroke(new BasicStroke(3));
            g2.drawRect(10, 10, w - 20, (int) pageFormat.getImageableHeight() - 20);
            g2.setStroke(new BasicStroke(1));
            g2.drawRect(16, 16, w - 32, (int) pageFormat.getImageableHeight() - 32);
            y = 60;
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Serif", Font.BOLD, 14));
            drawCentered(g2, "Republic of the Philippines", w, y); y += 20;
            drawCentered(g2, "City/Municipality of Cabanatuan", w, y); y += 20;
            drawCentered(g2, "BARANGAY BAGONG SIKAT", w, y); y += 18;
            g2.setFont(new Font("Serif", Font.PLAIN, 11));
            drawCentered(g2, "Barangay Hall, Bagong Sikat, Cabanatuan City", w, y); y += 30;
            g2.setColor(new Color(30, 100, 210));
            g2.setFont(new Font("Serif", Font.BOLD, 18));
            drawCentered(g2, req.requestType.toUpperCase(), w, y); y += 40;
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Serif", Font.PLAIN, 12));
            drawCentered(g2, "TO WHOM IT MAY CONCERN:", w, y); y += 30;
            drawWrapped(g2, "This is to certify that " + req.requesterName.toUpperCase()
                + " is a bonafide resident of Barangay Bagong Sikat and is known"
                + " to be of good moral character and has not been involved in any criminal case"
                + " filed in this barangay.", 60, y, w - 120); y += 40;
            drawWrapped(g2, "This certification is issued upon the request of the above-named person for: "
                + req.description, 60, y, w - 120); y += 50;
            drawCentered(g2, "Issued this " + req.dateSubmitted + " at Barangay Bagong Sikat.", w, y); y += 60;
            int sigX = w / 2 + 20;
            g2.setFont(new Font("Serif", Font.BOLD, 12));
            g2.drawString("BARANGAY CAPTAIN", sigX, y); y += 16;
            g2.setFont(new Font("Serif", Font.PLAIN, 11));
            g2.drawString("Barangay Bagong Sikat", sigX, y);
            g2.setFont(new Font("Serif", Font.ITALIC, 10));
            g2.setColor(Color.GRAY);
            g2.drawString("Request ID: #" + req.id, 60, (int) pageFormat.getImageableHeight() - 30);
            return Printable.PAGE_EXISTS;
        });
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