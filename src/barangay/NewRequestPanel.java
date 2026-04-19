package barangay;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class NewRequestPanel extends JPanel {

    private final AppController ctrl;

    private static final String[] REQUEST_TYPES = {
        "Barangay Clearance Certificate",
        "Certificate of Indigency",
        "Certificate of Residency",
        "Barangay Business Permit"
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
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        right.setOpaque(false);
        JButton settings = UITheme.settingsButton();
        JButton logout   = UITheme.logoutButton();
        settings.addActionListener(e -> ctrl.showGuestSettings());
        logout.addActionListener(e -> ctrl.logout());
        right.add(settings);
        right.add(logout);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(UITheme.BG_MAIN);
        body.setBorder(new EmptyBorder(16, 24, 20, 24));
        body.setMinimumSize(new Dimension(600, 500));

        JButton backBtn = UITheme.backButton("Back to Dashboard");
        backBtn.addActionListener(e -> ctrl.showGuestDashboard());

        //Form card
        JPanel card = UITheme.card();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        User user = Database.getCurrentUser();

        JTextField nameField    = UITheme.textField("Full Name");
        JTextField contactField = UITheme.textField("Contact Number");
        JTextField emailField   = UITheme.textField("Email Address");
        JComboBox<String> typeBox = new JComboBox<>(REQUEST_TYPES);
        typeBox.setFont(UITheme.FONT_BODY);
        JTextArea descArea = new JTextArea(4, 20);
        descArea.setFont(UITheme.FONT_BODY);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UITheme.BORDER, 1),
            new EmptyBorder(6, 8, 6, 8)));
        descArea.setToolTipText("Provide details about your request...");

        //Pre-fill from current user
        nameField.setText(user.fullName);
        contactField.setText(user.phone);
        emailField.setText(user.email);

        //Row 0 – Full Name (full width)
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(labeled("Full Name", nameField), gbc);

        //Row 1 – Contact Number (full width)
        gbc.gridy = 1;
        card.add(labeled("Contact Number", contactField), gbc);

        //Row 2 – Email (full width)
        gbc.gridy = 2;
        card.add(labeled("Email Address", emailField), gbc);

        //Row 3 – Request Type (full width)
        gbc.gridy = 3;
        card.add(labeled("Request Type", typeBox), gbc);

        //Row 4 – Description (full width)
        gbc.gridy = 4;
        card.add(labeled("Description", new JScrollPane(descArea) {{
            setBorder(null);
        }}), gbc);

        //Row 5 - Submit button
        JButton submitBtn = UITheme.primaryButton("Submit Request");
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
                + "Type : " 
                + type + 
                "\nName : " + name,
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

        gbc.gridy = 5; gbc.gridwidth = 2;
        card.add(submitBtn, gbc);

        body.add(backBtn, BorderLayout.NORTH);
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
        lbl.setFont(UITheme.FONT_BODY);
        lbl.setForeground(UITheme.TEXT_DARK);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }
}