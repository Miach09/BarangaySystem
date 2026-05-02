package barangay;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class NewRequestPanel extends JPanel {

    private final AppController ctrl;

    private static final String[] REQUEST_TYPES = {
        "Barangay Clearance Application",
        "Certificate of Indigency",
        "Certificate of Residency",
        "Barangay Business Clearance"
    };

    private static final String[] CIVIL_STATUSES = {
        "Single", "Married", "Widowed", "Legally Separated"
    };

    private static final String[] SEXES = { "Male", "Female" };

    public NewRequestPanel(AppController ctrl) {
        this.ctrl = ctrl;
        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel bar = UITheme.headerBar("New Request");
        bar.setPreferredSize(new Dimension(0, 60));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        right.setOpaque(false);
        JButton settings = UITheme.settingsButton();
        JButton logout   = UITheme.logoutButton();
        settings.addActionListener(e -> ctrl.showGuestSettings());
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
        body.setBorder(new EmptyBorder(16, 24, 20, 24));
        body.setMinimumSize(new Dimension(600, 500));

        JButton backBtn = UITheme.backButton("Back to Dashboard");
        backBtn.addActionListener(e -> ctrl.showGuestDashboard());

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        topRow.setOpaque(false);
        topRow.add(backBtn);

        // Request limit check
        User user = Database.getCurrentUser();
        int pending = Database.countPendingForUser(user.email);
        int limit   = Database.MAX_PENDING_REQUESTS;

        if (pending >= limit) {
            JPanel limitCard = new JPanel(new BorderLayout());
            limitCard.setBackground(new Color(255, 240, 200));
            limitCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 160, 0), 1, true),
                new EmptyBorder(12, 16, 12, 16)));
            JLabel limitLbl = new JLabel(
                "You have reached the maximum of " + limit +
                " pending requests. Please wait for your existing requests to be processed.");
            limitLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            limitLbl.setForeground(new Color(120, 70, 0));
            limitCard.add(limitLbl, BorderLayout.CENTER);
            body.add(topRow, BorderLayout.NORTH);
            body.add(limitCard, BorderLayout.CENTER);
            return body;
        }

        // Form card
        JPanel card = UITheme.card();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(6, 6, 6, 6);
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);
        Color lockedBg = new Color(240, 242, 245);
        Color lockedFg = new Color(90, 90, 100);

        // Locked fields (from account)
        JTextField nameField    = UITheme.textField("Full Name");
        JTextField contactField = UITheme.textField("Contact Number");
        JTextField emailField   = UITheme.textField("Email Address");

        nameField.setText(user.fullName);
        contactField.setText(user.phone);
        emailField.setText(user.email);

        for (JTextField f : new JTextField[]{nameField, contactField, emailField}) {
            f.setEditable(false);
            f.setBackground(lockedBg);
            f.setForeground(lockedFg);
            f.setToolTipText("Pulled from your account info");
        }

        // Editable fields by guest
        JComboBox<String> typeBox        = new JComboBox<>(REQUEST_TYPES);
        JComboBox<String> civilStatusBox = new JComboBox<>(CIVIL_STATUSES);
        JComboBox<String> sexBox         = new JComboBox<>(SEXES);

        typeBox.setFont(fieldFont);
        civilStatusBox.setFont(fieldFont);
        sexBox.setFont(fieldFont);

        JTextField birthdateField  = UITheme.textField("e.g. January 1, 2000");
        JTextField birthplaceField = UITheme.textField("e.g. Cabanatuan City, Nueva Ecija");

        JTextArea descArea = new JTextArea(3, 20);
        descArea.setFont(fieldFont);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UITheme.BORDER, 1),
            new EmptyBorder(6, 8, 6, 8)));

        // Lock hint + count
        JLabel lockHint = new JLabel("Name, contact and email are pulled from your account.");
        lockHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lockHint.setForeground(new Color(100, 100, 120));

        JLabel countLbl = new JLabel("Pending requests: " + pending + " / " + limit);
        countLbl.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        countLbl.setForeground(pending == limit - 1 ? new Color(180, 80, 0) : UITheme.TEXT_MUTED);

        // Layout
        int row = 0;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        card.add(lockHint, gbc); row++;

        gbc.gridy = row; gbc.gridwidth = 2;
        card.add(countLbl, gbc); row++;

        // Row: Full Name (full width)
        gbc.gridy = row; gbc.gridwidth = 2;
        card.add(labeled("Full Name", nameField), gbc); row++;

        // Row: Contact | Email (side by side)
        gbc.gridwidth = 1; gbc.weightx = 0.5;
        gbc.gridx = 0; gbc.gridy = row;
        card.add(labeled("Contact Number", contactField), gbc);
        gbc.gridx = 1;
        card.add(labeled("Email Address", emailField), gbc);
        gbc.weightx = 1.0; row++;

        // Row: Civil Status | Sex
        gbc.gridwidth = 1; gbc.weightx = 0.5;
        gbc.gridx = 0; gbc.gridy = row;
        card.add(labeled("Civil Status", civilStatusBox), gbc);
        gbc.gridx = 1;
        card.add(labeled("Sex", sexBox), gbc);
        gbc.weightx = 1.0; row++;

        // Row: Birthdate | Birthplace
        gbc.gridwidth = 1; gbc.weightx = 0.5;
        gbc.gridx = 0; gbc.gridy = row;
        card.add(labeled("Birthdate", birthdateField), gbc);
        gbc.gridx = 1;
        card.add(labeled("Birthplace", birthplaceField), gbc);
        gbc.weightx = 1.0; row++;

        // Row: Request Type (full width)
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        card.add(labeled("Request Type", typeBox), gbc); row++;

        // Row: Description (full width)
        gbc.gridy = row; gbc.gridwidth = 2;
        card.add(labeled("Description / Reason for Request", new JScrollPane(descArea) {{
            setBorder(null);
        }}), gbc); row++;

        // Submit button
        JButton submitBtn = UITheme.primaryButton("Submit Request");
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridy = row; gbc.gridwidth = 2;
        card.add(submitBtn, gbc);

        submitBtn.addActionListener(e -> {
            String name      = nameField.getText().trim();
            String contact   = contactField.getText().trim();
            String email2    = emailField.getText().trim();
            String type      = (String) typeBox.getSelectedItem();
            String civil     = (String) civilStatusBox.getSelectedItem();
            String sx        = (String) sexBox.getSelectedItem();
            String birthdate = birthdateField.getText().trim();
            String birthplace= birthplaceField.getText().trim();
            String desc      = descArea.getText().trim();

            if (birthdate.isEmpty() || birthplace.isEmpty() || desc.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Please fill in Birthdate, Birthplace, and Description.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to submit this request?\n\n"
                + "Type   : " + type + "\nName   : " + name,
                "Confirm Submission", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            CertRequest req = new CertRequest(name, contact, email2, type, desc);
            req.civilStatus = civil;
            req.sex         = sx;
            req.birthdate   = birthdate;
            req.birthplace  = birthplace;

            Database.addRequest(req);
            JOptionPane.showMessageDialog(this,
                "Request submitted successfully!\nRequest ID: #" + req.id,
                "Success", JOptionPane.INFORMATION_MESSAGE);
            ctrl.showGuestDashboard();
        });

        body.add(topRow, BorderLayout.NORTH);
        body.add(new JScrollPane(card) {{
            setBorder(null);
            setOpaque(false);
            getViewport().setOpaque(false);
        }}, BorderLayout.CENTER);

        return body;
    }

    private JPanel labeled(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(UITheme.TEXT_DARK);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }
}