package barangay;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.print.*;
import java.util.List;

public class AdminRequestHistoryPanel extends JPanel {

    private final AppController ctrl;
    private String currentSort   = "date_desc";
    private String currentFilter = "ACCEPTED";

    // Persistent references — refreshed without rebuilding layout
    private JLabel countLbl;
    private JPanel listPanel;
    private JComboBox<String> filterBox;
    private JComboBox<String> sortBox;

    public AdminRequestHistoryPanel(AppController ctrl) {
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
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setBackground(UITheme.BG_MAIN);
        body.setBorder(new EmptyBorder(16, 24, 20, 24));

        // Top section: back button + controls
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);

        // Back button
        JButton backBtn = UITheme.backButton("Back to Dashboard");
        backBtn.setAlignmentX(LEFT_ALIGNMENT);
        backBtn.addActionListener(e -> ctrl.showAdminDashboard());
        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        backRow.setOpaque(false);
        backRow.setAlignmentX(LEFT_ALIGNMENT);
        backRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        backRow.add(backBtn);

        // Controls row: filter + sort
        JPanel controlRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        controlRow.setOpaque(false);
        controlRow.setAlignmentX(LEFT_ALIGNMENT);
        controlRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel filterLbl = new JLabel("Filter:");
        filterLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        String[] filterOptions = {"All", "Pending", "Accepted", "Rejected"};
        filterBox = new JComboBox<>(filterOptions);
        filterBox.setSelectedIndex(2);
        filterBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
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

        controlRow.add(filterLbl);
        controlRow.add(filterBox);
        controlRow.add(Box.createHorizontalStrut(16));
        controlRow.add(sortLbl);
        controlRow.add(sortBox);

        // Request list card
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));
        card.setAlignmentX(LEFT_ALIGNMENT);

        countLbl = new JLabel("Showing 0 request(s)");
        countLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        card.add(countLbl, BorderLayout.NORTH);

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

        // Back-to-top floating button
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

        topSection.add(backRow);
        topSection.add(Box.createVerticalStrut(10));
        topSection.add(controlRow);
        topSection.add(Box.createVerticalStrut(12));

        body.add(topSection, BorderLayout.NORTH);
        body.add(card,       BorderLayout.CENTER);

        // Initial data load
        refreshList();

        return body;
    }

    // Refreshes only the list
    private void refreshList() {
        List<CertRequest> all = Database.getAllRequestsSorted(currentSort);
        if (!currentFilter.equals("ALL")) {
            all.removeIf(r -> !r.status.name().equals(currentFilter));
        }

        countLbl.setText("Showing " + all.size() + " request(s)");

        listPanel.removeAll();
        if (all.isEmpty()) {
            JLabel empty = new JLabel("No requests found.");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            empty.setForeground(UITheme.TEXT_MUTED);
            empty.setAlignmentX(LEFT_ALIGNMENT);
            listPanel.add(Box.createVerticalStrut(10));
            listPanel.add(empty);
        } else {
            for (CertRequest req : all) {
                listPanel.add(buildRow(req));
                listPanel.add(Box.createVerticalStrut(8));
            }
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel buildRow(CertRequest req) {
        JPanel row = new JPanel(new BorderLayout(10, 4));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(12, 16, 12, 16)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        row.setPreferredSize(new Dimension(0, 100));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JPanel textPanel = new JPanel(new GridLayout(0, 1, 0, 3));
        textPanel.setOpaque(false);

        JLabel nameLbl = new JLabel(req.requesterName);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel typeLbl = new JLabel(req.requestType);
        typeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        typeLbl.setForeground(UITheme.TEXT_MUTED);

        JLabel timeLbl = new JLabel(req.dateSubmitted);
        timeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLbl.setForeground(UITheme.TEXT_MUTED);

        textPanel.add(nameLbl);
        textPanel.add(typeLbl);
        textPanel.add(timeLbl);

        if (req.status == CertRequest.RequestStatus.REJECTED
                && req.rejectReason != null && !req.rejectReason.isEmpty()) {
            JLabel reasonLbl = new JLabel("Reason: " + req.rejectReason);
            reasonLbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            reasonLbl.setForeground(new Color(180, 30, 30));
            textPanel.add(reasonLbl);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
            row.setPreferredSize(new Dimension(0, 120));
        }

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(statusBadge(req.status));

        if (req.status == CertRequest.RequestStatus.ACCEPTED) {
            JButton printBtn = UITheme.primaryButton("Print");
            printBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            printBtn.setBorder(new EmptyBorder(5, 12, 5, 12));
            printBtn.addActionListener(e -> printCertificate(req));
            rightPanel.add(printBtn);
        }

        row.add(textPanel,  BorderLayout.CENTER);
        row.add(rightPanel, BorderLayout.EAST);
        return row;
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
                + " filed in this barangay.", 60, y, w - 120); y += 60;
            drawWrapped(g2, "This certification is issued upon the request of the above-named person for: "
                + req.description, 60, y, w - 120); y += 60;
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
            case ACCEPTED: text = "Accepted"; bg = new Color(180,240,200); fg = new Color(0,100,40);  break;
            case REJECTED: text = "Rejected"; bg = new Color(255,200,200); fg = new Color(150,0,0);   break;
            default:       text = "Pending";  bg = new Color(255,235,170); fg = new Color(120,70,0);  break;
        }
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setOpaque(true);
        lbl.setBackground(bg);
        lbl.setForeground(fg);
        lbl.setBorder(new EmptyBorder(4, 12, 4, 12));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        return lbl;
    }
}