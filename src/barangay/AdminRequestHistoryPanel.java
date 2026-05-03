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

        // Collect + validate helper
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
                    "Please fill in the following required fields:\n\u2022 "
                    + String.join("\n\u2022 ", missing2),
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

        //  Toolbar: zoom + buttons 
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

        //  Decorative outer frame 
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

        //  Header band 
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

        //  Gold rule 
        y = 120;
        g2.setColor(GOLD);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(mx, y, certW - mx, y);
        g2.setStroke(new BasicStroke(1f));
        y += 26;

        //  Certificate title 
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

        //  Body text 
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