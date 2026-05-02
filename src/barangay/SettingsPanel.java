package barangay;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class SettingsPanel extends JPanel {

    private final AppController ctrl;
    private final boolean isAdmin;

    public SettingsPanel(AppController ctrl, boolean isAdmin) {
        this.ctrl    = ctrl;
        this.isAdmin = isAdmin;
        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel bar = UITheme.headerBar("Settings");
        bar.setPreferredSize(new Dimension(0, 60));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        right.setOpaque(false);
        JButton logout = UITheme.logoutButton();
        logout.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(this), "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) ctrl.logout();
        });
        right.add(logout);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(UITheme.BG_MAIN);
        body.setBorder(new EmptyBorder(16, 24, 20, 24));
        body.setMinimumSize(new Dimension(600, 480));

        JButton backBtn = UITheme.backButton("Back to Dashboard");
        backBtn.addActionListener(e -> {
            if (isAdmin) ctrl.showAdminDashboard();
            else         ctrl.showGuestDashboard();
        });

        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        backRow.setOpaque(false);
        backRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        backRow.setAlignmentX(LEFT_ALIGNMENT);
        backRow.add(backBtn);

        User user = Database.getCurrentUser();
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

        // Profile card
        JPanel profileCard = UITheme.card();
        profileCard.setLayout(new GridBagLayout());
        profileCard.setAlignmentX(LEFT_ALIGNMENT);
        profileCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        GridBagConstraints gbc = defaultGbc();

        JTextField nameField    = UITheme.textField("Name");
        JTextField phoneField   = UITheme.textField("Phone No");
        JTextField emailField   = UITheme.textField("Email Address");
        JTextField addressField = UITheme.textField("Address");

        nameField.setFont(fieldFont);
        phoneField.setFont(fieldFont);
        emailField.setFont(fieldFont);
        addressField.setFont(fieldFont);

        nameField.setText(user.fullName);
        phoneField.setText(user.phone);
        emailField.setText(user.email);
        addressField.setText(user.address);

        // Row 0: Name | Phone
        gbc.gridx = 0; gbc.gridy = 0;
        profileCard.add(labeled("Name", nameField), gbc);
        gbc.gridx = 1;
        profileCard.add(labeled("Phone No", phoneField), gbc);

        // Row 1: Email | Address
        gbc.gridx = 0; gbc.gridy = 1;
        profileCard.add(labeled("Email Address", emailField), gbc);
        gbc.gridx = 1;
        profileCard.add(labeled("Address", addressField), gbc);

        // Row 2: Save button
        JButton saveBtn = UITheme.primaryButton("Save Changes");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.addActionListener(e -> {
            String name    = nameField.getText().trim();
            String phone   = phoneField.getText().trim();
            String email   = emailField.getText().trim();
            String address = addressField.getText().trim();
            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || address.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fields cannot be empty.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Database.updateProfile(name, phone, address, email);
            JOptionPane.showMessageDialog(this, "Profile updated!", "Success",
                JOptionPane.INFORMATION_MESSAGE);
        });
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        profileCard.add(saveBtn, gbc);

        // Password card
        JPanel passCard = UITheme.card();
        passCard.setLayout(new GridBagLayout());
        passCard.setAlignmentX(LEFT_ALIGNMENT);
        passCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        GridBagConstraints gbc2 = defaultGbc();

        JPasswordField currentPass = UITheme.passwordField();
        JPasswordField newPass     = UITheme.passwordField();
        JPasswordField confirmPass = UITheme.passwordField();
        currentPass.setFont(fieldFont);
        newPass.setFont(fieldFont);
        confirmPass.setFont(fieldFont);

        gbc2.gridx = 0; gbc2.gridy = 0;
        passCard.add(labeled("Current Password", currentPass), gbc2);
        gbc2.gridx = 1;
        passCard.add(labeled("New Password", newPass), gbc2);

        gbc2.gridx = 0; gbc2.gridy = 1;
        passCard.add(labeled("Confirm Password", confirmPass), gbc2);

        JButton updatePassBtn = UITheme.primaryButton("Update Password");
        updatePassBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        updatePassBtn.addActionListener(e -> {
            String cur     = new String(currentPass.getPassword());
            String newP    = new String(newPass.getPassword());
            String confirm = new String(confirmPass.getPassword());
            if (cur.isEmpty() || newP.isEmpty() || confirm.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All password fields are required.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!newP.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (newP.length() < 6) {
                JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            boolean ok = Database.updatePassword(cur, newP);
            if (!ok) {
                JOptionPane.showMessageDialog(this, "Current password is incorrect.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Password updated!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                currentPass.setText(""); newPass.setText(""); confirmPass.setText("");
            }
        });
        gbc2.gridx = 1; gbc2.gridy = 1;
        passCard.add(updatePassBtn, gbc2);

        body.add(backRow);
        body.add(Box.createVerticalStrut(14));
        body.add(profileCard);
        body.add(Box.createVerticalStrut(12));
        body.add(passCard);

        return body;
    }

    private GridBagConstraints defaultGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(6, 6, 6, 6);
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        return gbc;
    }

    private JPanel labeled(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(UITheme.TEXT_DARK);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }
}