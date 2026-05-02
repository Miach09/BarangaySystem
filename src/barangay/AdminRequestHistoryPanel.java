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

    // Persistent references
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
            int r = JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(this),
                    "Are you sure you want to logout?", 
                    "Confirm Logout", JOptionPane.YES_NO_OPTION);
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
        // Address
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

        printNowBtn.addActionListener(e -> {
            // Collect and validate all required fields
            final String address   = fldAddressArea.getText().trim();
            final String civil     = fldCivilStat.getText().trim();
            final String sex       = fldSex.getText().trim();
            final String dob       = fldDOB.getText().trim();
            final String pob       = fldPOB.getText().trim();
            final String citizen   = fldCitizen.getText().trim();
            final String purpose   = fldPurpose.getText().trim();
            final String captain   = fldCaptain.getText().trim();
            final String secretary = fldSecretary.getText().trim();

            // Validation — all fields required
            java.util.List<String> missing = new java.util.ArrayList<>();
            if (address.isEmpty())   missing.add("Address");
            if (civil.isEmpty())     missing.add("Civil Status");
            if (sex.isEmpty())       missing.add("Sex");
            if (dob.isEmpty())       missing.add("Date of Birth");
            if (pob.isEmpty())       missing.add("Place of Birth");
            if (citizen.isEmpty())   missing.add("Citizenship");
            if (purpose.isEmpty())   missing.add("Purpose / Where to Use");
            if (captain.isEmpty())   missing.add("Barangay Captain");
            if (secretary.isEmpty()) missing.add("Secretary");

            if (!missing.isEmpty()) {
                JOptionPane.showMessageDialog(form,
                    "Please fill in the following required fields:• "
                    + String.join("• ", missing),
                    "Required Fields", JOptionPane.WARNING_MESSAGE);
                return;
            }

            form.dispose();

            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName("Certificate - " + req.requestType);

            // Portrait page setup
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
                int mx = 30;
                int y  = 0;

                // Outer double border
                g2.setColor(new Color(0, 60, 150));
                g2.setStroke(new BasicStroke(3f));
                g2.drawRect(6, 6, pw - 12, ph - 12);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRect(12, 12, pw - 24, ph - 24);
                g2.setColor(Color.BLACK);

                // Header
                y = 42;
                g2.setFont(new Font("Serif", Font.PLAIN, 9));
                drawCentered(g2, "Republic of the Philippines", pw, y); y += 13;
                drawCentered(g2, "Municipality of Cabanatuan", pw, y); y += 13;

                g2.setFont(new Font("Serif", Font.BOLD, 18));
                g2.setColor(new Color(0, 60, 150));
                drawCentered(g2, "Barangay Bagong Sikat", pw, y); y += 22;

                g2.setFont(new Font("Serif", Font.ITALIC, 9));
                g2.setColor(Color.DARK_GRAY);
                drawCentered(g2, "OFFICE OF THE PUNONG BARANGAY", pw, y); y += 16;

                // Thin rule
                g2.setColor(new Color(0, 60, 150));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine(mx, y, pw - mx, y); y += 14;
                g2.setStroke(new BasicStroke(1f));
                g2.setColor(Color.BLACK);

                // Certificate type title
                g2.setFont(new Font("Serif", Font.BOLD, 20));
                drawCentered(g2, req.requestType, pw, y); y += 24;

                // Motto line
                g2.setFont(new Font("Serif", Font.BOLD + Font.ITALIC, 10));
                g2.setColor(new Color(0, 60, 150));
                drawCentered(g2, "\"BAGONG SIKAT, BAGONG PAG-ASA\"", pw, y); y += 10;

                // Date line right-aligned
                g2.setFont(new Font("Serif", Font.PLAIN, 9));
                g2.setColor(Color.BLACK);
                String dateLine = req.dateSubmitted;
                FontMetrics fmDate = g2.getFontMetrics();
                g2.drawString(dateLine, pw - mx - fmDate.stringWidth(dateLine), y); y += 20;

                // Body text
                g2.setFont(new Font("Serif", Font.BOLD, 10));
                drawCentered(g2, "TO WHOM IT MAY CONCERN:", pw, y); y += 18;

                g2.setFont(new Font("Serif", Font.PLAIN, 10));
                String bodyText = "    THIS IS TO CERTIFY that " + req.requesterName.toUpperCase()
                    + " is a resident of BARANGAY BAGONG SIKAT, Cabanatuan City, for"
                    + " years, with no criminal or any derogatory record of whatever nature as"
                    + " per the records of this Barangay is concern.";
                y = drawWrapped2(g2, bodyText, mx + 10, y, pw - (mx + 10) * 2) + 12;

                g2.setFont(new Font("Serif", Font.PLAIN, 10));
                String body2 = "    This certification is issued upon the request of the above-named person for "
                    + (purpose.isEmpty() ? "whatever legal purpose that it may served" : purpose) + ".";
                y = drawWrapped2(g2, body2, mx + 10, y, pw - (mx + 10) * 2) + 12;

                // Issued line
                String[] dateParts = req.dateSubmitted.split("-");
                String issuedDate = dateParts.length >= 3
                    ? "Issued this " + dateParts[2].split(" ")[0] + " day of " + getMonthName(dateParts[1]) + " " + dateParts[0]
                    : "Issued this day of " + req.dateSubmitted;
                g2.setFont(new Font("Serif", Font.PLAIN, 10));
                y = drawWrapped2(g2, "    " + issuedDate + ", at the Office of the Punong Barangay"
                    + " of Barangay Bagong Sikat, Cabanatuan City, Republic of the Philippines.", mx + 10, y, pw - (mx + 10) * 2) + 20;

                // PERSONAL DETAILS section
                g2.setColor(new Color(0, 60, 150));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine(mx, y, pw - mx, y); y += 14;
                g2.setStroke(new BasicStroke(1f));

                g2.setFont(new Font("Serif", Font.BOLD, 11));
                g2.setColor(Color.BLACK);
                drawCentered(g2, "PERSONAL DETAILS", pw, y); y += 16;

                // Two-column personal details
                int col1x = mx + 10, col2x = pw / 2 + 10;
                int lineH = 16;
                g2.setFont(new Font("Serif", Font.PLAIN, 9));

                java.util.List<String[]> details = new java.util.ArrayList<>();
                // Address spans full width on its own line first
                g2.setFont(new Font("Serif", Font.BOLD, 9));
                g2.drawString("Name:", col1x, y);
                g2.setFont(new Font("Serif", Font.PLAIN, 9));
                g2.drawString(req.requesterName, col1x + 52, y); y += lineH;

                g2.setFont(new Font("Serif", Font.BOLD, 9));
                g2.drawString("Address:", col1x, y);
                g2.setFont(new Font("Serif", Font.PLAIN, 9));
                // Wrap address across full width
                int addrY = drawWrapped2(g2, address, col1x + 60, y - g2.getFontMetrics().getAscent(), pw - col1x - 60 - mx);
                y = Math.max(y + lineH, addrY + 4);

                details.add(new String[]{"Civil Status:", civil});
                details.add(new String[]{"Sex:", sex});
                details.add(new String[]{"Date of Birth:", dob});
                details.add(new String[]{"Place of Birth:", pob});
                details.add(new String[]{"Citizenship:", citizen.isEmpty() ? "Filipino" : citizen});
                details.add(new String[]{"Purpose:", purpose});

                int half = (details.size() + 1) / 2;
                for (int i = 0; i < half; i++) {
                    int rowY = y + i * lineH;
                    // Left column
                    g2.setFont(new Font("Serif", Font.BOLD, 9));
                    g2.drawString(details.get(i)[0], col1x, rowY);
                    g2.setFont(new Font("Serif", Font.PLAIN, 9));
                    g2.drawString(details.get(i)[1], col1x + 80, rowY);
                    // Right column
                    if (i + half < details.size()) {
                        g2.setFont(new Font("Serif", Font.BOLD, 9));
                        g2.drawString(details.get(i + half)[0], col2x, rowY);
                        g2.setFont(new Font("Serif", Font.PLAIN, 9));
                        g2.drawString(details.get(i + half)[1], col2x + 80, rowY);
                    }
                }
                y += half * lineH + 20;

                // Signature block
                g2.setColor(new Color(0, 60, 150));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine(mx, y, pw - mx, y); y += 20;
                g2.setStroke(new BasicStroke(1f));
                g2.setColor(Color.BLACK);

                // Prepared by (left) | Approved by (right)
                int sigLeft = mx + 20, sigRight = pw / 2 + 30;

                g2.setFont(new Font("Serif", Font.PLAIN, 9));
                g2.drawString("Prepared by:", sigLeft, y);
                g2.drawString("Approved by:", sigRight, y); y += 36;

                // Signature lines
                g2.drawLine(sigLeft, y, sigLeft + 150, y);
                g2.drawLine(sigRight, y, sigRight + 150, y); y += 12;

                g2.setFont(new Font("Serif", Font.BOLD, 10));
                String secName = secretary.isEmpty() ? "___________________" : secretary.toUpperCase();
                String capName = captain.isEmpty()   ? "___________________" : captain.toUpperCase();
                g2.drawString(secName, sigLeft, y);
                g2.drawString(capName, sigRight, y); y += 12;

                g2.setFont(new Font("Serif", Font.PLAIN, 9));
                g2.drawString("Barangay Secretary", sigLeft, y);
                g2.drawString("Punong Barangay", sigRight, y); y += 20;

                // Footer note
                g2.setFont(new Font("Serif", Font.ITALIC, 8));
                g2.setColor(Color.GRAY);
                drawCentered(g2, "Note: valid only for 6 months from the date issued. Not valid without dry seal.", pw, ph - 20);
                g2.setFont(new Font("Serif", Font.PLAIN, 8));
                g2.drawString("Request ID: #" + req.id, mx, ph - 20);

                return Printable.PAGE_EXISTS;
            }, pf);

            if (job.printDialog()) {
                try { job.print(); }
                catch (PrinterException ex) {
                    JOptionPane.showMessageDialog(this, "Print failed: " + ex.getMessage(),
                        "Print Error", JOptionPane.ERROR_MESSAGE);
                }
            }
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