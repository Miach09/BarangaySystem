package barangay;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class RegisterPanel extends JPanel {

    private final AppController ctrl;

    public RegisterPanel(AppController ctrl) {
        this.ctrl = ctrl;
        setBackground(UITheme.BG_MAIN);
        setLayout(new GridBagLayout());
        add(buildCard());
    }

    private JPanel buildCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(40, 50, 40, 50)));
        card.setPreferredSize(new Dimension(500, 700));

        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(UITheme.TEXT_DARK);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Join the Barangay Portal", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(UITheme.TEXT_MUTED);
        sub.setAlignmentX(CENTER_ALIGNMENT);

        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 15);

        JTextField nameField    = UITheme.textField("First Name, Middle Initial, Last Name");
        JTextField emailField   = UITheme.textField("youremail@gmail.com");
        JTextField phoneField   = UITheme.textField("+63 9123456789");
        JTextField addressField = UITheme.textField("House No., Street, Barangay");
        JPasswordField passField = UITheme.passwordField();

        nameField.setFont(fieldFont);
        emailField.setFont(fieldFont);
        phoneField.setFont(fieldFont);
        addressField.setFont(fieldFont);
        passField.setFont(fieldFont);


        JPanel form = new JPanel(new GridLayout(5, 1, 0, 14));
        form.setOpaque(false);
        form.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        form.setAlignmentX(CENTER_ALIGNMENT);
        form.add(labeled("Full Name", nameField));
        form.add(labeled("Email Address", emailField));
        form.add(labeled("Phone No", phoneField));
        form.add(labeled("Address", addressField));
        form.add(labeled("Password", passField));

        JButton createBtn = UITheme.primaryButton("Create Account");
        JButton backBtn   = UITheme.primaryButton("Back to Login");
        createBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        createBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        backBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        createBtn.setAlignmentX(CENTER_ALIGNMENT);
        backBtn.setAlignmentX(CENTER_ALIGNMENT);

        createBtn.addActionListener(e -> {
            String name    = nameField.getText().trim();
            String email   = emailField.getText().trim();
            String phone   = phoneField.getText().trim();
            String address = addressField.getText().trim();
            String pass    = new String(passField.getPassword());

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty()
                    || address.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!email.contains("@")) {
                JOptionPane.showMessageDialog(this, "Enter a valid email address.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (pass.length() < 6) {
                JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            boolean ok = Database.register(name, email, phone, address, pass);
            if (!ok) {
                JOptionPane.showMessageDialog(this, "Email already registered.",
                    "Registration Failed", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Account created! You can now log in.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                ctrl.showLogin();
            }
        });

        backBtn.addActionListener(e -> ctrl.showLogin());

        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(sub);
        card.add(Box.createVerticalStrut(24));
        card.add(form);
        card.add(Box.createVerticalStrut(20));
        card.add(createBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(backBtn);

        return card;
    }

    private JPanel labeled(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(UITheme.TEXT_DARK);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }
}