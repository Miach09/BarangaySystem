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
        body.setMinimumSize(new Dimension(600, 500));

        JButton backBtn = UITheme.backButton("Back to Dashboard");
        backBtn.addActionListener(e -> ctrl.showGuestDashboard());

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        topRow.setOpaque(false);
        topRow.add(backBtn);

        // Request limit warning
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
                " pending requests. Please wait for your existing requests to be processed before submitting a new one.");
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

        JTextField nameField    = UITheme.textField("Full Name");
        JTextField contactField = UITheme.textField("Contact Number");
        JTextField emailField   = UITheme.textField("Email Address");
        JComboBox<String> typeBox = new JComboBox<>(REQUEST_TYPES);
        typeBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        typeBox.setPreferredSize(new Dimension(0, 40));

        JTextArea descArea = new JTextArea(4, 20);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UITheme.BORDER, 1),
            new EmptyBorder(6, 8, 6, 8)));

        // Pre-fill from current user and lock — only type and description are editable
        nameField.setText(user.fullName);
        contactField.setText(user.phone);
        emailField.setText(user.email);

        Color lockedBg = new Color(240, 242, 245);
        nameField.setEditable(false);
        nameField.setBackground(lockedBg);
        nameField.setForeground(new Color(90, 90, 100));
        nameField.setToolTipText("This is filled from your account info");
        contactField.setEditable(false);
        contactField.setBackground(lockedBg);
        contactField.setForeground(new Color(90, 90, 100));
        contactField.setToolTipText("This is filled from your account info");
        emailField.setEditable(false);
        emailField.setBackground(lockedBg);
        emailField.setForeground(new Color(90, 90, 100));
        emailField.setToolTipText("This is filled from your account info");

        // Small lock hint label
        JLabel lockHint = new JLabel("Name, contact and email are pulled from your account and cannot be changed here.");
        lockHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lockHint.setForeground(new Color(100, 100, 120));

        // Pending count info
        JLabel countLbl = new JLabel("Pending requests: " + pending + " / " + limit);
        countLbl.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        countLbl.setForeground(pending == limit - 1 ? new Color(180, 80, 0) : UITheme.TEXT_MUTED);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(lockHint, gbc);

        gbc.gridy = 1; gbc.gridwidth = 2;
        card.add(countLbl, gbc);

        gbc.gridy = 2; gbc.gridwidth = 2;
        card.add(labeled("Full Name", nameField), gbc);

        gbc.gridy = 3;
        card.add(labeled("Contact Number", contactField), gbc);

        gbc.gridy = 4;
        card.add(labeled("Email Address", emailField), gbc);

        gbc.gridy = 5;
        card.add(labeled("Request Type", typeBox), gbc);

        gbc.gridy = 6;
        card.add(labeled("Description", new JScrollPane(descArea) {{
            setBorder(null);
        }}), gbc);

        // Submit button
        JButton submitBtn = UITheme.primaryButton("Submit Request");
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridy = 7; gbc.gridwidth = 2;
        card.add(submitBtn, gbc);

        submitBtn.addActionListener(e -> {
            String name    = nameField.getText().trim();
            String contact = contactField.getText().trim();
            String email2  = emailField.getText().trim();
            String type    = (String) typeBox.getSelectedItem();
            String desc    = descArea.getText().trim();

            if (name.isEmpty() || contact.isEmpty() || email2.isEmpty() || desc.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to submit this request?\n\n"
                + "Type : " + type + "\nName : " + name,
                "Confirm Submission", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            CertRequest req = new CertRequest(name, contact, email2, type, desc);
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